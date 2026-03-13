package io.github.fengguoshuzhu.visionrealm.api.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fengguoshuzhu.visionrealm.impl.particle.ImmutableParticleConfig;
import io.github.fengguoshuzhu.visionrealm.impl.particle.WeightedParticleConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Core interface for particle configurations and spawning operations.
 * <p>
 * This interface serves as a complete particle system facade, providing both
 * immutable data structures for particle configuration and utility methods
 * for spawning particles in various contexts. It supports two distinct modes:
 * </p>
 * <ul>
 *   <li><b>Multi-particle mode ({@code count > 0})</b>: Spawns multiple particles with random
 *       position and velocity variations following Gaussian distributions</li>
 *   <li><b>Directional particle mode ({@code count = 0})</b>: Spawns a single particle with
 *       directional velocity derived from spread parameters</li>
 * </ul>
 *
 * <p><b>JSON Structure:</b></p>
 * <pre>
 * {
 *   "particle": "minecraft:flame",           // WeightedParticleGroup type (simple or block-state particle)
 *   "count": 5,                               // Number of particles (integer or range)
 *   "speed": 0.1,                             // Velocity factor
 *   "spread": 2.0,                            // Position spread - number (uniform) or [x,y,z] array
 *   "offset": [0.5, 0.5, 0.5]                 // Position offset - number or [x,y,z] array
 * }
 * </pre>
 *
 * @author JiXingDeFeng
 * @see ImmutableParticleConfig
 * @see WeightedParticleConfig
 * @since 1.0.0
 */
