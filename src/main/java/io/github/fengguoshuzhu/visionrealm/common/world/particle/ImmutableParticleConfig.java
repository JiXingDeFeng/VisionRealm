package io.github.fengguoshuzhu.visionrealm.common.world.particle;

import io.github.fengguoshuzhu.visionrealm.api.world.particle.ParticleConfig;
import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.NotNull;

public record ImmutableParticleConfig(@NotNull ParticleOptions particleType, int count, double speed,
                                      double spreadX, double spreadY, double spreadZ,
                                      double xOffset, double yOffset, double zOffset) implements ParticleConfig {

    public static ImmutableParticleConfig of(@NotNull ParticleOptions particleType, int count, double speed) {
        return of(particleType, count, speed, 0, 0, 0);
    }

    public static ImmutableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ
    ) {
        return of(particleType, count, speed, spreadX, spreadY, spreadZ, 0);
    }

    public static ImmutableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double yOffset
    ) {
        return of(particleType, count, speed, spreadX, spreadY, spreadZ, 0, yOffset, 0);
    }

    public static ImmutableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        return new ImmutableParticleConfig(particleType, count, speed, spreadX, spreadY, spreadZ, xOffset, yOffset, zOffset);
    }

    /**
     * Creates a mutable copy of this immutable configuration.
     * <p>
     * Since this configuration is immutable, this method provides a way to obtain
     * a modifiable version that can be adjusted for specific use cases.
     * The original immutable configuration remains unchanged.
     *
     * @return a new mutable particle configuration with the same values
     */
    public ModifiableParticleConfig copy() {
        return new ModifiableParticleConfig(this);
    }
}
