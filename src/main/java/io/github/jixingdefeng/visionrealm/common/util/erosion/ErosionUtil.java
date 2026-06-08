package io.github.jixingdefeng.visionrealm.common.util.erosion;

import io.github.jixingdefeng.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.infection.block.BlockErosionEntryManager;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ErosionUtil {

    public static ErosionType getBiomeErosionType(BlockPos pos, Level level) {
        return getBiomeErosionType(level.getBiome(pos).getKey());
    }

    public static ErosionType getBiomeErosionType(ResourceKey<Biome> biome) {
        Optional<BiomeErosionManager> manager = BiomeErosionManager.getInstance();
        if (manager.isPresent()) {
            return manager.get().getErosionType(biome);
        } else {
            return ErosionType.NONE;
        }
    }

    public static boolean canBeErosion(Block source) {
        return BlockErosionEntryManager.getInstance()
                .map(manager -> manager.containsKey(source))
                .orElse(false);
    }

    @Nullable
    public static CanBeErosionBlock<?, ?> getCanBeErosion(Block source, boolean hardCoding) {
        return BlockErosionEntryManager.getInstance()
                .map(manager -> manager.get(source, hardCoding))
                .orElse(null);
    }

    @Nullable
    public static CanBeErosionBlock<?, ?> getCanBeErosion(Block source, Level level, BlockPos pos, ErosionType type) {
        return BlockErosionEntryManager.getInstance()
                .map(manager -> manager.get(source, level, pos, type))
                .orElse(null);
    }
}
