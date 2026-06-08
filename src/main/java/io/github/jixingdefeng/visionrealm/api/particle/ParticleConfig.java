package io.github.jixingdefeng.visionrealm.api.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.common.particle.ParticleConfigLoader;
import io.github.jixingdefeng.visionrealm.common.util.particle.ParticleTemplates;
import io.github.jixingdefeng.visionrealm.impl.particle.list.WeightedParticleConfig;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Container interface that provides access to a particle configuration.
 * <p>
 * The {@code type} field in JSON determines which implementation to use:
 * <ul>
 *   <li><b>singleton</b> - Stores exactly one particle configuration. This is a
 *       simple wrapper that directly contains the particle data.</li>
 *   <li><b>multiple</b> - Stores a collection of particle configurations.
 *       When triggered, it selects one configuration from the collection according
 *       to its own selection logic. The system provides a weighted random
 *       implementation as one of the available options.</li>
 * </ul>
 *
 * <p><b>JSON Example:</b>
 * <pre>
 * {
 *   "type": "visionrealm:default"   // The actual fields depend on this value
 * }
 * </pre>
 *
 * @author JiXingDeFeng
 * @see SingletonParticleConfig Singleton particle configuration (single instance, no selection logic)
 * @see WeightedParticleConfig Weighted particle configuration list (multiple instances with weights, random selection)
 * @see ParticleTemplates Particle configuration template (predefined particle configuration templates)
 * @since 0.0.1-dev
 */
public interface ParticleConfig {
    Codec<ParticleConfig> CODEC = ResourceLocation.CODEC.dispatch(
            ParticleConfig::getType,
            ParticleConfigLoader::getCodec
    );
    Codec<ResourceLocation> LOCATION_CODEC = Codec.either(
            ResourceLocation.CODEC, CODEC
    ).xmap(
            either -> either.map(
                    location -> location,
                    particle -> ParticleConfigLoader.cachePersistent(particle, CODEC)
            ),
            Either::left
    );

    /**
     * Returns the underlying singleton particle configuration.
     * <p>
     * For singleton mode, returns itself. For multiple mode, returns the
     * configuration selected from the collection.
     *
     * @return The singleton particle configuration instance
     */
    @NotNull
    SingletonParticleConfig getSingleton();

    /**
     * Returns the type identifier used for codec dispatch.
     *
     * @return The type identifier for this particle configuration
     */
    @NotNull
    ResourceLocation getType();

    /**
     * Returns the list of particle configurations.
     * <p>
     * For singleton mode, returns a single-element list containing itself.
     * For multiple mode, returns the wrapped list of configurations.
     *
     * @return The list of particle configurations
     */
    @NotNull
    List<ParticleConfig> unwrap();

    /**
     * Checks whether this configuration is in singleton mode.
     *
     * @return {@code true} for singleton mode, {@code false} for multiple mode
     */
    default boolean isSingleton() {
        return false;
    }
}
