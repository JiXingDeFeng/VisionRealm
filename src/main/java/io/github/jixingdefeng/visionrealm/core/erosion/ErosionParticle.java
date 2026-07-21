package io.github.jixingdefeng.visionrealm.core.erosion;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for the {@code particles} field in block erosion JSON configurations,
 * supporting a single particle for all erosion types or a per‑type mapping.
 *
 * <p>An empty instance ({@link #EMPTY}) is available to represent the absence of
 * particle configuration.
 *
 * <p><b>JSON Examples:</b>
 * <ul>
 *   <li><b>Single particle for all erosion types:</b>
 *     <pre>{@code "particles": "visionrealm:blood_particles"}</pre>
 *   </li>
 *   <li><b>Per‑type mapping:</b>
 *     <pre>{@code "particles": {
 *   "visionrealm:blood": "visionrealm:blood_particles",
 *   "visionrealm:curse": "visionrealm:curse_particles"
 * }}</pre>
 *   </li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see ParticleConfig
 * @since 0.0.3‑dev
 */
public sealed interface ErosionParticle
        permits ErosionParticle.SingleErosionParticle, ErosionParticle.ErosionParticleMap {
    Codec<ErosionParticle> CODEC = Codec.xor(
            ParticleConfig.LOCATION_CODEC,
            Codec.unboundedMap(RegistryFixedCodec.create(ModRegistries.EROSION_TYPE), ParticleConfig.LOCATION_CODEC)
    ).xmap(
            either -> either.map(
                    SingleErosionParticle::new, ErosionParticleMap::new
            ),
            ErosionParticle::either
    );
    // An empty instance representing no particle configuration.
    ErosionParticle EMPTY = new ErosionParticleMap(Map.of());

    /**
     * Retrieves the particle texture location for the given erosion type.
     *
     * @param type the erosion type
     * @return the particle location, or {@code null} if no mapping exists for the type
     *         (in {@link ErosionParticleMap}) or if the particle is not configured
     */
    ResourceLocation get(ErosionType type);

    /**
     * Exports the underlying data as an {@link Either}.
     * <ul>
     *   <li>{@link Either.Left} represents a single global particle location.</li>
     *   <li>{@link Either.Right} represents a per‑type mapping.</li>
     * </ul>
     * Used internally for codec serialization.
     */
    Either<ResourceLocation, Map<Holder<ErosionType>, ResourceLocation>> either();

    /**
     * Returns all distinct particle locations configured in this instance.
     * <p>
     * For a single particle, returns a singleton list; for a map, returns its values.
     *
     * @return an unmodifiable view of the particle locations
     */
    Collection<ResourceLocation> unwrap();

    /**
     * Checks whether a particle is explicitly configured for the given erosion type.
     * <p>
     * For a single global particle this always returns {@code true}; for a map it
     * checks the presence of the type in the internal map.
     *
     * @param level the current level (currently unused, reserved for future context)
     * @param type  the erosion type
     * @return {@code true} if a particle is available for the type
     */
    boolean containsKey(Level level, ErosionType type);

    /**
     * A global particle configuration that applies the same {@link ResourceLocation}
     * to all erosion types.
     * <p>
     * Created when the JSON {@code "particles"} field is a single string.
     *
     * @since 0.0.3‑dev
     */
    record SingleErosionParticle(ResourceLocation particle) implements ErosionParticle {

        @Override
        public ResourceLocation get(ErosionType type) {
            return this.particle;
        }

        @Override
        public Either<ResourceLocation, Map<Holder<ErosionType>, ResourceLocation>> either() {
            return Either.left(this.particle);
        }

        @Override
        public Collection<ResourceLocation> unwrap() {
            return List.of(this.particle);
        }

        @Override
        public boolean containsKey(Level level, ErosionType type) {
            return true;
        }
    }

    /**
     * A per‑type particle map that stores distinct {@link ResourceLocation} values
     * keyed by {@link ErosionType}.
     * <p>
     * Created when the JSON {@code "particles"} field is a JSON object.
     * The constructor unwraps {@link Holder}{@code <ErosionType>} keys into plain
     * {@link ErosionType} references for efficient lookup.
     *
     * @since 0.0.3‑dev
     */
    final class ErosionParticleMap implements ErosionParticle {
        private final Map<ErosionType, ResourceLocation> locationMap;

        /**
         * Constructs a per‑type particle map.
         *
         * @param map a map from erosion type holders to particle resource locations
         */
        public ErosionParticleMap(Map<Holder<ErosionType>, ResourceLocation> map) {
            this.locationMap = new Object2ObjectOpenHashMap<>(map.size());
            map.forEach((key, value) ->
                    this.locationMap.put(key.value(), value)
            );
        }

        @Override
        public ResourceLocation get(ErosionType type) {
            return null;
        }

        @Override
        public Either<ResourceLocation, Map<Holder<ErosionType>, ResourceLocation>> either() {
            Map<Holder<ErosionType>, ResourceLocation> map = new HashMap<>(this.locationMap.size());
            this.locationMap.forEach((key, value) ->
                    map.put(Holder.direct(key), value)
            );
            return Either.right(map);
        }

        @Override
        public Collection<ResourceLocation> unwrap() {
            return this.locationMap.values();
        }

        @Override
        public boolean containsKey(Level level, ErosionType type) {
            return this.locationMap.containsKey(type);
        }
    }
}
