package io.github.jixingdefeng.visionrealm.core.world.entity.player;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import io.github.jixingdefeng.visionrealm.content.world.entity.player.MirrorPlayer;
import io.github.jixingdefeng.visionrealm.network.protocol.ClientMirrorDataPacketPayload;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MirrorPlayerFactory {
    private static final Map<MirrorPlayerKey, MirrorPlayer> mirrorPlayers = Maps.newHashMap();
    private static final Map<UUID, UUID> mirrorUUIDs = Maps.newHashMap();

    private record MirrorPlayerKey(ServerLevel serverLevel, GameProfile gameProfile) {}

    @Nullable
    public static MirrorPlayer getPlayer(ServerLevel level, GameProfile profile) {
        MirrorPlayerKey key = new MirrorPlayerKey(level, profile);
        return mirrorPlayers.get(key);
    }

    public static MirrorPlayer addPlayer(ServerLevel level, Player player) {
        UUID generatedUUID = MirrorPlayer.generatedUUID(player);
        GameProfile gameProfile = new GameProfile(generatedUUID, player.getName().getString());
        MirrorPlayerKey key = new MirrorPlayerKey(level, gameProfile);

        MirrorPlayer cachedPlayer = mirrorPlayers.get(key);
        if (cachedPlayer != null) {
            return cachedPlayer;
        } else {
            MirrorPlayer newMirror = new MirrorPlayer(level, gameProfile, player);
            mirrorPlayers.put(key, newMirror);
            mirrorUUIDs.put(generatedUUID, player.getUUID());
            return newMirror;
        }
    }

    public static void addToLevel(ServerLevel level, MirrorPlayer player) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, player
        );
        level.players().forEach(serverPlayer -> serverPlayer.connection.send(packet));
        level.addNewPlayer(player);
    }

    public static void unloadPlayer(ServerLevel level, GameProfile profile) {
        MirrorPlayerKey key = new MirrorPlayerKey(level, profile);
        mirrorPlayers.remove(key);
        mirrorUUIDs.remove(profile.getId());
    }

    public static void unloadLevel(ServerLevel level) {
        mirrorPlayers.entrySet().removeIf(entry -> entry.getKey().serverLevel == level);
    }

    public static void sync(ServerPlayer serverPlayer, Set<UUID> hiddenPlayers, @Nullable UUID mirror) {
        boolean isActivated = false;
        if (mirror != null && mirrorUUIDs.containsKey(mirror)) {
            Player player = serverPlayer.level().getPlayerByUUID(mirror);
            isActivated = player instanceof MirrorPlayer mirrorPlayer && mirrorPlayer.isActivated();
        }

        PacketDistributor.sendToPlayer(
                serverPlayer,
                new ClientMirrorDataPacketPayload(hiddenPlayers, mirrorUUIDs, mirror, isActivated)
        );
    }
}
