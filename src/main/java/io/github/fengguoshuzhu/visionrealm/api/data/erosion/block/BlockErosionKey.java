package io.github.fengguoshuzhu.visionrealm.api.data.erosion.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fengguoshuzhu.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.BaseBlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.TransformIntoBlock;
import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.TransformIntoEntity;
import io.github.fengguoshuzhu.visionrealm.impl.particle.WeightedParticleConfig;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

/**
 * Represents a block erosion key that defines how a block transforms under specific erosion types.
 * <p>
 * This class combines source block information with erosion keys and optional particle effects
 * to define complete erosion transformations.
 *
 * <p><b>JSON Structure:</b>
 * <pre>{
 *   "block": "minecraft:stone",
 *   "probability": 0.5,
 *   "keys": [
 *     {
 *       "target": "minecraft:cobblestone",
 *       "biome": "minecraft:plains",
 *       "probability": 0.3,
 *       "weight": 5
 *     }
 *   ],
 *   "particles": [
 *     {
 *       "type": "visionrealm:blood",
 *       "config": [
 *         {
 *           "particle": {
 *             "type": "minecraft:flame"
 *           },
 *           "count": 5,
 *           "weight": 3
 *         }
 *       ]
 *     }
 *   ]
 * }</pre>
 *
 * @param <T> The target type for erosion keys
 * @param <R> The final result type after erosion
 * @author JiXingDeFeng
 * @see BaseBlockErosionKey
 * @see TransformIntoBlock
 * @see TransformIntoEntity
 * @since 1.0.0
 */
public abstract class BlockErosionKey<T, R> implements CanBeErosionBlock<T, R>, WeightedEntry {

    /**
     * Constructor for subclasses to initialize the erosion key with core data.
     *
     * @param source            The source block this erosion key applies to
     * @param probability       Base probability of erosion occurring
     * @param typeKeys          Collection of erosion keys defining target types and conditions
     * @param particleGroupList Collection of particle groups for visual effects
     */
    protected BlockErosionKey(Block source, float probability, Collection<BaseBlockErosionKey.ErosionKey<T>> typeKeys, Collection<BaseBlockErosionKey.WeightedParticleGroup> particleGroupList) {
    }

    /**
     * Abstract base class for block-to-block erosion transformations.
     * <p>
     * This class is intended for subclassing by concrete implementations that define
     * specific block-to-block erosion behaviors.
     *
     * @see BaseBlockErosionKey
     * @see TransformIntoBlock
     * @since 1.0.0
     */
    public abstract static class AbstractTransformIntoBlock extends BaseBlockErosionKey<Block, BlockState> {

        /**
         * Protected constructor for subclasses.
         *
         * @param source            The source block to transform
         * @param probability       Base probability of erosion
         * @param typeKeys          Collection of erosion keys defining target blocks and conditions
         * @param particleGroupList Collection of particle groups for visual effects
         */
        protected AbstractTransformIntoBlock(Block source, float probability, Collection<BaseBlockErosionKey.ErosionKey<Block>> typeKeys, Collection<BaseBlockErosionKey.WeightedParticleGroup> particleGroupList) {
            super(source, probability, typeKeys, particleGroupList);
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
     * @since 1.0.0
     */
    public abstract static class AbstractTransformIntoEntity extends BaseBlockErosionKey<EntityType<?>, Entity> {

        /**
         * Protected constructor for subclasses.
         *
         * @param source            The source block to transform
         * @param probability       Base probability of erosion
         * @param typeKeys          Collection of erosion keys defining target entity types and conditions
         * @param particleGroupList Collection of particle groups for visual effects
         */
        protected AbstractTransformIntoEntity(Block source, float probability, Collection<ErosionKey<EntityType<?>>> typeKeys, Collection<WeightedParticleGroup> particleGroupList) {
            super(source, probability, typeKeys, particleGroupList);
        }
    }

    /**
     * Internal record for storing particle configuration data during initialization.
     * <p>
     * This record serves as an intermediate data structure when loading particle
     * configurations from data packs. Its contents are processed during erosion key
     * initialization and stored in optimized internal maps for runtime use.
     * <strong>Not intended for direct use outside of erosion key initialization.</strong>
     * </p>
     *
     * <p><b>JSON Structure (for reference):</b>
     * <pre>{
     *   "type": "visionrealm:blood",
     *   "config": [
     *     {
     *       "particle": {
     *         "type": "minecraft:flame"
     *       },
     *       "count": 5,
     *       "speed": 0.1,
     *       "spread": 2.0,
     *       "offset": [0.5, 0.5, 0.5],
     *       "weight": 3
     *     }
     *   ]
     * }</pre>
     *
     * @param particle The weighted list of particle configurations
     * @param type     The erosion type that triggers these particles
     * @see BaseBlockErosionKey
     * @see TransformIntoBlock
     * @see TransformIntoEntity
     * @since 1.0.0
     */
    public record WeightedParticleGroup(WeightedRandomList<WeightedParticleConfig> particle, ErosionType type) {
        /**
         * Codec for deserializing particle groups from data packs during initialization.
         */
        public static final Codec<BaseBlockErosionKey.WeightedParticleGroup> CODEC = RecordCodecBuilder.create(particle ->
                particle.group(
                        WeightedRandomList.codec(WeightedParticleConfig.CODEC).fieldOf("config").forGetter(BlockErosionKey.WeightedParticleGroup::particle),
                        ErosionType.CODEC.fieldOf("type").forGetter(BlockErosionKey.WeightedParticleGroup::type)
                ).apply(particle, BlockErosionKey.WeightedParticleGroup::new)
        );
    }
}
