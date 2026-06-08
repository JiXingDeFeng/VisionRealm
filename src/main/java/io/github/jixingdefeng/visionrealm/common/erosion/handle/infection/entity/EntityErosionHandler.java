package io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.entity;

import io.github.jixingdefeng.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.entity.CanBeErosionEntity;
import io.github.jixingdefeng.visionrealm.api.event.erosion.entity.EntityErosionEvent;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.BaseErosionHandle;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public class EntityErosionHandler {

    public static <T extends Entity, RT, R> boolean tryErosion(CanBeErosionEntity<T, RT, R> key, Level level, Vec3 pos, ErosionType type) {
        return process(key.getSource(), pos, level, type, source -> key);
    }

    private static <T extends Entity, RT, R> boolean process(
            T entity,
            Vec3 pos,
            Level level,
            ErosionType type,
            BaseErosionHandle.ErosionWrapper<T, RT, R, CanBeErosion<T, RT, R>> wrapper
    ) {
        if (wrapper.getErosion(entity).canBeEroded(level, pos, type)) {
            return BaseErosionHandle.process(entity, level, pos, type, wrapper, new BaseErosionHandle.EventSender<>() {

                @Override
                public BaseErosionHandle.PreEventResult<RT> sendPreEvent(T source, RT target, Level level, Vec3 pos, ErosionType type) {
                    EntityErosionEvent.Pre<RT> event = new EntityErosionEvent.Pre<>(source, target, level, pos, type);
                    NeoForge.EVENT_BUS.post(event);
                    return BaseErosionHandle.PreEventResult.of(event.getTarget(), !event.isCanceled());
                }

                @Override
                public void sendPostEvent(T source, R result, RT target, Level level, Vec3 pos, ErosionType type, boolean success) {
                    NeoForge.EVENT_BUS.post(new EntityErosionEvent.Post<>(source, result, target, level, pos, type, success));
                }
            });
        } else {
            return false;
        }
    }
}
