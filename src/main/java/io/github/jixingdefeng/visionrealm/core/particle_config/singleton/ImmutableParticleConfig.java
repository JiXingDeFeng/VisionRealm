package io.github.jixingdefeng.visionrealm.core.particle_config.singleton;

import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ImmutableParticleConfig(@NotNull ParticleOptions particleType, int count, double speed,
                                      double spreadX, double spreadY, double spreadZ,
                                      double xOffset, double yOffset, double zOffset) implements SingletonParticleConfig {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "immutable");

    public ImmutableParticleConfig(
            @NotNull ParticleOptions particleType,
            int count,
            double speed
    ) {
        this(particleType, count, speed, 0, 0, 0);
    }

    public ImmutableParticleConfig(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        this(particleType, count, speed, 0, 0, 0, xOffset, yOffset, zOffset);
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

    @Override
    public ResourceLocation getType() {
        return ImmutableParticleConfig.TYPE;
    }

    @Override
    @NotNull
    public MapCodec<? extends ParticleConfig> getMapCodec() {
        return SingletonParticleConfig.MAP_CODEC;
    }
}
