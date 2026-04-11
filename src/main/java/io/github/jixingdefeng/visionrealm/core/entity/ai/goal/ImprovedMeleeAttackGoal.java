package io.github.jixingdefeng.visionrealm.core.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ImprovedMeleeAttackGoal extends MeleeAttackGoal {
    private int stuckTicks = 0;

    public ImprovedMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public void tick() {
        super.tick();
        this.checkAndJump();
    }

    @Override
    public void start() {
        super.start();
        this.stuckTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.stuckTicks = 0;
    }

    private void checkAndJump() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            if (this.stuckTicks > 0) {
                this.stuckTicks = 0;
            }

            return;
        }

        if (this.shouldJump()) {
            this.mob.getJumpControl().jump();
        }
    }

    private boolean shouldJump() {
        Level level = this.mob.level();
        BlockPos frontPos = this.mob.blockPosition().relative(this.mob.getDirection());
        BlockState frontState = level.getBlockState(frontPos);
        if (frontState.isCollisionShapeFullBlock(this.mob.level(), frontPos)) {
            BlockPos abovePos = frontPos.above();
            BlockPos abovePos2 = abovePos.above();
            return blockCanPass(level, abovePos) && blockCanPass(level, abovePos2);
        }

        return this.isStuckForTooLong();
    }

    private boolean blockCanPass(Level level, BlockPos blockPos) {
        BlockState blockState = level.getBlockState(blockPos);
        return level.isEmptyBlock(blockPos) || blockState.getCollisionShape(level, blockPos).isEmpty();
    }

    private boolean isStuckForTooLong() {
        if (this.mob.getDeltaMovement().horizontalDistanceSqr() < 0.01 && this.stuckTicks > 20) {
            this.stuckTicks = 0;
            return true;
        } else {
            return false;
        }
    }
}
