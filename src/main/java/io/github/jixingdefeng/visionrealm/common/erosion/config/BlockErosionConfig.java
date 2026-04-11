package io.github.jixingdefeng.visionrealm.common.erosion.config;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration for a block's erosion behavior.
 * <p>
 * Defines which priority mode should be used when selecting erosion keys
 * for a specific block.
 *
 * <p><b>JSON Example:</b>
 * <pre>
 * {
 *   "block": "minecraft:stone",
 *   "mode": "DATAPACK_FIRST"
 * }
 * </pre>
 *
 * @author JiXingDeFeng
 * @since 0.0.1-dev-1
 */
public class BlockErosionConfig {
    public static final Codec<BlockErosionConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockErosionConfig::source),
                    BlockErosionConfig.PriorityMode.CODEC.optionalFieldOf("mode", BlockErosionConfig.PriorityMode.HARDCODED_FIRST).forGetter(BlockErosionConfig::priorityMode)

            ).apply(instance, BlockErosionConfig::new)
    );
    private final Block source;
    private final PriorityMode priorityMode;

    protected BlockErosionConfig(Block block, PriorityMode priorityMode) {
        this.source = block;
        this.priorityMode = priorityMode;
    }

    public Block source() {
        return this.source;
    }

    /**
     * Checks whether hardcoded erosion keys are enabled for this block.
     *
     * @return {@code false} if mode is {@link PriorityMode#DATAPACK_ONLY}, {@code true} otherwise
     */
    public boolean enableImp() {
        return this.priorityMode() != PriorityMode.DATAPACK_ONLY;
    }

    /**
     * Checks whether datapack erosion keys are enabled for this block.
     *
     * @return {@code false} if mode is {@link PriorityMode#HARDCODED_ONLY}, {@code true} otherwise
     */
    public boolean enableDataPack() {
        return this.priorityMode() != PriorityMode.HARDCODED_ONLY;
    }

    public PriorityMode priorityMode() {
        return this.priorityMode;
    }

    /**
     * Priority mode determining how erosion keys are selected.
     * <ul>
     *   <li><b>DATAPACK_FIRST</b> - Try datapack keys first, fall back to hardcoded</li>
     *   <li><b>HARDCODED_FIRST</b> - Try hardcoded keys first, fall back to datapack</li>
     *   <li><b>DATAPACK_ONLY</b> - Use only datapack keys</li>
     *   <li><b>HARDCODED_ONLY</b> - Use only hardcoded keys</li>
     * </ul>
     */
    public enum PriorityMode implements StringRepresentable {
        DATAPACK_FIRST("DATAPACK_FIRST"),
        HARDCODED_FIRST("HARDCODED_FIRST"),
        DATAPACK_ONLY("DATAPACK_ONLY"),
        HARDCODED_ONLY("HARDCODED_ONLY"),
        DISABLED("DISABLED");

        public static final Codec<PriorityMode> CODEC = StringRepresentable.fromEnum(PriorityMode::values);
        private final String name;

        PriorityMode(String name) {
            this.name = name;
        }

        public static PriorityMode byName(String name) {
            return CODEC.decode(JsonOps.INSTANCE, new JsonObject())
                    .result()
                    .orElseThrow().getFirst();
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
