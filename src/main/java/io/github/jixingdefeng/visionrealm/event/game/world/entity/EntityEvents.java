package io.github.jixingdefeng.visionrealm.event.game.world.entity;

import io.github.jixingdefeng.visionrealm.api.entity.LivingEntityExtensions;
import io.github.jixingdefeng.visionrealm.api.entity.particle.EntityParticleProvider;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.content.world.entity.ModEntities;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.ForgottenShadow;
import io.github.jixingdefeng.visionrealm.event.game.world.entity.player.PlayerEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class EntityEvents {

    public static void onRegisterSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.FORGOTTEN_SHADOW.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ForgottenShadow::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static void onEntityJoinLevel(final EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        EntityParticleProvider provider = (EntityParticleProvider) entity;
        if (provider.useParticleSystem() && provider.canShowSpawnParticles()) {
            SingletonParticleConfig particle = provider.getSpawnParticles();
            if (particle != null) {
                particle.spawnParticles(entity);
            }
        }
    }

    public static void updateParticles(final Entity entity) {
        EntityParticleProvider provider = (EntityParticleProvider) entity;
        if (provider.useParticleSystem()) {
            provider.updateTickParticles(entity);
        }
    }

    public static void onEntityHurt(final LivingDamageEvent.Pre event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntityExtensions livingEntity && livingEntity.getProtectionTime() > 0) {
            event.setNewDamage(0);
        }
    }

    public static void onEntityHurt(final LivingDamageEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            PlayerEvents.onHurt(player, event.getSource(), event.getNewDamage());
        }
    }
}
