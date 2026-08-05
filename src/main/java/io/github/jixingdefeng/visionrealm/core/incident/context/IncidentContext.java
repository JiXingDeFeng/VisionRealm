package io.github.jixingdefeng.visionrealm.core.incident.context;

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
 * @since 0.1.0
 */
public class IncidentContext<T> {
    protected final Collection<T> allSource;
    protected final ServerLevel level;
    protected final RandomSource random;

    public IncidentContext(Collection<T> allSource, ServerLevel level, RandomSource random) {
        this.allSource = allSource;
        this.level = level;
        this.random = random;
    }

    public Collection<T> getAllTarget() {
        return this.allSource;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public RandomSource getRandom() {
        return this.random;
    }
}
