package dev.hexnowloading.dungeonnowloading.item.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemKeyframeAnimations {

    public static void animate(AnimatedItemModel model, AnimationDefinition animation, long time, float interpolationFactor, Vector3f animationCache) {
        float f = getElapsedSeconds(animation, time);
        Iterator var7 = animation.boneAnimations().entrySet().iterator();

        while(var7.hasNext()) {
            Map.Entry<String, List<AnimationChannel>> entry = (Map.Entry)var7.next();
            Optional<ModelPart> optional = model.getAnyDescendantWithName((String)entry.getKey());
            List<AnimationChannel> list = (List)entry.getValue();
            optional.ifPresent((p_232330_) -> {
                list.forEach((p_288241_) -> {
                    Keyframe[] akeyframe = p_288241_.keyframes();
                    int i = Math.max(0, Mth.binarySearch(0, akeyframe.length, (p_232315_) -> {
                        return f <= akeyframe[p_232315_].timestamp();
                    }) - 1);
                    int j = Math.min(akeyframe.length - 1, i + 1);
                    Keyframe keyframe = akeyframe[i];
                    Keyframe keyframe1 = akeyframe[j];
                    float f1 = f - keyframe.timestamp();
                    float f2;
                    if (j != i) {
                        f2 = Mth.clamp(f1 / (keyframe1.timestamp() - keyframe.timestamp()), 0.0F, 1.0F);
                    } else {
                        f2 = 0.0F;
                    }

                    keyframe1.interpolation().apply(animationCache, f2, akeyframe, i, j, interpolationFactor);
                    p_288241_.target().apply(p_232330_, animationCache);
                });
            });
        }

    }

    private static float getElapsedSeconds(AnimationDefinition animation, long time) {
        float elapsedTime = time / 1000.0F; // Convert to seconds
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
