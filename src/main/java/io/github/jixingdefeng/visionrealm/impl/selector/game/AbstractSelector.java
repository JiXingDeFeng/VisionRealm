package io.github.jixingdefeng.visionrealm.impl.selector.game;

import com.google.common.collect.Range;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract base class for all target selectors.
 * <p>
 * Manages shared configuration (biomes, ranges, predicates) and provides
 * a snapshot/queue mechanism for deferred execution. Actual target
 * retrieval is deferred to subclasses via {@link #execute()}.
 *
 * <p>Provides configuration storage, state management, and copy functionality.
 * This class handles common selector operations including:</p>
 * <ul>
 *   <li>Biome and dimension filtering</li>
 *   <li>Position and area restrictions (center, range, box, height)</li>
 *   <li>Predicate filters and result limiting</li>
 *   <li>Random source management</li>
 *   <li>Snapshot mechanism for random operations</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This class does not implement the actual query execution logic.
 * Subclasses must override {@link #execute()} to provide concrete selection behavior.</p>
 *
 * <p><b>Thread Safety:</b> Instances of this class are <em>not</em> thread-safe.
 * They are designed to be used from a single thread (typically the server thread).
 * If concurrent access is required, create a fresh instance via {@link #copy(Level)}
 * for each thread, or implement external synchronization.
 *
 * @param <T> The target type returned by the selector (e.g., Vec3, Entity, BlockState)
 * @param <S> The self-referential type for fluent chaining
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public abstract class AbstractSelector<T, S extends TargetSelector<T, S>> implements TargetSelector<T, S> {
    protected final Level level;
    protected @Nullable Vec3 referenceCenter;
    protected @Nullable RandomSource random;

    protected @Nullable AABB boxRange;
    protected @Nullable SphereRange sphereRange;

    protected Collection<T> cachedResults;
    protected @NotNull Collection<ResourceKey<? extends Biome>> allowBiomes = List.of();
    protected @NotNull Collection<ResourceKey<? extends Biome>> denyBiomes = List.of();

    protected @NotNull Range<Double> heightRange = Range.all();
    protected @NotNull List<Predicate<? super T>> filters = new ArrayList<>();
    protected @NotNull Queue<Snapshot<T>> snapshot = new ArrayDeque<>();

    protected int limit = -1;
    protected boolean loadedOnly = false;
    private boolean executed = false;

    public AbstractSelector(Level level) {
        this.level = level;
    }

    public AbstractSelector(AbstractSelector<?, ?> source, @Nullable Level level) {
        this(level != null ? level : source.level);
        this.copyFrom(source);
    }

    /**
     * Returns the current instance cast to the self-referential type S.
     */
    protected abstract S self();

    protected abstract void execute();

    @Override
    public S allowBiome(@NotNull Collection<ResourceKey<? extends Biome>> biomes) {
        this.allowBiomes = biomes;
        return this.self();
    }

    @Override
    public S denyBiome(@NotNull Collection<ResourceKey<? extends Biome>> biomes) {
        this.denyBiomes = biomes;
        return this.self();
    }

    @Override
    public S filter(Predicate<? super T> filter) {
        this.filters.add(filter);
        return this.self();
    }

    @Override
    public S centerAt(@Nullable Vec3 center) {
        this.referenceCenter = center;
        return this.self();
    }

    @Override
    public S centerAt(Function<Level, Vec3> center) {
        this.referenceCenter = center.apply(this.level);
        return this.self();
    }

    @Override
    public S inHeightRange(double minY, double maxY) {
        this.heightRange = Range.closed(minY, maxY);
        return this.self();
    }

    @Override
    public S inRange(@NotNull Vec3 center, double radius) {
        this.sphereRange = new SphereRange(center, radius);
        return this.self();
    }

    @Override
    public S inBox(Vec3 min, Vec3 max) {
        this.boxRange = new AABB(min, max);
        return this.self();
    }

    @Override
    public S inBox(AABB box) {
        this.boxRange = box;
        return this.self();
    }

    @Override
    public S inBox(double maxX, double maxY, double maxZ, double minX, double minY, double minZ) {
        this.boxRange = new AABB(maxX, maxY, maxZ, minX, minY, minZ);
        return this.self();
    }

    @Override
    public S loadedOnly(boolean loadedOnly) {
        this.loadedOnly = loadedOnly;
        return this.self();
    }

    @Override
    public S limit(int limit) {
        this.limit = limit;
        return this.self();
    }

    @Override
    @NotNull
    public Level getLevel() {
        return this.level;
    }

    @Override
    public S randomSource(RandomSource random) {
        this.random = random;
        return this.self();
    }

    public boolean isExecuted() {
        return this.executed;
    }

    /**
     * Marks this selector as having been executed.
     * <p>
     * After this call, further attempts to copy configuration from this
     * selector will be rejected (see {@link #copyFrom}).
     */
    protected void markExecuted() {
        this.executed = true;
    }

    /**
     * Copies all compatible configuration from another selector into this instance.
     * <p>This method is used internally during selector conversion. Subclasses should
     * override this method to copy their own fields, and must call {@code super.copyFrom(source)}.</p>
     *
     * <p><strong>Note:</strong> Only configuration data that is compatible between
     * selectors is copied. Predicates ({@link #filter(Predicate)}) are typically not
     * copied as they may be type-incompatible.</p>
     *
     * @param source The source selector to copy configuration from
     */
    protected void copyFrom(AbstractSelector<?, ?> source) {
        if (source == null) {
            throw new NullPointerException("Source selector cannot be null");
        } else if (source.executed) {
            throw new IllegalStateException(
                    "Source selector has already executed a terminal operation. Its configuration state has been consumed and cannot be copied."
            );
        }

        this.allowBiomes = source.allowBiomes;
        this.denyBiomes = source.denyBiomes;
        this.referenceCenter = source.referenceCenter;
        this.random = source.random;
        this.limit = source.limit;
        this.loadedOnly = source.loadedOnly;
        this.heightRange = source.heightRange;
        this.boxRange = source.boxRange;
        this.sphereRange = source.sphereRange;
    }

    /**
     * Returns the default {@link TargetCustomizer} used when no explicit
     * strategy is provided.
     */
    protected TargetCustomizer<T, Level, RandomSource> defaultTargetCustomizer() {
        return (value, level) -> -1;
    }

    /**
     * Registers a new extraction request, freezing the current selector
     * state into a {@link Snapshot} and queuing it for later execution.
     */
    protected void push(TargetProvider<T> actuator) {
        Snapshot<T> snapshot = new Snapshot<>(this, actuator);
        this.snapshot.add(snapshot);
    }

    /**
     * Retrieves and removes the oldest pending {@link Snapshot} from the queue.
     *
     * @return the next snapshot to execute, or {@code null} if the queue is empty
     */
    protected Snapshot<T> poll() {
        return this.snapshot.poll();
    }

    /**
     * Defines a spherical search region with a center and radius.
     * <p>
     * The center can be absolute or relative to a reference point (see {@link #offset(Vec3)}).
     *
     * @since 0.0.2-dev
     */
    protected record SphereRange(@NotNull Vec3 center, double radius) {

        /**
         * Creates a sphere range centered on a block position.
         *
         * @param center the block position to use as center
         * @param radius the radius of the sphere
         */
        public static SphereRange of(@NotNull BlockPos center, double radius) {
            Vec3 vec3 = center.getCenter();
            return new SphereRange(vec3, radius);
        }

        /**
         * Returns a copy of this range with the center offset by the given reference,
         * or this instance if referenceCenter is {@code null}.
         */
        public SphereRange offset(@Nullable Vec3 referenceCenter) {
            if (referenceCenter == null) {
                return this;
            } else {
                return new SphereRange(this.center.add(referenceCenter), this.radius);
            }
        }

        public BlockPos getBlockPos() {
            return BlockPos.containing(this.center);
        }
    }

    /**
     * Immutable snapshot of a selector's configuration and a {@link TargetProvider}
     * at the moment an extraction is requested.
     * <p>
     * Freezes all parameters needed to perform one batch of target selection
     * independently of the original selector's mutable state. Instances are
     * queued and consumed by the execution pipeline.
     *
     * @param <T> the type of targets this snapshot will produce
     * @since 0.0.2-dev
     */
    protected static class Snapshot<T> {
        public final @Nullable Vec3 referenceCenter;
        public final @Nullable RandomSource random;
        public final @Nullable SphereRange sphereRange;
        public final @Nullable AABB boxRange;
        public final TargetProvider<T> provider;
        public final Range<Double> heightRange;
        public final boolean loadedOnly;
        public final Collection<ResourceKey<? extends Biome>> allowBiomes;
        public final Collection<ResourceKey<? extends Biome>> denyBiomes;
        public final List<Predicate<? super T>> filters;

        public Snapshot(
                @Nullable Vec3 referenceCenter,
                @Nullable RandomSource random,
                @Nullable AABB boxRange,
                @Nullable SphereRange sphereRange,
                Range<Double> heightRange,
                boolean loadedOnly,
                Collection<ResourceKey<? extends Biome>> allowBiomes,
                Collection<ResourceKey<? extends Biome>> denyBiomes,
                List<Predicate<? super T>> filters,
                TargetProvider<T> provider
        ) {
            this.referenceCenter = referenceCenter;
            this.random = random;
            this.heightRange = heightRange;
            this.boxRange = boxRange;
            this.sphereRange = sphereRange;
            this.loadedOnly = loadedOnly;
            this.allowBiomes = allowBiomes;
            this.denyBiomes = denyBiomes;
            this.filters = new ArrayList<>(filters);
            this.provider = provider;
        }

        public <S extends TargetSelector<T, S>> Snapshot(
                AbstractSelector<T, S> selector,
                TargetProvider<T> provider
        ) {
            this(
                    selector.referenceCenter,
                    selector.random,
                    selector.boxRange,
                    selector.sphereRange,
                    selector.heightRange,
                    selector.loadedOnly,
                    selector.allowBiomes,
                    selector.denyBiomes,
                    selector.filters,
                    provider
            );
            VisionRealm.LOGGER.warn("\n\nS");
        }
    }

    /**
     * Bundles an extraction strategy with optional random source and a flag
     * indicating whether only a single result is desired.
     * <p>
     * Stored inside a {@link Snapshot} and evaluated by the selector's execution
     * logic.
     *
     * @param <T> the target type
     * @since 0.0.2-dev
     */
    protected record TargetProvider<T>(
            @NotNull TargetCustomizer<T, Level, RandomSource> customizer,
            @Nullable RandomSource random,
            boolean single
    ) {
        public static <T> TargetProvider<T> of(
                @NotNull TargetCustomizer<T, Level, RandomSource> customizer,
                @Nullable RandomSource random
        ) {
            return new TargetProvider<>(customizer, random, false);
        }
    }
}
