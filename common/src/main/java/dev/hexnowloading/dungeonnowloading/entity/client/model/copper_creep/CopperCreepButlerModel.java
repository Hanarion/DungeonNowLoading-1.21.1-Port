package dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.copper_creep.CopperCreepButlerAnimation;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class CopperCreepButlerModel<T extends CopperCreepEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "copper_creep_butler"), "main");
    private final ModelPart coppercreep;
    private final ModelPart body;
    private final ModelPart tie;
    private final ModelPart suit;
    private final ModelPart tail;
    private final ModelPart mustache;
    private final ModelPart rod;
    private final ModelPart bodycube;
    private final ModelPart coffee_set;
    private final ModelPart coffee;
    private final ModelPart plate;
    private final ModelPart legs;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final ModelPart root;

    public CopperCreepButlerModel(ModelPart root) {
        this.coppercreep = root.getChild("coppercreep");
        this.body = this.coppercreep.getChild("body");
        this.tie = this.body.getChild("tie");
        this.suit = this.body.getChild("suit");
        this.tail = this.suit.getChild("tail");
        this.mustache = this.body.getChild("mustache");
        this.rod = this.body.getChild("rod");
        this.bodycube = this.body.getChild("bodycube");
        this.coffee_set = this.bodycube.getChild("coffee_set");
        this.coffee = this.coffee_set.getChild("coffee");
        this.plate = this.coffee_set.getChild("plate");
        this.legs = this.coppercreep.getChild("legs");
        this.left_leg = this.legs.getChild("left_leg");
        this.right_leg = this.legs.getChild("right_leg");
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition coppercreep = partdefinition.addOrReplaceChild("coppercreep", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition body = coppercreep.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.5F, 0.5F));

        PartDefinition tie = body.addOrReplaceChild("tie", CubeListBuilder.create().texOffs(57, 5).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5F, -4.75F));

        PartDefinition suit = body.addOrReplaceChild("suit", CubeListBuilder.create().texOffs(28, 46).addBox(-4.5F, -4.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = suit.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 5.0F));

        PartDefinition cube_r1 = tail.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 0).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.7F, -0.35F, 0.3927F, 0.0F, 0.0F));

        PartDefinition mustache = body.addOrReplaceChild("mustache", CubeListBuilder.create().texOffs(0, 37).addBox(-6.0F, -1.0F, -4.75F, 12.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rod = body.addOrReplaceChild("rod", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-2.0F, -6.0F, -1.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -0.5F));

        PartDefinition bodycube = body.addOrReplaceChild("bodycube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -13.0F, -4.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 8.5F, -0.5F));

        PartDefinition coffee_set = bodycube.addOrReplaceChild("coffee_set", CubeListBuilder.create(), PartPose.offset(-4.5F, -8.0F, 0.0F));

        PartDefinition coffee = coffee_set.addOrReplaceChild("coffee", CubeListBuilder.create().texOffs(56, 15).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 18).addBox(-1.5F, -2.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(62, 23).addBox(-2.5F, -2.5F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(58, 29).addBox(-1.5F, -7.0F, 0.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, -0.0023F, 0.5F));

        PartDefinition plate = coffee_set.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(44, 9).addBox(-5.0F, 0.0F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(52, 25).addBox(-4.0F, 0.75F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.0023F, 0.5F));

        PartDefinition legs = coppercreep.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition left_leg = legs.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 18).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, -4.0F, 0.5F));

        PartDefinition right_leg = legs.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 26).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, -4.0F, 0.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(CopperCreepEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.animate(entity.idleAnimationState, CopperCreepButlerAnimation.IDLE, ageInTicks);
        this.animate(entity.summonAnimationState, CopperCreepButlerAnimation.SUMMON, ageInTicks);
        this.animate(entity.detonationAnimationState, CopperCreepButlerAnimation.DETONATION, ageInTicks);
        this.animate(entity.sitAnimationState, CopperCreepButlerAnimation.SIT, ageInTicks);
        this.animate(entity.standAnimationState, CopperCreepButlerAnimation.STAND, ageInTicks);
        this.animate(entity.wrongOwnerAnimationState, CopperCreepButlerAnimation.WRONG_OWNER, ageInTicks);
        this.animate(entity.sittingAnimationState, CopperCreepButlerAnimation.SITTING, ageInTicks);
        this.animate(entity.sittingDetonationAnimationState, CopperCreepButlerAnimation.DETONATION_SITTING, ageInTicks);

        if (entity.getState() == CopperCreepEntity.State.IDLE || entity.getState() == CopperCreepEntity.State.WALKING_TOWARDS_PLAYER || entity.getState() == CopperCreepEntity.State.WANDERING) {
            this.animateWalk(CopperCreepButlerAnimation.WALKING, limbSwing, limbSwingAmount, 4.0f, 4.5f);
        } else if (entity.getState() == CopperCreepEntity.State.FOLLOWING || entity.getState() == CopperCreepEntity.State.RUNNING_TOWARDS_PLAYER) {
            this.animateWalk(CopperCreepButlerAnimation.RUNNING, limbSwing, limbSwingAmount, 2f, 2.25f);
        }
        this.animateHeadLookTarget(netHeadYaw, headPitch);
    }

    private void animateHeadLookTarget(float netHeadYaw, float headPitch) {
        this.coppercreep.yRot = netHeadYaw * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        coppercreep.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
