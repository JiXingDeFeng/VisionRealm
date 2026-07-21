package io.github.jixingdefeng.visionrealm.core.erosion.block;

import io.github.jixingdefeng.visionrealm.api.erosion.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.api.erosion.block.ErosionEntry;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionParticle;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.BaseBlockErosionEntry;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.TransformIntoEntity;
import net.minecraft.core.Holder;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Represents a block erosion entry that defines how a block transforms under specific erosion types.
 * <p>
 * This class combines source block information with erosion keys to define complete
 * erosion transformations. Each erosion key contains the erosion type, target,
 * probability, and weight.
 *
 * <p><b>Note:</b> When loading from data packs, the system uses {@link BaseBlockErosionEntry}
 * as the concrete implementation for deserialization.
 *
 * <p><b>JSON Structure:</b>
 * <pre>{
 *   "block": "minecraft:stone",
 *   "probability": 0.5,
 *   "entry": {
 *     "visionrealm:blood": {
 *       "values": [
 *         {
 *           "target": "minecraft:cobblestone",
 *           "probability": 0.3,
 *           "weight": 5,
 *           "predicate": {
 *             "type": "true"
 *           }
 *         }
 *       ]
 *     }
 *   },
 *   "predicate": {
 *     "predicate": {
 *       "type": "true"
 *     }
 *   },
 *   "particles": {
 *     "visionrealm:blood": {
 *       "type": "visionrealm:weighted",
 *       "values": [
 *         {
 *           "config": {                                // inline definition
 *             "type": "visionrealm:default",
 *             "particle": {
 *               "type": "minecraft:heart"
 *             },
 *             "count": 5
 *           },
 *           "weight": 1
 *         },
 *         {
 *           "config": "visionrealm:blood_particles",   // external reference
 *           "weight": 2
 *         }
 *       ]
 *     }
 *   }
 * }</pre>
 *
 * @param <RT> The target type for erosion keys (e.g., {@link Block}, {@link EntityType})
 * @param <R> The final result type after erosion (e.g., {@link BlockState}, {@link Entity})
 * @author JiXingDeFeng
 * @see BaseBlockErosionEntry
 * @see TransformIntoBlock
 * @see TransformIntoEntity
 * @since 0.0.1-dev
 */
public abstract class BlockErosionEntry<RT, R> implements CanBeErosionBlock<RT, R>, WeightedEntry {

    /**
     * Constructor for subclasses to initialize the erosion entry with core data.
     * <p>
     * All parameters are required for complete erosion configuration; subclasses
     * must pass them through to {@link BaseBlockErosionEntry} or handle them accordingly.
     *
     * @param source      The source block this erosion entry applies to
     * @param weight      The weight of this erosion entry relative to others
     * @param entryMap    Map of erosion types to weighted lists of possible targets
     * @param particle    Particle configuration to display when erosion occurs
     * @param predicate   Condition predicate for determining if erosion is allowed
     */
    protected BlockErosionEntry(
            Block source,
            Weight weight,
            Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<RT>>> entryMap,
            ErosionParticle particle,
            BlockErosionPredicate predicate
    ) {}

    /**
     * Abstract base class for block-to-block erosion transformations.
     * <p>
     * This class is intended for subclassing by concrete implementations that define
     * specific block-to-block erosion behaviors.
     *
     * @see BaseBlockErosionEntry
     * @see TransformIntoBlock
     * @since 0.0.1-dev
     */
    public abstract static class AbstractTransformIntoBlock extends BaseBlockErosionEntry<Block, BlockState> {

        /**
         * Protected constructor for subclasses.
         *
         * @param source      The source block to transform
         * @param weight      The weight of this erosion entry
         * @param entryMap    Map of erosion types to weighted target lists
         * @param particle    Particle configuration for this entry
         * @param predicate   Condition predicate for erosion
         */
        protected AbstractTransformIntoBlock(
                Block source,
                Weight weight,
                Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<Block>>> entryMap,
                ErosionParticle particle,
                BlockErosionPredicate predicate
        ) {
            super(source, weight, entryMap, particle, predicate);
        }
    }

    /**
     * Abstract base class for block-to-entity erosion transformations.
     * <p>
     * This class is intended for subclassing by concrete implementations that define
     * specific block-to-entity erosion behaviors.
     *
     * @see BaseBlockErosionEntry
     * @see TransformIntoEntity
     * @since 0.0.1-dev
     */
    public abstract static class AbstractTransformIntoEntity extends BaseBlockErosionEntry<EntityType<?>, Entity> {

        /**
         * Protected constructor for subclasses.
         *
         * @param source      The source block to transform
         * @param weight      The weight of this erosion entry
         * @param entryMap    Map of erosion types to weighted target lists
         * @param particle    Particle configuration for this entry
         * @param predicate   Condition predicate for erosion
         */
        protected AbstractTransformIntoEntity(
                Block source,
                Weight weight,
                Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<EntityType<?>>>> entryMap,
                ErosionParticle particle,
                BlockErosionPredicate predicate
        ) {
            super(source, weight, entryMap, particle, predicate);
        }
    }
}
