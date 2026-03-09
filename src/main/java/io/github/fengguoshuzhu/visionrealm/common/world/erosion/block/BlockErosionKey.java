package io.github.fengguoshuzhu.visionrealm.common.world.erosion.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection.block.CanBeErosionBlock;
import io.github.fengguoshuzhu.visionrealm.common.util.world.erosion.ErosionUtil;
import io.github.fengguoshuzhu.visionrealm.common.world.context.ErosionContext;
import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.handle.world.erosion.block.BlockErosionHandler;
import io.github.fengguoshuzhu.visionrealm.manager.world.erosion.biome.BiomeErosionManager;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BlockErosionKey<T, R> implements CanBeErosionBlock<T, R>, WeightedEntry {
    private final Collection<ErosionKey<T>> typeKeys;
    protected final float probability;
    protected final Block source;
    protected final Map<ErosionType, WeightedRandomList<ErosionKey<T>>> keys = new HashMap<>();
    protected final RandomSource random = RandomSource.create();
    private boolean initialized = false;
    protected Weight weight = Weight.of(1);

    private BlockErosionKey(Block source, float probability, Collection<ErosionKey<T>> typeKeys) {
        this.source = source;
        this.probability = probability;
        this.typeKeys = new ArrayList<>(typeKeys);
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
    public BlockErosionKey<T, R> merger(BlockErosionKey<T, R> newKey) {
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

    @NotNull
    @Override
    public String toString() {
        return "BlockErosionKey[source: " + this.source + ", probability: " + this.probability + ", keys: " + this.keys + "]";
    }

    @NotNull
    public Block getSource() {
        return this.source;
    }

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
        }
    }

    @NotNull
    @Override
    public Weight getWeight() {
        return this.weight;
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

    public static class TransformIntoBlock extends BlockErosionKey<Block, BlockState> {
        public static final Codec<BlockErosionKey<Block, BlockState>> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockErosionKey::getSource),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(BlockErosionKey::conversionProbability),
                        Codec.list(
                                RecordCodecBuilder.<ErosionKey<Block>>create(key ->
                                        key.group(
                                                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(ErosionKey::target),
                                                ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(ErosionKey::biome),
                                                Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(ErosionKey::probability),
                                                Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(ErosionKey::weight)
                                        ).apply(key, BlockKey::new)
                                )
                        ).fieldOf("keys").forGetter(BlockErosionKey::erosionKey)
                ).apply(instance, TransformIntoBlock::new)
        );

        public TransformIntoBlock(Block source, float probability, Collection<ErosionKey<Block>> typeKeys) {
            super(source, probability, typeKeys);
        }

        @Nullable
        @Override
        public BlockState transformed(Block target, Level level, BlockPos pos, ErosionType type, ErosionContext<Block> context) {
            return BlockErosionHandler.transformBlock(target, level, pos, type, context);
        }

        protected record BlockKey<T extends Block>(T target, ResourceKey<Biome> biome, float probability, Weight weight) implements ErosionKey<T> {

            @NotNull
            @Override
            public String toString() {
                return "BlockKey[target: " + this.target + ", probability: " + this.probability + "]";
            }
        }
    }

    public static class TransformIntoEntity extends BlockErosionKey<EntityType<Entity>, Entity> {
        public static final Codec<BlockErosionKey<EntityType<Entity>, Entity>> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockErosionKey::getSource),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(BlockErosionKey::conversionProbability),
                        Codec.list(
                                RecordCodecBuilder.<ErosionKey<EntityType<Entity>>>create(key ->
                                        key.group(
                                                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("target").forGetter(ErosionKey::target),
                                                ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(ErosionKey::biome),
                                                Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(ErosionKey::probability),
                                                Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(ErosionKey::weight)
                                        ).apply(key, EntityKey::of)
                                )
                        ).fieldOf("keys").forGetter(BlockErosionKey::erosionKey)
                ).apply(instance, TransformIntoEntity::new)
        );

        public TransformIntoEntity(Block source, float probability, Collection<ErosionKey<EntityType<Entity>>> typeKeys) {
            super(source, probability, typeKeys);
        }

        @Nullable
        @Override
        public Entity transformed(EntityType<Entity> target, Level level, BlockPos pos, ErosionType type, ErosionContext<Block> context) {
            return BlockErosionHandler.transformEntity(target, level, pos, type, context);
        }

        protected record EntityKey(EntityType<Entity> target, ResourceKey<Biome> biome, float probability, Weight weight) implements ErosionKey<EntityType<Entity>> {

            @SuppressWarnings("unchecked")
            public static EntityKey of(EntityType<?> target, ResourceKey<Biome> biome, float probability, Weight weight) {
                if (Entity.class.isAssignableFrom(target.getBaseClass())) {
                    return new EntityKey((EntityType<Entity>) target, biome, probability, weight);
                } else {
                    throw new RuntimeException("The parameter " + target + " passed to EntityKey constructor is not of type Entity or its subclass");
                }
            }

            @NotNull
            @Override
            public String toString() {
                return "EntityKey[target: " + this.target + ", probability: " + this.probability + "]";
            }
        }
    }

    public interface ErosionKey<T> extends WeightedEntry {

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
