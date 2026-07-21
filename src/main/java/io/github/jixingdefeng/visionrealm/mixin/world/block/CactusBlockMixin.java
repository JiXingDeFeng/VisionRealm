package io.github.jixingdefeng.visionrealm.mixin.world.block;

import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CactusBlock.class)
public class CactusBlockMixin {

    @SuppressWarnings("deprecation")
    @Inject(method = "canSurvive", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void canSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState1) {
        cir.setReturnValue((blockState1.is(Blocks.CACTUS) || blockState1.is(ModBlocks.ANOMALY_CACTUS) || blockState1.is(BlockTags.SAND)) && !level.getBlockState(pos.above()).liquid());
        cir.cancel();
    }
}
