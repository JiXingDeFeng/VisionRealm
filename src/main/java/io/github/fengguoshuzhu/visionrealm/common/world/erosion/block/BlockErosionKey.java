package io.github.fengguoshuzhu.visionrealm.common.world.erosion.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection.block.CanBeErosionBlock;
import io.github.fengguoshuzhu.visionrealm.common.util.world.erosion.ErosionUtil;
import io.github.fengguoshuzhu.visionrealm.common.world.context.ErosionContext;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.handle.world.erosion.block.BlockErosionHandler;
import io.github.fengguoshuzhu.visionrealm.manager.world.erosion.biome.BiomeErosionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BlockErosionKey<T, R> implements CanBeErosionBlock<T, R> {
    protected final float probability;
    protected final Block source;
    protected final Map<ErosionType, ErosionKey<T>> keys = new HashMap<>();
    private final Collection<ErosionKey<T>> typeKeys;

    private BlockErosionKey(Block source, float probability, Collection<ErosionKey<T>> typeKeys) {
        this.source = source;
        this.probability = probability;
        this.typeKeys = new ArrayList<>(typeKeys);
    }

    @Nullable
    @Override
    public T afterErosion(ErosionType type) {
        return this.keys.get(type).target();
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
        return this.keys.get(type).probability();
    }

    @Override
    public float conversionProbability() {
        return this.probability;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("BlockErosionKey[source: %s, probability: %f, keys:%s]", this.source, this.probability, this.keys);
    }

    @NotNull
    public Block getSource() {
        return this.source;
    }

    public void init() {
        if (BiomeErosionManager.getInstance() != null && !this.typeKeys.isEmpty()) {
            for (ErosionKey<T> key : typeKeys) {
                ErosionType erosionType = ErosionUtil.getBiomeErosionType(key.biome());
                if (erosionType != null && erosionType != ErosionType.NONE) {
                    this.keys.put(erosionType, key);
                }
            }
            this.typeKeys.clear();
        }
    }

    public List<ErosionKey<T>> erosionKeys() {
        return new ArrayList<>(this.keys.values());
    }

    public static class TransformIntoBlock extends BlockErosionKey<Block, BlockState> {
        public static final Codec<TransformIntoBlock> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockErosionKey::getSource),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(BlockErosionKey::conversionProbability),
                        Codec.list(
                                RecordCodecBuilder.<ErosionKey<Block>>create(key ->
                                        key.group(
                                                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(ErosionKey::target),
                                                ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(ErosionKey::biome),
                                                Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(ErosionKey::probability)
                                        ).apply(key, BlockKey::new)
                                )
                        ).fieldOf("keys").forGetter(BlockErosionKey::erosionKeys)
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

        protected record BlockKey<T extends Block>(T target, ResourceKey<Biome> biome, float probability) implements ErosionKey<T> {

            @NotNull
            @Override
            public String toString() {
                return String.format("BlockKey[target: %s, probability: %f]", this.target, this.probability);
            }
        }
    }

    public static class TransformIntoEntity<T extends Entity> extends BlockErosionKey<EntityType<T>, T> {
        @SuppressWarnings("unchecked")
        public static final Codec<TransformIntoEntity<Entity>> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockErosionKey::getSource),
                        Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(BlockErosionKey::conversionProbability),
                        Codec.list(
                                RecordCodecBuilder.<ErosionKey<EntityType<Entity>>>create(key ->
                                        key.group(
                                                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("target").forGetter(ErosionKey::target),
                                                ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(ErosionKey::biome),
                                                Codec.FLOAT.optionalFieldOf("probability", 1.0F).forGetter(ErosionKey::probability)
                                        ).apply(key, (target, biome, prob) -> new EntityKey<>((EntityType<Entity>) target, biome, prob))
                                )
                        ).fieldOf("keys").forGetter(BlockErosionKey::erosionKeys)
                ).apply(instance, TransformIntoEntity::new)
        );

        public TransformIntoEntity(Block source, float probability, Collection<ErosionKey<EntityType<T>>> typeKeys) {
            super(source, probability, typeKeys);
        }

        @Nullable
        @Override
        public T transformed(EntityType<T> target, Level level, BlockPos pos, ErosionType type, ErosionContext<Block> context) {
            return BlockErosionHandler.transformEntity(target, level, pos, type, context);
        }

        protected record EntityKey<T extends Entity>(EntityType<T> target, ResourceKey<Biome> biome, float probability) implements ErosionKey<EntityType<T>> {

            @NotNull
            @Override
            public String toString() {
                return String.format("EntityKey[target: %s, probability: %f]", this.target, this.probability);
            }
        }
    }

    public interface ErosionKey<T> {

        T target();

        ResourceKey<Biome> biome();

        float probability();
    }
}
