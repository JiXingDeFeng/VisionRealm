package io.github.jixingdefeng.visionrealm.content.world.entity.custom;

import io.github.jixingdefeng.visionrealm.content.world.entity.ai.goal.LightAwareStrollGoal;
import io.github.jixingdefeng.visionrealm.core.util.world.pos.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public interface Photophobic {

    static boolean isLightLevelAcceptable(
            ServerLevelAccessor level, BlockPos pos, LightFearType lightFearType, int maxLightLevel
    ) {
        int brightness = BlockPosUtil.getLightLevel(level, pos, lightFearType.coversSkyLight(), lightFearType.coversSkyLight());
        return brightness < maxLightLevel;
    }

    /**
     * Returns this entity's light sensitivity type.
     * <p>
     * Determines which light sources the entity fears. The default implementation
     * returns {@link LightFearType#ALL}, causing the entity to fear all light.
     *
     * @return the light sensitivity type
     */
    LightFearType getLightFearType();

    int getMaxAcceptBrightness();

    default boolean considerSkyLight() {
        return this.getLightFearType().coversSkyLight();
    }

    default boolean considerBlockLight() {
        return this.getLightFearType().coversBlockLight();
    }

    default int getBrightness(Level level, BlockPos pos) {
        boolean considerSky = this.considerSkyLight();
        boolean considerBlock = this.considerBlockLight();
        return BlockPosUtil.getLightLevel(level, pos, considerSky, considerBlock);
    }

    /**
     * Defines the light sensitivity type for a mob that fears certain light sources.
     * <p>
     * Determines whether the mob is afraid of sky light, block light, both, or neither.
     * Used in conjunction with {@link LightAwareStrollGoal} to control movement behavior
     * based on light conditions.
     */
    enum LightFearType {
        /** Not afraid of any light source; light does not affect movement. */
        NONE,

        /** Afraid of all light sources (both sky light and block light). */
        ALL,

        /** Afraid only of natural light (sky light). */
        SKY_LIGHT,

        /** Afraid only of artificial light (block light from torches, glowstone, etc.). */
        BLOCK_LIGHT,;

        /**
         * Checks whether this type includes fear of sky light.
         *
         * @return {@code true} if this type covers sky light, {@code false} otherwise
         */
        public boolean coversSkyLight() {
            return this == ALL ||  this == SKY_LIGHT;
        }

        /**
         * Checks whether this type includes fear of block light.
         *
         * @return {@code true} if this type covers block light, {@code false} otherwise
         */
        public boolean coversBlockLight() {
            return this == ALL || this == BLOCK_LIGHT;
        }
    }
}
