package io.github.jixingdefeng.visionrealm.core.erosion;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.common.util.serialization.Codecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ErosionType {
    public static final ErosionType NONE = new ErosionType("none", false, false);
    public static final Codec<ErosionType> CODEC = Codec.xor(
            RecordCodecBuilder.<ErosionType>create(instance ->
                    instance.group(
                            Codec.STRING.fieldOf("name").forGetter(ErosionType::getName),
                            Codec.BOOL.fieldOf("block").forGetter(ErosionType::block),
                            Codec.BOOL.fieldOf("entity").forGetter(ErosionType::entity)
                    ).apply(instance, ErosionType::new)
            ),
            Codecs.EMPTY_OBJECT
    ).xmap(
            either -> either.map(
                    type -> type,
                    type -> NONE
            ),
            Either::left
    );
    private final String name;
    private final boolean block;
    private final boolean entity;

    protected ErosionType(String name, boolean block, boolean entity) {
        this.name = name;
        this.block = block;
        this.entity = entity;
    }

    public String getName() {
        return this.name;
    }

    public boolean none() {
        return this.equals(NONE);
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
        if (this == NONE) return "ErosionType.NONE";
        return "ErosionType[name=" + this.name + ", block=" + this.block + ", entity=" + this.entity + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof ErosionType type
                && this.name.equals(type.name)
                && this.block == type.block
                && this.entity == type.entity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.block, this.entity);
    }
}
