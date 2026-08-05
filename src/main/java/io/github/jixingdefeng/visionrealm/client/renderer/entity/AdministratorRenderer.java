package io.github.jixingdefeng.visionrealm.client.renderer.entity;

import io.github.jixingdefeng.visionrealm.client.entity.model.AdministratorModel;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.Administrator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
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
