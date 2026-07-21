package io.github.jixingdefeng.visionrealm.core.entity.ai.goal.entity.administrator;

import io.github.jixingdefeng.visionrealm.core.entity.ai.goal.SecondaryTarget;
import io.github.jixingdefeng.visionrealm.core.entity.custom.Administrator;
import io.github.jixingdefeng.visionrealm.core.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

public class ObservationGoal extends AdministratorGoal {

    public ObservationGoal(Administrator mob) {
        super(mob);
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null || this.mob.getBehaviorMode() != Administrator.BehaviorMode.OBSERVATION) {
            return false;
        } else {
            return livingEntity.isAlive();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.observeTheTarget();
    }

    @Override
    protected void handleSecondaryTargets() {
        List<Player> players = this.mob.getTargetsByType((ServerLevel) this.mob.level(), SecondaryTarget.AngerType.RENAME, Player.class);
        if (!players.isEmpty()) {
            this.mob.level().playSound(null, this.mob, ModSounds.OBSERVATION_ADMIN_RENAMED.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            for (Player player : players) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(Component.translatable("entity.visionrealm.observation.administrator.rename", this.mob.getName()));
                }
            }

            this.mob.removeAlls(players);
        }
    }

    protected void observeTheTarget() {
        if (this.mob.getTarget() != null) {
            this.mob.getLookControl().setLookAt(this.mob.getTarget());
        }
    }
}
