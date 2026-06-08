package io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.player;

import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class PlayerEvents {

    public static void initAttributes(Player player, boolean endConquered) {
        if (!endConquered) {
            AttributeInstance sane = player.getAttributes().getInstance(ModAttributes.SANITY);
            if (sane != null) {
                sane.setBaseValue(100);
            }
        }
    }
}
