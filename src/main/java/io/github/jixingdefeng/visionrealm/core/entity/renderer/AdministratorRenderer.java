package io.github.jixingdefeng.visionrealm.core.entity.renderer;

import io.github.jixingdefeng.visionrealm.core.entity.client.model.AdministratorModel;
import io.github.jixingdefeng.visionrealm.core.entity.custom.Administrator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AdministratorRenderer extends MobRenderer<Administrator, AdministratorModel<Administrator>> {
    public AdministratorRenderer(EntityRendererProvider.Context context) {
        super(context, new AdministratorModel<>(context.bakeLayer(AdministratorModel.LAYER_LOCATION)), 0.3F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull Administrator entity) {
        return Administrator.TEXTURE;
    }
}
