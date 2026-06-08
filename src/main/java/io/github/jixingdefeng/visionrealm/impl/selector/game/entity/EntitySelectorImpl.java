package io.github.jixingdefeng.visionrealm.impl.selector.game.entity;

import com.google.common.collect.Range;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetCustomizer;
import io.github.jixingdefeng.visionrealm.api.selector.game.entity.EntitySelector;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.impl.selector.game.AbstractSelector;
import io.github.jixingdefeng.visionrealm.impl.selector.game.BaseSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

public class EntitySelectorImpl<T extends Entity> extends BaseSelector<T, EntitySelector<T>> implements EntitySelector<T> {
    protected final EntityType<T> type;

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
    protected void push(TargetProvider<T> actuator) {
        EntitySnapshot<T> snapshot = EntitySnapshot.of(this, actuator);
        this.snapshotDeque.add(snapshot);
    }

    @Override
    protected EntitySnapshot<T> pop() {
        return (EntitySnapshot<T>) this.snapshotDeque.pop();
    }

    @Override
    protected EntitySelector<T> self() {
        return this;
    }

    @Override
    public EntitySelector<T> copy(@Nullable Level level) {
        return new EntitySelectorImpl<>(this, level);
    }

    @Override
    protected void execute() {
        if (this.level instanceof ServerLevel serverLevel) {
            if (!this.isExecuted()) {
                super.execute();
                while (!this.snapshotDeque.isEmpty()) {
                    EntitySnapshot<T> snapshot = this.pop();
                    List<Biome> biomeList = this.getAllowBiomes(serverLevel, snapshot);
                    List<T> values = this.selectWithSnapshot(entity -> true, biomeList::contains, snapshot, serverLevel);
                    this.cachedResults.addAll(values);
                }
            }
        }
    }

    protected boolean validate(
            Predicate<? super Biome> filter,
            Queue<Predicate<? super T>> predicate,
            @NotNull T entity,
            @Nullable Vec3 referenceCenter,
            @Nullable SphereRange sphereRange,
            Level level,
            Range<Double> range,
            boolean loadedOnly
    ) {
        BlockPos blockPos = entity.getOnPos();
        Holder<Biome> biome = level.getBiome(blockPos);
        Vec3 position = entity.position();
        boolean valid = range.contains(position.y) && filter.test(biome.value());
        if (valid) {
            if (sphereRange != null) {
                valid = entity.distanceToSqr(sphereRange.getCenter(referenceCenter)) <= sphereRange.radius();
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

    protected <L extends Level, R extends RandomSource> List<T> filterAndPostProcess(
            Predicate<? super T> filter,
            TargetCustomizer<T, L, R> customizer,
            List<T> targets,
            L level,
            R random,
            int limit
    ) {
        List<T> values = customizer.postProcess(targets, level, random);
        List<T> results = new ArrayList<>();
        for (T entity : values) {
            if ((limit <= 0 || results.size() < limit) && filter.test(entity)) {
                results.add(entity);
            }
        }

        return results;
    }

    @Nullable
    protected AABB getRange(Vec3 referenceCenter, SphereRange sphereRange, RandomSource random) {
        Vec3 center;
        double radius;
        if (sphereRange != null) {
            center = sphereRange.getCenter(referenceCenter);
            radius = sphereRange.radius();
        } else if (referenceCenter != null) {
            center = referenceCenter;
            radius = random.nextInt(128);
        } else {
            return null;
        }

        return new AABB(
                center.x + radius, center.y + radius, center.z + radius,
                center.x - radius, center.y - radius, center.z - radius
        );
    }

    protected List<T> selectWithSnapshot(
            Predicate<? super T> predicate,
            Predicate<? super Biome> filter,
            EntitySnapshot<T> snapshot,
            ServerLevel level
    ) {
        TargetProvider<T> provider = snapshot.provider;
        RandomSource random = level.random;
        if (provider.random() != null) {
            random = provider.random();
        } else if (snapshot.random != null) {
            random = snapshot.random;
        }

        Vec3 referenceCenter = snapshot.referenceCenter;
        SphereRange sphereRange = snapshot.sphereRange;
        AABB boxRange = this.getOffsetResult(referenceCenter, snapshot.boxRange);
        boxRange = boxRange == null ? this.getRange(referenceCenter, sphereRange, random) : boxRange;
        if (boxRange != null) {
            TargetCustomizer<T, Level, RandomSource> customizer = provider.customizer();
            int limit = provider.single() ? 1 : customizer.getExtractionCount(level, random);
            List<T> valueList = this.getEntities(
                    entity -> predicate.test(entity)
                            && this.validate(
                            filter, snapshot.filters, entity, referenceCenter, sphereRange, level, snapshot.heightRange, snapshot.loadedOnly
                    ), level, boxRange, limit
            );
            return this.filterAndPostProcess(target -> true, customizer, valueList, level, random, snapshot.limit);
        } else {
            VisionRealm.LOGGER.warn("No position range specified for entity selection. Unable to query entities.", new RuntimeException());
            return List.of();
        }
    }

    protected List<T> getEntities(Predicate<T> predicate, ServerLevel level, AABB aabb, int maxResults) {
        List<T> entities = new ArrayList<>();
        level.getEntities(this.type, aabb, predicate, entities, maxResults);
        return entities;
    }

    public static class PlayerSelector extends EntitySelectorImpl<Player> {

        public PlayerSelector(AbstractSelector<?, ?> source, EntityType<Player> type, @Nullable Level level) {
            super(source, type, level);
        }

        public PlayerSelector(AbstractSelector<?, ?> source, @Nullable Level level) {
            this(source, EntityType.PLAYER, level);
        }

        public PlayerSelector(PlayerSelector source, @Nullable Level level) {
            super(source, level);
        }

        @Override
        protected List<Player> getEntities(Predicate<Player> predicate, ServerLevel level, AABB aabb, int maxResults) {
            return new ArrayList<>(
                    level.getPlayers(serverPlayer -> aabb.contains(serverPlayer.position()) && predicate.test(serverPlayer), maxResults)
            );
        }
    }

    public static class AllEntity extends EntitySelectorImpl<Entity> {
        protected static final EntityTypeTest<Entity, Entity> allEntity = new EntityTypeTest<>() {
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
        protected List<Entity> getEntities(Predicate<Entity> predicate, ServerLevel level, AABB aabb, int maxResults) {
            List<Entity> entities = new ArrayList<>();
            level.getEntities(allEntity, aabb, predicate, entities, maxResults);
            return entities;
        }
    }

    protected static class EntitySnapshot<T extends Entity> extends Snapshot<T> {
        public EntitySnapshot(
                @Nullable Vec3 referenceCenter,
                @Nullable RandomSource random,
                @Nullable AABB boxRange,
                @Nullable SphereRange sphereRange,
                Range<Double> heightRange,
                int limit,
                boolean loadedOnly,
                Collection<ResourceKey<? extends Biome>> allowBiomes,
                Collection<ResourceKey<? extends Biome>> denyBiomes,
                Queue<Predicate<? super T>> filters,
                TargetProvider<T> provider
        ) {
            super(referenceCenter, random, boxRange, sphereRange, heightRange, limit, loadedOnly, allowBiomes, denyBiomes, filters, provider);
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
                    selector.heightRange,
                    selector.limit,
                    selector.loadedOnly,
                    selector.allowBiomes,
                    selector.denyBiomes,
                    selector.filters,
                    provider
            );
        }
    }
}
