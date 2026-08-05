package io.github.jixingdefeng.visionrealm.core.erosion.block_erossion;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.erosion.block.ErodibleBlock;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.util.random.ArrayWeightRandomList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * Global registry of block erosion entries loaded from data packs,
 * with weighted random selection and hardcoded fallback support.
 * <p>
 * Each block is mapped to a map of {@link ArrayWeightRandomList} entries,
 * keyed by {@link ErosionType}. When an erosion event occurs, the store
 * returns a randomly selected entry based on configured weights, optionally
 * falling back to blocks that implement {@link ErodibleBlock} directly.
 * <p>
 * <b>Lifecycle:</b><br>
 * The registry is populated synchronously via {@link #create(Map, Map)}
 * during data-pack reload (server startup or {@code /reload}), replacing
 * all existing entries. On server stop, {@link #close()} clears the data.
 *
 * <p><b>Key behaviors:</b>
 * <ul>
 *   <li>{@link #getEntryAndValidate(Block, Level, BlockPos, ErosionType)} –
 *       prioritises data-pack entries that pass a positional predicate;
 *       falls back to hardcoded entries if the data-pack entry is inapplicable
 *       and {@code allowHardCoded()} is {@code true}.</li>
 *   <li>{@link #getEntry(Block, ErosionType)} – returns a random entry from
 *       the weighted list for a block and erosion type.</li>
 *   <li>{@link #containsKey(Block)} – reports whether any erosion configuration
 *       is available for a block.</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see BlockErosionEntry
 * @see ArrayWeightRandomList
 * @since 0.1.0
 */
public class BlockErosionManager {
    public static final String PATH = VisionRealm.MOD_ID + "/erosion/block_entry";
    public static final Function<Block, @Nullable ErodibleBlock<?, ?>> HARDCODED =
            block -> block instanceof ErodibleBlock<?,?> entry ? entry : null;
    private static final Object2ObjectOpenHashMap<Block, Map<ErosionType, ArrayWeightRandomList<BlockErosionEntry<?, ?>>>> ENTRY_MAP = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, BlockPredicate> PREDICATES = new Object2ObjectOpenHashMap<>();
    private static final RandomSource random = RandomSource.create();

    /**
     * Clears all loaded erosion entries and predicates from the registry.
     * <p>
     * This method is automatically called during server shutdown to release
     * resources and should not be invoked manually elsewhere.
     */
    @ApiStatus.Internal
    public static void close() {
        ENTRY_MAP.clear();
        PREDICATES.clear();
    }

    /**
     * Populates the global erosion registry with the given data.
     * <p>
     * This method is intended for internal use by the data pack reload listener.
     * It does not support incremental updates; each call replaces the entire
     * configuration with the provided data.
     *
     * @param blockMapMap  Map of blocks to erosion type entries
     * @param predicateMap Map of predicate resource locations to their predicates
     */
    public static void create(
            Map<Block, Map<ErosionType, ArrayWeightRandomList<BlockErosionEntry<?, ?>>>> blockMapMap,
            Map<ResourceLocation, BlockPredicate> predicateMap
    ) {
        close();
        ENTRY_MAP.putAll(blockMapMap);
        PREDICATES.putAll(predicateMap);
    }

    /**
     * Returns whether an erosion configuration exists for the given block.
     *
     * @param source the block to check
     * @return {@code true} if any erosion entry exists for the block
     */
    public static boolean containsKey(Block source) {
        return ENTRY_MAP.containsKey(source);
    }

    /**
     * Retrieves a random erosion entry that is applicable at the specified position.
     * <p>
     * This method first retrieves an entry from the data pack. If the entry exists
     * but is not applicable at the given position (based on its predicate), it falls
     * back to hardcoded erosion if {@link BlockErosionEntry#allowHardCoded()} is
     * {@code true}.
     *
     * @param source The source block
     * @param level  The level
     * @param pos    The position
     * @param type   The erosion type
     * @return An applicable erosion entry, or {@code null} if none available
     */
    @Nullable
    public static ErodibleBlock<?, ?> getEntryAndValidate(Block source, Level level, BlockPos pos, ErosionType type) {
        BlockErosionEntry<?, ?> entry = getEntry(source, type);
        if (entry != null) {
            if (entry.isApplicable(level, pos)) {
                return entry;
            } else if (entry.allowHardCoded()) {
                return HARDCODED.apply(source);
            }
        }

        return null;
    }

    /**
     * Retrieves a random erosion entry from the data pack for the specified
     * source block and erosion type.
     *
     * @param source The source block
     * @param type   The erosion type
     * @return A randomly selected erosion entry, or {@code null} if none available
     */
    @Nullable
    public static BlockErosionEntry<?, ?> getEntry(Block source, ErosionType type) {
        Map<ErosionType, ArrayWeightRandomList<BlockErosionEntry<?, ?>>> listMap = ENTRY_MAP.get(source);
        if (listMap != null) {
            ArrayWeightRandomList<BlockErosionEntry<?, ?>> list = listMap.get(type);
            if (list != null) {
                return list.getRandom(random).orElse(null);
            }
        }

        return null;
    }

    /**
     * Retrieves a block predicate by its resource location.
     *
     * @param location The resource location of the predicate
     * @return The block predicate, or {@code null} if not found
     */
    @Nullable
    public static BlockPredicate getPredicate(ResourceLocation location) {
        return PREDICATES.get(location);
    }
}
