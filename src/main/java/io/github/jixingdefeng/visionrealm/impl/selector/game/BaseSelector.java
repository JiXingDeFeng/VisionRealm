package io.github.jixingdefeng.visionrealm.impl.selector.game;

import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.common.util.collector.CollectionUtils;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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
import java.util.function.Function;
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
public class BaseSelector<T, S extends TargetSelector<T, S>> extends AbstractSelector<T, S> implements TargetSelector<T, S> {

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
        return this.executed ? this.cachedResults.size() : -1;
    }

    @Override
    public int count() {
        this.execution();
        return this.cachedResults.size();
    }

    @Override
    public Stream<T> stream() {
        this.execution();
        return this.cachedResults.stream();
    }

    @Override
    public List<T> toList() {
        this.execution();
        return new ArrayList<>(this.cachedResults);
    }

    @Override
    public T[] toArray(IntFunction<T[]> generator) {
        this.execution();
        return generator.apply(this.cachedResults.size());
    }

    @Override
    public Optional<T> get(Function<Integer, Integer> generator) {
        this.execution();
        List<T> results = this.toList();
        if (!CollectionUtils.isEmpty(results)) {
            return Optional.of(results.get(generator.apply(results.size())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<T> get() {
        this.execution();
        List<T> results = this.toList();
        if (!CollectionUtils.isEmpty(results)) {
            RandomSource random = this.random != null ? this.random : RandomSource.create();
            return Optional.of(results.get(random.nextInt(results.size())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    @Override
    protected void execute() {
        this.executed = true;
    }

    protected void execution() {
        if (!this.executed && this.level != null) {
            this.execute();
        } else {
            VisionRealm.LOGGER.error("");
        }
    }

    protected Registry<Biome> getBiomeRegistry(ServerLevel level) {
        return level.getServer()
                .registryAccess()
                .registryOrThrow(Registries.BIOME);
    }

    protected List<Biome> getAllowBiomes(ServerLevel level, Snapshot<T> snapshot) {
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

    @Nullable
    protected AABB getOffsetResult(@Nullable Vec3 referenceCenter, AABB boundingBox) {
        if (boundingBox == null) {
            return null;
        } else if (referenceCenter == null) {
            return boundingBox;
        } else {
            Vec3 max = boundingBox.getMaxPosition();
            Vec3 min = boundingBox.getMinPosition();
            return new AABB(min.add(referenceCenter), max.add(referenceCenter));
        }
    }

    public <T1, R extends TargetSelector<T1, R>> R converted(BiFunction<BaseSelector<T, S>, Level, R> selector) {
        return selector.apply(this, this.level);
    }

    protected void copyFrom(BaseSelector<T, S> source) {
        super.copyFrom(source);
        this.filters = source.filters;
        this.snapshotDeque = source.snapshotDeque;
    }
}
