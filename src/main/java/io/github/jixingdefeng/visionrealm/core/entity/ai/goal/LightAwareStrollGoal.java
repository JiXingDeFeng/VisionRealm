package io.github.jixingdefeng.visionrealm.core.entity.ai.goal;

import io.github.jixingdefeng.visionrealm.common.util.world.pos.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A random stroll goal that avoids (or seeks) areas based on light level.
 * <p>
 * The mob will attempt to find a random destination within a search radius that satisfies
 * the configured light condition (either above or below a threshold). If no such position is
 * found after a limited number of attempts, the best candidate found is returned, or falls back
 * to the default random stroll behavior.
 */
public class LightAwareStrollGoal extends RandomStrollGoal {
    protected final int lightThreshold;
    protected final int maxSearchAttempts;
    protected final int radius;
    protected final int verticalRange;
    protected final boolean darknessAvoidance;
    protected final boolean considerSkyLight;
    protected final boolean considerBlockLight;
    protected final boolean allowSuboptimal;

    public LightAwareStrollGoal(
            PathfinderMob mob,
            double speedModifier,
            int lightThreshold,
            boolean darknessAvoidance,
            boolean allowSuboptimal
    ) {
        this(mob, speedModifier, lightThreshold, darknessAvoidance, allowSuboptimal, true, true);
    }

    public LightAwareStrollGoal(
            PathfinderMob mob,
            double speedModifier,
            int lightThreshold,
            boolean darknessAvoidance,
            boolean allowSuboptimal,
            boolean considerSkyLight,
            boolean considerBlockLight
    ) {
        this(mob, speedModifier, lightThreshold, 30, 50, 10,
                darknessAvoidance, allowSuboptimal, considerSkyLight, considerBlockLight);
    }

    public LightAwareStrollGoal(
            PathfinderMob mob,
            double speedModifier,
            int lightThreshold,
            int maxSearchAttempts,
            int radius,
            int verticalRange,
            boolean darknessAvoidance,
            boolean allowSuboptimal,
            boolean considerSkyLight,
            boolean considerBlockLight
    ) {
        super(mob, speedModifier);
        this.lightThreshold = lightThreshold;
        this.maxSearchAttempts = Math.max(1, maxSearchAttempts);
        this.radius = radius;
        this.verticalRange = verticalRange;
        this.darknessAvoidance = darknessAvoidance;
        this.allowSuboptimal = allowSuboptimal;
        this.considerSkyLight = considerSkyLight;
        this.considerBlockLight = considerBlockLight;
    }

    /**
     * Attempts to find a random position that meets the light condition.
     * <p>
     * Up to {@link #maxSearchAttempts} random positions are generated within the search radius.
     * The first position that fully satisfies the light condition is returned immediately.
     * <p>
     * If no fully compliant position is found, the best available fallback is returned,
     * This ensures that even when no perfect position exists, the entity will move toward
     * the most favorable light conditions available.
     * <p>
     * Fallback behavior can be disabled by setting {@link #allowSuboptimal} to {@code false},
     * in which case this method returns {@code null} if no compliant position is found.
     *
     * @return a suitable target position, or {@code null} if no acceptable position was found
     *         (either because none exist or fallback is disabled)
     */
    @Nullable
    @Override
    protected Vec3 getPosition() {
        Level level = this.mob.level();
        Vec3 suboptimal = null;
        int brightness = this.darknessAvoidance ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < this.maxSearchAttempts; i++) {
            Vec3 candidatePos = LandRandomPos.getPos(this.mob, this.radius, this.verticalRange);
            if (candidatePos != null) {
                int brightness2 = this.getBrightness(level, BlockPos.containing(candidatePos));
                if (!this.shouldEvadeBasedOnLight(brightness2)) {
                    return candidatePos;
                } else if (this.allowSuboptimal && this.isBetterLight(brightness2, brightness)) {
                    suboptimal = candidatePos;
                    brightness = brightness2;
                }
            }
        }

        return suboptimal;
    }

    protected int getBrightness(Level level, BlockPos pos) {
        return BlockPosUtil.getLightLevel(level, pos, this.considerBlockLight, this.considerSkyLight);
    }

    protected boolean shouldEvadeBasedOnLight(int brightness) {
        return this.darknessAvoidance ? brightness <= this.lightThreshold : brightness >= this.lightThreshold;
    }

    protected boolean isBetterLight(int candidate, int currentBest) {
        if (this.darknessAvoidance) {
            return candidate > currentBest;
        } else {
            return candidate < currentBest;
        }
    }
}
