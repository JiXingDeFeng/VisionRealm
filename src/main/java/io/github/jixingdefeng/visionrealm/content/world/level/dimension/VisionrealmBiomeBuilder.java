package io.github.jixingdefeng.visionrealm.content.world.level.dimension;

import com.mojang.datafixers.util.Pair;
import io.github.jixingdefeng.visionrealm.content.world.level.biome.ModBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Biome builder for the VisionRealm dimension.
 * <p>
 * This class extends {@link OverworldBiomeBuilder} to create a customized biome distribution
 * for the VisionRealm dimension. It preserves the original overworld biome generation
 * structure while injecting custom biomes specific to VisionRealm.
 * <p>
 * The biome placement logic is based on the same climate parameter system (temperature,
 * humidity, continentalness, erosion, depth, and weirdness) used by the vanilla overworld.
 * Custom biomes are defined in {@link ModBiomes} and are assigned to specific climate
 * ranges using a matrix approach similar to the vanilla {@code OverworldBiomeBuilder}.
 *
 * @author JiXingDeFeng (Maintainer)
 * @since 0.1.0
 * @see OverworldBiomeBuilder
 * @see ModBiomes
 */
public class VisionrealmBiomeBuilder extends OverworldBiomeBuilder {
    private final Climate.Parameter[] temperatures = new Climate.Parameter[]{
            Climate.Parameter.span(-1.0F, -0.6F),   // T0: 极寒
            Climate.Parameter.span(-0.6F, -0.25F),  // T1: 寒冷
            Climate.Parameter.span(-0.25F, 0.05F),  // T2: 凉爽
            Climate.Parameter.span(0.05F, 0.3F),    // T3: 温和
            Climate.Parameter.span(0.3F, 0.6F),     // T4: 温暖
            Climate.Parameter.span(0.6F, 1.0F)      // T5: 炎热
    };
    private final Climate.Parameter[] humidities = new Climate.Parameter[]{
            Climate.Parameter.span(-1.0F, -0.4F),   // H0: 极旱
            Climate.Parameter.span(-0.4F, -0.15F),  // H1: 干旱
            Climate.Parameter.span(-0.15F, 0.1F),   // H2: 中性
            Climate.Parameter.span(0.1F, 0.35F),    // H3: 湿润
            Climate.Parameter.span(0.35F, 1.0F)     // H4: 极湿
    };
    private final Climate.Parameter[] erosions = new Climate.Parameter[]{
            Climate.Parameter.span(-1.0F, -0.78F),
            Climate.Parameter.span(-0.78F, -0.375F),
            Climate.Parameter.span(-0.375F, -0.2225F),
            Climate.Parameter.span(-0.2225F, 0.05F),
            Climate.Parameter.span(0.05F, 0.45F),
            Climate.Parameter.span(0.45F, 0.55F),
            Climate.Parameter.span(0.55F, 1.0F)
    };
    @SuppressWarnings("unchecked")
    private final ResourceKey<Biome>[][] MIDDLE_BIOMES = new ResourceKey[][]
            {
                    {null, null, null, null, null},
                    {null, null, null, null, null},
                    {null, null, null, null, null},
                    {null, null, null, null, null},
                    {ModBiomes.CURSED_DESERT, ModBiomes.CURSED_DESERT, null, null, null},
                    {ModBiomes.CURSED_DESERT, ModBiomes.CURSED_DESERT, ModBiomes.CURSED_DESERT, null, null}
            };
    @SuppressWarnings("unchecked")
    private final ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT = new ResourceKey[][]
            {
                    {null, null, null, null, null},
                    {null, null, null, null, null},
                    {null, ModBiomes.BLOOD_CORRODED_CHERRY_GROVE, null, null, null},
                    {null, ModBiomes.BLOOD_CORRODED_CHERRY_GROVE, ModBiomes.BLOOD_CORRODED_CHERRY_GROVE, ModBiomes.WITHERED_CHERRY_GROVE, null},
                    {null, null, ModBiomes.BLOOD_CORRODED_CHERRY_GROVE, ModBiomes.WITHERED_CHERRY_GROVE, null},
                    {null, null, null, null, null},
            };

    protected void addBiomes(@NotNull Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> key) {
//        super.addInlandBiomes(key);
//        super.addOffCoastBiomes(key);
//        super.addUndergroundBiomes(key);
        this.addCustomInlandBiomes(key);
    }

