package io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.api.entity.component.NightmareCreature;
import io.github.jixingdefeng.visionrealm.api.entity.particle.EntityParticleProvider;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.content.sound.ModSounds;
import io.github.jixingdefeng.visionrealm.content.world.entity.ai.goal.ImprovedNearestAttackableTargetGoal;
import io.github.jixingdefeng.visionrealm.content.world.entity.ai.goal.LightAwareStrollGoal;
import io.github.jixingdefeng.visionrealm.content.world.entity.ai.goal.entity.ForgottenShadowGoal;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.Photophobic;
import io.github.jixingdefeng.visionrealm.core.util.particle.ParticleTemplates;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgottenShadow extends Monster implements
        Photophobic, NightmareCreature, EntityParticleProvider {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/entity/forgotten_shadow.png");
    private static final EntityDataAccessor<Boolean> ATTACK = SynchedEntityData.defineId(ForgottenShadow.class, EntityDataSerializers.BOOLEAN);
    public final AnimationState STANDING_ANIM = new AnimationState();
    public final AnimationState STANDBY_ANIM = new AnimationState();
    public final AnimationState ATTACK_ANIM = new AnimationState();
    private int standingAnimTimer = 0;
    private int standbyAnimTimer = 0;
    private int fadeParticleCooldown = 0;
    private boolean isNight = false;
    private boolean isRaining;

    public static boolean checkSpawnRules(
            EntityType<? extends ForgottenShadow> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && (MobSpawnType.ignoresLightRequirements(spawnType) || Monster.isDarkEnoughToSpawn(level, pos, random))
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random)
                && Photophobic.isLightLevelAcceptable(level, pos, LightFearType.ALL, 10);
    }

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 20.0D)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 20.0D)
                .add(Attributes.BURNING_TIME, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 7.5D)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ARMOR_TOUGHNESS, 10)
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D)
                .add(Attributes.SCALE, 1.5);
    }

    public ForgottenShadow(EntityType<? extends ForgottenShadow> entityType, Level level) {
        super(entityType, level);
        this.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 0, false, false));
        this.getNavigation().setCanFloat(true);
        this.isRaining = level.isRaining();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this, ForgottenShadow.class));
        this.targetSelector.addGoal(1, new ImprovedNearestAttackableTargetGoal<>(this, Player.class, 0, true));
        this.targetSelector.addGoal(2, new ImprovedNearestAttackableTargetGoal<>(this, AbstractVillager.class, 0, true));
        this.targetSelector.addGoal(2, new ImprovedNearestAttackableTargetGoal<>(this, Raider.class, 0, true));

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new ForgottenShadowGoal(this, 1.0D, true));

        this.goalSelector.addGoal(2, new LightAwareStrollGoal(this, 1.0D, 10, false, true));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK, false);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level instanceof ClientLevel clientLevel) {
            this.updateAnimation();

            ClientLevel.ClientLevelData data = clientLevel.getLevelData();
            boolean isRaining = data.isRaining();
            if (isRaining != this.isRaining) {
                this.isRaining = isRaining;
                this.getSwitchParticles().spawnParticles(this);
            }

            long dayTime = data.getDayTime() % 24000;
            this.isNight = dayTime >= 12000;
            if (!isRaining && (dayTime < 100 || dayTime >= 12000 && dayTime < 12100)) {
                if (this.fadeParticleCooldown <= 0) {
                    this.fadeParticleCooldown = 5;
                    this.getFadeParticles().spawnParticles(this);
                } else {
                    this.fadeParticleCooldown--;
                }
            }
        }
    }

    @Override
    public void swing(@NotNull InteractionHand hand, boolean updateSelf) {
        if (this.level().isClientSide) {
            this.ATTACK_ANIM.start(this.tickCount);
        } else {
            super.swing(hand, updateSelf);
        }
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

    @Override
    public boolean isWithinMeleeAttackRange(@NotNull LivingEntity entity) {
        return this.distanceToSqr(entity) <= 2.25 || super.isWithinMeleeAttackRange(entity);
    }

    @NotNull
    @Override
    public AABB getAttackBoundingBox() {
        Vec3 look = this.getLookAngle();
        Vec3 pos = this.position();

        Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();
        Vec3 center = pos.add(horizontalLook.scale(1));

        return new AABB(
                center.x - 1, center.y, center.z - 1,
                center.x + 1, center.y + 3.5, center.z + 1
        );
    }

    @Override
    public double getVisibilityPercent(@Nullable Entity lookingEntity) {
        double visibility = super.getVisibilityPercent(lookingEntity);
        if (this.isVisibleInDarkness(lookingEntity)) {
            int brightness = this.getBrightness(this.level(), this.blockPosition());
            float percent = Math.max(0.1F, brightness / (float) this.getMaxAcceptBrightness());
            visibility *= percent;
        }

        return visibility;
    }

    @Override
    public LightFearType getLightFearType() {
        return LightFearType.ALL;
    }

    @Override
    public int getMaxAcceptBrightness() {
        return 10;
    }

    @NotNull
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return ModSounds.FORGOTTEN_SHADOW_HURT.get();
    }

    @NotNull
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FORGOTTEN_SHADOW_DEATH.get();
    }

    @Nullable
    @Override
    public SingletonParticleConfig getDeathParticles(DamageSource source) {
        return ParticleTemplates.LARGE_SMOKE.copy()
                .count(50)
                .spread(0.65, 2, 0.65)
                .yOffset(2.5);
    }

    @Override
    @NotNull
    public Calculation getCalculation() {
        return Calculation.DAMAGE_PERCENTAGE;
    }

    @Override
    public double getValue() {
        return 0.000875;
    }

    public void setAttack(boolean attacking) {
        this.getEntityData().set(ATTACK, attacking);
    }

    public SingletonParticleConfig getSwitchParticles() {
        return ParticleTemplates.LARGE_SMOKE.copy()
                .count(100)
                .spread(0.65, 2, 0.65)
                .yOffset(2.5);
    }

    public SingletonParticleConfig getFadeParticles() {
        return ParticleTemplates.LARGE_SMOKE.copy()
                .count(20)
                .spread(0.65, 2, 0.65)
                .yOffset(2.5);
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

    public boolean isHide() {
        return this.isNight || this.isRaining;
    }

    public boolean isVisibleInDarkness(Entity lookingEntity) {
        if (this.isOnFire()) {
            return false;
        }

        return lookingEntity instanceof LivingEntity livingEntity && !(
                livingEntity.hasEffect(MobEffects.NIGHT_VISION)
        );
    }

    public boolean immuneToTheDamage(DamageSource source) {
        return source.is(DamageTypes.FALL)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.DROWN);
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
