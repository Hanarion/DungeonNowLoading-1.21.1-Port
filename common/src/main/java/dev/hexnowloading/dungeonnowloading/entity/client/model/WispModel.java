package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.WispAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;

public class WispModel<T extends WispEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "wisp"), "main");
    private final ModelPart wisp;
    private final ModelPart outerlayer;
    private final ModelPart root;

    public WispModel(ModelPart root) {
        this.root = root;
        this.wisp = root.getChild("wisp");
        this.outerlayer = root.getChild("outerlayer");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wisp = partdefinition.addOrReplaceChild("wisp", CubeListBuilder.create().texOffs(0, 15).addBox(-2.5F, -3.0F, -5.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 15.0F, 3.0F));

        PartDefinition outerlayer = partdefinition.addOrReplaceChild("outerlayer", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4.0F, -6.0F, 7.0F, 7.0F, 7.0F, new CubeDeformation(-0.4F)), PartPose.offset(0.5F, 15.0F, 3.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    private final AnimationState idleLoop = new AnimationState();

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.idleLoop.startIfStopped(entity.tickCount);
        this.animate(idleLoop, WispAnimation.IDLE, ageInTicks);

        this.animate(entity.flareUpAnimationState, WispAnimation.FLARE_UP, ageInTicks);
        this.animate(entity.tackleStartAnimationState, WispAnimation.TACKLE_START, ageInTicks);
        this.animate(entity.tackleAnimationState, WispAnimation.TACKLE, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        wisp.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        outerlayer.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
