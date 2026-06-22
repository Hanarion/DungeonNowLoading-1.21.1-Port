package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WispwardLanternModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispwardLanternCartEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class WispwardLanternCartRenderer<T extends WispwardLanternCartEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/wispward_lantern_cart/wispward_lantern_cart.png");
    private static final ResourceLocation TIMED_TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/wispward_lantern_cart/timed_wispward_lantern_cart.png");
    protected final WispwardLanternModel<T> model;

    public WispwardLanternCartRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.shadowRadius = 0.7F;
        this.model = new WispwardLanternModel<>(renderManager.bakeLayer(WispwardLanternModel.LAYER_LOCATION));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.pushPose();

        long offsetSeed = (long) entity.getId() * 493286711L;
        offsetSeed = offsetSeed * offsetSeed * 4392167121L + offsetSeed * 98761L;
        float offsetX = (((float) (offsetSeed >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float offsetY = (((float) (offsetSeed >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float offsetZ = (((float) (offsetSeed >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate(offsetX, offsetY, offsetZ);

        double x = Mth.lerp((double) partialTicks, entity.xOld, entity.getX());
        double y = Mth.lerp((double) partialTicks, entity.yOld, entity.getY());
        double z = Mth.lerp((double) partialTicks, entity.zOld, entity.getZ());
        Vec3 railPos = entity.getPos(x, y, z);
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        if (railPos != null) {
            Vec3 front = entity.getPosOffs(x, y, z, 0.30000001192092896D);
            Vec3 back = entity.getPosOffs(x, y, z, -0.30000001192092896D);
            if (front == null) {
                front = railPos;
            }
            if (back == null) {
                back = railPos;
            }

            poseStack.translate(railPos.x - x, (front.y + back.y) / 2.0D - y, railPos.z - z);
            Vec3 railDirection = back.add(-front.x, -front.y, -front.z);
            if (railDirection.length() != 0.0D) {
                railDirection = railDirection.normalize();
                entityYaw = (float) (Math.atan2(railDirection.z, railDirection.x) * 180.0D / Math.PI);
                xRot = (float) (Math.atan(railDirection.y) * 73.0D);
            }
        }

        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));

        float hurtTime = (float) entity.getHurtTime() - partialTicks;
        float damage = entity.getDamage() - partialTicks;
        if (damage < 0.0F) {
            damage = 0.0F;
        }
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) entity.getHurtDir()));
        }

        rotateAndTranslate(poseStack);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        float ageInTicks = entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        int overlay = OverlayTexture.NO_OVERLAY;
        VertexConsumer baseConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderBase(poseStack, baseConsumer, packedLight, overlay);

        if (entity.isLit()) {
            VertexConsumer lightConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(this.getTextureLocation(entity), true));
            this.model.renderLight(poseStack, lightConsumer, LightTexture.FULL_BRIGHT, overlay);
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.isTimed() ? TIMED_TEXTURE : TEXTURE;
    }

    private void rotateAndTranslate(PoseStack poseStack) {
        poseStack.translate(0.0F, 1.1872F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }
}
