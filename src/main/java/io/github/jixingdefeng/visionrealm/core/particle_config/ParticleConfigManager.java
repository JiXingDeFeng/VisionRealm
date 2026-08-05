package io.github.jixingdefeng.visionrealm.core.particle_config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.content.particle.ModParticleConfigs;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistry;
import io.github.jixingdefeng.visionrealm.core.util.hash.HashUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches particle configurations from data packs.
 * <p>
 * This class provides a registry for reusable particle configurations. By extracting
 * common particle effects into separate JSON files, configurations can be referenced
 * by ID rather than being duplicated across multiple erosion key definitions.
 * </p>
 * <p>
 * The {@code type} field in the JSON file determines which particle configuration
 * implementation to use.
 * </p>
 *
 * <p><b>File Location:</b></p>
 * <pre>data/&lt;namespace&gt;/particle_configs/&lt;id&gt;.json</pre>
 *
 * <p><b>JSON Structure Example:</b></p>
 * <pre>
 * {@code {
 *   "type": "visionrealm:default",
 *   "particle": {
 *     "type": "minecraft:campfire_cosy_smoke"
 *   },
 *   "count": 5
 * }}
 * </pre>
 *
 * @author JiXingDeFeng
 * @see ParticleConfig
 * @since 0.1.0
 */
public class ParticleConfigManager {
    public static final String PATH = VisionRealm.MOD_ID + "/particle_config";
    private static final Map<ResourceLocation, ParticleConfig> PERSISTENT_STORAGE = new Object2ObjectOpenHashMap<>();

    /**
     * Retrieves the registry key associated with the given particle configuration type.
     * <p>
     * This method extracts the {@link ResourceKey} from the config's {@link MapCodec},
     * which is used to identify the particle configuration type in the registry.
     * If the codec is not registered, the key for {@link EmptyParticleConfig} is returned as a fallback.
     *
     * @param config The particle configuration instance whose codec key should be retrieved
     * @return The registry key of the codec, or the empty particle config key if the codec is not registered
     */
    public static ResourceKey<MapCodec<? extends ParticleConfig>> getCodec(ParticleConfig config) {
        return ModRegistry.PARTICLE_CONFIG_TYPE.getResourceKey(config.getMapCodec())
                .orElseGet(ModParticleConfigs.EMPTY::getKey);
    }

    /**
     * Generates a unique resource location key for a particle configuration based on its content.
     * <p>
     * The key is composed of:
     * <ul>
     *   <li>Namespace: hash of the particle's type identifier (using {@code ResourceLocation.CODEC})</li>
     *   <li>Path: a prefix followed by the hash of the particle configuration encoded by the given codec</li>
     * </ul>
     *
     * @param codec      The codec used to encode the particle configuration
     * @param particle   The particle configuration
     * @return A resource location key uniquely identifying this configuration
     */
    public static ResourceLocation generateKey(Codec<ParticleConfig> codec, ParticleConfig particle) {
        return ResourceLocation.fromNamespaceAndPath(
                HashUtil.hashString(ResourceLocation.CODEC, particle.getType()),
                VisionRealm.INTERNAL_DATA_PREFIX + HashUtil.hashString(codec, particle)
        );
    }

    /**
     * Handles server stopping to clean up resources.
     * <p>Clears persistent storage and releases the resource manager reference.</p>
     */
    @ApiStatus.Internal
    public static void close() {
        PERSISTENT_STORAGE.clear();
    }

    /**
     * Retrieves a particle configuration from persistent storage by its location.
     *
     * @param location The resource location key
     * @return The particle configuration, or {@code null} if not found
     */
    public static ParticleConfig get(ResourceLocation location) {
        return PERSISTENT_STORAGE.get(location);
    }

    /**
     * Stores a particle configuration in persistent storage using the default codec.
     * <p>
     * This method generates a unique key based on the particle configuration content,
     * then stores the configuration in the persistent storage.
     * It is a convenience method equivalent to calling
     * {@link #storePersistent(Codec, ParticleConfig)} with {@link ParticleConfig#CODEC}.
     *
     * @param particle The particle configuration to store
     * @return The generated resource location key for the stored configuration
     */
    public static ResourceLocation storePersistent(ParticleConfig particle) {
        return storePersistent(ParticleConfig.CODEC, particle);
    }

    /**
     * Stores a particle configuration persistently using a generated key.
     * <p>
     * The key is generated by hashing the type identifier and configuration content.
     *
     * @param codec    The codec used to encode/decode the particle
     * @param particle The particle configuration
     * @return The generated resource location key
     */
    public static ResourceLocation storePersistent(Codec<ParticleConfig> codec, ParticleConfig particle) {
        ResourceLocation key = generateKey(codec, particle);
        return storePersistent(key, particle);
    }

    /**
     * Stores a particle configuration persistently with the specified key.
     *
     * @param location The resource location key
     * @param config   The particle configuration to store
     * @return The same resource location key, or {@code null} if config is a placeholder
     */
    public static ResourceLocation storePersistent(ResourceLocation location, ParticleConfig config) {
        if (config instanceof EmptyParticleConfig) {
            return null;
        }

        PERSISTENT_STORAGE.put(location, config);
        return location;
    }

    /**
     * Stores multiple particle configurations persistently, generating keys automatically.
     *
     * @param codec   The codec used to encode/decode the particles
     * @param particles The particle configurations to store
     * @return A list of generated resource location keys (in the same order)
     */
    public static List<ResourceLocation> storePersistentAll(Codec<ParticleConfig> codec, ParticleConfig... particles) {
        return Arrays.stream(particles)
                .map(particle -> storePersistent(codec, particle))
                .toList();
    }

    /**
     * Stores a map of particle configurations persistently using explicit keys.
     * <p>
     * Entries with {@code null} or empty particle configurations are filtered out.
     *
     * @param configMap A map from resource location to particle configuration
     */
    public static void storePersistentAll(Map<ResourceLocation, ParticleConfig> configMap) {
        PERSISTENT_STORAGE.putAll(configMap);
    }

    /**
     * Removes a particle configuration from persistent storage.
     *
     * @param location The resource location key
     * @return The removed particle configuration, or {@code null} if not present
     */
    public static ParticleConfig remove(ResourceLocation location) {
        return PERSISTENT_STORAGE.remove(location);
    }
}
