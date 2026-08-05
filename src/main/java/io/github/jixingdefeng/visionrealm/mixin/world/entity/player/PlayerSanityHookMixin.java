package io.github.jixingdefeng.visionrealm.mixin.world.entity.player;

import io.github.jixingdefeng.visionrealm.content.world.entity.ai.attributes.ModAttributes;
import io.github.jixingdefeng.visionrealm.core.hook.player.PlayerSanityHook;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerSanityHookMixin implements PlayerSanityHook {

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
        Level level = this.visionrealm$player.level();
        BlockPos pos = this.visionrealm$player.blockPosition();
        if (attribute != null && this.canSanityChange(level, pos)) {
            double baseValue = attribute.getValue();
            double value = baseValue;

            if (this.isSanityContinuouslyDraining(level, pos)) {
                if (this.visionrealm$reduceTimer <= 0) {
                    this.visionRealm$resetTimer(level, pos);
                    value -= this.getSanityDrainRate(level, pos);
                } else {
                    this.visionrealm$reduceTimer--;
                }
            } else if (this.canSanityRecoverNaturally(level, pos)) {
                if (this.visionrealm$risingTimer <= 0) {
                    this.visionRealm$resetTimer(level, pos);
                    value += this.getSanityRecoveryAmount(level, pos);
                } else {
                    this.visionrealm$risingTimer--;
                }
            }

            if (value != baseValue) {
                attribute.setBaseValue(Math.max(value, 0));
            }
        }
    }

    @Override
    public void sanityHurtUpdate(DamageSource source, float amount) {
        Level level = this.visionrealm$player.level();
        BlockPos pos = this.visionrealm$player.blockPosition();
        this.visionRealm$resetTimer(level, pos);

        AttributeInstance saneAttribute = this.visionrealm$player.getAttributes().getInstance(ModAttributes.PLAYER_SANITY);
        if (saneAttribute != null) {
            Entity sourceEntity = source.getEntity();
            amount *= sourceEntity instanceof Enemy ? 0.5F : 0.25F;
            double value = this.visionrealm$player.getAttributeValue(ModAttributes.PLAYER_SANITY);

            if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                value -= 90;
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
    public boolean canSanityChange(Level level, BlockPos pos) {
        return !(this.isCreative() || this.visionrealm$player.isSleeping() || this.isSpectator());
    }

    @Override
    public boolean isSanityContinuouslyDraining(Level level, BlockPos pos) {
        return this.visionrealm$player.getRemainingFireTicks() > 0;
    }

    @Unique
    protected void visionRealm$resetTimer(Level level, BlockPos pos) {
        this.visionrealm$reduceTimer = this.getSanityDrainInterval(level, pos);
        this.visionrealm$risingTimer = this.getSanityRecoveryInterval(level, pos);
    }
}
