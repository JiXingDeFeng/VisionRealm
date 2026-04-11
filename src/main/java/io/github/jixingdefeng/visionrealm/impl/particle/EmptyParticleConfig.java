package io.github.jixingdefeng.visionrealm.impl.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.impl.particle.singleton.ImmutableParticleConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Empty particle configuration used as a placeholder.
 * <p>
 * This is intended for use only as a default value during deserialization
 * when no particle configuration is provided. It should NOT be used for
 * actual particle spawning or network synchronization, as the underlying
 * particle type is not registered.
 * <p>
 * The type identifier is {@code "default:empty"}, indicating this is a
 * system-level placeholder rather than a mod-specific particle.
 */
public class EmptyParticleConfig implements ParticleConfig {
    public static final EmptyParticleConfig INSTANCE = new EmptyParticleConfig();
    public static final MapCodec<EmptyParticleConfig> MAP_CODEC = MapCodec.assumeMapUnsafe(Codec.unit(INSTANCE));
    private final SingletonParticleConfig particleConfig = ImmutableParticleConfig.of(ParticleTypes.POOF, -1, 0);

    private EmptyParticleConfig() {
    }

    @Override
    public SingletonParticleConfig getSingleton() {
        return this.particleConfig;
    }

    @NotNull
    @Override
    public ResourceLocation getType() {
        return ResourceLocation.fromNamespaceAndPath("default", "empty");
    }

    @NotNull
    @Override
    public List<ParticleConfig> getList() {
        return this.particleConfig.getList();
    }
}
