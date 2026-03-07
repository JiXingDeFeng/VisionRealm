package io.github.fengguoshuzhu.visionrealm.core.entity.ai.goal.entity.administrator;

import io.github.fengguoshuzhu.visionrealm.core.entity.custom.AdministratorEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class AdministratorGoal extends Goal {
    protected final AdministratorEntity mob;

    public AdministratorGoal(AdministratorEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return this.mob.isAlive();
    }

    @Override
    public void tick() {
        super.tick();
        this.handleSecondaryTargets();
    }

    protected abstract void handleSecondaryTargets();
}
