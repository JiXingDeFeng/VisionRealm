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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class TransformIntoEntity extends BlockErosionKey.AbstractTransformIntoEntity {
    public static final Codec<BaseBlockErosionKey<EntityType<?>, Entity>> CODEC = BaseBlockErosionKey.codec(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec(),
            TransformIntoEntity.EntityKey::new,
            TransformIntoEntity::new
    ).codec();

    public TransformIntoEntity(Block source, float probability, Collection<ErosionKey<EntityType<?>>> typeKeys, List<WeightedParticleGroup> particles) {
        super(source, probability, typeKeys, particles);
    }

    @Nullable
    @Override
    public Entity transformed(EntityType<?> target, Level level, BlockPos pos, ErosionType type, ErosionContext<Block> context) {
        return BlockErosionHandler.transformEntity(target, level, pos, type, context);
    }

    protected record EntityKey(EntityType<?> target, ResourceKey<Biome> biome, float probability, Weight weight) implements ErosionKey<EntityType<?>> {
        @NotNull
        @Override
        public String toString() {
            return "EntityKey[target: " + this.target + ", probability: " + this.probability + "]";
        }
    }
}
