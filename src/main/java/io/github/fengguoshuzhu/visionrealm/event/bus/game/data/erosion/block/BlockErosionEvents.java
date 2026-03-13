package io.github.fengguoshuzhu.visionrealm.event.bus.game.data.erosion.block;

import io.github.fengguoshuzhu.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.TransformIntoBlock;
import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.TransformIntoEntity;

public class BlockErosionEvents {

    public static void onBlockErosionLoaderRegister(BlockErosionLoaderRegisterEvent event) {
        event.registryLoader("erosion/block_erosion/block", TransformIntoBlock.CODEC);
        event.registryLoader("erosion/block_erosion/entity", TransformIntoEntity.CODEC);
    }
}
