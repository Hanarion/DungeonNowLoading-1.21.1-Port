package dev.hexnowloading.dungeonnowloading.block.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class BurnacleJuvenileAnimation {
    public static final AnimationDefinition BURNACLE_IDLE2 = AnimationDefinition.Builder.withLength(16.0F).looping()
            .addAnimation("group2", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.05F, 1.0F, 1.05F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.05F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(4.0F, KeyframeAnimations.scaleVec(0.95F, 1.0F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(6.0F, KeyframeAnimations.scaleVec(1.0F, 0.95F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(8.0F, KeyframeAnimations.scaleVec(1.05F, 1.0F, 1.05F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(10.0F, KeyframeAnimations.scaleVec(1.0F, 1.05F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(12.0F, KeyframeAnimations.scaleVec(0.95F, 1.0F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(14.0F, KeyframeAnimations.scaleVec(1.0F, 0.95F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(16.0F, KeyframeAnimations.scaleVec(1.05F, 1.0F, 1.05F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("group3", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.95F, 1.0F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.09F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(8.0F, KeyframeAnimations.scaleVec(1.05F, 1.0F, 1.05F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(12.0F, KeyframeAnimations.scaleVec(1.0F, 0.91F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(16.0F, KeyframeAnimations.scaleVec(0.95F, 1.0F, 0.95F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("group", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.05F, 0.95F, 1.05F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.6667F, KeyframeAnimations.scaleVec(0.95F, 1.0F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(5.3333F, KeyframeAnimations.scaleVec(0.95F, 1.05F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(7.9583F, KeyframeAnimations.scaleVec(1.05F, 0.95F, 1.05F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(10.625F, KeyframeAnimations.scaleVec(0.95F, 1.0F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(13.2917F, KeyframeAnimations.scaleVec(0.95F, 1.05F, 0.95F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(15.9167F, KeyframeAnimations.scaleVec(1.05F, 0.95F, 1.05F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition BURNACLE_SPRAY2 = AnimationDefinition.Builder.withLength(3.0F)
            .addAnimation("group2", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.1667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6667F, KeyframeAnimations.scaleVec(1.1025F, 1.4F, 1.116F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.875F, KeyframeAnimations.scaleVec(1.278F, 0.715F, 1.3095F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0417F, KeyframeAnimations.scaleVec(1.161F, 0.9F, 1.188F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.1667F, KeyframeAnimations.scaleVec(1.107F, 0.875F, 1.134F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.scaleVec(1.062F, 0.9F, 1.062F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4583F, KeyframeAnimations.scaleVec(1.008F, 0.875F, 1.017F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.625F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.1667F, KeyframeAnimations.scaleVec(1.0F, 1.06F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.6667F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("group3", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.225F, 1.3F, 1.24F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.scaleVec(1.32F, 0.815F, 1.355F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.875F, KeyframeAnimations.scaleVec(1.29F, 0.9F, 1.32F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.23F, 0.875F, 1.26F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.125F, KeyframeAnimations.scaleVec(1.18F, 0.9F, 1.18F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.scaleVec(1.12F, 0.875F, 1.13F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4583F, KeyframeAnimations.scaleVec(1.07F, 0.9F, 1.06F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.06F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("group", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9167F, KeyframeAnimations.scaleVec(1.1025F, 1.5F, 1.116F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.scaleVec(1.278F, 0.815F, 1.3095F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4583F, KeyframeAnimations.scaleVec(1.161F, 0.9F, 1.188F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.6667F, KeyframeAnimations.scaleVec(1.107F, 0.875F, 1.134F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.875F, KeyframeAnimations.scaleVec(1.062F, 0.9F, 1.062F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0417F, KeyframeAnimations.scaleVec(1.008F, 0.875F, 1.017F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.2083F, KeyframeAnimations.scaleVec(1.0F, 0.9F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.5833F, KeyframeAnimations.scaleVec(1.0F, 1.06F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.9583F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();
}
