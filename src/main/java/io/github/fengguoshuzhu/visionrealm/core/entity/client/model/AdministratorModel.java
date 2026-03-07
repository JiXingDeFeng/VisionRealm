package io.github.fengguoshuzhu.visionrealm.core.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.fengguoshuzhu.visionrealm.core.VisionRealm;
import io.github.fengguoshuzhu.visionrealm.core.entity.client.animation.AdministratorAnimation;
import io.github.fengguoshuzhu.visionrealm.core.entity.custom.AdministratorEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class AdministratorModel<T extends AdministratorEntity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "administrator_model"), "main");
	private final ModelPart overall;
	private final ModelPart head;

    public AdministratorModel(ModelPart root) {
		this.overall = root.getChild("overall");
		this.head = this.overall.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition overall = partdefinition.addOrReplaceChild("overall", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition left_foot = overall.addOrReplaceChild("left_foot", CubeListBuilder.create(), PartPose.offset(2.0F, -12.0F, 0.0F));

        PartDefinition left_foot_1 = left_foot.addOrReplaceChild("left_foot_1", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_foot_2 = left_foot.addOrReplaceChild("left_foot_2", CubeListBuilder.create().texOffs(48, 26).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 46).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 6.0F, -2.0F));

        PartDefinition right_foot = overall.addOrReplaceChild("right_foot", CubeListBuilder.create(), PartPose.offset(-2.0F, -12.0F, 0.0F));

        PartDefinition reght_foot_1 = right_foot.addOrReplaceChild("reght_foot_1", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition reght_foot_2 = right_foot.addOrReplaceChild("reght_foot_2", CubeListBuilder.create().texOffs(32, 26).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 46).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 6.0F, -2.0F));

        PartDefinition right_hand = overall.addOrReplaceChild("right_hand", CubeListBuilder.create(), PartPose.offset(-6.0F, -22.0F, 0.0F));

        PartDefinition reght_hand_1 = right_hand.addOrReplaceChild("reght_hand_1", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition reght_hand_2 = right_hand.addOrReplaceChild("reght_hand_2", CubeListBuilder.create().texOffs(0, 26).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 4.0F, 2.0F));

        PartDefinition left_hand = overall.addOrReplaceChild("left_hand", CubeListBuilder.create(), PartPose.offset(6.0F, -22.0F, 0.0F));

        PartDefinition left_hand_1 = left_hand.addOrReplaceChild("left_hand_1", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 36).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_hand_2 = left_hand.addOrReplaceChild("left_hand_2", CubeListBuilder.create().texOffs(16, 26).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 46).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 4.0F, 2.0F));

        PartDefinition body = overall.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, 0.0F));

        PartDefinition body_1 = body.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(0, 56).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 56).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -2.0F, 2.0F));

        PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(0, 64).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 64).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 2.0F, 2.0F));

        PartDefinition body_3 = body.addOrReplaceChild("body_3", CubeListBuilder.create().texOffs(0, 72).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 72).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 6.0F, 2.0F));

        PartDefinition head = overall.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 96);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animate(entity.TELEPORT_TARGET_ANIM[0], AdministratorAnimation.grabTheTarget, ageInTicks);
        this.animate(entity.TELEPORT_TARGET_ANIM[1], AdministratorAnimation.teleportTarget, ageInTicks);
        this.animate(entity.OBSERVE_ANIM, AdministratorAnimation.observation, ageInTicks);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (color == -1) {
            packedOverlay = OverlayTexture.NO_OVERLAY;
        }

        this.overall.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @NotNull
    @Override
    public ModelPart root() {
        return this.overall;
    }

    private void applyHeadRotation(float headYaw, float headPitch) {
        headYaw = Mth.clamp(headYaw, -40F, 40F);
        headPitch = Mth.clamp(headPitch, -40F, 40F);

        this.head.yRot = headYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
    }
}