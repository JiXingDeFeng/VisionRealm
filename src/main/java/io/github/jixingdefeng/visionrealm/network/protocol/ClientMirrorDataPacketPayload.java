package io.github.jixingdefeng.visionrealm.network.protocol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Payload for synchronizing client-side mirror system state.
 * <p>
 * This packet is sent from the server to the client to update the complete
 * mirror system state, including:
 * <ul>
 *   <li>The list of players that should be hidden from the client's view</li>
 *   <li>The mapping of mirror player UUIDs to their original player UUIDs</li>
 *   <li>The client's own mirror player UUID, if one exists</li>
 *   <li>Whether the mirror system is currently enabled for the client</li>
 * </ul>
 *
 * @param hiddenPlayers The set of UUIDs representing players to hide
 * @param mirrorMap     The mapping of mirror player UUIDs to original player UUIDs
 * @param mirror        The client's own mirror player UUID, or empty if none
 * @param isActivated      Whether the mirror system is enabled for the client
 * @author JiXingDeFeng
 * @since 0.1.0
 */
public record ClientMirrorDataPacketPayload(
        Optional<Set<UUID>> hiddenPlayers,
        Optional<Map<UUID, UUID>> mirrorMap,
        Optional<UUID> mirror,
        boolean isActivated
) implements CustomPacketPayload {
    public static final Type<ClientMirrorDataPacketPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "client_mirror_data_payload")
    );
    public static final StreamCodec<ByteBuf, ClientMirrorDataPacketPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            UUIDUtil.CODEC_SET.optionalFieldOf("hiddenPlayers")
                                    .forGetter(ClientMirrorDataPacketPayload::hiddenPlayers),
                            Codec.unboundedMap(UUIDUtil.CODEC, UUIDUtil.CODEC).optionalFieldOf("mirrorMap")
                                    .forGetter(ClientMirrorDataPacketPayload::mirrorMap),
                            UUIDUtil.CODEC.optionalFieldOf("mirror")
                                    .forGetter(ClientMirrorDataPacketPayload::mirror),
                            Codec.BOOL.fieldOf("isActivated")
                                    .forGetter(ClientMirrorDataPacketPayload::isActivated)
                    ).apply(instance, ClientMirrorDataPacketPayload::new)
            )
    );

    public ClientMirrorDataPacketPayload(
            Set<UUID> hiddenPlayers,
            Map<UUID, UUID> mirrorMap,
            UUID mirror,
            boolean isActivated
    ) {
        this(Optional.ofNullable(hiddenPlayers), Optional.ofNullable(mirrorMap), Optional.ofNullable(mirror), isActivated);
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
