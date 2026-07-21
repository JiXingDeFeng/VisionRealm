package io.github.jixingdefeng.visionrealm.impl.selector.game;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * Base implementation of {@link TargetSelector} that provides full terminal operation logic.
 * <p>This class extends {@link AbstractSelector} and implements all terminal operations
 * ({@link #toList()}, {@link #stream()}, {@link #get()}, etc.) using the cached results
 * populated by {@link #execute()}.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Executes queries lazily - results are cached after the first terminal operation</li>
 *   <li>Supports random selections via {@link TargetProvider} and {@link TargetCustomizer}</li>
 *   <li>Provides snapshot mechanism for storing random operation configurations</li>
 *   <li>Supports copying with optional level override</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This class does not provide default query execution logic.
 * Subclasses must override {@link #execute()} to implement concrete world queries.
 * The random provider mechanism ({@link #randomSingle(RandomSource)} and related methods)
 * stores configuration snapshots for processing during execution.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create a concrete selector
 * PositionSelector selector = new PositionSelectorImpl(level)
 *     .centerAt(player.position())
 *     .inRange(50)
 *     .inAir();
 *
 * // Execute and get results
 * List<Vec3> results = selector.toList();
 *
 * // Copy with same level
 * PositionSelector copy = selector.copy();
 *
 * // Copy with different level
 * PositionSelector copy2 = selector.copy(otherLevel);
 * }</pre>
 *
 * @param <T> The target type returned by the selector (e.g., Vec3, Entity, BlockState)
 * @param <S> The self-referential type for fluent chaining
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public class BaseSelector<T, S extends TargetSelector<T, S>> extends AbstractSelector<T, S> {

    public BaseSelector(Level level) {
        super(level);
    }

    public BaseSelector(AbstractSelector<?, ?> selector, @Nullable Level level) {
        super(selector, level);
    }

    public BaseSelector(BaseSelector<T, S> selector, @Nullable Level level) {
        this(level != null ? level : selector.level);
        this.copyFrom(selector);
    }

    @Override
    public S randomSingle(@Nullable RandomSource random) {
        this.push(new TargetProvider<>(this.defaultTargetCustomizer(), random, true));
        return this.self();
    }

    @Override
    public S randomObtain(@Nullable RandomSource random) {
        this.push(TargetProvider.of(this.defaultTargetCustomizer(), random));
        return this.self();
    }

    @Override
    public S randomObtain(TargetCustomizer<T, Level, RandomSource> actuator, @Nullable RandomSource random) {
        this.push(TargetProvider.of(actuator, random));
        return this.self();
    }

    @Override
    @SuppressWarnings("unchecked")
    public S copy(@Nullable Level level) {
        return (S) new BaseSelector<>(this, level);
    }

    @Override
    public int size() {
        return this.isExecuted() ? this.cachedResults.size() : -1;
    }

    @Override
    public int count() {
        this.ensureExecuted();
        return this.cachedResults.size();
    }

    @Override
    public Stream<T> stream() {
        this.ensureExecuted();
        return this.cachedResults.stream();
    }

    @Override
    public List<T> toList() {
        this.ensureExecuted();
        return new ArrayList<>(this.cachedResults);
    }

    @Override
    public T[] toArray(IntFunction<T[]> generator) {
        this.ensureExecuted();
        return generator.apply(this.cachedResults.size());
    }

    @Override
    public Optional<T> get(IntFunction<Integer> generator) {
        this.ensureExecuted();
        List<T> results = this.toList();
        if (results != null && !results.isEmpty()) {
            return Optional.of(results.get(generator.apply(results.size())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<T> get() {
        return this.get(i -> 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    @Override
    protected void execute() {
    }

    /**
     * Ensures the selector has been executed at least once, if a level is present.
     * <p>
     * If no level is available (e.g., selector not bound to a world), an error
     * is logged and execution is skipped.
     */
    protected void ensureExecuted() {
        if (!this.isExecuted()) {
            if (this.level != null) {
                this.execute();
                this.markExecuted();
            } else {
                VisionRealm.LOGGER.error(
                        "Cannot execute selector [{}] because it is not bound to a level. Ensure the selector was created with a valid Level or call copy(Level) to bind it.",
                        this.toShortString()
                );
            }
        } else {
            VisionRealm.LOGGER.warn(
                    "Selector [{}] has already been executed; returning cached results. Use copy(Level) if you need a fresh selection.",
                    this.toShortString());
        }
    }

    /**
     * Returns a short human-readable description of this selector for debugging.
     * Subclasses may override to add type-specific details.
     */
    protected String toShortString() {
        return String.format("%s{limit=%d, filters=%d}", getClass().getSimpleName(), this.limit, this.filters.size());
    }

    protected Registry<Biome> getBiomeRegistry(Level level) {
        return level.registryAccess().registryOrThrow(Registries.BIOME);
    }

    /**
     * Computes the list of biomes that pass the snapshot's allow/deny rules.
     * <p>
     * A biome is included if it is:
     * <ul>
     *   <li>not in the deny list,</li>
     *   <li>and either the allow list is empty or the biome is in the allow list.</li>
     * </ul>
     */
    protected List<Biome> getAllowBiomes(Level level, Snapshot<T> snapshot) {
        Registry<Biome> biomeRegistry = getBiomeRegistry(level);
        List<ResourceKey<? extends Biome>> allowKeys = new ArrayList<>(snapshot.allowBiomes);
        List<ResourceKey<? extends Biome>> denyKeys = new ArrayList<>(snapshot.denyBiomes);
        return biomeRegistry.stream()
                .filter(biome -> biomeRegistry.getResourceKey(biome)
                        .filter(key ->
                                !denyKeys.contains(key) && (allowKeys.isEmpty() || allowKeys.contains(key)))
                        .isPresent()
                )
                .toList();
    }

    /**
     * Offsets the given bounding box by the specified reference center.
     * <p>
     * If {@code boxRange} is {@code null}, this method returns {@code null}.
     * If {@code referenceCenter} is {@code null}, the original {@code boxRange}
     * is returned unchanged. Otherwise, a new {@code AABB} with both corners
     * translated by {@code referenceCenter} is created and returned.
     *
     * @param referenceCenter the offset to apply, may be {@code null}
     * @param boxRange        the bounding box to offset, may be {@code null}
     * @return the offset bounding box, or {@code null} if {@code boxRange} was {@code null}
     */
    @Nullable
    protected AABB offset(@Nullable Vec3 referenceCenter, AABB boxRange) {
        if (boxRange == null) {
            return null;
        } else if (referenceCenter == null) {
            return boxRange;
        } else {
            Vec3 max = boxRange.getMaxPosition();
            Vec3 min = boxRange.getMinPosition();
            return new AABB(min.add(referenceCenter), max.add(referenceCenter));
        }
    }

    /**
     * Offsets the given {@link SphereRange} by the specified reference center.
     * <p>
     * If {@code sphereRange} is {@code null}, this method returns {@code null}.
     * If {@code referenceCenter} is {@code null}, the original {@code sphereRange}
     * is returned unchanged. Otherwise, a new {@code SphereRange} with the center
     * translated by {@code referenceCenter} is created and returned.
     *
     * @param referenceCenter the offset to apply to the sphere's center, may be {@code null}
     * @param sphereRange     the spherical region to offset, may be {@code null}
     * @return the offset sphere range, or {@code null} if {@code sphereRange} was {@code null}
     */
    @Nullable
    protected SphereRange offset(@Nullable Vec3 referenceCenter, SphereRange sphereRange) {
        if (sphereRange == null) {
            return null;
        } else if (referenceCenter == null) {
            return sphereRange;
        } else {
            return sphereRange.offset(referenceCenter);
        }
    }

    public <T1, R extends TargetSelector<T1, R>> R converted(BiFunction<BaseSelector<T, S>, Level, R> selector) {
        return selector.apply(this, this.level);
    }

    /**
     * Deep‑copies mutable state (filters, snapshots) from another BaseSelector.
     * <p>
     * Called by copy constructors to transfer configuration.
     */
    protected void copyFrom(BaseSelector<T, S> source) {
        super.copyFrom(source);
        this.filters = source.filters;
        this.snapshot = source.snapshot;
    }
}
