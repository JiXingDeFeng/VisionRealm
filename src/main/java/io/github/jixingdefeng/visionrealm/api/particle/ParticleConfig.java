package io.github.jixingdefeng.visionrealm.api.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.common.util.particle.ParticleTemplates;
import io.github.jixingdefeng.visionrealm.core.particle.config.ParticleConfigStore;
import io.github.jixingdefeng.visionrealm.impl.particle.EmptyParticleConfig;
import io.github.jixingdefeng.visionrealm.impl.particle.list.WeightedParticleConfig;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Container interface for a particle configuration that supports both
 * single and weighted variants.
 * <p>
 * The {@code "type"} field in JSON, which
 * holds a {@link ResourceLocation} (e.g. {@code "visionrealm:default"} or
 * {@code "visionrealm:weighted"}), determines the concrete implementation
 * via codec dispatch.
 *
 * <p><b>The {@code type} field in JSON determines which implementation to use:</b></p>
 * <ul>
 *   <li><b>{@code visionrealm:default}</b> – a single particle configuration.</li>
 *   <li><b>{@code visionrealm:weighted}</b> – a weighted collection of particle
 *       configurations; one is selected at random when triggered.</li>
 * </ul>
 *
 * <p><b>External vs Inline References:</b></p>
 * <ul>
 *   <li><b>External reference:</b> A resource location string (e.g., {@code "visionrealm:blood_particles"})
 *       pointing to a separate particle config JSON file in data packs.</li>
 *   <li><b>Inline definition:</b> A full particle configuration object embedded directly
 *       in the JSON (e.g., {@code {"type":"visionrealm:default","particle":{...},"count":5}}).</li>
 * </ul>
 * Both forms are handled by {@link #LOCATION_CODEC}, which automatically resolves and
 * stores inline definitions for later use.
 *
 * <p><b>JSON Examples:</b></p>
 * <ul>
 *   <li><b>Singleton particle config (visionrealm:default)</b>
 *     <pre>{@code {
 *   "type": "visionrealm:default",
 *   "particle": {
 *     "type": "minecraft:heart"
 *   },
 *   "count": 5
 * }}</pre>
 *   </li>
 *   <li><b>Weighted particle config (visionrealm:weighted)</b>
 *     <pre>{@code {
 *   "type": "visionrealm:weighted",
 *   "values": [
 *     {
 *       "config": "visionrealm:blood_particles",   // external reference
 *       "weight": 2
 *     },
 *     {
 *       "config": {                                // inline definition
 *         "type": "visionrealm:default",
 *         "particle": {
 *           "type": "minecraft:smoke"
 *         }
 *       },
 *       "weight": 1
 *     }
 *   ]
 * }}</pre>
 *   </li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see SingletonParticleConfig
 * @see WeightedParticleConfig
 * @see ParticleTemplates
 * @since 0.0.3-dev
 */
public interface ParticleConfig {
    Codec<ParticleConfig> CODEC = ResourceLocation.CODEC.dispatch(
            ParticleConfig::getType,
            ParticleConfigStore::getCodec
    );
    Codec<ResourceLocation> LOCATION_CODEC = Codec.either(
            ResourceLocation.CODEC, CODEC
    ).xmap(
            either -> either.map(
                    location -> location,
                    particle -> ParticleConfigStore.getInstance()
                            .orElseThrow(() -> new NullPointerException("ParticleConfigStore it is null"))
                            .storePersistent(particle)
            ),
            Either::left
    );

    /**
     * Returns the effective {@link SingletonParticleConfig} to be rendered.
     * <ul>
     *   <li>For a singleton config, returns itself.</li>
     *   <li>For a weighted config, returns one entry chosen at random
     *       according to the configured weights.</li>
     * </ul>
     *
     * @return a non‑null singleton particle configuration
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

    /**
     * Checks whether this configuration is empty.
     * <p>
     * Returns {@code true} if it is an {@link EmptyParticleConfig} or if its
     * {@link #unwrap() unwrapped} list is empty.
     *
     * @return {@code true} if no particles are configured
     */
    default boolean isEmpty() {
        return this instanceof EmptyParticleConfig || this.unwrap().isEmpty();
    }
}
