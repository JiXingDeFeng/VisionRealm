package io.github.jixingdefeng.visionrealm.mixin.world.particle.entity;

import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.provider.EntityParticleProvider;
import io.github.jixingdefeng.visionrealm.impl.particle.singleton.ModifiableParticleConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityParticleMixin implements EntityParticleProvider {

    @Unique private final LivingEntity visionrealm$entity = (LivingEntity) (Object) this;
    @Unique private DamageSource visionrealm$deathDamageSource;

    @Inject(method = "die", at = @At("HEAD"))
    private void die(DamageSource damageSource, CallbackInfo ci) {
        this.visionrealm$deathDamageSource = damageSource;
    }

    @Inject(method = "makePoofParticles", at = @At("HEAD"), cancellable = true)
    private void makePoofParticles(CallbackInfo ci) {
        if (this.canShowDeathParticles(this.visionrealm$deathDamageSource)) {
            SingletonParticleConfig deathParticle = this.getDeathParticles(this.visionrealm$deathDamageSource);
            if (deathParticle != null) {
                deathParticle.spawnParticles(this.visionrealm$entity);
            }
        }

        ci.cancel();
    }

    @Inject(method = "hurt", at = @At("TAIL"))
    private void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            this.makeHurtParticles(this.visionrealm$entity, source, amount);
        }
    }

    @Nullable
    @Override
    public SingletonParticleConfig getDeathParticles(DamageSource source) {
        SingletonParticleConfig particleConfig = EntityParticleProvider.super.getDeathParticles(source);
        if (particleConfig instanceof ModifiableParticleConfig particle) {
            particle.offset(0, this.visionrealm$entity.getBoundingBox().getYsize() / 2, 0);
            particle.spread(
                    this.visionrealm$entity.getBoundingBox().getXsize() / 2,
                    this.visionrealm$entity.getBoundingBox().getYsize() / 2,
                    this.visionrealm$entity.getBoundingBox().getZsize() / 2
            );

            return particle;
        }

        return particleConfig;
    }
}
