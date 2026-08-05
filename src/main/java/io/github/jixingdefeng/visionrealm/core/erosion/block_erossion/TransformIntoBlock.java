package io.github.jixingdefeng.visionrealm.core.erosion.block_erossion;

import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.handle.BlockErosionHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TransformIntoBlock extends BlockErosionEntry<Block, BlockState> {

    public TransformIntoBlock(
            Block source,
            ErosionType erosionType,
            Block target,
            @Nullable ResourceLocation particle,
            @Nullable ResourceLocation predicate,
            float probability,
            boolean allowHardCoded,
            Weight weight
    ) {
        super(source, erosionType, target, particle, predicate, probability, allowHardCoded, weight);
    }

    @Nullable
    @Override
    public BlockState transformed(Block target, BlockErosionContext context) {
        return BlockErosionHandle.transformBlock(target, context);
    }
}
