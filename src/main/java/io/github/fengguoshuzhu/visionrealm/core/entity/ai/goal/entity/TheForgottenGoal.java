package io.github.fengguoshuzhu.visionrealm.core.entity.ai.goal.entity;

import io.github.fengguoshuzhu.visionrealm.core.entity.ai.goal.ImprovedMeleeAttackGoal;
import io.github.fengguoshuzhu.visionrealm.core.entity.custom.monster.TheForgottenEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TheForgottenGoal extends ImprovedMeleeAttackGoal {
    private final TheForgottenEntity mob;
    private int ticksUntilNextAttack;
    private int attackTimer;

    public TheForgottenGoal(TheForgottenEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.mob = mob;
        this.ticksUntilNextAttack = 0;
        this.attackTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ticksUntilNextAttack > 0) {
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        } else {
            if (this.attackTimer > 0) {
                --this.attackTimer;
            } else if (this.mob.isAttack()) {
                this.mob.setAttack(false);
                this.resetAttackCooldown();
            }
        }

        this.attack();
    }

    @Override
    protected void checkAndPerformAttack(@NotNull LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.attackTimer = this.mob.getAttackAnimDuration();
            this.mob.setAttack(true);
            this.mob.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    protected boolean canPerformAttack(@NotNull LivingEntity entity) {
        return this.isTimeToAttack()
                && this.mob.isWithinMeleeAttackRange(entity)
                && this.mob.getSensing().hasLineOfSight(entity)
                && !this.mob.isAttack();
    }

    @Override
    public void stop() {
        super.stop();
        this.ticksUntilNextAttack = 0;
        this.attackTimer = 0;
        this.mob.setAttack(false);
    }

    @Override
    public void start() {
        super.start();
        this.ticksUntilNextAttack = 0;
        this.attackTimer = 0;
    }

    @Override
    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0 && this.attackTimer <= 0;
    }

    @Override
    protected void resetAttackCooldown() {
        super.resetAttackCooldown();
        this.ticksUntilNextAttack = this.adjustedTickDelay(20);
        this.attackTimer = 0;
    }

    @Override
    public int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    private void attack() {
        if (this.mob.isAttack()) {
            if (this.attackTimer == this.mob.getDamageTime()) {
                List<LivingEntity> entities = this.mob.level().getEntitiesOfClass(
                        LivingEntity.class, this.mob.getAttackBoundingBox(), entity -> entity.isAlive() && (
                                !(entity instanceof TheForgottenEntity || entity instanceof Enemy) || entity == this.mob.getTarget())
                );

                for (LivingEntity livingEntity : entities) {
                    this.mob.doHurtTarget(livingEntity);
                }
            }
        }
    }
}
