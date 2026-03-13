package io.github.fengguoshuzhu.visionrealm.common.data.erosion.block;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.fengguoshuzhu.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.BaseBlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.common.erosion.manager.block.BlockErosionKeyManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockErosionReloadListener extends SimplePreparableReloadListener<Collection<Map<Block, BaseBlockErosionKey<Object, Object>>>> {
    private final BlockErosionKeyManager manager;

    public BlockErosionReloadListener(BlockErosionKeyManager manager) {
        this.manager = manager;
    }

    @NotNull
    @Override
    protected Collection<Map<Block, BaseBlockErosionKey<Object, Object>>> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        BlockErosionLoaderRegisterEvent event = new BlockErosionLoaderRegisterEvent();
        NeoForge.EVENT_BUS.post(event);

        List<Map<Block, BaseBlockErosionKey<Object, Object>>> result = new ArrayList<>();
        Map<String, BlockErosionLoaderRegisterEvent.PendingLoader> pendingLoaderMap = new LinkedHashMap<>();

        for (var pendingLoader : event.getPendingLoaders()) {
            String path = pendingLoader.path();
            if (pendingLoaderMap.put(path, pendingLoader) != null) {
                VisionRealm.LOGGER.warn("Duplicate loader path: {}", path, new RuntimeException());
            }
        }

        for (var loader : pendingLoaderMap.values()) {
            String path = loader.path();
            try {
                var codec = this.conversionCodec(loader.codec());
                var loadedMap  = this.loadErosionKeys(resourceManager, path, codec);
                result.add(loadedMap);
            } catch (Exception e) {
                VisionRealm.LOGGER.error("Failed to load erosion keys from path: {}", path, e);
            }
        }

        return result;
    }

    @Override
    protected void apply(@NotNull Collection<Map<Block, BaseBlockErosionKey<Object, Object>>> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.manager.loadFromMap(map);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T, R> Codec<BaseBlockErosionKey<T, R>> conversionCodec(Codec<BaseBlockErosionKey<?, ?>> codec) {
        // 转换为具体类型的 Codec（虽然是 unchecked，但安全）
        // Convert to concrete type Codec (unchecked but safe)
        return (Codec<BaseBlockErosionKey<T, R>>) (Codec) codec;
    }

    protected <T, R> Map<Block, BaseBlockErosionKey<T, R>> loadErosionKeys(@NotNull ResourceManager resourceManager, @NotNull String path, @NotNull Codec<BaseBlockErosionKey<T, R>> codec) {
        Map<Block, Collection<BaseBlockErosionKey<T, R>>> map = new HashMap<>();
        resourceManager.listResources(path, fileName -> fileName.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        BaseBlockErosionKey<T, R> erosionKey = codec.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                .getOrThrow(JsonParseException::new)
                                .getFirst();
                        Block source = erosionKey.getSource();
                        if (nameQualified(BuiltInRegistries.BLOCK.getKey(source), fileId, path)) {
                            map.computeIfAbsent(source, key -> new ArrayList<>()).add(erosionKey);
                        }
                    } catch (Exception e) {
                        VisionRealm.LOGGER.error(e.getMessage(), e);
                    }
                });
        Map<Block, BaseBlockErosionKey<T, R>> newMap = new HashMap<>();
        map.forEach((block, erosionKeys) ->
                newMap.put(block, this.merger(erosionKeys))
        );
        VisionRealm.LOGGER.debug("Data structure for path '{}' loaded successfully", path);
        return newMap;
    }

    /**
     * Validates whether the file path complies with the naming convention.
     * <p>
     * Requirements:
     * - The file name (without .json) must equal the block's path
     * - The file must be located in a subfolder named after the block's namespace
     *   For example: block minecraft:stone should be placed at .../minecraft/stone.json
     *
     * @param source The source block's ResourceLocation
     * @param fileId The file's ResourceLocation
     * @param path   The base path (e.g., "data/mod/erosion/block/")
     * @return true if the file path meets the naming convention
     */
    protected boolean nameQualified(ResourceLocation source, ResourceLocation fileId, String path) {
        String jsonPath = fileId.getPath();
        String fileName = extractFileName(jsonPath);
        String folderName = extractFolderName(jsonPath, path);
        return source.getPath().equals(fileName) && source.getNamespace().equals(folderName);
    }

    private String extractFileName(String jsonPath) {
        return jsonPath.substring(jsonPath.lastIndexOf('/') + 1).replace(".json", "");
    }

    private String extractFolderName(String jsonPath, String path) {
        if (jsonPath.startsWith(path)) {
            String pathWithoutFile = jsonPath.substring(path.length() + 1);
            int index = pathWithoutFile.indexOf('/');
            if (index != -1) {
                return pathWithoutFile.substring(0, index);
            }
        }

        return "";
    }

    protected <T, R> BaseBlockErosionKey<T, R> merger(Collection<BaseBlockErosionKey<T, R>> keys) {
        BaseBlockErosionKey<T, R> presentKey = null;
        for (BaseBlockErosionKey<T, R> key : keys) {
            if (presentKey == null) {
                presentKey = key;
            } else {
                presentKey = presentKey.merger(key);
            }
        }

        return presentKey;
    }
}
