package io.github.jixingdefeng.visionrealm.api.selector.game.position;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.core.util.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.core.util.selector.SurfaceSelection;
import net.minecraft.world.phys.Vec3;

/**
 * A selector for retrieving world coordinates (Vec3) from the world.
 * <p>Useful for finding random positions, spawn locations, or area-based coordinates.
 * All selection criteria are combined with AND logic.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Find a random air block within 50 blocks of the player
 * PositionSelector selector = PositionSelector.create()
 *     .centerAt(player.position())
 *     .inRange(BlockPos.ZERO, 50)
 *     .inAir()
 *     .randomSingle(null);
 *
 * Vec3 result = selector.getSingle().orElseThrow();
 * }</pre>
 *
 * <p><strong>Coordinate Reference:</strong>
 * <ul>
 *   <li>If a reference point is set via {@link #centerAt(Vec3)}, range/box coordinates are relative to that point</li>
 *   <li>If no reference point is set, coordinates are treated as absolute world coordinates</li>
 * </ul>
 * </p>
 *
 * <p><strong>Note:</strong> All methods in this interface are intermediate operations
 * that configure the selector for chaining. Terminal operations are provided by the
 * parent interface {@link TargetSelector}.</p>
 *
 * @see PositionSelector
 * @author JiXingDeFeng
 * @since 0.1.0
 */
public interface PositionSelector extends TargetSelector<Vec3, PositionSelector> {

    /**
     * Selects only positions that are on the surface (topmost non-air block).
     * <p>A position is considered on the surface if the block at that position
     * is non-air and the block above it is air.</p>
     *
     * @param surface {@code true} to enable surface-only selection, {@code false} to disable
     * @return The current selector instance for chaining
     */
    PositionSelector surface(SurfaceSelection surface);

    /**
     * Restricts selection based on air state.
     *
     * @param air The air selection mode
     * @return The current selector instance for chaining
     */
    PositionSelector air(StateSelection air);

    /**
     * Restricts selection based on fluid state.
     *
     * @param fluid The fluid selection mode
     * @return The current selector instance for chaining
     */
    PositionSelector fluid(StateSelection fluid);
}
