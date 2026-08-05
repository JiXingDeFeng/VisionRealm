package io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.handle;

import io.github.jixingdefeng.visionrealm.api.erosion.block.ErodibleBlock;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.core.util.block.BlockStateHelper;
import io.github.jixingdefeng.visionrealm.event.events.erosion.block.BlockErosionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Function;

/**
 * Core processor for handling block erosion operations.
 * <p>
 * Provides a complete erosion processing pipeline:
 * <ul>
 *   <li>Resolving the erodible component via {@link ErodibleBlock}</li>
 *   <li>Dispatching pre-erosion events via {@link BlockErosionEvent.Pre}</li>
 *   <li>Executing the erosion transformation with probability checks</li>
 *   <li>Dispatching post-erosion events via {@link BlockErosionEvent.Post}</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see ErodibleBlock
 * @see ErosionType
 * @since 0.1.0
 */
public class BlockErosionHandle {

    /**
     * Processes a complete erosion cycle for the given block.
     * <p>
     * The processing flow:
     * <ol>
     *   <li>Verifies that the source can be eroded for the given type</li>
     *   <li>Obtains the erosion target from {@link ErodibleBlock#afterErosion}</li>
     *   <li>Sends a pre-erosion event ({@link BlockErosionEvent.Pre}) to check for cancellation</li>
     *   <li>If allowed, performs probability check via {@link ErodibleBlock#conversionProbability}</li>
     *   <li>If probability succeeds, executes the erosion transformation</li>
     *   <li>If successful, applies the {@link ErodibleBlock#onEroded} callback</li>
     *   <li>Sends a post-erosion event ({@link BlockErosionEvent.Post}) with the result</li>
     * </ol>
     * </p>
     * <p>
     * <b>Note:</b> If the pre-erosion event is canceled, the erosion process is aborted
     * and {@code false} is returned.
     *
     * @param <R>     Final result type after erosion (e.g., Entity, BlockState)
     * @param <RT>    Erosion target type (e.g., EntityType, Block)
     * @param <C>     Concrete erosion component type extending {@link ErodibleBlock}
     * @param source  The erodible block instance to process
     * @param level   The world where erosion occurs
     * @param pos     The position of erosion
     * @param type    The type of erosion to apply
     * @param context Factory function that creates {@link BlockErosionContext} from {@link BlockState}
     * @return {@code true} if erosion was successfully applied, {@code false} otherwise
     */
    public static <R, RT, C extends ErodibleBlock<RT, R>> boolean process(
            C source,
            Level level,
            BlockPos pos,
            ErosionType type,
            Function<BlockState, BlockErosionContext> context
    ) {
        Block block = source.getSource();
        if (!type.isValidFor(block)) {
            throw new IllegalArgumentException(
                    "Erosion type " + type + " is not compatible with source type: " + block.getClass().getSimpleName()
            );
        } else if (source.canBeEroded(level, pos, type)) {
            RT target = source.afterErosion(type);
            if (target != null) {
                BlockState state = level.getBlockState(pos);
                BlockErosionEvent.Pre<RT, R> event = new BlockErosionEvent.Pre<>(source, state, target, level, pos, type);
                NeoForge.EVENT_BUS.post(event);
                if (!event.isCanceled()) {
                    boolean success = false;
                    if (level.random.nextFloat() <= source.conversionProbability(type)) {
                        R result = source.transformed(event.getTarget(), context.apply(state));
                        success = result != null;
                        if (success) {
                            source.onEroded(result, level, pos, type);
                        }

                        NeoForge.EVENT_BUS.post(new BlockErosionEvent.Post<>(source, state, result, target, level, pos, type, success));
                    }

                    return success;
                }
            }
        }

        return false;
    }

    /**
     * Transforms a block at the given position into the target block.
     * <p>
     * This method replaces the block at the specified position with the target block,
     * copying applicable properties from the original block state to the new one.
     * The world is updated immediately with the change.
     *
     * @param target  The block to transform into
     * @param context The erosion context containing level, position, and other data
     * @return The new block state after transformation
     */
    public static BlockState transformBlock(
            Block target, BlockErosionContext context
    ) {
        Level level = context.level();
        BlockPos pos = context.blockPos();
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
     *
     * @param <T>     The entity type
     * @param target  The entity type to spawn
     * @param context The erosion context containing level, position, and other data
     * @return The spawned entity, or {@code null} if creation failed
     */
    public static <T extends Entity> T transformEntity(
            EntityType<T> target, BlockErosionContext context
    ) {
        Level level = context.level();
        BlockPos pos = context.blockPos();
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        T entity = target.create(level);
        if (entity != null) {
            entity.setPos(context.blockPos().getBottomCenter());
            level.addFreshEntity(entity);
        }

        return entity;
    }

    private BlockErosionHandle() {
    }
}
