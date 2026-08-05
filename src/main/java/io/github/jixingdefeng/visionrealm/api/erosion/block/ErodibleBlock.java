package io.github.jixingdefeng.visionrealm.api.erosion.block;

import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.handle.BlockErosionHandle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Core interface for blocks that can undergo erosion transformations.
 * <p>
 * This interface defines the contract for blocks that can be affected by various erosion types
 * ({@link ErosionType}). The erosion process follows this sequence:
 * <ol>
 *   <li>{@link #tryErosion(Level, BlockPos, ErosionType)} is called to initiate the erosion attempt</li>
 *   <li>{@link #canBeEroded(Level, BlockPos, ErosionType)} checks preconditions (environment, state, etc.)</li>
 *   <li>{@link #conversionProbability(ErosionType)} determines success chance of this attempt</li>
 *   <li>If probability passes, {@link #afterErosion(ErosionType)} determines the target</li>
 *   <li>{@link #transformed(Object, BlockErosionContext)} performs the actual conversion logic</li>
 *   <li>{@link #onEroded(Object, Level, BlockPos, ErosionType)} handles post-effects (particles, sounds, etc.)</li>
 * </ol>
 * </p>
 *
 * <p><b>Generic Parameters:</b></p>
 * <ul>
 *   <li><b>{@code RT}</b> - The target type of the result (e.g., {@link Block}, {@link EntityType})</li>
 *   <li><b>{@code R}</b> - The result instance after erosion (e.g., {@link BlockState}, {@link Entity})</li>
 * </ul>
 *
 * @param <RT> The target type of the result (e.g., Block, EntityType)
 * @param <R>  The result instance after erosion (e.g., BlockState, Entity)
 * @author JiXingDeFeng
 * @see ErosionType
 * @see BlockErosionHandle
 * @see BlockErosionEntry
 * @since 0.1.0
 */
public interface ErodibleBlock<RT, R> {

    /**
     * Returns the source object being eroded.
     * <p>
     * This is the object that will undergo transformation.
     * </p>
     *
     * @return The source object being eroded
     */
    Block getSource();

    /**
     * Determines the erosion target.
     * <p>
     * Called after the probability check passes. Returns what the source will
     * transform into. The result can be any type that identifies the target,
     * such as a type identifier, a resource key, or a direct reference.
     * The value is then passed to {@link #transformed}.
     * </p>
     *
     * @param type The type of erosion being applied
     * @return The erosion target, or {@code null} if no transformation is possible
     * @see #transformed
     */
    @Nullable
    RT afterErosion(ErosionType type);

    /**
     * Performs the actual erosion transformation for a block.
     * <p>
     * This is where the core conversion logic happens. The {@code target} parameter
     * comes from {@link #afterErosion}. Implementations should create or compute
     * the final result based on the target and the given context.
     * </p>
     *
     * @param target  The erosion target from {@link #afterErosion}
     * @param context The erosion context containing level, position, and type
     * @return The transformed result, or {@code null} if transformation fails
     * @see #afterErosion
     * @see #onEroded
     */
    @Nullable
    R transformed(RT target, BlockErosionContext context);

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
     * Called after {@link #canBeEroded} passes. A random check is performed against
     * this probability; only if it passes will {@link #afterErosion} be called.
     * </p>
     *
     * @param type The type of erosion being applied
     * @return A float between 0.0 and 1.0 (default 1.0)
     */
    default float conversionProbability(ErosionType type) {
        return 1.0f;
    }

    /**
     * Initiates the erosion process on the given block.
     * <p>
     * This is the main entry point for triggering erosion. This method directly
     * calls {@link BlockErosionHandle#process} to process the erosion.
     * </p>
     *
     * @param level  The world where the block exists
     * @param pos    The position of the block
     * @param type   The type of erosion to apply
     *
     * @see BlockErosionHandle#process
     * @see #canBeEroded(Level, BlockPos, ErosionType)
     */
    default boolean tryErosion(Level level, BlockPos pos, ErosionType type) {
        return BlockErosionHandle.process(
                this, level, pos, type, state -> new BlockErosionContext(state, level, pos, type)
        );
    }
}
