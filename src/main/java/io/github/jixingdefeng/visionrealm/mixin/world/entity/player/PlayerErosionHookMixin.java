package io.github.jixingdefeng.visionrealm.mixin.world.entity.player;

import io.github.jixingdefeng.visionrealm.api.entity.component.erosion.ErosionCauser;
import io.github.jixingdefeng.visionrealm.content.world.entity.ModEntities;
import io.github.jixingdefeng.visionrealm.content.world.entity.ai.attributes.ModAttributes;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.NightmareApostle;
import io.github.jixingdefeng.visionrealm.core.erosion.BiomeErosionData;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.hook.player.PlayerErosionHook;
import io.github.jixingdefeng.visionrealm.core.util.erosion.ErosionUtil;
import io.github.jixingdefeng.visionrealm.core.world.damagesource.ModDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerErosionHookMixin implements PlayerErosionHook {

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
        Level level = this.visionrealm$player.level();
        BlockPos pos = this.visionrealm$player.blockPosition();
        if (attribute != null) {
            if (attribute.getValue() >= 1) {
                this.erosionKill(this.visionrealm$player);
            }

            if (this.canErosionChange(level, pos)) {
                double baseValue = attribute.getValue();
                double value = baseValue;

                if (this.isErosionAccumulating(level, pos)) {
                    if (this.visionrealm$risingTimer <= 0) {
                        this.visionRealm$resetTimer(level, pos);
                        value += this.getErosionAccumulationRate(level, pos);
                    } else {
                        this.visionrealm$risingTimer--;
                    }
                } else if (this.canErosionReduceNaturally(level, pos)) {
                    if (this.visionrealm$reduceTimer <= 0) {
                        this.visionRealm$resetTimer(level, pos);
                        value -= this.getErosionReduceAmount(level, pos);
                    } else {
                        this.visionrealm$reduceTimer--;
                    }
                }

                if (value != baseValue) {
                    attribute.setBaseValue(Math.max(value, 0));
                }
            }
        }
    }

    @Override
    public void erosionHurtUpdate(DamageSource source, float amount) {
        Entity entity = source.getEntity();
        if (entity instanceof ErosionCauser causer) {
            Level level = this.visionrealm$player.level();
            BlockPos pos = this.visionrealm$player.blockPosition();
            this.visionRealm$resetTimer(level, pos);

            AttributeInstance attribute = this.visionrealm$player.getAttributes().getInstance(ModAttributes.PLAYER_SPIRIT_EROSION);
            if (attribute != null) {
                double baseValue = attribute.getValue();
                double added = switch (causer.getCalculation()) {
                    case FIXATION -> causer.getValue();
                    case DAMAGE_PERCENTAGE -> causer.getValue() * amount;
                };

                attribute.setBaseValue(Math.clamp(baseValue + added, 0, 1));
                if (attribute.getValue() >= 1) {
                    this.erosionKill(this.visionrealm$player);
                }
            }
        }
    }

    @Override
    public void erosionKill(Player player) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            if (player.isAlive()) {
                Holder<DamageType> damageType = ModDamageTypes.getDamageType(level, ModDamageTypes.SPIRIT_EROSION)
                        .orElseThrow(() -> new NullPointerException("Spirit erosion damage type not registered"));
                NightmareApostle nightmareApostle = ModEntities.NIGHTMARE_APOSTLE.get().create(serverLevel);
                if (nightmareApostle != null) {
                    nightmareApostle.setCustomName(player.getName());
                    nightmareApostle.setCustomNameVisible(true);
                    nightmareApostle.setPos(player.position());
                    serverLevel.addFreshEntity(nightmareApostle);
                }

                player.hurt(new DamageSource(damageType, nightmareApostle, player), Float.MAX_VALUE);
                player.setHealth(0);
            }
        }
    }

    @Override
    public boolean isErosionAccumulating(Level level, BlockPos pos) {
        return ErosionUtil.containsErosion(level.getBiome(pos));
    }

    @Override
    public boolean canErosionReduceNaturally(Level level, BlockPos pos) {
        return !this.isErosionAccumulating(level, pos);
    }

    @Override
    public int getErosionReduceInterval(Level level, BlockPos pos) {
        BiomeErosionData config = this.visionRealm$getBiomeConfig(level, pos);
        return config != null
               ? (int) Math.max(1, config.recoveryRate() * 1000)
               : PlayerErosionHook.super.getErosionReduceInterval(level, pos);
    }

    @Override
    public int getErosionAccumulationInterval(Level level, BlockPos pos) {
        BiomeErosionData config = this.visionRealm$getBiomeConfig(level, pos);
        return config != null
               ? (int) Math.max(1, config.erosionRate() * 1000)
               : PlayerErosionHook.super.getErosionAccumulationInterval(level, pos);
    }

    @Override
    public double getErosionAccumulationRate(Level level, BlockPos pos) {
        BiomeErosionData config = this.visionRealm$getBiomeConfig(level, pos);
        return config != null
                ? config.erosionStrength()
                : PlayerErosionHook.super.getErosionAccumulationRate(level, pos);
    }

    @Override
    public double getErosionReduceAmount(Level level, BlockPos pos) {
        BiomeErosionData config = this.visionRealm$getBiomeConfig(level, pos);
        return config != null
               ? config.reduceAmount()
               : PlayerErosionHook.super.getErosionReduceAmount(level, pos);
    }

    @Override
    public boolean canErosionChange(Level level, BlockPos pos) {
       if (!this.isCreative() && !this.visionrealm$player.isSleeping() && !this.isSpectator()) {
           BiomeErosionData config = this.visionRealm$getBiomeConfig(level, pos);
           if (config != null) {
               Holder<ErosionType> type = config.erosionType();
               if (type != null) {
                   return type.value().isValidFor(this.visionrealm$player);
               }
           }
       }

       return false;
    }

    @Unique
    @Nullable
    protected BiomeErosionData visionRealm$getBiomeConfig(Level level, BlockPos pos) {
        return level.getBiome(pos).getData(BiomeErosionData.DATA_MAP_TYPE);
    }

    @Unique
    protected void visionRealm$resetTimer(Level level, BlockPos pos) {
        this.visionrealm$reduceTimer = this.getErosionReduceInterval(level, pos);
        this.visionrealm$risingTimer = this.getErosionAccumulationInterval(level, pos);
    }
}
