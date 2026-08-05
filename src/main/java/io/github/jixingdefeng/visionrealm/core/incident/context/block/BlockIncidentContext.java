package io.github.jixingdefeng.visionrealm.core.incident.context.block;

import io.github.jixingdefeng.visionrealm.core.incident.context.IncidentContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.Collection;

public class BlockIncidentContext extends IncidentContext<BlockPos> {

    public static BlockIncidentContext of(IncidentContext<BlockPos> context) {
        if (context instanceof BlockIncidentContext blockContext) {
            return blockContext;
        } else {
            return new BlockIncidentContext(context);
        }
    }

    public BlockIncidentContext(IncidentContext<BlockPos> context) {
        this(context.getAllTarget(), context.getLevel(), context.getRandom());
    }

    public BlockIncidentContext(Collection<BlockPos> allSource, ServerLevel level, RandomSource random) {
        super(allSource, level, random);
    }
}
