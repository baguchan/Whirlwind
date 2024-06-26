package baguchan.whirl_wind.entity.behavior;

import baguchan.whirl_wind.registry.ModMemorys;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.phys.Vec3;

public class ShootGust extends Behavior<Breeze> {
    private static final int ATTACK_RANGE_MIN_SQRT = 4;
    private static final int ATTACK_RANGE_MAX_SQRT = 256;
    private static final int UNCERTAINTY_BASE = 5;
    private static final int UNCERTAINTY_MULTIPLIER = 4;
    private static final float PROJECTILE_MOVEMENT_SCALE = 0.7F;
    private static final int SHOOT_INITIAL_DELAY_TICKS = Math.round(15.0F);
    private static final int SHOOT_RECOVER_DELAY_TICKS = Math.round(10.0F);
    private static final int SHOOT_COOLDOWN_TICKS = Math.round(35.0F);
    private static final int SHOOT_REMAIN_DELAY_TICKS = Math.round(60.0F);

    @VisibleForTesting
    public ShootGust() {
        super(
                ImmutableMap.of(
                        MemoryModuleType.ATTACK_TARGET,
                        MemoryStatus.VALUE_PRESENT,
                        MemoryModuleType.BREEZE_SHOOT_COOLDOWN,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.BREEZE_SHOOT_CHARGING,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.BREEZE_SHOOT_RECOVERING,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.BREEZE_SHOOT,
                        MemoryStatus.VALUE_PRESENT,
                        MemoryModuleType.WALK_TARGET,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.BREEZE_JUMP_TARGET,
                        MemoryStatus.VALUE_ABSENT
                ),
                SHOOT_INITIAL_DELAY_TICKS + 1 + SHOOT_RECOVER_DELAY_TICKS
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel p_312041_, Breeze p_312169_) {
        return p_312169_.getPose() != Pose.STANDING
                ? false
                : p_312169_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(p_312632_ -> isTargetWithinRange(p_312169_, p_312632_)).map(p_312737_ -> {
            if (!p_312737_) {
                p_312169_.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
            }

            return p_312737_;
        }).orElse(false);
    }

    protected boolean canStillUse(ServerLevel p_312535_, Breeze p_312174_, long p_311812_) {
        return p_312174_.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && p_312174_.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_SHOOT);
    }

    protected void start(ServerLevel p_311932_, Breeze p_312618_, long p_311781_) {
        p_312618_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(p_312833_ -> p_312618_.setPose(Pose.SHOOTING));
        p_312618_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, (long) SHOOT_INITIAL_DELAY_TICKS);
        p_312618_.getBrain().setMemory(ModMemorys.BREEZE_SHOOT_REMAIN.get(), 2);
        p_312618_.playSound(SoundEvents.BREEZE_INHALE, 1.0F, 1.0F);
    }

    protected void stop(ServerLevel p_312137_, Breeze p_311803_, long p_312309_) {
        if (p_311803_.getPose() == Pose.SHOOTING) {
            p_311803_.setPose(Pose.STANDING);
        }

        p_311803_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, (long) SHOOT_COOLDOWN_TICKS);
        p_311803_.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
    }

    protected void tick(ServerLevel p_312907_, Breeze p_312605_, long p_312804_) {
        Brain<Breeze> brain = p_312605_.getBrain();
        LivingEntity livingentity = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (livingentity != null) {
            p_312605_.lookAt(EntityAnchorArgument.Anchor.EYES, livingentity.position());
            if (!brain.hasMemoryValue(ModMemorys.BREEZE_SHOOT_REMAIN_COOLDOWN.get()) || brain.getMemory(ModMemorys.BREEZE_SHOOT_REMAIN_COOLDOWN.get()).get() <= 0) {
                if (!brain.getMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() && !brain.getMemory(MemoryModuleType.BREEZE_SHOOT_RECOVERING).isPresent()) {


                    if (isFacingTarget(p_312605_, livingentity)) {
                        double d0 = livingentity.getX() - p_312605_.getX();
                        double d1 = livingentity.getY(0.3) - p_312605_.getY(0.5);
                        double d2 = livingentity.getZ() - p_312605_.getZ();
                        WindCharge windcharge = new WindCharge(EntityType.WIND_CHARGE, p_312907_);
                        p_312605_.playSound(SoundEvents.BREEZE_SHOOT, 1.5F, 1.0F);
                        windcharge.setOwner(p_312605_);
                        windcharge.setPos(p_312605_.getEyePosition());
                        windcharge.shoot(d0, d1, d2, 0.4F + brain.getMemory(ModMemorys.BREEZE_SHOOT_REMAIN.get()).get() * 0.1F, (float) (5 - p_312907_.getDifficulty().getId() * 4));
                        p_312907_.addFreshEntity(windcharge);
                    }
                    if (!brain.getMemory(ModMemorys.BREEZE_SHOOT_REMAIN.get()).isPresent() || brain.getMemory(ModMemorys.BREEZE_SHOOT_REMAIN.get()).get() <= 0) {
                        brain.setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_RECOVERING, Unit.INSTANCE, (long) SHOOT_RECOVER_DELAY_TICKS);

                    } else {
                        if (brain.getMemory(ModMemorys.BREEZE_SHOOT_REMAIN.get()).get() > 0) {
                            brain.setMemory(ModMemorys.BREEZE_SHOOT_REMAIN.get(), brain.getMemory(ModMemorys.BREEZE_SHOOT_REMAIN.get()).get() - 1);
                            brain.setMemory(ModMemorys.BREEZE_SHOOT_REMAIN_COOLDOWN.get(), 3);
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    public static boolean isFacingTarget(Breeze p_311845_, LivingEntity p_312453_) {
        Vec3 vec3 = p_311845_.getViewVector(1.0F);
        Vec3 vec31 = p_312453_.position().subtract(p_311845_.position()).normalize();
        return vec3.dot(vec31) > 0.5;
    }

    private static boolean isTargetWithinRange(Breeze p_312114_, LivingEntity p_312647_) {
        double d0 = p_312114_.position().distanceToSqr(p_312647_.position());
        return d0 > 4.0 && d0 < 256.0;
    }
}
