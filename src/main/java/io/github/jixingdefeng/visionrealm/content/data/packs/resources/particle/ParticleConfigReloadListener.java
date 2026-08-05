package io.github.jixingdefeng.visionrealm.content.data.packs.resources.particle;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.core.particle_config.EmptyParticleConfig;
import io.github.jixingdefeng.visionrealm.core.particle_config.ParticleConfigManager;
import io.github.jixingdefeng.visionrealm.core.util.file.PathUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class ParticleConfigReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, ParticleConfig>> {

    @Override
    @NotNull
    protected Map<ResourceLocation, ParticleConfig> prepare(
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profilerFiller
    ) {
        Map<ResourceLocation, ParticleConfig> configMap = new HashMap<>();
        resourceManager.listResources(ParticleConfigManager.PATH, location -> location.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        ParticleConfig config = ParticleConfig.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                .result()
                                .orElseThrow(() -> new NoSuchElementException("Failed to load particle config at " + location));
                        if (config instanceof EmptyParticleConfig) {
                            VisionRealm.LOGGER.warn(
                                    "Particle config at {} resolved to EmptyParticleConfig (fallback)."
                                            + " This may indicate a missing or invalid particle definition."
                                            + " If no particle effect is intended, omit the 'particle' field entirely.",
                                    location
                            );
                        } else {
                            String path = PathUtil.removeSuffix(location.getPath());
                            path = PathUtil.removePrefix(path, ParticleConfigManager.PATH);
                            configMap.put(ResourceLocation.fromNamespaceAndPath(location.getNamespace(), path), config);
                        }
                    } catch (IOException e) {
                        VisionRealm.LOGGER.error("Failed to load particle config at {}", location, e);
                    }
                });
        return configMap;
    }

    @Override
    protected void apply(
            @NotNull Map<ResourceLocation, ParticleConfig> configMap,
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profilerFiller
    ) {
        ParticleConfigManager.storePersistentAll(configMap);
    }
}
