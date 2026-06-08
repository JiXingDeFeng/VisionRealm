package io.github.jixingdefeng.visionrealm.api.controller.entity;

import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.event.bus.game.GameBusEvents;
import io.github.jixingdefeng.visionrealm.mixin.world.event.entity.EntityErosionControllerMixin;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * CanBeErodedEntity Erosion Update Interface
 * <p>
 * Entities implementing this interface will have an erosion degree system that changes over time and with damage events.
 * Erosion degree represents the extent to which an entity is corroded, affecting its behavior and attributes.
 * </p>
 *
 * <h2>Invocation Mechanism:</h2>
 * <ul>
 *   <li>{@link #updateErosion()} - Called during {@link EntityTickEvent.Pre} event</li>
 *   <li>{@link #onHurtUpdateErosion(DamageSource, float)} - Called during {@link LivingDamageEvent.Post} event</li>
 * </ul>
 *
 * <h2>Implementation:</h2>
 * <p>
 * Default implementations are provided by {@code EntityErosionControllerMixin} through Mixin injection into all LivingEntities.
 * Override these methods for custom behavior.
 * </p>
 *
 * @author JiXingDeFeng
 * @see EntityErosionControllerMixin
 * @since 0.0.1-dev
 */
public interface EntityErosionController {

    /**
     * Regular erosion update method.
     * <p>
     * This method is called during {@link EntityTickEvent.Pre} event, once per tick,
     * handling natural changes in erosion degree.
     * </p>
     *
     * <p><b>Default behavior (provided by {@code LivingEntityErosionMixin}):</b></p>
     * <ul>
     *   <li>Periodically increases erosion degree in erosion zones</li>
     *   <li>Periodically decreases erosion degree in non-erosion zones</li>
     *   <li>Uses {@link #getErosionDecreaseInterval(ErosionType)} to control change frequency</li>
     * </ul>
     *
     * @see GameBusEvents#onEntityTick(EntityTickEvent.Pre)
     * @see EntityErosionControllerMixin#updateErosion()
     */
    void updateErosion();

    /**
     * Sets the erosion progress for a specific erosion type.
     * <p>
     * Values should be between 0 and 10000 inclusive, where 0 = no erosion,
     * 10000 = fully eroded.
     * </p>
     *
     * @param type     The erosion type to set
     * @param progress The erosion progress value (0-10000)
     */
    void setErosionProgress(ErosionType type, short progress);

    /**
     * Gets the current value of the erosion decrease timer.
     * <p>
     * Returns the remaining ticks to wait until the next natural erosion decrease.
     * When this value is less than or equal to 0, the next erosion decrease will trigger.
     * This value decreases each game tick (typically handled by an event processor).
     *
     * @return The remaining ticks to wait, 0 or negative means decrease can trigger immediately
     *
     * @see #setErosionDecreaseTimer(ErosionType, int) Set the timer value
     * @see #getErosionDecreaseInterval(ErosionType) Get the decrease interval
     */
    int getErosionDecreaseTimer(ErosionType type);

    /**
     * Sets the current value of the erosion decrease timer.
     * <p>
     * Manually sets the remaining ticks to wait until the next natural erosion decrease.
     * Typically used for:
     * <ul>
     *   <li>Initializing the timer (set to decrease interval)</li>
     *   <li>Resetting the timer (after decrease triggers)</li>
     *   <li>External event influences (entering special biomes, using items, etc.)</li>
     * </ul>
     *
     * @param time The remaining ticks to set, positive values recommended (negative means trigger immediately)
     *
     * @see #getErosionDecreaseTimer(ErosionType) Get the current timer value
     * @see #getErosionDecreaseInterval(ErosionType) Get the decrease interval
     */
    void setErosionDecreaseTimer(ErosionType type, int time);

    /**
     * Gets the current erosion progress for a specific erosion type.
     *
     * @param type The erosion type
     * @return The erosion progress value (0-10000)
     */
    short getErosionProgress(ErosionType type);

    /**
     * Gets the erosion type with the highest progress value.
     *
     * @return The dominant erosion type, or {@link ErosionType#NONE} if no erosion present
     */
    ErosionType getTypeWithMaxValue();

    /**
     * Gets the maximum erosion progress value among all erosion types.
     *
     * @return The highest erosion progress value (0-10000)
     */
    short getMaxErosionProgress();

    /**
     * Erosion update method called when taking damage.
     * <p>
     * This method is called during {@link LivingDamageEvent.Post} event,
     * handling the impact of damage events on erosion degree.
     * </p>
     *
     * <p><b>Default behavior (provided by {@code LivingEntityErosionMixin}):</b></p>
     * <ul>
     *   <li>Increases erosion degree based on damage type (e.g., corrosive, fire damage)</li>
     *   <li>Calculates erosion increase based on damage amount</li>
     * </ul>
     *
     * @param source The damage source
     * @param amount The damage amount
     *
     * @see GameBusEvents#onEntityHurt(LivingDamageEvent.Post)
     */
    default void onHurtUpdateErosion(DamageSource source, float amount) {
    }

    /**
     * Checks whether the entity has completed erosion.
     * <p>
     * Determines if the entity has accumulated enough erosion degree to trigger
     * an erosion transformation event. This typically checks if the current
     * erosion value has reached or exceeded the threshold (1.0).
     * </p>
     *
     * <p><b>Default behavior (provided by {@code LivingEntityErosionMixin}):</b></p>
     * <ul>
     *   <li>Verifies the entity implements {@code CanBeErosion.CanBeErodedEntity}</li>
     *   <li>Checks if erosion attribute value is >= 1.0</li>
     *   <li>Used by tick handlers to determine when to trigger erosion effects</li>
     * </ul>
     *
     * @return true if erosion is complete and ready to trigger, false otherwise
     *
     * @see #updateErosion()
     */
    default boolean completeErosion(ErosionType type) {
        return false;
    }

    /**
     * Determines whether the entity can be affected by a specific erosion type.
     * <p>
     * This method allows fine-grained control over which erosion types can affect
     * the entity. It can be overridden to implement immunities, resistances, or
     * conditional erosion applicability based on entity state.
     * </p>
     *
     * @param type The erosion type to check for applicability
     * @return true if the entity can be affected by the specified erosion type,
     *         false if immune or cannot be eroded by that type
     *
     * @see ErosionType
     * @see #onHurtUpdateErosion(DamageSource, float)
     * @see #updateErosion()
     */
    default boolean canBeEroded(ErosionType type) {
        return true;
    }

    /**
     * Gets the time interval for erosion degree decrease.
     * <p>
     * This method controls the frequency of erosion decrease.
     * When an entity is not in an erosion zone, erosion degree decreases once
     * every interval returned by this method.
     * </p>
     *
     * @return The decrease interval in ticks (default 200 ticks = 10 seconds)
     *
     * @see #updateErosion()
     */
    default int getErosionDecreaseInterval(ErosionType type) {
        return 200;
    }

    /**
     * Gets the natural reduction rate of erosion value.
     * <p>
     * When the entity is in non-erosion areas and meets the natural reduction conditions,
     * the erosion value will naturally decrease at this rate per tick.
     * This value represents the amount decreased per tick, default is 1 (0.1% per tick).
     * It takes approximately 1000 ticks (50 seconds) to decrease from maximum (1.0) to 0.
     * <p>
     * Note: This reduction rate applies to normal situations. Special areas or states may affect the actual reduction rate.
     *
     * @return The amount of erosion value naturally decreased per tick, typically between 0 and 10000
     *
     * @see #canBeReducedNaturally(ErosionType) Check if natural reduction is allowed
     * @see #getErosionDecreaseInterval(ErosionType) Get the reduction interval time
     */
    default short naturallyReducedValue(ErosionType type) {
        return 10;
    }

    /**
     * Determines whether the erosion value can naturally decrease.
     * <p>
     * Controls whether the entity can naturally decrease erosion value in non-erosion areas.
     * Returns true if the entity can gradually decrease erosion value in safe areas,
     * returns false if erosion value cannot naturally decrease (may remain unchanged or require other methods to decrease).
     *
     * @return true if natural reduction is allowed, false otherwise
     *
     * @see #naturallyReducedValue(ErosionType) Get the natural reduction rate
     */
    default boolean canBeReducedNaturally(ErosionType type) {
        return true;
    }
}
