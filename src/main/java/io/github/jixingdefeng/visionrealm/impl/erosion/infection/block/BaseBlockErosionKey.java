package io.github.jixingdefeng.visionrealm.impl.erosion.infection.block;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.data.erosion.block.BlockErosionKey;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.common.handle.erosion.infection.block.BlockErosionHandler;
import io.github.jixingdefeng.visionrealm.common.manager.particle.ParticleConfigManager;
import io.github.jixingdefeng.visionrealm.common.util.erosion.ErosionUtil;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.block.TransformIntoBlock;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.entity.TransformIntoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link BlockErosionKey} providing common functionality
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
 * @param <T> The target type for erosion keys (e.g., {@link Block}, {@link EntityType})
 * @param <R> The final result type after erosion (e.g., {@link BlockState}, {@link Entity})
 * @author JiXingDeFeng
 * @see BlockErosionKey
 * @see TransformIntoBlock
 * @see TransformIntoEntity
 * @since 0.0.1-dev-1
 */
public abstract class BaseBlockErosionKey<T, R> extends BlockErosionKey<T, R> {
    protected final float probability;
    protected final Block source;
    protected final Map<ErosionType, WeightedRandomList<ErosionKey<T>>> keys = new HashMap<>();
    protected final Map<ErosionType, ResourceLocation> particles = new HashMap<>();
    protected final RandomSource random = RandomSource.create();
    protected Weight weight = Weight.of(1);

