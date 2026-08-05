package io.github.jixingdefeng.visionrealm.core.util.erosion;

import io.github.jixingdefeng.visionrealm.api.erosion.block.ErodibleBlock;
import io.github.jixingdefeng.visionrealm.core.erosion.BiomeErosionData;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.block_erossion.BlockErosionManager;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class ErosionUtil {

    public static ErosionType getErosionType(Holder<Biome> biome) {
        BiomeErosionData data = biome.getData(BiomeErosionData.DATA_MAP_TYPE);
        if (data != null) {
            Holder<ErosionType> typeHolder = data.erosionType();
            if (typeHolder != null) {
                return typeHolder.value();
            }
        }

        return ErosionType.NONE;
    }

    public static boolean containsErosion(Holder<Biome> biome) {
        BiomeErosionData data = biome.getData(BiomeErosionData.DATA_MAP_TYPE);
        return data != null && data.erosionType() != null;
    }

    @Nullable
    public static ErodibleBlock<?, ?> getErosionEntry(Block source, ErosionType type, boolean allowHardCoded) {
        ErodibleBlock<?, ?> erodibleBlock = BlockErosionManager.getEntry(source, type);
        if (erodibleBlock != null) {
            return erodibleBlock;
        } else {
            return allowHardCoded
                   ? BlockErosionManager.HARDCODED.apply(source)
                   : null;
        }
    }

    private ErosionUtil() {
    }
}
