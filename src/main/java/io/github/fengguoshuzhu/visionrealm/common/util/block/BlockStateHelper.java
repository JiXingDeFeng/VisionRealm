package io.github.fengguoshuzhu.visionrealm.common.util.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;

public class BlockStateHelper {

    public static BlockState copyProperties(BlockState source, BlockState target) {
        return copyProperties(source, target, source.getProperties());
    }

    public static BlockState copyProperties(BlockState source, BlockState target, Collection<Property<?>> properties) {
        BlockState result = target;
        for (Property<?> property : properties) {
            result = copyProperties(source, result, property);
        }

        return result;
    }

    public static <T extends Comparable<T>> BlockState copyProperties(BlockState source, BlockState target, Property<T> property) {
        if (target.hasProperty(property)) {
            return target.setValue(property, source.getValue(property));
        }

        return target;
    }
}
