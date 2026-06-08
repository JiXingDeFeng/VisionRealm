package io.github.jixingdefeng.visionrealm.impl.selector.game.block;

import com.google.common.collect.Range;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.block.BlockSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.position.PositionSelector;
import io.github.jixingdefeng.visionrealm.common.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.common.util.selector.TargetSelectors;
import io.github.jixingdefeng.visionrealm.impl.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.BaseSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class BlockSelectorImpl extends BaseSelector<BlockPos, BlockSelector> implements BlockSelector {
    protected @NotNull Collection<Block> allowedBlocks = new ArrayList<>(1);
    protected @NotNull Collection<Block> denyBlocks = new ArrayList<>(1);
    protected StateSelection air = StateSelection.ANY;
    protected StateSelection fluid = StateSelection.ANY;
    protected boolean surface = false;

    public BlockSelectorImpl(AbstractSelector<?, ?> source, @Nullable Level level) {
        super(source, level);
    }

    public BlockSelectorImpl(BlockSelectorImpl source, @Nullable Level level) {
        super(source, level);
    }

    @Override
    public BlockSelector allowBlocks(Collection<Block> blocks) {
        this.allowedBlocks = blocks;
        return this;
    }

    @Override
    public BlockSelector denyBlocks(Collection<Block> blocks) {
        this.denyBlocks = blocks;
        return this;
    }

    @Override
    public BlockSelector surface(boolean surface) {
        this.surface = surface;
        return this;
    }

    @Override
    public BlockSelector air(StateSelection air) {
        this.air = air;
        return this;
    }

    @Override
    public BlockSelector fluid(StateSelection fluid) {
        this.fluid = fluid;
        return this;
    }

    @Override
    protected void push(TargetProvider<BlockPos> actuator) {
        BlockSnapshot<BlockPos> snapshot = BlockSnapshot.of(this, actuator);
        this.snapshotDeque.add(snapshot);
    }

    @Override
    protected BlockSnapshot<BlockPos> pop() {
        return (BlockSnapshot<BlockPos>) super.pop();
    }

    @Override
    public BlockSelector copy(@Nullable Level level) {
        return new BlockSelectorImpl(this, level);
    }

    @Override
    protected void copyFrom(AbstractSelector<?, ?> source) {
        super.copyFrom(source);
        if (source instanceof BlockSelectorImpl blockSelector) {
            this.filters = blockSelector.filters;
        }
    }

    @Override
    protected void execute() {
        if (this.level instanceof ServerLevel) {
            if (!this.isExecuted()) {
                super.execute();
                while (!this.snapshotDeque.isEmpty()) {
                    BlockSnapshot<BlockPos> snapshot = this.pop();
                    List<Block> allowBlocks = this.getAllowBlocks(snapshot);
                    List<BlockPos> blockPosList = this.getBlocks(
                            blockPos -> this.validate(blockPos, snapshot.filters, allowBlocks), snapshot
                    );
                    this.cachedResults.addAll(blockPosList);
                }
            }
        }
    }

    protected List<Block> getAllowBlocks(BlockSnapshot<BlockPos> snapshot) {
        List<Block> allowKeys = new ArrayList<>(snapshot.allowedBlocks);
        List<Block> denyKeys = new ArrayList<>(snapshot.denyBlocks);
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> !denyKeys.contains(block) && (allowKeys.isEmpty() || allowKeys.contains(block)))
                .toList();
    }

    protected boolean validate(BlockPos blockPos, Deque<Predicate<? super BlockPos>> predicates, List<Block> allowBlocks) {
        BlockState blockState = this.level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        boolean value = (allowBlocks.isEmpty() || allowBlocks.contains(block));
        for (Predicate<? super BlockPos> predicate : predicates) {
            if (!value) break;
            value = predicate.test(blockPos);
        }

        return value;

    }

    protected List<BlockPos> getBlocks(Predicate<BlockPos> filter, BlockSnapshot<BlockPos> snapshot) {
        TargetCustomizer<BlockPos, Level, RandomSource> customizer = snapshot.provider.customizer();
        AABB boxRange = snapshot.boxRange;
        SphereRange sphereRange = snapshot.sphereRange;
        Range<Double> yRange = snapshot.heightRange;
        int limit = snapshot.limit;
        boolean surface = snapshot.surface;
        PositionSelector selector = TargetSelectors.position(this.level)
                .centerAt(snapshot.referenceCenter)
                .randomSource(snapshot.random)
                .limit(limit)
                .where(vec3 -> filter.test(BlockPos.containing(vec3)))
                .loadedOnly(snapshot.loadedOnly)
                .air(snapshot.air)
                .fluid(snapshot.fluid)
                .surface(surface)
                .allowBiome(snapshot.allowBiomes)
                .denyBiome(snapshot.denyBiomes);
        if (boxRange != null) {
            selector.inBox(boxRange.getMinPosition(), boxRange.getMaxPosition());
        }

        if (sphereRange != null) {
            selector.inRange(sphereRange.center(), sphereRange.radius());
        }

        if (yRange != null && yRange.hasLowerBound() && yRange.hasUpperBound()) {
            selector.inHeightRange(yRange.lowerEndpoint(), yRange.upperEndpoint());
        }

        int count = customizer.getAttemptsPerExtraction();
        List<Vec3> posList = new ArrayList<>(
                selector.randomObtain(this.conversionCustomizer(customizer), null).toList()
        );
        List<BlockPos> blockPosList = new ArrayList<>();
        for (Vec3 vec3 : posList) {
            BlockPos result = BlockPos.containing(vec3);
            blockPosList.add(surface ? result.below() : result);
            int size = blockPosList.size();
            if (size >= count || limit > 0 && size >= limit) {
                break;
            }
        }

        return blockPosList;
    }

    protected TargetCustomizer<Vec3, Level, RandomSource> conversionCustomizer(TargetCustomizer<BlockPos, Level, RandomSource> customizer) {
        return new TargetCustomizer<>() {

            @Override
            public boolean shouldIncludePosition(Vec3 pos, Level level, RandomSource random) {
                return customizer.shouldIncludePosition(BlockPos.containing(pos), level, random);
            }

            @Override
            public int getExtractionCount(Level level, RandomSource random) {
                return customizer.getExtractionCount(level, random);
            }

            @Override
            public int getAttemptsPerExtraction() {
                return customizer.getAttemptsPerExtraction();
            }

            @Override
            public List<Vec3> onEmptyResult(Level level, RandomSource random) {
                return customizer.onEmptyResult(level, random).stream()
                        .map(BlockPos::getCenter)
                        .toList();
            }

            @Override
            public List<Vec3> postProcess(List<Vec3> targets, Level level, RandomSource random) {
                List<BlockPos> newTargets = new ArrayList<>(targets.size());
                for (Vec3 target : targets) {
                    newTargets.add(BlockPos.containing(target));
                }

                List<BlockPos> result = customizer.postProcess(newTargets, level, random);
                return result.stream()
                        .map(BlockPos::getCenter)
                        .toList();
            }
        };
    }

    protected static class BlockSnapshot<T extends BlockPos> extends Snapshot<T> {
        public final @NotNull Collection<Block> allowedBlocks;
        public final @NotNull Collection<Block> denyBlocks;
        public final StateSelection air;
        public final StateSelection fluid;
        public final boolean surface;

        public BlockSnapshot(
                @Nullable Vec3 referenceCenter,
                @Nullable RandomSource random,
                @Nullable AABB boxRange,
                @Nullable SphereRange sphereRange,
                @NotNull Collection<Block> allowedBlocks,
                @NotNull Collection<Block> denyBlocks,
                Range<Double> heightRange,
                int limit,
                boolean loadedOnly,
                Collection<ResourceKey<? extends Biome>> allowBiomes,
                Collection<ResourceKey<? extends Biome>> denyBiomes,
                Queue<Predicate<? super T>> filters,
                TargetProvider<T> provider,
                StateSelection air,
                StateSelection fluid,
                boolean surface
        ) {
            super(referenceCenter, random, boxRange, sphereRange, heightRange, limit, loadedOnly, allowBiomes, denyBiomes, filters, provider);
            this.allowedBlocks = allowedBlocks;
            this.denyBlocks = denyBlocks;
            this.surface = surface;
            this.air = air;
            this.fluid = fluid;
        }

        public static BlockSnapshot<BlockPos> of(BlockSelectorImpl selector, TargetProvider<BlockPos> provider) {
            return new BlockSnapshot<>(
                    selector.referenceCenter,
                    selector.random,
                    selector.boxRange,
                    selector.sphereRange,
                    selector.allowedBlocks,
                    selector.denyBlocks,
                    selector.heightRange,
                    selector.limit,
                    selector.loadedOnly,
                    selector.allowBiomes,
                    selector.denyBiomes,
                    selector.filters,
                    provider,
                    selector.air,
                    selector.fluid,
                    selector.surface
            );
        }
    }
}
