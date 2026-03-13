package io.github.fengguoshuzhu.visionrealm.event.bus.game.world.entity;

import io.github.fengguoshuzhu.visionrealm.api.controller.entity.EntityErosionController;
import net.minecraft.world.entity.LivingEntity;

public class LivingTickEvents {

    public static void updateAttribute(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            if (entity instanceof EntityErosionController updatable) {
                updatable.updateErosion();
            }
        }
    }
}
