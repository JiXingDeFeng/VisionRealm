package io.github.jixingdefeng.visionrealm.core.registry;

import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModRegistries {
    public static final Registry<RegisteredIncident<?, ?>> INCIDENT = new RegistryBuilder<>(ModRegistryKeys.INCIDENT)
            .sync(true)
            .create();

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(INCIDENT);
    }

    public static void registryDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
    }
}
