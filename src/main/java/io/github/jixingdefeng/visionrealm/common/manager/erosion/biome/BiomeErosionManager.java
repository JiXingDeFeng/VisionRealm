package io.github.jixingdefeng.visionrealm.common.manager.erosion.biome;

import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.infection.block.BlockErosionKeyManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages biome-to-erosion-type mappings loaded from data packs.
 * <p>
 * This class maintains a cache of which erosion type applies to each biome,
 * used during erosion key initialization to convert biome conditions to
 * erosion types.
 *
 * @author JiXingDeFeng
 * @see ErosionType
 * @see BlockErosionKeyManager
 * @since 0.0.1-dev-1
 */
public class BiomeErosionManager {
    public static final String jsonPath = "erosion/biome_erosion_types";
    private static BiomeErosionManager INSTANCE;
    private final Map<ResourceKey<Biome>, ErosionType> BIOME_EROSION_TYPE = new HashMap<>();

    /**
     * Returns the singleton instance of the block erosion key manager.
     *
     * @return The manager instance, or {@code null} if not yet initialized or already stopped
     */
    public static BiomeErosionManager getInstance() {
        return INSTANCE;
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
     * Handles server stopping to clean up the manager instance.
     * <p>
     * <b>Note:</b> This method is automatically called during server shutdown
     * and should not be invoked manually elsewhere.
     *
     * @param event The server stopping event
     */
    @ApiStatus.Internal
    public static void serverStopping(ServerStoppingEvent event) {
        INSTANCE = null;
    }

    /**
     * Performs a heavy (bulk) load of biome-to-erosion-type mappings into the cache.
     * <p>
     * This method clears the existing cache and populates it with new mappings.
     * After loading, it sets itself as the active singleton instance.
     *
     * @param map A map containing biome-to-erosion-type mappings
     */
    @ApiStatus.Internal
    public void initializeCache(Map<ResourceKey<Biome>, ErosionType> map) {
        this.BIOME_EROSION_TYPE.putAll(map);
        INSTANCE = this;
    }

    /**
     * Checks whether an erosion type is registered for the specified biome.
     *
     * @param key The biome resource key to check
     * @return {@code true} if an erosion type exists for this biome, {@code false} otherwise
     */
    public boolean containsKey(ResourceKey<Biome> key) {
        return BIOME_EROSION_TYPE.containsKey(key);
    }

    /**
     * Retrieves the erosion type associated with the given biome.
     *
     * @param name The biome resource key
     * @return The erosion type for the biome, or {@link ErosionType#NONE} if not found
     */
    public ErosionType getErosionType(ResourceKey<Biome> name) {
        return Objects.requireNonNullElse(this.BIOME_EROSION_TYPE.get(name), ErosionType.NONE);
    }
}
