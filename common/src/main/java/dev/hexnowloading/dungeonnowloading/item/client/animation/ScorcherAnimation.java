package dev.hexnowloading.dungeonnowloading.item.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class ScorcherAnimation {
    public static final AnimationDefinition BASE = AnimationDefinition.Builder.withLength(15.6667F)
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(11.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(7.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(47.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(32.0F, 3.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(40.0F, -1.0F, -4.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition SCORCHER_ACTIVATE = AnimationDefinition.Builder.withLength(1.5F)
            .addAnimation("upperjaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-9.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(-20.2F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-30.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-41.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.375F, KeyframeAnimations.degreeVec(-41.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-38.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-35.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-33.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.625F, KeyframeAnimations.degreeVec(-35.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-40.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-44.72F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-42.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.875F, KeyframeAnimations.degreeVec(-44.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-44.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-44.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-55.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5F, KeyframeAnimations.degreeVec(-44.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("lowerjaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0833F, KeyframeAnimations.degreeVec(2.16F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(11.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(25.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(41.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.375F, KeyframeAnimations.degreeVec(41.8F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4583F, KeyframeAnimations.degreeVec(35.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5417F, KeyframeAnimations.degreeVec(33.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.625F, KeyframeAnimations.degreeVec(36.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7083F, KeyframeAnimations.degreeVec(44.72F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7917F, KeyframeAnimations.degreeVec(42.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.875F, KeyframeAnimations.degreeVec(44.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(44.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9583F, KeyframeAnimations.degreeVec(44.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(57.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5F, KeyframeAnimations.degreeVec(46.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(11.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(7.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(47.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(32.0F, 3.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(40.0F, -1.0F, -4.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("flameexit", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0417F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.01F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.09F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.26F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.51F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.78F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.65F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.6F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6667F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.64F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.76F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7917F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.78F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.875F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.75F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.8F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.79F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0417F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.8F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.75F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("scorcher", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.5F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.5F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4167F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("hilt", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.5F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.4167F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.25F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition SCORCHER_SHOOT = AnimationDefinition.Builder.withLength(6.0F)
            .addAnimation("scorcher", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-0.12F, -0.26F, -0.2F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.0333F, KeyframeAnimations.degreeVec(-0.05F, -0.09F, -0.13F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.0667F, KeyframeAnimations.degreeVec(-0.25F, -0.11F, -0.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1F, KeyframeAnimations.degreeVec(-0.09F, 0.01F, -0.24F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1333F, KeyframeAnimations.degreeVec(-0.26F, -0.2F, -0.23F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-0.07F, -0.15F, -0.22F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-0.13F, -0.17F, -0.03F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2333F, KeyframeAnimations.degreeVec(-0.22F, -0.07F, 0.07F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2667F, KeyframeAnimations.degreeVec(-0.02F, -0.02F, -0.22F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(-0.18F, -0.04F, -0.07F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-0.2F, -0.14F, -0.04F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3667F, KeyframeAnimations.degreeVec(-0.17F, 0.01F, 0.14F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(-0.19F, 0.0F, 0.1F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4333F, KeyframeAnimations.degreeVec(0.01F, -0.15F, 0.1F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4667F, KeyframeAnimations.degreeVec(0.02F, -0.08F, 0.04F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.07F, -0.21F, 0.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5333F, KeyframeAnimations.degreeVec(0.02F, 0.16F, -0.07F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5667F, KeyframeAnimations.degreeVec(-0.26F, -0.07F, 0.01F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6F, KeyframeAnimations.degreeVec(-0.29F, 0.04F, -0.24F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6333F, KeyframeAnimations.degreeVec(-0.18F, -0.11F, 0.14F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0.2F, -0.22F, 0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(-0.22F, 0.1F, 0.23F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7333F, KeyframeAnimations.degreeVec(-0.1F, 0.16F, 0.28F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7667F, KeyframeAnimations.degreeVec(0.0F, 0.01F, 0.06F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8F, KeyframeAnimations.degreeVec(0.05F, -0.08F, 0.14F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(0.24F, 0.19F, 0.16F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8667F, KeyframeAnimations.degreeVec(-0.27F, -0.22F, 0.05F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(-0.09F, 0.07F, 0.35F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9333F, KeyframeAnimations.degreeVec(-0.03F, -0.02F, 0.23F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9667F, KeyframeAnimations.degreeVec(0.07F, 0.31F, 0.35F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.06F, -0.07F, 0.08F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0333F, KeyframeAnimations.degreeVec(0.06F, 0.01F, 0.38F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0667F, KeyframeAnimations.degreeVec(0.01F, 0.09F, -0.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.04F, -0.26F, 0.26F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1333F, KeyframeAnimations.degreeVec(0.15F, 0.11F, 0.33F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1667F, KeyframeAnimations.degreeVec(-0.22F, 0.15F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2F, KeyframeAnimations.degreeVec(-0.19F, 0.37F, 0.46F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2333F, KeyframeAnimations.degreeVec(-0.18F, 0.45F, -0.13F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2667F, KeyframeAnimations.degreeVec(0.46F, 0.05F, 0.23F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3F, KeyframeAnimations.degreeVec(-0.08F, 0.25F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.07F, 0.24F, 0.53F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3667F, KeyframeAnimations.degreeVec(0.03F, 0.22F, 0.44F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.39F, -0.2F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.4333F, KeyframeAnimations.degreeVec(-0.18F, 0.34F, -0.27F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.4667F, KeyframeAnimations.degreeVec(0.21F, 0.42F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.degreeVec(-0.09F, -0.15F, -0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5333F, KeyframeAnimations.degreeVec(0.45F, -0.29F, 0.17F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5667F, KeyframeAnimations.degreeVec(-0.07F, 0.07F, 0.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6F, KeyframeAnimations.degreeVec(0.11F, 0.33F, -0.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6333F, KeyframeAnimations.degreeVec(-0.16F, -0.28F, 0.2F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.degreeVec(0.23F, 0.44F, -0.27F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.7F, KeyframeAnimations.degreeVec(0.0F, 0.5F, 0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.7333F, KeyframeAnimations.degreeVec(-0.18F, 0.44F, 0.45F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.7667F, KeyframeAnimations.degreeVec(0.69F, -0.17F, 0.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.8F, KeyframeAnimations.degreeVec(-0.05F, 0.09F, 0.59F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.8333F, KeyframeAnimations.degreeVec(-0.19F, 0.07F, 0.58F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.8667F, KeyframeAnimations.degreeVec(0.4F, 0.28F, -0.21F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.9F, KeyframeAnimations.degreeVec(-0.09F, 0.44F, -0.27F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.9333F, KeyframeAnimations.degreeVec(0.52F, 0.36F, 0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.9667F, KeyframeAnimations.degreeVec(0.76F, 0.73F, 0.34F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.56F, 0.09F, 0.46F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0333F, KeyframeAnimations.degreeVec(0.24F, -0.08F, -0.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0667F, KeyframeAnimations.degreeVec(0.49F, 0.29F, 0.64F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.1F, KeyframeAnimations.degreeVec(0.1F, 0.02F, 0.28F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.1333F, KeyframeAnimations.degreeVec(0.84F, 0.72F, 0.78F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.1667F, KeyframeAnimations.degreeVec(-0.26F, -0.25F, -0.28F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2F, KeyframeAnimations.degreeVec(0.01F, -0.25F, -0.05F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2333F, KeyframeAnimations.degreeVec(-0.07F, 0.76F, 0.81F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2667F, KeyframeAnimations.degreeVec(-0.02F, 0.03F, -0.08F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3F, KeyframeAnimations.degreeVec(0.89F, 0.67F, 0.14F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3333F, KeyframeAnimations.degreeVec(0.02F, -0.09F, 0.73F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3667F, KeyframeAnimations.degreeVec(0.06F, 0.0F, 0.03F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.69F, 0.89F, 0.86F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4333F, KeyframeAnimations.degreeVec(0.49F, 0.63F, -0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4667F, KeyframeAnimations.degreeVec(0.84F, 0.82F, 0.18F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.degreeVec(0.35F, 0.29F, -0.21F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5333F, KeyframeAnimations.degreeVec(-0.14F, -0.13F, 0.69F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5667F, KeyframeAnimations.degreeVec(1.01F, 0.38F, -0.11F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6F, KeyframeAnimations.degreeVec(0.56F, 0.88F, -0.17F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6333F, KeyframeAnimations.degreeVec(0.14F, -0.25F, 0.98F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6667F, KeyframeAnimations.degreeVec(-0.07F, -0.1F, 0.68F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7F, KeyframeAnimations.degreeVec(0.19F, -0.04F, 0.26F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7333F, KeyframeAnimations.degreeVec(0.84F, -0.3F, -0.13F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7667F, KeyframeAnimations.degreeVec(0.44F, -0.13F, 1.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8F, KeyframeAnimations.degreeVec(0.05F, 1.04F, 0.01F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8333F, KeyframeAnimations.degreeVec(0.16F, 0.48F, 0.94F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8667F, KeyframeAnimations.degreeVec(0.33F, 0.39F, -0.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9F, KeyframeAnimations.degreeVec(0.88F, 0.66F, 0.06F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9333F, KeyframeAnimations.degreeVec(-0.14F, -0.23F, 0.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9667F, KeyframeAnimations.degreeVec(0.82F, 0.5F, 1.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0F, KeyframeAnimations.degreeVec(1.15F, 0.27F, 0.14F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0333F, KeyframeAnimations.degreeVec(0.27F, 0.55F, 0.77F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0667F, KeyframeAnimations.degreeVec(-0.26F, 1.16F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.1F, KeyframeAnimations.degreeVec(-0.13F, 0.38F, 0.89F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.1333F, KeyframeAnimations.degreeVec(0.13F, 0.79F, 0.74F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.1667F, KeyframeAnimations.degreeVec(1.16F, 0.68F, 0.36F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.2F, KeyframeAnimations.degreeVec(0.67F, 0.83F, 0.47F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.2333F, KeyframeAnimations.degreeVec(0.54F, 0.74F, 0.08F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.2667F, KeyframeAnimations.degreeVec(0.9F, -0.1F, -0.07F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.3F, KeyframeAnimations.degreeVec(0.32F, 0.54F, -0.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.3333F, KeyframeAnimations.degreeVec(0.98F, 0.46F, 1.16F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.3667F, KeyframeAnimations.degreeVec(-0.24F, 0.23F, -0.28F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.4F, KeyframeAnimations.degreeVec(0.7F, 0.26F, 1.05F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.4333F, KeyframeAnimations.degreeVec(1.19F, -0.03F, 0.02F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.4667F, KeyframeAnimations.degreeVec(0.5F, 0.68F, 1.24F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.5F, KeyframeAnimations.degreeVec(-0.25F, 0.07F, 0.65F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.5333F, KeyframeAnimations.degreeVec(0.39F, 0.62F, 1.36F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.5667F, KeyframeAnimations.degreeVec(1.27F, 0.34F, 1.33F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.6F, KeyframeAnimations.degreeVec(1.29F, 1.13F, 0.38F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.6333F, KeyframeAnimations.degreeVec(1.43F, 1.39F, 1.21F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.6667F, KeyframeAnimations.degreeVec(1.13F, 0.59F, 0.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.7F, KeyframeAnimations.degreeVec(0.79F, 0.89F, 0.36F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.7333F, KeyframeAnimations.degreeVec(1.06F, 0.16F, 0.58F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.7667F, KeyframeAnimations.degreeVec(0.58F, -0.19F, 0.17F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.8F, KeyframeAnimations.degreeVec(0.86F, 0.09F, 0.44F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.8333F, KeyframeAnimations.degreeVec(0.27F, 1.41F, 0.31F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.8667F, KeyframeAnimations.degreeVec(0.5F, -0.18F, 0.03F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.9F, KeyframeAnimations.degreeVec(0.58F, -0.04F, -0.04F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.9333F, KeyframeAnimations.degreeVec(1.2F, 1.25F, 1.47F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.9667F, KeyframeAnimations.degreeVec(0.62F, 0.77F, 0.91F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.02F, 0.27F, 1.54F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.0333F, KeyframeAnimations.degreeVec(1.56F, 1.3F, 0.31F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.0667F, KeyframeAnimations.degreeVec(-0.09F, 0.89F, 0.6F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.1F, KeyframeAnimations.degreeVec(0.52F, 0.28F, 0.99F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.1333F, KeyframeAnimations.degreeVec(1.61F, 0.5F, 1.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.1667F, KeyframeAnimations.degreeVec(-0.13F, -0.29F, -0.17F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.2F, KeyframeAnimations.degreeVec(0.51F, 1.68F, 0.24F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.2333F, KeyframeAnimations.degreeVec(-0.03F, -0.12F, 0.45F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.2667F, KeyframeAnimations.degreeVec(1.13F, 1.36F, 0.06F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.3F, KeyframeAnimations.degreeVec(0.62F, 0.5F, 0.94F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.51F, 0.62F, 0.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.3667F, KeyframeAnimations.degreeVec(1.11F, 0.64F, 0.34F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.4F, KeyframeAnimations.degreeVec(-0.23F, 1.16F, 1.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.4333F, KeyframeAnimations.degreeVec(-0.04F, 0.93F, 0.98F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.4667F, KeyframeAnimations.degreeVec(0.11F, 0.99F, 1.75F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.5F, KeyframeAnimations.degreeVec(1.78F, 0.27F, 1.04F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.5333F, KeyframeAnimations.degreeVec(-0.21F, 0.62F, 0.02F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.5667F, KeyframeAnimations.degreeVec(0.59F, 1.11F, 0.78F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.6F, KeyframeAnimations.degreeVec(0.22F, 1.35F, 0.43F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.6333F, KeyframeAnimations.degreeVec(1.51F, 0.98F, 0.54F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.6667F, KeyframeAnimations.degreeVec(1.01F, 1.06F, 0.02F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.7F, KeyframeAnimations.degreeVec(-0.12F, 1.31F, -0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.7333F, KeyframeAnimations.degreeVec(1.76F, 1.59F, 0.62F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.7667F, KeyframeAnimations.degreeVec(0.92F, 1.14F, 1.35F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.8F, KeyframeAnimations.degreeVec(0.04F, 0.18F, 0.15F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.8333F, KeyframeAnimations.degreeVec(0.25F, 1.71F, 0.42F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.8667F, KeyframeAnimations.degreeVec(-0.18F, 0.58F, 0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.9F, KeyframeAnimations.degreeVec(1.36F, 1.41F, 0.26F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.9333F, KeyframeAnimations.degreeVec(1.89F, 1.92F, 0.8F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(4.9667F, KeyframeAnimations.degreeVec(-0.12F, 0.19F, 1.78F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.0F, KeyframeAnimations.degreeVec(1.05F, -0.09F, -0.23F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.0333F, KeyframeAnimations.degreeVec(1.46F, 1.72F, 1.44F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.0667F, KeyframeAnimations.degreeVec(0.93F, 0.36F, 1.98F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.1F, KeyframeAnimations.degreeVec(-0.1F, 0.28F, 1.65F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.1333F, KeyframeAnimations.degreeVec(-0.22F, 0.05F, 0.76F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.1667F, KeyframeAnimations.degreeVec(0.42F, 1.74F, 0.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.2F, KeyframeAnimations.degreeVec(0.72F, 0.89F, -0.26F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.2333F, KeyframeAnimations.degreeVec(1.57F, 1.17F, 1.2F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.2667F, KeyframeAnimations.degreeVec(-0.15F, 0.77F, 0.24F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.3F, KeyframeAnimations.degreeVec(1.75F, 2.11F, -0.22F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.3333F, KeyframeAnimations.degreeVec(0.82F, 0.43F, 1.23F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.3667F, KeyframeAnimations.degreeVec(1.66F, 0.6F, 1.46F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.4F, KeyframeAnimations.degreeVec(2.14F, 1.61F, 1.91F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.4333F, KeyframeAnimations.degreeVec(1.13F, 1.17F, 1.1F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.4667F, KeyframeAnimations.degreeVec(1.16F, 0.09F, 1.92F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.5F, KeyframeAnimations.degreeVec(1.32F, 1.97F, 1.52F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.5333F, KeyframeAnimations.degreeVec(1.1F, 0.8F, 0.78F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.5667F, KeyframeAnimations.degreeVec(1.43F, 1.49F, 0.71F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.6F, KeyframeAnimations.degreeVec(0.72F, 0.81F, 1.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.6333F, KeyframeAnimations.degreeVec(1.7F, 1.67F, 0.26F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.6667F, KeyframeAnimations.degreeVec(1.75F, 1.15F, 0.53F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.7F, KeyframeAnimations.degreeVec(2.21F, 0.3F, 0.6F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.7333F, KeyframeAnimations.degreeVec(1.37F, 1.06F, 0.55F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.7667F, KeyframeAnimations.degreeVec(0.82F, 0.35F, 2.14F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.8F, KeyframeAnimations.degreeVec(1.0F, 2.0F, 0.96F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.8333F, KeyframeAnimations.degreeVec(0.25F, 0.83F, 1.93F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.8667F, KeyframeAnimations.degreeVec(1.67F, 0.05F, 0.67F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.9F, KeyframeAnimations.degreeVec(1.24F, 1.73F, 1.86F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.9333F, KeyframeAnimations.degreeVec(0.33F, 1.82F, 1.92F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(5.9667F, KeyframeAnimations.degreeVec(2.29F, 0.72F, -0.13F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(6.0F, KeyframeAnimations.degreeVec(0.09F, 0.61F, 0.85F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("upperjaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("lowerjaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(11.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(7.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(47.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(32.0F, 3.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(40.0F, -1.0F, -4.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("flameexit", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.8F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition SCORCHER_STOP = AnimationDefinition.Builder.withLength(3.0F)
            .addAnimation("scorcher", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.93F, 0.36F, 1.98F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("scorcher", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.15F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("upperjaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.125F, KeyframeAnimations.degreeVec(-48.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-43.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-33.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-19.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-3.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-3.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-9.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-11.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(-8.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-3.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-1.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0417F, KeyframeAnimations.degreeVec(-0.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0833F, KeyframeAnimations.degreeVec(-0.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.125F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("lowerjaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0417F, KeyframeAnimations.degreeVec(52.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(35.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(25.05F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.375F, KeyframeAnimations.degreeVec(14.9F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(3.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4583F, KeyframeAnimations.degreeVec(3.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(7.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5417F, KeyframeAnimations.degreeVec(9.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.625F, KeyframeAnimations.degreeVec(11.44F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7083F, KeyframeAnimations.degreeVec(9.35F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(5.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7917F, KeyframeAnimations.degreeVec(0.3F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.875F, KeyframeAnimations.degreeVec(3.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9583F, KeyframeAnimations.degreeVec(1.22F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0417F, KeyframeAnimations.degreeVec(0.81F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0833F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.18F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.46F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 12.54F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 14.59F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 16.41F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 18.04F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5833F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 19.49F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 20.8F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 21.97F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 23.01F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 23.92F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 24.71F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 25.39F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.875F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 25.93F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 26.35F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 26.61F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 26.7F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(46.0F, 2.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2083F, KeyframeAnimations.posVec(46.0F, 2.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.25F, KeyframeAnimations.posVec(45.95F, 2.03F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2917F, KeyframeAnimations.posVec(45.85F, 2.09F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3333F, KeyframeAnimations.posVec(45.72F, 2.17F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.375F, KeyframeAnimations.posVec(45.59F, 2.26F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4167F, KeyframeAnimations.posVec(45.45F, 2.36F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4583F, KeyframeAnimations.posVec(45.32F, 2.47F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.posVec(45.18F, 2.57F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5417F, KeyframeAnimations.posVec(45.06F, 2.68F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5833F, KeyframeAnimations.posVec(44.94F, 2.8F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.625F, KeyframeAnimations.posVec(44.82F, 2.91F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6667F, KeyframeAnimations.posVec(44.71F, 3.03F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7083F, KeyframeAnimations.posVec(44.6F, 3.15F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.75F, KeyframeAnimations.posVec(44.5F, 3.27F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7917F, KeyframeAnimations.posVec(44.41F, 3.39F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8333F, KeyframeAnimations.posVec(44.32F, 3.51F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.875F, KeyframeAnimations.posVec(44.23F, 3.63F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9167F, KeyframeAnimations.posVec(44.15F, 3.75F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9583F, KeyframeAnimations.posVec(44.07F, 3.88F, -3.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0F, KeyframeAnimations.posVec(44.0F, 4.0F, -3.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffsmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.scaleVec(-0.0041F, -0.0041F, -0.0041F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0833F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.125F, KeyframeAnimations.scaleVec(0.1014F, 0.1014F, 0.1014F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.1667F, KeyframeAnimations.scaleVec(0.1782F, 0.1782F, 0.1782F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2083F, KeyframeAnimations.scaleVec(0.2394F, 0.2394F, 0.2394F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.25F, KeyframeAnimations.scaleVec(0.2894F, 0.2894F, 0.2894F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2917F, KeyframeAnimations.scaleVec(0.3311F, 0.3311F, 0.3311F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3333F, KeyframeAnimations.scaleVec(0.3662F, 0.3662F, 0.3662F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.375F, KeyframeAnimations.scaleVec(0.3959F, 0.3959F, 0.3959F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4167F, KeyframeAnimations.scaleVec(0.4212F, 0.4212F, 0.4212F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4583F, KeyframeAnimations.scaleVec(0.4428F, 0.4428F, 0.4428F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.scaleVec(0.4613F, 0.4613F, 0.4613F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5417F, KeyframeAnimations.scaleVec(0.4771F, 0.4771F, 0.4771F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5833F, KeyframeAnimations.scaleVec(0.4906F, 0.4906F, 0.4906F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.625F, KeyframeAnimations.scaleVec(0.5022F, 0.5022F, 0.5022F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6667F, KeyframeAnimations.scaleVec(0.5122F, 0.5122F, 0.5122F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7083F, KeyframeAnimations.scaleVec(0.5209F, 0.5209F, 0.5209F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.75F, KeyframeAnimations.scaleVec(0.5286F, 0.5286F, 0.5286F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7917F, KeyframeAnimations.scaleVec(0.5357F, 0.5357F, 0.5357F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8333F, KeyframeAnimations.scaleVec(0.5426F, 0.5426F, 0.5426F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.875F, KeyframeAnimations.scaleVec(0.55F, 0.55F, 0.55F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 58.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 57.03F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.125F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 55.57F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 54.13F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 52.71F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 51.31F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 49.94F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 48.59F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 47.27F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.7083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 45.99F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.7917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 44.76F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.875F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 43.57F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 42.44F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 41.37F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.125F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 40.37F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 39.46F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 38.62F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 37.88F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 37.22F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 36.65F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 36.15F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.71F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.7917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.32F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.875F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 34.98F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 34.65F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 34.5F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(32.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.0833F, KeyframeAnimations.posVec(32.36F, 2.0F, -1.02F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(32.76F, 2.0F, -1.04F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(33.18F, 2.0F, -1.07F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(33.59F, 2.0F, -1.1F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4167F, KeyframeAnimations.posVec(33.94F, 2.0F, -1.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(34.24F, 2.0F, -1.15F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.posVec(34.48F, 2.0F, -1.18F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.posVec(34.68F, 2.0F, -1.21F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.posVec(34.85F, 2.0F, -1.23F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.posVec(35.0F, 2.0F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9167F, KeyframeAnimations.posVec(35.13F, 2.04F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.posVec(35.26F, 2.1F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0833F, KeyframeAnimations.posVec(35.37F, 2.15F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1667F, KeyframeAnimations.posVec(35.48F, 2.21F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.25F, KeyframeAnimations.posVec(35.59F, 2.27F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.posVec(35.69F, 2.33F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.4167F, KeyframeAnimations.posVec(35.78F, 2.39F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.posVec(35.88F, 2.45F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5833F, KeyframeAnimations.posVec(35.97F, 2.51F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.posVec(36.06F, 2.57F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.75F, KeyframeAnimations.posVec(36.14F, 2.63F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.8333F, KeyframeAnimations.posVec(36.23F, 2.69F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.9167F, KeyframeAnimations.posVec(36.31F, 2.75F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.posVec(36.38F, 2.81F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0833F, KeyframeAnimations.posVec(36.46F, 2.87F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.1667F, KeyframeAnimations.posVec(36.53F, 2.93F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.25F, KeyframeAnimations.posVec(36.6F, 2.99F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3333F, KeyframeAnimations.posVec(36.67F, 3.05F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4167F, KeyframeAnimations.posVec(36.73F, 3.11F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.posVec(36.79F, 3.17F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5833F, KeyframeAnimations.posVec(36.84F, 3.23F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6667F, KeyframeAnimations.posVec(36.89F, 3.29F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.75F, KeyframeAnimations.posVec(36.93F, 3.35F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8333F, KeyframeAnimations.posVec(36.97F, 3.4F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9167F, KeyframeAnimations.posVec(36.99F, 3.46F, -1.25F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0F, KeyframeAnimations.posVec(37.0F, 3.5F, -1.25F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffbig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7917F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1667F, KeyframeAnimations.scaleVec(0.3F, 0.3F, 0.3F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.25F, KeyframeAnimations.scaleVec(0.3321F, 0.3321F, 0.3321F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.scaleVec(0.3576F, 0.3576F, 0.3576F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.4167F, KeyframeAnimations.scaleVec(0.3787F, 0.3787F, 0.3787F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.scaleVec(0.3965F, 0.3965F, 0.3965F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5833F, KeyframeAnimations.scaleVec(0.4119F, 0.4119F, 0.4119F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.scaleVec(0.4252F, 0.4252F, 0.4252F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.75F, KeyframeAnimations.scaleVec(0.4368F, 0.4368F, 0.4368F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.8333F, KeyframeAnimations.scaleVec(0.447F, 0.447F, 0.447F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.9167F, KeyframeAnimations.scaleVec(0.4559F, 0.4559F, 0.4559F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.scaleVec(0.4638F, 0.4638F, 0.4638F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0833F, KeyframeAnimations.scaleVec(0.4707F, 0.4707F, 0.4707F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.1667F, KeyframeAnimations.scaleVec(0.4767F, 0.4767F, 0.4767F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.25F, KeyframeAnimations.scaleVec(0.4819F, 0.4819F, 0.4819F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.3333F, KeyframeAnimations.scaleVec(0.4864F, 0.4864F, 0.4864F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.4167F, KeyframeAnimations.scaleVec(0.4901F, 0.4901F, 0.4901F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.scaleVec(0.4933F, 0.4933F, 0.4933F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5833F, KeyframeAnimations.scaleVec(0.4958F, 0.4958F, 0.4958F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.6667F, KeyframeAnimations.scaleVec(0.4978F, 0.4978F, 0.4978F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.75F, KeyframeAnimations.scaleVec(0.4991F, 0.4991F, 0.4991F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.8333F, KeyframeAnimations.scaleVec(0.5F, 0.5F, 0.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9167F, KeyframeAnimations.scaleVec(0.5002F, 0.5002F, 0.5002F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0F, KeyframeAnimations.scaleVec(0.5F, 0.5F, 0.5F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 11.4F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 23.82F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 41.99F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 51.7F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 57.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(40.0F, -3.0F, -9.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.125F, KeyframeAnimations.posVec(41.11F, -3.25F, -11.22F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(41.84F, -3.46F, -13.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.375F, KeyframeAnimations.posVec(42.42F, -3.63F, -14.94F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(42.91F, -3.75F, -16.49F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.625F, KeyframeAnimations.posVec(43.34F, -3.82F, -17.86F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.posVec(43.73F, -3.83F, -19.06F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.875F, KeyframeAnimations.posVec(44.07F, -3.77F, -20.11F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.posVec(44.39F, -3.65F, -21.03F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.125F, KeyframeAnimations.posVec(44.68F, -3.45F, -21.82F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.25F, KeyframeAnimations.posVec(44.94F, -3.18F, -22.51F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.375F, KeyframeAnimations.posVec(45.18F, -2.84F, -23.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.posVec(45.39F, -2.44F, -23.59F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.625F, KeyframeAnimations.posVec(45.59F, -1.98F, -24.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.75F, KeyframeAnimations.posVec(45.76F, -1.48F, -24.34F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.875F, KeyframeAnimations.posVec(45.91F, -0.94F, -24.62F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.posVec(46.03F, -0.38F, -24.83F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.125F, KeyframeAnimations.posVec(46.13F, 0.2F, -24.99F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.25F, KeyframeAnimations.posVec(46.21F, 0.79F, -25.09F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.375F, KeyframeAnimations.posVec(46.26F, 1.39F, -25.16F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.posVec(46.28F, 1.98F, -25.18F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.625F, KeyframeAnimations.posVec(46.26F, 2.57F, -25.17F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.75F, KeyframeAnimations.posVec(46.2F, 3.15F, -25.12F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.875F, KeyframeAnimations.posVec(46.08F, 3.72F, -25.05F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.9167F, KeyframeAnimations.posVec(46.0F, 4.0F, -25.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("smokepuffcluster", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.125F, KeyframeAnimations.scaleVec(0.4647F, 0.4647F, 0.4647F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.scaleVec(0.7755F, 0.7755F, 0.7755F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.375F, KeyframeAnimations.scaleVec(1.0081F, 1.0081F, 1.0081F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.1914F, 1.1914F, 1.1914F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.625F, KeyframeAnimations.scaleVec(1.3399F, 1.3399F, 1.3399F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.scaleVec(1.4622F, 1.4622F, 1.4622F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.875F, KeyframeAnimations.scaleVec(1.5639F, 1.5639F, 1.5639F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.649F, 1.649F, 1.649F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.125F, KeyframeAnimations.scaleVec(1.7203F, 1.7203F, 1.7203F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.25F, KeyframeAnimations.scaleVec(1.7799F, 1.7799F, 1.7799F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.375F, KeyframeAnimations.scaleVec(1.8295F, 1.8295F, 1.8295F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.scaleVec(1.8705F, 1.8705F, 1.8705F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.625F, KeyframeAnimations.scaleVec(1.904F, 1.904F, 1.904F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.75F, KeyframeAnimations.scaleVec(1.931F, 1.931F, 1.931F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.875F, KeyframeAnimations.scaleVec(1.9524F, 1.9524F, 1.9524F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.9687F, 1.9687F, 1.9687F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.125F, KeyframeAnimations.scaleVec(1.9808F, 1.9808F, 1.9808F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.25F, KeyframeAnimations.scaleVec(1.9892F, 1.9892F, 1.9892F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.375F, KeyframeAnimations.scaleVec(1.9946F, 1.9946F, 1.9946F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.scaleVec(1.9975F, 1.9975F, 1.9975F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.625F, KeyframeAnimations.scaleVec(1.9987F, 1.9987F, 1.9987F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.75F, KeyframeAnimations.scaleVec(1.9988F, 1.9988F, 1.9988F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.875F, KeyframeAnimations.scaleVec(1.9989F, 1.9989F, 1.9989F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0F, KeyframeAnimations.scaleVec(2.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firebig", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(7.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firemedium", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(11.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("firesmall", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("hilt", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.25F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("flameexit", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.6F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();
}
