package dev.hexnowloading.dungeonnowloading.entity.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class AnimationChainer<T extends Enum<T>> {
    private static class AnimationStep<T> {
        final T animation;
        final int durationTicks;
        final boolean isHanging;

        AnimationStep(T animation, float durationSeconds, boolean isHanging) {
            this.animation = animation;
            this.durationTicks = (int) (durationSeconds * 20);
            this.isHanging = isHanging;
        }

        AnimationStep(T animation, float durationSeconds) {
            this(animation, durationSeconds, false);
        }

        static <T> AnimationStep<T> hanging(T animation) {
            return new AnimationStep<>(animation, 0, true);
        }
    }

    private final Queue<AnimationStep<T>> animationQueue = new LinkedList<>();
    private AnimationStep<T> currentStep = null;
    private int ticksRemaining = 0;
    private boolean started = false;

    public void enqueue(T animation, float durationSeconds) {
        animationQueue.add(new AnimationStep<>(animation, durationSeconds));
    }

    public void enqueueHanging(T animation) {
        animationQueue.add(AnimationStep.hanging(animation));
    }

    public void tick(Consumer<T> transitionFunction) {
        if (currentStep == null && !animationQueue.isEmpty()) {
            currentStep = animationQueue.poll();
            ticksRemaining = currentStep.durationTicks;
            transitionFunction.accept(currentStep.animation);
            started = true;
        } else if (currentStep != null && !currentStep.isHanging) {
            if (--ticksRemaining <= 0) {
                currentStep = null;
            }
        }
    }

    public void reset() {
        animationQueue.clear();
        currentStep = null;
        ticksRemaining = 0;
        started = false;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return started && currentStep == null && animationQueue.isEmpty();
    }

    public boolean isEmpty() {
        return animationQueue.isEmpty();
    }
}