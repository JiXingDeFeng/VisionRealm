package io.github.jixingdefeng.visionrealm.common.manager.erosion.infection.block;

import io.github.jixingdefeng.visionrealm.api.data.erosion.block.BlockErosionKey;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.erosion.config.BlockErosionConfig;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.config.block.BlockErosionConfigManager;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.BaseBlockErosionKey;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages erosion keys for blocks, providing weighted random selection.
 * <p>
 * This class maintains a registry of erosion keys per block, each with associated
 * weights for random selection. It also supports fallback to hardcoded keys and
 * respects priority modes defined in {@link BlockErosionConfig}.
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Weighted random selection from multiple erosion keys</li>
 *   <li>Support for hardcoded fallback keys</li>
 *   <li>Priority mode configuration (hardcoded-first, datapack-first, etc.)</li>
 *   <li>Integration with {@link BlockErosionConfigManager} for per-block settings</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see BlockErosionKey
 * @see BlockErosionConfig
 * @see BlockErosionConfigManager
 * @since 0.0.1-dev-1
 */
public class BlockErosionKeyManager {
    protected static final Map<Block, CanBeErosionBlock<?,?>> HARDCODED_CACHE = new ConcurrentHashMap<>();
    protected static BlockErosionKeyManager INSTANCE;
    protected final Map<Block, WeightedRandomList<BlockErosionKey<?, ?>>> BLOCK_EROSION_KEY_MAP = new ConcurrentHashMap<>();
    protected final RandomSource random = RandomSource.create();

    /**
     * Returns the singleton instance of the block erosion key manager.
     *
     * @return The manager instance, or {@code null} if not yet initialized or already stopped
     */
    public static BlockErosionKeyManager getInstance() {
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
     * Caches hardcoded erosion keys from the block registry.
     * <p>
     * This method iterates through all registered blocks and adds any block that
     * implements {@link CanBeErosionBlock} to the hardcoded cache. It should be
     * called during mod initialization or registry events to populate the
     * hardcoded fallback keys.
     *
     * @param event The registry event containing the block registry
     */
    @ApiStatus.Internal
    public static void cacheRegistry(ModifyRegistriesEvent event) {
        Registry<Block> registry = event.getRegistry(Registries.BLOCK);
        for (Block block : registry) {
            if (block instanceof CanBeErosionBlock<?,?> canBeErosion) {
                HARDCODED_CACHE.put(block, canBeErosion);
            }
        }
    }

    /**
     * Loads and initializes erosion keys from multiple configuration maps.
     * <p>
     * This method processes all loaded erosion key definitions, initializes them,
     * and organizes them by source block for efficient retrieval.
     *
     * @param keys A collection of maps containing block-to-erosion-key mappings
     */
    @ApiStatus.Internal
    public void initializeKeys(Collection<Map<Block, BaseBlockErosionKey<Object, Object>>> keys) {
        Map<Block, List<BlockErosionKey<?, ?>>> newKeys = new HashMap<>();
        for (Map<Block, BaseBlockErosionKey<Object, Object>> map : keys) {
            for (Map.Entry<Block, BaseBlockErosionKey<Object, Object>> entry : map.entrySet()) {
                BlockErosionKey<?, ?> key = entry.getValue();
                newKeys.computeIfAbsent(entry.getKey(), type -> new ArrayList<>()).add(key);
            }
        }

        newKeys.forEach((block, blockKeyList) ->
                this.BLOCK_EROSION_KEY_MAP.put(block, WeightedRandomList.create(blockKeyList))
        );
        INSTANCE = this;
    }

    /**
     * Checks whether any erosion keys exist for the specified source block.
     * <p>
     * The check respects the priority mode:
     * <ul>
     *   <li>{@code DISABLED}: always returns {@code false}</li>
     *   <li>{@code HARDCODED_ONLY}: checks only hardcoded cache</li>
     *   <li>{@code DATAPACK_ONLY}: checks only datapack keys</li>
     *   <li>{@code DATAPACK_FIRST} / {@code HARDCODED_FIRST}: checks both</li>
     * </ul>
     *
     * @param source       The source block to check
     * @param priorityMode The priority mode determining which sources to check
     * @return {@code true} if at least one erosion key exists according to the priority mode
     */
    public boolean containsKey(Block source, BlockErosionConfig.PriorityMode priorityMode) {
        if (priorityMode == null) {
            return false;
        }

        return switch (priorityMode) {
            case DATAPACK_ONLY -> this.BLOCK_EROSION_KEY_MAP.containsKey(source);
            case HARDCODED_ONLY -> HARDCODED_CACHE.containsKey(source);
            case DATAPACK_FIRST, HARDCODED_FIRST -> this.BLOCK_EROSION_KEY_MAP.containsKey(source) || HARDCODED_CACHE.containsKey(source);
            default -> false;
        };
    }

    /**
     * Retrieves a random erosion key for the specified source block.
     * <p>
     * This method uses the priority mode defined in the block's configuration.
     * If no configuration exists, it defaults to {@link BlockErosionConfig.PriorityMode#HARDCODED_FIRST}.
     *
     * @param source The source block to get an erosion key for
     * @return A randomly selected erosion key, or {@code null} if none available
     */
    @Nullable
    public CanBeErosionBlock<?, ?> get(Block source) {
        BlockErosionConfig config = BlockErosionConfigManager.getConfig(source);
        BlockErosionConfig.PriorityMode mode = config != null
                ? config.priorityMode()
                : BlockErosionConfig.PriorityMode.HARDCODED_FIRST;
        return this.containsKey(source, mode)
                ? this.get(source, mode)
                : null;
    }

    /**
     * Retrieves an erosion key for the specified source block according to the given priority mode.
     *
     * @param source       The source block to get an erosion key for
     * @param priorityMode The priority mode determining retrieval behavior
     * @return A randomly selected erosion key, or {@code null} if none available
     */
    @Nullable
    public CanBeErosionBlock<?, ?> get(Block source, BlockErosionConfig.PriorityMode priorityMode) {
        return switch (priorityMode) {
            case HARDCODED_ONLY -> HARDCODED_CACHE.get(source);
            case DATAPACK_ONLY -> this.get(source, this.random, false);
            case DATAPACK_FIRST -> this.get(source, this.random, true);
            case HARDCODED_FIRST -> HARDCODED_CACHE.containsKey(source)
                    ? HARDCODED_CACHE.get(source)
                    : this.get(source, this.random, false);
            case DISABLED -> null;
        };
    }

    /**
     * Retrieves an erosion key from the datapack, with an optional fallback to hardcoded cache.
     * <p>
     * This method first attempts to get a random key from the datapack (weighted list).
     * If none exists and {@code useCacheBackup} is {@code true}, it falls back to the hardcoded cache.
     *
     * @param source          The source block to get an erosion key for
     * @param random          The random source for weighted selection
     * @param useCacheBackup  Whether to fall back to hardcoded cache if datapack has no keys
     * @return A randomly selected erosion key, or {@code null} if none available
     */
    @Nullable
    public CanBeErosionBlock<?, ?> get(Block source, RandomSource random, boolean useCacheBackup) {
        WeightedRandomList<BlockErosionKey<?, ?>> randomList = this.BLOCK_EROSION_KEY_MAP.get(source);
        if (randomList != null) {
            return randomList.getRandom(random).orElse(null);
        }

        if (useCacheBackup) {
            return HARDCODED_CACHE.get(source);
        }

        return null;
    }
}
