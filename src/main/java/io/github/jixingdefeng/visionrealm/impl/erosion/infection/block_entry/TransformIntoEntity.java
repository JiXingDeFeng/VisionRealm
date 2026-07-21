package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry;

import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.api.erosion.block.ErosionEntry;
import io.github.jixingdefeng.visionrealm.common.erosion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.BlockErosionHandler;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionParticle;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.core.erosion.block.BlockErosionPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TransformIntoEntity extends BlockErosionEntry.AbstractTransformIntoEntity {
    public static final Codec<BaseBlockErosionEntry<EntityType<?>, Entity>> CODEC = BaseBlockErosionEntry.codec(
            ErosionEntry.codec(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec(), EntityEntry::new
            ),
            TransformIntoEntity::new
    ).codec();

    public TransformIntoEntity(
            Block source,
            Weight weight,
            Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<EntityType<?>>>> entryMap,
            ErosionParticle particle,
            BlockErosionPredicate predicate
    ) {
        super(source, weight, entryMap, particle, predicate);
    }

    @Nullable
    @Override
    public Entity transformed(EntityType<?> target, Level level, BlockPos pos, ErosionType type, BlockErosionContext context) {
        return BlockErosionHandler.transformEntity(target, level, pos, type, context);
    }

    protected record EntityEntry(EntityType<?> target, float probability, BlockPredicate predicate,
                                 Weight weight) implements ErosionEntry<EntityType<?>> {
        @NotNull
        @Override
        public String toString() {
            return "EntityEntry[target: " + this.target + ", probability: " + this.probability + ", weight: " + this.weight + "]";
        }
    }
}
