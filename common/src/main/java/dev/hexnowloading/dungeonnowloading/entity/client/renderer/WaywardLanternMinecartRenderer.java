package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WaywardLanternMinecartModel;
import dev.hexnowloading.dungeonnowloading.entity.misc.WaywardLanternMinecartEntity;
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

public class WaywardLanternMinecartRenderer<T extends WaywardLanternMinecartEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/wayward_lantern_minecart/wayward_lantern_minecart.png");
    protected final EntityModel<T> model;
    private final BlockRenderDispatcher blockRenderer;

    public WaywardLanternMinecartRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.shadowRadius = 0.7F;
        this.model = new WaywardLanternMinecartModel<>(renderManager.bakeLayer(WaywardLanternMinecartModel.LAYER_LOCATION));
        this.blockRenderer = renderManager.getBlockRenderDispatcher();
    }

    public void render(T $$0, float $$1, float $$2, PoseStack $$3, MultiBufferSource $$4, int $$5) {
        super.render($$0, $$1, $$2, $$3, $$4, $$5);
        $$3.pushPose();
        long $$6 = (long)$$0.getId() * 493286711L;
        $$6 = $$6 * $$6 * 4392167121L + $$6 * 98761L;
        float $$7 = (((float)($$6 >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float $$8 = (((float)($$6 >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float $$9 = (((float)($$6 >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        $$3.translate($$7, $$8, $$9);
        double $$10 = Mth.lerp((double)$$2, $$0.xOld, $$0.getX());
        double $$11 = Mth.lerp((double)$$2, $$0.yOld, $$0.getY());
        double $$12 = Mth.lerp((double)$$2, $$0.zOld, $$0.getZ());
        double $$13 = 0.30000001192092896;
        Vec3 $$14 = $$0.getPos($$10, $$11, $$12);
        float $$15 = Mth.lerp($$2, $$0.xRotO, $$0.getXRot());
        if ($$14 != null) {
            Vec3 $$16 = $$0.getPosOffs($$10, $$11, $$12, 0.30000001192092896);
            Vec3 $$17 = $$0.getPosOffs($$10, $$11, $$12, -0.30000001192092896);
            if ($$16 == null) {
                $$16 = $$14;
            }

            if ($$17 == null) {
                $$17 = $$14;
            }

            $$3.translate($$14.x - $$10, ($$16.y + $$17.y) / 2.0 - $$11, $$14.z - $$12);
            Vec3 $$18 = $$17.add(-$$16.x, -$$16.y, -$$16.z);
            if ($$18.length() != 0.0) {
                $$18 = $$18.normalize();
                $$1 = (float)(Math.atan2($$18.z, $$18.x) * 180.0 / Math.PI);
                $$15 = (float)(Math.atan($$18.y) * 73.0);
            }
        }

        $$3.translate(0.0F, 0.375F, 0.0F);
        $$3.mulPose(Axis.YP.rotationDegrees(180.0F - $$1));
        $$3.mulPose(Axis.ZP.rotationDegrees(-$$15));
        float $$19 = (float)$$0.getHurtTime() - $$2;
        float $$20 = $$0.getDamage() - $$2;
        if ($$20 < 0.0F) {
            $$20 = 0.0F;
        }

        if ($$19 > 0.0F) {
            $$3.mulPose(Axis.XP.rotationDegrees(Mth.sin($$19) * $$19 * $$20 / 10.0F * (float)$$0.getHurtDir()));
        }

        int $$21 = $$0.getDisplayOffset();
        BlockState $$22 = $$0.getDisplayBlockState();
        if ($$22.getRenderShape() != RenderShape.INVISIBLE) {
            $$3.pushPose();
            float $$23 = 0.75F;
            $$3.scale(0.75F, 0.75F, 0.75F);
            $$3.translate(-0.5F, (float)($$21 - 8) / 16.0F, 0.5F);
            $$3.mulPose(Axis.YP.rotationDegrees(90.0F));
            this.renderMinecartContents($$0, $$2, $$22, $$3, $$4, $$5);
            $$3.popPose();
        }

        $$3.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim($$0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer $$24 = $$4.getBuffer(this.model.renderType(this.getTextureLocation($$0)));
        this.model.renderToBuffer($$3, $$24, $$5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        $$3.popPose();
    }

    public ResourceLocation getTextureLocation(T $$0) {
        return TEXTURE;
    }

    protected void renderMinecartContents(T $$0, float $$1, BlockState $$2, PoseStack $$3, MultiBufferSource $$4, int $$5) {
        this.blockRenderer.renderSingleBlock($$2, $$3, $$4, $$5, OverlayTexture.NO_OVERLAY);
    }
}

