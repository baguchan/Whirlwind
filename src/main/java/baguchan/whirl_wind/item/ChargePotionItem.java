package baguchan.whirl_wind.item;

import baguchan.whirl_wind.entity.ChargePotion;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;

public class ChargePotionItem extends PotionItem {
    public ChargePotionItem(Item.Properties p_43301_) {
        super(p_43301_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_43303_, Player p_43304_, InteractionHand p_43305_) {
        ItemStack itemstack = p_43304_.getItemInHand(p_43305_);
        if (!p_43303_.isClientSide) {
            ChargePotion thrownpotion = new ChargePotion(p_43303_, p_43304_);
            thrownpotion.setItem(itemstack);
            thrownpotion.shootFromRotation(p_43304_, p_43304_.getXRot(), p_43304_.getYRot(), -20.0F, 0.5F, 1.0F);
            p_43303_.addFreshEntity(thrownpotion);
        }

        p_43304_.awardStat(Stats.ITEM_USED.get(this));
        if (!p_43304_.getAbilities().instabuild) {
            itemstack.shrink(1);
        }
        p_43303_.playSound(
                null,
                p_43304_.getX(),
                p_43304_.getY(),
                p_43304_.getZ(),
                SoundEvents.LINGERING_POTION_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (p_43303_.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        return InteractionResultHolder.sidedSuccess(itemstack, p_43303_.isClientSide());
    }

    @Override
    public String getDescriptionId(ItemStack p_43003_) {
        return this.getOrCreateDescriptionId();
    }
}
