package dev.hexnowloading.dungeonnowloading.item.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.item.DNLAnimationState;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.AnimatedItemModel;
import dev.hexnowloading.dungeonnowloading.item.client.animation.ScorcherAnimation;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ScorcherModel extends AnimatedItemModel {
    public static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/item/scorcher.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "scorcher"), "main");
    private final ModelPart root;
    private final ModelPart scorcher;
    private final ModelPart vfx;
    private final ModelPart smokepuffsmall;
    private final ModelPart smokepuffbig;
    private final ModelPart smokepuffcluster;
    private final ModelPart firebig;
    private final ModelPart firemedium;
    private final ModelPart firesmall;
    private final ModelPart corpus;
    private final ModelPart base;
    private final ModelPart head;
    private final ModelPart upperjaw;
    private final ModelPart lowerjaw;
    private final ModelPart hilt;
    private final ModelPart neck;
    private final ModelPart flameexit;

    public ScorcherModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.scorcher = root.getChild("scorcher");
        this.vfx = this.scorcher.getChild("vfx");
        this.smokepuffsmall = this.vfx.getChild("smokepuffsmall");
        this.smokepuffbig = this.vfx.getChild("smokepuffbig");
        this.smokepuffcluster = this.vfx.getChild("smokepuffcluster");
        this.firebig = this.vfx.getChild("firebig");
        this.firemedium = this.vfx.getChild("firemedium");
        this.firesmall = this.vfx.getChild("firesmall");
        this.corpus = this.scorcher.getChild("corpus");
        this.base = this.corpus.getChild("base");
        this.head = this.corpus.getChild("head");
        this.upperjaw = this.head.getChild("upperjaw");
        this.lowerjaw = this.head.getChild("lowerjaw");
        this.hilt = this.corpus.getChild("hilt");
        this.neck = this.corpus.getChild("neck");
        this.flameexit = this.neck.getChild("flameexit");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition scorcher = partdefinition.addOrReplaceChild("scorcher", CubeListBuilder.create(), PartPose.offset(-0.5F, 21.5F, 2.0F));

        PartDefinition vfx = scorcher.addOrReplaceChild("vfx", CubeListBuilder.create(), PartPose.offset(0.5F, 2.5F, -2.0F));

        PartDefinition smokepuffsmall = vfx.addOrReplaceChild("smokepuffsmall", CubeListBuilder.create().texOffs(44, 30).addBox(-9.0F, -7.0F, 0.0F, 10.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-49.0F, -2.0F, -1.0F));

        PartDefinition smokepuffbig = vfx.addOrReplaceChild("smokepuffbig", CubeListBuilder.create().texOffs(40, 48).addBox(-1.0F, -10.0F, 0.0F, 12.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-33.0F, 0.0F, -1.0F));

        PartDefinition smokepuffcluster = vfx.addOrReplaceChild("smokepuffcluster", CubeListBuilder.create().texOffs(40, 38).addBox(-6.0F, -5.0F, 0.0F, 12.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-41.0F, -5.0F, -1.0F));

        PartDefinition firebig = vfx.addOrReplaceChild("firebig", CubeListBuilder.create().texOffs(54, 0).addBox(-2.5F, -8.0F, 0.0F, 5.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.5F, -3.0F, -1.0F));

        PartDefinition firemedium = vfx.addOrReplaceChild("firemedium", CubeListBuilder.create().texOffs(46, 0).addBox(-2.0F, -6.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, -2.0F, -1.0F));

        PartDefinition firesmall = vfx.addOrReplaceChild("firesmall", CubeListBuilder.create().texOffs(40, 0).addBox(-1.5F, -5.0F, 0.0F, 3.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-11.5F, -1.0F, -1.0F));

        PartDefinition corpus = scorcher.addOrReplaceChild("corpus", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.0F));

        PartDefinition base = corpus.addOrReplaceChild("base", CubeListBuilder.create().texOffs(13, 5).addBox(-0.5F, -7.0F, -3.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -5.0F, -2.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 2.5F, -1.0F));

        PartDefinition head = corpus.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.5F, 2.5F, -1.0F));

        PartDefinition upperjaw = head.addOrReplaceChild("upperjaw", CubeListBuilder.create().texOffs(22, 22).addBox(-2.5F, -2.5F, -5.5F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -2.5F, -6.5F));

        PartDefinition cube_r1 = upperjaw.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(17, 10).addBox(0.0F, -3.0F, -2.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.5F, -0.5F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r2 = upperjaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(17, 10).addBox(0.0F, -3.0F, -2.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.5F, -0.5F, 0.0F, 0.0F, 0.2618F));

        PartDefinition lowerjaw = head.addOrReplaceChild("lowerjaw", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, -0.5F, -5.5F, 5.0F, 3.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offset(-0.5F, -2.5F, -6.5F));

        PartDefinition hilt = corpus.addOrReplaceChild("hilt", CubeListBuilder.create().texOffs(24, 32).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(19, 9).addBox(1.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

        PartDefinition neck = corpus.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -4.0F, -2.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(20, 11).addBox(-2.0F, -4.0F, 1.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.3F)), PartPose.offset(0.5F, 2.5F, -8.0F));

        PartDefinition flameexit = neck.addOrReplaceChild("flameexit", CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -2.0F, -3.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, -3.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        scorcher.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setUpAnim(ScorcherItem item, Player player, ItemStack stack, float ageInTicks) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        item.updateAnimation(stack, player.level(), player);

        DNLAnimationState genericState = item.getAnimationState(stack);
        float progress = item.getAnimationProgress(stack, player.level());

        if (genericState instanceof ScorcherItem.ScorcherAnimationState state) {
            switch (state) {
                case SCORCHER_ACTIVATED ->
                        this.animate("scorcher_activated", ScorcherAnimation.SCORCHER_ACTIVATE, progress);
                case SCORCHER_IDLE ->
                        this.animate("scorcher_idle", ScorcherAnimation.SCORCHER_SHOOT, progress);
            }
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
