package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry;

import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.erosion.block.ErosionEntry;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.BlockErosionHandler;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionParticle;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.core.erosion.block.BlockErosionPredicate;
import io.github.jixingdefeng.visionrealm.core.particle.config.ParticleConfigStore;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Base implementation of {@link BlockErosionEntry} providing common functionality
 * for block-to-block and block-to-entity erosion transformations.
 * <p>
 * This abstract class handles:
 * <ul>
 *   <li>Grouping erosion entries by {@link ErosionType}</li>
 *   <li>Weighted random selection of erosion keys by type</li>
 *   <li>Storing particle configurations for each erosion type</li>
 *   <li>Weight calculation for random selection</li>
 * </ul>
 *
 * @param <RT> The target type for erosion keys (e.g., {@link Block}, {@link EntityType})
 * @param <R> The final result type after erosion (e.g., {@link BlockState}, {@link Entity})
 * @author JiXingDeFeng
 * @see BlockErosionEntry
 * @see TransformIntoBlock
 * @see TransformIntoEntity
 * @since 0.0.1-dev
 */
public abstract class BaseBlockErosionEntry<RT, R> extends BlockErosionEntry<RT, R> {
    protected final Block source;
    private final Map<ErosionType, ArrayWeightRandomList<ErosionEntry<RT>>> entryMap;
    private final ErosionParticle particle;
    private final BlockErosionPredicate predicate;
    protected final RandomSource random = RandomSource.create();
    protected final Weight weight;
    @Nullable protected ErosionEntry.SelectedEntry<RT> selectedEntry;

