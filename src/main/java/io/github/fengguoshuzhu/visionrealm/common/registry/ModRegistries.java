package io.github.fengguoshuzhu.visionrealm.common.registry;

import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistries {
    public static final DeferredRegister<Registry<?>> REGISTRY = DeferredRegister.create(Registries.ROOT_REGISTRY_NAME, VisionRealm.MOD_ID);

    public static void registry(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static void registryDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
    }
}
