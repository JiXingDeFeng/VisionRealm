package io.github.jixingdefeng.visionrealm.event.game.network;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.world.entity.player.ClientMirrorData;
import io.github.jixingdefeng.visionrealm.network.protocol.ClientMirrorDataPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PacketPayloadEvents {
    public static final String VERSION = VisionRealm.MOD_ID + "-0.1.0";

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar(VERSION).playToServer(
                ClientMirrorDataPacketPayload.TYPE,
                ClientMirrorDataPacketPayload.STREAM_CODEC,
                (payload, context) ->
                        ClientMirrorData.updateData(
                                payload.hiddenPlayers().orElse(null),
                                payload.mirrorMap().orElse(null),
                                payload.mirror().orElse(null),
                                payload.isActivated()
                        )
        );
    }
}
