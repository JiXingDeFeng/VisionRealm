package io.github.jixingdefeng.visionrealm.core.erosion.block_erossion;

import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.handle.BlockErosionHandle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class TransformIntoEntity extends BlockErosionEntry<EntityType<?>, Entity> {

    public TransformIntoEntity(
            Block source,
            ErosionType erosionType,
            EntityType<?> target,
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
    public Entity transformed(EntityType<?> target, BlockErosionContext context) {
        return BlockErosionHandle.transformEntity(target, context);
    }
}
