package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.entity;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class TransformIntoEntity extends BlockErosionKey.AbstractTransformIntoEntity {
    public static final Codec<BaseBlockErosionKey<EntityType<?>, Entity>> CODEC = BaseBlockErosionKey.codec(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec(),
            TransformIntoEntity.EntityKey::new,
            TransformIntoEntity::new
    ).codec();

    public TransformIntoEntity(Block source, float probability, Collection<ErosionKey<EntityType<?>>> typeKeys, Map<ErosionType, ResourceLocation> particles) {
        super(source, probability, typeKeys, particles);
    }

    @Nullable
    @Override
    public Entity transformed(EntityType<?> target, Level level, BlockPos pos, ErosionType type, BlockErosionContext<Block> context) {
        return BlockErosionHandler.transformEntity(target, level, pos, type, context);
    }

    protected record EntityKey(ErosionType type, EntityType<?> target,
                               float probability, Weight weight) implements ErosionKey<EntityType<?>> {
        @NotNull
        @Override
        public String toString() {
            return "EntityKey[type: " + this.type + ", target: " + this.target + ", probability: " + this.probability + ", weight: " + this.weight + "]";
        }
    }
}
