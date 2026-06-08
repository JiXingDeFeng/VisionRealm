package io.github.jixingdefeng.visionrealm.api.erosion.infection.block;

import io.github.jixingdefeng.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.jixingdefeng.visionrealm.common.data.erosion.block.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.common.erosion.context.ErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.context.infection.block.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.block.BlockErosionHandler;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
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
 * @param <RT> The target type of the result (e.g., Block, EntityType)
 * @param <R>  The result instance after erosion (e.g., BlockState, Entity)
 * @author JiXingDeFeng
 * @see ErosionType
 * @see BlockErosionHandler
 * @see BlockErosionEntry
 * @see CanBeErosion
 * @since 0.0.1-dev
 */
public interface CanBeErosionBlock<RT, R> extends CanBeErosion<Block, RT, R> {

    /**
     * Performs the actual erosion transformation for a block.
     * <p>
     * This is the block-specific version of {@link #transformed(Object, Level, Vec3, ErosionType, ErosionContext)}.
     * </p>
     *
     * @param target  The erosion target from {@link #afterErosion(ErosionType)}
     * @param level   The world containing the block
     * @param pos     The position of the block
     * @param type    The type of erosion being applied
     * @param context The erosion context
     * @return The transformed result, or {@code null} if transformation fails
     */
    @Nullable
    R transformed(RT target, Level level, BlockPos pos, ErosionType type, BlockErosionContext<Block> context);

    /**
     * Checks whether the block can be eroded at the given position.
     * <p>
     * This is a precondition check called before any probability calculation.
     * </p>
     *
     * @param level The level where erosion may occur
     * @param pos   The block position where erosion may occur
     * @param type  The type of erosion being considered
     * @return {@code true} if erosion can proceed, {@code false} otherwise
     */
    boolean canBeEroded(Level level, BlockPos pos, ErosionType type);

    /**
     * Called during chunk random ticking to handle passive erosion spread.
     * <p>
     * This method is invoked on the server side when a block receives a random tick
     * and the block is in an environment that can cause natural erosion.
     * </p>
     *
     * @param state  The current block state being ticked
     * @param level  The server level
     * @param pos    The block position
     * @param random The random source for probabilistic effects
     * @param type   The erosion type to apply
     */
    void randomTickInfection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, ErosionType type);

    /**
     * Handles post-erosion effects after successful transformation.
     * <p>
     * Called after {@link #transformed} succeeds. Use this for:
     * <ul>
     *   <li>Spawning particles</li>
     *   <li>Playing sounds</li>
     *   <li>Scheduling block ticks</li>
     *   <li>Dropping items or experience</li>
     * </ul>
     * Do not perform core state changes here; those belong in {@link #transformed}.
     * </p>
     *
     * @param result The result from {@link #transformed}
     * @param level  The world containing the block
     * @param pos    The position of the block
     * @param type   The type of erosion that was applied
     */
    default void onEroded(R result, Level level, BlockPos pos, ErosionType type) {
    }

    /**
     * Returns the probability that erosion will actually occur.
     * <p>
     * Called after {@link #canBeEroded(Level, BlockPos, ErosionType)} passes.
     * A random check is performed against this probability; only if it passes
     * will {@link #afterErosion(ErosionType)} be called.
     * </p>
     *
     * @return A float between 0.0 and 1.0 (default 1.0)
     */
    default float conversionProbability() {
        return 1.0F;
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
     * to {@link #onEroded(Object, Level, BlockPos, ErosionType)}.
     * </p>
     *
     * @param level  {@inheritDoc}
     * @param pos    {@inheritDoc} (converted to BlockPos)
     * @param type   {@inheritDoc}
     */
    @Override
    default boolean canBeEroded(Level level, Vec3 pos, ErosionType type) {
        return this.canBeEroded(level, BlockPos.containing(pos), type);
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
    default R transformed(RT target, Level level, Vec3 pos, ErosionType type, ErosionContext<Block> context) {
        return this.transformed(target, level, context.blockPos(), type, new BlockErosionContext<>(context));
    }

    /**
     * {@inheritDoc}
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
}
