package io.github.fengguoshuzhu.visionrealm.api.world.controller.entity.player;

import io.github.fengguoshuzhu.visionrealm.event.bus.game.GameBusEvents;
import io.github.fengguoshuzhu.visionrealm.mixin.world.event.entity.player.PlayerSanityControllerMixin;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Player Sanity Update Interface
 * <p>
 * Players implementing this interface will have a sanity value system that changes over time and with events.
 * Sanity affects player visuals, audio, and gameplay experience, triggering abnormal effects when too low.
 * </p>
 *
 * <h2>Invocation Mechanism:</h2>
 * <ul>
 *   <li>{@link #updateSanity()} - Called during {@link EntityTickEvent.Pre} event</li>
 *   <li>{@link #onHurtUpdateSanity(DamageSource, float)} - Called during {@link LivingDamageEvent.Post} event</li>
 * </ul>
 *
 * <h2>Implementation:</h2>
 * <p>
 * Default implementations are provided by {@code PlayerSanityControllerMixin} through Mixin injection into all Players.
 * Override these methods for custom behavior.
 * </p>
 *
 * @see PlayerSanityControllerMixin
 *
 * @author JiXingDeFeng
 * @since 1.0.0
 * @version 1.0.0
 */
public interface PlayerSanityController {

    /**
     * Regular sanity update method.
     * <p>
     * This method is called during {@link EntityTickEvent.Pre} event, once per tick,
     * handling natural changes in sanity value.
     * </p>
     *
     * <p><b>Default behavior (provided by {@code PlayerSanityMixin}):</b></p>
     * <ul>
     *   <li>Continuously decreases sanity when on fire</li>
     *   <li>Periodically recovers sanity in safe environments</li>
     *   <li>Uses {@link #getSanityRecoveryInterval()} to control recovery frequency</li>
     * </ul>
     *
     * @see GameBusEvents#onPlayerTick(PlayerTickEvent.Pre)
     * @see PlayerSanityControllerMixin#updateSanity()
     * @since 1.0.0
     */
    default void updateSanity() {
    }

    /**
     * Sanity update method called when taking damage.
     * <p>
     * This method is called during {@link LivingDamageEvent.Post} event,
     * handling the impact of damage events on sanity value.
     * </p>
     *
     * <p><b>Default behavior (provided by {@code PlayerSanityMixin}):</b></p>
     * <ul>
     *   <li>Lightning damage: Directly deducts significant sanity (90 points)</li>
     *   <li>Void damage: Deducts 50 sanity points</li>
     *   <li>Drowning damage: Deducts half of the damage value</li>
     *   <li>Other damage: Deducts 1/4 of damage value (minimum 1 point)</li>
     * </ul>
     *
     * @param source The damage source
     * @param amount The damage amount
     * @see GameBusEvents#onEntityHurt(LivingDamageEvent.Post)
     * @see PlayerSanityControllerMixin#onHurtUpdateSanity(DamageSource, float)
     * @since 1.0.0
     */
    default void onHurtUpdateSanity(DamageSource source, float amount) {
    }

    /**
     * Gets the time interval for sanity recovery.
     * <p>
     * This method controls the frequency of natural sanity recovery.
     * When a player is in a safe environment, sanity recovers once
     * every interval returned by this method.
     * </p>
     *
     * @return The recovery interval in ticks (default 200 ticks = 10 seconds)
     * @see #updateSanity()
     * @since 1.0.0
     */
    default int getSanityRecoveryInterval() {
        return 500;
    }

    /**
     * Gets the current value of the sanity recovery timer.
     * <p>
     * Returns the remaining ticks to wait until the next natural sanity recovery.
     * When this value is less than or equal to 0, the next sanity recovery will trigger.
     * This value decreases each game tick (typically handled by an event processor).
     *
     * @return The remaining ticks to wait, 0 or negative means recovery can trigger immediately
     *
     * @see #setSanityRecoveryTimer(int) Set the timer value
     * @see #getSanityRecoveryInterval() Get the recovery interval
     */
    default int getSanityRecoveryTimer() {
        return 0;
    }

    /**
     * Sets the current value of the sanity recovery timer.
     * <p>
     * Manually sets the remaining ticks to wait until the next natural sanity recovery.
     * Typically used for:
     * <ul>
     *   <li>Initializing the timer (set to recovery interval)</li>
     *   <li>Resetting the timer (after recovery triggers)</li>
     *   <li>External event influences (using items, taking damage, etc.)</li>
     * </ul>
     *
     * @param tick The remaining ticks to set, positive values recommended (negative means trigger immediately)
     *
     * @see #getSanityRecoveryTimer() Get the current timer value
     * @see #getSanityRecoveryInterval() Get the recovery interval
     */
    default void setSanityRecoveryTimer(int tick) {
    }

    /**
     * Gets the natural recovery rate of sanity value.
     * <p>
     * When the entity is in safe areas and meets the natural recovery conditions,
     * the sanity value will naturally increase at this rate per tick.
     * This value represents the amount increased per tick, default is 0.001 (0.1% per tick).
     * It takes approximately 1000 ticks (50 seconds) to fully recover from minimum (0) to maximum (1.0).
     * <p>
     * Note: This recovery rate applies to normal situations. Special areas or states may affect the actual recovery rate.
     *
     * @return The amount of sanity value naturally increased per tick, typically between 0.0 and 1.0
     *
     * @see #canBeRecoveryNaturally() Check if natural recovery is allowed
     * @see #getSanityRecoveryInterval() Get the recovery interval time
     */
    default double naturallyRecoveryValue() {
        return 1;
    }

    /**
     * Determines whether the sanity value can naturally recover.
     * <p>
     * Controls whether the entity can naturally recover sanity value in safe areas.
     * Returns true if the entity can gradually recover sanity in safe areas,
     * returns false if sanity value cannot naturally recover (may remain unchanged or require other methods to recover).
     * <p>
     * Can return different values based on the following conditions:
     * <ul>
     *   <li>CanBeErodedEntity type (player, animal, monster, etc.)</li>
     *   <li>CanBeErodedEntity state (well-lit area, dark area, etc.)</li>
     *   <li>Game difficulty (easy, normal, hard)</li>
     *   <li>Special effects (e.g., drank potion, eaten special food, etc.)</li>
     *   <li>Equipment (e.g., wearing sanity-restoring armor)</li>
     *   <li>Time of day (day/night cycle)</li>
     *   <li>Nearby blocks (torches, campfires, etc.)</li>
     * </ul>
     *
     * @return true if natural recovery is allowed, false otherwise
     *
     * @see #naturallyRecoveryValue() Get the natural recovery rate
     */
    default boolean canBeRecoveryNaturally() {
        return true;
    }

    default void sanityReduce(float amount) {
    }
}
