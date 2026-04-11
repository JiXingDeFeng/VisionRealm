package io.github.jixingdefeng.visionrealm.api.erosion;

import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;

/**
 * Represents an entity or block that can be immune to specific types of erosion.
 * <p>
 * Implementing this interface allows objects to selectively resist certain
 * erosion types, providing fine-grained control over which transformations
 * can affect them.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * public class MyEntity implements ImmuneErosion {
 *     @Override
 *     public boolean immune(ErosionType type) {
 *         return type == ErosionType.BLOOD; // Immune to blood erosion only
 *     }
 * }
 * }</pre>
 *
 * @author JiXingDeFeng
 * @see ErosionType
 * @since 0.0.1-dev-1
 */
public interface ImmuneErosion {

    /**
     * Determines whether the implementing object is immune to the specified erosion type.
     * <p>
     * The default implementation returns {@code true}, making the object immune to all
     * erosion types. Override this method to implement specific immunities.
     *
     * @param type The erosion type to check immunity against
     * @return {@code true} if the object is immune to this erosion type,
     *         {@code false} otherwise
     */
    default boolean immune(ErosionType type) {
        return true;
    }
}
