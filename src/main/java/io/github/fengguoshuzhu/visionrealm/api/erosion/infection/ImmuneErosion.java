package io.github.fengguoshuzhu.visionrealm.api.erosion.infection;

import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;

public interface ImmuneErosion {

    default boolean immune(ErosionType type) {
        return true;
    }
}
