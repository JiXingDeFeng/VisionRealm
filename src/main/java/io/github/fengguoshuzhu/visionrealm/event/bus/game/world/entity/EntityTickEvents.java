package io.github.fengguoshuzhu.visionrealm.event.bus.game.world.entity;

import io.github.fengguoshuzhu.visionrealm.api.world.particle.EntityParticleProvider;
import net.minecraft.world.entity.Entity;

public class EntityTickEvents {

    public static void updateParticles(Entity entity) {
        if (entity instanceof EntityParticleProvider provider) {
            provider.updateTickParticles(entity);
        }
    }
}
