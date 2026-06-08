package io.github.jixingdefeng.visionrealm.core.entity.custom;

import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.provider.EntityParticleProvider;
import io.github.jixingdefeng.visionrealm.common.util.world.pos.BlockPosUtil;
import io.github.jixingdefeng.visionrealm.impl.particle.singleton.ModifiableParticleConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Photophobic extends PathfinderMob implements EntityParticleProvider {
    private static final EntityDataAccessor<Boolean> IS_VANISHING = SynchedEntityData.defineId(Photophobic.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VANISH_TIMER = SynchedEntityData.defineId(Photophobic.class, EntityDataSerializers.INT);

    protected Photophobic(EntityType<? extends Photophobic> entityType, Level level) {
        super(entityType, level);
        this.setVanishTimer(this.getMaxVanishTime());
    }

    public static boolean checkPhotophobicSpawnRules(
            EntityType<? extends Photophobic> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        return (MobSpawnType.ignoresLightRequirements(spawnType) || Monster.isDarkEnoughToSpawn(level, pos, random))
                && checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.playVanishSounds();
        } else {
            this.updateVanishState();
        }
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_VANISHING, false);
        builder.define(VANISH_TIMER, 100);
    }

    @Nullable
    public SingletonParticleConfig getVanishParticles() {
        return ModifiableParticleConfig.of(ParticleTypes.LARGE_SMOKE, 8, 8, 0.02, this.getBoundingBox().getXsize() / 2,
                this.getBoundingBox().getYsize() / 2, this.getBoundingBox().getZsize() / 2, 0, this.getBoundingBox().getYsize() / 2, 0);
    }

    @Nullable
    public SoundEvent getVanishSound() {
        return null;
    }

    public int getMaxSafeBrightness() {
        return 10;
    }

    /**
     * CanBeErodedEntity's light sensitivity type:
     * <ul>
     *     <li><b>0</b> - Afraid of all light sources</li>
     *     <li><b>1</b> - Only afraid of artificial light (block light)</li>
     *     <li><b>2</b> - Only afraid of natural light (sky light)</li>
     * </ul>
     *
     * @return Light sensitivity type ID
     */
    @MagicConstant(intValues = {0, 1, 2})
    public int getPhotophobicType() {
        return 0;
    }

    public boolean considerSkyLight() {
        return this.getPhotophobicType() != 1;
    }

    public boolean considerBlockLight() {
        return this.getPhotophobicType() != 2;
    }

    public int getVanishTimer() {
        return this.getEntityData().get(VANISH_TIMER);
    }

    public boolean isVanishing() {
        return this.getEntityData().get(IS_VANISHING);
    }

    public int getVanishRate(int brightness) {
        return 1;
    }

    public int getRecoveryRate(int brightness) {
        return 1;
    }

    public int getMaxVanishTime() {
        return 500;
    }

    protected boolean shouldVanishInLight(Level level, BlockPos pos) {
        int maxBrightness = this.getMaxSafeBrightness();
        return this.considerBlockLight() && level.getBrightness(LightLayer.BLOCK, pos) >= maxBrightness
                || (this.considerSkyLight()
                && level.canSeeSky(pos)
                && level.isDay()
                && level.getBrightness(LightLayer.SKY, pos) >= maxBrightness
                && !level.isRaining())
                || this.isOnFire();
    }

    protected void setVanishing(boolean disappearing) {
        this.getEntityData().set(IS_VANISHING, disappearing);
    }

    protected void setVanishTimer(int vanishTimer) {
        this.getEntityData().set(VANISH_TIMER, vanishTimer);
    }

    private void updateVanishState() {
        Level level = this.level();
        BlockPos pos = this.getOnPos().offset(0, 1, 0);
        int brightness = BlockPosUtil.getLightLevel(level, pos, this.considerSkyLight(), this.considerBlockLight());
        if (this.shouldVanishInLight(level, pos)) {
            if (this.getVanishTimer() <= 0) {
                this.kill();
            } else {
                this.setVanishing(true);
                this.setVanishTimer(this.getVanishTimer() - this.getVanishRate(brightness));
                addVanishParticles();
            }
        } else if (this.getVanishTimer() < this.getMaxVanishTime()) {
            this.setVanishing(false);
            this.setVanishTimer(this.getVanishTimer() + this.getRecoveryRate(brightness));
        }
    }

    private void addVanishParticles() {
        SingletonParticleConfig particleConfig = this.getVanishParticles();
        if (particleConfig != null) {
            SingletonParticleConfig.spawnParticles(particleConfig, this.position(), this.level(), particleConfig.spreadY() + 1);
        }
    }

    private void playVanishSounds() {
        if (this.isVanishing()) {
            SoundEvent sound = this.getVanishSound();
            if (sound != null) {
                BlockPos pos = this.getOnPos().offset(0, 1, 0);
                float pitch = this.random.nextInt(90, 110) * 0.01F;
                this.level().playLocalSound(pos, sound, SoundSource.HOSTILE, 2.0F, pitch, true);
            }
        }
    }
}
