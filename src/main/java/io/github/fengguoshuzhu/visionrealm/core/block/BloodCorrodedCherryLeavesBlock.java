package io.github.fengguoshuzhu.visionrealm.core.block;

import com.mojang.serialization.MapCodec;
import io.github.fengguoshuzhu.visionrealm.api.erosion.infection.ImmuneErosion;
import io.github.fengguoshuzhu.visionrealm.core.particle.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BloodCorrodedCherryLeavesBlock extends LeavesBlock implements ImmuneErosion {
    public static final MapCodec<BloodCorrodedCherryLeavesBlock> CODEC = simpleCodec(BloodCorrodedCherryLeavesBlock::new);

    @NotNull
    @Override
    public MapCodec<BloodCorrodedCherryLeavesBlock> codec() {
        return CODEC;
    }

    public BloodCorrodedCherryLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(10) == 0) {
            BlockPos blockPos = pos.below();
            BlockState blockState = level.getBlockState(blockPos);
            if (!isFaceFull(blockState.getCollisionShape(level, blockPos), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(level, pos, random, ModParticleTypes.BLOOD_CORRODED_CHERRY.get());
            }
        }
    }
}
