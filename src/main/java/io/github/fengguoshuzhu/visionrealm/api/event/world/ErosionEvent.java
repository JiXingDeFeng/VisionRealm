package io.github.fengguoshuzhu.visionrealm.api.event.world;

import io.github.fengguoshuzhu.visionrealm.api.event.world.block.BlockErosionEvent;
import io.github.fengguoshuzhu.visionrealm.api.event.world.entity.EntityErosionEvent;
import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;

/**
 * Base event class for all erosion-related events.
 * <p>
 * This abstract class provides common data fields and accessors for erosion events,
 * including the source being eroded, the erosion result, location information,
 * and erosion type. It serves as the foundation for both entity and block erosion events.
 * </p>
 *
 * <p><b>Type Parameters:</b></p>
 * <ul>
 *   <li><b>T</b> - The type of source being eroded (e.g., {@link Entity}, {@link Block})</li>
 *   <li><b>R</b> - The type of erosion result (can be any type representing the transformation outcome)</li>
 * </ul>
 *
 * @param <T> The source type
 * @param <R> The result type
 * @author JiXingDeFeng
 * @see EntityErosionEvent
 * @see BlockErosionEvent
 * @see ErosionType
 * @since 1.0.0
 */
public abstract class ErosionEvent<T, R> extends Event {
    private final T source;
    private final Level level;
    private final ErosionType type;
    private final Vec3 position;
    protected R target;

    /**
     * Creates a new erosion event.
     *
     * @param source   The object being eroded (entity, block, etc.)
     * @param target   The erosion result (what it transforms into)
     * @param level    The world where erosion occurs
     * @param position The exact position of erosion (as Vec3 for precision)
     * @param type     The type of erosion causing the transformation
     */
    protected ErosionEvent(T source, R target, Level level, Vec3 position, ErosionType type) {
        this.source = source;
        this.target = target;
        this.level = level;
        this.position = position;
        this.type = type;
    }

    /**
     * Returns the object being eroded.
     *
     * @return The source (entity, block, etc.)
     */
    public T getSource() {
        return this.source;
    }

    /**
     * Returns the current erosion result.
     * <p>
     * For {@code Pre} events, this may be modified via {@code setResult}.
     * For {@code Post} events, this is the final result after transformation.
     * </p>
     *
     * @return The erosion result (what the source transforms into)
     */
    public R getTarget() {
        return this.target;
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
     * Returns the exact position where erosion occurs.
     * <p>
     * Position is stored as {@link Vec3} for maximum precision, even for block events.
     * For blocks, this can be converted to {@link BlockPos} via {@link BlockPos#containing(Position)}.
     * </p>
     *
     * @return The position as Vec3
     */
    public Vec3 getPosition() {
        return this.position;
    }

    /**
     * Returns the type of erosion being applied.
     *
     * @return The erosion type
     */
    public ErosionType getType() {
        return this.type;
    }
}
