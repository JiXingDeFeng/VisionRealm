package io.github.jixingdefeng.visionrealm.common.handle.erosion.infection;

import io.github.jixingdefeng.visionrealm.api.erosion.ImmuneErosion;
import io.github.jixingdefeng.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.jixingdefeng.visionrealm.common.context.erosion.ErosionContext;
import io.github.jixingdefeng.visionrealm.common.erosion.ErosionType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Core processor for handling erosion operations on various source types.
 * <p>
 * This class provides a generic erosion processing pipeline that handles:
 * <ul>
 *   <li>Retrieving the erodible component via {@link ErosionWrapper}</li>
 <li>Checking immunity via {@link ImmuneErosion}</li>
 *   <li>Dispatching pre-erosion events via {@link EventSender}</li>
 *   <li>Executing the erosion transformation with probability checks</li>
 *   <li>Dispatching post-erosion events</li>
 * </ul>
 * </p>
 *
 * <p><b>Type Parameters:</b></p>
 * <ul>
 *   <li><b>T</b> - The source type (e.g., Entity, Block, etc.)</li>
 *   <li><b>R</b> - The final result type after erosion (e.g., BlockState, Entity)</li>
 *   <li><b>E</b> - The erosion target type from {@link CanBeErosion#afterErosion}</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see CanBeErosion
 * @see ErosionType
 * @since 0.0.1-dev-1
 */
public class BaseErosionHandle {

    /**
     * Processes a complete erosion cycle for the given source.
     * <p>
     * The processing flow:
     * <ol>
     *   <li>Checks if the source is immune via {@link ImmuneErosion}</li>
     *   <li>Retrieves the {@link CanBeErosion} component via the wrapper</li>
     *   <li>Verifies that the source can be eroded for the given type</li>
     *   <li>Obtains the erosion target from {@link CanBeErosion#afterErosion}</li>
     *   <li>Sends a pre-erosion event to check for cancellation</li>
     *   <li>If allowed, performs probability check via {@link CanBeErosion#conversionProbability}</li>
     *   <li>If probability succeeds, executes the erosion transformation</li>
     *   <li>If successful, applies the {@link CanBeErosion#onEroded} callback</li>
     *   <li>Sends a post-erosion event with the result</li>
     * </ol>
     * </p>
     *
     * @param source      The source object to erode
     * @param level       The world where erosion occurs
     * @param pos         The position of erosion
     * @param type        The type of erosion to apply
     * @param wrapper     Provider for the {@link CanBeErosion} component
     * @param eventSender Handler for pre/post erosion events
     * @param <T>         Source type
     * @param <R>         Final result type after erosion
     * @param <E>         Erosion target type from {@link CanBeErosion#afterErosion}
     * @return {@code true} if erosion was successfully applied, {@code false} otherwise
     */
    public static <T, R, E> boolean process(
            T source,
            Level level,
            Vec3 pos,
            ErosionType type,
            ErosionWrapper<T, R, E> wrapper,
            EventSender<T, R, E> eventSender
    ) {
        if (!type.isValidFor(source)) {
            String sourceType = source != null ? source.getClass().getSimpleName() : "null";
            throw new IllegalArgumentException("Erosion type " + type + " is not compatible with source type: " + sourceType);
        } else {
            if (!(source instanceof ImmuneErosion immuneErosion && immuneErosion.immune(type))) {
                CanBeErosion<T, R, E> erodible = wrapper.getErosion(source);
                if (erodible != null && erodible.canBeEroded(type)) {
                    E target = erodible.afterErosion(type);
                    if (target != null) {
                        PreEventResult<E> preResult = eventSender.sendPreEvent(source, target, level, pos, type);
                        if (preResult.shouldProceed()) {
                            boolean success = false;
                            if (level.random.nextFloat() <= erodible.conversionProbability(type)) {
                                R result = erodible.transformed(preResult.target(), level, pos, type, wrapper.getContext(source, level, pos));
                                success = result != null;
                                if (success) {
                                    erodible.onEroded(result, level, pos, type);
                                }

                                eventSender.sendPostEvent(source, result, target, level, pos, type, success);
                            }

                            return success;
                        }
                    }
                }
            }

            return false;
        }
    }

    /**
     * Provides a way to extract the {@link CanBeErosion} component from a source object.
     *
     * @param <T> The source type
     * @param <R> The final result type after erosion
     * @param <E> The erosion target type from {@link CanBeErosion#afterErosion}
     *
     * @since 0.0.1-dev-1
    
     */
    public interface ErosionWrapper<T, R, E> {

        /**
         * Retrieves the {@link CanBeErosion} component from the source object.
         *
         * @param source The source object (entity, block, etc.)
         * @return The erosion-capable component, or {@code null} if not applicable
         */
        CanBeErosion<T, R, E> getErosion(T source);

        /**
         * Creates an erosion context containing additional information needed for transformation.
         * <p>
         * The context may include source-specific data such as original block state,
         * entity attributes, or environmental conditions that affect the erosion outcome.
         * </p>
         *
         * @param source The source object being eroded
         * @param level  The world where erosion occurs
         * @param pos    The position of erosion
         * @return A context object with relevant erosion information
         */
        default ErosionContext<T> getContext(T source, Level level, Vec3 pos) {
            return new ErosionContext<>(source, level, pos);
        }
    }

    /**
     * Handles event dispatching for the erosion process.
     *
     * @param <T> The source type
     * @param <R> The final result type after erosion
     * @param <E> The erosion target type from {@link CanBeErosion#afterErosion}
     *
     * @since 0.0.1-dev-1
    
     */
    public interface EventSender<T, R, E> {

        /**
         * Sends a pre-erosion event and returns the result.
         *
         * @param source      The source being erosion
         * @param target      The result from {@link CanBeErosion#afterErosion}
         * @param level       The world
         * @param pos         The position
         * @param type        The erosion type
         * @return The pre-event result containing whether to proceed and any modified data
         */
        PreEventResult<E> sendPreEvent(T source, E target, Level level, Vec3 pos, ErosionType type);

        /**
         * Sends a post-erosion event after processing.
         *
         * @param source  The source that was erosion
         * @param result  The final result from {@link CanBeErosion#transformed}
         * @param target  The erosion transformed target
         * @param level   The world
         * @param pos     The position
         * @param type    The erosion type
         * @param success Whether erosion was successful
         */
        void sendPostEvent(T source, R result, E target, Level level, Vec3 pos, ErosionType type, boolean success);
    }

    /**
     * Container for pre-erosion event results.
     *
     * @param <T> The erosion target type
     */
    public static class PreEventResult<T> {
        private final T target;
        private final boolean proceed;

        public PreEventResult(T target, boolean proceed) {
            this.target = target;
            this.proceed = proceed;
        }

        public static <T> PreEventResult<T> of(T result, boolean proceed) {
            return new PreEventResult<>(result, proceed);
        }

        public T target() {
            return target;
        }

        public boolean shouldProceed() {
            return proceed;
        }
    }
}
