package dev.hexnowloading.dungeonnowloading.item.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemKeyframeAnimations {

    public static void animate(AnimatedItemModel model, AnimationDefinition animation, long time, float interpolationFactor, Vector3f animationCache) {
        float elapsedTime = getElapsedSeconds(animation, time);

        for (Map.Entry<String, List<AnimationChannel>> entry : animation.boneAnimations().entrySet()) {
            Optional<ModelPart> part = model.getAnyDescendantWithName(entry.getKey());
            List<AnimationChannel> channels = entry.getValue();

            part.ifPresent(modelPart -> {
                channels.forEach(channel -> {
                    Keyframe[] keyframes = channel.keyframes();
                    int keyframeIndex = Math.max(0, Mth.binarySearch(0, keyframes.length, i -> elapsedTime <= keyframes[i].timestamp()) - 1);
                    int nextKeyframeIndex = Math.min(keyframes.length - 1, keyframeIndex + 1);

                    Keyframe currentKeyframe = keyframes[keyframeIndex];
                    Keyframe nextKeyframe = keyframes[nextKeyframeIndex];

                    float timeDifference = elapsedTime - currentKeyframe.timestamp();
                    float transitionProgress = (nextKeyframeIndex != keyframeIndex)
                            ? Mth.clamp(timeDifference / (nextKeyframe.timestamp() - currentKeyframe.timestamp()), 0.0F, 1.0F)
                            : 0.0F;

                    nextKeyframe.interpolation().apply(animationCache, transitionProgress, keyframes, keyframeIndex, nextKeyframeIndex, interpolationFactor);
                    channel.target().apply(modelPart, animationCache);
                });
            });
        }
    }

    private static float getElapsedSeconds(AnimationDefinition animation, long time) {
        float elapsedTime = time / 1000.0F;
        return animation.looping() ? elapsedTime % animation.lengthInSeconds() : elapsedTime;
    }

    public static Vector3f posVec(float x, float y, float z) {
        return new Vector3f(x, -y, z);
    }

    public static Vector3f degreeVec(float x, float y, float z) {
        return new Vector3f(x * 0.017453292F, y * 0.017453292F, z * 0.017453292F);
    }

    public static Vector3f scaleVec(double x, double y, double z) {
        return new Vector3f((float) (x - 1.0), (float) (y - 1.0), (float) (z - 1.0));
    }
}