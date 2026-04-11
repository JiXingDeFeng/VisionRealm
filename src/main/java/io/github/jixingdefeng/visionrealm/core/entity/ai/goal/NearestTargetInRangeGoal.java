package io.github.jixingdefeng.visionrealm.core.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class NearestTargetInRangeGoal<T extends LivingEntity> extends ImprovedNearestAttackableTargetGoal<T> {
    protected final float trackingRange;
    private final boolean mustBeWithinRange;

    public NearestTargetInRangeGoal(Mob mob, Class<T> targetType, boolean mustSee, boolean mustBeWithinRange) {
        this(mob, targetType, 10, 10, 0, mustSee, false, mustBeWithinRange, null);
    }

    public NearestTargetInRangeGoal(Mob mob, Class<T> targetType, float trackingRange, boolean mustSee, boolean mustBeWithinRange) {
        this(mob, targetType, 10, 10, trackingRange, mustSee, false, mustBeWithinRange, null);
    }

    public NearestTargetInRangeGoal(
            Mob mob,
            Class<T> targetType,
            int randomInterval,
            int updateInterval,
            float trackingRange,
            boolean mustSee,
            boolean mustReach,
            boolean mustBeWithinRange,
            @Nullable Predicate<LivingEntity> targetPredicate
    ) {
        super(mob, targetType, randomInterval, updateInterval, mustSee, mustReach, targetPredicate);
        this.mustBeWithinRange = mustBeWithinRange;
        this.trackingRange = trackingRange <= 0 ? Float.MAX_VALUE : trackingRange;
    }

    @Override
    protected void findTarget() {
        super.findTarget();
        if (this.target != null) {
            this.target = this.mob.distanceToSqr(this.target) <= this.trackingRangeSqr() ? this.target : null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.updateTarget();
    }

    protected double trackingRangeSqr() {
        return this.trackingRange * this.trackingRange;
    }

    private void updateTarget() {
        if (this.mustBeWithinRange) {
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                if (this.mob.distanceToSqr(target) > this.trackingRangeSqr()) {
                    this.mob.setTarget(null);
                }
            }
        }
    }
}
