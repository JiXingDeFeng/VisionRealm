package io.github.jixingdefeng.visionrealm.event.bus.game.world.entity;

import io.github.jixingdefeng.visionrealm.api.controller.entity.EntityErosionController;
import net.minecraft.world.entity.LivingEntity;

public class LivingTickEvents {

    public static void updateAttribute(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            ((EntityErosionController) entity).updateErosion();
        }
    }
}
