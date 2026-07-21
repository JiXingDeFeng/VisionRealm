package io.github.jixingdefeng.visionrealm.mixin.world.server;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.jixingdefeng.visionrealm.api.erosion.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.util.erosion.ErosionUtil;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Unique private final ServerLevel visionRealm$level = (ServerLevel) (Object) this;

    @Inject(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isRandomlyTicking()Z"
            )
    )
    private void tickChunk(
            LevelChunk chunk,
            int randomTickSpeed,
            CallbackInfo ci,
            @Local(name = "blockstate") BlockState blockstate,
            @Local(name = "blockpos1") BlockPos blockpos1
    ) {
        Level level = chunk.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            visionRealm$randomTickErosion(blockstate, blockpos1, serverLevel, serverLevel.getRandom());
        }
    }

    @Unique
    private static void visionRealm$randomTickErosion(BlockState state, BlockPos pos, ServerLevel level, RandomSource random) {
        Block block = state.getBlock();
        ErosionType type = ErosionUtil.getBiomeErosionType(pos, level);
        if (type.isValidFor(block)) {
            CanBeErosionBlock<?, ?> canBeErosion = ErosionUtil.getCanBeErosion(block, level, pos, type);
            if (canBeErosion != null) {
                canBeErosion.randomTickInfection(state, level, pos, random, type);
            }
        }
    }
}
