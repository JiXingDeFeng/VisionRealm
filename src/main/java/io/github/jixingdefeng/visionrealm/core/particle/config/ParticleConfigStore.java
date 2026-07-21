package io.github.jixingdefeng.visionrealm.core.particle.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.api.event.particle.RegistryParticleConfigTypeEvent;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.common.util.hash.FingerprintUtil;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.impl.particle.EmptyParticleConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
 * @since 0.0.3-dev
 */
public class ParticleConfigStore {
    public static final String PATH = "particle_config";
    private static final Map<ResourceLocation, MapCodec<? extends ParticleConfig>> CODECS = new Object2ObjectOpenHashMap<>();
    private static volatile ParticleConfigStore INSTANCE;
    protected final Map<ResourceLocation, ParticleConfig> PERSISTENT_STORAGE = new ConcurrentHashMap<>();

    /**
     * Returns the singleton instance of the particle config manager.
     *
     * @return An optional containing the manager instance, or empty if not initialized
     */
    public static Optional<ParticleConfigStore> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    /**
     * Checks whether a codec is registered for the specified particle configuration type.
     *
     * @param location The type identifier (e.g., "visionrealm:singleton")
     * @return {@code true} if a codec is registered, {@code false} otherwise
     */
    public static boolean containsCodec(ResourceLocation location) {
        return CODECS.containsKey(location);
    }

    /**
     * Retrieves the codec for the specified particle configuration type.
     *
     * @param type The type identifier (e.g., "visionrealm:singleton")
     * @return The codec for this type, or {@link EmptyParticleConfig#MAP_CODEC} if not registered
     */
    public static MapCodec<? extends ParticleConfig> getCodec(ResourceLocation type) {
        return CODECS.getOrDefault(type, EmptyParticleConfig.MAP_CODEC);
    }

    /**
     * Creates and activates a new particle config manager instance.
     * <p>
     * This method is intended for internal use, primarily during data pack reloading.
     * <p>
     * If an existing instance is active, it will be closed automatically during
     * activation after the new instance takes over.
     *
     * @return The newly created and activated manager instance
     */
    public static ParticleConfigStore create() {
        ParticleConfigStore manager = new ParticleConfigStore();
        manager.open();
        return manager;
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
    protected static ResourceLocation generateKey(Codec<ParticleConfig> codec, ParticleConfig particle) {
        return ResourceLocation.fromNamespaceAndPath(
                FingerprintUtil.hashString(ResourceLocation.CODEC, particle.getType()),
                VisionRealm.INTERNAL_DATA_PREFIX + FingerprintUtil.hashString(codec, particle)
        );
    }

    /**
     * Initializes the particle config manager by collecting registered particle config types.
     * <p>Posts a {@link RegistryParticleConfigTypeEvent} to collect all registered
     * particle configuration codecs, enabling dynamic dispatch based on the type field.</p>
     *
     * @param bus The mod event bus
     */
    @ApiStatus.Internal
    public static void initialize(IEventBus bus) {
        RegistryParticleConfigTypeEvent event = new RegistryParticleConfigTypeEvent();
        bus.post(event);
        CODECS.putAll(event.getParticles());
    }

    /**
     * Handles server stopping to clean up resources.
     * <p>Clears persistent storage and releases the resource manager reference.</p>
     *
     * @param event The server stopping event
     */
    @ApiStatus.Internal
    public static void close(ServerStoppingEvent event) {
        getInstance().ifPresent(ParticleConfigStore::close);
        INSTANCE = null;
    }

    /**
     * Checks whether a particle configuration exists in persistent storage for the given location.
     *
     * @param location The resource location key
     * @return {@code true} if a configuration exists, {@code false} otherwise
     */
    public boolean containsKey(ResourceLocation location) {
        return this.PERSISTENT_STORAGE.containsKey(location);
    }

    /**
     * Retrieves a particle configuration from persistent storage by its location.
     *
     * @param location The resource location key
     * @return The particle configuration, or {@code null} if not found
     */
    public ParticleConfig get(ResourceLocation location) {
        return this.PERSISTENT_STORAGE.get(location);
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
    public ResourceLocation storePersistent(ParticleConfig particle) {
        return this.storePersistent(ParticleConfig.CODEC, particle);
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
    public ResourceLocation storePersistent(Codec<ParticleConfig> codec, ParticleConfig particle) {
        ResourceLocation key = generateKey(codec, particle);
        return this.storePersistent(key, particle);
    }

    /**
     * Stores a particle configuration persistently with the specified key.
     *
     * @param location The resource location key
     * @param config   The particle configuration to store
     * @return The same resource location key, or {@code null} if config is a placeholder
     */
    public ResourceLocation storePersistent(ResourceLocation location, ParticleConfig config) {
        if (config instanceof EmptyParticleConfig) {
            return null;
        }

        this.PERSISTENT_STORAGE.put(location, config);
        return location;
    }

    /**
     * Stores multiple particle configurations persistently, generating keys automatically.
     *
     * @param codec   The codec used to encode/decode the particles
     * @param particles The particle configurations to store
     * @return A list of generated resource location keys (in the same order)
     */
    public List<ResourceLocation> storePersistentAll(Codec<ParticleConfig> codec, ParticleConfig... particles) {
        return Arrays.stream(particles)
                .map(particle -> this.storePersistent(codec, particle))
                .toList();
    }

    /**
     * Stores a map of particle configurations persistently using explicit keys.
     * <p>
     * Entries with {@code null} or empty particle configurations are filtered out.
     *
     * @param configMap A map from resource location to particle configuration
     */
    public void storePersistentMap(Map<ResourceLocation, ParticleConfig> configMap) {
        Map<ResourceLocation, ParticleConfig> newMap = new HashMap<>();
        configMap.entrySet().stream()
                .filter(entry -> {
                    ParticleConfig value = entry.getValue();
                    return value != null && !value.isEmpty();
                })
                .forEach(entry -> newMap.put(entry.getKey(), entry.getValue()));
        this.PERSISTENT_STORAGE.putAll(newMap);
    }

    /**
     * Removes a particle configuration from persistent storage.
     *
     * @param location The resource location key
     * @return The removed particle configuration, or {@code null} if not present
     */
    public ParticleConfig remove(ResourceLocation location) {
        return this.PERSISTENT_STORAGE.remove(location);
    }

    /**
     * Activates this manager instance as the global singleton.
     * <p>
     * If an instance already exists, it will be closed after this one becomes active.
     */
    protected void open() {
        ParticleConfigStore old = INSTANCE;
        INSTANCE = this;
        if (old != null) {
            old.close();
        }
    }

    /**
     * Cleans up resources by clearing persistent storage and invalidating the temporary cache.
     * <p>
     * This method is called when the manager is being closed (e.g., during server shutdown
     * or when a new manager instance replaces the current one).
     */
    protected void close() {
        this.PERSISTENT_STORAGE.clear();
    }
}
