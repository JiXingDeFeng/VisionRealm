package io.github.jixingdefeng.visionrealm.core.data.erosion.biome;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.common.util.file.PathUtil;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.registries.Registries;
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

public class BiomeErosionReloadListener extends SimplePreparableReloadListener<Map<ResourceKey<Biome>, ErosionType>> {
    private final BiomeErosionManager manager;

    public BiomeErosionReloadListener(BiomeErosionManager manager) {
        this.manager = manager;
    }

    @NotNull
    @Override
    protected Map<ResourceKey<Biome>, ErosionType> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceKey<Biome>, ErosionType> biomeErosionMap = new HashMap<>();
        resourceManager.listResources(BiomeErosionManager.jsonPath, location -> location.getPath().endsWith(".json"))
                .forEach(((location, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        String fileName = PathUtil.extractFileName(location.getPath(), false);
                        if (!fileName.equals(BiomeErosionManager.jsonName)) {
                            VisionRealm.LOGGER.warn("Invalid file found at {}, File: {}", location, fileName);
                        } else {
                            BiomeErosionConfig config = BiomeErosionConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                    .getOrThrow(JsonParseException::new)
                                    .getFirst();
                            if (config.replace()) {
                                biomeErosionMap.clear();
                            }

                            biomeErosionMap.putAll(config.values());
                            for (ResourceKey<Biome> key : config.remove()) {
                                biomeErosionMap.remove(key);
                            }
                        }
                    } catch (IOException exception) {
                        VisionRealm.LOGGER.error("Failed to parse biome erosion config at {} from data pack {}: {}",
                                location, resource.sourcePackId(), exception.getMessage(), exception);
                    }
                }));
        return biomeErosionMap;
    }

    @Override
    protected void apply(@NotNull Map<ResourceKey<Biome>, ErosionType> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.manager.start(map);
    }

    private record BiomeErosionConfig(boolean replace, Map<ResourceKey<Biome>, ErosionType> values, List<ResourceKey<Biome>> remove) {
        public static final Codec<BiomeErosionConfig> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("replace", false).forGetter(BiomeErosionConfig::replace),
                        Codec.unboundedMap(ResourceKey.codec(Registries.BIOME), ErosionType.CODEC)
                                .fieldOf("values")
                                .forGetter(BiomeErosionConfig::values),
                        Codec.list(ResourceKey.codec(Registries.BIOME))
                                .optionalFieldOf("removes", List.of())
                                .forGetter(BiomeErosionConfig::remove)
                ).apply(instance, BiomeErosionConfig::new)
        );
    }
}
