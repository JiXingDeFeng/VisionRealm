package io.github.jixingdefeng.visionrealm.common.util.particle;

import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.impl.particle.EmptyParticleConfig;
import io.github.jixingdefeng.visionrealm.impl.particle.singleton.ImmutableParticleConfig;
import net.minecraft.core.particles.ParticleTypes;

public final class ParticleTemplates {
    public static final ImmutableParticleConfig LARGE_SMOKE = ImmutableParticleConfig.of(ParticleTypes.LARGE_SMOKE, 1, 0.02, 0.5, 1, 0.5, 0, 1, 0);

    /**
     * Empty particle used as a placeholder.
     * <p>
     * This is intended for use only as a placeholder or default value during deserialization.
     * It should NOT be used for actual particle spawning or network synchronization,
     * as the underlying particle type is not registered.
     */
    public static ParticleConfig empty() {
        return EmptyParticleConfig.INSTANCE;
    }

    private ParticleTemplates() {
    }
}
