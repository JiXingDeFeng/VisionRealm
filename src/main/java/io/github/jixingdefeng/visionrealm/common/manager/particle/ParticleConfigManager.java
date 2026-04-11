package io.github.jixingdefeng.visionrealm.common.manager.particle;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.api.event.particle.RegistryParticleConfigTypeEvent;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.impl.particle.EmptyParticleConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Manages particle configurations loaded from data packs with caching support.
 * <p>
 * This class provides a registry for reusable particle configurations. By extracting
 * common particle effects into separate JSON files, configurations can be referenced
 * by ID rather than being duplicated across multiple erosion key definitions.
 * <p>
 * The {@code type} field in the JSON file determines which particle configuration
 * implementation to use.
 *
 * <p><b>File Location:</b></p>
 * <pre>
 * data/&lt;namespace&gt;/particle_configs/&lt;id&gt;.json
 * </pre>
 *
 * @author JiXingDeFeng
 * @see ParticleConfig
 * @since 0.0.1-dev-1
 */
public class ParticleConfigManager {
    public static final String path = "particle_configs";
    protected static ParticleConfigManager INSTANCE;
    protected final Cache<ResourceLocation, ParticleConfig> TEMP_CACHE = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    protected final Map<ResourceLocation, ParticleConfig> PERSISTENT_STORAGE = new HashMap<>();
    protected final Map<ResourceLocation, MapCodec<? extends ParticleConfig>> CODECS = new HashMap<>();
    protected ResourceManager resourceManager;

    public static ParticleConfigManager getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves the codec for the specified particle configuration type.
     * <p>
     * This static method delegates to the instance method {@link #getCodec(ResourceLocation)}.
     *
     * @param type The type identifier (e.g., "visionrealm:singleton")
     * @return The codec for this type, or {@link EmptyParticleConfig#MAP_CODEC} if not registered
     */
    public static MapCodec<? extends ParticleConfig> getCodecForType(ResourceLocation type) {
        return getInstance().getCodec(type);
    }

    /**
     * Initializes the particle config manager by collecting registered particle config types.
     * <p>
     * This method posts a {@link RegistryParticleConfigTypeEvent} to collect all registered
     * particle configuration codecs, enabling dynamic dispatch based on the particle type.
     * <p>
     * This static method delegates to the instance method {@link #init(IEventBus)}.
     *
     * @param bus The mod event bus
     */
    @ApiStatus.Internal
    public static void initialize(IEventBus bus) {
        ParticleConfigManager manager = new ParticleConfigManager();
        manager.init(bus);
        INSTANCE = manager;
    }

    /**
     * Handles server startup to initialize the resource manager.
     * <p>
     * This static method delegates to the instance method {@link #onServerStart(ServerStartingEvent)}.
     *
     * @param event The server starting event
     */
    @ApiStatus.Internal
    public static void serverStart(ServerStartingEvent event) {
        getInstance().onServerStart(event);
    }

    /**
     * Handles server stopping to clean up the manager cache.
     * <p>
     * <b>Note:</b> This method is automatically called during server shutdown
     * and should not be invoked manually elsewhere.
     * <p>
     * This static method delegates to the instance method {@link #onServerStopping(ServerStoppingEvent)}.
     *
     * @param event The server stopping event
     */
    @ApiStatus.Internal
    public static void serverStopping(ServerStoppingEvent event) {
        getInstance().onServerStopping(event);
    }

    protected ParticleConfigManager() {
    }

    public boolean containsKey(ResourceLocation location) {
        return PERSISTENT_STORAGE.containsKey(location);
    }

    /**
     * Retrieves the codec for the specified particle configuration type.
     *
     * @param type The type identifier (e.g., "visionrealm:default")
     * @return The codec for this type, or {@link EmptyParticleConfig#MAP_CODEC} if not registered
     */
    public MapCodec<? extends ParticleConfig> getCodec(ResourceLocation type) {
        return this.CODECS.getOrDefault(type, EmptyParticleConfig.MAP_CODEC);
    }

    /**
     * Caches a particle configuration with the specified key.
     *
     * @param location The resource location key
     * @param config   The particle configuration to cache
     * @return The resource location key, or {@code null} if the config is a placeholder
     */
    public ResourceLocation storage(ResourceLocation location, ParticleConfig config) {
        if (config instanceof EmptyParticleConfig) {
            return null;
        }

        this.PERSISTENT_STORAGE.put(location, config);
        return location;
    }

    @Nullable
    public ParticleConfig load(ResourceLocation id, boolean storage, boolean forceReread) {
        return load(this.resourceManager, id, storage, forceReread);
    }

    /**
     * Loads a particle configuration from the specified resource location.
     * <p>
     * This method looks for a JSON file at:
     * <pre>data/&lt;namespace&gt;/particle_configs/&lt;path&gt;.json</pre>
     *
     * @param resourceManager The resource manager to use for loading
     * @param id              The resource location ID of the particle configuration
     * @param storage         Whether to store permanently (true = MAP, false = temporary CACHE)
     * @param forceReread     Whether to ignore cache and force reload
     * @return The loaded particle configuration, or {@code null} if loading failed
     */
    @Nullable
    public ParticleConfig load(ResourceManager resourceManager, ResourceLocation id, boolean storage, boolean forceReread) {
        if (!forceReread) {
            if (this.PERSISTENT_STORAGE.containsKey(id)) {
                return this.PERSISTENT_STORAGE.get(id);
            }

            ParticleConfig particleConfig = this.TEMP_CACHE.getIfPresent(id);
            if (particleConfig != null) {
                return particleConfig;
            }
        }

        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path + "/" + id.getPath() + ".json");
        Optional<Resource> resource = resourceManager.getResource(location);

        try (var reader = resource.orElseThrow(() -> new NoSuchElementException("Resource not found: " + location)).openAsReader()) {
            ParticleConfig config = ParticleConfig.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .result()
                    .orElseThrow(() -> new NoSuchElementException("Failed to load particle config at " + location));
            if (config instanceof EmptyParticleConfig) {
                if (location.getPath().startsWith(VisionRealm.INLINE_DATA_ID)) {
                    VisionRealm.LOGGER.warn("Inline particle config with hash {} resolved to EmptyParticleConfig (fallback). Please check the inline particle definition.", location);
                } else {
                    VisionRealm.LOGGER.warn("Particle config at {} resolved to EmptyParticleConfig (fallback). This may indicate a missing or invalid particle definition. If no particle effect is intended, omit the 'particle' field entirely.", location);
                }

                return null;
            } else {
                if (storage) {
                    this.storage(id, config);
                } else {
                    this.TEMP_CACHE.put(id, config);
                }

                return config;
            }
        } catch (IOException e) {
            VisionRealm.LOGGER.error("Failed to load particle config at {}", location, e);
        }

        return null;
    }

    @ApiStatus.Internal
    protected void init(IEventBus bus) {
        RegistryParticleConfigTypeEvent event = new RegistryParticleConfigTypeEvent();
        bus.post(event);
        this.CODECS.putAll(event.getParticles());
    }

    @ApiStatus.Internal
    protected void onServerStart(ServerStartingEvent event) {
        this.resourceManager = event.getServer().getResourceManager();
    }

    @ApiStatus.Internal
    protected void onServerStopping(ServerStoppingEvent event) {
        this.PERSISTENT_STORAGE.clear();
        this.resourceManager = null;
    }
}
