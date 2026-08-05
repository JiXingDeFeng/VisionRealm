package io.github.jixingdefeng.visionrealm.content.world.entity.player;

import com.mojang.authlib.GameProfile;
import io.github.jixingdefeng.visionrealm.core.hook.player.PlayerMirrorStorageHook;
import io.github.jixingdefeng.visionrealm.core.world.entity.player.MirrorPlayerFactory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MirrorPlayer extends ServerPlayer {
    public static final long UUID_MOST_OFFSET = 281474976710655L;
    public static final long UUID_LEAST_OFFSET = 281474976710655L;
    protected static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.SERVERBOUND);
    protected final UUID targetPlayerUUID;
    protected boolean activated = false;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 50);
    }

    public static UUID generatedUUID(@NotNull Player player) {
        UUID playerUUID = player.getUUID();
        return new UUID(
                playerUUID.getLeastSignificantBits(),
                playerUUID.getMostSignificantBits()
        );
    }

    public MirrorPlayer(ServerLevel level, @NotNull Player player) {
        this(level, new GameProfile(generatedUUID(player), player.getName().getString()), player);
    }

    public MirrorPlayer(ServerLevel level, GameProfile gameProfile, @NotNull Player targetPlayer) {
        super(level.getServer(), level, gameProfile, ClientInformation.createDefault());
        this.connection = new ServerGamePacketListenerImpl(
                level.getServer(), DUMMY_CONNECTION, this, CommonListenerCookie.createInitial(this.getGameProfile(), false)
        );
        ((PlayerMirrorStorageHook) targetPlayer).addMirrorPlayer(this);
        this.targetPlayerUUID = targetPlayer.getGameProfile().getId();
        this.syncAttributes(targetPlayer);
        this.setHealth(targetPlayer.getHealth());
        this.syncItems(targetPlayer);

        if (targetPlayer instanceof ServerPlayer player) {
            this.setGameMode(player.gameMode.getGameModeForPlayer());
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        Player player = this.level().getPlayerByUUID(this.targetPlayerUUID);
        if (player != null) {
            boolean result = player.hurt(source, amount);
            this.setHealth(player.getHealth());
            if (!this.isAlive()) {
                this.kill();
            }

            return result;
        } else {
            this.kill();
            return false;
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public void die(@NotNull DamageSource source) {
        super.die(source);
        MirrorPlayerFactory.unloadPlayer((ServerLevel) this.level(), this.getGameProfile());
        this.kill();
    }

    public UUID getTargetPlayerUUID() {
        return this.targetPlayerUUID;
    }

    public boolean isActivated() {
        return this.activated;
    }

    protected void syncAttributes(Player player) {
        Collection<AttributeInstance> targetAttributes = player.getAttributes().getSyncableAttributes();
        AttributeMap currentMap = this.getAttributes();
        for (AttributeInstance targetAttribute : targetAttributes) {
            AttributeInstance currentAttribute = currentMap.getInstance(targetAttribute.getAttribute());
            if (currentAttribute != null) {
                currentAttribute.setBaseValue(targetAttribute.getBaseValue());
                for (AttributeModifier attribute : targetAttribute.getModifiers()) {
                    currentAttribute.addOrReplacePermanentModifier(attribute);
                }
            }
        }
    }

    protected void syncItems(Player player) {
        Inventory targetInventory = player.getInventory();
        Inventory currentInventory = this.getInventory();
        List<NonNullList<ItemStack>> targetList = List.of(targetInventory.items, targetInventory.armor, targetInventory.offhand);
        List<NonNullList<ItemStack>> currentList = List.of(currentInventory.items, currentInventory.armor, currentInventory.offhand);
        for (int i = 0; i < targetList.size(); ++i) {
            NonNullList<ItemStack> target  = targetList.get(i);
            NonNullList<ItemStack> current = currentList.get(i);
            for (int j = 0; j < target.size(); j++) {
                current.set(j, target.get(j));
            }
        }
    }
}
