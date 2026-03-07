package io.github.fengguoshuzhu.visionrealm.core.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ImprovedNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final int updateInterval;
    private int updateTimer = 0;
    private int trackTime = 0;
    private int ignoreTime = 0;
    private LivingEntity ignoreEntity;

    public ImprovedNearestAttackableTargetGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        this(mob, targetType, 10, 10, mustSee, false, null);
    }

    public ImprovedNearestAttackableTargetGoal(Mob mob, Class<T> targetType, int randomInterval, int updateInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
        this.updateInterval = updateInterval;
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

    private void checkForBetterTarget() {
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null) {
            return;
        }

        LivingEntity betterTarget = findBetterTarget();
        if (betterTarget != null && betterTarget != currentTarget) {
            this.mob.setTarget(betterTarget);
            this.target = betterTarget;
        }
    }

    private LivingEntity findBetterTarget() {
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

    private boolean isValidTarget(LivingEntity entity) {
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

    private boolean ignoreTarget(LivingEntity entity) {
        if (entity == this.ignoreEntity) {
            return true;
        } else if (entity != this.mob.getTarget()) {
            return false;
        } else {
            if (this.mob.distanceToSqr(entity) <= 3.0 * 3.0) {
                if (!this.mob.isWithinMeleeAttackRange(entity)) {
                    if (++this.trackTime >= 5) {
                        this.ignoreEntity = entity;
                        this.ignoreTime = this.trackTime;
                        this.trackTime = 0;
                        return true;
                    }
                } else {
                    this.trackTime = 0;
                }
            }

            return false;
        }
    }
}
