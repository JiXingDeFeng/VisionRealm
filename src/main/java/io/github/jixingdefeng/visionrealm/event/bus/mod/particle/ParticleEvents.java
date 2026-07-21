package io.github.jixingdefeng.visionrealm.event.bus.mod.particle;

import io.github.jixingdefeng.visionrealm.api.event.particle.RegistryParticleConfigTypeEvent;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.impl.particle.EmptyParticleConfig;
import io.github.jixingdefeng.visionrealm.impl.particle.list.WeightedParticleConfig;
import io.github.jixingdefeng.visionrealm.impl.particle.singleton.ImmutableParticleConfig;
import net.neoforged.bus.api.SubscribeEvent;

public class ParticleEvents {

    @SubscribeEvent
    public static void registryParticleConfig(RegistryParticleConfigTypeEvent event) {
        event.register(ImmutableParticleConfig.type, SingletonParticleConfig.CODEC);
        event.register(WeightedParticleConfig.type, WeightedParticleConfig.CODEC);
        event.register(EmptyParticleConfig.INSTANCE.getType(), EmptyParticleConfig.MAP_CODEC);
    }
}
