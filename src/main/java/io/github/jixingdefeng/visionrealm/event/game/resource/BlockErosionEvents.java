package io.github.jixingdefeng.visionrealm.event.game.resource;

import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.TransformIntoEntity;
import io.github.jixingdefeng.visionrealm.event.events.erosion.block.BlockErosionLoaderRegisterEvent;
import net.minecraft.core.registries.BuiltInRegistries;

public class BlockErosionEvents {

    public static void onBlockErosionLoaderRegister(final BlockErosionLoaderRegisterEvent event) {
        event.registryLoader("block", BuiltInRegistries.BLOCK.byNameCodec(), TransformIntoBlock::new);
        event.registryLoader("entity", BuiltInRegistries.ENTITY_TYPE.byNameCodec(), TransformIntoEntity::new);
    }
}
