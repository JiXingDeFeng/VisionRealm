package io.github.jixingdefeng.visionrealm.event.game.world.dimension;

import io.github.jixingdefeng.visionrealm.client.renderer.VisionRealmDimensionSpecialEffects;
import io.github.jixingdefeng.visionrealm.content.world.level.dimension.ModDimensionTypes;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

public class DimensionEvents {

    public static void onRegistryDimensionSpecialEffects(final RegisterDimensionSpecialEffectsEvent event) {
        event.register(ModDimensionTypes.VISIONREALM_EFFECTS, new VisionRealmDimensionSpecialEffects());
    }
}
