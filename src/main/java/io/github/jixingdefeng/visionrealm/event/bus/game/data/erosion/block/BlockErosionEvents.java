package io.github.jixingdefeng.visionrealm.event.bus.game.data.erosion.block;

import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.TransformIntoEntity;
import net.neoforged.bus.api.SubscribeEvent;

public class BlockErosionEvents {

    @SubscribeEvent
    public static void onBlockErosionLoaderRegister(BlockErosionLoaderRegisterEvent event) {
        event.registryLoader("block", TransformIntoBlock.CODEC);
        event.registryLoader("entity", TransformIntoEntity.CODEC);
    }
}
