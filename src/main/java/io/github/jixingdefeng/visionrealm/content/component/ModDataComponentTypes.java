package io.github.jixingdefeng.visionrealm.content.component;

import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponentTypes {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, VisionRealm.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY = COMPONENTS.registerComponentType(
            "energy",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
