package io.github.fengguoshuzhu.visionrealm.manager.world.erosion.block;

import io.github.fengguoshuzhu.visionrealm.common.world.erosion.block.BlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;

public class BlockErosionKeyManager {
    private static BlockErosionKeyManager instance;
    private final Map<Block, BlockErosionKey<Block, BlockState>> BLOCK_MAP = new HashMap<>();
    private final Map<Block, BlockErosionKey<EntityType<Entity>, Entity>> ENTITY_MAP = new HashMap<>();

    public static BlockErosionKeyManager getInstance() {
        return instance;
    }

    public static void serverStopping(ServerStoppingEvent event) {
        instance = null;
    }

    public void heavyLoad(Map<Block, BlockErosionKey<Block, BlockState>> block, Map<Block, BlockErosionKey<EntityType<Entity>, Entity>> entity) {
        block.forEach((key, blockErosionKey) -> {
            blockErosionKey.init();
            this.BLOCK_MAP.put(key, blockErosionKey);
        });
        this.ENTITY_MAP.putAll(entity);
        instance = this;
        block.forEach((key, value) -> VisionRealm.LOGGER.info("\n\n{}: {}", key, value));
    }

    public BlockErosionKey<Block, BlockState> get(Block source) {
        return BLOCK_MAP.get(source);
    }
}
