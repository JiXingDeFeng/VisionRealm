package io.github.jixingdefeng.visionrealm.core.server.packs.resources.erosion.biome;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.common.util.file.PathUtil;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.biome.BiomeErosionManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiomeErosionReloadListener extends SimplePreparableReloadListener<Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig>> {
    private final RegistryAccess registryAccess;

    public BiomeErosionReloadListener(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    @NotNull
    @Override
    protected Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> biomeConfigMap = new HashMap<>();
        resourceManager.listResources(BiomeErosionManager.JSON_PATH, location -> location.getPath().endsWith(".json"))
                .forEach(((location, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        String fileName = PathUtil.extractFileName(location.getPath(), false);
                        if (!fileName.equals(BiomeErosionManager.JSON_NAME)) {
                            VisionRealm.LOGGER.warn("Invalid file found at {}, File: {}", location, fileName);
                        } else {
                            BiomeErosionDataPack config = BiomeErosionDataPack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, this.registryAccess), JsonParser.parseReader(reader))
                                    .getOrThrow(JsonParseException::new)
                                    .getFirst();
                            if (config.replace()) {
                                biomeConfigMap.clear();
                            }

                            biomeConfigMap.putAll(config.values());
                            for (ResourceKey<Biome> key : config.remove()) {
                                biomeConfigMap.remove(key);
                            }
                        }
                    } catch (IOException exception) {
                        VisionRealm.LOGGER.error("Failed to parse biome erosion config at {} from data pack {}: {}",
                                location, resource.sourcePackId(), exception.getMessage(), exception);
                    }
                }));
        return biomeConfigMap;
    }

    @Override
    protected void apply(
            @NotNull Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> map,
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        BiomeErosionManager.start(map);
    }

    /**
     * Internal data holder for biome erosion configuration loaded from data packs.
     * <p>
     * This record represents the raw structure of {@code biome_erosion_types.json}
     * files, supporting incremental updates across multiple data packs.
     *
     * @param replace Whether to clear all existing entries before applying new ones
     * @param values  Map of biome keys to their erosion configurations
     * @param remove  List of biome keys to remove from the current configuration
     *
     * @see BiomeErosionManager
     * @since 0.0.1-dev
     */
    private record BiomeErosionDataPack(boolean replace, Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> values, List<ResourceKey<Biome>> remove) {
        public static final Codec<BiomeErosionDataPack> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("replace", false).forGetter(BiomeErosionDataPack::replace),
                        Codec.unboundedMap(ResourceKey.codec(Registries.BIOME), BiomeErosionManager.CONFIG_CODEC)
                                .fieldOf("values")
                                .forGetter(BiomeErosionDataPack::values),
                        Codec.list(ResourceKey.codec(Registries.BIOME))
                                .optionalFieldOf("removes", List.of())
                                .forGetter(BiomeErosionDataPack::remove)
                ).apply(instance, BiomeErosionDataPack::new)
        );
    }
}
