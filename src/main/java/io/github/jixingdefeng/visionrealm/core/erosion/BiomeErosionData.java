package io.github.jixingdefeng.visionrealm.core.erosion;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration data for biome-specific erosion behavior.
 *
 * @param erosionRate     Erosion speed. Actual interval between erosion increases
 *                        is calculated as {@code erosionRate * 1000} in ticks.
 * @param erosionStrength Erosion strength multiplier
 * @param recoveryRate    Recovery speed (erosion decrease). Actual interval between
 *                        recovery events is calculated as {@code recoveryRate * 1000} in ticks.
 * @param erosionType     The erosion type applied to this biome
 * @since 0.1.0
 */
public record BiomeErosionData(
        @Nullable Holder<ErosionType> erosionType,
        float erosionRate, double erosionStrength, float recoveryRate, double reduceAmount
) {
    public static final Codec<BiomeErosionData> CODEC = Codec.xor(
            RecordCodecBuilder.<BiomeErosionData>create(instance ->
                    instance.group(
                            RegistryFixedCodec.create(ModRegistries.EROSION_TYPE)
                                    .fieldOf("erosion_type")
                                    .forGetter(BiomeErosionData::erosionType),
                            Codec.FLOAT.optionalFieldOf("erosion_rate", 0.01F)
                                    .forGetter(BiomeErosionData::erosionRate),
                            Codec.DOUBLE.optionalFieldOf("erosion_strength", 0.005)
                                    .forGetter(BiomeErosionData::erosionStrength)
                    ).apply(instance, BiomeErosionData::create)
            ),
            RecordCodecBuilder.<BiomeErosionData>create(instance ->
                    instance.group(
                            Codec.FLOAT.fieldOf("recovery_rate")
                                    .forGetter(BiomeErosionData::recoveryRate),
                            Codec.DOUBLE.fieldOf("reduce_amount")
                                    .forGetter(BiomeErosionData::reduceAmount)
                    ).apply(instance, BiomeErosionData::create)
            )
    ).xmap(
            either -> either.map(
                    left -> left,
                    right -> right
            ),
            config -> config.erosionType != null
                      ? Either.left(config)
                      : Either.right(config)
    );
    public static final DataMapType<Biome, BiomeErosionData> DATA_MAP_TYPE =
            DataMapType.builder(
                            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "erosion_data"),
                            Registries.BIOME,
                            BiomeErosionData.CODEC
                    )
                    .synced(BiomeErosionData.CODEC, true)
                    .build();

    public static BiomeErosionData create(Holder<ErosionType> erosionType, float erosionRate, double erosionStrength) {
        if (erosionType.value().equals(ErosionType.NONE)) {
            throw new IllegalArgumentException("Erosion type cannot be NONE");
        } else {
            return new BiomeErosionData(erosionType, erosionRate, erosionStrength, 0, 0);
        }
    }

    public static BiomeErosionData create(float recoveryRate, double reduceAmount) {
        return new BiomeErosionData(null, 0, 0, recoveryRate, reduceAmount);
    }
}
