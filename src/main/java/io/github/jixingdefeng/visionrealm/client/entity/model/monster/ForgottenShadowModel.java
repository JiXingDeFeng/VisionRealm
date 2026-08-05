package io.github.jixingdefeng.visionrealm.client.entity.model.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.client.entity.animation.ForgottenShadowAnimation;
import io.github.jixingdefeng.visionrealm.content.world.entity.custom.monster.ForgottenShadow;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ForgottenShadowModel<T extends ForgottenShadow> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "forgotten_shadow"), "main");
    private final ModelPart root;
    private final ModelPart head;

    public ForgottenShadowModel(ModelPart root) {
        this.root = root;
        this.head = this.root.getChild("upper_body").getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition lower_body = partdefinition.addOrReplaceChild("lower_body", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition left_foot = lower_body.addOrReplaceChild("left_foot", CubeListBuilder.create(), PartPose.offset(2.0F, -4.0F, 0.0F));

        PartDefinition left_foot_1 = left_foot.addOrReplaceChild("left_foot_1", CubeListBuilder.create().texOffs(0, 37).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_foot_2 = left_foot.addOrReplaceChild("left_foot_2", CubeListBuilder.create().texOffs(0, 53).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -2.0F));

        PartDefinition right_foot = lower_body.addOrReplaceChild("right_foot", CubeListBuilder.create(), PartPose.offset(-2.0F, -4.0F, 0.0F));

        PartDefinition right_foot_1 = right_foot.addOrReplaceChild("right_foot_1", CubeListBuilder.create().texOffs(16, 37).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_foot_2 = right_foot.addOrReplaceChild("right_foot_2", CubeListBuilder.create().texOffs(16, 53).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -2.0F));

        PartDefinition upper_body = partdefinition.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 2.0F));

        PartDefinition head = upper_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, -2.0F));

        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(33, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(0, 28).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.15F))
                .texOffs(0, 0).addBox(-5.5F, -6.0F, -5.5F, 11.0F, 1.0F, 11.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = upper_body.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_1 = body.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(32, 13).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(32, 21).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition body_3 = body.addOrReplaceChild("body_3", CubeListBuilder.create().texOffs(32, 29).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_hand = upper_body.addOrReplaceChild("left_hand", CubeListBuilder.create(), PartPose.offset(6.0F, -10.0F, -2.0F));

        PartDefinition left_hand_1 = left_hand.addOrReplaceChild("left_hand_1", CubeListBuilder.create().texOffs(32, 37).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_hand_2 = left_hand.addOrReplaceChild("left_hand_2", CubeListBuilder.create().texOffs(32, 53).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 2.0F));

        PartDefinition right_hand = upper_body.addOrReplaceChild("right_hand", CubeListBuilder.create(), PartPose.offset(-6.0F, -10.0F, -2.0F));

        PartDefinition right_hand_1 = right_hand.addOrReplaceChild("right_hand_1", CubeListBuilder.create().texOffs(48, 37).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_hand_2 = right_hand.addOrReplaceChild("right_hand_2", CubeListBuilder.create().texOffs(48, 53).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, 2.0F));

        return LayerDefinition.create(meshdefinition, 80, 80);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animateWalk(ForgottenShadowAnimation.move, limbSwing, limbSwingAmount, (float) (entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 6.6667F), ageInTicks);
        this.animate(entity.STANDING_ANIM, ForgottenShadowAnimation.standing, ageInTicks);
        this.animate(entity.STANDBY_ANIM, ForgottenShadowAnimation.standby, ageInTicks);
        this.animate(entity.ATTACK_ANIM, ForgottenShadowAnimation.attack_1, ageInTicks);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @NotNull
    @Override
    public ModelPart root() {
        return this.root;
    }

    private void applyHeadRotation(float headYaw, float headPitch) {
        headYaw = Mth.clamp(headYaw, -40F, 40F);
        headPitch = Mth.clamp(headPitch, -40F, 40F);

        this.head.yRot = headYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }
}