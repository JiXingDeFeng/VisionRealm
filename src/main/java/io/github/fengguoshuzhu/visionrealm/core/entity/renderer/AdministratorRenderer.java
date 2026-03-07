package io.github.fengguoshuzhu.visionrealm.core.entity.renderer;

import io.github.fengguoshuzhu.visionrealm.core.entity.client.model.AdministratorModel;
import io.github.fengguoshuzhu.visionrealm.core.entity.custom.AdministratorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AdministratorRenderer extends MobRenderer<AdministratorEntity, AdministratorModel<AdministratorEntity>> {
    public AdministratorRenderer(EntityRendererProvider.Context context) {
        super(context, new AdministratorModel<>(context.bakeLayer(AdministratorModel.LAYER_LOCATION)), 0.3F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull AdministratorEntity entity) {
        return AdministratorEntity.TEXTURES[entity.getTextureId()];
    }
}
