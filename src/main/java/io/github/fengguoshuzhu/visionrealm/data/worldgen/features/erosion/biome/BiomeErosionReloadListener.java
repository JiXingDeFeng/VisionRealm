package io.github.fengguoshuzhu.visionrealm.data.worldgen.features.erosion.biome;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.manager.world.erosion.biome.BiomeErosionManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BiomeErosionReloadListener extends SimplePreparableReloadListener<Map<String, String>> {
    private static final Gson GSON = new Gson();
    private final BiomeErosionManager manager;

    public BiomeErosionReloadListener(BiomeErosionManager manager) {
        this.manager = manager;
    }

    @NotNull
    @Override
    protected Map<String, String> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<String, String> biomeErosionMap = new HashMap<>();
        String path = "erosion/biome_erosion_types";

        resourceManager.listResources(path, fileName -> fileName.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    try (var reader = resource.openAsReader()) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        JsonObject values = json.getAsJsonObject("values");

                        values.entrySet().forEach(entry -> {
                            String biomeId = entry.getKey();
                            String type = entry.getValue().getAsString();
                            biomeErosionMap.put(biomeId, type);
                        });
                    } catch (IOException e) {
                        VisionRealm.LOGGER.error(e.getMessage(), e);
                    }
                });
        VisionRealm.LOGGER.info("\n\n群系完成\n");
        return biomeErosionMap;
    }

    @Override
    protected void apply(@NotNull Map<String, String> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.manager.heavyLoad(map);
    }
}
