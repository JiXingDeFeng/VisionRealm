package io.github.jixingdefeng.visionrealm.api.incident.block;

import io.github.jixingdefeng.visionrealm.api.incident.Incident;
import io.github.jixingdefeng.visionrealm.api.incident.IncidentContext;
import io.github.jixingdefeng.visionrealm.common.incident.context.block.BlockIncidentContext;
import net.minecraft.core.BlockPos;

/**
 * An incident that operates on block targets.
 * <p>
 * This interface extends the generic {@link Incident} with block-specific behavior,
 * providing a convenience method that accepts {@link BlockIncidentContext}.
 * </p>
 *
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public interface BlockIncident extends Incident<BlockPos> {

    /**
     * Executes the incident with a block-specific context.
     *
     * @param context The block incident context
     * @return {@code true} if the incident executed successfully, {@code false} otherwise
     */
    boolean execute(BlockIncidentContext context);

    @Override
    default boolean execute(IncidentContext<BlockPos> context) {
        return this.execute(BlockIncidentContext.of(context));
    }
}
