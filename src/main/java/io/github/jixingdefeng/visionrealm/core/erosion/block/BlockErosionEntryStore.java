package io.github.jixingdefeng.visionrealm.core.erosion.block;

import io.github.jixingdefeng.visionrealm.api.erosion.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block_entry.BaseBlockErosionEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Global registry of block erosion entries loaded from data packs,
 * with weighted random selection and hardcoded fallback support.
 *
 * <p>Each block is mapped to an {@link ArrayWeightRandomList} of
 * {@link BlockErosionEntry} instances (grouped by erosion type internally).
 * When an erosion event occurs, the store returns a randomly selected
 * entry based on configured weights, optionally falling back to blocks
 * that implement {@link CanBeErosionBlock} directly.
 *
 * <p><b>Lifecycle:</b><br>
 * The store is a thread‑safe singleton backed by a {@code volatile} field.
 * It is populated synchronously via {@link #reload(Collection)} during
 * data‑pack reload (server startup or {@code /reload}), which replaces
 * the previous instance atomically. On server stop, {@link #close(ServerStoppingEvent)}
 * clears the data and removes the instance.
 *
 * <p><b>Key behaviors:</b>
 * <ul>
 *   <li>{@link #get(Block, Level, BlockPos, ErosionType)} – prioritises
 *       data‑pack entries that pass a positional predicate; falls back to
 *       hardcoded entries if the data‑pack entry is inapplicable and
 *       {@code useFallback()} is {@code true}.</li>
 *   <li>{@link #get(Block, boolean)} – pure random selection from the
 *       weighted list for a block; the boolean flag controls whether to
 *       fall back to a hardcoded implementation when no data‑pack entry
 *       exists.</li>
 *   <li>{@link #containsKey(Block)} – reports whether any erosion
 *       configuration (data‑pack or hardcoded) is available for a block.</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see BlockErosionEntry
 * @see ArrayWeightRandomList
 * @since 0.0.1‑dev
 */
public class BlockErosionEntryStore {
    public static final String PATH = VisionRealm.MOD_ID + "/erosion/block_entry";
    public static final Function<Block, @Nullable CanBeErosionBlock<?, ?>> HARDCODED =
            block -> block instanceof CanBeErosionBlock<?,?> entry ? entry : null;
    private static volatile BlockErosionEntryStore INSTANCE;
    protected final Object2ObjectOpenHashMap<Block, ArrayWeightRandomList<BlockErosionEntry<?, ?>>> entryMap = new Object2ObjectOpenHashMap<>();
    protected final RandomSource random = RandomSource.create();

    public static Optional<BlockErosionEntryStore> getInstance() {
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
     * Creates a new block erosion entry manager instance, populates it with the given data,
     * and activates it as the global singleton, replacing any existing instance.
     * <p>
     * This method is intended for internal use by the data pack reload listener.
     * It does not support incremental updates; each call replaces the entire configuration.
     *
     * @param collection A collection of maps containing block-to-erosion-key mappings
     */
    public static void reload(Collection<Map<Block, BaseBlockErosionEntry<Object, Object>>> collection) {
        BlockErosionEntryStore manager = new BlockErosionEntryStore();
        manager.addEntry(collection);
        manager.open();
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
        if (INSTANCE != null) {
            INSTANCE.close();
        }

        INSTANCE = null;
    }

    /**
     * Returns {@code true} if the given block has either a datapack erosion entry
     * or implements {@link CanBeErosionBlock} as a hardcoded fallback.
     *
     * @param source the block to check
     * @return {@code true} if an erosion configuration exists for the block
     */
    public boolean containsKey(Block source) {
        return this.entryMap.containsKey(source) || source instanceof CanBeErosionBlock;
    }

    /**
     * Retrieves a random erosion entry that is applicable at the specified position.
     * <p>
     * This method first tries to get an entry from the data pack. If the entry exists
     * but is not applicable at the given position (based on its predicate), it falls back
     * to hardcoded erosion.
     *
     * @param source The source block
     * @param level  The level
     * @param pos    The position
     * @param type   The erosion type
     * @return An applicable erosion entry, or {@code null} if none available
     */
    @Nullable
    public CanBeErosionBlock<?, ?> get(Block source, Level level, BlockPos pos, ErosionType type) {
        BaseBlockErosionEntry<?, ?> entry = (BaseBlockErosionEntry<?, ?>) this.get(source, false);
        if (entry != null) {
            if (entry.isApplicable(level, pos, type)) {
                return entry;
            } else if (!entry.useFallback()) {
                return null;
            }
        }

        return HARDCODED.apply(source);
    }

    /**
     * Retrieves a random erosion entry from the data pack for the specified source block.
     * <p>
     * If {@code fallback} is {@code true} and no data pack entry is found, falls back
     * to checking if the block itself implements {@link CanBeErosionBlock}.
     *
     * @param source       The source block to get an erosion key for
     * @param useHardcoded Whether to fall back to hardcoded erosion if data pack entry is missing
     * @return A randomly selected erosion entry, or {@code null} if none available
     */
    @Nullable
    public CanBeErosionBlock<?, ?> get(Block source, boolean useHardcoded) {
        ArrayWeightRandomList<BlockErosionEntry<?, ?>> randomList = this.entryMap.get(source);
        CanBeErosionBlock<?, ?> value = randomList != null ? randomList.getRandom(this.random).orElse(null) : null;
        return value != null || !useHardcoded ? value : HARDCODED.apply(source);
    }

    /**
     * Processes and stores the loaded erosion entry data.
     * <p>
     * This method attempts to execute on the server main thread via
     * {@link MinecraftServer#execute(Runnable)} if the server is available.
     * If the server is not yet started, it executes directly on the current thread.
     *
     * @param collection A collection of maps containing block-to-erosion-key mappings
     */
    @ApiStatus.Internal
    protected void addEntry(Collection<Map<Block, BaseBlockErosionEntry<Object, Object>>> collection) {
        Map<Block, List<BlockErosionEntry<?, ?>>> entryMap = new HashMap<>();
        for (Map<Block, BaseBlockErosionEntry<Object, Object>> map : collection) {
            map.forEach((block, entry) ->
                    entryMap.computeIfAbsent(block, b -> new ArrayList<>()).add(entry)
            );
        }

        this.entryMap.ensureCapacity(entryMap.size());
        entryMap.forEach((block, list) ->
                this.entryMap.put(block, ArrayWeightRandomList.create(list))
        );
    }

    /**
     * Sets this instance as the active singleton instance.
     * <p>
     * This method is called internally after the manager has been successfully
     * initialized or reloaded with new configurations.
     * <p>
     * The new instance becomes active immediately, and any existing instance
     * is closed afterward. This ensures that {@link #getInstance()} never
     * returns {@code null} during the replacement process.
     */
    protected final void open() {
        BlockErosionEntryStore old = INSTANCE;
        INSTANCE = this;
        if (old != null) {
            old.close();
        }
    }

    /**
     * Cleans up resources when the server stops.
     * <p>
     * This method is called during server shutdown to release any resources
     * and perform necessary cleanup operations.
     * <p>
     * Subclasses should override this method to implement their own cleanup logic.
     */
    protected void close() {
        this.entryMap.clear();
    }
}
