package io.github.jixingdefeng.visionrealm.core.server.packs.resources.erosion.block;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.jixingdefeng.visionrealm.common.util.file.PathUtil;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.block.BlockErosionEntryStore;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.BaseBlockErosionEntry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockErosionReloadListener extends SimplePreparableReloadListener<Collection<Map<Block, BaseBlockErosionEntry<Object, Object>>>> {
    private final RegistryAccess registryAccess;

    public BlockErosionReloadListener(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
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
            @NotNull Codec<BaseBlockErosionEntry<T, R>> codec,
            @NotNull RegistryAccess registryAccess
    ) {
        Map<Block, BaseBlockErosionEntry<T, R>> map = new HashMap<>();
        resourceManager.listResources(path, location -> location.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        BaseBlockErosionEntry<T, R> erosionEntry = codec.decode(RegistryOps.create(JsonOps.INSTANCE, registryAccess), JsonParser.parseReader(reader))
                                .getOrThrow(JsonParseException::new)
                                .getFirst();
                        Block source = erosionEntry.getSource();
                        ResourceLocation location = BuiltInRegistries.BLOCK.getKey(source);
                        if (PathUtil.isValidResourcePath(location, fileId, path, false)) {
                            map.put(source, erosionEntry);
                        } else {
                            VisionRealm.LOGGER.warn(
                                    "Invalid block erosion entry path: {}."
                                            + " Expected file path to follow: data/<namespace>/erosion/block/<block_namespace>/<block_path>.json,"
                                            + " where <block_namespace> matches the block's namespace and <block_path> (without .json) matches the block's path."
                                            + " Found for block {}.",
                                    fileId, location
                            );
                        }
                    } catch (Exception e) {
                        VisionRealm.LOGGER.error(e.getMessage(), e);
                    }
                });
        VisionRealm.LOGGER.debug("Block erosion entry for path '{}' loaded complete", path);
        return map;
    }

    @Nullable
    protected static Map<Block, BaseBlockErosionEntry<Object, Object>> load(
            @NotNull ResourceManager resourceManager,
            @NotNull PendingLoader pendingLoader,
            @NotNull RegistryAccess registryAccess
    ) {
        String path = pendingLoader.path();
        try {
            var codec = conversionCodec(pendingLoader.codec());
            return loadErosionEntry(resourceManager, path, codec, registryAccess);
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
                var loadedMap = load(resourceManager, pendingLoader, this.registryAccess);
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
        BlockErosionEntryStore.reload(collection);
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
