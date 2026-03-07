package io.github.fengguoshuzhu.visionrealm.api.world.particle;

import io.github.fengguoshuzhu.visionrealm.common.world.particle.ModifiableParticleConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Provides custom particle effects for entities.
 * <p>
 * Entities implementing this interface can define particle effects for various life cycle events:
 * <ol>
 *   <li><b>Spawn particles</b> - Particles triggered when an entity is created/spawned</li>
 *   <li><b>Hurt particles</b> - Particles displayed when an entity takes damage</li>
 *   <li><b>Continuous particles</b> - Particles generated each tick during entity updates</li>
 *   <li><b>Death particles</b> - Particle effects played when an entity dies</li>
 * </ol>
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class FlameZombie extends Zombie implements EntityParticleProvider {
 *     @Override
 *     public Particle getDeathParticles() {
 *         return Particle.of(ParticleTypes.FLAME, 30, 0.02);
 *     }
 * }
 * }</pre>
 *
 * @see net.minecraft.client.renderer.entity.EntityRenderer#render
 * @see LivingEntity#aiStep()
 * @see LivingEntity#hurt(DamageSource, float)
 * @see LivingEntity#die(DamageSource)
 * @since 1.0.0
 */
public interface EntityParticleProvider {

    /**
     * Determines whether spawn particles should be created for this entity.
     * <p>
     * This method provides a conditional check before spawning particles during
     * entity creation. Can be overridden to implement conditions such as:
     * <ul>
     *   <li>Only spawn particles in certain dimensions</li>
     *   <li>Prevent particle spawning during world generation</li>
     *   <li>Control particles based on game rules or configuration</li>
     * </ul>
     * </p>
     *
     * @return {@code true} to allow spawn particle effects (default), {@code false} to suppress them
     *
     * @see #getSpawnParticles()
     * @since 1.0.0
     */
    default boolean canShowSpawnParticles() {
        return true;
    }

    /**
     * Gets the particle effect to play when the entity spawns.
     * <p>
     * This is called when the entity is first created or spawned into the world
     * Useful for birth effects, or summoning animations.
     * </p>
     *
     * @return particle configuration, or {@code null} for no effect (default)
     *
     * @since 1.0.0
     */
    @Nullable
    default ParticleConfig getSpawnParticles() {
        return null;
    }

    /**
     * Determines whether hurt particles should be created when the entity takes damage.
     * <p>
     * This conditional check allows for damage-source-specific or situational
     * control over hurt particle effects. For example:
     * <ul>
     *   <li>Suppress particles for specific damage types (like drowning)</li>
     *   <li>Different behavior based on damage amount</li>
     *   <li>Disable particles during invulnerability frames</li>
     * </ul>
     * </p>
     *
     * @param source the damage source causing the hurt
     * @param amount the amount of damage being dealt
     * @return {@code true} to allow hurt particle effects (default), {@code false} to suppress them
     *
     * @see #getHurtParticles(DamageSource, float)
     * @since 1.0.0
     */
    default boolean canShowHurtParticles(DamageSource source, float amount) {
        return true;
    }

    /**
     * Gets the particle effect to play when the entity is hurt.
     * <p>
     * This configuration is automatically used by {@link #makeHurtParticles(Entity, DamageSource, float)}
     * when the entity takes damage through {@link LivingEntity#hurt(DamageSource, float)}.
     * </p>
     * <p>
     * Return {@code null} (default) to disable hurt particle effects.
     * </p>
     *
     * @param damageSource the damage source causing the hurt
     * @param amount the amount of damage being dealt
     * @return particle configuration for hurt effects, or {@code null} for no effect
     *
     * @see #makeHurtParticles(Entity, DamageSource, float)
     * @see LivingEntity#hurt(DamageSource, float)
     * @since 1.0.0
     */
    @Nullable
    default ParticleConfig getHurtParticles(DamageSource damageSource, float amount) {
        return null;
    }

    /**
     * Gets the particle effect to play at regular intervals during entity updates.
     * <p>
     * This configuration is automatically used by {@link #makeTickParticles(Entity)}
     * every {@link #getParticleUpdateInterval()} ticks while the entity is alive.
     * Useful for ambient effects like breath, aura, or trail particles.
     * </p>
     * <p>
     * <b>Performance Consideration:</b> Tick particles are spawned frequently
     * (controlled by {@link #getParticleUpdateInterval()}). Use simple particle
     * configurations to maintain good performance.
     * </p>
     * <p>
     * <b>Usage:</b> Return {@code null} (default) to disable automatic tick particles.
     * To enable, override both this method and {@link #getParticleUpdateInterval()}.
     * </p>
     *
     * @return particle configuration for tick-based effects, or {@code null} to disable
     *
     * @see #makeTickParticles(Entity)
     * @see #getParticleUpdateInterval()
     * @see LivingEntity#aiStep()
     *
     * @since 1.0.0
     */
    @Nullable
    default ParticleConfig getTickParticles() {
        return null;
    }

    /**
     * Gets the tick interval for automatic particle updates.
     * <p>
     * Controls how frequently {@link #makeTickParticles(Entity)} is called to spawn
     * particles configured by {@link #getTickParticles()}.
     * </p>
     * <p>
     * Return 0 to completely disable automatic tick particle spawning.
     * The default interval is 10 ticks (0.5 seconds at 20 ticks per second).
     * </p>
     *
     * @return particle update interval in ticks (-1 = disabled, default: 10)
     *
     * @see #getTickParticles()
     * @see #makeTickParticles(Entity)
     * @since 1.0.0
     */
    default int getParticleUpdateInterval() {
        return 10;
    }

