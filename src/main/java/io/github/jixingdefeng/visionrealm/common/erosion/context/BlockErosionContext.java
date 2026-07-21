package io.github.jixingdefeng.visionrealm.common.erosion.context;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public record BlockErosionContext(Block source, Level level, BlockPos blockPos) {

    public BlockErosionContext(Block source, Level level, Vec3 pos) {
        this(source, level, BlockPos.containing(pos));
    }

    public BlockErosionContext(BlockErosionContext context) {
        this(context.source(), context.level(), context.blockPos());
    }
}
