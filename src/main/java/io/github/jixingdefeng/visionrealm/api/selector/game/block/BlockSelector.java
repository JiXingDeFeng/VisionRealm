package io.github.jixingdefeng.visionrealm.api.selector.game.block;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.core.util.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.core.util.selector.SurfaceSelection;
import net.minecraft.core.BlockPos;

/**
 * A selector for retrieving block states from the world.
 * <p>All selection criteria are combined with AND logic.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Find a random stone block within 20 blocks of the player
 * BlockSelector selector = BlockSelector.create()
 *     .centerAt(player.position())
 *     .inRange(Vec3.ZERO, 20)
 *     .allow(Set.of(Blocks.STONE))
 *     .randomSingle(null);
 *
 * Optional<BlockPos> result = selector.getSingle();
 * }</pre>
 *
 * <p><strong>Note:</strong> All methods in this interface are intermediate operations
 * that configure the selector for chaining. Terminal operations are provided by the
 * parent interface {@link TargetSelector}.</p>
 *
 * @author JiXingDeFeng
 * @since 0.1.0
 */
public interface BlockSelector extends TargetSelector<BlockPos, BlockSelector> {

    /**
     * Selects only positions that are on the surface (topmost non-air block).
     * <p>A position is considered on the surface if the block at that position
     * is non-air and the block above it is air.</p>
     *
     * @param surface {@code true} to enable surface-only selection, {@code false} to disable
     * @return The current selector instance for chaining
     */
    BlockSelector surface(SurfaceSelection surface);

    /**
     * Restricts selection based on air state.
     *
     * @param air The air selection mode
     * @return The current selector instance for chaining
     */
    BlockSelector air(StateSelection air);

    /**
     * Restricts selection based on fluid state.
     *
     * @param fluid The fluid selection mode
     * @return The current selector instance for chaining
     */
    BlockSelector fluid(StateSelection fluid);
}
