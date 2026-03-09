package io.github.fengguoshuzhu.visionrealm.event.bus.game.data.erosion.block;

import io.github.fengguoshuzhu.visionrealm.api.event.data.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.fengguoshuzhu.visionrealm.common.world.erosion.block.BlockErosionKey;

public class BlockErosionEvents {

    public static void onBlockErosionLoaderRegister(BlockErosionLoaderRegisterEvent event) {
        event.registryLoader("erosion/block_erosion/block", BlockErosionKey.TransformIntoBlock.CODEC);
        event.registryLoader("erosion/block_erosion/entity", BlockErosionKey.TransformIntoEntity.CODEC);
    }
}
