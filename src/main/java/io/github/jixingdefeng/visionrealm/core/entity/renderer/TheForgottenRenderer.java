package io.github.jixingdefeng.visionrealm.core.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jixingdefeng.visionrealm.api.client.render.entity.EntityRenderRedirector;
import io.github.jixingdefeng.visionrealm.core.entity.client.model.TheForgottenModel;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.TheForgottenEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheForgottenRenderer extends MobRenderer<TheForgottenEntity, TheForgottenModel<TheForgottenEntity>> implements EntityRenderRedirector<TheForgottenEntity> {
    public TheForgottenRenderer(EntityRendererProvider.Context context) {
        super(context, new TheForgottenModel<>(context.bakeLayer(TheForgottenModel.LAYER_LOCATION)), 0.0F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull TheForgottenEntity entity) {
        return TheForgottenEntity.TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(@NotNull TheForgottenEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.itemEntityTranslucentCull(this.getTextureLocation(livingEntity));
    }

    @Override
    public void redirectRenderToBuffer(
            @NotNull EntityModel<TheForgottenEntity> model,
            @NotNull TheForgottenEntity entity,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        int alpha = 0x38;
        int constant = entity.hurtTime <= 0 ? 0x00202020 : 0x00FF2020;
        if (entity.isVanishing()) {
            int vanishTimer = Math.max(entity.getVanishTimer(), 0);
            float progress = (float) vanishTimer / entity.getMaxVanishTime();
            alpha = vanishTimer <= 0.0F ? 0x00 : (int) (alpha * progress);
        }

        color = alpha << 24 | (constant & color);
        EntityRenderRedirector.super.redirectRenderToBuffer(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
