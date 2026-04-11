package io.github.jixingdefeng.visionrealm.event.bus.mod.entity;

import io.github.jixingdefeng.visionrealm.core.entity.Entities;
import io.github.jixingdefeng.visionrealm.core.entity.client.model.AdministratorModel;
import io.github.jixingdefeng.visionrealm.core.entity.client.model.TheForgottenModel;
import io.github.jixingdefeng.visionrealm.core.entity.custom.AdministratorEntity;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.TheForgottenEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class EntityRegisterEvents {

    public static void registryLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AdministratorModel.LAYER_LOCATION, AdministratorModel::createBodyLayer);
        event.registerLayerDefinition(TheForgottenModel.LAYER_LOCATION, TheForgottenModel::createBodyLayer);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(Entities.ADMINISTRATOR.get(), AdministratorEntity.createMobAttributes().build());
        event.put(Entities.WANDERER.get(), TheForgottenEntity.createMobAttributes().build());
    }
}
