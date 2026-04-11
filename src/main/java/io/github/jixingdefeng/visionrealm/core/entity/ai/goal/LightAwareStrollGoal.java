package io.github.jixingdefeng.visionrealm.core.entity.ai.goal;

import io.github.jixingdefeng.visionrealm.common.util.world.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LightAwareStrollGoal extends RandomStrollGoal {
    private final int lightThreshold;
    private final int maxSearchAttempts;
    private final boolean darknessAvoidance;
    private final boolean considerSkyLight;

    public LightAwareStrollGoal(PathfinderMob mob, double speedModifier, int lightThreshold, boolean darknessAvoidance, boolean considerSkyLight) {
        this(mob, speedModifier, lightThreshold, 30, darknessAvoidance, considerSkyLight);
    }

    public LightAwareStrollGoal(
            PathfinderMob mob,
            double speedModifier,
            int lightThreshold,
            int maxSearchAttempts,
            boolean darknessAvoidance,
            boolean considerSkyLight
    ) {
        super(mob, speedModifier);
        this.lightThreshold = lightThreshold;
        this.maxSearchAttempts = maxSearchAttempts;
        this.darknessAvoidance = darknessAvoidance;
        this.considerSkyLight = considerSkyLight;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        Level level = this.mob.level();
        if (this.shouldEvadeBasedOnLight(level.getMaxLocalRawBrightness(this.mob.blockPosition()))) {
            Vec3 candidatePos;
            Vec3 position = null;
            int brightness = this.darknessAvoidance ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            for (int i = 0; i < this.maxSearchAttempts; i++) {
                candidatePos = LandRandomPos.getPos(this.mob, 50, 10);
                if (candidatePos != null) {
                    int brightness2 = BlockPosUtil.getLightLevel(level, BlockPos.containing(candidatePos), this.considerSkyLight);
                    if (!this.shouldEvadeBasedOnLight(brightness2)) {
                        return candidatePos;
                    } else if (this.isBetterLightValue(brightness2, brightness) && this.isValidPosition(candidatePos)) {
                        position = candidatePos;
                        brightness = brightness2;
                    }
                }
            }

            return position;
        }

        return super.getPosition();
    }

    public boolean shouldEvadeBasedOnLight(int brightness) {
        return this.darknessAvoidance ? brightness <= this.lightThreshold : brightness >= this.lightThreshold;
    }

    protected boolean isBetterLightValue(int candidate, int currentBest) {
        if (this.darknessAvoidance) {
            return candidate > currentBest;
        } else {
            return candidate < currentBest;
        }
    }

    protected boolean isValidPosition(Vec3 position) {
        return true;
    }
}
