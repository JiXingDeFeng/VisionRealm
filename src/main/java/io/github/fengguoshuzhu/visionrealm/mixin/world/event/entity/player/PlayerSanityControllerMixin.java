package io.github.fengguoshuzhu.visionrealm.mixin.world.event.entity.player;

import io.github.fengguoshuzhu.visionrealm.api.controller.entity.player.PlayerSanityController;
import io.github.fengguoshuzhu.visionrealm.core.entity.ai.attributes.ModAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerSanityControllerMixin implements PlayerSanityController {

    @Unique private final Player visionrealm$player = (Player) (Object) this;
    @Unique private int visionrealm$saneUpdateTimer = 0;
    @Unique private int visionrealm$saneRisingTimer = 0;

    @Override
    public void updateSanity() {
        AttributeInstance saneAttribute = this.visionrealm$player.getAttributes().getInstance(ModAttributes.SANITY);
        if (saneAttribute != null) {
            double baseValue = this.visionrealm$player.getAttributeValue(ModAttributes.SANITY);
            double value = baseValue;
            if (this.visionrealm$saneUpdateTimer <= 0) {
                this.visionrealm$saneUpdateTimer = 5;
                if (this.visionrealm$player.getRemainingFireTicks() > 0) {
                    this.visionrealm$saneRisingTimer = this.getSanityRecoveryInterval();
                    value -= 0.5;
                }
            } else {
                this.visionrealm$saneUpdateTimer--;
            }

            if (this.canBeRecoveryNaturally()) {
                if (this.visionrealm$saneRisingTimer <= 0) {
                    this.visionrealm$saneRisingTimer = this.getSanityRecoveryInterval();
                    value += this.naturallyRecoveryValue();
                } else {
                    this.visionrealm$saneRisingTimer--;
                }
            }

            if (value != baseValue) {
                saneAttribute.setBaseValue(Math.max(0, Math.min(value, 100)));
            }
        }
    }

    @Override
    public void onHurtUpdateSanity(DamageSource source, float amount) {
        AttributeInstance saneAttribute = this.visionrealm$player.getAttributes().getInstance(ModAttributes.SANITY);
        if (saneAttribute != null) {
            double value = this.visionrealm$player.getAttributeValue(ModAttributes.SANITY);
            if (source.getEntity() instanceof Monster) {
                amount *= 0.25F;
            }

            if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                value -= 90;
            } else if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                value -= 50.0;
            } else if (source.is(DamageTypes.DROWN)) {
                value -= Math.max(amount * 0.5, 1);
            } else {
                value -= Math.max(amount * 0.25, 1);
            }

            saneAttribute.setBaseValue(Math.max(0, Math.min(value, 100)));
        }
    }

    @Override
    public int getSanityRecoveryTimer() {
        return this.visionrealm$saneRisingTimer;
    }

    @Override
    public void setSanityRecoveryTimer(int tick) {
        this.visionrealm$saneRisingTimer = tick;
    }
}
