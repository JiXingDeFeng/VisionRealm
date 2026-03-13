package io.github.fengguoshuzhu.visionrealm.impl.erosion.block;

import com.mojang.serialization.Codec;
import io.github.fengguoshuzhu.visionrealm.api.data.erosion.block.BlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.common.erosion.context.block.ErosionContext;
import io.github.fengguoshuzhu.visionrealm.core.handle.erosion.block.BlockErosionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class TransformIntoBlock extends BlockErosionKey.AbstractTransformIntoBlock {
    public static final Codec<BaseBlockErosionKey<Block, BlockState>> CODEC = BaseBlockErosionKey.codec(
            BuiltInRegistries.BLOCK.byNameCodec(),
            TransformIntoBlock.BlockKey::new,
            TransformIntoBlock::new
    ).codec();

    public TransformIntoBlock(Block source, float probability, Collection<ErosionKey<Block>> typeKeys, List<WeightedParticleGroup> particles) {
        super(source, probability, typeKeys, particles);
    }

    @Nullable
    @Override
    public BlockState transformed(Block target, Level level, BlockPos pos, ErosionType type, ErosionContext<Block> context) {
        return BlockErosionHandler.transformBlock(target, level, pos, type, context);
    }

    protected record BlockKey<T extends Block>(T target, ResourceKey<Biome> biome, float probability, Weight weight) implements ErosionKey<T> {

        @NotNull
        @Override
        public String toString() {
            return "BlockKey[target: " + this.target + ", probability: " + this.probability + "]";
        }
    }
}
