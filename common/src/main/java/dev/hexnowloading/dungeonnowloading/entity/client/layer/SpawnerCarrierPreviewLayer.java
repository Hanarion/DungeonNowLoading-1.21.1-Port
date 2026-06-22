package dev.hexnowloading.dungeonnowloading.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SpawnerCarrierModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;

public class SpawnerCarrierPreviewLayer<T extends SpawnerCarrierEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private final EntityRenderDispatcher dispatcher;

    // If the preview is flipped/upside down, toggle these:
    private static final boolean UPRIGHT_FIX_X_180 = true;
    private static final boolean UPRIGHT_FIX_Z_180 = false;

    public SpawnerCarrierPreviewLayer(RenderLayerParent<T, M> parent,
                                      net.minecraft.client.renderer.entity.EntityRendererProvider.Context ctx) {
        super(parent);
        this.dispatcher = ctx.getEntityRenderDispatcher();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T carrier, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        if (carrier.isSpawnerBroken()) return;

        String id = carrier.getStoredEntityId();
        if (id == null || id.isEmpty()) return;

        Entity display = getOrCreateDisplayEntity(carrier, id);
        if (display == null) return;

        poseStack.pushPose();

        // ------------------------------------------------------------
        // 1) Anchor to the animated "Spawner" model part
        // ------------------------------------------------------------
        if (this.getParentModel() instanceof SpawnerCarrierModel<?> model) {
            model.root().translateAndRotate(poseStack);
            model.getAllPart().translateAndRotate(poseStack);
            model.getBodyPart().translateAndRotate(poseStack);
            model.getSpawnerPart().translateAndRotate(poseStack);
        }

        // ------------------------------------------------------------
        // 2) Smooth spin per carrier (matches vanilla "oSpin -> spin -> lerp")
        // ------------------------------------------------------------
        carrier.previewOSpin = carrier.previewSpin;

// vanilla-like speed (you can tweak multiplier)
        carrier.previewSpin = (carrier.previewSpin + 2.0F) % 360.0F;

// smooth interpolate in DEGREES
        float lerpedSpin = Mth.lerp(partialTick, carrier.previewOSpin, carrier.previewSpin);

        // ------------------------------------------------------------
        // 3) Cancel inherited rotations so our Y-spin is around true "up"
        // (prevents the "spins then snaps back" feeling)
        // ------------------------------------------------------------
        /*float yaw = Mth.rotLerp(partialTick, carrier.yBodyRotO, carrier.yBodyRot);
        float pitch = Mth.lerp(partialTick, carrier.xRotO, carrier.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));*/

        // ------------------------------------------------------------
        // 4) Fix Blockbench coordinate flips (very common)
        // If it's upside down, this usually fixes it.
        // ------------------------------------------------------------
        if (UPRIGHT_FIX_X_180) poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        if (UPRIGHT_FIX_Z_180) poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        // ------------------------------------------------------------
        // 5) Vanilla spawner scaling (auto shrink big mobs)
        // ------------------------------------------------------------
        float g = 0.53125F;
        float h = Math.max(display.getBbWidth(), display.getBbHeight());
        if (h > 1.0F) g /= h;

        // ------------------------------------------------------------
        // 6) Vanilla spawner offsets + rotations
        // ------------------------------------------------------------
        poseStack.translate(0.0F, -0.3F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(lerpedSpin));
        poseStack.translate(0.0F, -0.2F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(g, g, g);

        // ------------------------------------------------------------
        // 7) Render
        // ------------------------------------------------------------
        this.dispatcher.render(display, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Nullable
    private Entity getOrCreateDisplayEntity(T carrier, String id) {
        // Per-carrier cache so multiple carriers don't fight each other.
        if (carrier.previewEntity != null && id.equals(carrier.previewEntityId)) {
            return carrier.previewEntity;
        }

        carrier.previewEntityId = id;
        carrier.previewEntity = createDisplayEntity(carrier, id);
        return carrier.previewEntity;
    }

    @Nullable
    private Entity createDisplayEntity(T carrier, String id) {
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl);
            if (type == null) return null;

            Entity e = type.create(carrier.level());
            if (e == null) return null;

            e.setSilent(true);
            return e;
        } catch (Exception ignored) {
            return null;
        }
    }
}
