package io.github.jixingdefeng.visionrealm.common.util.serialization;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.util.Unit;

public final class Codecs {
    public static final PrimitiveCodec<Unit> EMPTY_OBJECT = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Unit> read(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input)
                    .flatMap(map -> map.entries().findAny().isEmpty()
                                    ? DataResult.success(Unit.INSTANCE)
                                    : DataResult.error(() -> "Expected empty object, but got: " + input)
                    );
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Unit value) {
            return ops.emptyMap();
        }

        @Override
        public String toString() {
            return "Void";
        }
    };
}
