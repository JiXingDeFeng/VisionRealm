package io.github.jixingdefeng.visionrealm.common.manager.erosion.config.block;

import io.github.jixingdefeng.visionrealm.common.erosion.config.BlockErosionConfig;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.infection.block.BlockErosionKeyManager;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages block erosion configuration settings loaded from data packs.
 * <p>
 * This class stores configuration options for each block, such as priority mode
 * (whether to prioritize hardcoded or datapack erosion keys), controlling how
 * erosion keys are selected at runtime.
 *
 * @author JiXingDeFeng
 * @see BlockErosionConfig
 * @see BlockErosionKeyManager
 * @since 0.0.1-dev-1
 */
public class BlockErosionConfigManager {
    public static final String jsonPath = "erosion/config/block";
    protected static BlockErosionConfigManager INSTANCE;
    protected final Map<Block, BlockErosionConfig> BLOCK_CONFIGS = new HashMap<>();

    /**
     * Returns the singleton instance of the block erosion key manager.
     *
     * @return The manager instance, or {@code null} if not yet initialized or already stopped
     */
    public static BlockErosionConfigManager getInstance() {
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
     * Retrieves the erosion configuration for the specified block.
     *
     * @param source The block to get the configuration for
     * @return The configuration for this block, or {@code null} if none exists
     */
    @Nullable
    public static BlockErosionConfig getConfig(Block source) {
        if (INSTANCE != null && INSTANCE.containsKey(source)) {
            return INSTANCE.get(source);
        }

        return null;
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
     * Initializes the manager with a map of block configurations.
     * <p>
     * This method should be called during data pack loading.
     * After initialization, the manager becomes the active singleton instance.
     *
     * @param config A map of block-to-configuration mappings
     */
    @ApiStatus.Internal
    public void initialize(Map<Block, BlockErosionConfig> config) {
        BLOCK_CONFIGS.putAll(config);
        INSTANCE = this;
    }

    /**
     * Checks whether a configuration exists for the specified block.
     *
     * @param key The block to check
     * @return {@code true} if a configuration exists for this block, {@code false} otherwise
     */
    public boolean containsKey(Block key) {
        return BLOCK_CONFIGS.containsKey(key);
    }

    /**
     * Retrieves the erosion configuration for the specified block.
     *
     * @param key The block to get the configuration for
     * @return The configuration for this block, or {@code null} if none exists
     */
    public BlockErosionConfig get(Block key) {
        return BLOCK_CONFIGS.get(key);
    }
}
