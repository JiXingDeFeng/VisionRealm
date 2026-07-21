package io.github.jixingdefeng.visionrealm.common.selector;

import java.util.function.BooleanSupplier;

/**
 * Defines how a target state condition is evaluated: unrestricted, match only, or match not.
 *
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public enum StateSelection {
    /** No restriction */
    ANY,
    /** Only match the specific state */
    ONLY,
    /** Only not match the specific state */
    NOT;

    public boolean test(BooleanSupplier value) {
        return switch (this) {
            case ANY -> true;
            case ONLY -> value.getAsBoolean();
            case NOT -> !value.getAsBoolean();
        };
    }
}
