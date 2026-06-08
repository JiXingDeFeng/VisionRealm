package io.github.jixingdefeng.visionrealm.api.selector.game.block;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.common.selector.StateSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

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
 * @since 0.0.2-dev
 */
public interface BlockSelector extends TargetSelector<BlockPos, BlockSelector> {

    /**
     * Allows only blocks of the specified types.
     * <p>This is a setter operation. Pass an empty collection to clear the filter.
     * Multiple calls will overwrite the previous value.</p>
     *
     * @param blocks The collection of allowed block types (must not be {@code null})
     * @return The current selector instance for chaining
     * @throws NullPointerException if {@code blocks} is {@code null}
     */
    BlockSelector allowBlocks(Collection<Block> blocks);

    /**
     * Excludes blocks of the specified types.
     * <p>This is a setter operation. Pass an empty collection to clear the filter.
     * Multiple calls will overwrite the previous value.</p>
     *
     * @param blocks The collection of excluded block types (must not be {@code null})
     * @return The current selector instance for chaining
     * @throws NullPointerException if {@code blocks} is {@code null}
     */
    BlockSelector denyBlocks(Collection<Block> blocks);

    /**
     * Selects only positions that are on the surface (topmost non-air block).
     * <p>A position is considered on the surface if the block at that position
     * is non-air and the block above it is air.</p>
     *
     * @param surface {@code true} to enable surface-only selection, {@code false} to disable
     * @return The current selector instance for chaining
     */
    BlockSelector surface(boolean surface);

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
