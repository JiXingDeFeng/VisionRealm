package io.github.jixingdefeng.visionrealm.common.erosion.context.infection.block;

import io.github.jixingdefeng.visionrealm.common.erosion.context.ErosionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BlockErosionContext<T extends Block> extends ErosionContext<T> {

    public BlockErosionContext(ErosionContext<T> context) {
        super(context.source(), context.level(), context.pos());
    }

    public BlockErosionContext(T source, Level level, BlockPos blockPos) {
        super(source, level, blockPos.getCenter(), blockPos);
    }
}
