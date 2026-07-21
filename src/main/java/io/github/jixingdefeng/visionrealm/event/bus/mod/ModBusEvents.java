package io.github.jixingdefeng.visionrealm.event.bus.mod;

import io.github.jixingdefeng.visionrealm.api.event.particle.RegistryParticleConfigTypeEvent;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.event.bus.mod.entity.EntityRegisterEvents;
import io.github.jixingdefeng.visionrealm.event.bus.mod.particle.ParticleEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

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
    public static void registryParticleConfig(final RegistryParticleConfigTypeEvent event) {
        ParticleEvents.registryParticleConfig(event);
    }
}
