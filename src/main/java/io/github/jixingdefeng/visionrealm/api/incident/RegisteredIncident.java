package io.github.jixingdefeng.visionrealm.api.incident;

import io.github.jixingdefeng.visionrealm.api.selector.game.RegisteredTargetSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.common.incident.context.BaseIncidentContext;
import io.github.jixingdefeng.visionrealm.impl.incident.SimpleRegisteredIncident;
import io.github.jixingdefeng.visionrealm.impl.incident.SimpleRegisteredTargetSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A registered incident that combines an incident with a target selector and execution probability.
 * <p>
 * This interface represents a fully configured incident that can be executed at runtime.
 * It includes:
 * <ul>
 *   <li>The incident logic to execute</li>
 *   <li>A target selector factory to create selectors at execution time</li>
 *   <li>A probability value to control execution frequency</li>
 *   <li>A weight value for weighted random selection among multiple incidents</li>
 * </ul>
 * </p>
 *
 * @param <T> The target type (e.g., Vec3, Entity, BlockState)
 * @param <S> The specific selector type (e.g., PositionSelector, EntitySelector, BlockSelector)
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public interface RegisteredIncident<T, S extends TargetSelector<T, S>> extends WeightedEntry {

    /**
     * Returns the incident to be executed.
     *
     * @return The incident instance
     */
    Incident<T> getIncident();

    /**
     * Returns the registered target selector that manages target selection.
     *
     * @return The registered target selector
     */
    RegisteredTargetSelector<T, S> getTargetSelector();

    /**
     * Returns the probability that this incident actually executes when selected.
     * <p>Value should be between 0.0 and 1.0 inclusive.</p>
     *
     * @return The execution probability (0.0 = never, 1.0 = always)
     */
    float getProbability();

    /**
     * Returns the weight of this incident for weighted random selection.
     *
     * @return The weight (must be non-negative)
     */
    @Override
    @NotNull Weight getWeight();

    /**
     * Executes the incident on the server level.
     * <p>
     * This method first checks the execution probability. If the probability check passes,
     * it retrieves the targets using the registered selector and executes the incident.
     * </p>
     *
     * @param level  The server level to execute on
     * @param random The random source for probability check and incident execution
     * @return {@code true} if the incident was executed successfully, {@code false} otherwise
     */
    default boolean execute(ServerLevel level, RandomSource random) {
        if (random.nextFloat() < getProbability()) {
            Collection<T> target = this.getTargetSelector().getTarget(level, random);
            return this.getIncident().execute(this.getContext(target, level, random));
        } else {
            return false;
        }
    }

    /**
     * Executes the incident across all applicable dimensions.
     * <p>
     * This method resolves the list of server levels from {@link RegisteredTargetSelector#getLevels(MinecraftServer)},
     * then invokes {@link #execute(ServerLevel, RandomSource)} on each level.
     * The return value is {@code true} if at least one level's execution succeeds.
     * </p>
     * <p>
     * Note that all levels are processed regardless of intermediate results,
     * because the selector may have side effects (e.g., targeting entities in each dimension).
     * </p>
     *
     * @param server The Minecraft server instance
     * @param random The random source for probability checks and target selection
     * @return {@code true} if the incident executed successfully on any applicable level
     */
    default boolean execute(MinecraftServer server, RandomSource random) {
        RegisteredTargetSelector<T, S> targetSelector = this.getTargetSelector();
        List<ServerLevel> levels = targetSelector.getLevels(server);
        boolean result = true;
        for (ServerLevel level : levels) {
            result &= this.execute(level, random);
        }

        return result;
    }


    /**
     * Creates an incident context for the given targets.
     * <p>
     * This method can be overridden by subclasses to provide custom context implementations
     * that carry additional information needed by the incident.
     * </p>
     *
     * @param target The selected targets
     * @param level  The server level
     * @param random The random source
     * @return An incident context containing the targets and execution environment
     */
    default IncidentContext<T> getContext(Collection<T> target, ServerLevel level, RandomSource random) {
        return new BaseIncidentContext<>(null, target, level, random);
    }

    /**
     * Builder class for creating {@link RegisteredIncident} instances.
     * <p>
     * This builder provides a fluent API for configuring incidents with:
     * <ul>
     *   <li>Target selector factory (creates selector at execution time)</li>
     *   <li>Dimensions where the incident can execute</li>
     *   <li>Target extraction (either single or multiple)</li>
     *   <li>Weight for random selection</li>
     *   <li>Execution probability</li>
     * </ul>
     * </p>
     *
     * @param <T> The target type (e.g., Vec3, Entity, BlockState)
     * @param <S> The specific selector type (e.g., PositionSelector, EntitySelector, BlockSelector)
     */
    class Builder<T, S extends TargetSelector<T, S>> {
        private final Incident<T> incident;
        private final BiFunction<ServerLevel, RandomSource, S> targetSelector;
        private final List<ResourceKey<Level>> dimensions;
        private Function<S, Collection<T>> extractor;
        private Function<S, T> singleExtractor;
        private int weight = 1;
        private float probability = 1.0F;

        /**
         * Creates a new builder instance.
         *
         * @param incident       The incident to be executed
         * @param targetSelector A factory function that creates a target selector for the given level and random source
         * @param dimensions     The dimensions where this incident can be executed (must not be empty)
         */
        public Builder(
                @NotNull Incident<T> incident,
                @NotNull BiFunction<ServerLevel, RandomSource, S> targetSelector,
                Collection<ResourceKey<Level>> dimensions
        ) {
            this.incident = incident;
            this.targetSelector = targetSelector;
            this.dimensions = new ArrayList<>(dimensions);
        }

        public Builder(
                @NotNull Incident<T> incident,
                @NotNull BiFunction<ServerLevel, RandomSource, S> targetSelector
        ) {
            this(incident, targetSelector, List.of());
        }

        /**
         * Adds a single dimension where this incident can be executed.
         *
         * @param dimension The dimension to add
         * @return This builder instance
         */
        public Builder<T, S> addDimension(ResourceKey<Level> dimension) {
            this.dimensions.add(dimension);
            return this;
        }

        /**
         * Adds multiple dimensions where this incident can be executed.
         *
         * @param dimensions The dimensions to add
         * @return This builder instance
         */
        public Builder<T, S> addDimensions(Collection<ResourceKey<Level>> dimensions) {
            this.dimensions.addAll(dimensions);
            return this;
        }

        /**
         * Removes a dimension from the execution scope.
         *
         * @param dimension The dimension to remove
         * @return This builder instance
         */
        public Builder<T, S> removeDimension(ResourceKey<Level> dimension) {
            this.dimensions.remove(dimension);
            return this;
        }

        /**
         * Removes multiple dimensions from the execution scope.
         *
         * @param dimensions The dimensions to remove
         * @return This builder instance
         */
        public Builder<T, S> removeDimensions(Collection<ResourceKey<Level>> dimensions) {
            this.dimensions.removeAll(dimensions);
            return this;
        }

        /**
         * Sets the extractor function for obtaining multiple targets.
         * <p>
         * This is used when the registered incident should return a collection of targets.
         * Typically corresponds to operations like {@link TargetSelector#toList()}.
         * </p>
         * <p>
         * Pass {@code null} to clear the extractor. Note that single extractor has priority
         * over multi extractor when both are set.
         * </p>
         *
         * @param extractor The extractor function, or {@code null} to clear
         * @return This builder instance
         */
        public Builder<T, S> extractor(@Nullable Function<S, Collection<T>> extractor) {
            this.extractor = extractor;
            return this;
        }

        /**
         * Sets the extractor function for obtaining a single target.
         * <p>
         * This is used when the registered incident should return a single target.
         * Typically corresponds to operations like {@link TargetSelector#get()}.
         * </p>
         * <p>
         * Single extractor has higher priority than multi extractor when both are set.
         * Pass {@code null} to clear the single extractor and fall back to multi extractor.
         * </p>
         *
         * @param singleExtractor The single target extractor function, or {@code null} to clear
         * @return This builder instance
         */
        public Builder<T, S> singleExtractor(@Nullable Function<S, T> singleExtractor) {
            this.singleExtractor = singleExtractor;
            return this;
        }

        /**
         * Sets the weight for weighted random selection.
         *
         * @param weight The weight value (higher = more likely to be selected)
         * @return This builder instance
         */
        public Builder<T, S> weight(int weight) {
            this.weight = weight;
            return this;
        }

        /**
         * Sets the execution probability.
         * <p>
         * After an incident is selected by weight, this probability determines
         * whether it actually executes.
         * </p>
         *
         * @param probability Value between 0.0 and 1.0 inclusive
         * @return This builder instance
         */
        public Builder<T, S> probability(float probability) {
            this.probability = probability;
            return this;
        }

        /**
         * Builds the {@link RegisteredIncident} instance.
         *
         * @return The built registered incident
         * @throws IllegalStateException if no extractor or singleExtractor is provided
         */
        public RegisteredIncident<T, S> build() {
            if (this.extractor != null || this.singleExtractor != null) {
                return new SimpleRegisteredIncident<>(this);
            } else {
                throw new IllegalStateException("No extractor or single extractor provided");
            }
        }

        /**
         * Builds the {@link RegisteredIncident} instance using a custom builder function.
         *
         * @param builder A function that takes this builder and returns a registered incident
         * @return The built registered incident
         * @throws IllegalStateException if no extractor or singleExtractor is provided
         */
        public RegisteredIncident<T, S> build(Function<Builder<T, S>, RegisteredIncident<T, S>> builder) {
            if (this.extractor != null || this.singleExtractor != null) {
                return builder.apply(this);
            } else {
                throw new IllegalStateException("No extractor or single extractor provided");
            }
        }

        /**
         * Builds a {@link RegisteredTargetSelector} from the current builder state.
         * <p>
         * The returned selector uses the target selector factory, extraction functions,
         * and dimension restrictions configured in this builder.
         *
         * @return a new {@code RegisteredTargetSelector} instance
         */
        public RegisteredTargetSelector<T, S> buildTargetSelector() {
            return new SimpleRegisteredTargetSelector<>(this.extractor(), this.singleExtractor(), this.targetSelector(), this.dimensions());
        }

        public Incident<T> incident() {
            return this.incident;
        }

        public BiFunction<ServerLevel, RandomSource, S> targetSelector() {
            return this.targetSelector;
        }

        public Function<S, Collection<T>> extractor() {
            return this.extractor;
        }

        public Function<S, T> singleExtractor() {
            return this.singleExtractor;
        }

        public Collection<ResourceKey<Level>> dimensions() {
            return this.dimensions;
        }

        public int weight() {
            return this.weight;
        }

        public float probability() {
            return this.probability;
        }
    }
}
