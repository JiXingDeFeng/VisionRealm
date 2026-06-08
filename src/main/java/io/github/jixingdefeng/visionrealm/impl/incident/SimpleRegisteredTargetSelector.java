package io.github.jixingdefeng.visionrealm.impl.incident;

import io.github.jixingdefeng.visionrealm.api.selector.game.RegisteredTargetSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A simple implementation of {@link RegisteredTargetSelector}.
 * <p>
 * This class stores a target selector factory and extraction functions.
 * The selector is created on-demand when {@link #getTarget(Level, RandomSource)} is called,
 * ensuring it always uses the current level and random source.
 * </p>
 *
 * <p><b>Usage Example (Multiple Targets):</b></p>
 * <pre>{@code
 * SimpleRegisteredTargetSelector<BlockPos, BlockSelector> registered =
 *     new SimpleRegisteredTargetSelector<>(
 *         (level, random) -> TargetSelectors.block(level).randomObtain(random),
 *         selector -> selector.toList(),
 *         null,
 *         List.of(Level.OVERWORLD)
 *     );
 * }</pre>
 *
 * <p><b>Usage Example (Single Target):</b></p>
 * <pre>{@code
 * SimpleRegisteredTargetSelector<BlockPos, BlockSelector> registered =
 *     new SimpleRegisteredTargetSelector<>(
 *         (level, random) -> TargetSelectors.block(level),
 *         null,
 *         selector -> selector.get().orElse(null),
 *         List.of(Level.OVERWORLD)
 *     );
 * }</pre>
 *
 * @param <T> The target type (e.g., Vec3, Entity, BlockState)
 * @param <S> The specific selector type (e.g., PositionSelector, EntitySelector, BlockSelector)
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public class SimpleRegisteredTargetSelector<T, S extends TargetSelector<T, S>> implements RegisteredTargetSelector<T, S> {
    protected final BiFunction<Level, RandomSource, S> targetSelector;
    protected final Function<S, Collection<T>> extractor;
    protected final Function<S, T> singleExtractor;
    protected final Collection<ResourceKey<Level>> dimension;

    public SimpleRegisteredTargetSelector(
            @Nullable Function<S, Collection<T>> extractor,
            @Nullable Function<S, T> singleExtractor,
            BiFunction<Level, RandomSource, S> targetSelector,
            Collection<ResourceKey<Level>> dimension
    ) {
        this.targetSelector = targetSelector;
        this.extractor = extractor;
        this.singleExtractor = singleExtractor;
        this.dimension = dimension;
    }

    @Override
    public S targetSelector(Level level, RandomSource random) {
        return this.targetSelector.apply(level, random);
    }

    @Override
    public Collection<T> getTarget(Level level, RandomSource random) {
        return this.singleExtractor != null
               ? Collections.singletonList(this.singleExtractor.apply(this.targetSelector(level, random)))
               : this.extractor != null ? this.extractor.apply(this.targetSelector(level, random)) : Collections.emptyList();
    }

    @Override
    public Collection<ResourceKey<Level>> getDimension() {
        return this.dimension;
    }

    @Override
    public boolean isSingle() {
        return this.singleExtractor != null;
    }
}
