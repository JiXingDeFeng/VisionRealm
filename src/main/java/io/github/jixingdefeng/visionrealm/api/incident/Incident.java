package io.github.jixingdefeng.visionrealm.api.incident;

/**
 * A generic incident that can be executed on a set of targets.
 * <p>
 * Implementations define specific behaviors that are triggered when the incident
 * is selected and its probability check passes.
 * </p>
 *
 * @param <T> The target type (e.g., BlockPos, Vec3, Entity)
 * @author JiXingDeFeng
 * @since 0.0.2-dev
 */
public interface Incident<T> {

    /**
     * Executes the incident with the given context.
     *
     * @param context The incident context containing targets and execution environment
     * @return {@code true} if the incident executed successfully, {@code false} otherwise
     */
    boolean execute(IncidentContext<T> context);
}
