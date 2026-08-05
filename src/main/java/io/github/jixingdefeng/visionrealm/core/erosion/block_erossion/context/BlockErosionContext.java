package io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.context;

import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record BlockErosionContext(BlockState source, Level level, BlockPos blockPos, ErosionType type) {

    public BlockErosionContext(BlockState source, Level level, Vec3 pos, ErosionType type) {
        this(source, level, BlockPos.containing(pos), type);
    }

    public BlockErosionContext(BlockErosionContext context) {
        this(context.source(), context.level(), context.blockPos(), context.type());
    }
}
