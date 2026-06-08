package io.github.jixingdefeng.visionrealm.api.selector.game;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.Collection;

/**
 * A registered target selector that provides delayed target extraction.
 * <p>
 * This interface encapsulates the logic to obtain targets at execution time,
 * allowing the selector to be created with the correct {@link Level}.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * RegisteredTargetSelector<BlockPos, BlockSelector> registered =
 *     new SimpleRegisteredTargetSelector<>(
 *         (level, random) -> TargetSelectors.block(level).randomObtain(random),
 *         selector -> selector.toList()
 *     );
 *
 * // Execute at runtime
 * Collection<BlockPos> targets = registered.getTarget(level, random);
 * }</pre>
 *
 * @param <T> The target type (e.g., Vec3, Entity, BlockPos)
 * @param <S> The specific selector type (e.g., PositionSelector, EntitySelector, BlockSelector)
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public interface RegisteredTargetSelector<T, S extends TargetSelector<T, S>> {

    /**
     * Creates a target selector instance for the given level and random source.
     *
     * @param level  The level to query
     * @param random The random source for random operations
     * @return A configured target selector instance
     */
    S targetSelector(Level level, RandomSource random);

    /**
     * Returns the execution result as a collection of targets.
     * <p>
     * If {@link #isSingle()} returns {@code true}, this collection contains exactly one element.
     * </p>
     *
     * @param level  The level to query
     * @param random The random source for random operations
     * @return The collection of targets
     */
    Collection<T> getTarget(Level level, RandomSource random);

    /**
     * Returns the dimensions where this target selector is applicable.
     *
     * @return An unmodifiable collection of dimension keys
     */
    Collection<ResourceKey<Level>> getDimension();

    /**
     * Returns whether the target selector yields a single result.
     * <p>
     * This corresponds to operations like {@link TargetSelector#get()}
     * which returns a single target, compared to {@link TargetSelector#toList()}
     * which returns a collection.
     * </p>
     *
     * @return {@code true} for single target, {@code false} for multiple
     */
    boolean isSingle();
}
