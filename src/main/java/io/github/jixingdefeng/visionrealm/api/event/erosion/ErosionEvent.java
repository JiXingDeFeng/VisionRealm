package io.github.jixingdefeng.visionrealm.api.event.erosion;

import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;

/**
 * Base event class for all erosion-related events.
 * <p>
 * This abstract class provides common data fields and accessors for erosion events,
 * including the source being eroded, location information, and the world reference.
 * It serves as the foundation for both entity and block erosion events.
 * </p>
 *
 * <p><b>Type Parameters:</b></p>
 * <ul>
 *   <li><b>S</b> - The type of source being eroded (e.g., {@link Entity}, {@link Block})</li>
 * </ul>
 *
 * @param <S> The source type
 * @author JiXingDeFeng
 * @see BlockErosionEvent
 * @since 0.0.1-dev
 */
public abstract class ErosionEvent<S> extends Event {
    private final S source;
    private final Level level;
    private final Vec3 position;

    /**
     * Creates a new erosion event.
     *
     * @param source   The object being eroded (entity, block, etc.)
     * @param level    The world where erosion occurs
     * @param position The position where erosion occurs
     */
    protected ErosionEvent(S source, Level level, Vec3 position) {
        this.source = source;
        this.level = level;
        this.position = position;
    }

    /**
     * Returns the object being eroded.
     *
     * @return The source (entity, block, etc.)
     */
    public S getSource() {
        return this.source;
    }

    /**
     * Returns the world where erosion is occurring.
     *
     * @return The level
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * Returns the position where erosion occurs.
     *
     * @return The position as Vec3
     */
    public Vec3 getPosition() {
        return this.position;
    }
}
