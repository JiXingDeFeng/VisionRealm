package io.github.jixingdefeng.visionrealm.api.incident;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.Collection;

/**
 * Provides context information for incident execution.
 * <p>
 * This interface encapsulates the targets and environment needed to execute an incident,
 * including the source targets, the current level, and a random source.
 * </p>
 *
 * @param <T> The target type (e.g., BlockPos, Vec3, Entity)
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public interface IncidentContext<T> {

    /**
     * Returns all targets as an iterable collection.
     *
     * @return All targets
     */
    Collection<T> getAllSource();

    /**
     * Returns a single target.
     * <p>If multiple targets are available, the behavior is implementation-dependent
     * (typically returns the first target).</p>
     *
     * @return A single target
     */
    T getSource();

    /**
     * Returns the server level where the incident is being executed.
     *
     * @return The server level
     */
    ServerLevel getLevel();

    /**
     * Returns the random source for random operations during execution.
     *
     * @return The random source
     */
    RandomSource getRandom();
}
