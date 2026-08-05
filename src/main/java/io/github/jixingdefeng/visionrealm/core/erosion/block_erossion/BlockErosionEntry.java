package io.github.jixingdefeng.visionrealm.core.erosion.block_erossion;

import com.mojang.datafixers.util.Function8;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.erosion.block.ErodibleBlock;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistries;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.core.particle_config.ParticleConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a block erosion entry that defines how a block transforms under specific erosion types.
 * <p>
 * This class combines source block information with erosion type, target, probability,
 * weight, and optional particle/predicate configurations to define a complete erosion
 * transformation.
 *
 * <p><b>JSON Structure:</b>
 * <pre>{@code {
 *   "block": "minecraft:stone",
 *   "values": {
 *     "visionrealm:blood": {
 *       "particle": "visionrealm:blood_particles",           // default particles for all entries
 *       "predicate": {                                       // default predicate for all entries
 *         "type": "true"
 *       },
 *       "values": [
 *         {
 *           "target": "minecraft:cobblestone",
 *           "probability": 0.3,
 *           "weight": 5,
 *           "predicate": {
 *             "type": "true"
 *           },
 *           "particle": {
 *             "type": "visionrealm:weighted",
 *             "values": [
 *               {
 *                 "config": {                                // inline definition
 *                   "type": "visionrealm:default",
 *                   "particle": { "type": "minecraft:heart" },
 *                   "count": 5
 *                 },
 *                 "weight": 1
 *               },
 *               {
 *                 "config": "visionrealm:blood_particles",   // external reference
 *                 "weight": 2
 *               }
 *             ]
 *           }
 *         }
 *       ]
 *     }
 *   }
 * }}</pre>
 *
 * @param <RT> The target type for erosion keys (e.g., {@link Block}, {@link EntityType})
 * @param <R>  The final result type after erosion (e.g., {@link BlockState}, {@link Entity})
 * @author JiXingDeFeng
 * @since 0.1.0
 */
public class BlockErosionEntry<RT, R> implements ErodibleBlock<RT, R>, WeightedEntry {
    protected final Block source;
    protected final ErosionType erosionType;
    protected final RT target;
    protected final @Nullable ResourceLocation particle;
    protected final @Nullable ResourceLocation predicate;
    protected final float probability;
    protected final boolean allowHardCoded;
    protected final Weight weight;
    protected final RandomSource random = RandomSource.create();

    public BlockErosionEntry(
            Block source,
            ErosionType erosionType,
            RT target,
            @Nullable ResourceLocation particle,
            @Nullable ResourceLocation predicate,
            float probability,
            boolean allowHardCoded,
            Weight weight
    ) {
        this.source = source;
        this.erosionType = erosionType;
        this.target = target;
        this.particle = particle;
        this.predicate = predicate;
        this.probability = probability;
        this.allowHardCoded = allowHardCoded;
        this.weight = weight;
    }

