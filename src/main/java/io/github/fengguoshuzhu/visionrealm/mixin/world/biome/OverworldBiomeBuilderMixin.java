package io.github.fengguoshuzhu.visionrealm.mixin.world.biome;

import io.github.fengguoshuzhu.visionrealm.core.biome.AnomalyBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

    @Mutable
    @Shadow
    @Final
    private Climate.Parameter[] temperatures;

    @Mutable
    @Shadow
    @Final
    private ResourceKey<Biome>[][] OCEANS;

    @Mutable
    @Shadow
    @Final
    private ResourceKey<Biome>[][] MIDDLE_BIOMES;

    @Mutable
    @Shadow
    @Final
    private ResourceKey<Biome>[][] MIDDLE_BIOMES_VARIANT;

    @Mutable
    @Shadow
    @Final
    private ResourceKey<Biome>[][] PLATEAU_BIOMES;

    @Mutable
    @Shadow
    @Final
    private ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT;

    @Mutable
    @Shadow
    @Final
    private ResourceKey<Biome>[][] SHATTERED_BIOMES;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.temperatures = new Climate.Parameter[]{
                Climate.Parameter.span(-1.0F, -0.45F),
                Climate.Parameter.span(-0.45F, -0.15F),
                Climate.Parameter.span(-0.15F, 0.2F),
                Climate.Parameter.span(0.2F, 0.55F),
                Climate.Parameter.span(0.55F, 1.0F),
                Climate.Parameter.span(-0.25F, 0.0F)
        };
        this.OCEANS = new ResourceKey[][]   // 不能有null
                {
                        {
                                Biomes.DEEP_FROZEN_OCEAN,
                                Biomes.DEEP_COLD_OCEAN,
                                Biomes.DEEP_OCEAN,
                                Biomes.DEEP_LUKEWARM_OCEAN,
                                Biomes.WARM_OCEAN,
                                Biomes.DEEP_COLD_OCEAN
                        },
                        {
                                Biomes.FROZEN_OCEAN,
                                Biomes.COLD_OCEAN,
                                Biomes.OCEAN,
                                Biomes.LUKEWARM_OCEAN,
                                Biomes.WARM_OCEAN,
                                Biomes.COLD_OCEAN
                        }
                };
        this.MIDDLE_BIOMES = new ResourceKey[][]    // 不能有null
                {
                        {Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.TAIGA},
                        {Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
                        {Biomes.FLOWER_FOREST, Biomes.PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST},
                        {Biomes.SAVANNA, Biomes.SAVANNA, Biomes.FOREST, Biomes.JUNGLE, Biomes.JUNGLE},
                        {Biomes.DESERT, AnomalyBiomes.ANOMALY_DESERT, AnomalyBiomes.ANOMALY_DESERT, AnomalyBiomes.ANOMALY_DESERT, Biomes.DESERT},
                        {Biomes.PLAINS, Biomes.PLAINS, Biomes.PLAINS, Biomes.PLAINS, Biomes.PLAINS}
                };
        this.MIDDLE_BIOMES_VARIANT = new ResourceKey[][]
                {
                        {Biomes.ICE_SPIKES, null, Biomes.SNOWY_TAIGA, null, null},
                        {null, null, null, null, Biomes.OLD_GROWTH_PINE_TAIGA},
                        {Biomes.SUNFLOWER_PLAINS, null, null, Biomes.OLD_GROWTH_BIRCH_FOREST, null},
                        {null, null, Biomes.PLAINS, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE},
                        {null, null, null, null, null},
                        {null, null, null, null, null}
                };
        this.PLATEAU_BIOMES = new ResourceKey[][]   // 不能有null
                {
                        {Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA},
                        {Biomes.MEADOW, Biomes.MEADOW, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
                        {Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.DARK_FOREST},
                        {Biomes.SAVANNA_PLATEAU, Biomes.SAVANNA_PLATEAU, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE},
                        {Biomes.BADLANDS, Biomes.BADLANDS, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WOODED_BADLANDS},
                        {Biomes.MEADOW, Biomes.MEADOW, Biomes.FOREST, Biomes.TAIGA, Biomes.TAIGA}
                };
        this.PLATEAU_BIOMES_VARIANT = new ResourceKey[][]
                {
                        {Biomes.ICE_SPIKES, null, null, null, null},
                        {AnomalyBiomes.WITHERED_CHERRY_GROVE, null, Biomes.MEADOW, Biomes.MEADOW, Biomes.OLD_GROWTH_PINE_TAIGA},
                        {AnomalyBiomes.WITHERED_CHERRY_GROVE, AnomalyBiomes.WITHERED_CHERRY_GROVE, Biomes.FOREST, Biomes.BIRCH_FOREST, null},
                        {null, null, null, null, null},
                        {Biomes.ERODED_BADLANDS, Biomes.ERODED_BADLANDS, null, null, null},
                        {AnomalyBiomes.WITHERED_CHERRY_GROVE, AnomalyBiomes.BLOOD_CORRODED_CHERRY_GROVE, AnomalyBiomes.BLOOD_CORRODED_CHERRY_GROVE, AnomalyBiomes.BLOOD_CORRODED_CHERRY_GROVE, AnomalyBiomes.WITHERED_CHERRY_GROVE}
                };
        this.SHATTERED_BIOMES = new ResourceKey[][]
                {
                        {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
                        {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
                        {Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null}
                };
    }
}
