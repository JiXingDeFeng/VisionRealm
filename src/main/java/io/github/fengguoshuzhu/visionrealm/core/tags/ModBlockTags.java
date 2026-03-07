package io.github.fengguoshuzhu.visionrealm.core.tags;

import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> Eroded = create("eroded");

    private ModBlockTags() {}

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
