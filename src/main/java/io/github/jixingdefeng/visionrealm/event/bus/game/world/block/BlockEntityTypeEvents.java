package io.github.jixingdefeng.visionrealm.event.bus.game.world.block;

import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

public class BlockEntityTypeEvents {

    @SubscribeEvent
    public static void addSignBlockEvents(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BlockEntityType.SIGN,
                ModBlocks.BLOOD_CORRODED_CHERRY_SIGN.get(),
                ModBlocks.BLOOD_CORRODED_CHERRY_WALL_SIGN.get()
        );
        event.modify(
                BlockEntityType.HANGING_SIGN,
                ModBlocks.BLOOD_CORRODED_CHERRY_HANGING_SIGN.get(),
                ModBlocks.BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN.get()
        );
    }
}
