package io.github.jixingdefeng.visionrealm.api.erosion.infection;

import io.github.jixingdefeng.visionrealm.api.controller.entity.EntityErosionController;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.erosion.context.ErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.BaseErosionHandle;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Core interface for objects that can undergo erosion transformations.
 * <p>
 * This interface defines the contract for objects (entities, blocks, etc.) that can be
 * affected by various erosion types ({@link ErosionType}). The erosion process follows this sequence:
 * <ol>
 *   <li>{@link #tryErosion} is called to initiate the erosion attempt</li>
 *   <li>{@link #canBeEroded} checks preconditions (environment, state, etc.)</li>
 *   <li>{@link #conversionProbability} determines success chance of this attempt</li>
 *   <li>If probability passes, {@link #afterErosion} determines the target</li>
 *   <li>{@link #transformed} performs the actual conversion logic</li>
 *   <li>{@link #onEroded} handles post-effects (particles, sounds, etc.)</li>
 * </ol>
 *
 * <p><b>Generic Parameters:</b></p>
 * <ul>
 *   <li><b>{@code T}</b> - The source type being eroded (e.g., {@link Block}, {@link Entity})</li>
 *   <li><b>{@code RT}</b> - The target type of the result (e.g., {@link EntityType}, {@link Block})</li>
 *   <li><b>{@code R}</b> - The result instance after erosion (e.g., {@link Entity}, {@link BlockState})</li>
 * </ul>
 *
 * <p><b>Type Relationship Example:</b></p>
 * <ul>
 *   <li>Block → Entity: {@code T = Block}, {@code R = Entity}, {@code RT = EntityType}</li>
 *   <li>Entity → Block: {@code T = Entity}, {@code R = BlockState}, {@code RT = Block}</li>
 *   <li>Block → Block: {@code T = Block}, {@code R = BlockState}, {@code RT = Block}</li>
 *   <li>Entity → Entity: {@code T = Entity}, {@code R = Entity}, {@code RT = EntityType}</li>
 * </ul>
 *
 * <p><b>Note:</b> These methods are only invoked when erosion is fully completed
 * (e.g., when {@link EntityErosionController#completeErosion(ErosionType)} returns {@code true}),
 * not during the gradual erosion process.</p>
 *
 * <p>
 * <b>About the entity erosion process, please see {@link EntityErosionController}</b>
 * </p>
 *
 * @param <T> The source type being eroded (block, entity, etc.)
 * @param <RT> The target type of the result (entity type, block, etc.)
 * @param <R> The result instance after erosion (actual entity, block state, etc.)
 *
 * @author JiXingDeFeng
 * @see ErosionType
 * @see EntityErosionController
 * @see BaseErosionHandle
 * @since 0.0.1-dev
 */
public interface CanBeErosion<T, RT, R> {

    /**
     * Returns the source object being eroded.
     * <p>
     * This is the object that will undergo transformation.
     * </p>
     *
     * @return The source object being eroded
     */
    T getSource();

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
     * Performs the actual erosion transformation.
     * <p>
     * This is where the core conversion logic happens. The {@code target} parameter
     * comes from {@link #afterErosion}. Implementations should create or compute
     * the final result based on the target and the given context.
     * </p>
     *
     * @param target  The erosion target from {@link #afterErosion}
     * @param level   The world where erosion occurs
     * @param pos     The position where erosion occurs
     * @param type    The type of erosion being applied
     * @param context The erosion context with additional information
     * @return The transformed result, or {@code null} if transformation fails
     * @see #afterErosion
     * @see #onEroded
     */
    @Nullable
    R transformed(RT target, Level level, Vec3 pos, ErosionType type, ErosionContext<T> context);

    /**
     * Checks whether the object can be eroded at the given position.
     * <p>
     * This is a precondition check called before any probability calculation.
     * Use this for conditions like:
     * <ul>
     *   <li>Verifying the surrounding environment</li>
     *   <li>Checking state or cooldowns</li>
     *   <li>Other runtime constraints</li>
     * </ul>
     * </p>
     *
     * @param level The level where erosion may occur
     * @param pos   The position where erosion may occur
     * @param type  The type of erosion being considered
     * @return {@code true} if erosion can proceed, {@code false} otherwise
     */
    boolean canBeEroded(Level level, Vec3 pos, ErosionType type);

    /**
     * Handles post-erosion effects after successful transformation.
     * <p>
     * Called after {@link #transformed} succeeds. Use this for:
     * <ul>
     *   <li>Spawning particles</li>
     *   <li>Playing sounds</li>
     *   <li>Other visual or audio feedback</li>
     * </ul>
     * Do not perform core state changes here; those belong in {@link #transformed}.
     * </p>
     *
     * @param result The result from {@link #transformed}
     * @param level  The world where erosion occurred
     * @param pos    The position where erosion occurred
     * @param type   The type of erosion that was applied
     */
    default void onEroded(R result, Level level, Vec3 pos, ErosionType type) {
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
     * Initiates the erosion process on the given source.
     * <p>
     * This is the main entry point for triggering erosion. Implementations should
     * delegate to the appropriate handler (e.g., block handler, entity handler).
     * <p>
     * This method provides a simple way to initiate the erosion process by delegating
     * to the appropriate erosion handler. The default implementation does nothing,
     * leaving subclasses to implement specific behavior for different target types
     * (entities, blocks, etc.).
     * </p>
     *
     * <p><b>Subclass implementations should:</b></p>
     * <ul>
     *   <li>Call the appropriate erosion handler (e.g., {@code EntityErosionHandler.tryErode})</li>
     *   <li>Handle type-specific position conversion (e.g., {@link BlockPos} for blocks)</li>
     *   <li>Consider the {@link #canBeEroded(Level, Vec3, ErosionType)} check before processing</li>
     * </ul>
     *
     * @param source The object to erode
     * @param level  The world where erosion occurs
     * @param pos    The position of erosion (as Vec3 for precision)
     * @param type   The type of erosion to apply
     *
     * @see CanBeErosionBlock#tryErosion
     * @see #canBeEroded(Level, Vec3, ErosionType)
     */
    default void tryErosion(T source, Level level, Vec3 pos, ErosionType type) {
    }
}
