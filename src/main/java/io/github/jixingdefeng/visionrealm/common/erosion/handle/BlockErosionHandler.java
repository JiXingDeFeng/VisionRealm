package io.github.jixingdefeng.visionrealm.common.erosion.handle;

import io.github.jixingdefeng.visionrealm.api.erosion.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionEvent;
import io.github.jixingdefeng.visionrealm.common.erosion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.common.util.block.BlockStateHelper;
import io.github.jixingdefeng.visionrealm.common.util.erosion.ErosionUtil;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

public class BlockErosionHandler {

    public static boolean tryErosion(Block block, Level level, BlockPos pos, ErosionType type) {
        return process(block, level, pos, type, source -> ErosionUtil.getCanBeErosion(source, level, pos, type));
    }

    public static boolean tryErosion(CanBeErosionBlock<?, ?> key, Level level, BlockPos pos, ErosionType type) {
        return process(key.getSource(), level, pos, type, source -> key);
    }

    public static <RT, R> boolean process(
            Block block,
            Level level,
            BlockPos pos,
            ErosionType type,
            BaseBlockErosionHandle.ErosionWrapper<RT, R, CanBeErosionBlock<RT, R>> wrapper
    ) {
        CanBeErosionBlock<RT, R> canBeErosion = wrapper.getErosion(block);
        if (canBeErosion != null && canBeErosion.canBeEroded(level, pos, type)) {
            return BaseBlockErosionHandle.process(block, level, pos, type, wrapper, new BaseBlockErosionHandle.EventSender<>() {

                @Override
                public BaseBlockErosionHandle.PreEventResult<RT> sendPreEvent(Block source, RT target, Level level, BlockPos pos, ErosionType type) {
                    BlockErosionEvent.Pre<RT> event = new BlockErosionEvent.Pre<>(source, target, level, pos, type);
                    NeoForge.EVENT_BUS.post(event);
                    return BaseBlockErosionHandle.PreEventResult.of(event.getTarget(), !event.isCanceled());
                }

                @Override
                public void sendPostEvent(Block source, R result, RT target, Level level, BlockPos pos, ErosionType type, boolean success) {
                    NeoForge.EVENT_BUS.post(new BlockErosionEvent.Post<>(source, result, target, level, pos, type, success));
                }
            });
        } else {
            return false;
        }
    }

    /**
     * Transforms a block at the given position into the target block.
     * <p>
     * This method replaces the block at the specified position with the target block,
     * copying applicable properties from the original block state to the new one.
     * The world is updated immediately with the change.
     * </p>
     *
     * @param target  The block to transform into
     * @param level   The world where the block exists
     * @param pos     The position of the block
     * @param type    The erosion type causing the transformation
     * @param context The erosion context
     * @return The new block state after transformation
     */
    public static BlockState transformBlock(
            Block target, Level level, BlockPos pos, ErosionType type, BlockErosionContext context
    ) {
        BlockState sourceState = level.getBlockState(pos);
        BlockState targetState = target.defaultBlockState();
        targetState = BlockStateHelper.copyProperties(sourceState, targetState);
        level.setBlock(pos, targetState, Block.UPDATE_ALL_IMMEDIATE);
        return targetState;
    }

    /**
     * Transforms a block at the given position into an entity.
     * <p>
     * This method removes the block at the specified position and spawns the target
     * entity at the same location. The entity is placed at the bottom center of the block.
     * </p>
     *
     * @param <T>     The entity type
     * @param target  The entity type to spawn
     * @param level   The world where the block exists
     * @param pos     The position of the block
     * @param type    The erosion type causing the transformation
     * @param context The erosion context
     * @return The spawned entity, or {@code null} if creation failed
     */
    public static <T extends Entity> T transformEntity(
            EntityType<T> target, Level level, BlockPos pos, ErosionType type, BlockErosionContext context
    ) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        T entity = target.create(level);
        if (entity != null) {
            entity.setPos(context.blockPos().getBottomCenter());
            level.addFreshEntity(entity);
        }

        return entity;
    }
}
