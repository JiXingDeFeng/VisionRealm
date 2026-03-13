package io.github.fengguoshuzhu.visionrealm.mixin.world.server;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.fengguoshuzhu.visionrealm.api.erosion.infection.ImmuneErosion;
import io.github.fengguoshuzhu.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.fengguoshuzhu.visionrealm.api.erosion.spreader.block.ErosionSpreaderBlock;
import io.github.fengguoshuzhu.visionrealm.common.util.world.erosion.ErosionUtil;
import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.BaseBlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.core.block.state.BlockBehaviourExpand;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.common.erosion.manager.block.BlockErosionKeyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
            @Local BlockState blockstate,
            @Local BlockPos blockpos1
    ) {
        Level level = chunk.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            Block block = blockstate.getBlock();
            if (block instanceof ErosionSpreaderBlock spreaderBlock) {
                spreaderBlock.randomTickSpreader(blockstate, serverLevel, blockpos1, serverLevel.getRandom());
            }

            ErosionType type = ErosionUtil.getBiomeErosionType(blockpos1, level);
            if (!(block instanceof ImmuneErosion immuneErosion && immuneErosion.immune(type))) {
                if (block instanceof CanBeErosionBlock<?, ?> canBeErodedBlock
                        && (!(block instanceof BlockBehaviourExpand expand) || expand.canBeEroded())) {
                    canBeErodedBlock.randomTickInfection(blockstate, serverLevel, blockpos1, serverLevel.getRandom());
                } else {
                    BaseBlockErosionKey<?, ?> blockErosionKey = BlockErosionKeyManager.getInstance().get(block);
                    if (blockErosionKey != null && serverLevel.getRandom().nextFloat() <= blockErosionKey.conversionProbability()) {
                        blockErosionKey.randomTickInfection(blockstate, serverLevel, blockpos1, serverLevel.getRandom());
                    }
                }
            }
        }
    }
}
