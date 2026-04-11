package io.github.jixingdefeng.visionrealm.core.data.erosion.biome;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

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
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(BiomeErosionManager.jsonPath);

        for (var entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation location = entry.getKey();
            ResourceLocation resourceLocation = fileToIdConverter.fileToId(location);

            for (var resource : entry.getValue()) {
                try (var reader = resource.openAsReader()) {
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
                } catch (Exception exception) {
                    VisionRealm.LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourceLocation, location, resource.sourcePackId(), exception);
                }
            }
        }

        return biomeErosionMap;
    }

    @Override
    protected void apply(@NotNull Map<ResourceKey<Biome>, ErosionType> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.manager.initializeCache(map);
    }

    private record BiomeErosionConfig(boolean replace, Map<ResourceKey<Biome>, ErosionType> values, List<ResourceKey<Biome>> remove) {
        public static final Codec<BiomeErosionConfig> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("replace", false).forGetter(BiomeErosionConfig::replace),
                        Codec.unboundedMap(ResourceKey.codec(Registries.BIOME), ErosionType.CODEC)
                                .fieldOf("values")
                                .forGetter(BiomeErosionConfig::values),
                        Codec.list(ResourceKey.codec(Registries.BIOME))
                                .optionalFieldOf("remove", List.of())
                                .forGetter(BiomeErosionConfig::remove)
                ).apply(instance, BiomeErosionConfig::new)
        );
    }
}
