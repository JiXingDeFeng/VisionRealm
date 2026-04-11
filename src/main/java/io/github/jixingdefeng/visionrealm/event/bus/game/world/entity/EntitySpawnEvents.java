package io.github.jixingdefeng.visionrealm.event.bus.game.world.entity;

import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.provider.EntityParticleProvider;
import io.github.jixingdefeng.visionrealm.core.entity.Entities;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.TheForgottenEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class EntitySpawnEvents {

    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                Entities.WANDERER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TheForgottenEntity::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityParticleProvider entity2) {
            if (entity2.canShowSpawnParticles()) {
                SingletonParticleConfig particle = entity2.getSpawnParticles();
                if (particle != null) {
                    particle.spawnParticles(entity);
                }
            }
        }
    }
}