    /**
     * Creates a {@link MapCodec} for {@link BaseBlockErosionEntry} instances.
     * <p>
     * This codec handles the common structure of block erosion entries, including:
     * <ul>
     *   <li>Source block</li>
     *   <li>Erosion probability and weight</li>
     *   <li>A map of {@link ErosionType} to weighted target lists</li>
     *   <li>Particle and predicate configuration</li>
     * </ul>
     * The actual subtype to instantiate is determined by the provided {@code blockEntryFactory}.
     *
     * @param <RT>              The target type of the inner erosion entries (e.g. {@link Block} or {@link EntityType})
     * @param <R>               The final result type after erosion (e.g. {@link BlockState} or {@link Entity})
     * @param entryCodec        Codec for individual erosion entries; used to decode the lists inside {@code entry}
     * @param blockEntryFactory Factory that constructs the concrete {@code BaseBlockErosionEntry} subclass
     *                          from the decoded components (block, probability, weight, entry map, particle, predicate)
     * @return A {@code MapCodec} for serializing and deserializing block erosion entries
     */
    public static <RT, R> MapCodec<BaseBlockErosionEntry<RT, R>> codec(
            MapCodec<ErosionEntry<RT>> entryCodec,
            Function5<Block, Weight, Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<RT>>>,
                    ErosionParticle, BlockErosionPredicate, BaseBlockErosionEntry<RT, R>> blockEntryFactory
    ) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec()
                                .fieldOf("block")
                                .forGetter(BaseBlockErosionEntry::getSource),
                        Weight.CODEC.optionalFieldOf("weight", Weight.of(1))
                                        .forGetter(BaseBlockErosionEntry::getWeight),
                        Codec.unboundedMap(RegistryFixedCodec.create(ModRegistries.EROSION_TYPE),
                                        ArrayWeightRandomList.codec(entryCodec).codec())
                                .fieldOf("entry")
                                .forGetter(BaseBlockErosionEntry::erosionEntry),
                        ErosionParticle.CODEC.optionalFieldOf("particles", ErosionParticle.EMPTY)
                                .forGetter(BaseBlockErosionEntry::particle),
                        BlockErosionPredicate.CODEC.optionalFieldOf("predicate", BlockErosionPredicate.alwaysTrue())
                                .forGetter(BaseBlockErosionEntry::predicate)
                ).apply(instance, blockEntryFactory));
    }

    /**
     * Constructor for subclasses.
     * <p>
     * This constructor performs the following initialization:
     * <ul>
     *   <li>Stores the source block, base probability, and weight</li>
     *   <li>Stores the block predicate condition</li>
     *   <li>Stores the particle configuration map</li>
     *   <li>Converts the entry map's keys from {@link Holder}<{@link ErosionType}> to direct
     *       {@link ErosionType} references for efficient lookup</li>
     * </ul>
     *
     * @param source      The source block this erosion entry applies to
     * @param weight      The weight of this erosion entry relative to others
     * @param entryMap    Map of erosion type holders to weighted target lists
     * @param particle    Particle configuration for this erosion entry
     * @param predicate   Condition predicate for erosion application
     */
    protected BaseBlockErosionEntry(
            Block source,
            Weight weight,
            Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<RT>>> entryMap,
            ErosionParticle particle,
            BlockErosionPredicate predicate
    ) {
        super(source, weight, entryMap, particle, predicate);
        this.source = source;
        this.weight = weight;
        this.particle = particle;
        this.predicate = predicate;
        this.entryMap = new Object2ObjectOpenHashMap<>(entryMap.size());
        entryMap.forEach((type, entry) ->
                this.entryMap.put(type.value(), entry)
        );
    }

    /**
     * Checks whether this erosion entry is applicable at the given position.
     *
     * @param level The level
     * @param pos   The position
     * @param type  The erosion type
     * @return {@code true} if the predicate passes, {@code false} otherwise
     */
    public boolean isApplicable(Level level, BlockPos pos, ErosionType type) {
        return level instanceof ServerLevel serverLevel && this.predicate.get(type).test(serverLevel, pos);
    }

    public boolean useFallback() {
        return this.predicate.useFallback();
    }

    public Collection<ResourceLocation> particles() {
        return this.particle.unwrap();
    }

    public ErosionParticle particle() {
        return this.particle;
    }

    public BlockErosionPredicate predicate() {
        return this.predicate;
    }

    protected Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<RT>>> erosionEntry() {
        Map<Holder<ErosionType>, ArrayWeightRandomList<ErosionEntry<RT>>> map = new HashMap<>(this.entryMap.size());
        this.entryMap.forEach((key, value) ->
                map.put(Holder.direct(key), value)
        );
        return map;
    }

    protected Optional<ErosionEntry<RT>> randomEntry(ErosionType type) {
        if (this.entryMap.containsKey(type)) {
            Optional<ErosionEntry<RT>> erosionEntry = this.entryMap.get(type).getRandom(this.random);
            erosionEntry.ifPresent(entry -> this.selectedEntry = new ErosionEntry.SelectedEntry<>(type, entry));
            return erosionEntry;
        }

        return Optional.empty();
    }

    @Override
    public void randomTickInfection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, ErosionType type) {
        if (this.entryMap.containsKey(type)) {
            BlockErosionHandler.tryErosion(this, level, pos, type);
        }
    }

    @Override
    public void onEroded(R result, Level level, BlockPos pos, ErosionType type) {
        ErosionParticle particle = this.particle;
        if (particle.containsKey(level, type)) {
            ResourceLocation location = particle.get(type);
            if (location != null) {
                ParticleConfigStore.getInstance().ifPresent(manager -> {
                    ParticleConfig particleConfig = manager.get(location);
                    if (particleConfig != null) {
                        particleConfig.getSingleton().spawnParticles(pos.getCenter(), level);
                    }
                });
            }
        }
    }

    @Override
    public boolean canBeEroded(Level level, BlockPos pos, ErosionType type) {
        return level instanceof ServerLevel serverLevel
                && this.entryMap.containsKey(type)
                && this.isApplicable(serverLevel, pos, type)
                && this.randomEntry(type).filter(entry -> entry.predicate().test(serverLevel, pos)).isPresent();
    }

    @Override
    public float conversionProbability(ErosionType type) {
        return this.selectedEntry != null && this.selectedEntry.isType(type) ? this.selectedEntry.entry().probability() : 0F;
    }

    @Nullable
    @Override
    public RT afterErosion(ErosionType type) {
        return this.selectedEntry != null && this.selectedEntry.isType(type) ? this.selectedEntry.entry().target() : null;
    }

    @NotNull
    @Override
    public String toString() {
        return "BaseBlockErosionEntry[source: " + this.source + ", keys: " + this.entryMap + "]";
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
}
