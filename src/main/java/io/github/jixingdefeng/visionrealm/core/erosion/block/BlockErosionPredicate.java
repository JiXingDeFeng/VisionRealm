package io.github.jixingdefeng.visionrealm.core.erosion.block;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A predicate that determines whether erosion can proceed at a given position,
 * supporting both a single global predicate and per-{@link ErosionType} predicates.
 *
 * <p><b>JSON Structure</b></p>
 * Two formats are accepted via {@link Either} codec:
 * <ul>
 *   <li><b>Global predicate</b> — applies the same condition to all erosion types:
 *     <pre>{@code "predicate": { "type": "true" }}</pre>
 *   </li>
 *   <li><b>Per‑type predicate map</b> — maps erosion type IDs to their own predicates:
 *     <pre>{@code "predicate": {
 *   "visionrealm:blood": { "type": "true" },
 *   "visionrealm:erosion": { ... }
 * }}</pre>
 *   </li>
 * </ul>
 * If the {@code "predicate"} field is omitted, the default is a global predicate that
 * always returns {@code true}.
 * <p>
 * An additional {@code "fallback"} boolean field (default {@code true}) controls whether
 * a datapack erosion entry should fall back to hardcoded erosion when the predicate fails.
 *
 * @author JiXingDeFeng
 * @see BlockPredicate
 * @since 0.0.3-dev
 */
public abstract sealed class BlockErosionPredicate
        permits BlockErosionPredicate.SingleBlockErosionPredicate, BlockErosionPredicate.BlockErosionPredicateMap {
    public static final Codec<BlockErosionPredicate> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.either(
                            Codec.unboundedMap(RegistryFixedCodec.create(ModRegistries.EROSION_TYPE), BlockPredicate.CODEC),
                            BlockPredicate.CODEC
                    ).fieldOf("predicate").forGetter(BlockErosionPredicate::either),
                    Codec.BOOL.optionalFieldOf("fallback", true)
                            .forGetter(predicate -> predicate.fallbackToHardcoded)
            ).apply(instance, (either, returnToHardCode) -> {
                Optional<BlockPredicate> predicate = either.right();
                return predicate.map(blockPredicate -> new SingleBlockErosionPredicate(blockPredicate, returnToHardCode))
                        .orElseGet(() -> new SingleBlockErosionPredicate(either.right()
                                .orElseThrow(), returnToHardCode));
            })
    );
    protected final boolean fallbackToHardcoded;

    /**
     * Returns a global predicate that always evaluates to {@code true}.
     *
     * @return a singleton instance with {@code fallback = true}
     */
    public static BlockErosionPredicate alwaysTrue() {
        return SingleBlockErosionPredicate.ALWAYS_TRUE;
    }

    protected BlockErosionPredicate(boolean fallbackToHardcoded) {
        this.fallbackToHardcoded = fallbackToHardcoded;
    }

    /**
     * Retrieves the {@link BlockPredicate} applicable for the given erosion type.
     * <p>
     * In a {@link SingleBlockErosionPredicate} the same predicate is returned for all types;
     * in a {@link BlockErosionPredicateMap} the predicate is looked up from the type‑specific map.
     *
     * @param type the erosion type currently being evaluated
     * @return the block predicate to test, or {@code null} if no mapping exists for the type
     */
    public abstract BlockPredicate get(ErosionType type);

    /**
     * Indicates whether the erosion system should fall back to hardcoded erosion
     * when this predicate does not allow erosion at a given position.
     *
     * @return {@code true} if fallback is enabled, {@code false} otherwise
     */
    public boolean useFallback() {
        return this.fallbackToHardcoded;
    }

    /**
     * Provides the raw representation of this predicate as an {@link Either}.
     * <p>
     * The left side represents a per‑type map, the right side a single global predicate.
     * Used internally by the codec for serialization.
     *
     * @return an {@code Either} containing the underlying predicate data
     */
    protected abstract Either<Map<Holder<ErosionType>, BlockPredicate>, BlockPredicate> either();

    /**
     * A global predicate that applies the same {@link BlockPredicate} to all erosion types.
     * <p>
     * Created when the JSON {@code "predicate"} field contains a single predicate object
     * instead of a map.
     *
     * @since 0.0.3-dev
     */
    public static final class SingleBlockErosionPredicate extends BlockErosionPredicate {
        public static final BlockErosionPredicate ALWAYS_TRUE = new SingleBlockErosionPredicate(BlockPredicate.alwaysTrue(), true);
        private final BlockPredicate predicate;

        public SingleBlockErosionPredicate(@NotNull BlockPredicate predicate, boolean fallbackToHardcoded) {
            super(fallbackToHardcoded);
            this.predicate = predicate;
        }

        @Override
        public BlockPredicate get(ErosionType type) {
            return this.predicate;
        }

        @Override
        public Either<Map<Holder<ErosionType>, BlockPredicate>, BlockPredicate> either() {
            return Either.right(this.predicate);
        }
    }

    /**
     * A per‑type predicate map that stores distinct {@link BlockPredicate} instances
     * keyed by {@link ErosionType}.
     * <p>
     * Created when the JSON {@code "predicate"} field contains a map of erosion type IDs
     * to predicate objects. The constructor converts {@link Holder}<{@link ErosionType}>
     * keys into plain {@link ErosionType} references for efficient lookup.
     *
     * @since 0.0.3-dev
     */
    public static final class BlockErosionPredicateMap extends BlockErosionPredicate {
        private final Map<ErosionType, BlockPredicate> predicateMap;

        /**
         * Constructs a per‑type predicate map.
         *
         * @param map                map of erosion type holders to their predicates
         * @param fallbackToHardcoded whether to fall back to hardcoded erosion when the predicate fails
         */
        public BlockErosionPredicateMap(@NotNull Map<Holder<ErosionType>, BlockPredicate> map, boolean fallbackToHardcoded) {
            super(fallbackToHardcoded);
            this.predicateMap = new Object2ObjectOpenHashMap<>(map.size());
            map.forEach((key, value) -> this.predicateMap.put(key.value(), value));
        }

        @Override
        public BlockPredicate get(ErosionType type) {
            return this.predicateMap.get(type);
        }

        @Override
        public Either<Map<Holder<ErosionType>, BlockPredicate>, BlockPredicate> either() {
            Map<Holder<ErosionType>, BlockPredicate> map = new HashMap<>(this.predicateMap.size());
            this.predicateMap.forEach((key, value) ->
                    map.put(Holder.direct(key), value)
            );
            return Either.left(map);
        }
    }
}
