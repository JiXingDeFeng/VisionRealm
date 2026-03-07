package io.github.fengguoshuzhu.visionrealm.api.event.world.block;

import io.github.fengguoshuzhu.visionrealm.api.event.world.ErosionEvent;
import io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection.CanBeErosion;
import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Event fired when a block undergoes erosion transformation.
 * <p>
 * This event family tracks the complete lifecycle of block erosion, from pre-erosion
 * checks to post-erosion results. It extends {@link ErosionEvent} with block-specific
 * context, using {@link BlockPos} instead of {@link Vec3} for position handling.
 * </p>
 *
 * <p><b>Important:</b> This event is only fired when a block is being transformed
 * due to erosion (e.g., when erosion conditions are met), not during the gradual
 * accumulation of erosion effects.</p>
 *
 * @param <T> The type of erosion result (can be any type representing the erosion outcome)
 * @author JiXingDeFeng
 * @see ErosionEvent
 * @see ErosionType
 * @since 1.0.0
 */
public abstract class BlockErosionEvent<T> extends ErosionEvent<Block, T> {

    /**
     * Creates a new block erosion event.
     *
     * @param source   The block being eroded
     * @param target   The erosion result (what it transforms into)
     * @param level    The world where erosion occurs
     * @param pos      The exact position of erosion (as Vec3 for precision)
     * @param type     The type of erosion causing the transformation
     * @since 1.0.0
     */
    protected BlockErosionEvent(Block source, T target, Level level, BlockPos pos, ErosionType type) {
        super(source, target, level, Vec3.atLowerCornerOf(pos), type);
    }

    /**
     * Pre-erosion event fired before a block is transformed.
     * <p>
     * This event is fired when a block has met the conditions for erosion completion
     * and is about to transform. It allows other mods to:
     * <ul>
     *   <li>Cancel the transformation entirely via {@link #setCanceled(boolean)}</li>
     *   <li>Modify what the block transforms into via {@link #setResult(Object)}</li>
     *   <li>Add custom logic or side effects before transformation</li>
     * </ul>
     * </p>
     *
     * @param <T> The type of transformation result
     * @since 1.0.0
     */
    public static class Pre<T> extends BlockErosionEvent<T> implements ICancellableEvent {

        /**
         * Creates a new pre-transformation event.
         *
         * @param source  The block about to transform
         * @param target The initial transformation result from {@link CanBeErosion#afterErosion}
         *                (what the block will transform into)
         * @param level   The world
         * @param pos     The block position
         * @param type    The erosion type causing the transformation
         * @since 1.0.0
         */
        public Pre(Block source, T target, Level level, BlockPos pos, ErosionType type) {
            super(source, target, level, pos, type);
        }

        @Override
        public void setCanceled(boolean isCanceled) {
            ICancellableEvent.super.setCanceled(isCanceled);
        }

        /**
         * Changes what the block transforms into.
         *
         * @param target The new transformation result (e.g., a different block state)
         * @since 1.0.0
         */
        public void setResult(T target) {
            this.target = target;
        }
    }

    /**
     * Post-erosion event fired after a block has been transformed.
     * <p>
     * This event is fired immediately after a block completes its erosion transformation.
     * It provides information about the final outcome and can be used for:
     * <ul>
     *   <li>Playing sound effects or particles at the transformation site</li>
     *   <li>Dropping items or experience</li>
     *   <li>Triggering follow-up events (e.g., spawning entities)</li>
     *   <li>Updating surrounding blocks</li>
     * </ul>
     * </p>
     *
     * @param <T> The type of transformation result
     * @since 1.0.0
     */
    public static class Post<T, R> extends BlockErosionEvent<T> {
        private final boolean success;
        private final R result;

        /**
         * Creates a new post-transformation event.
         *
         * @param source  The original block before transformation
         * @param target What the block transformed into (the result from {@link CanBeErosion#transformed})
         * @param level   The world
         * @param pos     The block position where transformation occurred
         * @param type    The erosion type that caused the transformation
         * @param result  {@code true} if transformation was successful, {@code false} otherwise
         * @since 1.0.0
         */
        public Post(Block source, R result, T target, Level level, BlockPos pos, ErosionType type, boolean success) {
            super(source, target, level, pos, type);
            this.success = success;
            this.result = result;
        }

        /**
         * Returns whether the transformation was successfully applied.
         *
         * @return {@code true} if the block was transformed, {@code false} if transformation failed
         *
         * @since 1.0.0
         */
        public boolean isSuccess() {
            return this.success;
        }

        /**
         * Returns the result of the erosion transformation.
         *
         * @return The transformed object after erosion, or {@code null} if transformation failed
         *
         * @since 1.0.0
         */
        public R getResult() {
            return this.result;
        }
    }
}