public interface ParticleConfig {
    MapCodec<ParticleConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.PARTICLE_TYPE.byNameCodec().<ParticleOptions>dispatch(
                            ParticleOptions::getType,
                            ParticleType::codec
                    ).fieldOf("particle").forGetter(ParticleConfig::particleType),
                    IntProvider.NON_NEGATIVE_CODEC.fieldOf("count").forGetter(ParticleConfig::countProvider),
                    Codec.DOUBLE.optionalFieldOf("speed", 0.0).forGetter(ParticleConfig::speed),
                    Codec.either(Codec.DOUBLE, Vec3.CODEC).xmap(
                                    either -> either.map(
                                            spread -> new Vec3(spread, spread, spread),
                                            vec3 -> vec3
                                    ),
                                    vec3 -> vec3.x == vec3.y && vec3.y == vec3.z ? Either.left(vec3.x) : Either.right(vec3)
                            ).optionalFieldOf("spread", new Vec3(0.5, 0.5, 0.5))
                            .forGetter(config -> new Vec3(config.spreadX(), config.spreadY(), config.spreadZ())),
                    Codec.either(Codec.DOUBLE, Vec3.CODEC).xmap(
                                    either -> either.map(
                                            spread -> new Vec3(spread, spread, spread),
                                            vec3 -> vec3
                                    ),
                                    vec3 -> vec3.x == vec3.y && vec3.y == vec3.z ? Either.left(vec3.x) : Either.right(vec3)
                            ).optionalFieldOf("offset", new Vec3(0.5, 0.5, 0.5))
                            .forGetter(config -> new Vec3(config.xOffset(), config.yOffset(), config.zOffset()))
            ).apply(instance, (particleType, count, speed, spread, offset) ->
                    new ImmutableParticleConfig(particleType, count, speed, spread.x, spread.y, spread.z, offset.x, offset.y, offset.z)
            )
    );

    /**
     * Creates a new immutable particle configuration.
     *
     * @param particleType the type of particle to spawn
     * @param count the number of particles to spawn (if 0, spawns a single particle with directional velocity)
     * @param speed the speed parameter - when count > 0: standard deviation for random velocity;
     *              when count = 0: base speed magnitude multiplied with spread direction
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
     * Creates a new immutable particle configuration.
     *
     * @param particleType the type of particle to spawn
     * @param count the range of particles to spawn (e.g., UniformInt.of(3, 8));
     *              note: the special behavior for count = 0 does not apply here,
     *              as IntProvider does not support zero
     * @param speed the speed parameter - when count > 0: standard deviation for random velocity;
     *              when count = 0: base speed magnitude multiplied with spread direction
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
            IntProvider count,
            double speed,
            double spreadX,
            double spreadY,
            double spreadZ,
            double xOffset,
            double yOffset,
            double zOffset
    ) {
        return new ImmutableParticleConfig(particleType, count, speed, spreadX, spreadY, spreadZ, xOffset, yOffset, zOffset);
    }

    /**
     * Spawns particles at a world position using the particle's configured Y offset.
     * <p>
     * This is a convenience method that calls {@link #spawnParticles(ParticleConfig, Vec3, Level, double)}
     * with the particle's configured Y offset.
     *
     * @param particle the particle configuration
     * @param position the world position to spawn particles at
     * @param level    the level to spawn particles in
     */
    static void spawnParticles(@NotNull ParticleConfig particle, @NotNull Vec3 position, @NotNull Level level) {
        spawnParticles(particle, position, level, particle.yOffset());
    }

    /**
     * Spawns particles at a world position with a custom Y offset.
     * <p>
     * Uses the provided position as the base spawn point, then applies the particle's
     * configured X/Z offsets and the provided Y offset.
     *
     * @param particle the particle configuration
     * @param position the world position to spawn particles at
     * @param level    the level to spawn particles in
     * @param yOffset  vertical offset from the base position
     */
    static void spawnParticles(@NotNull ParticleConfig particle, @NotNull Vec3 position, @NotNull Level level, double yOffset) {
        spawnParticles(particle, level, position, particle.xOffset(), yOffset, particle.zOffset(), false);
    }

    /**
     * Spawns particles at a world position with full offset control.
     * <p>
     * This method handles both server and client-side spawning:
     * <ul>
     *   <li>On server: Uses {@link ServerLevel#sendParticles} for optimized network transmission</li>
     *   <li>On client: Falls back to {@link #spawnParticles(ParticleConfig, Vec3, Level, RandomSource, boolean)}</li>
     * </ul>
     *
     * @param particle the particle configuration
     * @param level    the level to spawn particles in
     * @param position the base spawn position (before offsets)
     * @param xOffset  X offset to add to base position
     * @param yOffset  Y offset to add to base position
     * @param zOffset  Z offset to add to base position
     * @param applyConfigOffset whether to add the particle's configured offsets to the spawn position
     */
    static void spawnParticles(
            @NotNull ParticleConfig particle,
            @NotNull Level level,
            @NotNull Vec3 position,
            double xOffset,
            double yOffset,
            double zOffset,
            boolean applyConfigOffset
    ) {
        if (applyConfigOffset) {
            position.add(particle.xOffset(), particle.yOffset(), particle.zOffset());
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    particle.particleType(),
                    position.x + xOffset,
                    position.y + yOffset,
                    position.z + zOffset,
                    particle.count(level.random),
                    particle.spreadX(),
                    particle.spreadY(),
                    particle.spreadZ(),
                    particle.speed()
            );
        } else {
            spawnParticles(particle, position.add(xOffset, yOffset, zOffset), level, level.getRandom(), false);
        }
    }

    /**
     * Spawns particles at a specific world position with configurable offset application.
     * <p>
     * This method handles particle spawning based on the configuration:
     * <ul>
     *   <li><b>Directional mode ({@code count == 0})</b>: Spawns a single particle with velocity
     *       {@code speed * (spreadX, spreadY, spreadZ)}. This creates a particle moving in a fixed direction.</li>
     *   <li><b>Multi-particle mode ({@code count > 0})</b>: Spawns multiple particles with
     *       Gaussian-distributed random positions and velocities:
     *       <ul>
     *         <li>Position variation: {@code nextGaussian() * spread}</li>
     *         <li>Velocity variation: {@code nextGaussian() * speed}</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p>The method can optionally apply the particle's configured X, Y, Z offsets to the
     * spawn position before generating particles.
     *
     * @param particle          the particle configuration
     * @param position          the world position to spawn particles at
     * @param level             the level to spawn particles in
     * @param random            the random source for Gaussian distributions
     * @param applyConfigOffset whether to add the particle's configured offsets to the spawn position
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

        int count = particle.count(random);
        if (count == 0) {
            double d0 = particle.speed() * particle.spreadX();
            double d1 = particle.speed() * particle.spreadY();
            double d2 = particle.speed() * particle.spreadZ();
            level.addParticle(particle.particleType(), false, position.x, position.y, position.z, d0, d1, d2);
        } else {
            for (int i = 0; i < count; i++) {
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

    /**
     * Spawns particles at the entity's position using this configuration.
     * <p>
     * Convenience method that delegates to {@link #spawnParticles(Level, Vec3)}.
     *
     * @param entity the entity to spawn particles at
     */
    default void spawnParticles(Entity entity) {
        spawnParticles(entity.level(), entity.position());
    }

    /**
     * Spawns particles at the specified world position using this configuration.
     * <p>
     * This method uses the particle's configured X, Y, Z offsets and does not apply
     * any additional offset processing
     *
     * @param level    the level to spawn particles in
     * @param position the world position to spawn particles at
     */
    default void spawnParticles(Level level, Vec3 position) {
        spawnParticles(this, position, level);
    }

    /**
     * Gets a random particle count within the configured range.
     * <p>
     * Delegates to the {@link IntProvider#sample(RandomSource)} method of the underlying
     * count provider, which handles all the complexity of generating random values
     * according to the specific provider type (uniform, constant, clamped normal, etc.).
     *
     * @param random the random source
     * @return a random count value from the configured provider
     */
    default int count(RandomSource random) {
        return this.countProvider().sample(random);
    }

    /**
     * Gets the type of particle to spawn.
     *
     * @return the particle options
     */
    ParticleOptions particleType();

    /**
     * Gets the provider for particle count.
     * <p>
     * This can be a constant value (e.g., {@link ConstantInt}) or a range
     * (e.g., {@link UniformInt}) for randomized particle counts.
     *
     * @return the count provider
     */
    IntProvider countProvider();

    /**
     * Gets the speed factor for particle movement.
     * <p>
     * In multi-particle mode (count > 0), this is the standard deviation for random velocity.
     * In directional mode (count = 0), this is multiplied with spread to determine velocity.
     *
     * @return the speed value
     */
    double speed();

    /**
     * Gets the X-axis spread or direction component.
     * <p>
     * In multi-particle mode (count > 0): standard deviation for X-axis position spread.
     * In directional mode (count = 0): X component of direction vector.
     *
     * @return the X spread value
     */
    double spreadX();

    /**
     * Gets the Y-axis spread or direction component.
     * <p>
     * In multi-particle mode (count > 0): standard deviation for Y-axis position spread.
     * In directional mode (count = 0): Y component of direction vector.
     *
     * @return the Y spread value
     */
    double spreadY();

    /**
     * Gets the Z-axis spread or direction component.
     * <p>
     * In multi-particle mode (count > 0): standard deviation for Z-axis position spread.
     * In directional mode (count = 0): Z component of direction vector.
     *
     * @return the Z spread value
     */
    double spreadZ();

    /**
     * Gets the X-axis offset from the spawn position.
     *
     * @return the X offset
     */
    double xOffset();

    /**
     * Gets the Y-axis offset from the spawn position.
     *
     * @return the Y offset
     */
    double yOffset();

    /**
     * Gets the Z-axis offset from the spawn position.
     *
     * @return the Z offset
     */
    double zOffset();
}
