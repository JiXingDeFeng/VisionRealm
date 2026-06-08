package io.github.jixingdefeng.visionrealm.core.data.erosion.block;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.infection.block.BlockErosionEntryManager;
import io.github.jixingdefeng.visionrealm.common.util.file.PathUtil;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.BaseBlockErosionEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockErosionReloadListener extends SimplePreparableReloadListener<Collection<Map<Block, BaseBlockErosionEntry<Object, Object>>>> {
    protected final BlockErosionEntryManager manager;

    public BlockErosionReloadListener(BlockErosionEntryManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T, R> Codec<BaseBlockErosionEntry<T, R>> conversionCodec(Codec<BaseBlockErosionEntry<?, ?>> codec) {
        // 转换为具体类型的 Codec（虽然是 unchecked，但安全）
        // Convert to concrete type Codec (unchecked but safe)
        return (Codec<BaseBlockErosionEntry<T, R>>) (Codec) codec;
    }

    protected static <T, R> Map<Block, BaseBlockErosionEntry<T, R>> loadErosionEntry(
            @NotNull ResourceManager resourceManager,
            @NotNull String path,
            @NotNull Codec<BaseBlockErosionEntry<T, R>> codec
    ) {
        Map<Block, Collection<BaseBlockErosionEntry<T, R>>> map = new HashMap<>();
        resourceManager.listResources(path, location -> location.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        BaseBlockErosionEntry<T, R> erosionEntry = codec.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                .getOrThrow(JsonParseException::new)
                                .getFirst();
                        Block source = erosionEntry.getSource();
                        if (PathUtil.isValidResourcePath(BuiltInRegistries.BLOCK.getKey(source), fileId, path)) {
                            map.computeIfAbsent(source, key -> new ArrayList<>()).add(erosionEntry);
                        }
                    } catch (Exception e) {
                        VisionRealm.LOGGER.error(e.getMessage(), e);
                    }
                });
        Map<Block, BaseBlockErosionEntry<T, R>> newMap = new HashMap<>();
        map.forEach((block, erosionEntryList) ->
                newMap.put(block, BaseBlockErosionEntry.merger(erosionEntryList))
        );
        VisionRealm.LOGGER.debug("Block erosion entry for path '{}' loaded complete", path);
        return newMap;
    }

    @Nullable
    protected static Map<Block, BaseBlockErosionEntry<Object, Object>> load(
            @NotNull ResourceManager resourceManager,
            @NotNull PendingLoader pendingLoader
    ) {
        String path = pendingLoader.path();
        try {
            var codec = conversionCodec(pendingLoader.codec());
            return loadErosionEntry(resourceManager, path, codec);
        } catch (Exception e) {
            VisionRealm.LOGGER.error("Failed to load erosion entry from path: {}", path, e);
        }

        return null;
    }

    @NotNull
    @Override
    protected Collection<Map<Block, BaseBlockErosionEntry<Object, Object>>> prepare(
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        BlockErosionLoaderRegisterEvent event = new BlockErosionLoaderRegisterEvent();
        NeoForge.EVENT_BUS.post(event);
        Queue<PendingLoader> queue = new ArrayDeque<>(event.getPendingLoaders());

        List<Map<Block, BaseBlockErosionEntry<Object, Object>>> entryList = new ArrayList<>();
        while (!queue.isEmpty()) {
            PendingLoader pendingLoader = queue.poll();
            if (pendingLoader != null) {
                var loadedMap = load(resourceManager, pendingLoader);
                entryList.add(loadedMap);
            }
        }

        return entryList;
    }

    @Override
    protected void apply(
            @NotNull Collection<Map<Block, BaseBlockErosionEntry<Object, Object>>> collection,
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        this.manager.start(collection);
    }

    /**
     * Represents a pending block erosion loader registration.
     *
     * @param path  The resource path for JSON files
     * @param codec The codec for decoding {@link BaseBlockErosionEntry} instances
     */
    public record PendingLoader(String path, Codec<BaseBlockErosionEntry<?, ?>> codec) {
    }
}
