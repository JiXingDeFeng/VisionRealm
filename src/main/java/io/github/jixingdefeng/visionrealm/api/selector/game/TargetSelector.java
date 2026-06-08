package io.github.jixingdefeng.visionrealm.api.selector.game;

import io.github.jixingdefeng.visionrealm.common.util.selector.TargetSelectors;
import io.github.jixingdefeng.visionrealm.impl.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.BaseSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A universal target selector for retrieving game world objects.
 * <p>Acts as a query builder for selecting entities, positions, or blocks from the world.</p>
 *
 * <p>This selector is <strong>single-use</strong> - the first terminal operation
 * ({@link #stream()}, {@link #toList()}, {@link #toArray(IntFunction)}, {@link #get()}, {@link #count()})
 * executes the selection and caches the result. Subsequent terminal operations return the cached result.</p>
 *
 * <p><strong>Warning:</strong> While multiple terminal operations are technically supported,
 * mixing different terminal operations on the same selector is discouraged and may lead to
 * unexpected behavior. Use {@link #copy(Level)} to create a fresh selector when multiple different
 * result types are needed.</p>
 *
 * <p><strong>Random Source Priority:</strong>
 * <ol>
 *   <li>The {@code random} parameter passed directly to the method (if non-null)</li>
 *   <li>The global random source set via {@link #randomSource(RandomSource)} (if set)</li>
 *   <li>A default {@link RandomSource} instance</li>
 * </ol>
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create and configure a selector
 * PositionSelector selector = TargetSelectors.positionSelector()
 *     .centerAt(player.position())
 *     .inRange(50)
 *     .inAir();
 *
 * // Execute once
 * List<Vec3> results = selector.toList();
 *
 * // Reuse with copy
 * PositionSelector copy = selector.copy();
 * Optional<Vec3> single = copy.getSingle();
 * }</pre>
 *
 * @param <T> The target type returned by the selector (e.g., Vec3, Entity, BlockState)
 * @param <S> The self-referential type for fluent chaining (e.g., PositionSelector, EntitySelector)
 * @see AbstractSelector
 * @see BaseSelector
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public interface TargetSelector<T, S extends TargetSelector<T, S>> {

    /**
     * Creates an empty default selector for configuration purposes.
     * <p>The created selector is associated with the given level and can be used
     * to build a query configuration before converting to a concrete selector type.</p>
     *
     * <pre>{@code
     * BaseSelector<Vec3> config = TargetSelector.create(level)
     *     .centerAt(player.position())
     *     .inRange(50)
     *     .inAir();
     *
     * PositionSelector selector = config.converted(PositionSelector::form);
     * }</pre>
     *
     * @param level The level to associate with this selector
     * @param <T>   The target type
     * @return A new default selector instance
     */
    static <T, S extends TargetSelector<T, S>> BaseSelector<T, S> create(Level level) {
        return TargetSelectors.create(level);
    }

    /**
     * Restricts selection to targets within the specified biomes.
     * <p>This is a setter operation. Pass an empty collection to clear the filter.
     * Multiple calls will overwrite the previous value.</p>
     * <p><strong>Note:</strong> Only biomes that are in the whitelist AND NOT in the
     * blacklist will be selected. If the whitelist is empty, any biome not in the
     * blacklist is allowed.</p>
     *
     * @param biomes The biomes to select from (must not be {@code null})
     * @return The current selector instance for chaining
     * @throws NullPointerException if {@code biomes} is {@code null}
     */
    S allowBiome(@NotNull Collection<ResourceKey<? extends Biome>> biomes);

    /**
     * Excludes targets within the specified biomes from selection.
     * <p>This is a setter operation. Pass an empty collection to clear the filter.
     * Multiple calls will overwrite the previous value.</p>
     * <p><strong>Note:</strong> Biomes in the blacklist are excluded. If a whitelist
     * is also set, biomes must be in the whitelist AND NOT in the blacklist to be
     * allowed. If only the blacklist is set, all biomes except those in the blacklist
     * are allowed.</p>
     *
     * @param biomes The biomes to exclude (must not be {@code null})
     * @return The current selector instance for chaining
     * @throws NullPointerException if {@code biomes} is {@code null}
     */
    S denyBiome(@NotNull Collection<ResourceKey<? extends Biome>> biomes);

    /**
     * Sets a fixed reference point for area-based selection.
     * <p>This provides a reference point for subsequent {@link #inRange} or {@link #inBox}
     * restrictions. Pass {@code null} to clear the reference point and use absolute coordinates.</p>
     * <p>Coordinates in range/box methods will be treated as relative to this reference point.</p>
     *
     * @param center The reference point coordinates, or {@code null} to clear
     * @return The current selector instance for chaining
     */
    S centerAt(@Nullable Vec3 center);

    /**
     * Sets a dynamic reference point that is resolved at execution time.
     * <p>The function is called when the selector executes, allowing the reference point
     * to be determined based on the current level (e.g., player position, world spawn).</p>
     * <p>To clear the reference point and use absolute coordinates, use {@link #centerAt(Vec3)} with {@code null}.</p>
     *
     * @param center A function that takes the level and returns the reference point coordinates
     * @return The current selector instance for chaining
     */
    S centerAt(Function<Level, Vec3> center);

    /**
     * Restricts selection to a specific Y-axis range.
     * <p>Coordinates are always absolute world Y values, unaffected by {@link #centerAt(Vec3)}.</p>
     *
     * @param minY Minimum Y coordinate (inclusive)
     * @param maxY Maximum Y coordinate (inclusive)
     * @return The current selector instance for chaining
     */
    S inHeightRange(double minY, double maxY);

    /**
     * Restricts selection to a spherical area centered on the given point.
     * <p>If a reference point is set via {@link #centerAt(Vec3)}, the center coordinates
     * are treated as relative to that reference point. Otherwise, coordinates are treated
     * as absolute world coordinates.</p>
     *
     * @param center The sphere center (must not be {@code null})
     * @param radius The sphere radius (must be >= 0)
     * @return The current selector instance for chaining
     * @throws IllegalArgumentException if radius < 0
     * @throws NullPointerException if {@code center} is null
     */
    S inRange(@NotNull Vec3 center, double radius);

    /**
     * Restricts selection to a cuboid area defined by the given bounds.
     * <p>If a reference point is set via {@link #centerAt(Vec3)}, the bounds coordinates
     * are treated as relative to that reference point. Otherwise, coordinates are treated
     * as absolute world coordinates.</p>
     *
     * @param min The minimum corner coordinates
     * @param max The maximum corner coordinates
     * @return The current selector instance for chaining
     */
    S inBox(Vec3 min, Vec3 max);

    /**
     * Selects only targets within loaded chunks.
     * <p>Targets in unloaded chunks will be excluded from selection.</p>
     *
     * @param loadedOnly {@code true} to restrict to loaded chunks, {@code false} to disable
     * @return The current selector instance for chaining
     */
    S loadedOnly(boolean loadedOnly);

    /**
     * Adds a custom filter to narrow down the selection.
     * <p>Multiple {@code where} calls are combined using AND logic.
     * Filters are applied during selection execution.</p>
     *
     * @param filter The predicate condition for filtering targets
     * @return The current selector instance for chaining
     */
    S where(Predicate<? super T> filter);

    /**
     * Sets the global random source for random selection operations.
     * <p>If not set, a default {@link RandomSource} will be used.</p>
     * <p>The random source is stored and reused across multiple random operation calls
     * on the same selector instance.</p>
     * <p><strong>Note:</strong> When a random source is provided directly to
     * {@link #randomSingle(RandomSource)} or {@link #randomObtain(RandomSource)},
     * that source takes precedence over the globally set one for that specific call.</p>
     *
     * @param random The random source to use (must not be {@code null})
     * @return The current selector instance for chaining
     * @throws NullPointerException if {@code random} is {@code null}
     */
    S randomSource(RandomSource random);

    /**
     * Performs a single random target selection.
     * <p>Each call to this method adds a new randomly selected target to the result list.
     * Multiple calls will accumulate results.</p>
     *
     * @param random The random source to use, or {@code null} to fall back
     * @return The current selector instance for chaining
     */
    S randomSingle(@Nullable RandomSource random);

    /**
     * Performs multiple random target selections using default transformation logic.
     * <p>Each call to this method adds multiple randomly selected targets to the result list.
     * Multiple calls will accumulate results.</p>
     *
     * @param random The random source to use, or {@code null} to fall back
     * @return The current selector instance for chaining
     */
    S randomObtain(@Nullable RandomSource random);

    /**
     * Performs multiple random target selections using a custom transformer.
     * <p>Each call to this method adds multiple randomly selected targets to the result list.
     * Multiple calls will accumulate results.</p>
     *
     * @param customizer The custom transformation logic
     * @param random     The random source to use, or {@code null} to fall back
     * @return The current selector instance for chaining
     */
    S randomObtain(TargetCustomizer<T, Level, RandomSource> customizer, @Nullable RandomSource random);

    /**
     * Limits the maximum number of targets to retrieve.
     * <p>If {@code limit < 1}, no limit is applied (returns all matching targets).</p>
     *
     * @param limit The maximum number of targets (values {@code < 1} mean no limit)
     * @return The current selector instance for chaining
     */
    S limit(int limit);

    /**
     * Returns the number of selected elements.
     * <p>Returns {@code -1} if no terminal operation has been executed yet (selection not performed).</p>
     * <p>Once a terminal operation is executed, this method returns the cached result count.</p>
     *
     * @return The number of selected elements, or {@code -1} if selection not yet executed
     */
    int size();

    /**
     * Returns the level (world) associated with this target selector.
     * <p>The level is required for querying entities, blocks, and positions,
     * and is determined when the selector is created.</p>
     *
     * @return The level (never {@code null})
     */
    @NotNull Level getLevel();

    /**
     * Executes the selection and returns the number of selected elements.
     * <p>This is a terminal operation that triggers selection execution if not already performed.</p>
     *
     * @return The number of selected elements (never negative)
     */
    int count();

    /**
     * Creates a deep copy of this selector.
     * <p>Use this method when you need to reuse the same selection criteria multiple times,
     * as terminal operations consume the original selector.</p>
     * <p>If a {@code level} is provided, the copy will use that level instead of the original's level.</p>
     *
     * @param level The level for the copied selector, or {@code null} to use the original's level
     * @return A new independent copy of this selector
     */
    S copy(@Nullable Level level);

    /**
     * Executes the selection and returns the results as a {@link Stream}.
     * <p><strong>Note:</strong> The first terminal operation triggers selection execution
     * and caches the result. Subsequent terminal operations (including this one) return
     * the cached result without re-executing.</p>
     * <p>The stream is sequential and evaluated lazily, but backed by the cached result.</p>
     *
     * @return A {@code Stream} containing all selected targets
     */
    Stream<T> stream();

    /**
     * Executes the selection and collects the results into a {@link List}.
     * <p><strong>Note:</strong> The first terminal operation triggers selection execution
     * and caches the result. Subsequent terminal operations return the cached result.</p>
     * <p>The returned list is immutable and contains all selected targets
     * in the order they were found.</p>
     *
     * @return A {@code List} containing all selected targets
     */
    List<T> toList();

    /**
     * Executes the selection and converts the results into an array.
     * <p><strong>Note:</strong> The first terminal operation triggers selection execution
     * and caches the result. Subsequent terminal operations return the cached result.</p>
     * <p>The provided generator creates an array of the appropriate type.</p>
     *
     * @param generator Array generator function for creating the target array type
     * @return An array containing all selected targets
     */
    T[] toArray(IntFunction<T[]> generator);

    /**
     * Executes the selection and retrieves a single target.
     * <p><strong>Note:</strong> The first terminal operation triggers selection execution
     * and caches the result. Subsequent terminal operations return the cached result.</p>
     * <p>Returns {@link Optional#empty()} if no target is found.
     * If multiple targets match the selection criteria, the first one is returned
     * (order is implementation-dependent).</p>
     *
     * @return An {@code Optional} containing the selected target, or empty if none found
     */
    Optional<T> get();

    /**
     * Retrieves an element at the index calculated by the generator function.
     * <p>The generator receives the total number of selected elements as input and should return
     * the target index position. This allows for dynamic index selection based on result size.</p>
     * <p><strong>Note:</strong> The first terminal operation triggers selection execution
     * and caches the result. Subsequent terminal operations return the cached result.</p>
     * <p>Returns {@link Optional#empty()} if no target is found or if the generated index
     * is out of bounds (index &lt; 0 or index &gt;= size).</p>
     *
     * @param generator A function that takes the total element count and returns the desired index
     * @return An {@code Optional} containing the element at the generated index, or empty if invalid
     * @throws NullPointerException if generator is null
     */
    default Optional<T> get(Function<Integer, Integer> generator) {
        List<T> list = this.toList();
        return Optional.ofNullable(list.get(generator.apply(list.size())));
    }
}
