package io.github.fengguoshuzhu.visionrealm.event.bus.game.world.entity.player;

import io.github.fengguoshuzhu.visionrealm.core.entity.ai.attributes.ModAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class PlayerEvents {

    public static void initAttributes(Player player, boolean endConquered) {
        if (!endConquered) {
            AttributeInstance sane = player.getAttributes().getInstance(ModAttributes.SANITY);
            AttributeInstance erosion = player.getAttributes().getInstance(ModAttributes.EROSION);

            if (sane != null && erosion != null) {
                sane.setBaseValue(100);
                erosion.setBaseValue(0);
            }
        }
    }
}
