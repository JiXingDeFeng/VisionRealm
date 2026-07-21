package io.github.jixingdefeng.visionrealm.core.entity.renderer;

import io.github.jixingdefeng.visionrealm.core.entity.client.model.AdministratorModel;
import io.github.jixingdefeng.visionrealm.core.entity.client.model.NightmareApostleModel;
import io.github.jixingdefeng.visionrealm.core.entity.custom.Administrator;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.NightmareApostle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class NightmareApostleRenderer extends MobRenderer<NightmareApostle, NightmareApostleModel<NightmareApostle>> {
    public NightmareApostleRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareApostleModel<>(context.bakeLayer(AdministratorModel.LAYER_LOCATION)), 0.3F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull NightmareApostle entity) {
        return Administrator.TEXTURE;
    }
}
