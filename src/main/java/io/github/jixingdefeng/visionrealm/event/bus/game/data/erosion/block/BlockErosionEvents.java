package io.github.jixingdefeng.visionrealm.event.bus.game.data.erosion.block;

import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.block.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.entity.TransformIntoEntity;

public class BlockErosionEvents {

    public static void onBlockErosionLoaderRegister(BlockErosionLoaderRegisterEvent event) {
        event.registryLoader("block", TransformIntoBlock.CODEC);
        event.registryLoader("entity", TransformIntoEntity.CODEC);
    }
}
