package io.github.jixingdefeng.visionrealm.core.block.grower;

import io.github.jixingdefeng.visionrealm.core.data.worldgen.features.ModTreeFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class ModTreeGrower {
    public static final TreeGrower BLOOD_CORRODED_CHERRY = new TreeGrower(
            "blood_corroded_cherry",
            Optional.empty(),
            Optional.of(ModTreeFeatures.BLOOD_CORRODED_CHERRY),
            Optional.of(ModTreeFeatures.BLOOD_CORRODED_CHERRY_BEES_005)
    );
}
