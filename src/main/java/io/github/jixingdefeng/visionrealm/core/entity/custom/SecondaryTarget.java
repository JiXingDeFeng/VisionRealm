package io.github.jixingdefeng.visionrealm.core.entity.custom;

import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public interface SecondaryTarget {

    Map<UUID, AngerType> getSecondaryTargets();

    enum AngerType {
        RENAME,
        ATTACK
    }

    default void removeAll(List<UUID> toRemove) {
        if (toRemove != null && !toRemove.isEmpty()) {
            toRemove.forEach(this.getSecondaryTargets()::remove);
        }
    }

    default void removeAlls(List<? extends Entity> toRemove) {
        if (toRemove != null && !toRemove.isEmpty()) {
            toRemove.forEach(entity -> this.getSecondaryTargets().remove(entity.getUUID()));
        }
    }

    default <T extends Entity> List<T> getTargetsByType(ServerLevel level, AngerType type, @NotNull Class<T> tClass) {
        return this.getTargetsByType(level, type, tClass, null);
    }

    default <T extends Entity> List<T> getTargetsByType(ServerLevel level, AngerType type, @NotNull Class<T> tClass, @Nullable Predicate<T> filter) {
        Map<UUID, AngerType> targets = this.getSecondaryTargets();
        if (targets.isEmpty()) return List.of();

        List<UUID> toBeCleared = Lists.newArrayList();
        List<T> values = Lists.newArrayList();
        for (Map.Entry<UUID, AngerType> entry : targets.entrySet()) {
            UUID uuid = entry.getKey();
            if (entry.getValue() == type) {
                Entity entity = level.getEntity(uuid);
                if (entity != null && entity.isAlive()) {
                    if (tClass.isInstance(entity)) {
                        T target = tClass.cast(entity);
                        if (filter == null || filter.test(target)) {
                            values.add(target);
                        }
                    }
                } else {
                    toBeCleared.add(uuid);
                }
            }
        }

        this.removeAll(toBeCleared);
        return values;
    }
}
