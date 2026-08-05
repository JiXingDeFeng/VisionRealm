package io.github.jixingdefeng.visionrealm.core.hook.player;

import io.github.jixingdefeng.visionrealm.api.entity.component.erosion.ErosionCauser;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.NightmareApostle;
import io.github.jixingdefeng.visionrealm.mixin.world.entity.player.PlayerErosionHookMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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
 * Default implementations are provided by {@code PlayerErosionHookMixin} through Mixin injection into all Players.
 * Override these methods for custom behavior.
 * </p>
 *
 * @author JiXingDeFeng
 * @see PlayerErosionHookMixin
 * @since 0.1.0
 */
public interface PlayerErosionHook {

    /**
     * Updates the erosion value, called once per game tick.
     * <p>
     * This method is called during the player's tick and handles natural changes
     * to the erosion value. Depending on the current environment state, the erosion
     * value either increases or decreases:
     * <ul>
     *   <li>In erosion environments, the erosion value continuously increases</li>
     *   <li>In non-erosion environments, and if {@link #canErosionReduceNaturally(Level, BlockPos)} returns
     *       {@code true}, a natural reduction is triggered every
     *       {@link #getErosionReduceInterval(Level, BlockPos)} ticks, decreasing the erosion value
     *       by {@link #getErosionReduceAmount(Level, BlockPos)}</li>
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
     *
     * @param player the player to kill when erosion reaches maximum value
     * @throws NullPointerException if the {@code SPIRIT_EROSION} damage type
     *         is not registered
     * @see ErosionCauser
     */
    default void erosionKill(Player player) {
    }

    /**
     * Determines whether the player is currently in a state that allows erosion changes.
     * <p>
     * This method is used as a precondition check before applying any erosion changes.
     * Returns {@code true} if erosion can proceed, {@code false} if it should be blocked.
     * <p>
     * The default implementation always returns {@code true}.
     *
     * @param level the level containing the position
     * @param pos   the position where the natural reduction occurs
     * @return {@code true} if erosion changes are allowed, {@code false} otherwise
     */
    default boolean canErosionChange(Level level, BlockPos pos) {
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
     * @param level the level containing the position
     * @param pos   the position where the natural reduction occurs
     * @return {@code true} if natural reduction is allowed, {@code false} otherwise
     */
    default boolean canErosionReduceNaturally(Level level, BlockPos pos) {
        return true;
    }

    /**
     * The interval between natural erosion reduction events.
     * <p>
     * When the player is in a non-erosion environment, a natural reduction occurs
     * every time this interval elapses. Each reduction decreases the erosion value
     * by the amount specified in {@link #getErosionReduceAmount(Level, BlockPos)}.
     *
     * @param level the level containing the position
     * @param pos   the position where the natural reduction occurs
     * @return the interval in ticks between natural reduction events
     */
    default int getErosionReduceInterval(Level level, BlockPos pos) {
        return 300;
    }

    /**
     * The amount of erosion decreased per natural reduction event.
     * <p>
     * When {@link #updateErosion()} triggers a natural reduction, the erosion value
     * decreases by the amount returned by this method.
     * The default value is 0.0025.
     *
     * @param level the level containing the position
     * @param pos   the position where the natural reduction occurs
     * @return the decrease amount per natural reduction event
     */
    default double getErosionReduceAmount(Level level, BlockPos pos) {
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
     * call {@link #getErosionAccumulationRate(Level, BlockPos)} to determine how much erosion increases
     * per tick, and {@link #getErosionAccumulationInterval(Level, BlockPos)} to control the frequency.
     * The default implementation returns {@code false}.
     *
     * @param level the level containing the position
     * @param pos   the position where the natural reduction occurs
     * @return {@code true} if erosion is continuously increasing, {@code false} otherwise
     */
    default boolean isErosionAccumulating(Level level, BlockPos pos) {
        return false;
    }

    /**
     * The interval between erosion accumulation events.
     * <p>
     * When {@link #isErosionAccumulating(Level, BlockPos)} returns {@code true},
     * erosion increases by {@link #getErosionAccumulationRate(Level, BlockPos)} every time this interval elapses.
     * <p>
     * This method may return different values based on the current biome.
     * The default value is 10 ticks (0.5 second).
     *
     * @param level the level containing the position
     * @param pos   the position where the natural reduction occurs
     * @return the interval in ticks between accumulation events
     */
    default int getErosionAccumulationInterval(Level level, BlockPos pos) {
        return 10;
    }

    /**
     * The amount of erosion increased per accumulation event.
     * <p>
     * When {@link #isErosionAccumulating(Level, BlockPos)} returns {@code true},
     * the erosion value increases by the amount returned by this method
     * every {@link #getErosionAccumulationInterval(Level, BlockPos)} ticks.
     * The default value is 0.005.
     *
     * @param level the level containing the position
     * @param pos   the position where the natural reduction occurs
     * @return the increase amount per accumulation event
     */
    default double getErosionAccumulationRate(Level level, BlockPos pos) {
        return 0.00125;
    }
}
