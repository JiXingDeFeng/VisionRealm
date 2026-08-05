package io.github.jixingdefeng.visionrealm.core.util.selector;

/**
 * Determines surface detection behavior when scanning blocks.
 * Controls whether surface restriction is applied and how fluid blocks are treated.
 *
 * @see #isSurfaceMode()
 * @see #allowFluid()
 * @author JiXingDeFeng
 * @since 0.1.0
 */
public enum SurfaceSelection {
    /** Surface detection disabled; all blocks are considered. */
    DISABLED,
    /** Surface detection enabled; fluid blocks are accepted as valid surfaces. */
    FLUID_INCLUDED,
    /** Surface detection enabled; fluid blocks are ignored as surfaces. */
    FLUID_EXCLUDED;

    public boolean isSurfaceMode() {
        return this != DISABLED;
    }

    public boolean allowFluid() {
        return this == FLUID_INCLUDED;
    }
}
