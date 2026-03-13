package io.github.fengguoshuzhu.visionrealm.impl.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.fengguoshuzhu.visionrealm.api.particle.ParticleConfig;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import org.jetbrains.annotations.NotNull;

public class WeightedParticleConfig extends ModifiableParticleConfig implements WeightedEntry {
    public static final Codec<WeightedParticleConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ParticleConfig.CODEC.forGetter((WeightedParticleConfig config) -> config),
                    Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(WeightedParticleConfig::getWeight)
            ).apply(instance, (WeightedParticleConfig::new))
    );
    private final Weight weight;

    public WeightedParticleConfig(ParticleConfig particleConfig, Weight weight) {
        super(particleConfig);
        this.weight = weight;
    }

    @NotNull
    @Override
    public Weight getWeight() {
        return this.weight;
    }
}
