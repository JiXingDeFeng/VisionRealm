package io.github.fengguoshuzhu.visionrealm.api.erosion.infection;

import io.github.fengguoshuzhu.visionrealm.api.controller.entity.EntityErosionController;
import io.github.fengguoshuzhu.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.fengguoshuzhu.visionrealm.common.erosion.context.block.ErosionContext;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.common.erosion.handle.BaseErosionHandle;
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
 * affected by various erosion types ({@link ErosionType}). It provides a three-stage
 * erosion process:
 * <ol>
 *   <li>{@link #afterErosion(ErosionType)} - Called when erosion completes, determines what this object transforms into</li>
 *   <li>{@link #transformed(Object, Level, Vec3, ErosionType, ErosionContext)} - Handles the final transformation logic with context</li>
 *   <li>{@link #onEroded(Object, Level, Vec3, ErosionType)} - Applies the erosion effects after transformation</li>
 * </ol>
 * </p>
 *
 * <p><b>Generic Parameters:</b></p>
 * <ul>
 *   <li><b>{@code T}</b> - The source type being eroded (e.g., {@link Block}, {@link Entity})</li>
 *   <li><b>{@code R}</b> - The result instance after erosion (e.g., {@link Entity}, {@link BlockState})</li>
 *   <li><b>{@code E}</b> - The target type of the result (e.g., {@link EntityType}, {@link Block})</li>
 * </ul>
 *
 * <p><b>Type Relationship Example:</b></p>
 * <ul>
 *   <li>Block → Entity: {@code T = Block}, {@code R = Entity}, {@code E = EntityType}</li>
 *   <li>Entity → Block: {@code T = Entity}, {@code R = BlockState}, {@code E = Block}</li>
 *   <li>Block → Block: {@code T = Block}, {@code R = BlockState}, {@code E = Block}</li>
 *   <li>Entity → Entity: {@code T = Entity}, {@code R = Entity}, {@code E = EntityType}</li>
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
 * @param <R> The result instance after erosion (actual entity, block state, etc.)
 * @param <E> The target type of the result (entity type, block, etc.)
 *
 * @author JiXingDeFeng
 * @see ErosionType
 * @see EntityErosionController
 * @see BaseErosionHandle
 * @since 1.0.0
 */
public interface CanBeErosion<T, R, E> {

    /**
     * Called when erosion fully completes. Returns the result of the erosion transformation.
     * <p>
     * This method determines what the object transforms into after reaching full erosion.
     * The return type {@code T} can be any type - it could be a new entity, a block state,
     * an item stack, or even a custom effect object. The transformed result will then be
     * processed by {@link #transformed} and {@link #onEroded}.
     * </p>
     *
     * @param type The type of erosion that triggered the transformation
     * @return The erosion result (can be an entity, block state, item, etc.),
     *         or {@code null} if the object cannot be transformed
     *
     * @see #transformed(Object, Level, Vec3, ErosionType, ErosionContext)
     * @see #onEroded(Object, Level, Vec3, ErosionType)
     */
    @Nullable
    E afterErosion(ErosionType type);

    /**
     * Transforms the erosion result based on current context.
     * <p>
     * This method is called after {@link #afterErosion(ErosionType)} to allow
     * the erosion result to be modified based on the object's current environment
     * (level, position) and the erosion type.
     * </p>
     *
     * @param target The initial erosion result from {@link #afterErosion(ErosionType)}
     * @param level  The world where erosion is occurring
     * @param pos    The position where erosion is occurring (as Vec3 for precision)
     * @param type   The type of erosion being applied
     * @return The transformed erosion result, or {@code null} if transformation fails,
     *         or {@code null} if no transformation occurs
     *
     * @see #afterErosion(ErosionType)
     * @see #onEroded(Object, Level, Vec3, ErosionType)
     * @since 1.0.0
     */
    @Nullable
    R transformed(E target, Level level, Vec3 pos, ErosionType type, ErosionContext<T> context);

    /**
     * Applies the final erosion effects after transformation.
     * <p>
     * This method is called after all transformations are complete to actually apply
     * the erosion effects. This is where the actual changes happen:
     * </p>
     * <ul>
     *   <li>For entities: Replace with new entity type, apply effects, play sounds</li>
     *   <li>For blocks: Set new block state, spawn particles, drop items</li>
     * </ul>
     *
     * @param result The final transformed erosion result from {@link #transformed}
     * @param level  The world where erosion is occurring
     * @param pos    The position where erosion is occurring
     * @param type   The type of erosion that was applied
     *
     * @see #afterErosion(ErosionType)
     * @see #transformed(Object, Level, Vec3, ErosionType, ErosionContext)
     * @since 1.0.0
     */
    default void onEroded(R result, Level level, Vec3 pos, ErosionType type) {
    }

    /**
     * Determines whether the object can currently be eroded.
     * <p>
     * This method allows objects to define conditions under which they can be eroded.
     *
     * @return {@code true} if the object can currently be eroded, {@code false} otherwise
     *
     * @implSpec The default implementation returns {@code false}, meaning objects must
     *           explicitly opt into erosion by overriding this method.
     *
     * @since 1.0.0
     */
    boolean canBeEroded(ErosionType type);

    /**
     * Returns the probability that erosion will successfully convert the object.
     * <p>
     * This method allows for probabilistic erosion outcomes. When erosion conditions are met,
     * this probability determines whether the erosion actually takes effect or fails.
     * Can be used for:
     * </p>
     * <ul>
     *   <li>Rare or special erosion types that don't always succeed</li>
     *   <li>Objects with natural resistance to certain erosion types</li>
     *   <li>Environmental factors affecting erosion success rate</li>
     * </ul>
     *
     * @return A float between 0.0 and 1.0 representing success probability,
     *         where 0.0 = never succeeds, 1.0 = always succeeds (default)
     *
     * @implSpec The default implementation returns 1.0f (always succeed). Override to
     *           implement probabilistic erosion.
     *
     * @since 1.0.0
     */
    default float conversionProbability(ErosionType type) {
        return 1.0f;
    }

    /**
     * Convenience method to trigger erosion on the implementing object.
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
     *   <li>Consider the {@link #canBeEroded(ErosionType)} check before processing</li>
     * </ul>
     *
     * @param source The object to erode
     * @param level  The world where erosion occurs
     * @param pos    The position of erosion (as Vec3 for precision)
     * @param type   The type of erosion to apply
     *
     * @see CanBeErosionBlock#tryErosion
     * @see #canBeEroded(ErosionType)
     * @since 1.0.0
     */
    default void tryErosion(T source, Level level, Vec3 pos, ErosionType type) {
    }
}
