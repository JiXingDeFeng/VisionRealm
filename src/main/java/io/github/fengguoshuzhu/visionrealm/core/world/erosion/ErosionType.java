package io.github.fengguoshuzhu.visionrealm.core.world.erosion;

import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ErosionType {
    private static final Map<ResourceLocation, ErosionType> TYPES = new HashMap<>();
    private final ResourceLocation name;
    private final boolean block;
    private final boolean entity;

    public static final ErosionType NONE = create("none", true, true);
    public static final ErosionType BLOOD = create("blood", true, false);
    public static final ErosionType CURSE = create("curse", true, true);
    public static final ErosionType DREAD = create("dread", false, true);

    private ErosionType(ResourceLocation name, boolean block, boolean entity) {
        this.name = name;
        this.block = block;
        this.entity = entity;
    }

    public static ErosionType create(ResourceLocation name, boolean general) {
        return create(name, general, general);
    }

    public static ErosionType create(ResourceLocation name, boolean block, boolean entity) {
        ErosionType type = new ErosionType(name, block, entity);
        TYPES.put(name, type);
        return type;
    }

    private static ErosionType create(String name, boolean block, boolean entity) {
        return create(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name), block, entity);
    }

    public static ErosionType get(ResourceLocation name) {
        return TYPES.get(name);
    }

    public ResourceLocation getName(){
        return this.name;
    }

    public boolean isBlock() {
        return this.block;
    }

    public boolean isEntity() {
        return this.entity;
    }

    public boolean isGeneral() {
        return this.block && this.entity;
    }

    @Override
    public String toString() {
        return String.format("ErosionType[%s, %s, %s]]", this.name, this.block, this.entity);
    }
}
