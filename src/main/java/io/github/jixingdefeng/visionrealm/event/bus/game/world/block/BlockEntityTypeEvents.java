package io.github.jixingdefeng.visionrealm.event.bus.game.world.block;

import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

public class BlockEntityTypeEvents {

    public static void addSignBlockEvents(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BlockEntityType.SIGN,
                ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_SIGN.get(),
                ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_WALL_SIGN.get()
        );
        event.modify(
                BlockEntityType.HANGING_SIGN,
                ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_HANGING_SIGN.get(),
                ModBlocks.VanillaBlockVariants.BLOOD_CORRODED_CHERRY_WALL_HANGING_SIGN.get()
        );
    }
}
