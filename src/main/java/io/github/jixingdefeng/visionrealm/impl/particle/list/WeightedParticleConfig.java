package io.github.jixingdefeng.visionrealm.impl.particle.list;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.api.particle.ParticleConfig;
import io.github.jixingdefeng.visionrealm.api.particle.SingletonParticleConfig;
import io.github.jixingdefeng.visionrealm.common.util.particle.ParticleTemplates;
import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.particle.config.ParticleConfigStore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
 *   "values": [
 *      {
 *        "type": "..."
 *        "weight": 3,
 *      }
 *   ]
 * }
 * </pre>
 *
 * @author JiXingDeFeng
 * @see ParticleConfig
 * @see SingletonParticleConfig
 * @since 0.0.1-dev
 */
public class WeightedParticleConfig implements ParticleConfig {
    public static final MapCodec<WeightedParticleConfig> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ArrayWeightRandomList.codec(ParticleConfig.LOCATION_CODEC.fieldOf("config"))
                            .forGetter(WeightedParticleConfig::getConfigList)
            ).apply(instance, WeightedParticleConfig::new)
    );
    public static final ResourceLocation type = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "weighted");
    protected final ArrayWeightRandomList<ResourceLocation> randomList;
    protected final RandomSource random;

    public static WeightedParticleConfig create(
            @Nullable RandomSource random,
            List<ResourceLocation> particleConfigs,
            Function<ResourceLocation, Integer> weight
    ) {
        ArrayWeightRandomList<ResourceLocation> randomList = new ArrayWeightRandomList<>(particleConfigs, weight);
        return new WeightedParticleConfig(random != null ? random : RandomSource.create(), randomList);
    }

    public WeightedParticleConfig(ArrayWeightRandomList<ResourceLocation> randomList) {
        this(RandomSource.create(), randomList);
    }

    public WeightedParticleConfig(RandomSource random, ArrayWeightRandomList<ResourceLocation> randomList) {
        this.randomList = randomList;
        this.random = random;
    }

    @NotNull
    public List<ResourceLocation> unwrapLocation() {
        return this.randomList.unwrapValue();
    }

    public ArrayWeightRandomList<ResourceLocation> getConfigList() {
        return this.randomList;
    }

    @NotNull
    @Override
    public SingletonParticleConfig getSingleton() {
        return this.randomList.getRandom(this.random)
                .flatMap(location -> ParticleConfigStore.getInstance()
                        .flatMap(manager -> Optional.ofNullable(manager.get(location))))
                .map(ParticleConfig::getSingleton)
                .orElse(ParticleTemplates.empty().getSingleton());
    }

    @NotNull
    @Override
    public ResourceLocation getType() {
        return type;
    }

    @NotNull
    @Override
    public List<ParticleConfig> unwrap() {
        return this.randomList.unwrapValue().stream()
                .map(location -> ParticleConfigStore.getInstance()
                        .flatMap(manager -> Optional.ofNullable(manager.get(location))))
                .flatMap(Optional::stream)
                .toList();
    }
}
