package io.github.fengguoshuzhu.visionrealm.manager.world.erosion.biome;

import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BiomeErosionManager {
    private static BiomeErosionManager instance;
    private final Map<ResourceKey<Biome>, ErosionType> CACHE = new HashMap<>();

    public static BiomeErosionManager getInstance() {
        return instance;
    }

    public static void serverStopping(ServerStoppingEvent event) {
        instance = null;
    }

    public void heavyLoad(Map<ResourceKey<Biome>, ErosionType> map) {
        this.CACHE.putAll(map);
        instance = this;
    }

    public ErosionType getErosionType(ResourceKey<Biome> name) {
        return Objects.requireNonNullElse(this.CACHE.get(name), ErosionType.NONE);
    }
}
