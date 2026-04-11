package io.github.jixingdefeng.visionrealm.api.erosion.infection.block;

import io.github.jixingdefeng.visionrealm.api.controller.entity.EntityErosionController;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.jixingdefeng.visionrealm.common.context.erosion.ErosionContext;
import io.github.jixingdefeng.visionrealm.common.context.erosion.infection.block.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.common.handle.erosion.infection.block.BlockErosionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Specialized interface for blocks that can be eroded.
 * <p>
 * This interface adapts the base {@link CanBeErosion} methods to work with
 * {@link BlockPos} instead of {@link Vec3} for convenience when dealing with blocks.
 * It provides default implementations that convert Vec3 parameters to BlockPos
 * before delegating to block-specific methods.
 * </p>
 *
 * @param <T> The type of erosion target
 * @param <R> The result of erosion
 *
 * @author JiXingDeFeng
 * @see ErosionType
 * @see EntityErosionController
 * @see CanBeErosion
 * @since 0.0.1-dev-1
 */
public interface CanBeErosionBlock<T, R> extends CanBeErosion<Block, R, T> {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation converts the Vec3 position to a BlockPos and delegates
     * to {@link #onEroded(Object, Level, BlockPos, ErosionType)}.
     * </p>
     *
     * @param result {@inheritDoc}
     * @param level  {@inheritDoc}
     * @param pos    {@inheritDoc} (converted to BlockPos)
     * @param type   {@inheritDoc}
     */
    @Override
    default void onEroded(R result, Level level, Vec3 pos, ErosionType type) {
        this.onEroded(result, level, BlockPos.containing(pos), type);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation converts the Vec3 position to a BlockPos and delegates
     * to {@link #transformed(Object, Level, BlockPos, ErosionType, BlockErosionContext)}.
     * </p>
     *
     * @param target {@inheritDoc}
     * @param level  {@inheritDoc}
     * @param pos    {@inheritDoc} (converted to BlockPos)
     * @param type   {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Nullable
    @Override
    default R transformed(T target, Level level, Vec3 pos, ErosionType type, ErosionContext<Block> context) {
        return this.transformed(target, level, context.blockPos(), type, new BlockErosionContext<>(context));
    }

    /**
     * Convenience method to quickly trigger erosion on a block.
     * <p>
     * This method provides a simple way to initiate the erosion process for a block
     * by delegating to {@link BlockErosionHandler#tryErosion}. It automatically converts
     * the {@link Vec3} position to a {@link BlockPos} for block-specific handling.
     * </p>
     *
     * <p><b>Note:</b> This method implements the parent interface's {@link CanBeErosion#tryErosion}
     * method, providing block-specific position conversion.</p>
     *
     * @param source The block to erode
     * @param level  The world where the block exists
     * @param pos    The position of the block (as Vec3, will be converted to BlockPos)
     * @param type   The type of erosion to apply
     *
     * @see BlockErosionHandler#tryErosion(Block, Level, BlockPos, ErosionType)
     * @see #tryErosion(Block, Level, BlockPos, ErosionType)
     */
    @Override
    default void tryErosion(Block source, Level level, Vec3 pos, ErosionType type) {
        this.tryErosion(source, level, BlockPos.containing(pos), type);
    }

    /**
     * Convenience method to trigger erosion on a block.
     * <p>
     * This method provides a block-specific implementation of {@link CanBeErosion#tryErosion},
     * directly delegating to {@link BlockErosionHandler} for processing erosion on blocks.
     * It accepts a {@link BlockPos} position for more natural block handling.
     * </p>
     *
     * <p><b>Note:</b> This is the core implementation method that directly calls the
     * erosion handler. The {@link #tryErosion(Block, Level, Vec3, ErosionType)} variant
     * converts Vec3 positions and delegates to this method.</p>
     *
     * @param source The block to erode
     * @param level  The world where the block exists
     * @param pos    The position of the block (as BlockPos)
     * @param type   The type of erosion to apply
     *
     * @see BlockErosionHandler#tryErosion(Block, Level, BlockPos, ErosionType)
     * @see #tryErosion(Block, Level, Vec3, ErosionType)
     */
    default void tryErosion(Block source, Level level, BlockPos pos, ErosionType type) {
        BlockErosionHandler.tryErosion(source, level, pos, type);
    }

    /**
     * Transforms the erosion result for a block at the specified BlockPos.
     * <p>
     * This is the block-specific version of {@link #transformed(Object, Level, Vec3, ErosionType, ErosionContext)}.
     * Implementations can adjust erosion effects based on the block's exact position,
     * neighboring blocks, or other world context.
     * </p>
     *
     * @param target The initial erosion result from {@link #afterErosion(ErosionType)}
     * @param level  The world containing the block
     * @param pos    The position of the block (as BlockPos)
     * @param type   The type of erosion being applied
     * @return The transformed erosion result
     *
     * @see #transformed(Object, Level, Vec3, ErosionType, ErosionContext)
     */
    @Nullable
    R transformed(T target, Level level, BlockPos pos, ErosionType type, BlockErosionContext<Block> context);

    /**
     * Applies the final erosion effects to a block at the specified BlockPos.
     * <p>
     * This is the block-specific version of {@link #onEroded(Object, Level, Vec3, ErosionType)}.
     * Implementations should handle block-specific logic such as:
     * </p>
     * <ul>
     *   <li>Changing block states</li>
     *   <li>Spawning particles at the block position</li>
     *   <li>Dropping items or experience</li>
     *   <li>Scheduling block ticks for delayed effects</li>
     * </ul>
     *
     * @param result The final transformed erosion result
     * @param level  The world containing the block
     * @param pos    The position of the block (as BlockPos)
     * @param type   The type of erosion that was applied
     *
     * @see #onEroded(Object, Level, Vec3, ErosionType)
     */
    default void onEroded(R result, Level level, BlockPos pos, ErosionType type) {
    }

    /**
     * Gets the probability that erosion will successfully convert the block.
     * <p>
     * This method determines the chance of successful erosion when conditions are met.
     * A value of 1.0 means guaranteed success, while lower values introduce randomness
     * to the erosion process.
     * </p>
     *
     * <p><b>Usage examples:</b></p>
     * <ul>
     *   <li>Rare transformations can use lower probability values</li>
     *   <li>Environmental factors can modify the base probability</li>
     *   <li>Can be combined with random ticks to create gradual effects</li>
     * </ul>
     *
     * <p><b>Note:</b> This probability is checked during the erosion attempt,
     * before any transformation occurs. If the check fails, no erosion takes place
     * and no events are fired.</p>
     *
     * @return The probability of successful erosion, between 0.0 and 1.0
     *         (default implementation returns 1.0F)
     *
     * @see #onEroded(Object, Level, BlockPos, ErosionType)
     * @see #afterErosion(ErosionType)
     */
    default float conversionProbability() {
        return 1.0F;
    }

    /**
     * Handles passive infection spread on random ticks for erodible blocks.
     * <p>
     * Called during chunk random ticking when an erodible block is in an erosion-prone
     * environment (e.g., Blood Erosion Cherry Forest). Controls natural infection over time
     * without external intervention.
     * </p>
     *
     * <p><b>Examples:</b> Cherry blocks → Blood Erosion Cherry blocks,
     * Stone near cursed areas → Cursed Stone</p>
     *
     * <p><b>Note:</b> This is passive/natural infection, distinct from active spread. Server-side only.</p>
     *
     * @param state  The current block state being ticked
     * @param level  The server level where the block exists
     * @param pos    The position of the block
     * @param random The random source for probabilistic infection
     *
     * @see CanBeErosionBlock#afterErosion(ErosionType)
     */
    void randomTickInfection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);
}
