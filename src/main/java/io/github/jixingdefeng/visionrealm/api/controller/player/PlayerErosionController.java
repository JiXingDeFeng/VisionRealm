package io.github.jixingdefeng.visionrealm.api.controller.player;

import io.github.jixingdefeng.visionrealm.api.entity.component.erosion.ErosionCauser;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.NightmareApostle;
import io.github.jixingdefeng.visionrealm.mixin.world.entity.player.PlayerErosionControllerMixin;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.biome.Biome;

/**
 * Player Erosion Update Interface
 * <p>
 * Players implementing this interface will have an erosion value system that changes over time and with events.
 * Erosion affects player visuals, audio, and gameplay experience, triggering abnormal effects when too high.
 * </p>
 *
 * <h2>Invocation Mechanism:</h2>
 * <ul>
 *   <li>{@link #updateErosion()} - Called during {@code Player.tick()} event</li>
 *   <li>{@link #erosionHurtUpdate(DamageSource, float)} - Called during {@code Player.hurt()} event</li>
 * </ul>
 *
 * <h2>Implementation:</h2>
 * <p>
 * Default implementations are provided by {@code PlayerErosionControllerMixin} through Mixin injection into all Players.
 * Override these methods for custom behavior.
 * </p>
 *
 * @author JiXingDeFeng
 * @see PlayerErosionControllerMixin
 * @since 0.0.3-dev
 */
public interface PlayerErosionController {

    /**
     * Updates the erosion value, called once per game tick.
     * <p>
     * This method is called during the player's tick and handles natural changes
     * to the erosion value. Depending on the current environment state, the erosion
     * value either increases or decreases:
     * <ul>
     *   <li>In erosion environments, the erosion value continuously increases</li>
     *   <li>In non-erosion environments, and if {@link #canErosionReduceNaturally(Holder)} returns
     *       {@code true}, a natural reduction is triggered every
     *       {@link #getErosionReduceInterval(Holder)} ticks, decreasing the erosion value
     *       by {@link #getErosionReduceAmount(Holder)}</li>
     * </ul>
     * When the erosion value changes, the relevant timers are reset.
     */
    default void updateErosion() {
    }

    /**
     * Updates erosion when the player takes damage.
     * <p>
     * This method is called when the player is hurt.
     * If the damage source entity implements the {@link ErosionCauser} interface,
     * the erosion value increases according to the calculation method and value
     * configured by that entity.
     * <p>
     * If the damage source entity does not implement this interface, this method
     * does nothing.
     *
     * @param source the damage source
     * @param amount the original damage amount
     */
    default void erosionHurtUpdate(DamageSource source, float amount) {
    }

    /**
     * Called when erosion reaches its maximum value.
     * <p>
     * The default Mixin implementation kills the entity by applying spirit erosion damage
     * and spawning a {@link NightmareApostle} at the player's death position.
     * <p>
     * The specific behavior is as follows:
     * <ul>
     *   <li>If the player is not alive, the method does nothing</li>
     *   <li>Retrieves the {@code SPIRIT_EROSION} damage type from the registry</li>
     *   <li>Creates a {@link NightmareApostle} at the player's current position,
     *       with the player's name displayed above it</li>
     *   <li>Applies spirit erosion damage to the player, with the nightmareApostle set
     *       as the damage source entity</li>
     *   <li>Forces the player's health to 0 to ensure death</li>
     * </ul>
     * <p>
     * The zombie is spawned as a visual representation of the player's final erosion.
     *
     * @throws NullPointerException if the {@code SPIRIT_EROSION} damage type
     *         is not registered
     * @see ErosionCauser
     */
    default void erosionKill() {
    }

    /**
     * Determines whether the player is currently in a state that allows erosion changes.
     * <p>
     * This method is used as a precondition check before applying any erosion changes.
     * Returns {@code true} if erosion can proceed, {@code false} if it should be blocked.
     * <p>
     * The default implementation always returns {@code true}.
     *
     * @return {@code true} if erosion changes are allowed, {@code false} otherwise
     */
    default boolean canErosionChange() {
        return true;
    }

    /**
     * Whether erosion can naturally decrease in the current environment.
     * <p>
     * This method determines if the current environment allows natural erosion reduction.
     * If {@code false}, {@link #updateErosion()} will not execute the natural decrease logic.
     * In erosion biome, this method should return {@code false}.
     * The default implementation returns {@code true}.
     *
     * @param biome the biome the player is currently in
     * @return {@code true} if natural reduction is allowed, {@code false} otherwise
     */
    default boolean canErosionReduceNaturally(Holder<Biome> biome) {
        return true;
    }

    /**
     * The interval between natural erosion reduction events.
     * <p>
     * When the player is in a non-erosion environment, a natural reduction occurs
     * every time this interval elapses. Each reduction decreases the erosion value
     * by the amount specified in {@link #getErosionReduceAmount(Holder)}.
     *
     * @param biome the biome the player is currently in
     * @return the interval in ticks between natural reduction events
     */
    default int getErosionReduceInterval(Holder<Biome> biome) {
        return 300;
    }

    /**
     * The amount of erosion decreased per natural reduction event.
     * <p>
     * When {@link #updateErosion()} triggers a natural reduction, the erosion value
     * decreases by the amount returned by this method.
     * The default value is 0.0025.
     *
     * @param biome the biome the player is currently in
     * @return the decrease amount per natural reduction event
     */
    default double getErosionReduceAmount(Holder<Biome> biome) {
        return 0.0025;
    }

    /**
     * Whether erosion is continuously increasing in the current environment.
     * <p>
     * This method determines if the current environment or state causes erosion
     * to continuously rise over time. For example, being in an erosion biome or
     * under certain negative effects may trigger continuous increase.
     * <p>
     * When this method returns {@code true}, {@link #updateErosion()} will
     * call {@link #getErosionAccumulationRate(Holder)} to determine how much erosion increases
     * per tick, and {@link #getErosionAccumulationInterval(Holder)} to control the frequency.
     * The default implementation returns {@code false}.
     *
     * @param biome the biome the player is currently in
     * @return {@code true} if erosion is continuously increasing, {@code false} otherwise
     */
    default boolean isErosionAccumulating(Holder<Biome> biome) {
        return false;
    }

    /**
     * The interval between erosion accumulation events.
     * <p>
     * When {@link #isErosionAccumulating(Holder)} returns {@code true},
     * erosion increases by {@link #getErosionAccumulationRate(Holder)} every time this interval elapses.
     * <p>
     * This method may return different values based on the current biome.
     * The default value is 10 ticks (0.5 second).
     *
     * @param biome the biome the player is currently in
     * @return the interval in ticks between accumulation events
     */
    default int getErosionAccumulationInterval(Holder<Biome> biome) {
        return 10;
    }

    /**
     * The amount of erosion increased per accumulation event.
     * <p>
     * When {@link #isErosionAccumulating(Holder)} returns {@code true},
     * the erosion value increases by the amount returned by this method
     * every {@link #getErosionAccumulationInterval(Holder)} ticks.
     * The default value is 0.005.
     *
     * @param biome the biome the player is currently in
     * @return the increase amount per accumulation event
     */
    default double getErosionAccumulationRate(Holder<Biome> biome) {
        return 0.00125;
    }
}
