package io.github.jixingdefeng.visionrealm.core.selector.game;

import io.github.jixingdefeng.visionrealm.api.selector.game.RegisteredTargetSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A simple implementation of {@link RegisteredTargetSelector}.
 * <p>
 * This class stores a target selector factory and extraction functions.
 * The selector is created on-demand when {@link #getTarget(ServerLevel, RandomSource)} is called,
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
 * @since 0.1.0
 */
public class SimpleRegisteredTargetSelector<T, S extends TargetSelector<T, S>> implements RegisteredTargetSelector<T, S> {
    protected final BiFunction<ServerLevel, RandomSource, S> targetSelector;
    protected final Function<S, Collection<T>> multiExtractor;
    protected final Function<S, T> singleExtractor;
    protected final Collection<ResourceKey<Level>> dimension;

    /**
     * Constructs a registered target selector.
     * <p>
     * Exactly one of {@code extractor} or {@code singleExtractor} should be non‑{@code null},
     * indicating whether the selector returns multiple targets or a single target.
     *
     * @param multiExtractor       function that extracts a collection of targets from the selector
     *                        (used when the selector returns multiple targets)
     * @param singleExtractor function that extracts a single target from the selector
     *                        (used when the selector returns a single target)
     * @param targetSelector  factory that creates the underlying {@link TargetSelector}
     *                        given a {@link Level} and {@link RandomSource}
     * @param dimension       collection of dimension keys where this selector is valid
     */
    public SimpleRegisteredTargetSelector(
            @Nullable Function<S, Collection<T>> multiExtractor,
            @Nullable Function<S, T> singleExtractor,
            BiFunction<ServerLevel, RandomSource, S> targetSelector,
            Collection<ResourceKey<Level>> dimension
    ) {
        this.targetSelector = targetSelector;
        this.multiExtractor = multiExtractor;
        this.singleExtractor = singleExtractor;
        this.dimension = dimension;
    }

    @Override
    public S targetSelector(ServerLevel level, RandomSource random) {
        return this.targetSelector.apply(level, random);
    }

    @Override
    public Collection<T> getTarget(ServerLevel level, RandomSource random) {
        return this.singleExtractor != null
               ? Collections.singletonList(this.singleExtractor.apply(this.targetSelector(level, random)))
               : this.multiExtractor != null ? this.multiExtractor.apply(this.targetSelector(level, random)) : Collections.emptyList();
    }

    @Override
    public Collection<T> getTarget(MinecraftServer server, RandomSource random) {
        return this.getLevels(server).stream()
                .flatMap(level -> this.getTarget(level, random).stream())
                .toList();
    }

    @Override
    public Collection<ResourceKey<Level>> getDimensionKeys() {
        return List.copyOf(this.dimension);
    }

    @Override
    public List<ServerLevel> getLevels(MinecraftServer server) {
        Collection<ResourceKey<Level>> levels = this.getDimensionKeys();
        return (!levels.isEmpty() ? levels : this.getRegisteredDimensionKeys(server))
                .stream()
                .map(server::getLevel)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean isSingle() {
        return this.singleExtractor != null;
    }

    public Collection<ResourceKey<Level>> getRegisteredDimensionKeys(MinecraftServer server) {
        Registry<Level> registry = server.registryAccess().registry(Registries.DIMENSION).orElse(null);
        return registry != null ? registry.registryKeySet() : Collections.emptyList();
    }
}
