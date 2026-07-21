package io.github.jixingdefeng.visionrealm.api.event.particle;

import com.mojang.serialization.MapCodec;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.core.particle.config.ParticleConfigStore;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Event fired during mod initialization to register particle configuration types.
 * <p>
 * This event is used to register {@link MapCodec} implementations for different
 * particle configuration types. The registered codecs are then used by the
 * dispatch system to deserialize particle configurations from JSON.
 * <p>
 * This event must be posted on the mod event bus and is typically handled in
 * the mod constructor or a subscriber method.
 *
 * @author JiXingDeFeng
 * @see ParticleConfig
 * @see ParticleConfigStore
 * @since 0.0.1-dev
 */
public class RegistryParticleConfigTypeEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, MapCodec<? extends ParticleConfig>> particles = new HashMap<>();

    /**
     * Registers a particle configuration codec for the specified type.
     * <p>
     * The registered codec will be used when a JSON particle configuration has
     * a {@code type} field matching the given identifier.
     *
     * @param type  The type identifier (e.g., "visionrealm:singleton")
     * @param codec The MapCodec for deserializing this particle configuration type
     */
    public void register(ResourceLocation type, MapCodec<? extends ParticleConfig> codec) {
        this.particles.put(type, codec);
    }

    /**
     * Returns an immutable copy of all registered particle configuration codecs.
     *
     * @return A new map containing all registered type-codec mappings
     */
    public Map<ResourceLocation, MapCodec<? extends ParticleConfig>> getParticles() {
        return new HashMap<>(this.particles);
    }
}
