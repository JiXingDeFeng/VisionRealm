package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry;

import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.common.data.erosion.block.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.block.BlockErosionHandler;
import io.github.jixingdefeng.visionrealm.common.particle.ParticleConfigLoader;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.block.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.entity.TransformIntoEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link BlockErosionEntry} providing common functionality
 * for block-to-block and block-to-entity erosion transformations.
 * <p>
 * This abstract class handles:
 * <ul>
 *   <li>Grouping erosion keys by {@link ErosionType}</li>
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
    protected final Object2ObjectOpenHashMap<ErosionType, ArrayWeightRandomList<ErosionEntry<RT>>> entryMap = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<ErosionType, ResourceLocation> particles = new Object2ObjectOpenHashMap<>();
    protected final RandomSource random = RandomSource.create();
    protected final Predicate predicate;
    @Nullable protected ErosionEntry<RT> selectedEntry;
    protected float probability;
    protected Weight weight;

    /**
     * Creates a MapCodec for {@link BaseBlockErosionEntry} instances.
     * <p>
     * This factory method returns a {@link MapCodec} that handles the base structure
     * by delegating to the appropriate codecs for erosion keys.
     * The returned MapCodec can be extended by subclasses to add additional fields
     * such as particle effects.
     *
     * @param <RT> The target type for erosion keys
     * @param <R> The final result type after erosion
     * @param targetCodec The Codec for target values {@link BuiltInRegistries#BLOCK},{@link Registry#byNameCodec()}
     * @param entryFactory Factory function to create erosion keys from decoded values
     * @param blockEntryFactory Factory function to create block erosion keys from decoded components
     * @return A MapCodec that can be extended by subclasses
     */
    public static <RT, R> MapCodec<BaseBlockErosionEntry<RT, R>> codec(
            final Codec<RT> targetCodec,
            final Function5<ErosionType, RT, Float, BlockPredicate, Weight, ErosionEntry<RT>> entryFactory,
            final Function5<Block, Float, List<ErosionEntry<RT>>, Map<ErosionType, ResourceLocation>, Predicate, BaseBlockErosionEntry<RT, R>> blockEntryFactory
    ) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec()
                                .fieldOf("block")
                                .forGetter(BaseBlockErosionEntry::getSource),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F)
                                .forGetter(BaseBlockErosionEntry::conversionProbability),
                        Codec.list(ErosionEntry.codec(targetCodec, entryFactory).codec())
                                .fieldOf("entry")
                                .forGetter(BaseBlockErosionEntry::erosionKey),
                        Codec.unboundedMap(ErosionType.CODEC, ParticleConfig.LOCATION_CODEC)
                                .optionalFieldOf("particles", Map.of())
                                .forGetter(key -> key.particles),
                        Predicate.CODEC.optionalFieldOf("predicate", Predicate.ALWAYS_TRUE)
                                .forGetter(entry -> entry.predicate)
                ).apply(instance, blockEntryFactory)
        );
    }

    /**
     * Merges multiple erosion entries into a single entry.
     * <p>
     * If only one entry is provided, it is returned directly.
     * Otherwise, entries are merged sequentially using {@link #merger(BaseBlockErosionEntry)}.
     * </p>
     *
     * @param entryList The collection of entries to merge
     * @param <RT>       The target type of the erosion entry
     * @param <R>       The result type after erosion
     * @return A single merged erosion entry containing all data from the input collection
     */
    public static <RT, R> BaseBlockErosionEntry<RT, R> merger(Collection<BaseBlockErosionEntry<RT, R>> entryList) {
        if (entryList.size() == 1) {
            return entryList.iterator().next();
        } else {
            BaseBlockErosionEntry<RT, R> presentEntry = null;
            for (BaseBlockErosionEntry<RT, R> entry : entryList) {
                if (presentEntry == null) {
                    presentEntry = entry;
                } else {
                    presentEntry = presentEntry.merger(entry);
                }
            }

            return presentEntry;
        }
    }

    /**
     * Constructor for subclasses.
     * <p>
     * This constructor performs the following initialization:
     * <ul>
     *   <li>Stores the source block and base probability</li>
     *   <li>Stores the block predicate condition</li>
     *   <li>Initializes particle configuration map</li>
     *   <li>Calls {@link #initEntry(Collection)} to build erosion type groups</li>
     * </ul>
     *
     * @param source      The source block this erosion key applies to
     * @param probability Base probability of erosion occurring
     * @param entryList   Collection of erosion keys defining target types and conditions
     * @param particles   Map of erosion types to particle configuration resource locations
     * @param predicate   Block predicate for additional conditions
     */
    protected BaseBlockErosionEntry(
            Block source,
            float probability,
            Collection<ErosionEntry<RT>> entryList,
            Map<ErosionType, ResourceLocation> particles,
            Predicate predicate
    ) {
        super(source, probability, entryList, particles);
        this.source = source;
        this.probability = probability;
        this.predicate = predicate;

        this.particles.clear();
        this.particles.putAll(particles);

        this.initEntry(entryList);
    }

    /**
     * Merges another erosion key into this one.
     * <p>
     * Combines the erosion keys and weights from both instances,
     * aggregating them by erosion type and updating the total weight.
     *
     * @param newEntry The erosion key to merge into this one
     * @return This merged erosion key instance
     */
    public BaseBlockErosionEntry<RT, R> merger(BaseBlockErosionEntry<RT, R> newEntry) {
        Map<ErosionType, ArrayList<ErosionEntry<RT>>> newEntryMap = new Object2ObjectOpenHashMap<>(this.entryMap.size() + newEntry.entryMap.size());
        this.entryMap.forEach((type, keyList) ->
                newEntryMap.put(type, new ArrayList<>(keyList.unwrapValue()))
        );
        newEntry.entryMap.forEach((type, keyList) ->
                newEntryMap.computeIfAbsent(type, k -> new ArrayList<>()).addAll(keyList.unwrapValue())
        );
        this.entryMap.clear();
        this.entryMap.ensureCapacity(newEntryMap.size());
        newEntryMap.forEach((type, keyList) ->
                this.entryMap.put(type, ArrayWeightRandomList.create(keyList))
        );
        this.particles.putAll(newEntry.particles);
        this.weight = Weight.of((this.weight.asInt() + newEntry.weight.asInt()) / 2);
        this.probability = (this.probability + newEntry.probability) / 2;
        return this;
    }

    public List<ErosionEntry<RT>> erosionKey() {
        return this.entryMap.values().stream()
                .flatMap(wl -> wl.unwrap().stream())
                .map(ArrayWeightRandomList.Entry::value)
                .collect(Collectors.toList());
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
        return this.particles.values();
    }

    protected Optional<ErosionEntry<RT>> randomEntry(ErosionType type) {
        if (this.entryMap.containsKey(type)) {
            Optional<ErosionEntry<RT>> erosionEntry = this.entryMap.get(type).getRandom(this.random);
            erosionEntry.ifPresent(entry -> this.selectedEntry = entry);
            return erosionEntry;
        }

        return Optional.empty();
    }

    /**
     * Initializes the erosion entry map by grouping entries by erosion type
     * and building weighted random lists for each type.
     * <p>
     * This method is called during construction and can be overridden by subclasses
     * to customize the initialization behavior.
     *
     * @param entryList The collection of erosion entries to initialize
     * @see #BaseBlockErosionEntry(Block, float, Collection, Map, Predicate) Constructor of BaseBlockErosionEntry
     */
    @ApiStatus.Internal
    protected void initEntry(Collection<ErosionEntry<RT>> entryList) {
        this.entryMap.clear();
        Map<ErosionType, ArrayList<ErosionEntry<RT>>> newKeys = new HashMap<>(entryList.size());
        int weight = 0;
        for (ErosionEntry<RT> entry : entryList) {
            ErosionType erosionType = entry.type();
            if (erosionType != null && erosionType != ErosionType.NONE) {
                newKeys.computeIfAbsent(erosionType, type -> new ArrayList<>()).add(entry);
                weight += entry.weight().asInt();
            }
        }

        newKeys.forEach((type, keys) ->
                this.entryMap.put(type, ArrayWeightRandomList.create(keys))
        );

        int keysSize = this.entryMap.size();
        if (keysSize > 0) {
            this.weight = Weight.of(Mth.ceil((float) weight / keysSize));
        } else {
            this.weight = Weight.of(0);
            VisionRealm.LOGGER.warn("No valid erosion keys for block: {}", this.getSource());
        }
    }

    @Override
    public void randomTickInfection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, ErosionType type) {
        if (this.entryMap.containsKey(type)) {
            BlockErosionHandler.tryErosion(this, level, pos, type);
        }
    }

    @Override
    public void onEroded(R result, Level level, BlockPos pos, ErosionType type) {
        if (this.particles.containsKey(type)) {
            ResourceLocation location = this.particles.get(type);
            if (location != null) {
                ParticleConfig particle = ParticleConfigLoader.load(location, true, false);
                if (particle != null) {
                    particle.getSingleton().spawnParticles(level, pos.getCenter());
                }
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
        return this.selectedEntry != null ? this.selectedEntry.probability() : 0F;
    }

    @Override
    public float conversionProbability() {
        return this.probability;
    }

    @Nullable
    @Override
    public RT afterErosion(ErosionType type) {
        return this.selectedEntry != null ? this.selectedEntry.target() : null;
    }

    @NotNull
    @Override
    public String toString() {
        return "BaseBlockErosionEntry[source: " + this.source + ", probability: " + this.probability + ", keys: " + this.entryMap + "]";
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
