package baguchan.wild_gale.client.render;

import baguchan.wild_gale.WhirlWindMod;
import baguchan.wild_gale.client.ModModelLayers;
import baguchan.wild_gale.client.render.layer.WindLayer;
import baguchan.wild_gale.client.render.model.WhirlWindModel;
import baguchan.wild_gale.entity.WhirlWind;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class WhirlWindRender<T extends WhirlWind> extends MobRenderer<T, WhirlWindModel<T>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(WhirlWindMod.MODID, "textures/entity/whirl_wind.png");
    private static final RenderType EYES = RenderType.eyes(new ResourceLocation(WhirlWindMod.MODID, "textures/entity/whirl_wind_eye.png"));

    public WhirlWindRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new WhirlWindModel<>(renderManagerIn.bakeLayer(ModModelLayers.WHIRLWIND)), 0.5F);
        this.addLayer(new WindLayer<>(this, renderManagerIn.getModelSet()));
        this.addLayer(new EyesLayer<T, WhirlWindModel<T>>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return TEXTURE;
    }
}