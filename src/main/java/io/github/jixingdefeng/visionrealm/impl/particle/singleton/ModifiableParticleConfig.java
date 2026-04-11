package io.github.jixingdefeng.visionrealm.impl.particle.singleton;

import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.NotNull;

public class ModifiableParticleConfig implements SingletonParticleConfig {
    protected @NotNull ParticleOptions particleType;
    protected IntProvider count;
    protected double speed;
    protected double spreadX;
    protected double spreadY;
    protected double spreadZ;
    protected double xOffset;
    protected double yOffset;
    protected double zOffset;
    protected RandomSource random = RandomSource.create();

    public static ModifiableParticleConfig of(@NotNull ParticleOptions particleType, int count, double speed) {
        return of(particleType, count, count, speed, 0, 0, 0);
    }

    public static ModifiableParticleConfig of(@NotNull ParticleOptions particleType, int maxCount, int minCount, double speed) {
        return of(particleType, maxCount, minCount, speed, 0, 0, 0);
    }

    public static ModifiableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int maxCount,
            int minCount,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ
    ) {
        return of(particleType, maxCount, minCount, speed, spreadX, spreadY, spreadZ, 0);
    }

    public static ModifiableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int maxCount,
            int minCount,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double yOffset
    ) {
        return of(particleType, maxCount, minCount, speed, spreadX, spreadY, spreadZ, 0, yOffset, 0);
    }

    public static ModifiableParticleConfig of(
            @NotNull ParticleOptions particleType,
            int maxCount,
            int minCount,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        return new ModifiableParticleConfig(particleType, UniformInt.of(maxCount, minCount), speed, spreadX, spreadY, spreadZ, xOffset, yOffset, zOffset);
    }

    public ModifiableParticleConfig(SingletonParticleConfig particleConfig) {
        this.particleType = particleConfig.particleType();
        this.count = particleConfig.countProvider();
        this.speed = particleConfig.speed();
        this.spreadX = particleConfig.spreadX();
        this.spreadY = particleConfig.spreadY();
        this.spreadZ = particleConfig.spreadZ();
        this.xOffset = particleConfig.xOffset();
        this.yOffset = particleConfig.yOffset();
    }

    private ModifiableParticleConfig(
            @NotNull ParticleOptions particleType,
            IntProvider count,
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

    @NotNull
    @Override
    public ResourceLocation getType() {
        return ImmutableParticleConfig.type;
    }

    @Override
    public ParticleOptions particleType() {
        return this.particleType;
    }

    @Override
    public IntProvider countProvider() {
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

    public ModifiableParticleConfig count(int max, int min) {
        this.count = UniformInt.of(max, min);
        return this;
    }

    public ModifiableParticleConfig count(int count) {
        return count(count, count);
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
        return new ImmutableParticleConfig(
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
