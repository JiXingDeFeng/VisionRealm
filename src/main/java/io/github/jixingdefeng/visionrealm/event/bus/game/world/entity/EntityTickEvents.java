package io.github.jixingdefeng.visionrealm.event.bus.game.world.entity;

import io.github.jixingdefeng.visionrealm.api.particle.provider.EntityParticleProvider;
import net.minecraft.world.entity.Entity;

public class EntityTickEvents {

    public static void updateParticles(Entity entity) {
        ((EntityParticleProvider) entity).updateTickParticles(entity);
    }
}
