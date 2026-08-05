package io.github.jixingdefeng.visionrealm.api.selector.game.entity;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * A selector for retrieving entities from the world.
 * <p>Useful for finding specific entities, filtering by type, or selecting random entities.
 * All selection criteria are combined with AND logic.</p>
 *
 * <p><strong>Coordinate Reference:</strong>
 * <ul>
 *   <li>If a reference entity is set via {@link #centerAt(Vec3)}, range/box coordinates are relative to that entity's position</li>
 *   <li>If no reference entity is set, coordinates are treated as absolute world coordinates</li>
 * </ul>
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Find a random player within 30 blocks
 * EntitySelector<Player> selector = EntitySelector.create()
 *     .around(player)
 *     .inRange(Vec3.ZERO, 30)
 *     .players()
 *     .randomSingle(null);
 *
 * Optional<Player> target = selector.getSingle();
 * }</pre>
 *
 * <p><strong>Note:</strong> All methods in this interface are intermediate operations
 * that configure the selector for chaining. Terminal operations are provided by the
 * parent interface {@link TargetSelector}.</p>
 *
 * @param <T> The entity type (must extend Entity)
 * @author JiXingDeFeng
 * @since 0.1.0
 */
public interface EntitySelector<T extends Entity> extends TargetSelector<T, EntitySelector<T>> {
}
