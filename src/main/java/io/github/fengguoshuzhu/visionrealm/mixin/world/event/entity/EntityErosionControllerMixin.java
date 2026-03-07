package io.github.fengguoshuzhu.visionrealm.mixin.world.event.entity;

import io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection.CanBeErosion;
import io.github.fengguoshuzhu.visionrealm.api.world.controller.entity.EntityErosionController;
import io.github.fengguoshuzhu.visionrealm.common.util.world.erosion.ErosionUtil;
import io.github.fengguoshuzhu.visionrealm.core.entity.ai.attributes.ModAttributes;
import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import io.github.fengguoshuzhu.visionrealm.handle.world.erosion.entity.EntityErosionHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class EntityErosionControllerMixin implements EntityErosionController {

    @Unique private final LivingEntity visionrealm$player = (LivingEntity) (Object) this;
    @Unique private int visionRealm$erosionReduceTimer;
    @Unique private int visionRealm$erosionRisingTimer;
    @Unique private AttributeInstance visionRealm$erosionAttribute;
    @Unique private Holder<Biome> visionRealm$biomeHolder;
    @Unique private ErosionType visionRealm$erosionType = ErosionType.NONE;

    @Unique
    private AttributeInstance visionRealm$getAttribute() {
        if (this.visionRealm$erosionAttribute == null) {
            this.visionRealm$erosionAttribute = this.visionrealm$player.getAttributes().getInstance(ModAttributes.EROSION);
        }

        return this.visionRealm$erosionAttribute;
    }

    @Override
    public void updateErosion() {
        Holder<Biome> biome = this.visionrealm$player.level().getBiome(this.visionrealm$player.blockPosition());
        if (!biome.equals(this.visionRealm$biomeHolder)) {
            this.visionRealm$biomeHolder = biome;
            ErosionType erosionType = ErosionUtil.getBiomeErosionType(biome.getKey());
            if (erosionType != this.visionRealm$erosionType) {
                this.visionRealm$erosionType = erosionType;
            }
        }

        if (this.canBeEroded(this.visionRealm$erosionType)) {
            double baseValue = this.visionrealm$player.getAttributeValue(ModAttributes.EROSION);
            double value = baseValue;
            int decreaseTime = this.getErosionDecreaseInterval(this.visionRealm$erosionType);

            if (this.canBeReducedNaturally(this.visionRealm$erosionType)) {
                if (this.visionRealm$erosionReduceTimer <= 0) {
                    this.visionRealm$erosionReduceTimer = decreaseTime;
                    value -= this.naturallyReducedValue(this.visionRealm$erosionType);
                } else {
                    this.visionRealm$erosionReduceTimer--;
                }
            }

            if (this.visionRealm$erosionRisingTimer <= 0) {
                this.visionRealm$erosionRisingTimer = 10;
                this.visionRealm$erosionReduceTimer = decreaseTime;
                value += 0.005;
            } else {
                this.visionRealm$erosionRisingTimer--;
            }

            if (value != baseValue) {
                this.visionRealm$getAttribute().setBaseValue(Math.max(0, Math.min(value, 1)));
            }
        }

        if (this.completeErosion(this.visionRealm$erosionType)) {
            EntityErosionHandler.tryErosion(
                    this.visionrealm$player,
                    this.visionrealm$player.level(),
                    this.visionrealm$player.position(),
                    this.visionRealm$erosionType
            );
        }
    }

    @Override
    public boolean completeErosion(ErosionType type) {
        return this.visionrealm$player instanceof CanBeErosion
                && this.visionrealm$player.getAttributeValue(ModAttributes.EROSION) >= 1.0;
    }

    @Override
    public boolean canBeEroded(ErosionType type) {
        return this.visionRealm$getAttribute() != null;
    }

    @Override
    public int getErosionDecreaseTimer(ErosionType type) {
        return this.visionRealm$erosionRisingTimer;
    }

    @Override
    public void setErosionDecreaseTimer(ErosionType type, int time) {
        this.visionRealm$erosionRisingTimer = time;
    }

    @Override
    public boolean canBeReducedNaturally(ErosionType type) {
        return true;
    }

    @Override
    public void setErosionType(ErosionType type) {
        this.visionRealm$erosionType = type;
    }

    @NotNull
    @Override
    public ErosionType getErosionType() {
        return Objects.requireNonNullElse(this.visionRealm$erosionType, ErosionType.NONE);
    }
}
