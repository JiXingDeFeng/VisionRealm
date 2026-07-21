package io.github.jixingdefeng.visionrealm.core.registry;

import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModRegistry {
    public static final Registry<RegisteredIncident<?, ?>> INCIDENT = new RegistryBuilder<>(ModRegistries.INCIDENT)
            .sync(false)
            .create();

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(INCIDENT);
    }

    @SubscribeEvent
    public static void registryDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                ModRegistries.EROSION_TYPE,
                ErosionType.CODEC,
                ErosionType.CODEC
        );
        event.dataPackRegistry(
                ModRegistries.PARTICLE_CONFIG,
                ParticleConfig.CODEC
        );
    }
}
