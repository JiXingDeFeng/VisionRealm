package io.github.fengguoshuzhu.visionrealm.api.world.particle;

import io.github.fengguoshuzhu.visionrealm.common.world.particle.ImmutableParticleConfig;
import io.github.fengguoshuzhu.visionrealm.common.world.particle.ModifiableParticleConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface ParticleConfig {

    /**
     * Creates a new immutable particle configuration.
     *
     * @param particleType the type of particle to spawn
     * @param count the number of particles to spawn (if 0, spawns a single particle with directional velocity)
     * @param speed the speed parameter - when count > 0: standard deviation for random velocity;
     *             when count = 0: base speed magnitude multiplied with spread direction
     * @param spreadX when count > 0: standard deviation for X-axis position spread;
     *                when count = 0: X component of direction vector (multiplied by speed)
     * @param spreadY when count > 0: standard deviation for Y-axis position spread;
     *                when count = 0: Y component of direction vector (multiplied by speed)
     * @param spreadZ when count > 0: standard deviation for Z-axis position spread;
     *                when count = 0: Z component of direction vector (multiplied by speed)
     * @param xOffset X-axis offset from entity's center
     * @param yOffset Y-axis offset from entity's base position
     * @param zOffset Z-axis offset from entity's center
     * @return a new immutable particle configuration
     */
    static ParticleConfig create(
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
        return ImmutableParticleConfig.of(particleType, count, speed, spreadX, spreadY, spreadZ, xOffset, yOffset, zOffset);
    }

    /**
     * Spawns particles around the entity's position.
     *
     * @param particle particle configuration
     * @param entity         the source entity
     * @param level          the server level
     */
    static void spawnParticles(@NotNull ParticleConfig particle, @NotNull Entity entity, @NotNull Level level) {
        spawnParticles(particle, entity, level, particle.yOffset());
    }

    /**
     * Spawns particles around the entity's position with Y offset.
     *
     * @param particle particle configuration
     * @param entity         the source entity
     * @param level          the server level
     * @param yOffset        vertical offset from entity's base position
     */
    static void spawnParticles(@NotNull ParticleConfig particle, @NotNull Entity entity, @NotNull Level level, double yOffset) {
        spawnParticles(particle, entity, level, particle.xOffset(), yOffset, particle.zOffset());
    }

    /**
     * Spawns particles around the entity's position with custom offsets.
     *
     * @param particle particle configuration
     * @param entity         the source entity
     * @param level          the server level
     * @param xOffset        horizontal X offset
     * @param yOffset        vertical Y offset
     * @param zOffset        horizontal Z offset
     */
    static void spawnParticles(
            @NotNull ParticleConfig particle,
            @NotNull Entity entity,
            @NotNull Level level,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        double x = entity.getBoundingBox().getCenter().x + xOffset;
        double y = entity.getY() + yOffset;
        double z = entity.getBoundingBox().getCenter().z + zOffset;
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle.particleType(), x, y, z, particle.count(), particle.spreadX(), particle.spreadY(), particle.spreadZ(), particle.speed());
        } else {
            spawnParticles(particle, entity.position().add(xOffset, yOffset, zOffset), level, entity.getRandom(), false);
        }
    }

    /**
     * Spawns particles at a specific world position.
     * <p>
     * This method handles particle spawning based on the configuration:
     * <ul>
     *   <li>If {@code count == 0}: Spawns a single particle with directional velocity.
     *       The velocity is calculated as {@code speed * (spreadX, spreadY, spreadZ)}.</li>
     *   <li>If {@code count > 0}: Spawns multiple particles with random positions and velocities.
     *       Position randomness follows Gaussian distribution with standard deviation {@code spread}.
     *       Velocity randomness follows Gaussian distribution with standard deviation {@code speed}.</li>
     * </ul>
     *
     * @param particle the particle configuration
     * @param position the world position to spawn particles at
     * @param level the level to spawn particles in
     * @param random the random source to use for position and velocity variations
     */
    static void spawnParticles(
            @NotNull ParticleConfig particle,
            @NotNull Vec3 position,
            @NotNull Level level,
            RandomSource random,
            boolean applyConfigOffset
    ) {
        if (applyConfigOffset) {
            position.add(particle.xOffset(), particle.yOffset(), particle.zOffset());
        }

        if (particle.count() == 0) {
            double d0 = particle.speed() * particle.spreadX();
            double d1 = particle.speed() * particle.spreadY();
            double d2 = particle.speed() * particle.spreadZ();
            level.addParticle(particle.particleType(), false, position.x, position.y, position.z, d0, d1, d2);
        } else {
            for (int i = 0; i < particle.count(); i++) {
                double d0 = random.nextGaussian() * particle.spreadX();
                double d1 = random.nextGaussian() * particle.spreadY();
                double d2 = random.nextGaussian() * particle.spreadZ();
                level.addParticle(
                        particle.particleType(),
                        false, position.x + d0, position.y + d1, position.z + d2,
                        random.nextGaussian() * particle.speed(),
                        random.nextGaussian() * particle.speed(),
                        random.nextGaussian() * particle.speed()
                );
            }
        }
    }

    default void spawnParticles(Entity entity) {
        spawnParticles(this, entity, entity.level());
    }

    ParticleOptions particleType();

    int count();

    double speed();

    double spreadX();

    double spreadY();

    double spreadZ();

    double xOffset();

    double yOffset();

    double zOffset();
}
