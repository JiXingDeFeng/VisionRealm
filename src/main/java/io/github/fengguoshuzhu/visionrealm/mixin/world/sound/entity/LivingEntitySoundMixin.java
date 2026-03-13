package io.github.fengguoshuzhu.visionrealm.mixin.world.sound.entity;

import io.github.fengguoshuzhu.visionrealm.api.entity.LivingEntityExtensions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntitySoundMixin implements LivingEntityExtensions {

    @Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
    private void playHurtSound(DamageSource damageSource, CallbackInfo ci) {
        if (!this.canMakeHurtSound()) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "handleDamageEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
            )
    )
    private void makeHurtSound(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch) {
        if (instance instanceof LivingEntityExtensions expandEntity &&  expandEntity.canMakeHurtSound()) {
            instance.playSound(soundEvent, volume, pitch);
        }
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;makeSound(Lnet/minecraft/sounds/SoundEvent;)V"
            )
    )
    private void makeDeathSound_1(LivingEntity livingEntity, SoundEvent soundEvent) {
        if (livingEntity instanceof LivingEntityExtensions expandEntity && expandEntity.canMakeDeathSound()) {
            livingEntity.makeSound(soundEvent);
        }
    }

    @Redirect(
            method = "handleEntityEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"
            )
    )
    private void makeDeathSound_2(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch) {
        if (instance instanceof LivingEntityExtensions expandEntity && expandEntity.canMakeDeathSound()) {
            instance.playSound(soundEvent, volume, pitch);
        }
    }
}
