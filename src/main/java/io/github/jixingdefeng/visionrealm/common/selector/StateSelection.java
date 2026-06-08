package io.github.jixingdefeng.visionrealm.common.selector;

import java.util.function.BooleanSupplier;

public enum StateSelection {
    /**
     * No restriction
     */
    ANY,
    /**
     * Only match the specific state
     */
    ONLY,
    /**
     * Only not match the specific state
     */
    NOT;

    public boolean test(BooleanSupplier value) {
        return switch (this) {
            case ANY -> true;
            case ONLY -> value.getAsBoolean();
            case NOT -> !value.getAsBoolean();
        };
    }
}
