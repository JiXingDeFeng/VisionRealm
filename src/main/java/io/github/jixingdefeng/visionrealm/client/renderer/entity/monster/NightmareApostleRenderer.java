package io.github.jixingdefeng.visionrealm.client.renderer.entity.monster;

import io.github.jixingdefeng.visionrealm.client.entity.model.AdministratorModel;
import io.github.jixingdefeng.visionrealm.client.entity.model.monster.NightmareApostleModel;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.Administrator;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.NightmareApostle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class NightmareApostleRenderer extends LivingEntityRenderer<NightmareApostle, NightmareApostleModel<NightmareApostle>> {
    public NightmareApostleRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareApostleModel<>(context.bakeLayer(AdministratorModel.LAYER_LOCATION)), 0.3F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull NightmareApostle entity) {
        return Administrator.TEXTURE;
    }
}
