package io.github.fengguoshuzhu.visionrealm.mixin.world.item;

import io.github.fengguoshuzhu.visionrealm.core.entity.custom.AdministratorEntity;
import io.github.fengguoshuzhu.visionrealm.core.entity.custom.SecondaryTarget;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
public class NameTagItemMixin {

    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    public void interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                     InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (target instanceof AdministratorEntity entity) {
            entity.addSecondaryTarget(player, SecondaryTarget.AngerType.RENAME);
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }
}
