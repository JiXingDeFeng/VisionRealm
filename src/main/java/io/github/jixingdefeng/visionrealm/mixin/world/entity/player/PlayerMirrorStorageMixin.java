package io.github.jixingdefeng.visionrealm.mixin.world.entity.player;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.github.jixingdefeng.visionrealm.content.world.entity.player.MirrorPlayer;
import io.github.jixingdefeng.visionrealm.core.hook.player.PlayerMirrorStorageHook;
import io.github.jixingdefeng.visionrealm.core.world.entity.player.MirrorPlayerFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Mixin(Player.class)
public class PlayerMirrorStorageMixin implements PlayerMirrorStorageHook {
    @Unique
    private final Player visionRealm$player = (Player) (Object) this;
    @Unique
    private Set<UUID> visionRealm$hiddenPlayers = Sets.newHashSet();
    @Unique
    @Nullable
    private GameProfile visionRealm$mirrorPlayer;
    @Unique
    private boolean visionRealm$dirtyMirror = false;

    @Override
    public void addHiddenPlayer(UUID player) {
        this.visionRealm$hiddenPlayers.add(player);
        this.visionRealm$sync();
    }

    @Override
    public void addHiddenPlayerAll(Collection<UUID> player) {
        this.visionRealm$hiddenPlayers.addAll(player);
        this.visionRealm$sync();
    }

    @Override
    public void removeHiddenPlayerAll(UUID player) {
        this.visionRealm$hiddenPlayers.remove(player);
        this.visionRealm$sync();
    }

    @Override
    public void removeHiddenPlayerAll(Collection<UUID> player) {
        this.visionRealm$hiddenPlayers.removeAll(player);
        this.visionRealm$sync();
    }

    @Override
    public Set<UUID> getHiddenPlayers() {
        return Collections.unmodifiableSet(this.visionRealm$hiddenPlayers);
    }

    @Override
    public void addMirrorPlayer(MirrorPlayer mirrorPlayer) {
        if (this.visionRealm$mirrorPlayer == null) {
            this.visionRealm$mirrorPlayer = mirrorPlayer.getGameProfile();
            this.visionRealm$dirtyMirror = true;
        } else if (this.visionRealm$player.level() instanceof ServerLevel level){
            Player player = MirrorPlayerFactory.getPlayer(level, this.visionRealm$mirrorPlayer);
            if (player == null) {
                this.visionRealm$mirrorPlayer = mirrorPlayer.getGameProfile();
                this.visionRealm$dirtyMirror = true;
            }
        }
    }

    @Override
    public void removeMirrorPlayer() {
        this.visionRealm$mirrorPlayer = null;
        this.visionRealm$dirtyMirror = true;
    }

    @Nullable
    @Override
    public GameProfile getMirrorPlayer() {
        return this.visionRealm$mirrorPlayer;
    }

    @Unique
    private void visionRealm$sync() {
        if (this.visionRealm$player instanceof ServerPlayer player) {
            UUID uuid = null;
            if (this.visionRealm$dirtyMirror && this.visionRealm$mirrorPlayer != null) {
                uuid = this.visionRealm$mirrorPlayer.getId();
                this.visionRealm$dirtyMirror = false;
            }

            MirrorPlayerFactory.sync(player, this.visionRealm$hiddenPlayers, uuid);
        }
    }
}