    /**
     * Creates a MapCodec for {@link BaseBlockErosionKey} instances.
     * <p>
     * This factory method returns a {@link MapCodec} that handles the base structure
     * by delegating to the appropriate codecs for erosion keys.
     * The returned MapCodec can be extended by subclasses to add additional fields
     * such as particle effects.
     *
     * @param <T> The target type for erosion keys
     * @param <R> The final result type after erosion
     * @param targetCodec The Codec for target values (e.g., {@link BuiltInRegistries#BLOCK}.byNameCodec())
     * @param keyFactory Factory function to create erosion keys from decoded values
     * @param blockKeyFactory Factory function to create block erosion keys from decoded components
     * @return A MapCodec that can be extended by subclasses
     */
    public static <T, R> MapCodec<BaseBlockErosionKey<T, R>> codec(
            Codec<T> targetCodec,
            final Function4<ErosionType, T, Float, Weight, ErosionKey<T>> keyFactory,
            Function4<Block, Float, List<ErosionKey<T>>, Map<ErosionType, ResourceLocation>, BaseBlockErosionKey<T, R>> blockKeyFactory
    ) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BaseBlockErosionKey::getSource),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(BaseBlockErosionKey::conversionProbability),
                        Codec.list(ErosionKey.codec(targetCodec, keyFactory).codec()).fieldOf("keys").forGetter(BaseBlockErosionKey::erosionKey),
                        Codec.unboundedMap(ErosionType.CODEC, ParticleConfig.LOCATION_CODEC)
                                .optionalFieldOf("particles", Map.of()).forGetter(key -> key.particles)
                ).apply(instance, blockKeyFactory)
        );
    }

    /**
     * Constructor for subclasses.
     *
     * @param source      The source block this erosion key applies to
     * @param probability Base probability of erosion occurring
     * @param typeKeys    Collection of erosion keys defining target types and conditions
     * @param particles   Map of erosion types to particle configuration IDs
     */
    protected BaseBlockErosionKey(Block source, float probability, Collection<ErosionKey<T>> typeKeys, Map<ErosionType, ResourceLocation> particles) {
        super(source, probability, typeKeys, particles);
        this.source = source;
        this.probability = probability;
        this.initErosionKey(typeKeys);
        particles.forEach((type, particle) -> {
            if (particle != null) {
                this.particles.put(type, particle);
            }
        });
    }

    /**
     * Merges another erosion key into this one.
     * <p>
     * Combines the erosion keys and weights from both instances,
     * aggregating them by erosion type and updating the total weight.
     *
     * @param newKey The erosion key to merge into this one
     * @return This merged erosion key instance
     */
    public BaseBlockErosionKey<T, R> merger(BaseBlockErosionKey<T, R> newKey) {
        Map<ErosionType, ArrayList<ErosionKey<T>>> keyMap = new HashMap<>();
        this.keys.forEach((type, keyList) ->
                keyMap.put(type, new ArrayList<>(keyList.unwrap()))
        );
        newKey.keys.forEach((type, keyList) ->
                keyMap.computeIfAbsent(type, k -> new ArrayList<>()).addAll(keyList.unwrap())
        );
        this.keys.clear();
        keyMap.forEach((type, keyList) ->
                this.keys.put(type, WeightedRandomList.create(keyList))
        );
        this.weight = Weight.of(this.weight.asInt() + newKey.weight.asInt());
        return this;
    }

    @Override
    public void randomTickInfection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ErosionType type = ErosionUtil.getBiomeErosionType(pos, level);
        if (type.isBlock() && this.keys.containsKey(type)) {
            BlockErosionHandler.tryErosion(this, level, pos, type);
        }
    }

    @Override
    public void onEroded(R result, Level level, BlockPos pos, ErosionType type) {
        if (this.particles.containsKey(type)) {
            ResourceLocation location = this.particles.get(type);
            ParticleConfigManager manager = ParticleConfigManager.getInstance();
            if (manager != null && location != null) {
                ParticleConfig particle = manager.load(location, true, false);
                if (particle != null) {
                    particle.getSingleton().spawnParticles(level, pos.getCenter());
                }
            }
        }
    }

    @Override
    public boolean canBeEroded(ErosionType type) {
        return this.keys.containsKey(type);
    }

    @Override
    public float conversionProbability(ErosionType type) {
        Optional<ErosionKey<T>> key = this.getRandom(type);
        return key.map(ErosionKey::probability).orElse(0F);
    }

    @Override
    public float conversionProbability() {
        return this.probability;
    }

    @Nullable
    @Override
    public T afterErosion(ErosionType type) {
        Optional<ErosionKey<T>> key = this.getRandom(type);
        return key.map(ErosionKey::target).orElse(null);

    }

    @NotNull
    @Override
    public String toString() {
        return "BaseBlockErosionKey[source: " + this.source + ", probability: " + this.probability + ", keys: " + this.keys + "]";
    }

    @NotNull
    @Override
    public Weight getWeight() {
        return this.weight;
    }

    @NotNull
    public Block getSource() {
        return this.source;
    }

    public Optional<ErosionKey<T>> getRandom(ErosionType type) {
        if (this.keys.containsKey(type)) {
            return this.keys.get(type).getRandom(this.random);
        }

        return Optional.empty();
    }

    public List<ErosionKey<T>> erosionKey() {
        return this.keys.values().stream()
                .flatMap(wl -> wl.unwrap().stream())
                .collect(Collectors.toList());
    }

    public Collection<ResourceLocation> particles() {
        return this.particles.values();
    }

    /**
     * Initializes erosion keys from the raw key collection.
     * <p>
     * This method:
     * <ul>
     *   <li>Groups keys by {@link ErosionType}</li>
     *   <li>Filters out {@link ErosionType#NONE} and null types</li>
     *   <li>Builds weighted random lists for each erosion type</li>
     *   <li>Stores particle configuration IDs for each erosion type</li>
     *   <li>Calculates the overall weight</li>
     * </ul>
     *
     * @param typeKeys The raw collection of erosion keys to initialize
     */
    @ApiStatus.Internal
    private void initErosionKey(Collection<ErosionKey<T>> typeKeys) {
        this.keys.clear();
        this.particles.clear();

        Map<ErosionType, ArrayList<ErosionKey<T>>> newKeys = new HashMap<>(typeKeys.size());
        int weight = 0;
        for (ErosionKey<T> key : typeKeys) {
            ErosionType erosionType = key.type();
            if (erosionType != null && erosionType != ErosionType.NONE) {
                newKeys.computeIfAbsent(erosionType, type -> new ArrayList<>()).add(key);
                weight += key.weight().asInt();
            }
        }

        newKeys.forEach((type, keys) ->
                this.keys.put(type, WeightedRandomList.create(keys))
        );

        int keysSize = this.keys.size();
        if (keysSize > 0) {
            this.weight = Weight.of(Mth.ceil((float) weight / keysSize));
            VisionRealm.LOGGER.debug("Initialized erosion key for block: {} with {} keys and {} particle groups",
                    this.getSource(), keysSize, this.particles.size());
        } else {
            this.weight = Weight.of(0);
            VisionRealm.LOGGER.warn("No valid erosion keys for block: {}", this.getSource());
        }
    }
}
