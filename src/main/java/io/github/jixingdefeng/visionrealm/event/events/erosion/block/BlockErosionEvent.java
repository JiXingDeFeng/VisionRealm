package io.github.jixingdefeng.visionrealm.event.events.erosion.block;

import io.github.jixingdefeng.visionrealm.api.erosion.block.ErodibleBlock;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Event fired when a block undergoes erosion transformation.
 * <p>
 * This event provides pre- and post-transformation hooks via its {@link Pre}
 * and {@link Post} inner classes. It extends {@link BlockEvent} with erosion-specific
 * context, including the erodible block instance, erosion type, and transformation target.
 * </p>
 *
 * @param <RT> The type of erosion target (what the block transforms into)
 * @param <R>  The type of erosion result (the actual transformed object)
 * @author JiXingDeFeng
 * @see ErosionType
 * @see ErodibleBlock
 * @since 0.1.0
 */
public abstract class BlockErosionEvent<RT, R> extends BlockEvent {
    protected final ErodibleBlock<RT, R> source;
    protected final ErosionType type;
    protected RT target;

    /**
     * Creates a new block erosion event.
     *
     * @param source The erodible block instance being eroded
     * @param state  The current block state being eroded
     * @param target The erosion result (what it transforms into)
     * @param level  The world where erosion occurs
     * @param pos    The position of the block
     * @param type   The type of erosion causing the transformation
     */
    protected BlockErosionEvent(
            ErodibleBlock<RT, R> source,
            BlockState state,
            RT target,
            Level level,
            BlockPos pos,
            ErosionType type
    ) {
        super(level, pos, state);
        this.source = source;
        this.type = type;
        this.target = target;
    }

    /**
     * Returns the current erosion result.
     * <p>
     * For {@code Pre} events, this may be modified via {@code setResult}.
     * For {@code Post} events, this is the final result after transformation.
     * </p>
     *
     * @return The erosion result (what the source transforms into)
     */
    public RT getTarget() {
        return this.target;
    }

    /**
     * Returns the type of erosion being applied.
     *
     * @return The erosion type
     */
    public ErosionType getType() {
        return this.type;
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
     * @param <RT> The type of transformation result
     * @since 0.1.0
     */
    public static class Pre<RT, R> extends BlockErosionEvent<RT, R> implements ICancellableEvent {

        /**
         * Creates a new pre-transformation event.
         *
         * @param source The erodible block about to transform
         * @param state  The current block state
         * @param target The initial transformation target from {@link ErodibleBlock#afterErosion}
         * @param level  The world
         * @param pos    The block position
         * @param type   The erosion type causing the transformation
         */
        public Pre(ErodibleBlock<RT, R> source, BlockState state, RT target, Level level, BlockPos pos, ErosionType type) {
            super(source, state, target, level, pos, type);
        }

        @Override
        public void setCanceled(boolean isCanceled) {
            ICancellableEvent.super.setCanceled(isCanceled);
        }

        /**
         * Changes what the block transforms into.
         *
         * @param target The new transformation result (e.g., a different block state)
         */
        public void setResult(RT target) {
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
     * @param <RT> The type of transformation result
     * @since 0.1.0
     */
    public static class Post<RT, R> extends BlockErosionEvent<RT, R> {
        private final boolean success;
        private final R result;

        /**
         * Creates a new post-transformation event.
         *
         * @param source  The erodible block before transformation
         * @param state   The block state before transformation
         * @param result  The transformed result from {@link ErodibleBlock#transformed}
         * @param target  What the block transformed into
         * @param level   The world
         * @param pos     The block position where transformation occurred
         * @param type    The erosion type that caused the transformation
         * @param success Whether the transformation was successful
         */
        public Post(
                ErodibleBlock<RT, R> source,
                BlockState state,
                R result,
                RT target,
                Level level,
                BlockPos pos,
                ErosionType type,
                boolean success
        ) {
            super(source, state, target, level, pos, type);
            this.success = success;
            this.result = result;
        }

        /**
         * Returns whether the transformation was successfully applied.
         *
         * @return {@code true} if the block was transformed, {@code false} if transformation failed
         */
        public boolean isSuccess() {
            return this.success;
        }

        /**
         * Returns the result of the erosion transformation.
         *
         * @return The transformed object after erosion, or {@code null} if transformation failed
         */
        public R getResult() {
            return this.result;
        }
    }
}
