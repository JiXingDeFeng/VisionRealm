package io.github.fengguoshuzhu.visionrealm.common.world.particle;

import io.github.fengguoshuzhu.visionrealm.api.world.particle.ParticleConfig;
import io.github.fengguoshuzhu.visionrealm.common.util.world.particle.ParticleTemplates;
import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.NotNull;

public class ModifiableParticleConfig implements ParticleConfig {
    private @NotNull ParticleOptions particleType;
    private int count;
    private double speed;
    private double spreadX;
    private double spreadY;
    private double spreadZ;
    private double xOffset;
    private double yOffset;
    private double zOffset;

    public static ModifiableParticleConfig of(@NotNull ParticleOptions particleType, int count, double speed) {
        return of(particleType, count, speed, 0, 0, 0);
    }

    public static ModifiableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ
    ) {
        return of(particleType, count, speed, spreadX, spreadY, spreadZ, 0);
    }

    public static ModifiableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double yOffset
    ) {
        return of(particleType, count, speed, spreadX, spreadY, spreadZ, 0, yOffset, 0);
    }

    public static ModifiableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        return new ModifiableParticleConfig(particleType, count, speed, spreadX, spreadY, spreadZ, xOffset, yOffset, zOffset);
    }

    public ModifiableParticleConfig(ParticleConfig particleConfig) {
        this.particleType = particleConfig.particleType();
        this.count = particleConfig.count();
        this.speed = particleConfig.speed();
        this.spreadX = particleConfig.spreadX();
        this.spreadY = particleConfig.spreadY();
        this.spreadZ = particleConfig.spreadZ();
        this.xOffset = particleConfig.xOffset();
        this.yOffset = particleConfig.yOffset();
    }

    private ModifiableParticleConfig(
            @NotNull ParticleOptions particleType,
            int count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        this.particleType = particleType;
        this.count = count;
        this.speed = speed;
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        this.spreadZ = spreadZ;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    @Override
    public ParticleOptions particleType() {
        return this.particleType;
    }

    @Override
    public int count() {
        return this.count;
    }

    @Override
    public double speed() {
        return this.speed;
    }

    @Override
    public double spreadX() {
        return this.spreadX;
    }

    @Override
    public double spreadY() {
        return this.spreadY;
    }

    @Override
    public double spreadZ() {
        return this.spreadZ;
    }

    @Override
    public double xOffset() {
        return this.xOffset;
    }

    @Override
    public double yOffset() {
        return this.yOffset;
    }

    @Override
    public double zOffset() {
        return this.zOffset;
    }

    public ModifiableParticleConfig particleType(ParticleOptions particleType) {
        this.particleType = particleType;
        return this;
    }

    public ModifiableParticleConfig count(int count) {
        this.count = count;
        return this;
    }

    public ModifiableParticleConfig speed(double speed) {
        this.speed = speed;
        return this;
    }

    public ModifiableParticleConfig spread(double spread) {
        return this.spread(spread, spread, spread);
    }

    public ModifiableParticleConfig spread(double spreadX, double spreadY, double spreadZ) {
        this.spreadX(spreadX);
        this.spreadY(spreadY);
        this.spreadZ(spreadZ);
        return this;
    }

    public ModifiableParticleConfig spreadX(double spreadX) {
        this.spreadX = spreadX;
        return this;
    }

    public ModifiableParticleConfig spreadY(double spreadY) {
        this.spreadY = spreadY;
        return this;
    }

    public ModifiableParticleConfig spreadZ(double spreadZ) {
        this.spreadZ = spreadZ;
        return this;
    }

    public ModifiableParticleConfig offset(double offset) {
        return this.offset(offset, offset, offset);
    }

    public ModifiableParticleConfig offset(double xOffset, double yOffset, double zOffset) {
        this.xOffset(xOffset);
        this.yOffset(yOffset);
        this.zOffset(zOffset);
        return this;
    }

    public ModifiableParticleConfig xOffset(double xOffset) {
        this.xOffset = xOffset;
        return this;
    }

    public ModifiableParticleConfig yOffset(double yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    public ModifiableParticleConfig zOffset(double zOffset) {
        this.zOffset = zOffset;
        return this;
    }

    /**
     * Creates a new mutable copy of this configuration.
     * <p>
     * The returned copy is independent and can be modified without affecting the original.
     * This is useful when you need to create variations of a base configuration.
     *
     * @return a new mutable particle configuration with the same values
     */
    public ModifiableParticleConfig copy() {
        return new ModifiableParticleConfig(this);
    }

    /**
     * Creates an immutable copy of this mutable configuration.
     *
     * @return a new immutable particle configuration with the same values
     */
    public ImmutableParticleConfig toImmutable() {
        return ImmutableParticleConfig.of(
                this.particleType,
                this.count,
                this.speed,
                this.spreadX,
                this.spreadY,
                this.spreadZ,
                this.xOffset,
                this.yOffset,
                this.zOffset
        );
    }
}
