package io.github.jixingdefeng.visionrealm.api.erosion.block;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block.BlockErosionEntry;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;

/**
 * A single possible target for a {@link BlockErosionEntry},
 * specifying the result value, probability, predicate, and weight.
 * <p>
 * Together with other entries of the same {@link ErosionType}, these
 * form the weighted random list inside a block erosion configuration.
 * Each entry can optionally restrict applicability via a
 * {@link BlockPredicate} at the target position.
 *
 * <p><b>JSON Structure:</b>
 * <pre>{
 *   "target": "minecraft:cobblestone",
 *   "probability": 0.3,
 *   "weight": 5,
 *   "predicate": { ... }  // optional block predicate for additional conditions
 * }</pre>
 *
 * @param <T> the type of target value (e.g. {@link Block}, {@link EntityType})
 * @see BlockErosionEntry
 * @author JiXingDeFeng
 * @since 0.0.1-dev
 */
public interface ErosionEntry<T> extends WeightedEntry {

    /**
     * Creates a {@link MapCodec} for {@code ErosionEntry} instances.
     *
     * @param <T>          the type of target value
     * @param targetCodec  Codec used to serialize/deserialize the {@code target} field
     * @param entryFactory Factory that constructs an {@code ErosionEntry}
     *                     from the four decoded fields:
     *                     {@code (target, probability, predicate, weight)}
     * @return a {@code MapCodec} capable of encoding/decoding erosion entries
     */
    static <T> MapCodec<ErosionEntry<T>> codec(
            Codec<T> targetCodec,
            Function4<T, Float, BlockPredicate, Weight, ErosionEntry<T>> entryFactory
    ) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        targetCodec.fieldOf("target").forGetter(ErosionEntry::target),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F)
                                .forGetter(ErosionEntry::probability),
                        BlockPredicate.CODEC.optionalFieldOf("predicate", BlockPredicate.alwaysTrue())
                                .forGetter(ErosionEntry::predicate),
                        Weight.CODEC.optionalFieldOf("weight", Weight.of(1))
                                .forGetter(ErosionEntry::weight)
                ).apply(instance, entryFactory)
        );
    }

    /** @return the target value (e.g. a {@link Block} or {@link EntityType}) */
    T target();

    /** @return the block predicate that must be satisfied at the target position */
    BlockPredicate predicate();

    /** @return the weight of this entry relative to others in the same list */
    Weight weight();

    /** @return the base probability (0.0–1.0) that this entry will be considered */
    float probability();

    @NotNull
    @Override
    default Weight getWeight() {
        return this.weight();
    }

    /**
     * A pairing of an {@link ErosionType} with a specific {@link ErosionEntry},
     * typically produced after a successful random selection from a
     * {@code BlockErosionEntry}'s weighted list.
     *
     * @since 0.0.3-dev
     */
    record SelectedEntry<T>(ErosionType type, ErosionEntry<T> entry) {
        /**
         * Checks whether this selected entry is associated with the given erosion type.
         */
        public boolean isType(ErosionType type) {
            return this.type().equals(type);
        }
    }
}
