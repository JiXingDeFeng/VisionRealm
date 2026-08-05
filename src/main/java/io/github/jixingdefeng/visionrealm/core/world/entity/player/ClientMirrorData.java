package io.github.jixingdefeng.visionrealm.core.world.entity.player;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientMirrorData {
    private static final Map<UUID, UUID> MIRROR_MAP = new Object2ObjectOpenHashMap<>();
    private static final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private static @Nullable UUID mirrorUUID = null;
    private static boolean isEnable = false;

    public static boolean isHidden(Player player) {
        return hiddenPlayers.contains(player.getUUID());
    }

    @Nullable
    public static UUID getMirrorTarget(UUID uuid) {
        return hiddenPlayers.contains(uuid) ? null : MIRROR_MAP.get(uuid);
    }

    public static Set<UUID> getHiddenPlayers() {
        return Collections.unmodifiableSet(hiddenPlayers);
    }

    public static @Nullable UUID mirrorUUID() {
        return mirrorUUID;
    }

    public static boolean isEnable() {
        return isEnable;
    }

    @ApiStatus.Internal
    public static void updateData(
            @Nullable Set<UUID> butCouldNotBeSeen,
            @Nullable Map<UUID, UUID> mirroredPlayers,
            @Nullable UUID mirror,
            boolean isEnable
    ) {
        if (butCouldNotBeSeen != null) {
            ClientMirrorData.hiddenPlayers.clear();
            ClientMirrorData.hiddenPlayers.addAll(butCouldNotBeSeen);
        }

        if (mirroredPlayers != null) {
            ClientMirrorData.MIRROR_MAP.clear();
            ClientMirrorData.MIRROR_MAP.putAll(mirroredPlayers);
        }

        ClientMirrorData.mirrorUUID = mirror;
        if (mirror != null) {
            ClientMirrorData.isEnable = isEnable;
        } else {
            ClientMirrorData.isEnable = false;
        }
    }
}
