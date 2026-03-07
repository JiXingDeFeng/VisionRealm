package io.github.fengguoshuzhu.visionrealm.data.worldgen.features.erosion.block;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.fengguoshuzhu.visionrealm.common.world.erosion.block.BlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.manager.world.erosion.block.BlockErosionKeyManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockErosionReloadListener extends SimplePreparableReloadListener<BlockErosionReloadListener.BlockErosionDataMap> {
    private final BlockErosionKeyManager manager;

    public BlockErosionReloadListener(BlockErosionKeyManager manager) {
        this.manager = manager;
    }

    @NotNull
    @Override
    protected BlockErosionReloadListener.BlockErosionDataMap prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<Block, BlockErosionKey<Block, BlockState>> block = new HashMap<>();
        Map<Block, BlockErosionKey<EntityType<Entity>, Entity>> entity = new HashMap<>();
        String blockPath = "erosion/block_erosion/block";
        String entityPath = "erosion/block_erosion/entity";

        resourceManager.listResources(blockPath, fileName -> fileName.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        BlockErosionKey.TransformIntoBlock blockErosionKey = BlockErosionKey.TransformIntoBlock.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                .getOrThrow(JsonParseException::new)
                                .getFirst();
                        block.put(blockErosionKey.getSource(), blockErosionKey);
                    } catch (IOException e) {
                        VisionRealm.LOGGER.error(e.getMessage(), e);
                    }
                });
        resourceManager.listResources(entityPath, fileName -> fileName.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        BlockErosionKey.TransformIntoEntity<Entity> blockErosionKey = BlockErosionKey.TransformIntoEntity.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                                .getOrThrow(JsonParseException::new)
                                .getFirst();
                        entity.put(blockErosionKey.getSource(), blockErosionKey);
                    } catch (IOException e) {
                        VisionRealm.LOGGER.error(e.getMessage(), e);
                    }
                });
        VisionRealm.LOGGER.info("\n\n方块完成\n");
        return new BlockErosionDataMap(block, entity);
    }

    @Override
    protected void apply(@NotNull BlockErosionReloadListener.BlockErosionDataMap map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.manager.heavyLoad(map.block, map.entity);
    }

    protected record BlockErosionDataMap(Map<Block, BlockErosionKey<Block, BlockState>> block, Map<Block, BlockErosionKey<EntityType<Entity>, Entity>> entity) {
    }
}
