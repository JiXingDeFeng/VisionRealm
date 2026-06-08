package io.github.jixingdefeng.visionrealm.core.erosion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class ErosionType {
    public static final Codec<ErosionType> CODEC = ResourceLocation.CODEC
            .flatXmap(
                    resourceLocation -> {
                        ErosionType type = ErosionType.get(resourceLocation);
                        return type != null
                                ? DataResult.success(type)
                                : DataResult.error(() -> "Unknown erosion type: " + resourceLocation);
                    },
                    erosionType -> DataResult.success(erosionType.getName())
            );
    private static final Map<ResourceLocation, ErosionType> TYPES = new HashMap<>();
    private final ResourceLocation name;
    private final boolean block;
    private final boolean entity;

    public static final ErosionType NONE = create("none", true, true);
    public static final ErosionType BLOOD = create("blood", true, false);
    public static final ErosionType CURSE = create("curse", true, true);
    public static final ErosionType DREAD = create("dread", false, true);

    protected ErosionType(ResourceLocation name, boolean block, boolean entity) {
        this.name = name;
        this.block = block;
        this.entity = entity;
    }

    public static ErosionType create(ResourceLocation name, boolean general) {
        return create(name, general, general);
    }

    public static ErosionType create(ResourceLocation name, boolean block, boolean entity) {
        return register(new ErosionType(name, block, entity));
    }

    public static ErosionType register(ErosionType type) {
        TYPES.put(type.name, type);
        return type;
    }

    private static ErosionType create(String name, boolean block, boolean entity) {
        return create(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, name), block, entity);
    }

    public static ErosionType get(ResourceLocation name) {
        return TYPES.get(name);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public boolean block() {
        return this.block;
    }

    public boolean entity() {
        return this.entity;
    }

    public boolean general() {
        return this.block && this.entity;
    }

    public <T> boolean isValidFor(T source) {
        if (this.general()) {
            return true;
        } else if (source == null) {
            return false;
        } else if (source instanceof Block || source instanceof BlockState) {
            return this.block();
        } else if (source instanceof Entity) {
            return this.entity();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "ErosionType[" + this.name + ", " + this.block + ", " + this.entity + "]";
    }
}
