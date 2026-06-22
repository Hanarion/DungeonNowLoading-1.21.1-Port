package dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.seeping_soul.SeepingSoulSerpentCallerAnimation;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class SeepingSoulSerpentCallerModel<T extends SeepingSoulEntity> extends HierarchicalModel<T> implements SeepingSoulRenderModel{
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "seeping_soul_fairkeeper_serpent_caller"), "main");
    private final ModelPart root;
    private final ModelPart BourosSoul;
    private final ModelPart CompleteSoul;
    private final ModelPart eye;
    private final ModelPart pupil;
    private final ModelPart Souls;
    private final ModelPart BorosSoul;
    private final ModelPart BorosHead;
    private final ModelPart eye1;
    private final ModelPart jaw1;
    private final ModelPart OurosSoul;
    private final ModelPart OurosHead;
    private final ModelPart eye2;
    private final ModelPart jaw2;

    public SeepingSoulSerpentCallerModel(ModelPart root) {
        this.root = root;
        this.BourosSoul = root.getChild("BourosSoul");
        this.CompleteSoul = this.BourosSoul.getChild("CompleteSoul");
        this.eye = this.CompleteSoul.getChild("eye");
        this.pupil = this.eye.getChild("pupil");
        this.Souls = this.CompleteSoul.getChild("Souls");
        this.BorosSoul = this.Souls.getChild("BorosSoul");
        this.BorosHead = this.BorosSoul.getChild("BorosHead");
        this.eye1 = this.BorosHead.getChild("eye1");
        this.jaw1 = this.BorosSoul.getChild("jaw1");
        this.OurosSoul = this.Souls.getChild("OurosSoul");
        this.OurosHead = this.OurosSoul.getChild("OurosHead");
        this.eye2 = this.OurosHead.getChild("eye2");
        this.jaw2 = this.OurosSoul.getChild("jaw2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition BourosSoul = partdefinition.addOrReplaceChild("BourosSoul", CubeListBuilder.create(), PartPose.offset(0.0F, 15.5F, 0.0F));

        PartDefinition CompleteSoul = BourosSoul.addOrReplaceChild("CompleteSoul", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition eye = CompleteSoul.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(56, 38).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition pupil = eye.addOrReplaceChild("pupil", CubeListBuilder.create().texOffs(62, 36).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -2.1F));

        PartDefinition Souls = CompleteSoul.addOrReplaceChild("Souls", CubeListBuilder.create(), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition BorosSoul = Souls.addOrReplaceChild("BorosSoul", CubeListBuilder.create().texOffs(32, 29).addBox(-6.5714F, -6.5F, -6.1429F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-6.5714F, 2.5F, -1.1429F, 5.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-1.5714F, 2.5F, 4.8571F, 11.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(52, 19).addBox(4.4286F, 2.5F, 0.8571F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.4286F, -1.5F, -1.8571F));

        PartDefinition BorosHead = BorosSoul.addOrReplaceChild("BorosHead", CubeListBuilder.create().texOffs(1, 61).addBox(-2.0F, -4.5F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(0, 42).addBox(0.0F, -2.5F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offset(-1.5714F, -4.0F, -3.6429F));

        PartDefinition eye1 = BorosHead.addOrReplaceChild("eye1", CubeListBuilder.create().texOffs(73, 2).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(1.5F, -1.1F, -2.6F));

        PartDefinition jaw1 = BorosSoul.addOrReplaceChild("jaw1", CubeListBuilder.create().texOffs(40, 48).addBox(0.0F, -1.0F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(-2.5714F, -3.5F, -3.6429F));

        PartDefinition OurosSoul = Souls.addOrReplaceChild("OurosSoul", CubeListBuilder.create().texOffs(32, 10).addBox(1.7857F, -7.5F, -6.1429F, 5.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.7857F, -7.5F, -1.1429F, 5.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(52, 10).addBox(-9.2143F, -7.5F, 0.8571F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-9.2143F, -7.5F, 4.8571F, 11.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.2143F, 1.5F, -1.8571F));

        PartDefinition OurosHead = OurosSoul.addOrReplaceChild("OurosHead", CubeListBuilder.create().texOffs(38, 59).addBox(-4.0F, 2.4F, -2.6333F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(20, 48).addBox(-5.0F, -1.6F, -2.6333F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offset(1.7857F, 4.1F, -3.5095F));

        PartDefinition eye2 = OurosHead.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(73, 5).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(-1.5F, 1.0F, -2.7333F));

        PartDefinition jaw2 = OurosSoul.addOrReplaceChild("jaw2", CubeListBuilder.create().texOffs(0, 51).addBox(-5.0F, -2.0F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offset(2.7857F, 3.5F, -3.6429F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(SeepingSoulEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float yaw = entity.getYRot() * ((float)Math.PI / 180F);
        this.BourosSoul.yRot = yaw;

        this.animate(entity.idleAnimation, SeepingSoulSerpentCallerAnimation.IDLE, ageInTicks);
        this.animate(entity.idleBreakAnimation, SeepingSoulSerpentCallerAnimation.IDLE_BREAK, ageInTicks);
        this.animate(entity.spawnAnimation, SeepingSoulSerpentCallerAnimation.SPAWN, ageInTicks);
        this.animate(entity.hurtLeftAnimation, SeepingSoulSerpentCallerAnimation.HURT, ageInTicks);
        this.animate(entity.hurtRightAnimation, SeepingSoulSerpentCallerAnimation.HURT, ageInTicks);
        this.animate(entity.recallingAnimation, SeepingSoulSerpentCallerAnimation.RECALLING, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        BourosSoul.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
