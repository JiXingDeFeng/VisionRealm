package io.github.jixingdefeng.visionrealm.content.data.packs.resources.block_erosion;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Function8;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.BlockErosionManager;
import io.github.jixingdefeng.visionrealm.core.util.file.PathUtil;
import io.github.jixingdefeng.visionrealm.core.util.hash.HashUtil;
import io.github.jixingdefeng.visionrealm.core.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.event.events.erosion.block.BlockErosionLoaderRegisterEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class BlockErosionReloadListener extends SimplePreparableReloadListener<BlockErosionReloadListener.BlockErosionData> {
    private final RegistryAccess registryAccess;

    public BlockErosionReloadListener(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    protected static <RT, R> List<BlockErosionEntry<RT, R>> load(
            @NotNull ResourceManager resourceManager,
            @NotNull PendingLoader<RT, R> pendingLoader,
            @NotNull RegistryAccess registryAccess,
            @NotNull Function<@Nullable BlockPredicate, @Nullable ResourceLocation> idFunction
    ) {
        String path = pendingLoader.path();
        Codec<RT> targetCodec = pendingLoader.targetCodec();
        var createFunction = pendingLoader.createFunction();
        List<BlockErosionEntry<RT, R>> arrayList = new ArrayList<>();
        resourceManager.listResources(path, location -> location.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        BlockErosionEntry.Wrapper<RT> wrapper = BlockErosionEntry.Wrapper.codec(targetCodec)
                                .decode(RegistryOps.create(JsonOps.INSTANCE, registryAccess), JsonParser.parseReader(reader))
                                .getOrThrow(JsonParseException::new)
                                .getFirst();
                        Block source = wrapper.source();
                        ResourceLocation location = BuiltInRegistries.BLOCK.getKey(source);
                        if (PathUtil.isValidResourcePath(location, fileId, path, false)) {
                            arrayList.addAll(wrapper.build(createFunction, idFunction));
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
        VisionRealm.LOGGER.info("Block erosion entry for path '{}' loaded complete", path);
        return arrayList;
    }

    protected static Map<Block, Map<ErosionType, ArrayWeightRandomList<BlockErosionEntry<?, ?>>>> wrapper(
            @NotNull List<BlockErosionEntry<?, ?>> entryList
    ) {
        Map<Block, Map<ErosionType, List<BlockErosionEntry<?, ?>>>> groupedByBlockAndType = new HashMap<>();
        for (BlockErosionEntry<?, ?> entry : entryList) {
            Block block = entry.getSource();
            ErosionType type = entry.getErosionType();
            groupedByBlockAndType.computeIfAbsent(block, k -> new HashMap<>())
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(entry);
        }

        Map<Block, Map<ErosionType, ArrayWeightRandomList<BlockErosionEntry<?, ?>>>> result = new HashMap<>();
        groupedByBlockAndType.forEach((block, blockMap) -> {
            Map<ErosionType, ArrayWeightRandomList<BlockErosionEntry<?, ?>>> listMap = result.computeIfAbsent(block, k -> new HashMap<>());
            blockMap.forEach((type, list) ->
                    listMap.put(type, ArrayWeightRandomList.create(list))
            );
        });

        return result;
    }

    protected static ResourceLocation generateKey(BlockPredicate predicate) {
        return ResourceLocation.withDefaultNamespace(
                VisionRealm.INTERNAL_DATA_PREFIX + HashUtil.hashString(BlockPredicate.CODEC, predicate)
        );
    }

    @NotNull
    @Override
    protected BlockErosionData prepare(
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        BlockErosionLoaderRegisterEvent event = new BlockErosionLoaderRegisterEvent();
        NeoForge.EVENT_BUS.post(event);
        Queue<PendingLoader<?, ?>> queue = new ArrayDeque<>(event.getPendingLoaders());

        BiMap<ResourceLocation, BlockPredicate> predicateMap = HashBiMap.create();
        List<BlockErosionEntry<?, ?>> entryList = new ArrayList<>();
        while (!queue.isEmpty()) {
            PendingLoader<?, ?> pendingLoader = queue.poll();
            if (pendingLoader != null) {
                entryList.addAll(BlockErosionReloadListener.load(
                        resourceManager, pendingLoader, this.registryAccess,
                        predicate -> {
                            if (predicate != null) {
                                if (predicateMap.containsValue(predicate)) {
                                    return predicateMap.inverse().get(predicate);
                                } else {
                                    ResourceLocation location = generateKey(predicate);
                                    predicateMap.put(location, predicate);
                                    return location;
                                }
                            } else {
                                return null;
                            }
                        })
                );
            }
        }

        return new BlockErosionData(wrapper(entryList), predicateMap);
    }

    @Override
    protected void apply(
            @NotNull BlockErosionData data,
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        BlockErosionManager.create(data.entryMap, data.predicateMap);
    }

    public record PendingLoader<RT, R>(
            String path,
            Codec<RT> targetCodec,
            Function8<Block, ErosionType, RT, @Nullable ResourceLocation,
                    @Nullable ResourceLocation, Float, Boolean, Weight, BlockErosionEntry<RT, R>> createFunction
    ) {
    }

    protected record BlockErosionData(
            Map<Block, Map<ErosionType, ArrayWeightRandomList<BlockErosionEntry<?, ?>>>> entryMap,
            Map<ResourceLocation, BlockPredicate> predicateMap
    ) {
    }
}
