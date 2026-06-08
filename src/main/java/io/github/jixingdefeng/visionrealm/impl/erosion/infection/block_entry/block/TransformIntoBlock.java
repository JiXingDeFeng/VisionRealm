package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.block;

import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.common.data.erosion.block.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.common.erosion.context.infection.block.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.block.BlockErosionHandler;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.BaseBlockErosionEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class TransformIntoBlock extends BlockErosionEntry.AbstractTransformIntoBlock {
    public static final Codec<BaseBlockErosionEntry<Block, BlockState>> CODEC = BaseBlockErosionEntry.codec(
            BuiltInRegistries.BLOCK.byNameCodec(),
            BlockEntry::new,
            TransformIntoBlock::new
    ).codec();

    public TransformIntoBlock(
            Block source,
            float probability,
            Collection<ErosionEntry<Block>> entryList,
            Map<ErosionType, ResourceLocation> particles,
            Predicate predicate
    ) {
        super(source, probability, entryList, particles, predicate);
    }

    @Nullable
    @Override
    public BlockState transformed(Block target, Level level, BlockPos pos, ErosionType type, BlockErosionContext<Block> context) {
        return BlockErosionHandler.transformBlock(target, level, pos, type, context);
    }

    protected record BlockEntry<T extends Block>(ErosionType type, T target, float probability,
                                                 BlockPredicate predicate, Weight weight) implements ErosionEntry<T> {

        @NotNull
        @Override
        public String toString() {
            return "BlockEntry[type: " + this.type + ", target: " + this.target + ", probability: " + this.probability + ", weight: " + this.weight + "]";
        }
    }
}
