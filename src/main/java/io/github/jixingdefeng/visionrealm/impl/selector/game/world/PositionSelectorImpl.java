package io.github.jixingdefeng.visionrealm.impl.selector.game.world;

import com.google.common.collect.Range;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.position.PositionSelector;
import io.github.jixingdefeng.visionrealm.common.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.impl.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.BaseSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class PositionSelectorImpl extends BaseSelector<Vec3, PositionSelector> implements PositionSelector {
    protected StateSelection air = StateSelection.ANY;
    protected StateSelection fluid = StateSelection.ANY;
    protected boolean surface = false;

    public PositionSelectorImpl(AbstractSelector<?, ?> source, @Nullable Level level) {
        super(source, level);
    }

    public PositionSelectorImpl(PositionSelectorImpl source, @Nullable Level level) {
        super(source, level);
        this.surface = source.surface;
        this.air = source.air;
        this.fluid = source.fluid;
    }

    @Override
    protected PositionSelector self() {
        return this;
    }

    @Override
    public PositionSelector surface(boolean surface) {
        this.surface = surface;
        return this;
    }

    @Override
    public PositionSelector air(StateSelection air) {
        this.air = air;
        return this;
    }

    @Override
    public PositionSelector fluid(StateSelection fluid) {
        this.fluid = fluid;
        return this;
    }

    @Override
    public PositionSelector copy(@Nullable Level level) {
        return new PositionSelectorImpl(this, level);
    }

    @Override
    protected void push(TargetProvider<Vec3> actuator) {
        PositionSnapshot<Vec3> snapshot = PositionSnapshot.of(this, actuator);
        this.snapshotDeque.add(snapshot);
    }

    @Override
    protected PositionSnapshot<Vec3> pop() {
        return (PositionSnapshot<Vec3>) this.snapshotDeque.poll();
    }

    @Override
    protected void execute() {
        if (this.level instanceof ServerLevel serverLevel) {
            if (!this.isExecuted()) {
                super.execute();
                while (!this.snapshotDeque.isEmpty()) {
                    PositionSnapshot<Vec3> snapshot = this.pop();
                    List<Biome> biomeList = this.getAllowBiomes(serverLevel, snapshot);
                    this.randomExtraction(biomeList::contains, serverLevel, snapshot);
                }
            }
        }
    }

    /**
     * Generates a list of random positions based on the provided configuration.
     *
     * @param filter                    Additional filter predicate for the generated positions
     * @param referenceCenter           Reference center for relative positioning
     * @param randomSource              Optional custom random source
     * @param sphereRange               Spherical range constraint (null = use player-centered fallback)
     * @param provider                  Target provider containing the customizer
     * @param level                     The server level
     * @param limit                     Maximum number of results to return (0 = no limit)
     * @param loadedOnly                Whether to only return positions in loaded chunks
     * @return List of generated positions (never {@link null}, may be empty)
     */
    protected List<Vec3> randomExtractionList(
            Predicate<Vec3> filter,
            @Nullable Vec3 referenceCenter,
            @Nullable RandomSource randomSource,
            @Nullable SphereRange sphereRange,
            TargetProvider<Vec3> provider,
            ServerLevel level,
            int limit,
            boolean surface,
            boolean loadedOnly
    ) {
        List<Vec3> values = new ArrayList<>();
        TargetCustomizer<Vec3, Level, RandomSource> customizer = provider.customizer();
        RandomSource random = level.random;
        if (provider.random() != null) {
            random = provider.random();
        } else if (randomSource != null) {
            random = randomSource;
        }

        int count = customizer.getExtractionCount(level, random);
        int maxAttempts = count * Math.max(1, customizer.getAttemptsPerExtraction());

        for (int i = 0; values.size() < count && i < maxAttempts; i++) {
            Vec3 result = this.generateRandomPosition(customizer, referenceCenter, sphereRange, level, random, surface, loadedOnly);
            if (result != null) {
                if (filter.test(result) && (!loadedOnly || level.isLoaded(BlockPos.containing(result)))) {
                    values.add(result);
                }
            }

            int size = values.size();
            if (size >= count || limit > 0 && size >= limit) {
                break;
            }
        }

        if (values.isEmpty()) {
            return customizer.onEmptyResult(level, random);
        } else {
            return new ArrayList<>(customizer.postProcess(values, level, random));
        }
    }

    @Nullable
    protected Vec3 generateRandomPosition(
            @NotNull TargetCustomizer<Vec3, Level, RandomSource> customizer,
            @Nullable Vec3 referenceCenter,
            @Nullable SphereRange sphereRange,
            ServerLevel level,
            RandomSource random,
            boolean surface,
            boolean loadedOnly
    ) {
        Vec3 center = this.calculateCenter(referenceCenter, sphereRange, random, level, customizer.getAttemptsPerExtraction(), loadedOnly);
        double radius = sphereRange != null ? sphereRange.radius() : 128;
        Vec3 value = this.getRandomPointInSphere(center, random, radius);
        if (customizer.shouldIncludePosition(value, level, random)) {
            int distance = (int) (radius - Math.abs(center.y - value.y));
            return this.getSurface(level, value, distance, surface);
        } else {
            return null;
        }
    }

    protected Vec3 calculateCenter(
            @Nullable Vec3 referenceCenter,
            @Nullable SphereRange sphereRange,
            RandomSource random,
            ServerLevel level,
            int maxRetries,
            boolean loadedOnly
    ) {
        if (sphereRange != null) {
            return sphereRange.getCenter(referenceCenter);
        } else {
            return this.getRandomCenter(random, level, maxRetries, loadedOnly);
        }
    }

    protected Vec3 getRandomCenter(@Nullable RandomSource randomSource, ServerLevel level, int maxRetries, boolean loadedOnly) {
        RandomSource random = randomSource != null ? randomSource : level.random;
        Vec3 center = level.getSharedSpawnPos().getCenter();
        List<ServerPlayer> players = new ArrayList<>(level.players());
        if (!players.isEmpty()) {
            center = players.get(random.nextInt(players.size())).position();
        }

        for (int i = 0; i < maxRetries; i++) {
            Vec3 result = this.getRandomPointInSphere(center, random, 128);
            if (!loadedOnly || level.isLoaded(BlockPos.containing(result))) {
                return result;
            }
        }

        return center;
    }

    /**
     * Generates a random point uniformly distributed within a sphere.
     *
     * <p>Uses the cubic root method to ensure uniform distribution throughout
     * the sphere volume, not just on the surface.</p>
     *
     * @param center The sphere center
     * @param random The random source
     * @param radius The sphere radius (must be >= 0)
     * @return A random point within the sphere
     * @throws IllegalArgumentException if radius is negative
     */
    protected Vec3 getRandomPointInSphere(
            @NotNull Vec3 center,
            @NotNull RandomSource random,
            double radius
    ) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius cannot be negative");
        }

        double r = radius * Math.cbrt(random.nextDouble());
        double theta = 2 * Math.PI * random.nextDouble();
        double phi = Math.acos(2 * random.nextDouble() - 1);

        double x = r * Math.sin(phi) * Math.cos(theta);
        double y = r * Math.sin(phi) * Math.sin(theta);
        double z = r * Math.cos(phi);

        return center.add(x, y, z);
    }

    @Nullable
    protected Vec3 getSurface(Level level, Vec3 pos, int range, boolean surface) {
        if (!surface) {
            return pos;
        } else {
            BlockPos blockPos = BlockPos.containing(pos);
            BlockState state = level.getBlockState(blockPos);
            BlockState belowState = level.getBlockState(blockPos.below());
            if (state.isAir() && !belowState.isAir()) {
                return pos;
            } else {
                BlockState aboveState = level.getBlockState(blockPos.above());
                for (int i = range; i >= -range; i--) {
                    BlockPos value = blockPos.offset(0, i, 0);
                    BlockState blockState = level.getBlockState(value);
                    if (aboveState.isAir() && !blockState.isAir()) {
                        double yOffset = (blockPos.getY() - pos.y) + i + 1;
                        return pos.add(0, yOffset, 0);
                    } else {
                        aboveState = blockState;
                    }
                }

                return null;
            }
        }
    }

    private void randomExtraction(Predicate<Biome> filter, ServerLevel level, PositionSnapshot<Vec3> snapshot) {
        Vec3 referenceCenter = snapshot.referenceCenter;
        boolean surface = snapshot.surface;
        List<Vec3> values = this.randomExtractionList(
                pos -> this.isPositionValid(
                        this.getOffsetResult(referenceCenter, snapshot.boxRange), pos, level, filter,
                        snapshot.heightRange, snapshot.filters, snapshot.air, snapshot.fluid, surface
                ),
                referenceCenter, snapshot.random, snapshot.sphereRange,
                snapshot.provider, level, snapshot.limit, surface, snapshot.loadedOnly
        );
        if (values.isEmpty()) {
            VisionRealm.LOGGER.warn("No suitable positions found after all attempts");
        }

        this.cachedResults.addAll(values);
    }

    private boolean isPositionValid(
            @Nullable AABB boxRange,
            Vec3 pos,
            ServerLevel level,
            Predicate<Biome> filter,
            Range<Double> yRange,
            Deque<Predicate<? super Vec3>> filters,
            StateSelection air,
            StateSelection fluid,
            boolean surface
    ) {
        BlockPos blockPos = BlockPos.containing(pos);
        BlockState state = level.getBlockState(blockPos);
        boolean value = filter.test(level.getBiome(blockPos).value())
                && yRange.contains(pos.y)
                && (boxRange == null || boxRange.contains(pos))
                && (
                surface || air.test(state::isAir)
                        && (fluid.test(() -> state.getFluidState().getType() != Fluids.EMPTY))
        );
        for (Predicate<? super Vec3> predicate : filters) {
            if (!value) break;
            value = predicate.test(pos);
        }

        return value;
    }


    protected static class PositionSnapshot<T extends Vec3> extends Snapshot<T> {
        public final StateSelection air;
        public final StateSelection fluid;
        public final boolean surface;

        public PositionSnapshot(
                @Nullable Vec3 referenceCenter,
                @Nullable RandomSource random,
                @Nullable AABB boxRange,
                @Nullable SphereRange sphereRange,
                TargetProvider<T> provider,
                Range<Double> heightRange,
                int limit,
                boolean loadedOnly,
                Collection<ResourceKey<? extends Biome>> allowBiomes,
                Collection<ResourceKey<? extends Biome>> denyBiomes,
                Queue<Predicate<? super T>> filters,
                StateSelection air,
                StateSelection fluid,
                boolean surface
        ) {
            super(referenceCenter, random, boxRange, sphereRange, heightRange, limit, loadedOnly, allowBiomes, denyBiomes, filters, provider);
            this.surface = surface;
            this.air = air;
            this.fluid = fluid;
        }

        public static PositionSnapshot<Vec3> of(
                PositionSelectorImpl selector,
                TargetProvider<Vec3> provider
        ) {
            return new PositionSnapshot<>(
                    selector.referenceCenter,
                    selector.random,
                    selector.boxRange,
                    selector.sphereRange,
                    provider,
                    selector.heightRange,
                    selector.limit,
                    selector.loadedOnly,
                    selector.allowBiomes,
                    selector.denyBiomes,
                    selector.filters,
                    selector.air,
                    selector.fluid,
                    selector.surface
            );
        }
    }
}
