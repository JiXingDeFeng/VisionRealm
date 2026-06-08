package io.github.jixingdefeng.visionrealm.mixin.world.entity.ai.attribute.player;

import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerAttributeMixin {

    @Inject(method = "createAttributes", at = @At("TAIL"), cancellable = true)
    private static void createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.SANITY)
        );
    }
}
