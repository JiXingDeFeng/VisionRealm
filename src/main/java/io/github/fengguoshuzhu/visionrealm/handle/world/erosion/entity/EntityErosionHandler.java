package io.github.fengguoshuzhu.visionrealm.handle.world.erosion.entity;

import io.github.fengguoshuzhu.visionrealm.api.event.world.entity.EntityErosionEvent;
import io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection.CanBeErosion;
import io.github.fengguoshuzhu.visionrealm.handle.world.erosion.BaseErosionHandle;
import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public class EntityErosionHandler {

    @SuppressWarnings("unchecked")
    public static boolean tryErosion(Entity entity, Level level, Vec3 pos, ErosionType type) {
        return process(entity, level, pos, type, source -> {
            if (source instanceof CanBeErosion) {
                return (CanBeErosion<Entity, ?, ?>) source;
            } else {
                return null;
            }
        });
    }

    private static <T, R> boolean process(
            Entity entity,
            Level level,
            Vec3 pos,
            ErosionType type,
            BaseErosionHandle.ErosionWrapper<Entity, R, T> wrapper
    ) {
        if (wrapper.getErosion(entity).canBeEroded(type)) {
            return BaseErosionHandle.process(entity, level, pos, type, wrapper, new BaseErosionHandle.EventSender<>() {

                @Override
                public BaseErosionHandle.PreEventResult<T> sendPreEvent(Entity source, T target, Level level, Vec3 pos, ErosionType type) {
                    EntityErosionEvent.Pre<T> event = new EntityErosionEvent.Pre<>(source, target, level, pos, type);
                    NeoForge.EVENT_BUS.post(event);
                    return BaseErosionHandle.PreEventResult.of(event.getTarget(), !event.isCanceled());
                }

                @Override
                public void sendPostEvent(Entity source, R result, T target, Level level, Vec3 pos, ErosionType type, boolean success) {
                    NeoForge.EVENT_BUS.post(new EntityErosionEvent.Post<>(source, result, target, level, pos, type, success));
                }
            });
        } else {
            return false;
        }
    }
}
