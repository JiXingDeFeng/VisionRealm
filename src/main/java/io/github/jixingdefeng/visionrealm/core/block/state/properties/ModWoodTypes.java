package io.github.jixingdefeng.visionrealm.core.block.state.properties;

import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.WoodType;

import static net.minecraft.world.level.block.state.properties.WoodType.register;

public class ModWoodTypes {
    public static final WoodType BLOOD_CORRODED_CHERRY = register(
            new WoodType(
                    VisionRealm.MOD_ID + ":blood_corroded_cherry",
                    ModBlockSetTypes.BLOOD_CORRODED_CHERRY,
                    SoundType.CHERRY_WOOD,
                    SoundType.CHERRY_WOOD_HANGING_SIGN,
                    SoundEvents.CHERRY_WOOD_FENCE_GATE_CLOSE,
                    SoundEvents.CHERRY_WOOD_FENCE_GATE_OPEN
            )
    );
}
