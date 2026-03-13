package io.github.fengguoshuzhu.visionrealm.api.event.erosion.entity;

import io.github.fengguoshuzhu.visionrealm.api.event.erosion.ErosionEvent;
import io.github.fengguoshuzhu.visionrealm.api.erosion.infection.CanBeErosion;
import io.github.fengguoshuzhu.visionrealm.api.controller.entity.EntityErosionController;
import io.github.fengguoshuzhu.visionrealm.common.erosion.ErosionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Event fired when an entity undergoes erosion transformation.
 * <p>
 * This event family tracks the complete lifecycle of entity erosion, from pre-erosion
 * checks to post-erosion results. It extends {@link ErosionEvent} with entity-specific
 * context.
 * </p>
 *
 * <p><b>Important:</b> This event is only fired when an entity is being transformed
 * due to erosion (e.g., when {@link EntityErosionController#completeErosion(ErosionType)} returns
 * {@code true}), not during the gradual accumulation of erosion value.</p>
 *
 * @param <T> The type of erosion result (can be any type representing the erosion outcome)
 * @author JiXingDeFeng
 * @see ErosionEvent
 * @see ErosionType
 * @see EntityErosionController#completeErosion(ErosionType)
 * @since 1.0.0
 */
public abstract class EntityErosionEvent<T> extends ErosionEvent<Entity, T> {

    /**
     * Creates a new entity erosion event.
     *
     * @param source   The entity being eroded
     * @param target   The erosion result (what it transforms into)
     * @param level    The world where erosion occurs
     * @param position The exact position of erosion (as Vec3 for precision)
     * @param type     The type of erosion causing the transformation
     * @since 1.0.0
     */
    protected EntityErosionEvent(Entity source, T target, Level level, Vec3 position, ErosionType type) {
        super(source, target, level, position, type);
    }

    /**
     * Pre-erosion event fired before an entity is transformed.
     * <p>
     * This event is fired when an entity has met the conditions for erosion completion
     * (e.g., erosion value reached 1.0) and is about to transform. It allows other mods to:
     * <ul>
     *   <li>Cancel the transformation entirely via {@link #setCanceled(boolean)}</li>
     *   <li>Modify what the entity transforms into via {@link #setResult(Object)}</li>
     *   <li>Add custom logic or side effects before transformation</li>
     * </ul>
     * </p>
     *
     * @param <T> The type of transformation result
     * @since 1.0.0
     */
    public static class Pre<T> extends EntityErosionEvent<T> implements ICancellableEvent {

        /**
         * Creates a new pre-transformation event.
         *
         * @param source  The entity about to transform
         * @param target The initial transformation result from {@link CanBeErosion#afterErosion}
         *                (what the entity will transform into)
         * @param level   The world
         * @param pos     The position
         * @param type    The erosion type causing the transformation
         * @since 1.0.0
         */
        public Pre(Entity source, T target, Level level, Vec3 pos, ErosionType type) {
            super(source, target, level, pos, type);
        }

        @Override
        public void setCanceled(boolean isCanceled) {
            ICancellableEvent.super.setCanceled(isCanceled);
        }

        /**
         * Changes what the entity transforms into.
         *
         * @param result The new transformation result (e.g., a different entity type)
         * @since 1.0.0
         */
        public void setResult(T result) {
            this.target = result;
        }
    }

    /**
     * Post-erosion event fired after an entity has been transformed.
     * <p>
     * This event is fired immediately after an entity completes its erosion transformation.
     * It provides information about the final outcome and can be used for:
     * <ul>
     *   <li>Playing sound effects or particles at the transformation site</li>
     *   <li>Tracking statistics or advancements</li>
     *   <li>Triggering follow-up events (e.g., spawning additional entities)</li>
     *   <li>Notifying other systems of the transformation</li>
     * </ul>
     * </p>
     *
     * @param <T> The type of transformation result
     * @since 1.0.0
     */
    public static class Post<T, R> extends EntityErosionEvent<T> {
        private final boolean success;
        private final R result;

        /**
         * Creates a new post-transformation event.
         *
         * @param source  The original entity before transformation
         * @param target What the entity transformed into (the result from {@link CanBeErosion#transformed})
         * @param level   The world
         * @param pos     The position where transformation occurred
         * @param type    The erosion type that caused the transformation
         * @param success  {@code true} if transformation was successful, {@code false} otherwise
         * @since 1.0.0
         */
        public Post(Entity source, R result, T target, Level level, Vec3 pos, ErosionType type, boolean success) {
            super(source, target, level, pos, type);
            this.success = success;
            this.result = result;
        }

        /**
         * Returns whether the transformation was successfully applied.
         *
         * @return {@code true} if the entity was transformed, {@code false} if transformation failed
         *
         * @since 1.0.0
         */
        public boolean isSuccess() {
            return this.success;
        }

        /**
         * Returns the result of the erosion transformation.
         *
         * @return The transformed object after erosion, or {@code null} if transformation failed
         *
         * @since 1.0.0
         */
        public R getResult() {
            return this.result;
        }
    }
}
