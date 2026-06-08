package io.github.jixingdefeng.visionrealm.api.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Extension interface for {@link LivingEntity} to add custom behavior.
 * <p>
 * This interface provides additional hooks for controlling entity death
 * animations, sound effects, and other behaviors without requiring
 * complex inheritance hierarchies.
 * </p>
 *
 * @author JiXingDeFeng
 * @see LivingEntity
 * @since 0.0.1-dev
 */
public interface LivingEntityExtensions {

    /**
     * Determines whether the entity should play its death sound effect.
     * <p>
     * This method controls the death sound playback when the entity dies.
     * If {@code false} is returned, the entity will not emit any death sound,
     * which can be useful for:
     * <ul>
     *   <li>Silent deaths (e.g., magic, void damage)</li>
     *   <li>Instant vanish deaths</li>
     *   <li>Custom death effects that handle sounds separately</li>
     * </ul>
     * </p>
     *
     * @return {@code true} to play the death sound (default),
     *         {@code false} to suppress the death sound
     *
     * @see LivingEntity#die(DamageSource)
     * @see LivingEntity#getDeathSound()
     */
    default boolean canMakeDeathSound() {
        return true;
    }

    /**
     * Determines whether the entity should play its hurt sound effect.
     * <p>
     * This method controls the hurt sound playback when the entity takes damage.
     * Returning {@code false} can be useful for:
     * <ul>
     *   <li>Silent damage effects (e.g., environmental damage)</li>
     *   <li>Entities that shouldn't express pain (e.g., mindless creatures)</li>
     *   <li>Custom damage handling that manages sounds separately</li>
     * </ul>
     * </p>
     * <p>
     * Note that this only affects the hurt sound, not other damage effects
     * like the red tint or knockback.
     * </p>
     *
     * @return {@code true} to play the hurt sound (default),
     *         {@code false} to suppress the hurt sound
     *
     * @see LivingEntity#hurt(DamageSource, float)
     * @see LivingEntity#getHurtSound(DamageSource)
     */
    default boolean canMakeHurtSound() {
        return true;
    }
}
