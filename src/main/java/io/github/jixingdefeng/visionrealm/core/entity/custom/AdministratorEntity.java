package io.github.jixingdefeng.visionrealm.core.entity.custom;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.github.jixingdefeng.visionrealm.api.entity.LivingEntityExtensions;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.provider.EntityParticleProvider;
import io.github.jixingdefeng.visionrealm.core.Config;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import io.github.jixingdefeng.visionrealm.core.entity.ai.goal.entity.administrator.InterferenceGoal;
import io.github.jixingdefeng.visionrealm.core.entity.ai.goal.entity.administrator.ObservationGoal;
import io.github.jixingdefeng.visionrealm.core.particle.ModParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.IntFunction;

public class AdministratorEntity extends PathfinderMob implements EntityParticleProvider, LivingEntityExtensions, SecondaryTarget {
    public static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/entity/administrator/administrator_1.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/entity/administrator/administrator_2.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/entity/administrator/administrator_3.png"),
    };
    public static final String[] NAMES = {
            "Administrator",
            "UnknownUser316"
    };
    protected static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(AdministratorEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> TELEPORT_TARGET_PHASE =  SynchedEntityData.defineId(AdministratorEntity.class, EntityDataSerializers.INT);
    public final AnimationState[] TELEPORT_TARGET_ANIM = {new AnimationState(), new AnimationState()};
    public final AnimationState OBSERVE_ANIM = new AnimationState();
    protected final boolean isReally;
    protected final Map<UUID, Integer> ignoredEntities = Maps.newHashMap();
    protected final Map<UUID, AngerType> secondaryTargets = Maps.newHashMap();
    protected final int TEXTURE_ID = this.getRandom().nextInt(AdministratorEntity.TEXTURES.length);
    protected final String NAME;
    protected int existenceTime = this.getExistenceTime();
    protected int grabAnimTimer = 0;
    protected int teleportTargetAnimTimer = 0;
    protected int observeAnimTimer = 0;
    protected int modeSwitchTimer = 0;

    public AdministratorEntity(EntityType<? extends AdministratorEntity> type, Level level) {
        super(type, level);
        this.isReally = false;
        this.NAME = NAMES[this.getRandom().nextInt(NAMES.length)];
        this.setCustomNameVisible(true);
    }

    @NotNull
    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 100.0)
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10000D)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 10000D)
                .add(Attributes.BURNING_TIME, 0.0D);
    }

    public static void initEntity(@NotNull AdministratorEntity entity, Player player, @NotNull BehaviorMode behaviorMode) {
        entity.setBehaviorMode(behaviorMode);
        entity.addIgnoredEntities(player, 200);
        entity.setTarget(player);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.updateExistenceTime();
            this.updateSurvivalStatus();
        } else {
            this.updateAnimation();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            this.updateTheIgnoreList();
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        BehaviorMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getBehaviorMode())
                .result()
                .ifPresent(tag -> nbt.put("visionRealm:behaviorMode", tag));
        nbt.putInt("visionRealm:existenceTime", this.getExistenceTime());
        if (this.getTarget() != null) {
            nbt.putUUID("source", this.getTarget().getUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        if (nbt.contains("visionRealm:behaviorMode")) {
            BehaviorMode.CODEC.parse(NbtOps.INSTANCE, nbt.get("visionRealm:behaviorMode"))
                    .result()
                    .ifPresent(this::setBehaviorMode);
        }

        if (nbt.contains("visionRealm:existenceTime")) {
            this.setExistenceTime(nbt.getInt("visionRealm:existenceTime"));
        }

        if (this.level() instanceof ServerLevel level) {
            if (nbt.contains("source")) {
                Entity entity = level.getEntity(nbt.getUUID("source"));
                if (entity != null) {
                    if (entity instanceof LivingEntity livingEntity) {
                        this.setTarget(livingEntity);
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.transferImmediately(source)) {
            this.tickDeath();
            return false;
        } else if (this.immunityTheDamage(source)) {
            return false;
        } else {
            amount = Math.min(amount / 3, 7);
            boolean damaged = super.hurt(source, amount);

            if (!this.isAlive()) {
                this.tickDeath();
            }

            return damaged;
        }
    }

    @Override
    protected void tickDeath() {
        if (!this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MODE, BehaviorMode.EMPTY.getId());
        builder.define(TELEPORT_TARGET_PHASE, 0);
    }

    @Override
    public final void setCustomName(@Nullable Component name) {
    }

    @Nullable
    @Override
    public SingletonParticleConfig getHurtParticles(DamageSource source, float amount) {
        return SingletonParticleConfig.create(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.CustomBlocks.ERROR_BLOCK.get().defaultBlockState()),
                5, 0, 0.65, 1.0, 0.65, 0, 1.5, 0);
    }

    @Nullable
    @Override
    public SingletonParticleConfig getTickParticles() {
        return SingletonParticleConfig.create(ModParticleTypes.ERROR_PARTICLE_1.get(), this.getBehaviorMode().getParticleCount(),
                0.0, 0.5, 0.75, 0.5, 0, 1.5, 0);
    }

    @Nullable
    @Override
    public SingletonParticleConfig getDeathParticles(DamageSource source) {
        return SingletonParticleConfig.create(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.CustomBlocks.ERROR_BLOCK.get().defaultBlockState()),
                50, 0, 0.65, 1.0, 0.65, 0, 1.5, 0);
    }

    @Override
    public int getParticleUpdateInterval() {
        return this.getBehaviorMode().getParticleSpawnInterval();
    }

    @NotNull
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }

    @NotNull
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.STONE_FALL;
    }

    @Override
    public Map<UUID, AngerType> getSecondaryTargets() {
        return this.secondaryTargets;
    }

    @NotNull
    @Override
    public Component getName() {
        return MutableComponent.create(new PlainTextContents.LiteralContents(this.isReally ? NAMES[1] : this.NAME));
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 5.0F;
    }

    @Override
    public boolean isSpectator() {
        return this.isReally;
    }

    public boolean canBeAttacked() {
        BehaviorMode behaviorMode = this.getBehaviorMode();
        return behaviorMode == BehaviorMode.ATTACK
                || behaviorMode == BehaviorMode.INTERFERENCE;
    }

    public boolean immunityTheDamage(DamageSource source) {
        return !(
                source.is(DamageTypes.GENERIC_KILL)
                        || this.canBeAttacked()
                        && (
                        source.is(DamageTypes.GENERIC)
                                || source.is(DamageTypes.MAGIC)
                                || source.is(DamageTypes.INDIRECT_MAGIC)
                                || Config.ADMINISTRATOR_VULNERABLE.get()
                                && !(
                                source.is(DamageTypes.IN_FIRE)
                                        || source.is(DamageTypes.ON_FIRE)
                                        || source.is(DamageTypes.LAVA)
                        )
                )
        );
    }

    public boolean transferImmediately(DamageSource source) {
        return source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.GENERIC_KILL);
    }

    public void setTeleportTargetStage(int AnimationPhase) {
        this.getEntityData().set(AdministratorEntity.TELEPORT_TARGET_PHASE, AnimationPhase);
    }

    public void setBehaviorMode(BehaviorMode behaviorMode) {
        this.resetState(this.getBehaviorMode());
        this.getEntityData().set(AdministratorEntity.MODE, behaviorMode.getId());
        this.setExistenceTime(behaviorMode.getExistenceTime());
        this.modeSwitchTimer = 50;
    }

    public void addIgnoredEntities(@NotNull LivingEntity entity, int ignoreTime) {
        this.ignoredEntities.put(entity.getUUID(), ignoreTime);
    }

    public void addSecondaryTarget(LivingEntity entity, AngerType angerType) {
        this.secondaryTargets.put(entity.getUUID(), angerType);
    }

    public void removeSecondaryTarget(LivingEntity entity) {
        this.secondaryTargets.remove(entity.getUUID());
    }

    public void setExistenceTime(int existenceTime) {
        this.existenceTime = existenceTime;
    }

    public BehaviorMode getBehaviorMode() {
        return BehaviorMode.byId(this.getEntityData().get(MODE));
    }

    public int getTextureId() {
        return this.isReally ? 1 : this.TEXTURE_ID;
    }

    public int getExistenceTime() {
        return this.existenceTime;
    }

    public int getTeleportTargetStage() {
        return this.getEntityData().get(AdministratorEntity.TELEPORT_TARGET_PHASE);
    }

    public boolean isReally() {
        return this.isReally;
    }

    protected void resetState(BehaviorMode behaviorMode) {
        switch (behaviorMode) {
            case BehaviorMode.INTERFERENCE -> this.setTeleportTargetStage(0);
        }
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ObservationGoal(this));
        this.goalSelector.addGoal(1, new InterferenceGoal(this));
    }

    protected void updateTheIgnoreList() {
        if (!this.ignoredEntities.isEmpty()) {
            for (UUID uuid : this.ignoredEntities.keySet()) {
                int ignoreTime = this.ignoredEntities.get(uuid) - 1;
                if (ignoreTime <= 0) {
                    this.ignoredEntities.remove(uuid);
                } else {
                    this.ignoredEntities.put(uuid, ignoreTime);
                }
            }
        }
    }

    protected void updateAnimation() {
        if (this.getTeleportTargetStage() == 1) {
            if (this.grabAnimTimer <= 0) {
                this.grabAnimTimer = 10;
                this.TELEPORT_TARGET_ANIM[0].start(this.tickCount);
            } else {
                --this.grabAnimTimer;
            }
        } else if (this.grabAnimTimer > 0) {
            this.grabAnimTimer = 0;
            this.TELEPORT_TARGET_ANIM[0].stop();
        }

        if (this.getTeleportTargetStage() == 2) {
            if (this.teleportTargetAnimTimer <= 0) {
                this.teleportTargetAnimTimer = 30;
                this.TELEPORT_TARGET_ANIM[1].start(this.tickCount);
            } else {
                --this.teleportTargetAnimTimer;
            }
        } else if (this.teleportTargetAnimTimer > 0) {
            this.teleportTargetAnimTimer = 0;
            this.TELEPORT_TARGET_ANIM[1].stop();
        }

        if (this.getBehaviorMode() == BehaviorMode.OBSERVATION) {
            if (this.observeAnimTimer <= 0) {
                this.observeAnimTimer = 40;
                this.OBSERVE_ANIM.start(this.tickCount);
            } else {
                --this.observeAnimTimer;
            }
        } else if (this.observeAnimTimer > 0) {
            this.observeAnimTimer = 0;
            this.OBSERVE_ANIM.stop();
        }
    }

    protected void updateSurvivalStatus() {
        if (this.modeSwitchTimer > 0) {
            --this.modeSwitchTimer;
        } else {
            if (this.getBehaviorMode() != BehaviorMode.MODIFY && this.getBehaviorMode() != BehaviorMode.EMPTY) {
                if (this.getTarget() == null || !this.getTarget().isAlive()) {
                    this.kill();
                } else if (this.getBehaviorMode() == BehaviorMode.OBSERVATION) {
                    Player player = this.level().getNearestPlayer(
                            TargetingConditions.forNonCombat().range(20),
                            this,
                            this.getX(),
                            this.getY(),
                            this.getZ()
                    );
                    if (
                            player != null && !this.ignoredEntities.containsKey(player.getUUID())
                                    || this.distanceTo(this.getTarget()) > this.getAttributeValue(Attributes.FOLLOW_RANGE)
                    ) {
                        this.kill();
                    }
                }
            }
        }
    }

    protected void updateExistenceTime() {
        if (this.existenceTime > 0) {
            --this.existenceTime;
        } else if (this.existenceTime == 0) {
            this.kill();
        }
    }

    public enum BehaviorMode implements StringRepresentable {
        /**
         * <p>
         * 默认模式，字面意思，空行为逻辑，什么都不会做
         * </p>
         * <p>
         * The default mode, literally, null behavior logic, does nothing
         * </p>
         */
        EMPTY(0, "Empty", fixed(200)),
        /**
         * <p>
         * 观察模式，只会观察目标行为等。
         * </p>
         * <p>
         * Observe the pattern, only observe the source behavior, etc.
         * </p>
         */
        OBSERVATION(1, "Observation", ofRange(1200, 6000), fixed(1), fixed(2)),
        /**
         * <p>
         * 修改模式，会修改世界的部分信息（如世界规则、结构等等）。
         * </p>
         * <p>
         * Modify mode, which will modify part of the world's information (such as world rules, structure, etc.).
         * </p>
         */
        MODIFY(2, "ModifyY", fixed(-1)),
        /**
         * <p>
         * 攻击模式，会追踪并攻击目标。
         * </p>
         * <p>
         * Attack mode that tracks and attacks the source.
         * </p>
         */
        ATTACK(3, "Attack", fixed(200)),
        /**
         * <p>
         * 干扰模式，会主动干涉目标行为。
         * </p>
         * <p>
         * Interference mode will actively interfere with the source behavior.
         * </p>
         */
        INTERFERENCE(4, "Interference", ofRange(600, 2400), ofRange(2, 5), fixed(2));

        public static final Codec<BehaviorMode> CODEC = StringRepresentable.fromEnum(BehaviorMode::values);
        public static final IntFunction<BehaviorMode> BY_ID = ByIdMap.continuous(BehaviorMode::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        private final RandomSource random = RandomSource.create();
        private final IntProvider existenceTime;
        private final IntProvider particleCount;
        private final IntProvider particleSpawnInterval;
        private final String name;
        private final int id;

        BehaviorMode(int id, String name, IntProvider existenceTime) {
            this(id, name, existenceTime, fixed(0), fixed(0));
        }

        BehaviorMode(
                int id,
                String name,
                IntProvider existenceTime,
                IntProvider particleCount,
                IntProvider particleSpawnInterval
        ) {
            this.id = id;
            this.name = name;
            this.existenceTime = existenceTime;
            this.particleCount = particleCount;
            this.particleSpawnInterval = particleSpawnInterval;
        }

        public static BehaviorMode byId(int id) {
            return BY_ID.apply(id);
        }

        public static IntProvider fixed(int value) {
            return ofRange(value, value);
        }

        public static IntProvider ofRange(int a, int b) {
            int max = Math.max(a, b);
            int min = Math.min(a, b);
            return UniformInt.of(min, max);
        }

        public int getId() {
            return this.id;
        }

        public int getExistenceTime() {
            return this.existenceTime.sample(this.random);
        }

        public int getParticleSpawnInterval() {
            return this.particleSpawnInterval.sample(this.random);
        }

        public int getParticleCount() {
            return this.particleCount.sample(this.random);
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

}

