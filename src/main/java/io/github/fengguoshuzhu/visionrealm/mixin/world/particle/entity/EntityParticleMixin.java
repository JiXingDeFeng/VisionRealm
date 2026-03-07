package io.github.fengguoshuzhu.visionrealm.mixin.world.particle.entity;

import io.github.fengguoshuzhu.visionrealm.api.world.particle.EntityParticleProvider;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityParticleMixin implements EntityParticleProvider {

    @Unique private final Entity visionrealm$entity = (Entity) (Object) this;
    @Unique private int visionrealm$particleTimer = 0;

    @Override
    public void updateTickParticles(Entity entity) {
        int updateInterval = this.getParticleUpdateInterval();
        if (updateInterval >= 0) {
            if (this.visionrealm$particleTimer == 0) {
                this.visionrealm$particleTimer = updateInterval;
                this.makeTickParticles(this.visionrealm$entity);
            } else {
                this.visionrealm$particleTimer--;
            }
        }
    }
}
