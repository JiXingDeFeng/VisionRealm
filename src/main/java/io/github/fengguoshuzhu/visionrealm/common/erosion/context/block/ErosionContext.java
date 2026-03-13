package io.github.fengguoshuzhu.visionrealm.common.erosion.context.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public record ErosionContext<T>(T source, Level level, Vec3 pos, BlockPos blockPos) {
    public ErosionContext(T source, Level level, Vec3 pos) {
        this(source, level, pos, BlockPos.containing(pos));
    }

    public BlockPlaceContext conversionBlock() {
        BlockState state = this.level.getBlockState(this.blockPos);
        Direction direction = state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) : Direction.UP;
        return new BlockPlaceContext(this.level, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(this.pos, direction, this.blockPos));
    }
}
