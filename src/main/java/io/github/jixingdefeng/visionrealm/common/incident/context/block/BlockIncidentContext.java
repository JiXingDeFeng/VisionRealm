package io.github.jixingdefeng.visionrealm.common.incident.context.block;

import io.github.jixingdefeng.visionrealm.api.incident.IncidentContext;
import io.github.jixingdefeng.visionrealm.common.incident.context.BaseIncidentContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockIncidentContext extends BaseIncidentContext<BlockPos> {
    protected final Collection<BlockState> states;

    public static BlockIncidentContext of(IncidentContext<BlockPos> context) {
        if (context instanceof BlockIncidentContext blockContext) {
            return blockContext;
        } else {
            return new BlockIncidentContext(context);
        }
    }

    public BlockIncidentContext(IncidentContext<BlockPos> context) {
        this(context.getSource(), context.getAllSource(), context.getLevel(), context.getRandom());
    }

    public BlockIncidentContext(
            @Nullable BlockPos source,
            Collection<BlockPos> allSource,
            ServerLevel level,
            RandomSource random
    ) {
        super(source, allSource, level, random);
        List<BlockState> states = new ArrayList<>();
        for (BlockPos pos : this.allSource) {
            states.add(this.level.getBlockState(pos));
        }

        this.states = states;
    }

    public Collection<BlockState> getBlockStates() {
        return this.states;
    }
}
