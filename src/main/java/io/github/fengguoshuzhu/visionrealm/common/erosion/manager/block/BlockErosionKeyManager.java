package io.github.fengguoshuzhu.visionrealm.common.erosion.manager.block;

import io.github.fengguoshuzhu.visionrealm.impl.erosion.block.BaseBlockErosionKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.*;

public class BlockErosionKeyManager {
    protected static BlockErosionKeyManager instance;
    protected final Map<Block, WeightedRandomList<BaseBlockErosionKey<?, ?>>> BLOCK_EROSION_KEY_MAP = new HashMap<>();
    protected final RandomSource random = RandomSource.create();

    /**
     * Returns the singleton instance of the block erosion key manager.
     *
     * @return The manager instance, or {@code null} if not yet initialized or already stopped
     */
    public static BlockErosionKeyManager getInstance() {
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

    /**
     * Loads and initializes erosion keys from multiple configuration maps.
     * <p>
     * This method processes all loaded erosion key definitions, initializes them,
     * and organizes them by source block for efficient retrieval.
     *
     * @param keys A collection of maps containing block-to-erosion-key mappings
     */
    public void loadFromMap(Collection<Map<Block, BaseBlockErosionKey<Object, Object>>> keys) {
        Map<Block, List<BaseBlockErosionKey<?, ?>>> newKeys = new HashMap<>();
        for (Map<Block, BaseBlockErosionKey<Object, Object>> map : keys) {
            for (Map.Entry<Block, BaseBlockErosionKey<Object, Object>> entry : map.entrySet()) {
                BaseBlockErosionKey<?, ?> key = entry.getValue();
                if (!key.isInitialized()) key.init();
                newKeys.computeIfAbsent(entry.getKey(), type -> new ArrayList<>())
                        .add(key);
            }
        }

        newKeys.forEach((block, blockKeyList) ->
                this.BLOCK_EROSION_KEY_MAP.put(block, WeightedRandomList.create(blockKeyList))
        );
        instance = this;
    }

    /**
     * Retrieves a random erosion key for the specified source block.
     * <p>
     * The returned key is selected randomly from the weighted list
     * associated with the block, if any exists.
     *
     * @param source The source block to get an erosion key for
     * @return A randomly selected erosion key, or {@code null} if none available
     */
    public BaseBlockErosionKey<?, ?> get(Block source) {
        return this.BLOCK_EROSION_KEY_MAP.containsKey(source)
                ? this.BLOCK_EROSION_KEY_MAP.get(source).getRandom(this.random).orElse(null)
                : null;
    }
}
