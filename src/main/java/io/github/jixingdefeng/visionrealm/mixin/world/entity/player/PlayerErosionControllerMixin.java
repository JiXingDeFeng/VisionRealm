package io.github.jixingdefeng.visionrealm.mixin.world.entity.player;

import io.github.jixingdefeng.visionrealm.api.controller.player.PlayerErosionController;
import io.github.jixingdefeng.visionrealm.api.entity.component.erosion.ErosionCauser;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.damagesource.ModDamageTypes;
import io.github.jixingdefeng.visionrealm.core.entity.ModEntities;
import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import io.github.jixingdefeng.visionrealm.core.entity.custom.monster.NightmareApostle;
import io.github.jixingdefeng.visionrealm.core.erosion.biome.BiomeErosionManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerErosionControllerMixin implements PlayerErosionController {

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract boolean isCreative();

    @Unique private final Player visionrealm$player = (Player) (Object) this;
    @Unique private int visionrealm$risingTimer = 0;
    @Unique private int visionrealm$reduceTimer = 0;

    @Override
    public void updateErosion() {
        AttributeInstance attribute = this.visionrealm$player.getAttributes()
                .getInstance(ModAttributes.PLAYER_SPIRIT_EROSION);
        if (attribute != null && this.canErosionChange()) {
            Holder<Biome> biome = this.visionrealm$player.level().getBiome(this.visionrealm$player.blockPosition());
            double baseValue = attribute.getValue();
            double value = baseValue;

            if (this.isErosionAccumulating(biome)) {
                if (this.visionrealm$risingTimer <= 0) {
                    this.visionRealm$resetTimer(biome);
                    value += this.getErosionAccumulationRate(biome);
                } else {
                    this.visionrealm$risingTimer--;
                }
            } else if (this.canErosionReduceNaturally(biome)) {
                if (this.visionrealm$reduceTimer <= 0) {
                    this.visionRealm$resetTimer(biome);
                    value -= this.getErosionReduceAmount(biome);
                } else {
                    this.visionrealm$reduceTimer--;
                }
            }

            if (value != baseValue) {
                attribute.setBaseValue(Math.clamp(value, 0, 1));
            }

            if (attribute.getValue() >= 1) {
                this.erosionKill();
            }
        }
    }

    @Override
    public void erosionHurtUpdate(DamageSource source, float amount) {
        Entity entity = source.getEntity();
        if (entity instanceof ErosionCauser causer) {
            Holder<Biome> biome = this.visionrealm$player.level().getBiome(this.visionrealm$player.blockPosition());
            this.visionRealm$resetTimer(biome);

            AttributeInstance attribute = this.visionrealm$player.getAttributes().getInstance(ModAttributes.PLAYER_SPIRIT_EROSION);
            VisionRealm.LOGGER.debug("\n\n受伤, erosionAttribute: {}, source: {}, amount: {}\n", attribute, source, amount);
            if (attribute != null) {
                double baseValue = attribute.getValue();
                double added = switch (causer.getCalculation()) {
                    case FIXATION -> causer.getValue();
                    case DAMAGE_PERCENTAGE -> causer.getValue() * amount;
                };

                attribute.setBaseValue(Math.clamp(baseValue + added, 0, 1));
                if (attribute.getValue() >= 1) {
                    this.erosionKill();
                }
            }
        }
    }

    @Override
    public void erosionKill() {
        if (this.visionrealm$player.isAlive()) {
            Level level = this.visionrealm$player.level();
            if (level instanceof ServerLevel serverLevel) {
                Holder<DamageType> damageType = ModDamageTypes.getDamageType(level, ModDamageTypes.SPIRIT_EROSION)
                        .orElseThrow(() -> new NullPointerException("Spirit erosion damage type not registered"));
                NightmareApostle nightmareApostle = ModEntities.NIGHTMARE_APOSTLE.get().create(serverLevel);
                if (nightmareApostle != null) {
                    nightmareApostle.setCustomName(this.visionrealm$player.getName());
                    nightmareApostle.setCustomNameVisible(true);
                    nightmareApostle.setPos(this.visionrealm$player.position());
                    serverLevel.addFreshEntity(nightmareApostle);
                }

                this.visionrealm$player.hurt(new DamageSource(damageType, nightmareApostle, this.visionrealm$player), Float.MAX_VALUE);
                this.visionrealm$player.setHealth(0);
            }
        }
    }

    @Override
    public boolean isErosionAccumulating(Holder<Biome> biome) {
        return BiomeErosionManager.getInstance()
                .map(manager -> manager.containsKey(biome.getKey()))
                .orElse(false);
    }

    @Override
    public boolean canErosionReduceNaturally(Holder<Biome> biome) {
        return !this.isErosionAccumulating(biome);
    }

    @Override
    public int getErosionReduceInterval(Holder<Biome> biome) {
        return this.visionRealm$getBiomeConfig(biome)
                .map(config -> (int) Math.max(1, config.recoveryRate() * 1000))
                .orElse(PlayerErosionController.super.getErosionReduceInterval(biome));
    }

    @Override
    public int getErosionAccumulationInterval(Holder<Biome> biome) {
        return this.visionRealm$getBiomeConfig(biome)
                .map(config -> (int) Math.max(1, config.erosionRate() * 1000))
                .orElse(PlayerErosionController.super.getErosionAccumulationInterval(biome));
    }

    @Override
    public double getErosionAccumulationRate(Holder<Biome> biome) {
        return this.visionRealm$getBiomeConfig(biome)
                .map(BiomeErosionManager.BiomeConfig::erosionStrength)
                .orElse(PlayerErosionController.super.getErosionAccumulationRate(biome));
    }

    @Override
    public double getErosionReduceAmount(Holder<Biome> biome) {
        return this.visionRealm$getBiomeConfig(biome)
                .map(BiomeErosionManager.BiomeConfig::reduceAmount)
                .orElse(PlayerErosionController.super.getErosionReduceAmount(biome));
    }

    @Override
    public boolean canErosionChange() {
        return !(this.isCreative() || this.visionrealm$player.isSleeping() || this.isSpectator());
    }

    @Unique
    protected Optional<BiomeErosionManager.BiomeConfig> visionRealm$getBiomeConfig(Holder<Biome> biome) {
        return BiomeErosionManager.getInstance()
                .map(manager -> manager.getBiomeConfig(biome.getKey()));
    }

    @Unique
    protected void visionRealm$resetTimer(Holder<Biome> biome) {
        this.visionrealm$reduceTimer = this.getErosionReduceInterval(biome);
        this.visionrealm$risingTimer = this.getErosionAccumulationInterval(biome);
    }
}
