package io.github.jixingdefeng.visionrealm.core.hook.player;

import com.mojang.authlib.GameProfile;
import io.github.jixingdefeng.visionrealm.content.world.entity.player.MirrorPlayer;
import io.github.jixingdefeng.visionrealm.core.world.entity.player.MirrorPlayerFactory;
import io.github.jixingdefeng.visionrealm.mixin.world.entity.player.PlayerMirrorStorageMixin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Extension interface for {@link Player} to store mirror system related data.
 * <p>
 * This interface is implemented via Mixin on {@link Player} and provides storage for:
 * <ul>
 *   <li>A set of players that are currently hidden (invisible) to this player</li>
 *   <li>The current {@link MirrorPlayer} instance associated with this player, stored as {@link GameProfile}</li>
 *   <li>A dirty flag indicating whether the mirror state needs synchronization</li>
 * </ul>
 * <p>
 * All data is stored directly on the player instance, ensuring proper lifecycle
 * management and automatic cleanup when the player is removed.
 * <p>
 * When the hidden list or mirror state changes, the data is automatically
 * synchronized to the client via {@link MirrorPlayerFactory#sync(ServerPlayer, Set, UUID)}.
 *
 * @see PlayerMirrorStorageMixin
 * @see MirrorPlayerFactory
 * @since 0.1.0
 */
public interface PlayerMirrorStorageHook {

    /**
     * Adds a player UUID to the hidden list.
     * <p>
     * Players in this list will be invisible to this player, meaning they should
     * not be rendered or interactable.
     * <p>
     * This method automatically triggers network synchronization to the client.
     *
     * @param player the UUID of the player to hide
     */
    void addHiddenPlayer(UUID player);

    /**
     * Adds multiple player UUIDs to the hidden list.
     * <p>
     * This is a batch version of {@link #addHiddenPlayer(UUID)} for efficiency
     * when multiple players become hidden simultaneously.
     *
     * @param players the collection of UUIDs to hide
     */
    void addHiddenPlayerAll(Collection<UUID> players);

    /**
     * Removes a player UUID from the hidden list.
     * <p>
     * After removal, the player will become visible to this player again.
     * <p>
     * This method automatically triggers network synchronization to the client.
     *
     * @param player the UUID of the player to unhide
     */
    void removeHiddenPlayerAll(UUID player);

    /**
     * Removes multiple player UUIDs from the hidden list.
     * <p>
     * This is a batch version of {@link #removeHiddenPlayerAll(UUID)} for efficiency.
     *
     * @param players the collection of UUIDs to unhide
     */
    void removeHiddenPlayerAll(Collection<UUID> players);

    /**
     * Returns an immutable view of the hidden players set.
     * <p>
     * The returned set cannot be modified directly. Use the add/remove methods
     * to modify the underlying data.
     *
     * @return an unmodifiable set of hidden player UUIDs
     */
    Set<UUID> getHiddenPlayers();

    /**
     * Associates a mirror player with this player.
     * <p>
     * Each player can have at most one mirror player at a time. If a mirror
     * already exists and its corresponding player entity is still present in
     * the world, the new mirror will be rejected. If the existing mirror
     * player has been removed from the world, the new mirror will replace it.
     * <p>
     * The mirror is stored as a {@link GameProfile} containing both the UUID
     * and name of the mirror player. This allows the client to resolve the
     * correct skin texture and display name.
     * <p>
     * This method sets a dirty flag, and the next call to
     * {@link MirrorPlayerFactory#sync(ServerPlayer, Set, UUID)} will synchronize
     * the state to the client.
     *
     * @param mirrorPlayer the mirror player to associate
     */
    void addMirrorPlayer(MirrorPlayer mirrorPlayer);

    /**
     * Removes the current mirror player association.
     * <p>
     * After calling this method:
     * <ul>
     *   <li>{@link #getMirrorPlayer()} will return {@code null}</li>
     *   <li>The dirty flag will be set, triggering a sync to the client</li>
     * </ul>
     */
    void removeMirrorPlayer();

    /**
     * Returns the {@link GameProfile} of the mirror player associated with this player.
     * <p>
     * The GameProfile contains both the UUID and the display name of the mirror,
     * enabling proper skin resolution and name rendering on the client side.
     *
     * @return the mirror player's GameProfile, or {@code null} if not set
     */
    @Nullable
    GameProfile getMirrorPlayer();
}
