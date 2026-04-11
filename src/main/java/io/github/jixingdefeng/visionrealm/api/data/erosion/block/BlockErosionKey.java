package io.github.jixingdefeng.visionrealm.api.data.erosion.block;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.BaseBlockErosionKey;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.block.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.entity.TransformIntoEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a block erosion key that defines how a block transforms under specific erosion types.
 * <p>
 * This class combines source block information with erosion keys to define complete
 * erosion transformations. Each erosion key contains the erosion type, target,
 * probability, and weight.
 *
 * <p><b>Note:</b> When loading from data packs, the system uses {@link BaseBlockErosionKey}
 * as the concrete implementation for deserialization.
 *
 * <p><b>JSON Structure:</b>
 * <pre>{
 *   "block": "minecraft:stone",
 *   "probability": 0.5,
 *   "keys": [
 *     {
 *       "type": "visionrealm:blood",
 *       "target": "minecraft:cobblestone",
 *       "probability": 0.3,
 *       "weight": 5
 *     }
 *   ],
 *   "particles": {
 *     "visionrealm:blood": "visionrealm:blood_particles"
 *   }
 * }</pre>
 *
 * @param <T> The target type for erosion keys (e.g., {@link Block}, {@link EntityType})
 * @param <R> The final result type after erosion (e.g., {@link BlockState}, {@link Entity})
 * @author JiXingDeFeng
 * @see BaseBlockErosionKey
 * @see TransformIntoBlock
 * @see TransformIntoEntity
 * @since 0.0.1-dev-1
 */
public abstract class BlockErosionKey<T, R> implements CanBeErosionBlock<T, R>, WeightedEntry {

    /**
     * Constructor for subclasses to initialize the erosion key with core data.
     *
     * @param source      The source block this erosion key applies to
     * @param probability Base probability of erosion occurring
     * @param typeKeys    Collection of erosion keys defining target types and conditions
     * @param particles   Map of erosion types to particle configuration resource locations
     */
    protected BlockErosionKey(Block source, float probability, Collection<ErosionKey<T>> typeKeys, Map<ErosionType, ResourceLocation> particles) {
    }

    /**
     * Abstract base class for block-to-block erosion transformations.
     * <p>
     * This class is intended for subclassing by concrete implementations that define
     * specific block-to-block erosion behaviors.
     *
     * @see BaseBlockErosionKey
     * @see TransformIntoBlock
     * @since 0.0.1-dev-1
     */
    public abstract static class AbstractTransformIntoBlock extends BaseBlockErosionKey<Block, BlockState> {

        /**
         * Protected constructor for subclasses.
         *
         * @param source            The source block to transform
         * @param probability       Base probability of erosion
         * @param typeKeys          Collection of erosion keys defining target blocks and conditions
         * @param particles   Map of erosion types to particle configuration resource locations
         */
        protected AbstractTransformIntoBlock(
                Block source, float probability, Collection<ErosionKey<Block>> typeKeys, Map<ErosionType, ResourceLocation> particles
        ) {
            super(source, probability, typeKeys, particles);
        }
    }

    /**
     * Abstract base class for block-to-entity erosion transformations.
     * <p>
     * This class is intended for subclassing by concrete implementations that define
     * specific block-to-entity erosion behaviors.
     *
     * @see BaseBlockErosionKey
     * @see TransformIntoEntity
     * @since 0.0.1-dev-1
     */
    public abstract static class AbstractTransformIntoEntity extends BaseBlockErosionKey<EntityType<?>, Entity> {

        /**
         * Protected constructor for subclasses.
         *
         * @param source            The source block to transform
         * @param probability       Base probability of erosion
         * @param typeKeys          Collection of erosion keys defining target entity types and conditions
         * @param particles   Map of erosion types to particle configuration resource locations
         */
        protected AbstractTransformIntoEntity(
                Block source, float probability, Collection<ErosionKey<EntityType<?>>> typeKeys, Map<ErosionType, ResourceLocation> particles
        ) {
            super(source, probability, typeKeys, particles);
        }
    }

    /**
     * Represents a single erosion key that defines a target transformation under specific conditions.
     * <p>
     * This interface defines the core data structure for mapping a target value (e.g., a block,
     * entity type) to an erosion type with associated probability and weight.
     *
     * <p><b>JSON Structure:</b>
     * <pre>{
     *   "type": "visionrealm:blood",
     *   "target": "minecraft:cobblestone",
     *   "probability": 0.3,
     *   "weight": 5
     * }</pre>
     *
     * @param <T> The type of target value (e.g., {@link Block}, {@link EntityType})
     * @since 0.0.1-dev-1
     */
    public interface ErosionKey<T> extends WeightedEntry {

        static <T> MapCodec<ErosionKey<T>> codec(
                Codec<T> targetCodec, final Function4<ErosionType, T, Float, Weight, ErosionKey<T>> keyFactory
        ) {
            return RecordCodecBuilder.mapCodec(keyInstance ->
                    keyInstance.group(
                            ErosionType.CODEC.fieldOf("type").forGetter(ErosionKey::type),
                            targetCodec.fieldOf("target").forGetter(ErosionKey::target),
                            Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(ErosionKey::probability),
                            Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(ErosionKey::weight)
                    ).apply(keyInstance, keyFactory)
            );
        }

        T target();

        ErosionType type();

        Weight weight();

        float probability();

        @NotNull
        @Override
        default Weight getWeight() {
            return this.weight();
        }
    }
}
