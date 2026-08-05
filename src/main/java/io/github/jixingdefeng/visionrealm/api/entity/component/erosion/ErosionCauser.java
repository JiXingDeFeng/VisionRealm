package io.github.jixingdefeng.visionrealm.api.entity.component.erosion;

import io.github.jixingdefeng.visionrealm.core.hook.player.PlayerErosionHook;
import org.jetbrains.annotations.NotNull;

/**
 * Marker interface for entities that can cause erosion.
 * <p>
 * Entities implementing this interface are considered capable of inflicting
 * erosion upon players, typically through direct contact, environmental effects,
 * or special abilities.
 *
 * @see PlayerErosionHook
 */
public interface ErosionCauser {

    /**
     * @return The type of erosion calculation used by this causer
     */
    @NotNull
    Calculation getCalculation();

    /**
     * @return The erosion value applied to the player
     *         <ul>
     *           <li>If {@link Calculation#DAMAGE_PERCENTAGE}: this value is treated as a percentage of the damage dealt</li>
     *           <li>If {@link Calculation#FIXATION}: this value is a fixed amount added directly</li>
     *         </ul>
     */
    double getValue();

    /**
     * Defines how erosion value is calculated.
     */
    enum Calculation {
        /** Erosion increases by a percentage of the damage amount inflicted. */
        DAMAGE_PERCENTAGE,
        /** Erosion increases by a fixed value regardless of damage. */
        FIXATION
    }
}
