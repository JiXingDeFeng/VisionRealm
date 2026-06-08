package io.github.jixingdefeng.visionrealm.mixin.world.event.entity;

import com.google.common.collect.Maps;
import io.github.jixingdefeng.visionrealm.api.controller.entity.EntityErosionController;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.entity.CanBeErosionEntity;
import io.github.jixingdefeng.visionrealm.common.erosion.handle.infection.entity.EntityErosionHandler;
import io.github.jixingdefeng.visionrealm.common.util.erosion.ErosionUtil;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(Entity.class)
public class EntityErosionControllerMixin implements EntityErosionController {
    @Unique protected Map<ErosionType, Short> visionRealm$erosionProgress = Maps.newHashMap();
    @Unique protected Map<ErosionType, Short> visionRealm$previousErosionProgress = Maps.newHashMap();
    @Unique protected final Entity visionrealm$entity = (Entity) (Object) this;
    // 所有侵蚀类型中侵蚀度最大的侵蚀类型
    @Unique protected ErosionType visionRealm$typeWithMaxValue = ErosionType.NONE;
    // 所有侵蚀类型中侵蚀度最大的侵蚀类型的值
    @Unique protected short visionRealm$maxErosionProgress = 0;
    @Unique protected int visionRealm$erosionReduceTimer;
    @Unique protected int visionRealm$erosionRisingTimer;
    @Unique protected Holder<Biome> visionRealm$biomeHolder;

    @Override
    public void updateErosion() {
        Holder<Biome> biome = this.visionrealm$entity.level().getBiome(this.visionrealm$entity.blockPosition());
        ErosionType erosionType = ErosionUtil.getBiomeErosionType(biome.getKey());
        if (!biome.equals(this.visionRealm$biomeHolder)) {
            this.visionRealm$biomeHolder = biome;
        }

        this.visionRealm$updateErosion(erosionType);
    }

    @Override
    public boolean completeErosion(ErosionType type) {
        return this.visionrealm$entity instanceof CanBeErosion
                && this.visionRealm$maxErosionProgress >= 10000;
    }

    @Override
    public boolean canBeEroded(ErosionType type) {
        return false;
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
    public short getErosionProgress(ErosionType type) {
        return this.visionRealm$erosionProgress.get(type);
    }

    @Override
    public ErosionType getTypeWithMaxValue() {
        return this.visionRealm$typeWithMaxValue;
    }

    @Override
    public short getMaxErosionProgress() {
        return this.visionRealm$maxErosionProgress;
    }

    @Override
    public void setErosionProgress(ErosionType type, short progress) {
        this.visionRealm$erosionProgress.put(type, progress);
    }

    @Unique
    private void visionRealm$updateErosion(ErosionType type) {
        if (this.canBeEroded(type)) {
            short baseValue = this.visionRealm$erosionProgress.get(type);
            short value = baseValue;
            int decreaseTime = this.getErosionDecreaseInterval(type);

            if (this.canBeReducedNaturally(type)) {
                if (this.visionRealm$erosionReduceTimer <= 0) {
                    this.visionRealm$erosionReduceTimer = decreaseTime;
                    value -= this.naturallyReducedValue(type);
                } else {
                    this.visionRealm$erosionReduceTimer--;
                }
            }

            if (this.visionRealm$erosionRisingTimer <= 0) {
                this.visionRealm$erosionRisingTimer = 10;
                this.visionRealm$erosionReduceTimer = decreaseTime;
                value += 50;
            } else {
                this.visionRealm$erosionRisingTimer--;
            }

            if (value != baseValue) {
                this.visionRealm$erosionProgress.put(type, value);
            }
        }

        if (this.completeErosion(type)) {
            EntityErosionHandler.tryErosion(
                    (CanBeErosionEntity<?, ?, ?>) this.visionrealm$entity,
                    this.visionrealm$entity.level(),
                    this.visionrealm$entity.position(),
                    type
            );
        }
    }
}
