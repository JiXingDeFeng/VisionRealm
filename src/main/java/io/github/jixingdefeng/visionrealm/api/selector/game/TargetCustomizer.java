package io.github.jixingdefeng.visionrealm.api.selector.game;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Extension point for customizing target selection behavior.
 *
 * <p>All methods have default implementations, so you only need to override
 * the methods you care about.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * TargetCustomizer<Vec3, Level, RandomSource> customizer = new TargetCustomizer<>() {
 *     {@code @Override}
 *     public boolean shouldIncludePosition(Vec3 pos, Level level, RandomSource random) {
 *         // Only include positions above Y=64
 *         return pos.y() > 64;
 *     }
 *
 *     {@code @Override}
 *     public int getExtractionCount(Level level, RandomSource random) {
 *         // Dynamic count based on player count
 *         if (level instanceof ServerLevel sl) {
 *             return Math.min(20, sl.players().size() * 2);
 *         }
 *         return random.nextIntBetween(5, 15);
 *     }
 * };
 * }</pre>
 *
 * @param <T> The target type (e.g., Vec3, Entity, BlockState)
 * @param <L> The level type (extends Level)
 * @param <R> The random source type (extends RandomSource)
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
@FunctionalInterface
public interface TargetCustomizer<T, L extends Level, R extends RandomSource> {

    /**
     * Filters positions before they are processed.
     *
     * <p>Called for each candidate position before any target-specific logic.
     * Return {@code false} to skip this position entirely.</p>
     *
     * @param value    The candidate
     * @param level  The current level
     * @param random The random source
     * @return {@code true} to include this position, {@code false} to skip
     */
    boolean shouldIncludePosition(T value, L level, R random);

    /**
     * Returns the number of extractions to perform.
     *
     * <p>Called once per execution. This determines how many random positions
     * will be generated and transformed.</p>
     *
     * @param level The current level
     * @param random The random source
     * @return The number of extractions (must be >= 0)
     */
    default int getExtractionCount(L level, R random) {
        return 5;
    }

    /**
     * Returns the number of attempts to generate each extraction.
     * <p>A higher value increases the chance of finding valid positions
     * but may impact performance.</p>
     *
     * @return The number of attempts per extraction (must be >= 1)
     */
    default int getAttemptsPerExtraction() {
        return 10;
    }

    /**
     * Post-processes the collected targets before the limit is applied.
     *
     * <p>Called after all targets have been collected but before the limit check.
     * Useful for operations like deduplication, sorting, or additional filtering.</p>
     *
     * <p><strong>Note:</strong> The returned list will be used as the final result.
     * Modifying the input list directly and returning it is acceptable.</p>
     *
     * @param targets The collected targets (modifiable)
     * @param level   The current level
     * @param random  The random source
     * @return The processed target list
     */
    default List<T> postProcess(List<T> targets, L level, R random) {
        // Default: do nothing
        return targets;
    }

    /**
     * Called when no targets were found after all attempts.
     *
     * <p>Allows custom fallback behavior, such as returning default positions
     * or the world spawn point.</p>
     *
     * @param level  The current level
     * @param random The random source
     * @return Fallback targets (empty list by default)
     */
    default List<T> onEmptyResult(L level, R random) {
        return List.of();
    }
}
