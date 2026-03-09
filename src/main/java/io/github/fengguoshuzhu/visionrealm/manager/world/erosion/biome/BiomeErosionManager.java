package io.github.fengguoshuzhu.visionrealm.manager.world.erosion.biome;

import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BiomeErosionManager {
    private static BiomeErosionManager instance;
    private final Map<ResourceKey<Biome>, ErosionType> CACHE = new HashMap<>();

    /**
     * Returns the singleton instance of the block erosion key manager.
     *
     * @return The manager instance, or {@code null} if not yet initialized or already stopped
     */
    public static BiomeErosionManager getInstance() {
        return instance;
    }

    /**
     * Handles server stopping to clean up the manager instance.
     * <p>
     * <b>Note:</b> This method is automatically called during server shutdown
     * and should not be invoked manually elsewhere.
     *
     * @param event The server stopping event
     */
    public static void serverStopping(ServerStoppingEvent event) {
        instance = null;
    }

    public void heavyLoad(Map<ResourceKey<Biome>, ErosionType> map) {
        this.CACHE.putAll(map);
        instance = this;
    }

    public ErosionType getErosionType(ResourceKey<Biome> name) {
        return Objects.requireNonNullElse(this.CACHE.get(name), ErosionType.NONE);
    }
}
