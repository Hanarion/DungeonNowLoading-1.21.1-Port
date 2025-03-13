package dev.hexnowloading.dungeonnowloading.item.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Function;

public class AnimatedItemModel extends Model {

    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public AnimatedItemModel(Function<ResourceLocation, RenderType> typeFunction) {
        super(typeFunction);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
    }

    public ModelPart root() {
        return null;
    }

    public Optional<ModelPart> getAnyDescendantWithName(String string) {
        return string.equals("root") ? Optional.of(this.root()) : this.root().getAllParts().filter((p_233400_) -> {
            return p_233400_.hasChild(string);
        }).findFirst().map((p_233397_) -> {
            return p_233397_.getChild(string);
        });
    }

    public void animate(AnimationState animationState, AnimationDefinition animationDefinition ,float ageInTicks) {
        this.animate(animationState, animationDefinition, ageInTicks, 1.0F);
    }

    public void animate(AnimationState animationState, AnimationDefinition animationDefinition, float ageInTicks, float i) {
        animationState.updateTime(ageInTicks, i);
        animationState.ifStarted(animationState1 -> {
            ItemKeyframeAnimations.animate(this, animationDefinition, animationState1.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE);
        });
    }
}
