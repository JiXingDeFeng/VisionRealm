package io.github.fengguoshuzhu.visionrealm.event.bus.game.world.entity.player;

import io.github.fengguoshuzhu.visionrealm.api.controller.entity.player.PlayerSanityController;
import net.minecraft.world.entity.player.Player;

public class PlayerTickEvents {

    public static void updateAttribute(Player player) {
        if (!player.level().isClientSide) {
            if (player instanceof PlayerSanityController updatable) {
                updatable.updateSanity();
            }
        }
    }
}
