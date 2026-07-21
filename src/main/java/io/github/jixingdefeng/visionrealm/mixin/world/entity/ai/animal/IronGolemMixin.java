package io.github.jixingdefeng.visionrealm.mixin.world.entity.ai.animal;

import io.github.jixingdefeng.visionrealm.core.entity.ai.goal.ImprovedNearestAttackableTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(IronGolem.class)
public class IronGolemMixin {

    @Redirect(
            method = "registerGoals",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)Lnet/minecraft/world/entity/ai/goal/target/NearestAttackableTargetGoal;"
            )
    )
    private <T extends LivingEntity> NearestAttackableTargetGoal<T> initNearestAttackableTargetGoal(Mob mob, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach, Predicate<LivingEntity> targetPredicate) {
        return new ImprovedNearestAttackableTargetGoal<>(mob, targetType, randomInterval, 10, 0, mustSee, mustReach, targetPredicate);
    }
}
