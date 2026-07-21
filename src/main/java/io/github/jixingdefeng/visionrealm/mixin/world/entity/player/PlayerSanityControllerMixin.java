package io.github.jixingdefeng.visionrealm.mixin.world.entity.player;

import io.github.jixingdefeng.visionrealm.api.controller.player.PlayerSanityController;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerSanityControllerMixin implements PlayerSanityController {

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract boolean isCreative();

    @Unique private final Player visionrealm$player = (Player) (Object) this;
    @Unique private int visionrealm$risingTimer = 0;
    @Unique private int visionrealm$reduceTimer = 0;

    @Override
    public void updateSanity() {
        AttributeInstance attribute = this.visionrealm$player.getAttributes()
                .getInstance(ModAttributes.PLAYER_SANITY);
        if (attribute != null && this.canSanityChange()) {
            Holder<Biome> biome = this.visionrealm$player.level().getBiome(this.visionrealm$player.blockPosition());
            double baseValue = attribute.getValue();
            double value = baseValue;

            if (this.isSanityContinuouslyDraining(biome)) {
                if (this.visionrealm$reduceTimer <= 0) {
                    this.visionRealm$resetTimer(biome);
                    value -= this.getSanityDrainRate();
                } else {
                    this.visionrealm$reduceTimer--;
                }
            } else if (this.canSanityRecoverNaturally(biome)) {
                if (this.visionrealm$risingTimer <= 0) {
                    this.visionRealm$resetTimer(biome);
                    value += this.getSanityRecoveryAmount();
                } else {
                    this.visionrealm$risingTimer--;
                }
            }

            if (value != baseValue) {
                attribute.setBaseValue(Math.clamp(value, 0, 100));
            }
        }
    }

    @Override
    public void sanityHurtUpdate(DamageSource source, float amount) {
        Holder<Biome> biome = this.visionrealm$player.level().getBiome(this.visionrealm$player.blockPosition());
        this.visionRealm$resetTimer(biome);

        AttributeInstance saneAttribute = this.visionrealm$player.getAttributes().getInstance(ModAttributes.PLAYER_SANITY);
        VisionRealm.LOGGER.debug("\n\n受伤, saneAttribute: {}, source: {}, amount: {}\n", saneAttribute, source, amount);
        if (saneAttribute != null) {
            Entity sourceEntity = source.getEntity();
            amount *= sourceEntity instanceof Enemy ? 0.5F : 0.25F;
            double value = this.visionrealm$player.getAttributeValue(ModAttributes.PLAYER_SANITY);

            VisionRealm.LOGGER.debug("\n\n被攻击, amount: {}\n", amount);
            if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                value -= 90;
                VisionRealm.LOGGER.debug("\n\n闪电, value: {}\n", value);
            } else if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                value -= 50.0;
            } else if (source.is(DamageTypes.DROWN)) {
                value -= Math.max(amount * 0.5, 1);
            } else {
                value -= Math.max(amount * this.getHurtLossPercentage(source, amount), 1);
            }

            saneAttribute.setBaseValue(Math.clamp(value, 0, 100));
        }
    }

    @Override
    public boolean canSanityChange() {
        return !(this.isCreative() || this.visionrealm$player.isSleeping() || this.isSpectator());
    }

    @Override
    public boolean isSanityContinuouslyDraining(Holder<Biome> biome) {
        return this.visionrealm$player.getRemainingFireTicks() > 0;
    }

    @Unique
    protected void visionRealm$resetTimer(Holder<Biome> biome) {
        this.visionrealm$reduceTimer = this.getSanityDrainInterval(biome);
        this.visionrealm$risingTimer = this.getSanityRecoveryInterval(biome);
    }
}
