package io.github.jixingdefeng.visionrealm.core.erosion;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jixingdefeng.visionrealm.core.util.serialization.Codecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public record ErosionType(String name, boolean block, boolean entity) {
    public static final ErosionType NONE = new ErosionType("none", false, false);
    public static final Codec<ErosionType> CODEC = Codec.xor(
            RecordCodecBuilder.<ErosionType>create(instance ->
                    instance.group(
                            Codec.STRING.fieldOf("name").forGetter(ErosionType::name),
                            Codec.BOOL.optionalFieldOf("block", true).forGetter(ErosionType::block),
                            Codec.BOOL.optionalFieldOf("entity", true).forGetter(ErosionType::entity)
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

    public boolean none() {
        return this.equals(NONE);
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
    @NotNull
    public String toString() {
        if (this == NONE) return "ErosionType.NONE";
        return "ErosionType[name=" + this.name + ", block=" + this.block + ", entity=" + this.entity + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof ErosionType(String name1, boolean block1, boolean entity1)
                && this.name.equals(name1)
                && this.block == block1
                && this.entity == entity1;
    }
}
