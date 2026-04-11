package io.github.jixingdefeng.visionrealm.api.event.erosion.block;

import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.data.erosion.block.BlockErosionReloadListener;
import io.github.jixingdefeng.visionrealm.impl.erosion.infection.block.BaseBlockErosionKey;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event fired during the data loading phase to register block erosion loaders.
 * <p>
 * This event allows other mods to register custom block erosion data loaders by
 * specifying a path and a codec. The registered loaders will be used during
 * resource reloading to parse and load erosion key definitions from JSON files.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegisterLoaders(BlockErosionLoaderRegisterEvent event) {
 *     event.registryLoader("erosion/block_erosion/custom", CustomBlockErosionKey.CODEC);
 * }
 * }</pre>
 *
 * <p><b>Note:</b> Paths are relative to the data pack root. Duplicate paths will be
 * logged but not prevented (last registration wins).</p>
 *
 * @author JiXingDeFeng
 * @see BlockErosionReloadListener
 * @since 0.0.1-dev-1
 */
public class BlockErosionLoaderRegisterEvent extends Event {
    private final List<PendingLoader> pendingLoaders = new ArrayList<>();

    public BlockErosionLoaderRegisterEvent() {
    }

    /**
     * Registers a block erosion loader for the specified path.
     *
     * @param path  The resource path where JSON files are located (e.g., "erosion/block_erosion/custom")
     * @param codec The codec used to decode JSON files into {@link BaseBlockErosionKey} instances
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registryLoader(String path, Codec codec) {
        try {
            this.pendingLoaders.add(new PendingLoader("erosion/block_erosion/" + path, (Codec<BaseBlockErosionKey<?, ?>>) codec));
        } catch (Exception e) {
            VisionRealm.LOGGER.error("Failed to load block erosion loader for path '{}': codec mismatch", path, e);
        }
    }

    /**
     * Returns an immutable copy of the currently registered pending loaders.
     *
     * @return A new list containing all registered loaders
     */
    public List<PendingLoader> getPendingLoaders() {
        return new ArrayList<>(this.pendingLoaders);
    }

    /**
     * Represents a pending block erosion loader registration.
     *
     * @param path  The resource path for JSON files
     * @param codec The codec for decoding {@link BaseBlockErosionKey} instances
     */
    public record PendingLoader(String path, Codec<BaseBlockErosionKey<?, ?>> codec) {
    }
}
