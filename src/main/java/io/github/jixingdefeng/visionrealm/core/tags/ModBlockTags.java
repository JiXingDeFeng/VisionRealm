package io.github.jixingdefeng.visionrealm.core.tags;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> ERODED = create("eroded");
    public static final TagKey<Block> INFINIBURN_VISIONREALM = create("infiniburn_visionrealm");

    private ModBlockTags() {}

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
