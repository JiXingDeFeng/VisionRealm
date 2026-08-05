package io.github.jixingdefeng.visionrealm.api.incident;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.selector.game.RegisteredTargetSelector;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistry;
import io.github.jixingdefeng.visionrealm.core.incident.SimpleRegisteredIncident;
import io.github.jixingdefeng.visionrealm.core.incident.context.IncidentContext;
import io.github.jixingdefeng.visionrealm.core.selector.game.SimpleRegisteredTargetSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
 * @since 0.1.0
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
     * Returns whether the target selector should search across all dimensions for targets,
     * or limit the search to the dimension where the incident is currently executing.
     * <p>
     * When {@code true}, the target selector will consider all available dimensions.
     * When {@code false}, only the current execution dimension will be used as the target source.
     *
     * @return {@code true} to search targets in all dimensions, {@code false} to limit
     *         to the current dimension
     */
    boolean runInAllDimensions();

    /**
     * Executes the incident on a single server level.
     * <p>
     * This method is the main entry point for executing an incident on a specific dimension.
     * It logs the execution and delegates to {@link #executeInternal(ServerLevel, RandomSource)}.
     *
     * @param level  the server level to execute on
     * @param random the random source for probability check and incident execution
     * @return {@code true} if the incident was executed successfully, {@code false} otherwise
     * @see #executeInternal(ServerLevel, RandomSource)
     */
    default boolean execute(ServerLevel level, RandomSource random) {
        VisionRealm.LOGGER.info("Executing incident [{}] in dimension: {}", ModRegistry.INCIDENT.getKey(this), level.dimension().location());
        return this.executeInternal(level, random);
    }

    /**
     * Executes the incident across all applicable dimensions.
     * <p>
     * This method resolves the list of server levels from {@link RegisteredTargetSelector#getLevels(MinecraftServer)},
     * then invokes the incident on each level or a single randomly selected level,
     * depending on the value of {@link #runInAllDimensions()}.
     * The return value is {@code true} if at least one level's execution succeeds.
     * </p>
     * <p>
     * Note that all levels are processed regardless of intermediate results.
     * </p>
     *
     * @param server the Minecraft server instance
     * @param random the random source for probability checks and target selection
     * @return {@code true} if the incident executed successfully on any applicable level
     */
    default boolean execute(MinecraftServer server, RandomSource random) {
        RegisteredTargetSelector<T, S> targetSelector = this.getTargetSelector();
        List<ServerLevel> levels = targetSelector.getLevels(server);
        if (this.runInAllDimensions()) {
            ResourceLocation incidentId = ModRegistry.INCIDENT.getKey(this);
            VisionRealm.LOGGER.info("Executing incident [{}] across {} dimensions", incidentId, levels.size());

            boolean result = true;
            List<ResourceLocation> levelLocations = new ArrayList<>(levels.size());
            for (ServerLevel level : levels) {
                levelLocations.add(level.dimension().location());
                result &= this.executeInternal(level, random);
            }

            VisionRealm.LOGGER.info("Incident [{}] executed in dimensions: {}", incidentId, levelLocations);
            return result;
        } else {
            ServerLevel level = levels.get(random.nextInt(levels.size()));
            return this.execute(level, random);
        }
    }

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
    default boolean executeInternal(ServerLevel level, RandomSource random) {
        if (random.nextFloat() < getProbability()) {
            Collection<T> target = this.getTargetSelector().getTarget(level, random);
            return this.getIncident().execute(this.getContext(target, level, random));
        } else {
            return false;
        }
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
        return new IncidentContext<>(target, level, random);
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
        private final List<ResourceKey<Level>> dimensions = new ArrayList<>();
        private Function<S, Collection<T>> multiExtractor;
        private Function<S, T> singleExtractor;
        private int weight = 1;
        private float probability = 1.0F;
        protected boolean runInAllDimensions = false;

        /**
         * Creates a new builder instance with an empty dimension list.
         *
         * @param incident       The incident to be executed
         * @param targetSelector A factory function that creates a target selector for the given level and random source
         */
        public Builder(
                @NotNull Incident<T> incident,
                @NotNull BiFunction<ServerLevel, RandomSource, S> targetSelector
        ) {
            this.incident = incident;
            this.targetSelector = targetSelector;
        }

        /**
         * Adds one or more dimensions where this incident can be executed.
         * <p>
         * This method accepts a varargs array of dimension resource keys.
         *
         * @param dimension The dimensions to add
         * @return This builder instance
         */
        @SafeVarargs
        public final Builder<T, S> addDimensions(ResourceKey<Level>... dimension) {
            this.dimensions.addAll(List.of(dimension));
            return this;
        }

        /**
         * Removes one or more dimensions from the execution scope.
         * <p>
         * This method accepts a varargs array of dimension resource keys.
         *
         * @param dimension The dimensions to remove
         * @return This builder instance
         */
        @SafeVarargs
        public final Builder<T, S> removeDimensions(ResourceKey<Level>... dimension) {
            this.dimensions.removeAll(List.of(dimension));
            return this;
        }

        /**
         * Replaces the current dimension list with the given collection.
         * <p>
         * This method clears any previously added dimensions and sets the execution
         * scope to the provided collection.
         *
         * @param dimensions The new set of dimensions where this incident can be executed
         * @return This builder instance
         */
        public Builder<T, S> setDimensions(Collection<ResourceKey<Level>> dimensions) {
            this.dimensions.clear();
            this.dimensions.addAll(dimensions);
            return this;
        }

        /**
         * Sets this incident to search for targets across all dimensions.
         * <p>
         * When enabled, the target selector will consider all available dimensions.
         * Otherwise, only the dimension where the incident is currently executing
         * will be used as the target source.
         *
         * @return this builder instance for chaining
         */
        public Builder<T, S> runInAllDimensions() {
            this.runInAllDimensions = true;
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
         * @param multiExtractor The extractor function, or {@code null} to clear
         * @return This builder instance
         */
        public Builder<T, S> multiExtractor(@Nullable Function<S, Collection<T>> multiExtractor) {
            this.multiExtractor = multiExtractor;
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
            if (this.multiExtractor != null || this.singleExtractor != null) {
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
            if (this.multiExtractor != null || this.singleExtractor != null) {
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
            return new SimpleRegisteredTargetSelector<>(this.multiExtractor(), this.singleExtractor(), this.targetSelector(), this.dimensions());
        }

        public Incident<T> incident() {
            return this.incident;
        }

        public BiFunction<ServerLevel, RandomSource, S> targetSelector() {
            return this.targetSelector;
        }

        public Function<S, Collection<T>> multiExtractor() {
            return this.multiExtractor;
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

        public boolean isRunInAllDimensions() {
            return this.runInAllDimensions;
        }
    }
}
