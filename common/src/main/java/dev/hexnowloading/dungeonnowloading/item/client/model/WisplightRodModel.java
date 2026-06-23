package dev.hexnowloading.dungeonnowloading.item.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.item.WisplightRodItem;
import dev.hexnowloading.dungeonnowloading.item.client.AnimatedItemModel;
import dev.hexnowloading.dungeonnowloading.item.client.ItemKeyframeAnimations;
import dev.hexnowloading.dungeonnowloading.item.client.animation.WisplightRodAnimation;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class WisplightRodModel extends AnimatedItemModel {
    private static final int CHARGE_UP_START_TICKS = 10;
    private static final int CHARGED_START_TICKS = 30;
    private static final float CHARGE_UP_DURATION_TICKS = CHARGED_START_TICKS - CHARGE_UP_START_TICKS;
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/item/wisplight_rod/wisplight_rod_handheld.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "wisplight_rod"), "main");
    private final ModelPart root;
    private final ModelPart All;
    private final ModelPart Wand;
    private final ModelPart UppperPart;
    private final ModelPart FireBall;
    private final ModelPart Baller;
    private final ModelPart core;

    public WisplightRodModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.All = root.getChild("All");
        this.Wand = this.All.getChild("Wand");
        this.UppperPart = this.All.getChild("UppperPart");
        this.FireBall = this.All.getChild("FireBall");
        this.Baller = this.FireBall.getChild("Baller");
        this.core = this.Baller.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 15.8F, 0.0F));

        PartDefinition Wand = All.addOrReplaceChild("Wand", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, -7.8F, -1.0F, 2.0F, 19.0F, 2.0F, new CubeDeformation(-0.2F))
                .texOffs(23, 36).addBox(-1.0F, -4.8F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 36).addBox(-1.0F, 8.2F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-3.0F, -8.8F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F))
                .texOffs(26, 17).addBox(-3.0F, 11.0F, 0.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(9, 20).addBox(-2.0F, 11.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = Wand.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 36).addBox(-2.0F, -4.0F, 0.0F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8F, -8.6F, 2.8F, 0.0F, 0.7418F, 0.0F));

        PartDefinition cube_r2 = Wand.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(9, 34).addBox(-2.0F, -4.0F, 0.0F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8F, -8.6F, -2.8F, 0.0F, -0.7418F, 0.0F));

        PartDefinition cube_r3 = Wand.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(36, 22).addBox(-1.0F, -4.0F, 0.0F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8F, -8.6F, 2.8F, 0.0F, -0.7418F, 0.0F));

        PartDefinition cube_r4 = Wand.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(33, 0).addBox(-1.0F, -4.0F, 0.0F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8F, -8.6F, -2.8F, 0.0F, 0.7418F, 0.0F));

        PartDefinition UppperPart = All.addOrReplaceChild("UppperPart", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -1.25F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(-0.2F))
                .texOffs(25, 11).addBox(-3.5F, -6.05F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.55F, 0.0F));

        PartDefinition cube_r5 = UppperPart.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(18, 29).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8F, -1.05F, 3.8F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r6 = UppperPart.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(9, 27).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8F, -1.05F, -3.8F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r7 = UppperPart.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(27, 29).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8F, -1.05F, 3.8F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r8 = UppperPart.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(26, 22).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8F, -1.05F, -3.8F, 0.0F, 0.7854F, 0.0F));

        PartDefinition FireBall = All.addOrReplaceChild("FireBall", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition Baller = FireBall.addOrReplaceChild("Baller", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition core = Baller.addOrReplaceChild("core", CubeListBuilder.create().texOffs(36, 30).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F))
                .texOffs(45, 30).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    public void setupAnim(WisplightRodItem item, Player player, ItemStack stack, float partialTicks) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float ageInSeconds = (player.tickCount + partialTicks) / 20.0F;
        animateLoop(WisplightRodAnimation.IDLE, ageInSeconds);
        animateLoop(WisplightRodAnimation.BALL_SPIN, ageInSeconds);

        if (!player.isUsingItem() || player.getUseItem().isEmpty() || !player.getUseItem().is(stack.getItem())) {
            return;
        }

        float useTicks = stack.getUseDuration(player) - player.getUseItemRemainingTicks() + partialTicks;
        if (useTicks >= CHARGED_START_TICKS) {
            animateLoop(WisplightRodAnimation.CHARGEED, (useTicks - CHARGED_START_TICKS) / 20.0F);
        } else if (useTicks >= CHARGE_UP_START_TICKS) {
            float progress = Math.min((useTicks - CHARGE_UP_START_TICKS) / CHARGE_UP_DURATION_TICKS, 1.0F);
            animateOnce(WisplightRodAnimation.CHARGE_UP, progress);
        }
    }

    private void animateLoop(net.minecraft.client.animation.AnimationDefinition animationDefinition, float elapsedSeconds) {
        ItemKeyframeAnimations.animate(this, animationDefinition, (long) (elapsedSeconds * 1000.0F), 1.0F, ANIMATION_VECTOR_CACHE);
    }

    private void animateOnce(net.minecraft.client.animation.AnimationDefinition animationDefinition, float progress) {
        ItemKeyframeAnimations.animate(this, animationDefinition, (long) (progress * animationDefinition.lengthInSeconds() * 1000.0F), 1.0F, ANIMATION_VECTOR_CACHE);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        All.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void renderSolidToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        int color = net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue);
        poseStack.pushPose();
        this.All.translateAndRotate(poseStack);
        this.Wand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.UppperPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    public void renderTranslucentToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        int color = net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue);
        poseStack.pushPose();
        this.All.translateAndRotate(poseStack);
        this.FireBall.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
