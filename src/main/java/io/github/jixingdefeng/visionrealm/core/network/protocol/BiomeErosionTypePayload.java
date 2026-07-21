package io.github.jixingdefeng.visionrealm.core.network.protocol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import io.github.jixingdefeng.visionrealm.core.erosion.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record BiomeErosionTypePayload(Map<ResourceKey<Biome>, Config> map) implements CustomPacketPayload {
    public static final Type<BiomeErosionTypePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "biome_erosion_type_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeErosionTypePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(
                            HashMap::new,
                            ResourceKey.streamCodec(Registries.BIOME),
                            ByteBufCodecs.fromCodec(Config.CODEC)
                    ),
                    BiomeErosionTypePayload::map,
                    BiomeErosionTypePayload::new
            );

    public static BiomeErosionTypePayload create(Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> map) {
        Map<ResourceKey<Biome>, Config> newMap = new HashMap<>();
        map.forEach((key, config) -> newMap.put(key, new Config(config)));
        return new BiomeErosionTypePayload(newMap);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public record Config(
            float erosionRate, double erosionStrength, float recoveryRate, double reduceAmount, ResourceKey<ErosionType> erosionType
    ) {
        public static Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.FLOAT.optionalFieldOf("erosion_rate", 0.01F)
                                .forGetter(Config::erosionRate),
                        Codec.DOUBLE.optionalFieldOf("erosion_strength", 0.005)
                                .forGetter(Config::erosionStrength),
                        Codec.FLOAT.optionalFieldOf("recovery_rate", 0.3F)
                                .forGetter(Config::recoveryRate),
                        Codec.DOUBLE.optionalFieldOf("reduce_amount", 0.0025)
                                .forGetter(Config::reduceAmount),
                        ResourceKey.codec(ModRegistries.EROSION_TYPE)
                                .fieldOf("erosion_type")
                                .forGetter(Config::erosionType)
                ).apply(instance, Config::new)
        );

        public Config(BiomeErosionManager.BiomeConfig config) {
            this(config.erosionRate(), config.erosionStrength(), config.recoveryRate(), config.reduceAmount(), config.erosionType().getKey());
        }

        @Nullable
        public BiomeErosionManager.BiomeConfig converted(RegistryAccess access) {
            return access.holder(this.erosionType)
                    .map(holder ->
                            new BiomeErosionManager.BiomeConfig(
                                    this.erosionRate, this.erosionStrength, this.recoveryRate, this.reduceAmount, holder)
                    ).orElse(null);
        }
    }
}
