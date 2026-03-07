package io.github.fengguoshuzhu.visionrealm.core.entity.custom.monster;

import io.github.fengguoshuzhu.visionrealm.api.world.entity.LivingEntityExtensions;
import io.github.fengguoshuzhu.visionrealm.api.world.particle.ParticleConfig;
import io.github.fengguoshuzhu.visionrealm.common.util.world.particle.ParticleTemplates;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.core.entity.ai.goal.LightAwareStrollGoal;
import io.github.fengguoshuzhu.visionrealm.core.entity.ai.goal.entity.TheForgottenGoal;
import io.github.fengguoshuzhu.visionrealm.core.entity.ai.goal.NearestTargetInRangeGoal;
import io.github.fengguoshuzhu.visionrealm.core.entity.custom.Photophobic;
import io.github.fengguoshuzhu.visionrealm.core.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TheForgottenEntity extends Photophobic implements LivingEntityExtensions {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/entity/the_forgotten.png");
    private static final EntityDataAccessor<Boolean> ATTACK = SynchedEntityData.defineId(TheForgottenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_MANNER = SynchedEntityData.defineId(TheForgottenEntity.class, EntityDataSerializers.INT);
    public final AnimationState STANDING_ANIM = new AnimationState();
    public final AnimationState STANDBY_ANIM = new AnimationState();
    public final AnimationState[] ATTACK_ANIM = {new AnimationState(), new AnimationState()};
    private int standingAnimTimer = 0;
    private int standbyAnimTimer = 0;

    public TheForgottenEntity(EntityType<? extends TheForgottenEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean checkSpawnRules(
            EntityType<? extends TheForgottenEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        return level.getDifficulty() != Difficulty.PEACEFUL && checkPhotophobicSpawnRules(type, level, spawnType, pos, random);
    }

    @NotNull
    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 50.0D)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 50.0D)
                .add(Attributes.BURNING_TIME, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ARMOR_TOUGHNESS, 10)
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 100.0D)
                .add(Attributes.SCALE, 2.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this, TheForgottenEntity.class));
        this.targetSelector.addGoal(1, new NearestTargetInRangeGoal<>(this, Player.class, 5.0F, true, true));
        this.targetSelector.addGoal(2, new NearestTargetInRangeGoal<>(this, AbstractVillager.class, 5.0F, true, true));

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new TheForgottenGoal(this, 1.0D, true));

        this.goalSelector.addGoal(2, new LightAwareStrollGoal(this, 1.0D, 8, false, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK, false);
        builder.define(ATTACK_MANNER, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.updateAnimation();
        }
    }

    @Override
    public void swing(@NotNull InteractionHand hand, boolean updateSelf) {
        if (this.level().isClientSide) {
            this.ATTACK_ANIM[this.getAttackManner()].start(this.tickCount);
        } else {
            super.swing(hand, updateSelf);
        }
    }

    @NotNull
    @Override
    public AABB getAttackBoundingBox() {
        Vec3 look = this.getAttackDirection();
        Vec3 pos = this.position();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = look.cross(up).normalize();

        double rightRange = 1.0;
        double leftRange = 1.0;
        double upRange = 2.0;

        switch (this.getAttackManner()) {
            case 0 -> rightRange = 0.25;
            case 1 -> {
                rightRange = 0.25;
                leftRange = 0.25;
                upRange = 2.5;
            }
        }

        Vec3 start = new Vec3(pos.x(), pos.y() - 0.5, pos.z())
                .add(look.scale(0.25));
        Vec3 end = new Vec3(pos.x(), pos.y() + 2.5, pos.z())
                .add(look.scale(upRange));

        return new AABB(start, end)
                .expandTowards(right.scale(rightRange))
                .expandTowards(right.scale(-leftRange));
    }

    @NotNull
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return ModSounds.THE_FORGOTTEN_HURT.get();
    }

    @NotNull
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.THE_FORGOTTEN_DEATH.get();
    }

    @Nullable
    @Override
    public ParticleConfig getDeathParticles(DamageSource source) {
        return ParticleTemplates.LARGE_SMOKE.copy()
                .count(50)
                .spread(0.65, 2, 0.65)
                .yOffset(2.5);
    }

    @Override
    public ParticleConfig getVanishParticles() {
        return ParticleTemplates.LARGE_SMOKE.copy()
                .spread(0.65, 2, 0.65)
                .yOffset(2.5);
    }

    @Override
    public boolean canShowDeathParticles(DamageSource source) {
        return !this.isVanishing();
    }

    @Override
    public boolean canMakeDeathSound() {
        return !this.isVanishing();
    }

    @Override
    public int getVanishRate(int brightness) {
        return brightness > 14 ? 2 : 1;
    }

    @Override
    public int getRecoveryRate(int brightness) {
        return brightness <= 1 ? 3 : brightness <= 5 ? 2 : 1;
    }

    @Override
    public int getMaxVanishTime() {
        return 200;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.immuneToTheDamage(source)) {
            return false;
        } else if (this.getTarget() == null) {
            amount /= 10;
        } else if (source.getEntity() != this.getTarget()) {
            amount /= 5;
        } else {
            amount /= 2;
        }

        return super.hurt(source, amount);
    }

    @Override
    protected void tickDeath() {
        if (!this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public void resetAttackAnimation() {
        for (AnimationState state : this.ATTACK_ANIM) {
            state.stop();
        }
    }

    public void setAttack(boolean attacking) {
        this.getEntityData().set(ATTACK, attacking);
        this.getEntityData().set(ATTACK_MANNER, this.random.nextInt(this.ATTACK_ANIM.length));
        if (!attacking) {
            this.resetAttackAnimation();
        }
    }

    /**
     * Attack type:
     * <ul>
     *     <li><b>0</b> - Sweep attack</li>
     *     <li><b>1</b> - Punching</li>
     * </ul>
     * @return Attack type ID
     */
    public int getAttackManner() {
        return this.getEntityData().get(ATTACK_MANNER);
    }

    public int getAttackAnimDuration() {
        return 35;
    }

    public int getDamageTime() {
        return 15;
    }

    public boolean isAttack() {
        return this.getEntityData().get(ATTACK);
    }

    public boolean immuneToTheDamage(DamageSource source) {
        return source.is(DamageTypes.FALL)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE);
    }

    public Vec3 getAttackDirection() {
        return this.getLookAngle();
    }

    private void updateAnimation() {
        if (this.standingAnimTimer <= 0) {
            this.standingAnimTimer = 5;
            this.STANDING_ANIM.start(this.tickCount);
        } else {
            --this.standingAnimTimer;
        }

        if (this.standbyAnimTimer <= 0) {
            this.standbyAnimTimer = 60;
            this.STANDBY_ANIM.start(this.tickCount);
        } else {
            --this.standbyAnimTimer;
        }
    }
}
