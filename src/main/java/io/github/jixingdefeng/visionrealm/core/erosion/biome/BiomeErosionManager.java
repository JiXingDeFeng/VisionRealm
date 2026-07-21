package io.github.jixingdefeng.visionrealm.core.erosion.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block.BlockErosionEntryStore;
import io.github.jixingdefeng.visionrealm.core.network.protocol.BiomeErosionTypePayload;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import io.github.jixingdefeng.visionrealm.core.server.packs.resources.erosion.biome.BiomeErosionReloadListener;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Manages biome-to-erosion-type mappings loaded from data packs.
 * <p>
 * Maintains a lookup table from {@link ResourceKey}<{@link Biome}> to
 * {@link BiomeConfig}, used throughout the erosion system when a biome
 * condition needs to be resolved to a concrete erosion type and its
 * associated parameters. The data is loaded from the data pack at
 * {@code data/<modid>/visionrealm/erosion/config/biome_erosion_types.json}.
 * <p>
 * The configuration supports incremental loading: multiple data packs
 * can contribute entries, with the {@code replace} flag controlling
 * whether existing entries should be cleared before applying new ones.
 *
 * <p><b>JSON Structure:</b>
 * <pre>{@code
 * {
 *   "replace": false,
 *   "values": {
 *     "minecraft:plains": {
 *       "erosion_rate": 0.01,
 *       "erosion_strength": 0.005,
 *       "recovery_rate": 0.3,
 *       "erosion_type": "visionrealm:blood"
 *     },
 *     "minecraft:desert": {
 *       "erosion_rate": 0.02,
 *       "erosion_strength": 0.01,
 *       "recovery_rate": 0.1,
 *       "erosion_type": "visionrealm:curse"
 *     }
 *   },
 *   "remove": [
 *     "minecraft:forest"
 *   ]
 * }
 * }</pre>
 *
 * <ul>
 *   <li>{@code replace}: If {@code true}, clears all existing entries before applying new ones.</li>
 *   <li>{@code values}: Map of biome keys to their erosion configurations.</li>
 *   <li>{@code remove}: List of biome keys to remove from the current configuration.</li>
 * </ul>
 *
 * <p><b>BiomeConfig Fields:</b></p>
 * <ul>
 *   <li>{@code erosion_rate}: Speed of erosion accumulation. Actual interval
 *       is calculated as {@code erosion_rate * 1000} in ticks.</li>
 *   <li>{@code erosion_strength}: Multiplier for erosion strength.</li>
 *   <li>{@code recovery_rate}: Speed of natural erosion recovery. Actual interval
 *       is calculated as {@code recovery_rate * 1000} in ticks.</li>
 *   <li>{@code erosion_type}: Resource key of the erosion type to apply.</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see BiomeConfig
 * @see BiomeErosionReloadListener.BiomeErosionDataPack
 * @see ErosionType
 * @see BlockErosionEntryStore
 * @since 0.0.1-dev
 */
