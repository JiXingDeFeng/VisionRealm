package io.github.jixingdefeng.visionrealm.content.world.entity.ai.goal.entity.administrator;

import io.github.jixingdefeng.visionrealm.content.world.entity.custom.Administrator;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class AdministratorGoal extends Goal {
    protected final Administrator mob;

    public AdministratorGoal(Administrator mob) {
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
