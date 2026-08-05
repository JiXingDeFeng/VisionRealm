package io.github.jixingdefeng.visionrealm.content.world.entity.ai.goal.entity.administrator;

import io.github.jixingdefeng.visionrealm.content.world.entity.custom.Administrator;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;

public class InterferenceGoal extends AdministratorGoal {
    public int j = -1;

    public InterferenceGoal(Administrator mob) {
        super(mob);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null || this.mob.getBehaviorMode() != Administrator.BehaviorMode.INTERFERENCE) {
            return false;
        } else {
            return livingEntity.isAlive();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.j <= 0) {
            this.mob.setTeleportTargetStage(2);
            this.j = 30;
        } else {
            this.j--;
        }
    }

    @Override
    protected void handleSecondaryTargets() {
    }
}
