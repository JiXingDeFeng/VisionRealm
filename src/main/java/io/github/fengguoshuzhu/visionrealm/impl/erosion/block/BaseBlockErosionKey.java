package io.github.fengguoshuzhu.visionrealm.impl.erosion.block;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fengguoshuzhu.visionrealm.api.data.erosion.block.BlockErosionKey;
import io.github.fengguoshuzhu.visionrealm.api.particle.ParticleConfig;
import io.github.fengguoshuzhu.visionrealm.common.util.world.erosion.ErosionUtil;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.core.handle.erosion.block.BlockErosionHandler;
import io.github.fengguoshuzhu.visionrealm.common.erosion.manager.biome.BiomeErosionManager;
import io.github.fengguoshuzhu.visionrealm.impl.particle.WeightedParticleConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseBlockErosionKey<T, R> extends BlockErosionKey<T, R> {
    private final Collection<ErosionKey<T>> typeKeys;
    protected final float probability;
    protected final Block source;
    protected final Map<ErosionType, WeightedRandomList<ErosionKey<T>>> keys = new HashMap<>();
    protected final Map<ErosionType, WeightedParticleGroup> particles = new HashMap<>();
    protected final RandomSource random = RandomSource.create();
    private boolean initialized = false;
    protected Weight weight = Weight.of(1);

    protected BaseBlockErosionKey(Block source, float probability, Collection<ErosionKey<T>> typeKeys, Collection<WeightedParticleGroup> particleGroupList) {
        super(source, probability, typeKeys, particleGroupList);
        this.source = source;
        this.probability = probability;
        this.typeKeys = new ArrayList<>(typeKeys);
        for (WeightedParticleGroup particleGroup : particleGroupList) {
            ErosionType type = particleGroup.type();
            if (!this.particles.containsKey(type)) {
                this.particles.put(type, particleGroup);
            } else {
                WeightedParticleGroup group = this.particles.get(type);
                List<WeightedParticleConfig> configList = new ArrayList<>();
                configList.addAll(group.particle().unwrap());
                configList.addAll(particleGroup.particle().unwrap());
                WeightedParticleGroup newGroup = new WeightedParticleGroup(WeightedRandomList.create(configList), type);
                this.particles.put(type, newGroup);
            }
        }
    }

    /**
     * Creates a MapCodec for {@link BaseBlockErosionKey} instances.
     * <p>
     * This factory method returns a {@link MapCodec} that handles the base structure
     * by delegating to the appropriate codecs for erosion keys and particles.
     * The returned MapCodec can be extended by subclasses to add additional fields.
     *
     * @param <T> The target type for erosion keys
     * @param <R> The final result type after erosion
     * @param targetCodec The Codec for target values
     * @param keyFactory Factory function to create erosion keys
     * @param blockKeyFactory Factory function to create block erosion keys
     * @return A MapCodec that can be extended by subclasses
     */
    public static <T, R> MapCodec<BaseBlockErosionKey<T, R>> codec(
            Codec<T> targetCodec,
            final Function4<T, ResourceKey<Biome>, Float, Weight, ErosionKey<T>> keyFactory,
            Function4<Block, Float, List<ErosionKey<T>>, List<WeightedParticleGroup>, BaseBlockErosionKey<T, R>> blockKeyFactory
    ) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BaseBlockErosionKey::getSource),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(BaseBlockErosionKey::conversionProbability),
                        Codec.list(ErosionKey.codec(targetCodec, keyFactory).codec()).fieldOf("keys").forGetter(BaseBlockErosionKey::erosionKey),
                        Codec.list(WeightedParticleGroup.CODEC).optionalFieldOf("particles", List.of()).forGetter(BaseBlockErosionKey::particles)
                ).apply(instance, blockKeyFactory)
        );
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

    @Nullable
    @Override
    public T afterErosion(ErosionType type) {
        Optional<ErosionKey<T>> key = this.getRandom(type);
        return key.map(ErosionKey::target).orElse(null);

    }

    @Override
    public void randomTickInfection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ErosionType type = ErosionUtil.getBiomeErosionType(pos, level);
        if (this.keys.containsKey(type)) {
            BlockErosionHandler.tryErosion(this, level, pos, type);
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

    @Override
    public void onEroded(R result, Level level, BlockPos pos, ErosionType type) {
        WeightedParticleGroup particles = this.particles.get(type);
        if (particles != null) {
            Optional<? extends ParticleConfig> particleConfig = particles.particle().getRandom(this.random);
            particleConfig.ifPresent(particle -> particle.spawnParticles(level, pos.getCenter()));
        }
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

    /**
     * Initializes the erosion data after biome erosion information becomes available.
     * <p>
     * This method performs lazy initialization of erosion keys because biome erosion data
     * may not be available at construction time. It should be called automatically after
     * all biome erosion information has been loaded.
     * </p>
     *
     * <p><b>Initialization process:</b></p>
     * <ul>
     *   <li>Checks if {@link BiomeErosionManager} is available and keys exist</li>
     *   <li>Converts each erosion key's biome to the corresponding erosion type</li>
     *   <li>Groups keys by erosion type into weighted random lists</li>
     *   <li>Calculates total weight as average of all key weights</li>
     *   <li>Clears temporary storage and marks as initialized</li>
     * </ul>
     *
     * <p><b>Note:</b> Keys with {@link ErosionType#NONE} or null erosion types are skipped.
     * This method does nothing if already initialized or if prerequisites are not met.</p>
     */
    public void init() {
        if (BiomeErosionManager.getInstance() != null && !this.typeKeys.isEmpty()) {
            Map<ErosionType, ArrayList<ErosionKey<T>>> newKeys = new HashMap<>();
            int weight = 0;
            for (ErosionKey<T> key : typeKeys) {
                ErosionType erosionType = ErosionUtil.getBiomeErosionType(key.biome());
                if (erosionType != null && erosionType != ErosionType.NONE) {
                    newKeys.computeIfAbsent(erosionType, type -> new ArrayList<>()).add(key);
                    weight += key.weight().asInt();
                }
            }

            newKeys.forEach((type, keys) ->
                    this.keys.put(type, WeightedRandomList.create(keys))
            );
            this.weight = Weight.of(Mth.ceil((float) weight / this.keys.size()));
            this.typeKeys.clear();
            this.initialized = true;
            VisionRealm.LOGGER.debug("Initializing erosion key for block: {}", this.getSource());
        }
    }

    public Optional<ErosionKey<T>> getRandom(ErosionType type) {
        if (this.keys.containsKey(type)) {
            return this.keys.get(type).getRandom(this.random);
        }

        return Optional.empty();
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public List<ErosionKey<T>> erosionKey() {
        return this.keys.values().stream()
                .flatMap(wl -> wl.unwrap().stream())
                .collect(Collectors.toList());
    }

    public List<WeightedParticleGroup> particles() {
        return this.particles.values().stream().toList();
    }

    public interface ErosionKey<T> extends WeightedEntry {

        static <T> MapCodec<ErosionKey<T>> codec(Codec<T> targetCodec, final Function4<T, ResourceKey<Biome>, Float, Weight, ErosionKey<T>> keyFactory) {
            return RecordCodecBuilder.mapCodec(keyInstance ->
                    keyInstance.group(
                            targetCodec.fieldOf("target").forGetter(ErosionKey::target),
                            ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(ErosionKey::biome),
                            Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(ErosionKey::probability),
                            Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(ErosionKey::weight)
                    ).apply(keyInstance, keyFactory)
            );
        }

        T target();

        ResourceKey<Biome> biome();

        float probability();

        Weight weight();

        @NotNull
        @Override
        default Weight getWeight() {
            return this.weight();
        }
    }
}
