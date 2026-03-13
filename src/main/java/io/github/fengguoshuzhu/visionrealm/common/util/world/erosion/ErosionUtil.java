package io.github.fengguoshuzhu.visionrealm.common.util.world.erosion;

import io.github.fengguoshuzhu.visionrealm.common.erosion.manager.biome.BiomeErosionManager;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

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
}
