package io.github.jixingdefeng.visionrealm.api.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for intercepting and modifying entity rendering behavior.
 * <p>
 * This interface provides a mechanism to customize how entities are rendered by intercepting
 * or replacing rendering calls. It serves as an extension point for adding custom rendering
 * effects, conditional rendering logic, or complete rendering overrides.
 * </p>
 * <p>
 * <b>Implementation approaches:</b>
 * <ul>
 *   <li><b>Custom entity renderer:</b> Direct implementation in your entity's renderer class</li>
 *   <li><b>Decorator pattern:</b> Wrap existing renderers with additional functionality</li>
 *   <li><b>Global render manager:</b> Central system that processes all entity rendering</li>
 * </ul>
 * </p>
 * <p>
 * <b>Example 1: Custom entity renderer implementation</b>
 * <pre>{@code
 * // Basic implementation example
 * public class MyEntityRenderer<TargetCustomizer extends MyEntity> extends EntityRenderer<TargetCustomizer>
 *         implements EntityRenderRedirector<TargetCustomizer> {
 *
 *     private final EntityModel<TargetCustomizer> model;
 *
 *     public MyEntityRenderer(EntityRendererProvider.Context context, EntityModel<TargetCustomizer> model) {
 *         super(context);
 *         this.model = model;
 *     }
 *
 *     @Override
 *     public void redirectRender(EntityRenderer<TargetCustomizer> renderer, TargetCustomizer entity,
 *                       float entityYaw, float partialTicks, PoseStack poseStack,
 *                       MultiBufferSource bufferSource, int packedLight) {
 *         // Custom rendering logic here
 *         // This method is called via the interface
 *         this.model.renderToBuffer(poseStack,
 *             bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(entity))),
 *             packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
 *     }
 *
 *     @Override
 *     public ResourceLocation getTextureLocation(TargetCustomizer entity) {
 *         return entity.getCustomTexture();
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @param <E> the entity type that this redirector handles
 *
 * @see net.minecraft.client.renderer.entity.EntityRenderer
 * @see #renderToBuffer(EntityModel, E, PoseStack, VertexConsumer, int, int, int)
 *
 * @author JiXingDeFeng
 * @since 0.0.1-dev
 */
public interface EntityRenderRedirector<E extends LivingEntity, M extends EntityModel<E>> {

    /**
     * Redirects the rendering of an entity model to a vertex buffer with the entity parameter.
     * <p>
     * This method provides a hook for modifying entity rendering by intercepting calls to
     * {@link EntityModel#renderToBuffer(PoseStack, VertexConsumer, int, int, int)}. The default
     * implementation delegates to the overloaded version without the entity parameter.
     * </p>
     * <p>
     * Implementations can override this method to apply custom rendering effects, modify
     * rendering parameters, or conditionally alter the rendering based on entity state.
     * </p>
     *
     * @param model           the entity model to render
     * @param entity          the entity being rendered
     * @param poseStack       the pose stack for transformation operations
     * @param vertexConsumer  the vertex consumer for buffer writing
     * @param packedLight     packed light coordinates for lighting calculations
     * @param packedOverlay   packed overlay texture coordinates
     * @param color           the color in ARGB format (Alpha, Red, Green, Blue)
     *
     * @implNote This method is typically called from Mixin interceptors that capture
     *           {@code EntityModel.renderToBuffer()} calls. The entity parameter is
     *           extracted from the local variables of the intercepted method.
     * @implSpec The default implementation calls
     *           {@link #renderToBuffer(EntityModel, PoseStack, VertexConsumer, int, int, int)}
     *           with the same parameters (excluding the entity). Overriding implementations
     *           should either call the super method or provide equivalent functionality.
     *
     * @see EntityModel#renderToBuffer(PoseStack, VertexConsumer, int, int, int)
     */
    default void renderToBuffer(
            @NotNull M model,
            @NotNull E entity,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        this.renderToBuffer(model, poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * Redirects the rendering of an entity model to a vertex buffer.
     * <p>
     * This method provides the core rendering functionality and is called by the overloaded
     * version that includes an entity parameter. It directly invokes the underlying
     * {@link EntityModel#renderToBuffer(PoseStack, VertexConsumer, int, int, int)} method.
     * </p>
     * <p>
     * This method can be overridden to apply global rendering modifications that don't
     * require access to the specific entity instance, such as shader effects, global
     * color transformations, or post-processing effects.
     * </p>
     *
     * @param model           the entity model to render
     * @param poseStack       the pose stack for transformation operations
     * @param vertexConsumer  the vertex consumer for buffer writing
     * @param packedLight     packed light coordinates for lighting calculations
     * @param packedOverlay   packed overlay texture coordinates
     * @param color           the color in ARGB format (Alpha, Red, Green, Blue)
     *
     * @implNote This method serves as the final rendering endpoint. Overriding implementations
     *           should generally call {@code model.renderToBuffer()} at some point to ensure
     *           the model is actually rendered.
     * @implSpec The default implementation directly calls
     *           {@link EntityModel#renderToBuffer(PoseStack, VertexConsumer, int, int, int)}.
     *           Overriding implementations must ensure the model is rendered to the buffer.
     *
     * @see EntityModel#renderToBuffer(PoseStack, VertexConsumer, int, int, int)
     * @see #renderToBuffer(EntityModel, E, PoseStack, VertexConsumer, int, int, int)
     */
    default void renderToBuffer(
            @NotNull M model,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
