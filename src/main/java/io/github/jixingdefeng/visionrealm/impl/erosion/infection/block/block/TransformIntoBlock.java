package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.block;

import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.api.data.erosion.block.BlockErosionKey;
import io.github.jixingdefeng.visionrealm.common.context.erosion.infection.block.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.common.handle.erosion.infection.block.BlockErosionHandler;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.BaseBlockErosionKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class TransformIntoBlock extends BlockErosionKey.AbstractTransformIntoBlock {
    public static final Codec<BaseBlockErosionKey<Block, BlockState>> CODEC = BaseBlockErosionKey.codec(
            BuiltInRegistries.BLOCK.byNameCodec(),
            TransformIntoBlock.BlockKey::new,
            TransformIntoBlock::new
    ).codec();

    public TransformIntoBlock(Block source, float probability, Collection<ErosionKey<Block>> typeKeys, Map<ErosionType, ResourceLocation> particles) {
        super(source, probability, typeKeys, particles);
    }

    @Nullable
    @Override
    public BlockState transformed(Block target, Level level, BlockPos pos, ErosionType type, BlockErosionContext<Block> context) {
        return BlockErosionHandler.transformBlock(target, level, pos, type, context);
    }

    protected record BlockKey<T extends Block>(ErosionType type, T target, float probability, Weight weight) implements ErosionKey<T> {

        @NotNull
        @Override
        public String toString() {
            return "BlockKey[type: " + this.type + ", target: " + this.target + ", probability: " + this.probability + ", weight: " + this.weight + "]";
        }
    }
}
