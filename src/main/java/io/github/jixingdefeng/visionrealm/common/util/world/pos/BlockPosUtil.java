package io.github.jixingdefeng.visionrealm.common.util.world.pos;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;

public final class BlockPosUtil {

    public static int getLightLevel(LevelAccessor level, BlockPos pos, boolean considerSky, boolean considerBlock) {
        if (considerBlock && considerSky) {
            return level.getRawBrightness(pos, level.getSkyDarken());
        } else if (considerSky) {
            return level.getBrightness(LightLayer.SKY, pos);
        } else if (considerBlock) {
            return level.getBrightness(LightLayer.BLOCK, pos);
        } else {
            return 0;
        }
    }

    private BlockPosUtil() {
    }
}
