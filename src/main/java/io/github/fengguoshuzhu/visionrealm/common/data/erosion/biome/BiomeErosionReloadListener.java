package io.github.fengguoshuzhu.visionrealm.common.data.erosion.biome;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.common.erosion.manager.biome.BiomeErosionManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
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
        String path = "erosion/biome_erosion_types";

        resourceManager.listResources(path, fileName -> fileName.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        BiomeErosionConfig config = BiomeErosionConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                .getOrThrow(JsonParseException::new)
                                .getFirst();
                        biomeErosionMap.putAll(config.values());
                    } catch (IOException e) {
                        VisionRealm.LOGGER.error(e.getMessage(), e);
                    }
                });
        return biomeErosionMap;
    }

    @Override
    protected void apply(@NotNull Map<ResourceKey<Biome>, ErosionType> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.manager.heavyLoad(map);
    }

    private record BiomeErosionConfig(Map<ResourceKey<Biome>, ErosionType> values) {
        public static final Codec<BiomeErosionConfig> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.unboundedMap(ResourceKey.codec(Registries.BIOME), ErosionType.CODEC)
                                .fieldOf("values")
                                .forGetter(BiomeErosionConfig::values)
                ).apply(instance, BiomeErosionConfig::new)
        );
    }
}
