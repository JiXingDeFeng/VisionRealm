package io.github.jixingdefeng.visionrealm.mixin.world.entity;

import io.github.jixingdefeng.visionrealm.api.entity.LivingEntityExtensions;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityExtensionsMixin implements LivingEntityExtensions {

    @Unique
    private int visionRealm$protectionTime = 0;

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTick(CallbackInfo ci) {
        if (this.visionRealm$protectionTime > 0) {
            this.visionRealm$protectionTime--;
        }
    }

    @Override
    public void setProtectionTime(int time) {
        this.visionRealm$protectionTime = time;
    }

    @Override
    public int getProtectionTime() {
        return this.visionRealm$protectionTime;
    }
}
