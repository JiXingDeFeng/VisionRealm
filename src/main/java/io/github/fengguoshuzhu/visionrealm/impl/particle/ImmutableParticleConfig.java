package io.github.fengguoshuzhu.visionrealm.impl.particle;

import io.github.fengguoshuzhu.visionrealm.api.particle.ParticleConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.NotNull;

public record ImmutableParticleConfig(@NotNull ParticleOptions particleType, IntProvider countProvider, double speed,
                                      double spreadX, double spreadY, double spreadZ,
                                      double xOffset, double yOffset, double zOffset) implements ParticleConfig {

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
        return of(particleType, count, count, speed, spreadX, spreadY, spreadZ, xOffset, yOffset, zOffset);
    }

    public static ImmutableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int maxCount,
            int minCount,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        return new ImmutableParticleConfig(particleType, UniformInt.of(maxCount, minCount), speed, spreadX, spreadY, spreadZ, xOffset, yOffset, zOffset);
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
