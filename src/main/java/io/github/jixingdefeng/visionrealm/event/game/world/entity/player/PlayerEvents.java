package io.github.jixingdefeng.visionrealm.event.game.world.entity.player;

import io.github.jixingdefeng.visionrealm.content.world.entity.ai.attributes.ModAttributes;
import io.github.jixingdefeng.visionrealm.core.hook.player.PlayerErosionHook;
import io.github.jixingdefeng.visionrealm.core.hook.player.PlayerSanityHook;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class PlayerEvents {

    public static void initAttributes(final ServerPlayer player) {
        Holder<Attribute> saneHolder = ModAttributes.PLAYER_SANITY;
        Holder<Attribute> erosionHolder = ModAttributes.PLAYER_SPIRIT_EROSION;
        AttributeInstance sane = player.getAttributes().getInstance(saneHolder);
        AttributeInstance erosion = player.getAttributes().getInstance(erosionHolder);

        if (sane != null) sane.setBaseValue(saneHolder.value().getDefaultValue());
        if (erosion != null) erosion.setBaseValue(erosionHolder.value().getDefaultValue());
    }

    public static void updateAttribute(final Player player) {
        if (!player.level().isClientSide) {
            ((PlayerSanityHook) player).updateSanity();
            ((PlayerErosionHook) player).updateErosion();
        }
    }

    public static void onHurt(final Player player, final DamageSource source, final float amount) {
        if (!player.level().isClientSide) {
            ((PlayerSanityHook) player).sanityHurtUpdate(source, amount);
            ((PlayerErosionHook) player).erosionHurtUpdate(source, amount);
        }
    }
}
