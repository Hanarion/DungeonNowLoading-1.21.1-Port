package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.MimicartModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.MimicartEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MimicartRenderer<T extends MimicartEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/mimicart/mimicart.png");
    protected final EntityModel<T> model;
    private final BlockRenderDispatcher blockRenderer;

    public MimicartRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.shadowRadius = 0.7F;
        this.model = new MimicartModel<>(renderManager.bakeLayer(MimicartModel.LAYER_LOCATION));
        this.blockRenderer = renderManager.getBlockRenderDispatcher();
    }

    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.pushPose();
        long $$6 = (long)entity.getId() * 493286711L;
        $$6 = $$6 * $$6 * 4392167121L + $$6 * 98761L;
        float $$7 = (((float)($$6 >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float $$8 = (((float)($$6 >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float $$9 = (((float)($$6 >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate($$7, $$8, $$9);
        double $$10 = Mth.lerp((double)partialTicks, entity.xOld, entity.getX());
        double $$11 = Mth.lerp((double)partialTicks, entity.yOld, entity.getY());
        double $$12 = Mth.lerp((double)partialTicks, entity.zOld, entity.getZ());
        double $$13 = 0.30000001192092896;
        Vec3 $$14 = entity.getPos($$10, $$11, $$12);
        float $$15 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if ($$14 != null) {
            Vec3 $$16 = entity.getPosOffs($$10, $$11, $$12, 0.30000001192092896);
            Vec3 $$17 = entity.getPosOffs($$10, $$11, $$12, -0.30000001192092896);
            if ($$16 == null) {
                $$16 = $$14;
            }

            if ($$17 == null) {
                $$17 = $$14;
            }

            poseStack.translate($$14.x - $$10, ($$16.y + $$17.y) / 2.0 - $$11, $$14.z - $$12);
            Vec3 $$18 = $$17.add(-$$16.x, -$$16.y, -$$16.z);
            if ($$18.length() != 0.0) {
                $$18 = $$18.normalize();
                entityYaw = (float)(Math.atan2($$18.z, $$18.x) * 180.0 / Math.PI);
                $$15 = (float)(Math.atan($$18.y) * 73.0);
            }
        }

        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-$$15));
        float $$19 = (float)entity.getHurtTime() - partialTicks;
        float $$20 = entity.getDamage() - partialTicks;
        if ($$20 < 0.0F) {
            $$20 = 0.0F;
        }

        if ($$19 > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin($$19) * $$19 * $$20 / 10.0F * (float)entity.getHurtDir()));
        }

        int $$21 = entity.getDisplayOffset();
        BlockState $$22 = entity.getDisplayBlockState();
        if ($$22.getRenderShape() != RenderShape.INVISIBLE) {
            poseStack.pushPose();
            float $$23 = 0.75F;
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(-0.5F, (float)($$21 - 8) / 16.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            this.renderMinecartContents(entity, partialTicks, $$22, poseStack, buffer, packedLight);
            poseStack.popPose();
        }

        rotateAndTranslate(poseStack);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        float ageInTicks = entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        boolean hurt = entity.getHurtTime() > 0;

        int overlay = OverlayTexture.pack(0.0F, hurt);

        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        float a = 1.0F;

        VertexConsumer vc = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vc, packedLight, overlay, r, g, b, a);

        poseStack.popPose();
    }

    public ResourceLocation getTextureLocation(T $$0) {
        return TEXTURE;
    }

    protected void renderMinecartContents(T $$0, float $$1, BlockState $$2, PoseStack $$3, MultiBufferSource $$4, int $$5) {
        this.blockRenderer.renderSingleBlock($$2, $$3, $$4, $$5, OverlayTexture.NO_OVERLAY);
    }

    // For matching the vanilla minecart model position
    private void rotateAndTranslate(PoseStack poseStack) {
        poseStack.translate(0.0F, 1.1872F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    }
}

