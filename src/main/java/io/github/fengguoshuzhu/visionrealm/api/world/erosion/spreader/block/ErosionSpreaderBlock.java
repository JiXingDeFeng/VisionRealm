package io.github.fengguoshuzhu.visionrealm.api.world.erosion.spreader.block;

import io.github.fengguoshuzhu.visionrealm.api.world.erosion.spreader.ErosionSpreader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface ErosionSpreaderBlock extends ErosionSpreader {

    void randomTickSpreader(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);
}
