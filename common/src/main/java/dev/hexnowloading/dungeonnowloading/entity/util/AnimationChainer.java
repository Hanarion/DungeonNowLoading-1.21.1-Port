package dev.hexnowloading.dungeonnowloading.entity.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class AnimationChainer<T extends Enum<T>> {

    public static class AnimationStep<T> {
        final T animation;
        final int durationTicks;
        final boolean isLooping;
        final boolean isHanging;
        final Runnable onStart;
        final Runnable onComplete;

        private AnimationStep(T animation, float seconds, boolean looping, boolean hanging,
                              Runnable onStart, Runnable onComplete) {
            this.animation = animation;
            this.durationTicks = (int) (seconds * 20);
            this.isLooping = looping;
            this.isHanging = hanging;
            this.onStart = onStart;
            this.onComplete = onComplete;
        }

        // --- Factory methods ---

        public static <T> AnimationStep<T> of(T animation, float seconds) {
            return new AnimationStep<>(animation, seconds, false, false, null, null);
        }

        public static <T> AnimationStep<T> of(T animation, float seconds, Runnable onStart, Runnable onEnd) {
            return new AnimationStep<>(animation, seconds, false, false, onStart, onEnd);
        }

        public static <T> AnimationStep<T> looping(T animation, float seconds) {
            return new AnimationStep<>(animation, seconds, true, false, null, null);
        }

        public static <T> AnimationStep<T> looping(T animation, float seconds, Runnable onStart, Runnable onEnd) {
            return new AnimationStep<>(animation, seconds, true, false, onStart, onEnd);
        }

        public static <T> AnimationStep<T> hanging(T animation) {
            return new AnimationStep<>(animation, 0, false, true, null, null);
        }

        public static <T> AnimationStep<T> hanging(T animation, Runnable onStart) {
            return new AnimationStep<>(animation, 0, false, true, onStart, null);
        }
    }

    private final Queue<AnimationStep<T>> animationQueue = new LinkedList<>();
    private AnimationStep<T> currentStep = null;
    private int ticksRemaining = 0;
    private boolean started = false;

    public void enqueue(AnimationStep<T> step) {
        animationQueue.add(step);
    }

    public void tick(Consumer<T> transitionFunction) {
        if (currentStep == null && !animationQueue.isEmpty()) {
            currentStep = animationQueue.poll();
            ticksRemaining = currentStep.durationTicks;
            System.out.println("▶ Starting animation: " + currentStep.animation + " (" + ticksRemaining + " ticks)");
            transitionFunction.accept(currentStep.animation);
            if (currentStep.onStart != null) currentStep.onStart.run();
            started = true;
        } else if (currentStep != null && !currentStep.isHanging) {
            System.out.println("⏳ Ticking animation: " + currentStep.animation + " [" + ticksRemaining + "]");
            if (--ticksRemaining <= 0) {
                System.out.println("✔ Animation done: " + currentStep.animation);
                if (currentStep.onComplete != null) currentStep.onComplete.run();
                if (currentStep.isLooping) {
                    ticksRemaining = currentStep.durationTicks;
                    transitionFunction.accept(currentStep.animation);
                    if (currentStep.onStart != null) currentStep.onStart.run();
                } else {
                    currentStep = null;
                }
            }
        }
    }

    public void forceNext(Consumer<T> transitionFunction) {
        if (currentStep != null && currentStep.isLooping) {
            if (currentStep.onComplete != null) currentStep.onComplete.run();
            currentStep = null;
            tick(transitionFunction);
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

    public boolean isLooping() {
        return currentStep != null && currentStep.isLooping;
    }

    public boolean isHanging() {
        return currentStep != null && currentStep.isHanging;
    }

    public boolean isEmpty() {
        return animationQueue.isEmpty();
    }
}