package io.github.jixingdefeng.visionrealm.event.bus.mod.entity;

import io.github.jixingdefeng.visionrealm.core.entity.ModEntities;
import io.github.jixingdefeng.visionrealm.core.entity.client.model.AdministratorModel;
import io.github.jixingdefeng.visionrealm.core.entity.client.model.ForgottenShadowModel;
import io.github.jixingdefeng.visionrealm.core.entity.client.model.NightmareApostleModel;
import io.github.jixingdefeng.visionrealm.core.entity.custom.Administrator;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.ForgottenShadow;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.NightmareApostle;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class EntityRegisterEvents {

    public static void registryLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AdministratorModel.LAYER_LOCATION, AdministratorModel::createBodyLayer);
        event.registerLayerDefinition(ForgottenShadowModel.LAYER_LOCATION, ForgottenShadowModel::createBodyLayer);
        event.registerLayerDefinition(NightmareApostleModel.LAYER_LOCATION, NightmareApostleModel::createBodyLayer);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ADMINISTRATOR.get(), Administrator.createMobAttributes().build());
        event.put(ModEntities.FORGOTTEN_SHADOW.get(), ForgottenShadow.createMobAttributes().build());
        event.put(ModEntities.NIGHTMARE_APOSTLE.get(), NightmareApostle.createMobAttributes().build());
    }
}
