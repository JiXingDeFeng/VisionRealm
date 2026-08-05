package io.github.jixingdefeng.visionrealm.mixin.world.item;

import io.github.jixingdefeng.visionrealm.content.world.entity.ai.goal.SecondaryTarget;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.Administrator;
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
        if (target instanceof Administrator entity) {
            entity.addSecondaryTarget(player, SecondaryTarget.AngerType.RENAME);
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }
}
