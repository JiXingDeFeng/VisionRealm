package io.github.jixingdefeng.visionrealm.impl.selector.game.block;

import com.google.common.collect.Range;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.block.BlockSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.position.PositionSelector;
import io.github.jixingdefeng.visionrealm.common.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.common.selector.SurfaceSelection;
import io.github.jixingdefeng.visionrealm.common.util.selector.TargetSelectors;
import io.github.jixingdefeng.visionrealm.impl.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.BaseSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Default implementation of {@link BlockSelector} that selects {@link BlockPos}
 * targets by delegating the heavy lifting to a {@link PositionSelector}.
 *
 * <p>Rather than implementing its own position generation, this selector
 * configures an internal {@code PositionSelector} with the same spatial
 * filters (range, height, biomes, air/fluid, etc.) and then maps the
 * resulting {@link Vec3} positions to {@code BlockPos} (optionally offset
 * to the block below when {@code surface} is active). Block‑type filtering
 * ({@link #allowBlocks} / {@link #denyBlocks}) is applied as a post‑step
 * on top of the position selector's own predicate chain.
 *
 * <p>Lazy execution is fully supported; the underlying position selector
 * is only invoked when a terminal operation (e.g., {@code toList()}) is
 * called.
 *
 * <p><b>Extending:</b> Subclasses may override:
 * <ul>
 *   <li>{@link #validate(Level, BlockPos, List, List)} – to customize
 *       block validity checks.</li>
 *   <li>{@link #getBlockPos(Predicate, Level, BlockSnapshot)} – to alter
 *       the complete query logic.</li>
 *   <li>{@link #conversionCustomizer(TargetCustomizer)} – to adapt the
 *       customizer for the position‑based pipeline.</li>
 * </ul>
 *
 * @see PositionSelector
 * @see BaseSelector
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public class BlockSelectorImpl extends BaseSelector<BlockPos, BlockSelector> implements BlockSelector {
    protected @NotNull Collection<Block> allowedBlocks = new ArrayList<>(1);
    protected @NotNull Collection<Block> denyBlocks = new ArrayList<>(1);
    protected StateSelection air = StateSelection.ANY;
    protected StateSelection fluid = StateSelection.ANY;
    protected SurfaceSelection surface = SurfaceSelection.DISABLED;

    public BlockSelectorImpl(AbstractSelector<?, ?> source, @Nullable Level level) {
        super(source, level);
    }

    public BlockSelectorImpl(BlockSelectorImpl source, @Nullable Level level) {
        super(source, level);
    }

    @Override
    protected BlockSelector self() {
        return this;
    }

    @Override
    public BlockSelector allowBlocks(Block... blocks) {
        this.allowedBlocks = Arrays.asList(blocks);
        return this;
    }

    @Override
    public BlockSelector denyBlocks(Block... blocks) {
        this.denyBlocks = Arrays.asList(blocks);
        return this;
    }

    @Override
    public BlockSelector surface(SurfaceSelection surface) {
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
        this.snapshot.add(snapshot);
    }

    @Override
    protected BlockSnapshot<BlockPos> poll() {
        return (BlockSnapshot<BlockPos>) super.poll();
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
        if (!this.isExecuted()) {
            super.execute();
            Level level = this.level;
            List<BlockPos> blockPosList = new ArrayList<>();
            while (!this.snapshot.isEmpty()) {
                BlockSnapshot<BlockPos> snapshot = this.poll();
                List<Block> allowBlocks = this.getAllowBlocks(snapshot);
                blockPosList.addAll(this.getBlockPos(
                        blockPos -> this.validate(level, blockPos, snapshot.filters, allowBlocks), level, snapshot
                ));
            }

            this.cachedResults = List.copyOf(blockPosList);
        }
    }

    protected List<Block> getAllowBlocks(BlockSnapshot<BlockPos> snapshot) {
        List<Block> allowKeys = new ArrayList<>(snapshot.allowedBlocks);
        List<Block> denyKeys = new ArrayList<>(snapshot.denyBlocks);
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> !denyKeys.contains(block) && (allowKeys.isEmpty() || allowKeys.contains(block)))
                .toList();
    }

    protected boolean validate(
            Level level,
            BlockPos blockPos,
            List<Predicate<? super BlockPos>> predicates,
            List<Block> allowBlocks
    ) {
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        boolean value = (allowBlocks.isEmpty() || allowBlocks.contains(block));
        for (Predicate<? super BlockPos> predicate : predicates) {
            if (!value) break;
            value = predicate.test(blockPos);
        }

        return value;
    }

    protected List<BlockPos> getBlockPos(
            Predicate<BlockPos> filter,
            Level level,
            BlockSnapshot<BlockPos> snapshot
    ) {
        TargetProvider<BlockPos> provider = snapshot.provider;
        TargetCustomizer<BlockPos, Level, RandomSource> customizer = provider.customizer();
        AABB boxRange = snapshot.boxRange;
        SphereRange sphereRange = snapshot.sphereRange;
        Range<Double> yRange = snapshot.heightRange;
        RandomSource random = snapshot.random;
        SurfaceSelection surface = snapshot.surface;
        PositionSelector selector = TargetSelectors.position(level)
                .centerAt(snapshot.referenceCenter)
                .randomSource(random)
                .limit(this.limit)
                .filter(vec3 -> filter.test(BlockPos.containing(vec3)))
                .loadedOnly(snapshot.loadedOnly)
                .air(snapshot.air)
                .fluid(snapshot.fluid)
                .surface(surface)
                .allowBiome(snapshot.allowBiomes)
                .denyBiome(snapshot.denyBiomes);
        if (boxRange != null) {
            selector.inBox(boxRange);
        }

        if (sphereRange != null) {
            selector.inRange(sphereRange.center(), sphereRange.radius());
        }

        if (yRange != null && yRange.hasLowerBound() && yRange.hasUpperBound()) {
            selector.inHeightRange(yRange.lowerEndpoint(), yRange.upperEndpoint());
        }

        return selector.randomObtain(this.conversionCustomizer(customizer), provider.random()).stream()
                .map(vec3 -> this.resolveVec3(level, vec3, surface))
                .filter(Objects::nonNull)
                .toList();
    }

    @Nullable
    protected BlockPos resolveVec3(Level level, Vec3 vec3, SurfaceSelection surface) {
        BlockPos blockPos = BlockPos.containing(vec3);
        if (!surface.isSurfaceMode()) {
            return blockPos;
        } else {
            BlockState state = level.getBlockState(blockPos);
            VoxelShape shape = state.getCollisionShape(level, blockPos);
            if (!shape.isEmpty()) {
                return blockPos;
            } else {
                BlockPos below = blockPos.below();
                BlockState belowState = level.getBlockState(below);
                VoxelShape belowShape = belowState.getCollisionShape(level, below);
                if (!belowShape.isEmpty() || surface.allowFluid() && !state.getFluidState().isEmpty()) {
                    return below;
                } else {
                    return null;
                }
            }
        }
    }

    protected TargetCustomizer<Vec3, Level, RandomSource> conversionCustomizer(TargetCustomizer<BlockPos, Level, RandomSource> customizer) {
        return new TargetCustomizer<>() {

            @Override
            public int getExtractionCount(Level level, RandomSource random) {
                return customizer.getExtractionCount(level, random);
            }

            @Override
            public int getMaxAttemptsPerExtraction() {
                return customizer.getMaxAttemptsPerExtraction();
            }

            @Override
            public List<Vec3> onFallback(Level level, RandomSource random) {
                return customizer.onFallback(level, random).stream()
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
        public final SurfaceSelection surface;

        public BlockSnapshot(
                @Nullable Vec3 referenceCenter,
                @Nullable RandomSource random,
                @Nullable AABB boxRange,
                @Nullable SphereRange sphereRange,
                @NotNull Collection<Block> allowedBlocks,
                @NotNull Collection<Block> denyBlocks,
                Range<Double> heightRange,
                boolean loadedOnly,
                Collection<ResourceKey<? extends Biome>> allowBiomes,
                Collection<ResourceKey<? extends Biome>> denyBiomes,
                List<Predicate<? super T>> filters,
                TargetProvider<T> provider,
                StateSelection air,
                StateSelection fluid,
                SurfaceSelection surface
        ) {
            super(referenceCenter, random, boxRange, sphereRange, heightRange, loadedOnly, allowBiomes, denyBiomes, filters, provider);
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
