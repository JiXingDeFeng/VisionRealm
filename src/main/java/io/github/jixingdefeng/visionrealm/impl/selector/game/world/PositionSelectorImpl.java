package io.github.jixingdefeng.visionrealm.impl.selector.game.world;

import com.google.common.collect.Range;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.position.PositionSelector;
import io.github.jixingdefeng.visionrealm.common.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.common.selector.SurfaceSelection;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.impl.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.BaseSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Default implementation of {@link PositionSelector} that generates random {@link Vec3} positions
 * within a defined search region and applies configurable filters.
 *
 * <p><b>Performance Considerations:</b></p>
 * Scanning a large search region may generate a large number of results, which can
 * significantly increase memory usage and degrade performance.
 * It is recommended to:
 * <ul>
 *   <li>Use {@link #limit(int)} to cap the number of results returned</li>
 *   <li>Apply restrictive filters (e.g., {@link #inRange(Vec3, double)}, {@link #inBox(Vec3, Vec3)})</li>
 *   <li>Prefer streaming operations ({@link #stream()}) over bulk collection when possible</li>
 * </ul>
 *
 * @see PositionSelector
 * @see TargetCustomizer
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public class PositionSelectorImpl extends BaseSelector<Vec3, PositionSelector> implements PositionSelector {
    protected StateSelection air = StateSelection.ANY;
    protected StateSelection fluid = StateSelection.ANY;
    protected SurfaceSelection surface = SurfaceSelection.DISABLED;

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
    public PositionSelector surface(SurfaceSelection surface) {
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
        this.snapshot.add(snapshot);
    }

    @Override
    protected PositionSnapshot<Vec3> poll() {
        return (PositionSnapshot<Vec3>) this.snapshot.poll();
    }

    @Override
    protected void execute() {
        if (!this.isExecuted()) {
            super.execute();
            Level level = this.level;
            List<Vec3> results = new ArrayList<>();
            while (!this.snapshot.isEmpty()) {
                PositionSnapshot<Vec3> snapshot = this.poll();
                List<Biome> biomeList = this.getAllowBiomes(level, snapshot);
                results.addAll(this.runExtraction(biomeList::contains, level, snapshot));
            }

            this.cachedResults = List.copyOf(results);
        }
    }

    @Override
    protected void copyFrom(AbstractSelector<?, ?> source) {
        super.copyFrom(source);
        if (source instanceof PositionSelectorImpl positionSelector) {
            this.surface = positionSelector.surface;
            this.air = positionSelector.air;
            this.fluid = positionSelector.fluid;
        }
    }

    @Override
    protected TargetCustomizer<Vec3, Level, RandomSource> defaultTargetCustomizer() {
        return (level, random) -> 5;
    }

    protected RandomSource getRandomSource(
            TargetProvider<Vec3> provider,
            RandomSource randomSource,
            RandomSource defaultRandomSource
    ) {
        if (provider.random() != null) {
            return provider.random();
        } else if (randomSource != null) {
            return randomSource;
        } else {
            return defaultRandomSource;
        }
    }

    /**
     * Generates a list of random positions based on spherical range.
     * <p>
     * <b>At least one of {@code sphereRange} or {@code boxRange} must be non-null.</b>
     *
     * @param filter         additional predicate for the generated positions
     * @param randomSource   optional override for the random source
     * @param sphereRange    spherical constraint; if {@code null}, fallback is returned immediately
     * @param provider       target provider containing the customizer
     * @param level          the server level
     * @param limit          maximum results (≤0 = no limit)
     * @param surface        if {@code true}, positions are snapped to a surface
     * @return generated positions (never {@code null}, may be empty)
     */
    protected List<Vec3> randomExtractionList(
            Predicate<Vec3> filter,
            @Nullable RandomSource randomSource,
            @Nullable SphereRange sphereRange,
            @Nullable AABB boxRange,
            TargetProvider<Vec3> provider,
            Level level,
            SurfaceSelection surface,
            int limit
    ) {
        TargetCustomizer<Vec3, Level, RandomSource> customizer = provider.customizer();
        RandomSource random = this.getRandomSource(provider, randomSource, level.random);
        int count = customizer.getExtractionCount(level, random);
        int maxAttempts = count * Math.max(1, customizer.getMaxAttemptsPerExtraction());

        if (sphereRange != null || boxRange != null) {
            List<Vec3> values;
            if (count > -1) {
                values = this.collectCandidates(
                        filter, sphereRange, boxRange, random, level, surface, limit, count, maxAttempts
                );
            } else {
                values = this.collectAllWithinRegion(filter, sphereRange, boxRange, level, surface, limit);
            }

            return new ArrayList<>(customizer.postProcess(values, level, random));
        } else {
            VisionRealm.LOGGER.error("Neither sphere range nor box range specified.");
            return customizer.onFallback(level, random);
        }
    }

    /**
     * Generates up to {@code count} valid positions within the given search regions.
     * <p>
     * Candidates are generated via {@link #randomPos} and filtered
     * through the supplied predicate. Generation stops early if the global
     * {@code limit} is reached or {@code maxAttempts} is exhausted.
     * <p>
     * <b>At least one of {@code sphereRange} or {@code boxRange} must be non-null.</b>
     *
     * @param filter       predicate to test each generated position
     * @param sphereRange  optional spherical search region
     * @param boxRange     optional box‑shaped search region
     * @param level        the server level
     * @param random       the random source for coordinate generation
     * @param surface      whether to snap positions to a solid surface
     * @param count        desired number of candidates
     * @param maxAttempts  maximum total generation attempts (count × attempts per extraction)
     * @param limit        global result limit; stops generation when {@code values.size() >= limit} (if &gt; 0)
     * @return a mutable list of valid candidate positions (never {@code null})
     */
    protected List<Vec3> collectCandidates(
            Predicate<Vec3> filter,
            @Nullable SphereRange sphereRange,
            @Nullable AABB boxRange,
            RandomSource random,
            Level level,
            SurfaceSelection surface,
            int limit,
            int count,
            int maxAttempts
    ) {
        List<Vec3> values = new ArrayList<>();
        for (int i = 0; values.size() < count && i < maxAttempts; i++) {
            Vec3 result = this.randomPos(sphereRange, boxRange, level, random, surface);
            if (result != null && !values.contains(result) && filter.test(result)) {
                values.add(result);
            }

            int size = values.size();
            if (size >= count || limit > 0 && size >= limit) {
                break;
            }
        }

        return values;
    }

    /**
     * Generates a random position within the specified search region,
     * optionally snapped to a surface.
     * <p>
     * If both ranges are provided, the box is tried first for better performance.
     * At least one of {@code sphereRange} or {@code boxRange} must be non‑null;
     * otherwise the method returns {@code null}.
     *
     * @param sphereRange optional spherical search region (used only if boxRange is null)
     * @param boxRange    optional box‑shaped search region (preferred for performance)
     * @param level       the server level
     * @param random      random source for generating coordinates
     * @param surface     if {@code true}, the resulting point is snapped to a solid surface
     * @return the generated position, or {@code null} if no valid region was specified
     *         or no surface could be found
     */
    @Nullable
    protected Vec3 randomPos(
            @Nullable SphereRange sphereRange,
            @Nullable AABB boxRange,
            Level level,
            RandomSource random,
            SurfaceSelection surface
    ) {
        Vec3 point;
        if (boxRange != null) {
            point = this.randomInBox(boxRange, random);
            return surface.isSurfaceMode()
                   ? this.snapSurface(point, level, boxRange, surface.allowFluid())
                   : point;
        } else if (sphereRange != null) {
            double radius = sphereRange.radius();
            Vec3 center = sphereRange.center();
            point = this.randomInSphere(center, random, radius);
            return surface.isSurfaceMode()
                   ? this.snapSurface(point, level, center, radius, surface.allowFluid())
                   : point;
        } else {
            return null;
        }
    }

    protected Vec3 randomInBox(
            @NotNull AABB boxRange,
            @NotNull RandomSource random
    ) {
        double x = boxRange.minX + random.nextDouble() * (boxRange.maxX - boxRange.minX);
        double y = boxRange.minY + random.nextDouble() * (boxRange.maxY - boxRange.minY);
        double z = boxRange.minZ + random.nextDouble() * (boxRange.maxZ - boxRange.minZ);
        return new Vec3(x, y, z);
    }

    /**
     * Generates a random point uniformly distributed within a sphere.
     *
     * <p>Uses the cubic root method to ensure uniform distribution throughout
     * the sphere volume.</p>
     *
     * @param center The sphere center
     * @param random The random source
     * @param radius The sphere radius (must be >= 0)
     * @return A random point within the sphere
     * @throws IllegalArgumentException if radius is negative
     */
    protected Vec3 randomInSphere(
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

    protected Vec3 snapSurface(Vec3 vec3, Level level, AABB boxRange, boolean allowFluid) {
        int py = (int) Math.floor(vec3.y);
        int minY = (int) Math.floor(boxRange.minY);
        int maxY = (int) Math.floor(boxRange.maxY);
        int upperRange = maxY - py;
        int belowRange = py - minY;
        return this.snapSurface(vec3, level, upperRange, belowRange, allowFluid);
    }

    protected Vec3 snapSurface(Vec3 vec3, Level level, Vec3 center, double radius, boolean allowFluid) {
        double px = vec3.x - center.x;
        double pz = vec3.z - center.z;
        double sqrtVal = Math.sqrt(radius * radius - px * px - pz * pz);
        if (Double.isNaN(sqrtVal)) {
            return null;
        } else {
            double topY = center.y + sqrtVal;
            double bottomY = center.y - sqrtVal;
            int upperRange = (int) Math.floor(topY - vec3.y);
            int belowRange = (int) Math.floor(vec3.y - bottomY);
            return this.snapSurface(vec3, level, upperRange, belowRange, allowFluid);
        }
    }

    /**
     * Snaps a position to a solid surface (top of a block) if
     * {@code surface} is {@code true}; otherwise returns the input unchanged.
     * <p>
     * Searches up to {@code range} blocks above and below the original
     * position to find a block with air above it.
     *
     * @return the adjusted position, or {@code null} if no valid surface was found
     */
    @Nullable
    protected Vec3 snapSurface(Vec3 vec3, Level level, int upperRange, int belowRange, boolean allowFluid) {
        BlockPos blockPos = BlockPos.containing(vec3);
        if (this.isSurfaceBlock(level, blockPos, allowFluid)) {
            return new Vec3(vec3.x, this.getSurfaceHeight(level, blockPos, allowFluid), vec3.z);
        } else {
            int posY = blockPos.getY();
            BlockPos.MutableBlockPos previous = blockPos.mutable();
            for (int i = upperRange; i > 0; i--) {
                previous.setY(posY + i);
                if (this.isSurfaceBlock(level, previous, allowFluid)) {
                    return new Vec3(vec3.x, this.getSurfaceHeight(level, previous, allowFluid), vec3.z);
                }
            }

            for (int i = belowRange; i > 0; i--) {
                previous.setY(posY - i);
                if (this.isSurfaceBlock(level, previous, allowFluid)) {
                    return new Vec3(vec3.x, this.getSurfaceHeight(level, previous, allowFluid), vec3.z);
                }
            }

            return null;
        }
    }

    protected double getSurfaceHeight(Level level, BlockPos pos, boolean allowFluid) {
        BlockState state = level.getBlockState(pos);
        VoxelShape voxelShape = state.getCollisionShape(level, pos);
        if (!voxelShape.isEmpty()) {
            AABB decisionBox = voxelShape.bounds();
            return pos.getY() + decisionBox.maxY;
        } else if (allowFluid && !state.getFluidState().isEmpty()) {
            return pos.getY() + 1;
        } else {
            return pos.getY();
        }
    }

    protected boolean isSurfaceBlock(Level level, BlockPos pos, boolean allowFluid) {
        BlockState state = level.getBlockState(pos);
        VoxelShape currentShape = state.getCollisionShape(level, pos);
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        VoxelShape aboveShape = aboveState.getCollisionShape(level, abovePos);

        boolean aboveIsEmpty = aboveShape.isEmpty();
        boolean aboveNotFluid = aboveState.getFluidState().isEmpty();
        boolean currentNotEmpty = !currentShape.isEmpty();
        boolean currentNotFluid = !state.getFluidState().isEmpty();
        return aboveIsEmpty && (!allowFluid || aboveNotFluid)
                && (currentNotEmpty || allowFluid && currentNotFluid);
    }

    /**
     * Scans the entire defined region (AABB or Sphere) and collects all qualifying positions.
     * Applies the given filter and optional surface restriction, then truncates results to the specified limit.
     * <p>
     * <b>At least one of {@code sphereRange} or {@code boxRange} must be non-null.</b>
     *
     * @param filter       predicate to test each candidate position
     * @param sphereRange  sphere region (if boxRange is null)
     * @param boxRange     AABB region (if sphereRange is null)
     * @param level        current level
     * @param surface      surface behavior (disabled/included/excluded)
     * @param limit        max number of results to return
     * @return list of collected positions (may be empty)
     */
    protected List<Vec3> collectAllWithinRegion(
            Predicate<Vec3> filter,
            @Nullable SphereRange sphereRange,
            @Nullable AABB boxRange,
            Level level,
            SurfaceSelection surface,
            int limit
    ) {
        List<Vec3> values;
        if (boxRange != null) {
            values = this.scanAABB(filter, boxRange, level, surface);
        } else if (sphereRange != null) {
            values = this.scanSphere(filter, sphereRange, level, surface);
        } else {
            throw new IllegalArgumentException("Box or Sphere range cannot be null");
        }

        if (limit >= 0) {
            return values.stream().limit(limit).toList();
        } else {
            return values;
        }
    }

    /**
     * Scans all integer block positions within an AABB, returns bottom‑center coordinates
     * (or surface heights if surface mode is enabled).
     *
     * @param filter  predicate for final position filtering
     * @param box     bounding box
     * @param level   current level
     * @param surface surface behavior
     * @return list of positions (block centers or surface points)
     */
    protected List<Vec3> scanAABB(
            Predicate<Vec3> filter,
            AABB box,
            Level level,
            SurfaceSelection surface
    ) {
        List<Vec3> values = new ArrayList<>();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Vec3 pos;

        double[] max = {box.maxX, box.maxY, box.maxZ};
        double[] min = {box.minX, box.minY, box.minZ};
        int xLength = (int) Math.floor(max[0] - min[0]);
        int height = (int) Math.floor(max[1] - min[1]);
        int zLength = (int) Math.floor(max[2] - min[2]);

        for (int i = height; i >= 0; i--) {
            for (int j = xLength; j >= 0; j--) {
                for (int k = zLength; k >= 0; k--) {
                    double[] xyz = {min[0] + j, min[1] + i, min[2] + k};
                    if (surface.isSurfaceMode()) {
                        blockPos.set(xyz[0], xyz[1], xyz[2]);
                        if (!this.isSurfaceBlock(level, blockPos, surface.allowFluid())) continue;
                        pos = new Vec3(xyz[0], this.getSurfaceHeight(level, blockPos, surface.allowFluid()), xyz[2]);
                    } else {
                        pos = new Vec3(xyz[0], xyz[1], xyz[2]);
                    }

                    if (filter.test(pos)) {
                        values.add(pos);
                    }
                }
            }
        }

        return values;
    }

    /**
     * Scans all integer block positions within a sphere, returns bottom‑center coordinates
     * (or surface heights if surface mode is enabled). Uses bounding box + distance check.
     *
     * @param filter  predicate for final position filtering
     * @param sphereRange sphere region (center + radius)
     * @param level   current level
     * @param surface surface behavior
     * @return list of positions (block centers or surface points)
     */
    protected List<Vec3> scanSphere(
            Predicate<Vec3> filter,
            SphereRange sphereRange,
            Level level,
            SurfaceSelection surface
    ) {
        List<Vec3> results = new ArrayList<>();
        Vec3 center = sphereRange.center();
        double radius = sphereRange.radius();
        int minX = (int)Math.floor(center.x - radius);
        int maxX = (int)Math.ceil(center.x + radius);
        int minY = (int)Math.floor(center.y - radius);
        int maxY = (int)Math.ceil(center.y + radius);
        int minZ = (int)Math.floor(center.z - radius);
        int maxZ = (int)Math.ceil(center.z + radius);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    double dx = x + 0.5 - center.x;
                    double dy = y + 0.5 - center.y;
                    double dz = z + 0.5 - center.z;
                    if (dx*dx + dy*dy + dz*dz > radius*radius) continue;

                    blockPos.set(x, y, z);
                    if (surface.isSurfaceMode()) {
                        if (!isSurfaceBlock(level, blockPos, surface.allowFluid())) continue;
                        double surfaceY = getSurfaceHeight(level, blockPos, surface.allowFluid());
                        Vec3 pos = new Vec3(x + 0.5, surfaceY, z + 0.5);
                        if (filter.test(pos)) results.add(pos);
                    } else {
                        Vec3 pos = new Vec3(x + 0.5, y + 0.5, z + 0.5);
                        if (filter.test(pos)) results.add(pos);
                    }
                }
            }
        }
        return results;
    }

    protected List<Vec3> runExtraction(Predicate<Biome> filter, Level level, PositionSnapshot<Vec3> snapshot) {
        Vec3 referenceCenter = snapshot.referenceCenter;
        AABB boxRange = this.offset(referenceCenter, snapshot.boxRange);
        SphereRange sphereRange = this.offset(referenceCenter, snapshot.sphereRange);
        SurfaceSelection surface = snapshot.surface;

        List<Vec3> values = this.randomExtractionList(
                pos -> this.isPositionValid(
                        filter, snapshot.filters, boxRange, sphereRange, pos, level,
                        snapshot.heightRange, snapshot.air, snapshot.fluid, surface, snapshot.loadedOnly
                ),
                snapshot.random, sphereRange, boxRange, snapshot.provider, level, surface, this.limit
        );
        if (values.isEmpty()) {
            VisionRealm.LOGGER.warn("No suitable positions found after all attempts");
        }

        return values;
    }

    /**
     * Checks whether a candidate position satisfies all active filters.
     * <p>
     * Checks are performed in order of increasing cost, short‑circuiting on
     * first failure:
     * <ol>
     *   <li>Biome filter</li>
     *   <li>Height range</li>
     *   <li>Chunk loading (if requested)</li>
     *   <li>Box containment (if a box is set)</li>
     *   <li>Sphere containment (if a sphere is set, uses squared distance)</li>
     *   <li>Surface requirement (if enabled): the block below must be a valid
     *       surface (has collision shape and air above)</li>
     *   <li>Air/fluid state selection</li>
     *   <li>Custom user predicates</li>
     * </ol>
     *
     * @param biomeFilter     biome predicate (from allow/deny lists)
     * @param filters    additional predicates added via {@code TargetSelector#filter}
     * @param boxRange   optional bounding box (null = no constraint)
     * @param sphereRange optional spherical region (null = no constraint)
     * @param pos        candidate position (block center)
     * @param level      the current level
     * @param yRange     allowed vertical range
     * @param air        air block selection rule
     * @param fluid      fluid block selection rule
     * @param surface    if {@code true}, requires the position to be on a solid surface
     * @param loadedOnly if {@code true}, only loaded chunks are accepted
     * @return {@code true} if the position passes all checks
     */
    protected boolean isPositionValid(
            Predicate<Biome> biomeFilter,
            List<Predicate<? super Vec3>> filters,
            @Nullable AABB boxRange,
            @Nullable SphereRange sphereRange,
            Vec3 pos,
            Level level,
            Range<Double> yRange,
            StateSelection air,
            StateSelection fluid,
            SurfaceSelection surface,
            boolean loadedOnly
    ) {
        BlockPos blockPos = BlockPos.containing(pos);
        BlockState state = level.getBlockState(blockPos);
        boolean value = biomeFilter.test(level.getBiome(blockPos).value())
                && yRange.contains(pos.y)
                && (!loadedOnly || level.isLoaded(blockPos));

        if (value && boxRange != null) {
            value = boxRange.contains(pos);
        }

        if (value && sphereRange != null) {
            Vec3 center = sphereRange.center();
            double radius = sphereRange.radius();
            value = pos.distanceToSqr(center) <= radius * radius;
        }

        if (value && surface.isSurfaceMode()) {
            VoxelShape currentShape = state.getCollisionShape(level, blockPos);
            BlockPos position = !currentShape.isEmpty() ? blockPos : blockPos.below();
            value = this.isSurfaceBlock(level, position, surface.allowFluid());
        }

        if (value) {
            BlockState blockState = (surface.isSurfaceMode() && state.getCollisionShape(level, blockPos).isEmpty())
                                    ? level.getBlockState(blockPos.below())
                                    : state;
            value = air.test(blockState::isAir) && fluid.test(() -> !blockState.getFluidState().isEmpty());
        }

        for (Predicate<? super Vec3> predicate : filters) {
            if (!value) break;
            value = predicate.test(pos);
        }

        return value;
    }

    protected static class PositionSnapshot<T extends Vec3> extends Snapshot<T> {
        public final StateSelection air;
        public final StateSelection fluid;
        public final SurfaceSelection surface;

        public PositionSnapshot(
                @Nullable Vec3 referenceCenter,
                @Nullable RandomSource random,
                @Nullable AABB boxRange,
                @Nullable SphereRange sphereRange,
                TargetProvider<T> provider,
                Range<Double> heightRange,
                boolean loadedOnly,
                Collection<ResourceKey<? extends Biome>> allowBiomes,
                Collection<ResourceKey<? extends Biome>> denyBiomes,
                List<Predicate<? super T>> filters,
                StateSelection air,
                StateSelection fluid,
                SurfaceSelection surface
        ) {
            super(referenceCenter, random, boxRange, sphereRange, heightRange, loadedOnly, allowBiomes, denyBiomes, filters, provider);
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
