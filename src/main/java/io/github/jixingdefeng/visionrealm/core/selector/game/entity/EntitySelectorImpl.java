package io.github.jixingdefeng.visionrealm.core.selector.game.entity;

import com.google.common.collect.Range;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.entity.EntitySelector;
import io.github.jixingdefeng.visionrealm.core.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.core.selector.game.BaseSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Default implementation of {@link EntitySelector} that retrieves entities
 * from a {@link Level} based on various filters, ranges, and limits.
 *
 * <p>This class is the backbone for entity-based target selection. It supports:
 * <ul>
 *   <li>Bounding box and spherical range queries</li>
 *   <li>Height range filtering</li>
 *   <li>Biome allow/deny lists</li>
 *   <li>Custom per-entity predicate chains</li>
 *   <li>Per-extraction customization via {@link TargetCustomizer}</li>
 *   <li>Snapshot-based execution for consistent batch selection</li>
 * </ul>
 *
 * <p><b>Extending:</b> Subclasses only need to override {@link #getEntities}
 * to adapt the entity fetching logic (e.g., for players or multi-type queries).
 * All other behavior is provided by this base class.
 *
 * @param <T> the entity type this selector targets
 * @author JiXingDeFeng
 * @since 0.1.0
 */
public class EntitySelectorImpl<T extends Entity> extends BaseSelector<T, EntitySelector<T>> implements EntitySelector<T> {
    protected final EntityType<T> type;

    public static PlayerSelector createPlayer(AbstractSelector<?, ?> source, @Nullable Level level) {
        return new PlayerSelector(source, level);
    }

    public static MultiTypeEntity createMultipleTypes(
            AbstractSelector<?, ?> source,
            @Nullable Level level,
            EntityType<?>... types
    ) {
        return new MultiTypeEntity(source, level, types);
    }

    public static AllEntity createAll(AbstractSelector<?, ?> source, @Nullable Level level) {
        return new AllEntity(source, level);
    }

    public EntitySelectorImpl(EntitySelectorImpl<T> source, @Nullable Level level) {
        super(source, level);
        this.type = source.type;
    }

    public EntitySelectorImpl(AbstractSelector<?, ?> source, EntityType<T> type, @Nullable Level level) {
        super(source, level);
        this.type = type;
    }

    @Override
    public EntitySelector<T> self() {
        return this;
    }

    @Override
    public EntitySelector<T> copy(@Nullable Level level) {
        return new EntitySelectorImpl<>(this, level);
    }

    @Override
    public void execute() {
        if (!this.isExecuted()) {
            super.execute();
            Level level = this.level;
            List<T> values = new ArrayList<>();
            while (!this.snapshot.isEmpty()) {
                EntitySnapshot<T> snapshot = this.poll();
                values.addAll(this.selectWithSnapshot(snapshot, level));
            }

            this.cachedResults = List.copyOf(values);
        }
    }

    @Override
    protected void push(TargetProvider<T> actuator) {
        EntitySnapshot<T> snapshot = EntitySnapshot.of(this, actuator);
        this.snapshot.add(snapshot);
    }

    @Override
    protected EntitySnapshot<T> poll() {
        return (EntitySnapshot<T>) this.snapshot.poll();
    }

    /**
     * Validates a single entity against all active filters.
     *
     * <p>Checks in order: height range, biome, spherical range (if set),
     * loaded chunk requirement (if requested), and finally the list of
     * custom predicates added via {@link #filter}.
     *
     * @param biomePredicate   biome predicate
     * @param predicate        additional entity predicates
     * @param entity           the entity being tested
     * @param sphereRange      spherical restriction (already offset by reference center if needed)
     * @param level            the current level
     * @param yRange            allowed height range
     * @param loadedOnly       if {@code true}, the entity's block position must be loaded
     * @return {@code true} if the entity passes all checks
     */
    protected boolean validate(
            @Nullable Predicate<? super ResourceKey<Biome>> biomePredicate,
            List<Predicate<? super T>> predicate,
            @Nullable SphereRange sphereRange,
            @NotNull T entity,
            Level level,
            Range<Double> yRange,
            boolean loadedOnly
    ) {
        BlockPos blockPos = entity.getOnPos();
        Vec3 position = entity.position();
        boolean valid = yRange.contains(position.y)
                && (biomePredicate == null || biomePredicate.test(level.getBiome(blockPos).getKey()));
        if (valid) {
            if (sphereRange != null) {
                valid = entity.distanceToSqr(sphereRange.center()) <= sphereRange.radius();
            }

            if (loadedOnly) {
                valid = level.isLoaded(blockPos);
            }

            for (Predicate<? super T> pred : predicate) {
                if (!valid) break;
                valid = pred.test(entity);
            }
        }

        return valid;
    }

    @Nullable
    protected AABB getBoxRange(SphereRange sphereRange) {
        if (sphereRange == null) {
            return null;
        } else {
            Vec3 center = sphereRange.center();
            double radius = sphereRange.radius();

            return new AABB(
                    center.x + radius, center.y + radius, center.z + radius,
                    center.x - radius, center.y - radius, center.z - radius
            );
        }
    }

    /**
     * Resolves the final search bounding box by prioritizing a stored
     * box (from {@link #offset}) over a dynamically computed
     * one from {@link #getBoxRange}.
     *
     * @param sphereRange    sphere configuration
     * @param boxRange       an optional pre-defined box (may be offset later)
     * @return the effective bounding box to use for the query
     */
    @Nullable
    protected AABB getRange(SphereRange sphereRange, AABB boxRange) {
        if (boxRange != null) {
            return boxRange;
        } else if (sphereRange != null) {
            return this.getBoxRange(sphereRange);
        } else {
            return null;
        }
    }

    /**
     * Core method that executes one snapshot of entity selection.
     * <p>
     * This method:
     * <ol>
     *   <li>Determines the random source (provider &gt; snapshot &gt; level fallback)</li>
     *   <li>Computes the search bounding box via {@link #getRange}, after applying
     *       reference‑center offsets to the snapshot's sphere and box ranges</li>
     *   <li>Fetches candidate entities using {@link #getEntities} (with a
     *       candidate limit from the customizer), filtering them through
     *       {@link #validate}</li>
     *   <li>Trims the candidates to at most {@link #limit} results (if &gt; 0),
     *       then applies {@link TargetCustomizer#postProcess post‑processing}
     *       and returns the final list</li>
     * </ol>
     * If no valid search region exists, logs an error and returns the
     * fallback from {@link TargetCustomizer#onFallback}.
     *
     * @param snapshot the current snapshot configuration (spatial constraints
     *                 should already be offset for any reference center)
     * @param level    the level to search in (typically a {@code ServerLevel})
     * @return the list of selected targets (never {@code null}, may be empty)
     */
    protected List<T> selectWithSnapshot(
            EntitySnapshot<T> snapshot,
            Level level
    ) {
        TargetProvider<T> provider = snapshot.provider;
        RandomSource random = level.random;
        if (provider.random() != null) {
            random = provider.random();
        } else if (snapshot.random != null) {
            random = snapshot.random;
        }

        TargetCustomizer<T, Level, RandomSource> customizer = provider.customizer();
        Vec3 referenceCenter = snapshot.referenceCenter;
        SphereRange sphereRange = this.offset(referenceCenter, snapshot.sphereRange);
        AABB box = this.offset(referenceCenter, snapshot.boxRange);
        AABB boxRange = this.getRange(sphereRange, box);
        if (boxRange != null) {
            int number = provider.single() ? 1 : customizer.getExtractionCount(level, random);
            List<T> values = this.getEntities(
                    entity -> this.validate(
                            snapshot.biomeFilter, snapshot.filters, sphereRange, entity, level, snapshot.heightRange, snapshot.loadedOnly
                    ), level, boxRange, number >= 0 ? number : Integer.MAX_VALUE
            );
            List<T> results = new ArrayList<>();
            for (T entity : values) {
                if (this.limit <= 0 || results.size() < this.limit) {
                    results.add(entity);
                }
            }

            return customizer.postProcess(results, level, random);
        } else {
            VisionRealm.LOGGER.error("No position range specified for entity selection. Unable to query entities.");
            return customizer.onFallback(level, random);
        }
    }

    /**
     * Retrieves a list of entities from the level that match the given
     * predicate and bounding box, up to the specified maximum.
     * <p>
     * Subclasses override this to customize how entities are fetched
     * (e.g., using {@code level.getPlayers} for player selection, or
     * using a custom {@link EntityTypeTest} for multi-type selection).
     *
     * @param predicate  the combined filter predicate
     * @param level      the server level
     * @param aabb       the bounding box to search in
     * @param maxResults the maximum number of entities to return
     * @return a mutable list of matching entities
     */
    protected List<T> getEntities(Predicate<T> predicate, Level level, AABB aabb, int maxResults) {
        List<T> entities = new ArrayList<>();
        level.getEntities(this.type, aabb, predicate, entities, maxResults);
        return entities;
    }

    /**
     * Selector specialized for players.
     * <p>
     * Overrides {@link #getEntities} to use {@link ServerLevel#getPlayers},
     * which is more efficient and respects player‑specific visibility rules.
     * On the client side, this returns an empty list because player queries
     * are only meaningful on the server.
     *
     * @since 0.0.2‑dev
     */
    public static class PlayerSelector extends EntitySelectorImpl<Player> {

        public PlayerSelector(AbstractSelector<?, ?> source, @Nullable Level level) {
            super(source, EntityType.PLAYER, level);
        }

        public PlayerSelector(PlayerSelector source, @Nullable Level level) {
            super(source, level);
        }

        @Override


        protected List<Player> getEntities(Predicate<Player> predicate, Level level, AABB aabb, int maxResults) {
            if (level instanceof ServerLevel serverLevel) {
                return new ArrayList<>(
                        serverLevel.getPlayers(serverPlayer -> aabb.contains(serverPlayer.position())
                                && predicate.test(serverPlayer), maxResults)
                );
            } else {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Selector that matches all entity types.
     * <p>
     * Uses a universal {@link EntityTypeTest} that accepts every entity.
     * Useful when filtering is intended to be done solely through
     * predicates rather than by entity type.
     *
     * @since 0.1.0
     */
    public static class AllEntity extends EntitySelectorImpl<Entity> {
        protected static final EntityTypeTest<Entity, Entity> ALL_ENTITY = new EntityTypeTest<>() {
            @Override
            public Entity tryCast(@NotNull Entity entity) {
                return entity;
            }

            @NotNull
            @Override
            public Class<? extends Entity> getBaseClass() {
                return Entity.class;
            }
        };

        public AllEntity(AbstractSelector<?, ?> source, @Nullable Level level) {
            super(source, null, level);
        }

        public AllEntity(AllEntity source, @Nullable Level level) {
            super(source, level);
        }

        @Override
        protected List<Entity> getEntities(Predicate<Entity> predicate, Level level, AABB aabb, int maxResults) {
            List<Entity> entities = new ArrayList<>();
            level.getEntities(ALL_ENTITY, aabb, predicate, entities, maxResults);
            return entities;
        }
    }

    /**
     * Selector that matches entities of several specific types.
     * <p>
     * The allowed types are fixed at construction time and stored in an
     * immutable set. Internally, a custom {@link EntityTypeTest} is used
     * to perform the type check.
     *
     * @since 0.1.0
     */
    public static class MultiTypeEntity extends EntitySelectorImpl<Entity> {
        protected final MultipleTypes multipleTypes;

        public MultiTypeEntity(AbstractSelector<?, ?> source, @Nullable Level level, EntityType<?>... types) {
            super(source, null, level);
            this.multipleTypes = new MultipleTypes(Set.of(types));
        }

        public MultiTypeEntity(MultiTypeEntity source, @Nullable Level level) {
            super(source, level);
            this.multipleTypes = new MultipleTypes(source.multipleTypes.types());
        }

        protected record MultipleTypes(Set<EntityType<?>> types) implements EntityTypeTest<Entity, Entity> {

            @Override
            @Nullable
            public Entity tryCast(@NotNull Entity entity) {
                return this.types.contains(entity.getType()) ? entity : null;
            }

            @Override
            @NotNull
            public Class<? extends Entity> getBaseClass() {
                return Entity.class;
            }
        }

        @Override
        protected List<Entity> getEntities(Predicate<Entity> predicate, Level level, AABB aabb, int maxResults) {
            List<Entity> entities = new ArrayList<>();
            level.getEntities(this.multipleTypes, aabb, predicate, entities, maxResults);
            return entities;
        }
    }

    protected static class EntitySnapshot<T extends Entity> extends Snapshot<T> {
        public EntitySnapshot(
                @Nullable Vec3 referenceCenter,
                @Nullable RandomSource random,
                @Nullable AABB boxRange,
                @Nullable SphereRange sphereRange,
                @Nullable Predicate<? super ResourceKey<Biome>> biomeFilter,
                Range<Double> heightRange,
                boolean loadedOnly,
                List<Predicate<? super T>> filters,
                TargetProvider<T> provider
        ) {
            super(referenceCenter, random, boxRange, sphereRange, biomeFilter, heightRange, loadedOnly, filters, provider);
        }

        public static <T extends Entity> EntitySnapshot<T> of(
                EntitySelectorImpl<T> selector,
                TargetProvider<T> provider
        ) {
            return new EntitySnapshot<>(
                    selector.referenceCenter,
                    selector.random,
                    selector.boxRange,
                    selector.sphereRange,
                    selector.biomeFilter,
                    selector.heightRange,
                    selector.loadedOnly,
                    selector.filters,
                    provider
            );
        }
    }
}
