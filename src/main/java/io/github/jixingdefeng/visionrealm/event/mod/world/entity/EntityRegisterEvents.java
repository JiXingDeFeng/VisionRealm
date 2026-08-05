package io.github.jixingdefeng.visionrealm.event.mod.world.entity;

import io.github.jixingdefeng.visionrealm.client.entity.model.AdministratorModel;
import io.github.jixingdefeng.visionrealm.client.entity.model.monster.ForgottenShadowModel;
import io.github.jixingdefeng.visionrealm.client.entity.model.monster.NightmareApostleModel;
import io.github.jixingdefeng.visionrealm.content.world.entity.ModEntities;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.Administrator;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.ForgottenShadow;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.NightmareApostle;
import io.github.jixingdefeng.visionrealm.content.world.level.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class EntityRegisterEvents {

    public static void registryLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AdministratorModel.LAYER_LOCATION, AdministratorModel::createBodyLayer);
        event.registerLayerDefinition(ForgottenShadowModel.LAYER_LOCATION, ForgottenShadowModel::createBodyLayer);
        event.registerLayerDefinition(NightmareApostleModel.LAYER_LOCATION, NightmareApostleModel::createBodyLayer);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ADMINISTRATOR.get(), Administrator.createAttributes().build());
        event.put(ModEntities.FORGOTTEN_SHADOW.get(), ForgottenShadow.createAttributes().build());
        event.put(ModEntities.NIGHTMARE_APOSTLE.get(), NightmareApostle.createAttributes().build());
    }

    public static void addSignBlockEvents(final BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BlockEntityType.SIGN,
                ModBlocks.BLOOD_CORRODED_CHERRY_SIGN.get(),
                ModBlocks.BLOOD_CORRODED_CHERRY_WALL_SIGN.get()
        );
        event.modify(
                BlockEntityType.HANGING_SIGN,
                ModBlocks.BLOOD_CORRODED_CHERRY_HANGING_SIGN.get(),
                ModBlocks.BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN.get()
        );
    }
}
