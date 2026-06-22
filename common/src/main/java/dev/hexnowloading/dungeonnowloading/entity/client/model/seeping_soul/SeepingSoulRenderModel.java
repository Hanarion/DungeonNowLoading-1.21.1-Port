package dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import net.minecraft.client.model.geom.ModelPart;

public interface SeepingSoulRenderModel {
    void setupAnim(SeepingSoulEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);
    void renderToBuffer(PoseStack poseStack, VertexConsumer vc, int packedLight, int packedOverlay, int color);
    ModelPart root();
}
