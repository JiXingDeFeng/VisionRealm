package io.github.jixingdefeng.visionrealm.core.tags;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> Eroded = create("eroded");

    private ModItemTags() {}

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name));
    }
}
