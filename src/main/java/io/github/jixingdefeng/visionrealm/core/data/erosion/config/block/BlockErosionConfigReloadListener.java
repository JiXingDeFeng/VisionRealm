package io.github.jixingdefeng.visionrealm.core.data.erosion.config.block;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.common.erosion.config.BlockErosionConfig;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.config.block.BlockErosionConfigManager;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockErosionConfigReloadListener extends SimplePreparableReloadListener<Map<Block, BlockErosionConfig>> {
    private final BlockErosionConfigManager manager;

    public BlockErosionConfigReloadListener(BlockErosionConfigManager manager) {
        this.manager = manager;
    }

    @NotNull
    @Override
    protected Map<Block, BlockErosionConfig> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<Block, BlockErosionConfig> biomeErosionMap = new HashMap<>();
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(BlockErosionConfigManager.jsonPath);

        for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation location = entry.getKey();
            String path = location.getPath();

            for (Resource resource : entry.getValue()) {
                try (var reader = resource.openAsReader()) {
                    ConfigKey configKey = ConfigKey.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                            .getOrThrow(JsonParseException::new)
                            .getFirst();
                    if (configKey.replace()) {
                        biomeErosionMap.clear();
                    }

                    for (BlockErosionConfig config : configKey.configs()) {
                        biomeErosionMap.put(config.source(), config);
                    }

                    for (Block key : configKey.remove()) {
                        biomeErosionMap.remove(key);
                    }
                } catch (Exception exception) {
                    VisionRealm.LOGGER.error("Couldn't read block erosion config list {} from {} in data pack {}",
                            fileToIdConverter.fileToId(location), location, resource.sourcePackId(), exception);
                }
            }

            VisionRealm.LOGGER.debug("Block erosion config for path '{}' loaded successfully", path);
        }

        return biomeErosionMap;
    }

    @Override
    protected void apply(@NotNull Map<Block, BlockErosionConfig> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.manager.initialize(map);
    }

    private record ConfigKey(boolean replace, List<BlockErosionConfig> configs, List<Block> remove) {
        public static final Codec<ConfigKey> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("replace", false).forGetter(ConfigKey::replace),
                        Codec.list(BlockErosionConfig.CODEC)
                                .fieldOf("configs")
                                .forGetter(ConfigKey::configs),
                        Codec.list(BuiltInRegistries.BLOCK.byNameCodec())
                                .optionalFieldOf("remove", List.of())
                                .forGetter(ConfigKey::remove)
                ).apply(instance, ConfigKey::new)
        );
    }
}
