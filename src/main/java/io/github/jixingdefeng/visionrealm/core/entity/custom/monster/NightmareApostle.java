package io.github.jixingdefeng.visionrealm.core.entity.custom.monster;

import io.github.jixingdefeng.visionrealm.api.entity.component.NightmareCreature;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NightmareApostle extends Monster implements NightmareCreature {

    @NotNull
    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    public NightmareApostle(EntityType<? extends NightmareApostle> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(
            @NotNull ServerLevelAccessor level,
            @NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        if (spawnGroupData == null) {
            spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        }

        return spawnGroupData;
    }

    @Override
    @NotNull
    public Calculation getCalculation() {
        return Calculation.FIXATION;
    }

    @Override
    public double getValue() {
        return 0.004125;
    }
}
