package io.github.jixingdefeng.visionrealm.common.erosion.manager.biome;

import io.github.jixingdefeng.visionrealm.common.erosion.manager.infection.block.BlockErosionEntryManager;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages biome-to-erosion-type mappings loaded from data packs.
 * <p>
 * This class maintains a cache of which erosion type applies to each biome,
 * used during erosion key initialization to convert biome conditions to
 * erosion types.
 *
 * @author JiXingDeFeng
 * @see ErosionType
 * @see BlockErosionEntryManager
 * @since 0.0.1-dev
 */
public class BiomeErosionManager {
    public static final String jsonPath = "erosion/config";
    public static final String jsonName = "biome_erosion_types";
    private static BiomeErosionManager INSTANCE;
    protected final Map<ResourceKey<Biome>, ErosionType> BIOME_EROSION_TYPE = new HashMap<>();

    /**
     * Returns the singleton instance of the particle config manager.
     *
     * @return An Optional containing the manager instance if initialized, empty otherwise
     */
    public static Optional<BiomeErosionManager> getInstance() {
        return Optional.ofNullable(INSTANCE);
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

    /**
     * Performs a heavy (bulk) load of biome-to-erosion-type mappings into the cache.
     * <p>
     * This method clears the existing cache and populates it with new mappings.
     *
     * @param map A map containing biome-to-erosion-type mappings
     */
    public void start(Map<ResourceKey<Biome>, ErosionType> map) {
        if (this.BIOME_EROSION_TYPE.isEmpty()) {
            this.BIOME_EROSION_TYPE.putAll(map);
            this.open();
        } else {
            throw new IllegalStateException("Already started");
        }
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
     * Retrieves the erosion types associated with the given biome.
     *
     * @param name The biome resource key
     * @return A list of erosion types for the biome, or a list containing {@link ErosionType#NONE} if not found
     */
    public ErosionType getErosionType(ResourceKey<Biome> name) {
        return Objects.requireNonNullElse(this.BIOME_EROSION_TYPE.get(name), ErosionType.NONE);
    }

    /**
     * Cleans up resources when the server stops.
     * <p>
     * This method is called during server shutdown to release any resources
     * and perform necessary cleanup operations.
     */
    protected void close() {
        // Cleanup logic to be implemented
    }

    /**
     * Sets this instance as the active singleton instance.
     * <p>
     * This method is called internally after the manager has been successfully
     * initialized or reloaded with new mappings.
     * <p>
     * If an existing instance is active, it will be closed before the new
     * instance takes over.
     */
    protected void open() {
        if (INSTANCE != null) {
            INSTANCE.close();
        }

        INSTANCE = this;
    }
}
