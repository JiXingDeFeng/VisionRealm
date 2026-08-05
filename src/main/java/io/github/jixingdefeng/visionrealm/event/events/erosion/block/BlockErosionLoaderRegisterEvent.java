package io.github.jixingdefeng.visionrealm.event.events.erosion.block;

import com.mojang.datafixers.util.Function8;
import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.content.data.packs.resources.block_erosion.BlockErosionReloadListener;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.BlockErosionEntry;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.BlockErosionManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Event fired during the data loading phase to register block erosion loaders.
 * <p>
 * This event allows other mods to register custom block erosion data loaders by
 * specifying a path, a target codec, and a factory function. The registered loaders
 * will be used during resource reloading to parse and load erosion entries from JSON files
 * located at {@code visionrealm/erosion/block_entry/[path]/}.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegisterLoaders(BlockErosionLoaderRegisterEvent event) {
 *     event.registryLoader("custom", CustomBlockErosionKey.CODEC, CustomBlockErosionKey::new);
 * }
 * }</pre>
 *
 * <p><b>Note:</b> Paths are relative to the data pack root. Duplicate paths will be
 * logged but not prevented (last registration wins).</p>
 *
 * @author JiXingDeFeng
 * @see BlockErosionReloadListener
 * @since 0.1.0
 */
public class BlockErosionLoaderRegisterEvent extends Event {
    private final List<BlockErosionReloadListener.PendingLoader<?, ?>> pendingLoaders = new ArrayList<>();

    @ApiStatus.Internal
    public BlockErosionLoaderRegisterEvent() {
    }

    /**
     * Registers a block erosion loader for the specified path.
     * <p>
     * The loader will parse JSON files located at {@code visionrealm/erosion/block_entry/[path]/}
     * and deserialize them into {@link BlockErosionEntry} instances using the
     * provided codec and factory function.
     *
     * @param path            The resource path where JSON files are located (e.g., "custom")
     * @param targetCodec     The codec for deserializing the target type
     * @param createFunction  Factory function that creates {@link BlockErosionEntry} instances
     * @param <RT>            The target type (e.g., {@link Block}, {@link EntityType})
     * @param <R>             The final result type after erosion (e.g., {@link BlockState}, {@link Entity})
     */
    public <RT, R> void registryLoader(
            String path,
            Codec<RT> targetCodec,
            Function8<Block, ErosionType, RT, @Nullable ResourceLocation,
                    @Nullable ResourceLocation, Float, Boolean, Weight, BlockErosionEntry<RT, R>> createFunction
    ) {
        try {
            this.pendingLoaders.add(
                    new BlockErosionReloadListener.PendingLoader<>(
                            BlockErosionManager.PATH + '/' + path, targetCodec, createFunction
                    )
            );
        } catch (Exception e) {
            VisionRealm.LOGGER.error("Failed to load block erosion loader for path '{}': codec mismatch", path, e);
        }
    }

    /**
     * Returns an immutable copy of the currently registered pending loaders.
     * <p>
     * The returned list is a shallow copy; modifying it will not affect the
     * internal state of this event.
     *
     * @return A new list containing all registered loaders
     */
    public List<BlockErosionReloadListener.PendingLoader<?, ?>> getPendingLoaders() {
        return new ArrayList<>(this.pendingLoaders);
    }
}
