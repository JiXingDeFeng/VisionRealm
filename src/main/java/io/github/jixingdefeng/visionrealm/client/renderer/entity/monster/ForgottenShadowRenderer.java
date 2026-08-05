package io.github.jixingdefeng.visionrealm.client.renderer.entity.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jixingdefeng.visionrealm.api.client.render.entity.EntityRenderRedirector;
import io.github.jixingdefeng.visionrealm.client.entity.model.monster.ForgottenShadowModel;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.ForgottenShadow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ForgottenShadowRenderer extends MobRenderer<ForgottenShadow, ForgottenShadowModel<ForgottenShadow>>
        implements EntityRenderRedirector<ForgottenShadow, ForgottenShadowModel<ForgottenShadow>> {
    public ForgottenShadowRenderer(EntityRendererProvider.Context context) {
        super(context, new ForgottenShadowModel<>(context.bakeLayer(ForgottenShadowModel.LAYER_LOCATION)), 0.0F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull ForgottenShadow entity) {
        return ForgottenShadow.TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(@NotNull ForgottenShadow livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.itemEntityTranslucentCull(this.getTextureLocation(livingEntity));
    }

    @Override
    public void renderToBuffer(
            @NotNull ForgottenShadowModel<ForgottenShadow> model,
            @NotNull ForgottenShadow entity,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        int colorTint = entity.hurtTime <= 0 ? 0x00202020 : 0x00FF0000;
        int alpha = 0x60;

        int brightness = this.getBrightness(entity, packedLight);
        int maxBrightness = entity.getMaxAcceptBrightness();
        if (entity.isVisibleInDarkness(Minecraft.getInstance().player) && brightness < maxBrightness) {
            float percent = Math.max(0.1F, brightness / (float) maxBrightness);
            alpha = (int) (alpha * percent);
        }

        color = alpha << 24 | (colorTint & color);
        EntityRenderRedirector.super.renderToBuffer(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    protected int getBrightness(ForgottenShadow entity, int packedLight) {
        if (entity.level() instanceof ClientLevel level) {
            ClientLevel.ClientLevelData data = level.getLevelData();
            int blockLight = (packedLight & 0xFFFF) >> 4;
            int skyLight = ((packedLight >> 16) & 0xFFFF) >> 4;
            long dayTime = data.getDayTime() % 24000;
            if (data.isRaining()) {
                skyLight /= 5;
            } else if (dayTime >= 12000) {
                if (dayTime < 12100) {
                    skyLight = (int) (skyLight * ((12100 - dayTime) * 0.01));
                } else {
                    skyLight = 0;
                }
            } else if (dayTime < 100) {
                skyLight = (int) (skyLight * (dayTime * 0.002));
            }

            return Math.max(skyLight, blockLight);
        } else {
            return 0;
        }
    }
}
