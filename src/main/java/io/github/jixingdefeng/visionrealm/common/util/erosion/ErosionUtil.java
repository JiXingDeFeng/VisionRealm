package io.github.jixingdefeng.visionrealm.common.util.erosion;

import io.github.jixingdefeng.visionrealm.api.erosion.infection.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.infection.block.BlockErosionKeyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class ErosionUtil {

    public static ErosionType getBiomeErosionType(BlockPos pos, Level level) {
        return getBiomeErosionType(level.getBiome(pos).getKey());
    }

    public static ErosionType getBiomeErosionType(ResourceKey<Biome> biome) {
        BiomeErosionManager manager = BiomeErosionManager.getInstance();
        if (manager != null) {
            return manager.getErosionType(biome);
        } else {
            return ErosionType.NONE;
        }
    }

    @Nullable
    public static CanBeErosionBlock<?, ?> getCanBeErosion(Block source) {
        if (source instanceof CanBeErosionBlock<?, ?> canBeErosion) {
            return canBeErosion;
        } else {
            return BlockErosionKeyManager.getInstance().get(source);
        }
    }
}
