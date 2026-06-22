package dev.hexnowloading.dungeonnowloading.item.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Function;

public abstract class AnimatedItemModel extends Model {

    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public AnimatedItemModel(Function<ResourceLocation, RenderType> typeFunction) {
        super(typeFunction);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
    }

    public abstract ModelPart root(); // Ensure each model provides a root

    public Optional<ModelPart> getAnyDescendantWithName(String string) {
        return string.equals("root") ? Optional.of(this.root()) : this.root().getAllParts()
                .filter(part -> part.hasChild(string))
                .findFirst()
                .map(part -> part.getChild(string));
    }

    public void animate(String animationKey, AnimationDefinition animationDefinition, ItemStack stack, Player player, float partialTicks) {
        float progress = ItemAnimationState.getProgress(stack, animationKey, player.level().getGameTime(), partialTicks);
        if (progress > 0) {
            ItemKeyframeAnimations.animate(this, animationDefinition, (long)(progress * animationDefinition.lengthInSeconds() * 1000), 1.0F, ANIMATION_VECTOR_CACHE);
        }
    }
}
