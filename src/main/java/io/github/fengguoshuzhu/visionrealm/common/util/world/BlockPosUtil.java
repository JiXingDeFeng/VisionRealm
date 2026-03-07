package io.github.fengguoshuzhu.visionrealm.common.util.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class BlockPosUtil {

    public static int getLightLevel(Level level, BlockPos pos) {
        return getLightLevel(level, pos, true);
    }

    public static int getLightLevel(Level level, BlockPos pos, boolean shouldSeeSky) {
        return getLightLevel(level, pos, shouldSeeSky, true);
    }

    public static int getLightLevel(Level level, BlockPos pos, boolean considerSky, boolean considerBlock) {
        int blockBrightness = level.getBrightness(LightLayer.BLOCK, pos);
        int skyBrightness = level.getBrightness(LightLayer.SKY, pos);
        if (considerSky && considerBlock) {
            return Math.max(blockBrightness, skyBrightness);
        } else if (considerSky) {
            return skyBrightness;
        } else if (considerBlock) {
            return blockBrightness;
        } else {
            return 0;
        }
    }
}