    private void addCustomInlandBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        this.addCustomMidSlice(consumer, Climate.Parameter.span(-1.0F, -0.93333334F));
        this.addCustomHighSlice(consumer, Climate.Parameter.span(-0.93333334F, -0.7666667F));
        this.addCustomPeaks(consumer, Climate.Parameter.span(-0.7666667F, -0.56666666F));
        this.addCustomHighSlice(consumer, Climate.Parameter.span(-0.56666666F, -0.4F));
        this.addCustomMidSlice(consumer, Climate.Parameter.span(-0.4F, -0.26666668F));
        this.addCustomLowSlice(consumer, Climate.Parameter.span(-0.26666668F, -0.05F));
        this.addCustomValleys(consumer, Climate.Parameter.span(-0.05F, 0.05F));
        this.addCustomLowSlice(consumer, Climate.Parameter.span(0.05F, 0.26666668F));
        this.addCustomMidSlice(consumer, Climate.Parameter.span(0.26666668F, 0.4F));
        this.addCustomHighSlice(consumer, Climate.Parameter.span(0.4F, 0.56666666F));
        this.addCustomPeaks(consumer, Climate.Parameter.span(0.56666666F, 0.7666667F));
        this.addCustomHighSlice(consumer, Climate.Parameter.span(0.7666667F, 0.93333334F));
        this.addCustomMidSlice(consumer, Climate.Parameter.span(0.93333334F, 1.0F));
    }

    private void addCustomPeaks(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter param) {
        for (int i = 0; i < this.temperatures.length; i++) {
            Climate.Parameter temperature = this.temperatures[i];

            for (int j = 0; j < this.humidities.length; j++) {
                Climate.Parameter humidity = this.humidities[j];
                ResourceKey<Biome> middleBiome = this.pickCustomMiddleBiome(i, j, param);
                ResourceKey<Biome> plateauBiome = this.pickPlateauBiome(i, j, param);

                if (middleBiome != null) {
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
                            Climate.Parameter.span(this.erosions[2], this.erosions[3]),
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
                            this.erosions[4],
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
                            this.erosions[6],
                            param,
                            0.0F,
                            middleBiome
                    );
                }

                if (plateauBiome != null) {
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                            this.erosions[2],
                            param,
                            0.0F,
                            plateauBiome
                    );
                }
            }
        }
    }

    private void addCustomHighSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter param) {
        for (int i = 0; i < this.temperatures.length; i++) {
            Climate.Parameter temperature = this.temperatures[i];

            for (int j = 0; j < this.humidities.length; j++) {
                Climate.Parameter humidity = this.humidities[j];
                ResourceKey<Biome> middleBiome = this.pickCustomMiddleBiome(i, j, param);
                ResourceKey<Biome> plateauBiome = this.pickPlateauBiome(i, j, param);

                if (middleBiome != null) {
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            this.coastContinentalness,
                            Climate.Parameter.span(this.erosions[0], this.erosions[1]),
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
                            Climate.Parameter.span(this.erosions[2], this.erosions[3]),
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
                            this.erosions[4],
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
                            this.erosions[6],
                            param,
                            0.0F,
                            middleBiome
                    );
                }

                if (plateauBiome != null) {
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                            this.erosions[2],
                            param,
                            0.0F,
                            plateauBiome
                    );
                }
            }
        }
    }

    private void addCustomMidSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter param) {
        for (int i = 0; i < this.temperatures.length; i++) {
            Climate.Parameter temperature = this.temperatures[i];

            for (int j = 0; j < this.humidities.length; j++) {
                Climate.Parameter humidity = this.humidities[j];
                ResourceKey<Biome> middleBiome = this.pickCustomMiddleBiome(i, j, param);
                ResourceKey<Biome> plateauBiome = this.pickPlateauBiome(i, j, param);

                if (middleBiome != null) {
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            this.nearInlandContinentalness,
                            this.erosions[2],
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
                            this.erosions[3],
                            param,
                            0.0F,
                            middleBiome
                    );

                    if (param.max() < 0L) {
                        this.addSurfaceBiome(
                                consumer,
                                temperature,
                                humidity,
                                Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                                this.erosions[4],
                                param,
                                0.0F,
                                middleBiome
                        );
                    } else {
                        this.addSurfaceBiome(
                                consumer,
                                temperature,
                                humidity,
                                Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
                                this.erosions[4],
                                param,
                                0.0F,
                                middleBiome
                        );
                        this.addSurfaceBiome(
                                consumer,
                                temperature,
                                humidity,
                                this.coastContinentalness,
                                this.erosions[6],
                                param,
                                0.0F,
                                middleBiome
                        );
                    }
                }

                if (plateauBiome != null && i != 0) {
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            this.farInlandContinentalness,
                            this.erosions[1],
                            param,
                            0.0F,
                            plateauBiome
                    );
                }
            }
        }
    }

    private void addCustomLowSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter param) {
        for (int i = 0; i < this.temperatures.length; i++) {
            Climate.Parameter temperature = this.temperatures[i];

            for (int j = 0; j < this.humidities.length; j++) {
                Climate.Parameter humidity = this.humidities[j];
                ResourceKey<Biome> middleBiome = this.pickCustomMiddleBiome(i, j, param);

                if (middleBiome != null) {
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            this.nearInlandContinentalness,
                            Climate.Parameter.span(this.erosions[2], this.erosions[3]),
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                            this.erosions[4],
                            param,
                            0.0F,
                            middleBiome
                    );
                    this.addSurfaceBiome(
                            consumer,
                            temperature,
                            humidity,
                            Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
                            this.erosions[5],
                            param,
                            0.0F,
                            middleBiome
                    );

                    if (i == 0) {
                        this.addSurfaceBiome(
                                consumer,
                                temperature,
                                humidity,
                                Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
                                this.erosions[6],
                                param,
                                0.0F,
                                middleBiome
                        );
                    }
                }
            }
        }
    }

    private void addCustomValleys(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter param) {
    }

    @Nullable
    private ResourceKey<Biome> pickCustomMiddleBiome(int temperature, int humidity, Climate.Parameter param) {
        if (param.max() >= 0L || this.MIDDLE_BIOMES[temperature][humidity] == null) {
            return this.MIDDLE_BIOMES[temperature][humidity];
        } else {
            return null;
        }
    }

    @Nullable
    private ResourceKey<Biome> pickPlateauBiome(int temperature, int humidity, Climate.Parameter param) {
        if (param.max() >= 0L) {
            return this.PLATEAU_BIOMES_VARIANT[temperature][humidity];
        } else {
            return null;
        }
    }
}