    @Override
    public void randomTickInfection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, ErosionType type) {
        ErodibleBlock.super.tryErosion(level, pos, type);
    }

    @Override
    public void onEroded(R result, Level level, BlockPos pos, ErosionType type) {
        if (this.particle != null) {
            ParticleConfig particleConfig = ParticleConfigManager.get(this.particle);
            if (particleConfig != null) {
                particleConfig.getSingleton().spawnParticles(pos.getCenter(), level);
            }
        }
    }

    @Override
    public boolean canBeEroded(Level level, BlockPos pos, ErosionType type) {
        return this.isApplicable(level, pos);
    }

    @Override
    public float conversionProbability(ErosionType type) {
        return this.probability;
    }

    @Nullable
    @Override
    public RT afterErosion(ErosionType type) {
        return this.target;
    }

    @Override
    public @Nullable R transformed(RT target, BlockErosionContext context) {
        return null;
    }

    @NotNull
    @Override
    public String toString() {
        return "BlockErosionEntry[source: " + this.source
                + ", target: " + this.target
                + ", particle: " + this.particle
                + ", predicate: " + this.predicate
                + ", probability: " + this.probability
                + ", allowHardCoded: " + this.allowHardCoded
                + ", weight: " + this.weight
                + "]";
    }

    @NotNull
    @Override
    public Weight getWeight() {
        return this.weight;
    }

    @NotNull
    @Override
    public Block getSource() {
        return this.source;
    }

    /**
     * Checks whether this erosion entry is applicable at the given position.
     * <p>
     * The method resolves the predicate associated with this entry and tests it
     * against the position. If no predicate is defined, the entry is considered
     * applicable by default.
     *
     * @param level The level
     * @param pos   The position to check
     * @return {@code true} if the entry is applicable, {@code false} otherwise
     */
    public boolean isApplicable(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPredicate predicate = BlockErosionManager.getPredicate(this.predicate);
            return predicate == null || predicate.test(serverLevel, pos);
        } else {
            return true;
        }
    }

    /**
     * Returns whether this entry allows falling back to hardcoded erosion behavior
     * when the predicate conditions are not met.
     *
     * @return {@code true} if hardcoded fallback is allowed, {@code false} otherwise
     */
    public boolean allowHardCoded() {
        return this.allowHardCoded;
    }

    /**
     * Returns the erosion type associated with this entry.
     *
     * @return The erosion type
     */
    public ErosionType getErosionType() {
        return this.erosionType;
    }

    /**
     * Returns the resource location of the particle configuration for this entry.
     *
     * @return The particle resource location, or {@code null} if not configured
     */
    @Nullable
    public ResourceLocation getParticle() {
        return this.particle;
    }

    /**
     * Returns the resource location of the predicate configuration for this entry.
     *
     * @return The predicate resource location, or {@code null} if not configured
     */
    @Nullable
    public ResourceLocation getPredicate() {
        return this.predicate;
    }

    public record Wrapper<RT>(
            Block source,
            Map<Holder<ErosionType>, List<Entry<RT>>> entryMap,
            Optional<ResourceLocation> particle,
            Optional<BlockPredicate> predicate
    ) {
        public static <T> Codec<Wrapper<T>> codec(Codec<T> targetCodee) {
            return RecordCodecBuilder.create(instance ->
                    instance.group(
                            BuiltInRegistries.BLOCK.byNameCodec()
                                    .fieldOf("block")
                                    .forGetter(Wrapper::source),
                            Codec.unboundedMap(RegistryFixedCodec.create(ModRegistries.EROSION_TYPE), Codec.list(Entry.codec(targetCodee)))
                                    .fieldOf("values")
                                    .forGetter(Wrapper::entryMap),
                            ParticleConfig.LOCATION_CODEC
                                    .optionalFieldOf("particle")
                                    .forGetter(Wrapper::particle),
                            BlockPredicate.CODEC.optionalFieldOf("predicate")
                                    .forGetter(Wrapper::predicate)
                    ).apply(instance, Wrapper::new)
            );
        }

        public <R, T extends BlockErosionEntry<RT, R>> List<T> build(
                Function8<Block, ErosionType, RT, @Nullable ResourceLocation,
                        @Nullable ResourceLocation, Float, Boolean, Weight, T> createFunction,
                Function<@Nullable BlockPredicate, @Nullable ResourceLocation> idFunction
        ) {
            ResourceLocation defaultParticle = this.particle.orElse(null);
            BlockPredicate defaultPredicate = this.predicate.orElse(null);
            return entryMap.entrySet().stream()
                    .flatMap(list -> list.getValue().stream()
                            .map(entry -> createFunction.apply(
                                    this.source,
                                    list.getKey().value(),
                                    entry.target,
                                    entry.particle.orElse(defaultParticle),
                                    idFunction.apply(entry.predicate.orElse(defaultPredicate)),
                                    entry.probability,
                                    entry.allowHardCoded,
                                    entry.weight
                            ))
                    )
                    .toList();
        }

        public record Entry<RT>(
                RT target,
                Optional<ResourceLocation> particle,
                Optional<BlockPredicate> predicate,
                float probability,
                boolean allowHardCoded,
                Weight weight
        ) {
            public static <T> Codec<Entry<T>> codec(Codec<T> targetCodec) {
                return RecordCodecBuilder.create(instance ->
                        instance.group(
                                targetCodec.fieldOf("target")
                                        .forGetter(Entry::target),
                                ParticleConfig.LOCATION_CODEC
                                        .optionalFieldOf("particle")
                                        .forGetter(Entry::particle),
                                BlockPredicate.CODEC.optionalFieldOf("predicate")
                                        .forGetter(Entry::predicate),
                                Codec.FLOAT.optionalFieldOf("probability", 1F)
                                        .forGetter(Entry::probability),
                                Codec.BOOL.optionalFieldOf("allowHardCoded", true)
                                        .forGetter(Entry::allowHardCoded),
                                Weight.CODEC.optionalFieldOf("weight", Weight.of(1))
                                        .forGetter(Entry::weight)
                        ).apply(instance, Entry::new)
                );
            }
        }
    }
}
