package io.github.jixingdefeng.visionrealm.common.erosion.handle;

import io.github.jixingdefeng.visionrealm.api.erosion.block.CanBeErosionBlock;
import io.github.jixingdefeng.visionrealm.common.erosion.context.BlockErosionContext;
import io.github.jixingdefeng.visionrealm.core.erosion.ErosionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Core processor for handling erosion operations on blocks.
 * <p>
 * This class provides a generic erosion processing pipeline that handles:
 * <ul>
 *   <li>Retrieving the erodible component via {@link ErosionWrapper}</li>
 *   <li>Dispatching pre-erosion events via {@link EventSender}</li>
 *   <li>Executing the erosion transformation with probability checks</li>
 *   <li>Dispatching post-erosion events</li>
 * </ul>
 * </p>
 *
 * <p><b>Type Parameters:</b></p>
 * <ul>
 *   <li><b>R</b> - The final result type after erosion (e.g., BlockState, Entity)</li>
 *   <li><b>RT</b> - The erosion target type from {@link CanBeErosionBlock#afterErosion}</li>
 * </ul>
 *
 * @author JiXingDeFeng
 * @see CanBeErosionBlock
 * @see ErosionType
 * @since 0.0.1-dev
 */
public class BaseBlockErosionHandle {

    /**
     * Processes a complete erosion cycle for the given block.
     * <p>
     * The processing flow:
     * <ol>
     *   <li>Retrieves the {@link CanBeErosionBlock} component via the wrapper</li>
     *   <li>Verifies that the source can be eroded for the given type</li>
     *   <li>Obtains the erosion target from {@link CanBeErosionBlock#afterErosion}</li>
     *   <li>Sends a pre-erosion event to check for cancellation</li>
     *   <li>If allowed, performs probability check via {@link CanBeErosionBlock#conversionProbability}</li>
     *   <li>If probability succeeds, executes the erosion transformation</li>
     *   <li>If successful, applies the {@link CanBeErosionBlock#onEroded} callback</li>
     *   <li>Sends a post-erosion event with the result</li>
     * </ol>
     * </p>
     *
     * @param <R>         Final result type after erosion (e.g., Entity, BlockState)
     * @param <RT>        Erosion target type (e.g., EntityType, Block)
     * @param <C>         Concrete erosion component type
     * @param source      The block to erode
     * @param level       The world where erosion occurs
     * @param pos         The position of erosion
     * @param type        The type of erosion to apply
     * @param wrapper     Provider for the {@link CanBeErosionBlock} component
     * @param eventSender Handler for pre-/post-erosion events
     * @return {@code true} if erosion was successfully applied, {@code false} otherwise
     */
    public static <R, RT, C extends CanBeErosionBlock<RT, R>> boolean process(
            Block source,
            Level level,
            BlockPos pos,
            ErosionType type,
            ErosionWrapper<RT, R, C> wrapper,
            EventSender<RT, R> eventSender
    ) {
        if (!type.isValidFor(source)) {
            String sourceType = source != null ? source.getClass().getSimpleName() : "null";
            throw new IllegalArgumentException(
                    "Erosion type " + type + " is not compatible with source type: " + sourceType
            );
        } else {
            CanBeErosionBlock<RT, R> erodible = wrapper.getErosion(source);
            if (erodible != null && erodible.canBeEroded(level, pos, type)) {
                RT target = erodible.afterErosion(type);
                if (target != null) {
                    PreEventResult<RT> preResult = eventSender.sendPreEvent(source, target, level, pos, type);
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

            return false;
        }
    }

    /**
     * Provides a way to extract the {@link CanBeErosionBlock} component from a block.
     * <p>
     * This wrapper bridges blocks to their erosion-capable implementations.
     * </p>
     *
     * @param <R>  The final result type after erosion (e.g., Entity, BlockState)
     * @param <RT> The erosion target type from {@link CanBeErosionBlock#afterErosion}
     * @param <C>  The concrete erosion component type extending {@link CanBeErosionBlock}
     * @author JiXingDeFeng
     * @since 0.0.2-dev
     */
    public interface ErosionWrapper<RT, R, C extends CanBeErosionBlock<RT, R>> {

        /**
         * Retrieves the {@link CanBeErosionBlock} component from the source object.
         *
         * @param source The source object (entity, block, etc.)
         * @return The erosion-capable component, or {@code null} if not applicable
         */
        C getErosion(Block source);

        /**
         * Creates an erosion context containing additional information needed for transformation.
         * <p>
         * The context includes block-specific data such as original block state and
         * environmental conditions that affect the erosion outcome.
         * </p>
         *
         * @param source The block being eroded
         * @param level  The world where erosion occurs
         * @param pos    The position of erosion
         * @return A context object with relevant erosion information
         */
        default BlockErosionContext getContext(Block source, Level level, BlockPos pos) {
            return new BlockErosionContext(source, level, pos);
        }
    }

    /**
     * Handles event dispatching for the erosion process.
     *
     * @param <R> The final result type after erosion
     * @param <RT> The erosion target type from {@link CanBeErosionBlock#afterErosion}
     * @since 0.0.1-dev
     */
    public interface EventSender<RT, R> {

        /**
         * Sends a pre-erosion event and returns the result.
         *
         * @param source The block being eroded
         * @param target The result from {@link CanBeErosionBlock#afterErosion}
         * @param level  The world
         * @param pos    The position
         * @param type   The erosion type
         * @return The pre-event result containing whether to proceed and any modified data
         */
        PreEventResult<RT> sendPreEvent(Block source, RT target, Level level, BlockPos pos, ErosionType type);

        /**
         * Sends a post-erosion event after processing.
         *
         * @param source  The block that was eroded
         * @param result  The final result from {@link CanBeErosionBlock#transformed}
         * @param target  The erosion transformed target
         * @param level   The world
         * @param pos     The position
         * @param type    The erosion type
         * @param success Whether erosion was successful
         */
        void sendPostEvent(Block source, R result, RT target, Level level, BlockPos pos, ErosionType type, boolean success);
    }

    /**
     * Container for pre-erosion event results.
     *
     * @param <T> The erosion target type
     * @since 0.0.1-dev
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
