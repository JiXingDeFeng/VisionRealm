package io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.player;

import io.github.jixingdefeng.visionrealm.api.controller.player.PlayerErosionController;
import io.github.jixingdefeng.visionrealm.api.controller.player.PlayerSanityController;
import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class PlayerEvents {

    public static void initAttributes(ServerPlayer player) {
        Holder<Attribute> saneHolder = ModAttributes.PLAYER_SANITY;
        Holder<Attribute> erosionHolder = ModAttributes.PLAYER_SPIRIT_EROSION;
        AttributeInstance sane = player.getAttributes().getInstance(saneHolder);
        AttributeInstance erosion = player.getAttributes().getInstance(erosionHolder);

        if (sane != null) sane.setBaseValue(saneHolder.value().getDefaultValue());
        if (erosion != null) erosion.setBaseValue(erosionHolder.value().getDefaultValue());
    }

    public static void updateAttribute(Player player) {
        if (!player.level().isClientSide) {
            ((PlayerSanityController) player).updateSanity();
            ((PlayerErosionController) player).updateErosion();
        }
    }

    public static void onHurt(Player player, DamageSource source, float amount) {
        if (!player.level().isClientSide) {
            ((PlayerSanityController) player).sanityHurtUpdate(source, amount);
            ((PlayerErosionController) player).erosionHurtUpdate(source, amount);
        }
    }
}
