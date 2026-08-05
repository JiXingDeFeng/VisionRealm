package io.github.jixingdefeng.visionrealm.event.game.datagen;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

public class GatherDataEvents {

    public static void addDimension(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(
                event.includeServer(),
                (DataProvider.Factory<DatapackBuiltinEntriesProvider>) output -> new DatapackBuiltinEntriesProvider(
                        output,
                        event.getLookupProvider(),
                        new RegistrySetBuilder(),
                        Set.of(VisionRealm.MOD_ID)
                )
        );
    }
}
