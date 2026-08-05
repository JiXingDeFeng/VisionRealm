package io.github.jixingdefeng.visionrealm.event.mod.data;

import io.github.jixingdefeng.visionrealm.core.erosion.BiomeErosionData;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class DataEvents {

    public static void registerDataMapTypes(final RegisterDataMapTypesEvent event) {
        event.register(BiomeErosionData.DATA_MAP_TYPE);
    }
}
