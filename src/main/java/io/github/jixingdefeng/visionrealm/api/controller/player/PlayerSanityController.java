package io.github.jixingdefeng.visionrealm.api.controller.player;

import io.github.jixingdefeng.visionrealm.mixin.world.entity.player.PlayerSanityControllerMixin;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

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
 *   <li>{@link #sanityHurtUpdate(DamageSource, float)} - Called during {@link LivingDamageEvent.Post} event</li>
 * </ul>
 *
 * <h2>Implementation:</h2>
 * <p>
 * Default implementations are provided by {@code PlayerSanityControllerMixin} through Mixin injection into all Players.
 * Override these methods for custom behavior.
 * </p>
 *
 * @author JiXingDeFeng
 * @see PlayerSanityControllerMixin
 * @since 0.0.1-dev
 */
public interface PlayerSanityController {

    /**
     * Updates the sanity value, called once per game tick.
     * <p>
     * This method is called during the player's tick and handles natural changes
     * to the sanity value. Depending on the current environment state and player
     * conditions, the sanity value either recovers or decreases:
     * <ul>
     *   <li>If {@link #isSanityContinuouslyDraining(Holder)} returns {@code true},
     *       sanity decreases by {@link #getSanityDrainRate()} every
     *       {@link #getSanityDrainInterval(Holder)} ticks</li>
     *   <li>If the player is in a safe environment and {@link #canSanityRecoverNaturally(Holder)}
     *       returns {@code true}, sanity recovers by {@link #getSanityRecoveryAmount()}
     *       every {@link #getSanityRecoveryInterval(Holder)} ticks</li>
     * </ul>
     * When the sanity value changes, the relevant timers are reset.
     */
    default void updateSanity() {
    }

    /**
     * Updates sanity when the player takes damage.
     * <p>
     * This method is called automatically when the player is hurt, allowing the
     * sanity system to react to damage events. The default behavior (provided by
     * {@code PlayerSanityControllerMixin}) reduces the player's sanity based on
     * the damage source, the damage amount, and the loss percentage returned by
     * {@link #getHurtLossPercentage(DamageSource, float)}.
     * </p>
     * <p>
     * The default implementation applies the following rules:
     * <ul>
     *   <li>If the damage source entity is a {@code Monster}, the effective damage
     *       is reduced to 25% before sanity loss calculation</li>
     *   <li>Lightning damage: immediately reduces sanity by 90 points</li>
     *   <li>Void damage (out of world): reduces sanity by 50 points</li>
     *   <li>Drowning damage: reduces sanity by half of the effective damage,
     *       with a minimum loss of 1 point</li>
     *   <li>Other damage types: reduces sanity by the effective damage multiplied
     *       by the percentage returned by {@link #getHurtLossPercentage(DamageSource, float)},
     *       with a minimum loss of 1 point</li>
     * </ul>
     * </p>
     * <p>
     * The timer for natural sanity changes is reset whenever this method is invoked.
     * </p>
     *
     * @param source the damage source
     * @param amount the raw damage amount
     * @see #getHurtLossPercentage(DamageSource, float)
     */
    default void sanityHurtUpdate(DamageSource source, float amount) {
    }

    /**
     * The percentage of sanity lost when taking damage.
     * <p>
     * When the player takes damage, the sanity value decreases by this percentage
     * of the original damage amount.
     * <p>
     * For example, if this method returns 0.25 and the player takes 10 damage,
     * sanity decreases by 2.5 points.
     * The default value is 0.25F (no sanity loss on hurt).
     *
     * @param source the damage source
     * @param amount the original damage amount
     * @return the loss percentage as a float (e.g., 0.25 for 25%)
     */
    default float getHurtLossPercentage(DamageSource source, float amount) {
        return 0.25F;
    }

    /**
     * Whether sanity can naturally change (recover or drain) in the current environment.
     * <p>
     * This method determines if the current environment or player state allows
     * natural sanity changes. If {@code false}, {@link #updateSanity()} will not
     * execute any automatic sanity modification logic.
     * <p>
     * Unlike erosion, sanity changes are primarily driven by player state
     * rather than environment. The default implementation returns {@code true}.
     *
     * @return {@code true} if natural sanity changes are allowed, {@code false} otherwise
     */
    default boolean canSanityChange() {
        return true;
    }

    /**
     * Whether sanity can naturally recover in the current environment.
     * <p>
     * This method determines if the current environment allows natural sanity recovery.
     * If {@code false}, {@link #updateSanity()} will not execute the recovery logic.
     * <p>
     * Typically returns {@code true} in safe environments (well-lit areas, daytime,
     * near torches, etc.) and {@code false} in dangerous environments.
     * The default implementation returns {@code true}.
     *
     * @param biome the biome the player is currently in
     * @return {@code true} if natural recovery is allowed, {@code false} otherwise
     */
    default boolean canSanityRecoverNaturally(Holder<Biome> biome) {
        return true;
    }

    /**
     * The interval between natural sanity recovery events.
     * <p>
     * When the player is in a safe environment and {@link #canSanityRecoverNaturally(Holder)}
     * returns {@code true}, sanity increases by {@link #getSanityRecoveryAmount()}
     * every time this interval elapses.
     * <p>
     * This method may return different values based on the current biome
     * or game difficulty. The default value is 500 ticks (25 seconds).
     *
     * @param biome the biome the player is currently in
     * @return the interval in ticks between recovery events
     */
    default int getSanityRecoveryInterval(Holder<Biome> biome) {
        return 500;
    }

    /**
     * The amount of sanity recovered per natural recovery event.
     * <p>
     * When {@link #canSanityRecoverNaturally(Holder)} returns {@code true} and the recovery
     * interval has elapsed, the sanity value increases by the amount returned
     * by this method.
     * The default value is 1.
     *
     * @return the recovery amount per recovery event
     */
    default double getSanityRecoveryAmount() {
        return 1;
    }

    /**
     * Whether sanity is continuously decreasing in the current environment.
     * <p>
     * This method determines if the current environment or player state causes
     * sanity to continuously drain over time. For example, being on fire or
     * standing in darkness may trigger continuous decrease.
     * <p>
     * When this method returns {@code true}, {@link #updateSanity()} will
     * decrease sanity by {@link #getSanityDrainRate()} every
     * {@link #getSanityDrainInterval(Holder)} ticks.
     * The default implementation returns {@code false}.
     *
     * @param biome the biome the player is currently in
     * @return {@code true} if sanity is continuously decreasing, {@code false} otherwise
     */
    default boolean isSanityContinuouslyDraining(Holder<Biome> biome) {
        return false;
    }

    /**
     * The interval between sanity drain events.
     * <p>
     * When {@link #isSanityContinuouslyDraining(Holder)} returns {@code true},
     * sanity decreases by {@link #getSanityDrainRate()} every time this interval elapses.
     * <p>
     * This method may return different values based on the current biome.
     * The default value is 5 ticks (0.25 second).
     *
     * @param biome the biome the player is currently in
     * @return the interval in ticks between drain events
     */
    default int getSanityDrainInterval(Holder<Biome> biome) {
        return 5;
    }

    /**
     * The amount of sanity decreased per drain event.
     * <p>
     * When {@link #isSanityContinuouslyDraining(Holder)} returns {@code true},
     * the sanity value decreases by the amount returned by this method {@link #getSanityDrainInterval(Holder)}.
     * The default value is 0.5.
     *
     * @return the decrease amount per drain event
     */
    default double getSanityDrainRate() {
        return 0.5;
    }
}