    /**
     * Determines whether death particles should be created when the entity dies.
     * <p>
     * This conditional check allows for death-source-specific control over
     * particle effects. For example:
     * <ul>
     *   <li>Different particles for different death causes</li>
     *   <li>Suppress particles in certain game modes or scenarios</li>
     *   <li>Conditional effects based on entity state at death</li>
     * </ul>
     * </p>
     *
     * @param source the damage source causing the death
     * @return {@code true} to allow death particle effects (default), {@code false} to suppress them
     *
     * @see #getDeathParticles(DamageSource)
     * @since 1.0.0
     */
    default boolean canShowDeathParticles(DamageSource source) {
        return true;
    }

    /**
     * Gets the particle effect to play when the entity dies.
     * <p>
     * This is called upon entity death. Typically used for explosion effects,
     * disintegration animations, or elemental bursts.
     * </p>
     *
     * @param source the damage source causing the death
     * @return particle configuration, default returns {@link ParticleTypes#POOF} with 20 particles
     * @see LivingEntity#die(DamageSource)
     * @since 1.0.0
     */
    @Nullable
    default ParticleConfig getDeathParticles(DamageSource source) {
        return ModifiableParticleConfig.of(ParticleTypes.POOF, 20, 0.02);
    }

    /**
     * Generates hurt particles for the entity when taking damage.
     * <p>
     * This method is automatically triggered through the Mixin injection in
     * {@link LivingEntity#hurt(DamageSource, float)}. When the entity takes damage,
     * the Mixin intercepts the hurt method and calls this particle method.
     * </p>
     *
     * <p><b>Default behavior (provided by {@code LivingEntityParticleMixin}):</b></p>
     * <ul>
     *   <li>Called automatically when entity takes damage via {@link LivingEntity#hurt}</li>
     *   <li>Checks {@link #canShowHurtParticles(DamageSource, float)} for permission</li>
     *   <li>Retrieves particle configuration via {@link #getHurtParticles(DamageSource, float)}</li>
     *   <li>Spawns particles at entity position if configuration exists</li>
     * </ul>
     *
     * <p><b>Note:</b> If you are not using the hurt particles provided by this interface,
     * you can safely ignore this method. The default implementation will do nothing if
     * {@link #getHurtParticles(DamageSource, float)} returns {@code null}.</p>
     *
     * @param entity the entity taking damage
     * @param source the damage source causing the damage
     * @param amount the amount of damage taken
     *
     * @see #getHurtParticles(DamageSource, float)
     * @see #canShowHurtParticles(DamageSource, float)
     * @see LivingEntity#hurt(DamageSource, float)
     * @since 1.0.0
     */
    default void makeHurtParticles(Entity entity, DamageSource source, float amount) {
        if (this.canShowHurtParticles(source, amount)) {
            ParticleConfig hurtParticles = this.getHurtParticles(source, amount);
            if (hurtParticles != null) {
                hurtParticles.spawnParticles(entity);
            }
        }
    }

    /**
     * Spawns ambient particles at regular intervals.
     * <p>
     * This method is automatically called from {@link #updateTickParticles(Entity)},
     * which is invoked via Mixin injection into the entity's {@link Entity#tick()} method.
     * It spawns particles configured by {@link #getTickParticles()} when available.
     * </p>
     *
     * <p><b>Default behavior (provided by {@code EntityParticleMixin}):</b></p>
     * <ul>
     *   <li>Called automatically during entity ticking at intervals controlled by {@link #updateTickParticles}</li>
     *   <li>Retrieves particle configuration via {@link #getTickParticles()}</li>
     *   <li>Spawns ambient particles at entity position</li>
     * </ul>
     *
     * <p><b>Note:</b> If you are not using tick particles (i.e., {@link #getTickParticles()}
     * returns {@code null}), you can safely ignore this method.</p>
     *
     * @param entity the entity for which to generate particles
     *
     * @see #getTickParticles()
     * @see #updateTickParticles(Entity)
     * @see Entity#tick()
     * @since 1.0.0
     */
    default void makeTickParticles(Entity entity) {
        ParticleConfig tickParticles = this.getTickParticles();
        if (tickParticles != null) {
            tickParticles.spawnParticles(entity);
        }
    }

    /**
     * Manages the timing and execution of tick-based particle effects.
     * <p>
     * This method is automatically called every tick through Mixin injection into
     * {@link Entity#tick()}. It maintains an internal counter and triggers
     * {@link #makeTickParticles(Entity)} at the configured interval.
     * </p>
     *
     * <p><b>Default behavior (provided by {@code EntityParticleMixin}):</b></p>
     * <ul>
     *   <li>Called every tick from {@link Entity#tick()} via Mixin</li>
     *   <li>Maintains an internal tick counter for particle intervals</li>
     *   <li>Calls {@link #makeTickParticles(Entity)} when interval threshold is reached</li>
     *   <li>Resets counter based on {@link #getParticleUpdateInterval()}</li>
     *   <li>Does nothing if {@link #getTickParticles()} returns {@code null}</li>
     * </ul>
     *
     * <p><b>For Custom Entities:</b></p>
     * <ul>
     *   <li>The Mixin automatically handles all entities implementing this interface</li>
     *   <li>No manual calls are required, even for custom entities</li>
     *   <li>Particle timing is fully managed by the Mixin system</li>
     * </ul>
     *
     * @param entity the entity being updated (typically {@code this})
     *
     * @see #makeTickParticles(Entity)
     * @see #getParticleUpdateInterval()
     * @see #getTickParticles()
     * @see Entity#tick()
     * @since 1.0.0
     */
    default void updateTickParticles(Entity entity) {}
}
