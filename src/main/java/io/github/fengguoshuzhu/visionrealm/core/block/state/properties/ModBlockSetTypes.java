package io.github.fengguoshuzhu.visionrealm.core.block.state.properties;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import static net.minecraft.world.level.block.state.properties.BlockSetType.register;

public class ModBlockSetTypes {
    public static final BlockSetType BLOOD_CORRODED_CHERRY = register(
            new BlockSetType(
                    "blood_corroded_cherry",
                    true,
                    true,
                    true,
                    BlockSetType.PressurePlateSensitivity.EVERYTHING,
                    SoundType.CHERRY_WOOD,
                    SoundEvents.CHERRY_WOOD_DOOR_CLOSE,
                    SoundEvents.CHERRY_WOOD_DOOR_OPEN,
                    SoundEvents.CHERRY_WOOD_TRAPDOOR_CLOSE,
                    SoundEvents.CHERRY_WOOD_TRAPDOOR_OPEN,
                    SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF,
                    SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON,
                    SoundEvents.CHERRY_WOOD_BUTTON_CLICK_OFF,
                    SoundEvents.CHERRY_WOOD_BUTTON_CLICK_ON
            )
    );
}
