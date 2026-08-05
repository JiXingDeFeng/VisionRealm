package io.github.jixingdefeng.visionrealm.content.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.function.Function;

public class ModMultiNoiseBiomeSourceParameterLists {
    public static final MultiNoiseBiomeSourceParameterList.Preset VISIONREALM_PRESET = createVisionrealmPreset();

    @SuppressWarnings("unchecked")
    public static <T> MultiNoiseBiomeSourceParameterList.Preset createPreset(
            ResourceLocation id,
            Function<Function<ResourceKey<Biome>, T>, Climate.ParameterList<T>> function
    ) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList$Preset$SourceProvider");
            Object instance = Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class<?>[]{clazz},
                    (proxy, method, args) -> {
                        if (method.getName().equals("apply") && args.length == 1) {
                            return function.apply((Function<ResourceKey<Biome>, T>) args[0]);
                        } else {
                            throw new UnsupportedOperationException("Unexpected method: " + method);
                        }
                    }
            );

            Constructor<MultiNoiseBiomeSourceParameterList.Preset> constructor =
                    MultiNoiseBiomeSourceParameterList.Preset.class.getDeclaredConstructor(ResourceLocation.class, clazz);
            return constructor.newInstance(id, instance);
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e
        ) {
            throw new RuntimeException("Failed to load MultiNoiseBiomeSourceParameterList$Preset preset", e);
        }
    }

    private static <T> MultiNoiseBiomeSourceParameterList.Preset createVisionrealmPreset() {
        return ModMultiNoiseBiomeSourceParameterLists.<T>createPreset(
                ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "visionrealm"),
                valueGetter -> {
                    ImmutableList.Builder<Pair<Climate.ParameterPoint, T>> builder = ImmutableList.builder();
                    new VisionrealmBiomeBuilder().addBiomes(
                            pair -> builder.add(pair.mapSecond(valueGetter))
                    );
                    return new Climate.ParameterList<>(builder.build());
                });
    }

    private ModMultiNoiseBiomeSourceParameterLists() {
    }
}
