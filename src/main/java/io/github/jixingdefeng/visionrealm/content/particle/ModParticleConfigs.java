package io.github.jixingdefeng.visionrealm.content.particle;

import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistry;
import io.github.jixingdefeng.visionrealm.core.particle_config.EmptyParticleConfig;
import io.github.jixingdefeng.visionrealm.core.particle_config.list.WeightedParticleConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticleConfigs {
    public static final DeferredRegister<MapCodec<? extends ParticleConfig>> REGISTER = DeferredRegister.create(ModRegistry.PARTICLE_CONFIG_TYPE, VisionRealm.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ParticleConfig>, MapCodec<EmptyParticleConfig>> EMPTY = REGISTER.register(
            "empty",
            () -> EmptyParticleConfig.MAP_CODEC
    );
    public static final DeferredHolder<MapCodec<? extends ParticleConfig>, MapCodec<SingletonParticleConfig>> DEFAULT = REGISTER.register(
            "default",
            () -> SingletonParticleConfig.MAP_CODEC
    );
    public static final DeferredHolder<MapCodec<? extends ParticleConfig>, MapCodec<WeightedParticleConfig>> WEIGHTED = REGISTER.register(
            "weighted",
            () -> WeightedParticleConfig.MAP_CODEC
    );

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
    }
}
