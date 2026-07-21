package io.github.jixingdefeng.visionrealm.core.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ImprovedNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    protected final float trackingRange;
    protected final int updateInterval;
    protected LivingEntity ignoreEntity;
    private int updateTimer = 0;
    private int trackTime = 0;
    private int ignoreTime = 0;

    public ImprovedNearestAttackableTargetGoal(Mob mob, Class<T> targetType, float trackingRange, boolean mustSee) {
        this(mob, targetType, 10, 10, trackingRange, mustSee, false, null);
    }

    public ImprovedNearestAttackableTargetGoal(
            Mob mob,
            Class<T> targetType,
            int randomInterval,
            int updateInterval,
            float trackingRange,
            boolean mustSee,
            boolean mustReach,
            @Nullable Predicate<LivingEntity> targetPredicate
    ) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
        this.updateInterval = updateInterval;
        this.trackingRange = trackingRange > 0 ? trackingRange : (float) mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob.getLastHurtByMob() != this.mob.getTarget()) {
            if (++this.updateTimer >= this.updateInterval) {
                this.updateTimer = 0;
                this.checkForBetterTarget();
                if (this.ignoreEntity != null && --this.ignoreTime <= 0) {
                    this.ignoreEntity = null;
                }
            }
        }
    }

    protected double trackingRangeSqr() {
        return this.trackingRange * this.trackingRange;
    }

    protected void checkForBetterTarget() {
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null) {
            return;
        }

        LivingEntity betterTarget = this.findBetterTarget();
        if (betterTarget != null && betterTarget != currentTarget) {
            this.mob.setTarget(betterTarget);
            this.target = betterTarget;
        }
    }

    protected LivingEntity findBetterTarget() {
        List<T> targets = this.mob.level().getEntitiesOfClass(
                this.targetType,
                this.getTargetSearchArea(this.getFollowDistance()),
                this::isValidTarget
        );

        if (targets.isEmpty()) {
            return null;
        }

        return targets.stream()
                .min(Comparator.comparingDouble(this.mob::distanceToSqr))
                .orElse(null);
    }

    protected boolean isValidTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        } else if (this.mustSee && !this.mob.getSensing().hasLineOfSight(entity)) {
            return false;
        } else if (this.ignoreTarget(entity)) {
            return false;
        } else {
            return this.targetConditions.test(this.mob, entity);
        }
    }

    protected boolean ignoreTarget(LivingEntity entity) {
        if (entity == this.ignoreEntity) {
            return true;
        } else {
            LivingEntity target = this.mob.getTarget();
            if (entity == target) {
                if (this.mob.isWithinMeleeAttackRange(entity) && this.mob.distanceToSqr(target) > this.trackingRangeSqr()) {
                    this.trackTime = 0;
                } else {
                    if (this.trackTime >= 5) {
                        this.ignoreEntity = entity;
                        this.ignoreTime = this.trackTime;
                        this.trackTime = 0;
                        return true;
                    } else {
                        ++this.trackTime;
                    }
                }
            }

            return false;
        }
    }
}
