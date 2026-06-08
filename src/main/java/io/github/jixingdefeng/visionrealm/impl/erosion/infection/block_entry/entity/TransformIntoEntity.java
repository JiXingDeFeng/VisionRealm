package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.entity;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class TransformIntoEntity extends BlockErosionEntry.AbstractTransformIntoEntity {
    public static final Codec<BaseBlockErosionEntry<EntityType<?>, Entity>> CODEC = BaseBlockErosionEntry.codec(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec(),
            EntityEntry::new,
            TransformIntoEntity::new
    ).codec();

    public TransformIntoEntity(
            Block source,
            float probability,
            Collection<ErosionEntry<EntityType<?>>> entryList,
            Map<ErosionType, ResourceLocation> particles,
            Predicate predicate
    ) {
        super(source, probability, entryList, particles, predicate);
    }

    @Nullable
    @Override
    public Entity transformed(EntityType<?> target, Level level, BlockPos pos, ErosionType type, BlockErosionContext<Block> context) {
        return BlockErosionHandler.transformEntity(target, level, pos, type, context);
    }

    protected record EntityEntry(ErosionType type, EntityType<?> target, float probability,
                                 BlockPredicate predicate, Weight weight) implements ErosionEntry<EntityType<?>> {
        @NotNull
        @Override
        public String toString() {
            return "EntityEntry[type: " + this.type + ", target: " + this.target + ", probability: " + this.probability + ", weight: " + this.weight + "]";
        }
    }
}
