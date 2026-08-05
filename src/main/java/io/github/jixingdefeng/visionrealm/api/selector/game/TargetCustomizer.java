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
 * TargetCustomizer<Vec3, Level, RandomSource> customizer = (level, random) -> {
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
 * @since 0.1.0
 */
@FunctionalInterface
public interface TargetCustomizer<T, L extends Level, R extends RandomSource> {

    /**
     * Returns the number of extractions to perform.
     * <p>
     * Called once per execution. This determines how many candidate targets
     * will be obtained from the level. The actual number of results may be
     * further limited by the selector's own settings or post‑processing.
     * <p>
     * <b>A negative value means unlimited</b> – all matching targets are
     * fetched. <b>The default implementation returns {@code -1} (unlimited).</b>
     * Callers must consider the performance implications of fetching an
     * unbounded number of targets, especially in large worlds or dense
     * environments.
     *
     * @param level  The current level
     * @param random The random source
     * @return The number of extractions (negative = unlimited)
     */
    int getExtractionCount(L level, R random);

    /**
     * Returns the number of attempts for each extraction.
     * <p>
     * When a single extraction may fail (e.g., because a generated candidate
     * does not meet requirements), this value controls how many times the
     * extraction will be retried before giving up and moving to the next
     * extraction or finishing.
     * </p>
     * <p>
     * If a selector always obtains its candidates in a single batch without
     * per‑candidate failure, this value has no effect.
     * </p>
     *
     * @return The number of attempts per extraction (must be ≥ 1)
     */
    default int getMaxAttemptsPerExtraction() {
        return 10;
    }

    /**
     * Post-processes the candidates obtained from the current extraction.
     * <p>
     * This method receives the full list of targets that were successfully
     * obtained during this execution, before any final result limit is
     * applied. Use it for operations like deduplication, sorting, logging,
     * or side‑effect marking.
     * </p>
     * <p>
     * The returned list will be further filtered and possibly truncated
     * according to the selector's result limit before becoming the final
     * output. Modifying the input list directly and returning it is
     * acceptable.
     * </p>
     *
     * @param targets the candidates from the current extraction (modifiable)
     * @param level   the current level
     * @param random  the random source
     * @return the processed candidate list
     */
    default List<T> postProcess(List<T> targets, L level, R random) {
        // Default: do nothing
        return targets;
    }

    /**
     * Called when extraction could not be performed due to missing or invalid
     * configuration.
     * <p>
     * This is <b>not</b> triggered when extraction runs but finds no targets.
     * It acts as a safe fallback when the extraction itself cannot proceed
     * (e.g., no search area was specified, or required parameters are absent).
     * </p>
     * <p>
     * The returned list is used directly as the final result, bypassing any
     * further processing or filtering.
     * </p>
     *
     * @param level  The current level
     * @param random The random source
     * @return Fallback targets (empty list by default)
     */
    default List<T> onFallback(L level, R random) {
        return List.of();
    }
}
