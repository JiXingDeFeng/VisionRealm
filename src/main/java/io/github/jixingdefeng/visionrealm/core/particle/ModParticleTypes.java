package io.github.jixingdefeng.visionrealm.core.particle;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, VisionRealm.MOD_ID);

    public static final Supplier<SimpleParticleType> ERROR_PARTICLE_1 = PARTICLE_TYPES.register(
            "error_particle_1",
            () -> new SimpleParticleType(false)
    );
    public static final Supplier<SimpleParticleType> BLOOD_CORRODED_CHERRY = PARTICLE_TYPES.register(
            "blood_corroded_cherry",
            () -> new SimpleParticleType(true)
    );

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
