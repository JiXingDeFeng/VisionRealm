package io.github.jixingdefeng.visionrealm.api.erosion.infection.entity;

import io.github.jixingdefeng.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.entity.EntityErosionHandler;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Specialized interface for entities that can undergo erosion transformations.
 * <p>
 * This interface adapts the base {@link CanBeErosion} methods for entity-specific
 * erosion handling. It provides a default implementation of {@link #tryErosion}
 * that delegates to {@link EntityErosionHandler}.
 * </p>
 *
 * @param <T> The source entity type (must extend {@link Entity})
 * @param <R> The result type after erosion (e.g., Entity, BlockState)
 * @param <E> The target type of the result (e.g., EntityType, Block)
 * @author JiXingDeFeng
 * @see CanBeErosion
 * @see EntityErosionHandler
 * @see ErosionType
 * @since 0.0.1-dev
 */
public interface CanBeErosionEntity<T extends Entity, R, E> extends CanBeErosion<T, R, E> {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link EntityErosionHandler#tryErosion}
     * for processing entity erosion.
     * </p>
     *
     * @param source {@inheritDoc}
     * @param level  {@inheritDoc}
     * @param pos    {@inheritDoc}
     * @param type   {@inheritDoc}
     */
    @Override
    default void tryErosion(T source, Level level, Vec3 pos, ErosionType type) {
        if (source instanceof CanBeErosionEntity<?, ?, ?> canBeErosion) {
            EntityErosionHandler.tryErosion(canBeErosion, level, pos, type);
        }
    }
}
