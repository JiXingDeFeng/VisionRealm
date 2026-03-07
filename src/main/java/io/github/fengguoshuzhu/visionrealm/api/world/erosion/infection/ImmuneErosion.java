package io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection;

import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;

public interface ImmuneErosion {

    default boolean immune(ErosionType type) {
        return true;
    }
}
