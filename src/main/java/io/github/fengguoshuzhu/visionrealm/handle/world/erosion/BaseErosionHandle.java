package io.github.fengguoshuzhu.visionrealm.handle.world.erosion;

import io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection.CanBeErosion;
import io.github.fengguoshuzhu.visionrealm.api.world.erosion.infection.ImmuneErosion;
import io.github.fengguoshuzhu.visionrealm.common.world.context.ErosionContext;
import io.github.fengguoshuzhu.visionrealm.core.world.erosion.ErosionType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Core processor for handling erosion operations on various source types.
 * <p>
 * This class provides a generic erosion processing pipeline that handles:
 * <ul>
 *   <li>Retrieving the erodible component via {@link ErosionWrapper}</li>
 *   <li>Dispatching pre-erosion events via {@link EventSender}</li>
 *   <li>Executing the erosion transformation</li>
 *   <li>Dispatching post-erosion events</li>
 * </ul>
 * </p>
 *
 * <p><b>Type Parameters:</b></p>
 * <ul>
 *   <li><b>T</b> - The source type (e.g., CanBeErodedEntity, CanBeErosionBlock, etc.)</li>
 *   <li><b>E</b> - The erosion result type from {@link CanBeErosion}</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see CanBeErosion
 * @see ErosionType
 * @since 1.0.0
 */
public class BaseErosionHandle {

    /**
     * Processes a complete erosion cycle for the given source.
     * <p>
     * The processing flow:
     * <ol>
     *   <li>Retrieves the {@link CanBeErosion} component via the wrapper</li>
     *   <li>Sends a pre-erosion event to check for cancellation</li>
     *   <li>If allowed, performs the erosion transformation</li>
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
     * @param <T>         Target type
     * @param <E>         Erosion result type
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

    /**
     * Provides a way to extract the {@link CanBeErosion} component from a source object.
     *
     * @param <T> The source type
     * @param <E> The erosion result type
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
     * @param <E> The erosion result type
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
     * @param <T> The erosion result type
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
