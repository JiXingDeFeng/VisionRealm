package io.github.jixingdefeng.visionrealm.common.data.erosion.block;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.BaseBlockErosionEntry;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.block.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.entity.TransformIntoEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a block erosion key that defines how a block transforms under specific erosion types.
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
 *   "entry": [
 *     {
 *       "type": "visionrealm:blood",
 *       "target": "minecraft:cobblestone",
 *       "probability": 0.3,
 *       "weight": 5,
 *       "predicate": {
 *         "type": "true"
 *       }
 *     }
 *   ],
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
     * Constructor for subclasses to initialize the erosion key with core data.
     *
     * @param source      The source block this erosion key applies to
     * @param probability Base probability of erosion occurring
     * @param entryList    Collection of erosion keys defining target types and conditions
     * @param particles   Map of erosion types to particle configuration resource locations
     */
    protected BlockErosionEntry(
            Block source,
            float probability,
            Collection<ErosionEntry<RT>> entryList,
            Map<ErosionType, ResourceLocation> particles
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
         * @param probability Base probability of erosion
         * @param entryList   Collection of erosion keys defining target blocks and conditions
         * @param particles   Map of erosion types to particle configuration resource locations
         * @param predicate   Block predicate for additional conditions (can be global or per-type)
         */
        protected AbstractTransformIntoBlock(
                Block source,
                float probability,
                Collection<ErosionEntry<Block>> entryList,
                Map<ErosionType, ResourceLocation> particles,
                Predicate predicate
        ) {
            super(source, probability, entryList, particles, predicate);
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
         * @param probability Base probability of erosion
         * @param entryList   Collection of erosion keys defining target entity types and conditions
         * @param particles   Map of erosion types to particle configuration resource locations
         * @param predicate   Block predicate for additional conditions (can be global or per-type)
         */
        protected AbstractTransformIntoEntity(
                Block source,
                float probability,
                Collection<ErosionEntry<EntityType<?>>> entryList,
                Map<ErosionType, ResourceLocation> particles,
                Predicate predicate
        ) {
            super(source, probability, entryList, particles, predicate);
        }
    }

    /**
     * Represents a single erosion key that defines a target transformation under specific conditions.
     * <p>
     * This interface defines the core data structure for mapping a target value (e.g., a block,
     * entity type) to an erosion type with associated probability, weight, and optional predicate.
     *
     * <p><b>JSON Structure:</b>
     * <pre>{
     *   "type": "visionrealm:blood",
     *   "target": "minecraft:cobblestone",
     *   "probability": 0.3,
     *   "weight": 5,
     *   "predicate": { ... }  // optional block predicate for additional conditions
     * }</pre>
     *
     * @param <T> The type of target value (e.g., {@link Block}, {@link EntityType})
     * @since 0.0.1-dev
     */
    public interface ErosionEntry<T> extends WeightedEntry {
        static <T> MapCodec<ErosionEntry<T>> codec(
                final Codec<T> targetCodec,
                final Function5<ErosionType, T, Float, BlockPredicate, Weight, ErosionEntry<T>> entryFactory
        ) {
            return RecordCodecBuilder.mapCodec(keyInstance ->
                    keyInstance.group(
                            ErosionType.CODEC.fieldOf("type").forGetter(ErosionEntry::type),
                            targetCodec.fieldOf("target").forGetter(ErosionEntry::target),
                            Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(ErosionEntry::probability),
                            BlockPredicate.CODEC.optionalFieldOf("predicate", BlockPredicate.alwaysTrue()).forGetter(ErosionEntry::predicate),
                            Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(ErosionEntry::weight)
                    ).apply(keyInstance, entryFactory)
            );
        }

        T target();

        BlockPredicate predicate();

        ErosionType type();

        Weight weight();

        float probability();

        @NotNull
        @Override
        default Weight getWeight() {
            return this.weight();
        }
    }


    /**
     * A container for block predicates that can be either global or per erosion type.
     * <p>
     * Supports two JSON formats:
     * <ul>
     *   <li>Single predicate applied to all erosion types: {@code "predicate": {...}}</li>
     *   <li>Per-type predicates: {@code "predicate": {"erosion_type": {...}}}</li>
     * </ul>
     *
     * @since 0.0.2-dev
     */
    public static class Predicate {
        public static final Codec<Predicate> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.either(
                                Codec.unboundedMap(ErosionType.CODEC, BlockPredicate.CODEC),
                                BlockPredicate.CODEC
                        ).fieldOf("predicate").forGetter(Predicate::either),
                        Codec.BOOL.optionalFieldOf("fallback", true)
                                .forGetter(predicate -> predicate.fallbackToHardcoded)
                ).apply(instance, (either, returnToHardCode) -> {
                    Optional<BlockPredicate> predicate = either.right();
                    return predicate.map(blockPredicate -> new Predicate(blockPredicate, returnToHardCode))
                            .orElseGet(() -> new Predicate(either.orThrow(), returnToHardCode));
                })
        );
        public static final Predicate ALWAYS_TRUE = new Predicate(BlockPredicate.alwaysTrue(), true);
        private final Map<ErosionType, BlockPredicate> map;
        private final BlockPredicate predicate;
        private final boolean fallbackToHardcoded;

        public Predicate(@NotNull Map<ErosionType, BlockPredicate> map, boolean fallbackToHardcoded) {
            this.map = map;
            this.predicate = null;
            this.fallbackToHardcoded = fallbackToHardcoded;
        }

        public Predicate(@NotNull BlockPredicate predicate, boolean fallbackToHardcoded) {
            this.predicate = predicate;
            this.map = Maps.newHashMap();
            this.fallbackToHardcoded = fallbackToHardcoded;
        }

        public BlockPredicate get(ErosionType type) {
            if (this.predicate != null) {
                return this.predicate;
            } else {
                return this.map.get(type);
            }
        }

        public boolean useFallback() {
            return this.fallbackToHardcoded;
        }

        private Either<Map<ErosionType, BlockPredicate>, BlockPredicate> either() {
            return this.predicate != null
                   ? Either.right(this.predicate)
                   : Either.left(this.map);
        }
    }
}
