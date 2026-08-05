package io.github.jixingdefeng.visionrealm.event.mod;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.event.mod.data.DataEvents;
import io.github.jixingdefeng.visionrealm.event.mod.world.entity.EntityRegisterEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = VisionRealm.MOD_ID)
public class ModBusEvents {

    @SubscribeEvent
    public static void registryLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        EntityRegisterEvents.registryLayers(event);
    }

    @SubscribeEvent
    public static void registerAttributes(final EntityAttributeCreationEvent event) {
        EntityRegisterEvents.registerAttributes(event);
    }

    @SubscribeEvent
    public static void onRegisterDataMapTypesEvent(final RegisterDataMapTypesEvent event) {
        DataEvents.registerDataMapTypes(event);
    }

    @SubscribeEvent
    public static void addBlockEntityTypeEvent(final BlockEntityTypeAddBlocksEvent event) {
        EntityRegisterEvents.addSignBlockEvents(event);
    }
}
