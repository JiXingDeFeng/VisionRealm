package io.github.jixingdefeng.visionrealm.impl.particle.list;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Weighted particle configuration for random selection.
 * <p>
 * This class wraps a list of weighted particle configurations, allowing random
 * selection based on weights. It is used when multiple particle effects can be
 * triggered for the same erosion type.
 *
 * <p><b>JSON Structure:</b>
 * <pre>
 * {
 *   "type": "visionrealm:weighted",
 *   "particles": [
 *      {
 *       "particle": {...},
 *       "weight": 3
 *      }
 *   ]
 * }
 * </pre>
 *
 * @author JiXingDeFeng
 * @see ParticleConfig
 * @see SingletonParticleConfig
 * @see WeightedConfig
 * @since 0.0.1-dev-1
 */
public class WeightedParticleConfig implements ParticleConfig {
    public static final MapCodec<WeightedParticleConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.list(WeightedConfig.CODEC).fieldOf("particles").forGetter(WeightedParticleConfig::getConfigList)
            ).apply(instance, WeightedParticleConfig::new)
    );
    public static final ResourceLocation type = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "weighted");
    protected final WeightedRandomList<WeightedConfig> particleConfigs;
    protected final RandomSource random;

    public static WeightedParticleConfig create(List<ParticleConfig> particleConfigs) {
        List<WeightedConfig> weightedConfigs = new ArrayList<>(particleConfigs.size());
        for (ParticleConfig particleConfig : particleConfigs) {
            weightedConfigs.add(WeightedConfig.create(particleConfig));
        }

        return new WeightedParticleConfig(weightedConfigs);
    }

    public WeightedParticleConfig(List<WeightedConfig> particleConfigs) {
        this(RandomSource.create(), particleConfigs);
    }

    public WeightedParticleConfig(RandomSource random, List<WeightedConfig> particleConfigs) {
        this.particleConfigs = WeightedRandomList.create(particleConfigs);
        this.random = random;
    }

    /**
     * A single entry in a weighted particle configuration list.
     * <p>
     * This record pairs a particle configuration with its weight for random selection.
     *
     * @param particleConfig The particle configuration
     * @param weight         The weight for random selection
     * @author JiXingDeFeng
     * @see WeightedParticleConfig
     * @since 0.0.1-dev-1
     */
    public record WeightedConfig(@NotNull ParticleConfig particleConfig, @NotNull Weight weight) implements WeightedEntry {
        public static final Codec<WeightedConfig> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ParticleConfig.MAP_CODEC.forGetter(WeightedConfig::particleConfig),
                        Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(WeightedConfig::getWeight)
                ).apply(instance, WeightedConfig::new)
        );

        public static WeightedConfig create(ParticleConfig particleConfig) {
            return new WeightedConfig(particleConfig, Weight.of(1));
        }

        @NotNull
        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }

    @Override
    public SingletonParticleConfig getSingleton() {
        return this.particleConfigs.getRandom(this.random)
                .map(WeightedConfig::particleConfig)
                .map(ParticleConfig::getSingleton)
                .orElse(null);
    }

    @NotNull
    @Override
    public ResourceLocation getType() {
        return type;
    }

    @NotNull
    @Override
    public List<ParticleConfig> getList() {
        return this.getConfigList().stream()
                .map(WeightedConfig::particleConfig)
                .toList();
    }

    public List<WeightedConfig> getConfigList() {
        return this.particleConfigs.unwrap();
    }
}
