package io.github.jixingdefeng.visionrealm.api.entity;

import net.minecraft.world.entity.LivingEntity;

/**
 * Extension interface for {@link LivingEntity} to add custom behavior.
 *
 * @author JiXingDeFeng
 * @see LivingEntity
 * @since 0.1.0
 */
public interface LivingEntityExtensions {

    default void setProtectionTime(int time) {
    }

    default int getProtectionTime() {
        return 0;
    }
}
