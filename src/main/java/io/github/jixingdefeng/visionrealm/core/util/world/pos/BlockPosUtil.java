package io.github.jixingdefeng.visionrealm.core.util.world.pos;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class BlockPosUtil {

    public static int getLightLevel(LevelAccessor level, BlockPos pos, boolean considerSky, boolean considerBlock) {
        if (considerBlock && considerSky) {
            return level.getRawBrightness(pos, level.getSkyDarken());
        } else if (considerSky) {
            return level.getBrightness(LightLayer.SKY, pos);
        } else if (considerBlock) {
            return level.getBrightness(LightLayer.BLOCK, pos);
        } else {
            return 0;
        }
    }

    /**
     * Snaps a position to a solid surface (top of a block) if
     * {@code surface} is {@code true}; otherwise returns the input unchanged.
     * <p>
     * Searches up to {@code range} blocks above and below the original
     * position to find a block with air above it.
     *
     * @return the adjusted position, or {@code null} if no valid surface was found
     */
    @Nullable
    public static Vec3 snapSurface(
            Vec3 vec3,
            Level level,
            int upperRange,
            int belowRange,
            boolean includeFluidAsSurface
    ) {
        BlockPos blockPos = BlockPos.containing(vec3);
        BlockPos.MutableBlockPos previous = blockPos.mutable();
        int posY = blockPos.getY();
        for (int i = upperRange; i >= 0; i--) {
            previous.setY(posY + i);
            if (isSurfaceBlock(level, previous, includeFluidAsSurface)) {
                return new Vec3(vec3.x, getSurfaceHeight(level, previous, includeFluidAsSurface), vec3.z);
            }
        }

        for (int i = belowRange; i > 0; i--) {
            previous.setY(posY - i);
            if (isSurfaceBlock(level, previous, includeFluidAsSurface)) {
                return new Vec3(vec3.x, getSurfaceHeight(level, previous, includeFluidAsSurface), vec3.z);
            }
        }

        return null;
    }

    public static double getSurfaceHeight(Level level, BlockPos pos, boolean includeFluidAsSurface) {
        BlockState state = level.getBlockState(pos);
        VoxelShape voxelShape = state.getCollisionShape(level, pos);
        if (!voxelShape.isEmpty()) {
            AABB decisionBox = voxelShape.bounds();
            return pos.getY() + decisionBox.maxY;
        } else if (includeFluidAsSurface && !state.getFluidState().isEmpty()) {
            return pos.getY() + 1;
        } else {
            return pos.getY();
        }
    }

    public static boolean isSurfaceBlock(Level level, BlockPos pos, boolean includeFluidAsSurface) {
        BlockState state = level.getBlockState(pos);
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        VoxelShape aboveShape = aboveState.getCollisionShape(level, abovePos);

        if (aboveShape.isEmpty()) {
            VoxelShape currentShape = state.getCollisionShape(level, pos);

            boolean above = !includeFluidAsSurface || aboveState.getFluidState().isEmpty();
            boolean current = !currentShape.isEmpty() || includeFluidAsSurface && !state.getFluidState().isEmpty();
            return above && current;
        }

        return false;
    }

    private BlockPosUtil() {
    }
}
