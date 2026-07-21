package io.github.jixingdefeng.visionrealm.api.selector.game.block;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.common.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.common.selector.SurfaceSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

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
     * <p>This is a setter operation. Calling with no arguments clears the filter.
     * Multiple calls will overwrite the previous value.</p>
     *
     * <p><strong>Note:</strong> Only blocks that are in the whitelist AND NOT in the
     * blacklist will be selected. If the whitelist is empty, any block not in the
     * blacklist is allowed.</p>
     *
     * @param blocks The block types to allow (optional)
     * @return The current selector instance for chaining
     */
    BlockSelector allowBlocks(Block... blocks);

    /**
     * Excludes blocks of the specified types.
     * <p>This is a setter operation. Calling with no arguments clears the filter.
     * Multiple calls will overwrite the previous value.</p>
     *
     * <p><strong>Note:</strong> Blocks in the blacklist are excluded. If a whitelist
     * is also set, blocks must be in the whitelist AND NOT in the blacklist to be
     * allowed. If only the blacklist is set, all blocks except those in the blacklist
     * are allowed.</p>
     *
     * @param blocks The block types to deny (optional)
     * @return The current selector instance for chaining
     */
    BlockSelector denyBlocks(Block... blocks);

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