public class BiomeErosionManager {
    public static final String JSON_PATH = VisionRealm.MOD_ID + "/erosion/config";
    public static final String JSON_NAME = "biome_erosion_types";
    public static Codec<BiomeConfig> CONFIG_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("erosion_rate", 0.01F)
                            .forGetter(BiomeConfig::erosionRate),
                    Codec.DOUBLE.optionalFieldOf("erosion_strength", 0.00125)
                            .forGetter(BiomeConfig::erosionStrength),
                    Codec.FLOAT.optionalFieldOf("recovery_rate", 0.3F)
                                    .forGetter(BiomeConfig::recoveryRate),
                    Codec.DOUBLE.optionalFieldOf("reduce_amount", 0.0025)
                                    .forGetter(BiomeConfig::reduceAmount),
                    RegistryFixedCodec.create(ModRegistries.EROSION_TYPE)
                            .fieldOf("erosion_type")
                            .forGetter(BiomeConfig::erosionType)
            ).apply(instance, BiomeConfig::new)
    );
    private static volatile BiomeErosionManager INSTANCE;
    protected final Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> erosionTypeMap;

    public static Optional<BiomeErosionManager> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    /**
     * Checks whether the manager has been initialized and is available for use.
     *
     * @return {@code true} if the manager instance exists, {@code false} otherwise
     */
    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    /**
     * Creates a new biome erosion type manager instance, populates it with the given mappings,
     * and activates it as the global singleton, replacing any existing instance.
     * <p>
     * This method is intended for internal use by the data pack reload listener.
     * It does not support incremental updates; each call replaces the entire configuration.
     *
     * @param map A map containing biome-to-erosion-type mappings (biome key → erosion type key)
     */
    public static void start(Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> map) {
        new BiomeErosionManager(map).open();
    }

    /**
     * Handles server stopping to clean up the manager instance.
     * <p>
     * <b>Note:</b> This method is automatically called during server shutdown
     * and should not be invoked manually elsewhere.
     *
     * @param event The server stopping event
     */
    @ApiStatus.Internal
    public static void close(ServerStoppingEvent event) {
        getInstance().ifPresent(BiomeErosionManager::close);
        INSTANCE = null;
    }

    protected BiomeErosionManager(Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> map) {
        this.erosionTypeMap = new Object2ObjectOpenHashMap<>(map);
    }

    public boolean containsKey(ResourceKey<Biome> key) {
        return erosionTypeMap.containsKey(key);
    }

    /**
     * Retrieves the erosion configuration for the given biome.
     *
     * @param key The biome resource key
     * @return The biome configuration, or {@code null} if not found
     */
    @Nullable
    public BiomeConfig getBiomeConfig(ResourceKey<Biome> key) {
        return erosionTypeMap.get(key);
    }

    /**
     * Sends the current biome erosion mappings to a specific player.
     *
     * @param player the target player
     */
    public void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, BiomeErosionTypePayload.create(this.erosionTypeMap));
    }

    /**
     * Broadcasts the current biome erosion mappings to all connected players.
     * <p>
     * This operation is only executed on the server side; if the current environment
     * does not have a running server (e.g., on the client), the method does nothing.
     */
    public void sync() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            PacketDistributor.sendToAllPlayers(BiomeErosionTypePayload.create(this.erosionTypeMap));
        }
    }

    /**
     * Configuration data for biome-specific erosion behavior.
     *
     * @param erosionRate     Erosion speed. Actual interval between erosion increases
     *                        is calculated as {@code erosionRate * 1000} in ticks.
     * @param erosionStrength Erosion strength multiplier
     * @param recoveryRate    Recovery speed (erosion decrease). Actual interval between
     *                        recovery events is calculated as {@code recoveryRate * 1000} in ticks.
     * @param erosionType     The erosion type applied to this biome
     * @since 0.0.3-dev
     */
    public record BiomeConfig(
            float erosionRate, double erosionStrength, float recoveryRate, double reduceAmount, Holder<ErosionType> erosionType
    ) {
    }

    /**
     * Sets this instance as the active singleton instance.
     * <p>
     * This method is called internally after the manager has been successfully
     * initialized or reloaded with new mappings.
     * <p>
     * The new instance becomes active immediately, and the existing instance
     * (if any) is closed afterward. This ensures that {@link #getInstance()}
     * never returns {@code null} during the replacement process.
     * <p>
     * If invoked on the server side, after becoming active it broadcasts the updated
     * biome erosion mappings to all connected clients by calling {@link #sync()}.
     */
    protected void open() {
        BiomeErosionManager old = INSTANCE;
        INSTANCE = this;
        this.sync();
        if (old != null) {
            old.close();
        }
    }

    /**
     * Cleans up resources when the server stops.
     * <p>
     * This method is called during server shutdown to release any resources
     * and perform necessary cleanup operations.
     */
    protected void close() {
        this.erosionTypeMap.clear();
    }
}
