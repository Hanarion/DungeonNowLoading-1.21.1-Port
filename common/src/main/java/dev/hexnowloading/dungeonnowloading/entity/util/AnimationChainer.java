package dev.hexnowloading.dungeonnowloading.entity.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnimationChainer<T extends Enum<T>> {

    public static class AnimationStep<T> {
        final T animation;
        final int durationTicks;
        final boolean isLooping;
        final boolean isHanging;
        final Runnable onStart;
        final Runnable onComplete;
        final BiConsumer<T, Float> onProgress;

        private AnimationStep(
                T animation,
                float seconds,
                boolean looping,
                boolean hanging,
                Runnable onStart,
                Runnable onComplete
        ) {
            this(animation, seconds, looping, hanging, onStart, onComplete, null);
        }

        private AnimationStep(
                T animation,
                float seconds,
                boolean looping,
                boolean hanging,
                Runnable onStart,
                Runnable onComplete,
                BiConsumer<T, Float> onProgress
        ) {
            this.animation = animation;
            this.durationTicks = (int) (seconds * 20);
            this.isLooping = looping;
            this.isHanging = hanging;
            this.onStart = onStart;
            this.onComplete = onComplete;
            this.onProgress = onProgress;
        }

        // --- Factory methods ---

        public static <T> AnimationStep<T> of(T animation, float seconds) {
            return new AnimationStep<>(animation, seconds, false, false, null, null);
        }

        public static <T> AnimationStep<T> of(T animation, float seconds,
                                              Runnable onStart, Runnable onEnd) {
            return new AnimationStep<>(animation, seconds, false, false, onStart, onEnd);
        }

        public static <T> AnimationStep<T> of(T animation, float seconds,
                                              BiConsumer<T, Float> onProgress) {
            return new AnimationStep<>(animation, seconds, false, false, null, null, onProgress);
        }

        public static <T> AnimationStep<T> of(T animation, float seconds,
                                              Runnable onStart, Runnable onEnd,
                                              BiConsumer<T, Float> onProgress) {
            return new AnimationStep<>(animation, seconds, false, false, onStart, onEnd, onProgress);
        }

        public static <T> AnimationStep<T> looping(T animation, float seconds) {
            return new AnimationStep<>(animation, seconds, true, false, null, null);
        }

        public static <T> AnimationStep<T> looping(T animation, float seconds,
                                                   Runnable onStart, Runnable onEnd) {
            return new AnimationStep<>(animation, seconds, true, false, onStart, onEnd);
        }

        public static <T> AnimationStep<T> looping(T animation, float seconds,
                                                   BiConsumer<T, Float> onProgress) {
            return new AnimationStep<>(animation, seconds, true, false, null, null, onProgress);
        }

        public static <T> AnimationStep<T> looping(T animation, float seconds,
                                                   Runnable onStart, Runnable onEnd,
                                                   BiConsumer<T, Float> onProgress) {
            return new AnimationStep<>(animation, seconds, true, false, onStart, onEnd, onProgress);
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
    private int ticksElapsed = 0;
    private boolean started = false;

    public void enqueue(AnimationStep<T> step) {
        animationQueue.add(step);
    }

    public void tick(Consumer<T> transitionFunction) {
        // Start first step if needed
        if (currentStep == null && !animationQueue.isEmpty()) {
            currentStep = animationQueue.poll();
            ticksRemaining = currentStep.durationTicks;
            ticksElapsed = 0;

            transitionFunction.accept(currentStep.animation);
            if (currentStep.onStart != null) currentStep.onStart.run();
            started = true;

            // Optional: progress at 0.0 when starting
            if (currentStep.onProgress != null && currentStep.durationTicks > 0) {
                currentStep.onProgress.accept(currentStep.animation, 0.0f);
            }
            return;
        }

        if (currentStep == null || currentStep.isHanging) {
            // Nothing active or we're in a hanging step (no time-based progress)
            return;
        }

        if (currentStep.durationTicks <= 0) {
            // Degenerate case – no duration, just complete immediately
            if (currentStep.onComplete != null) currentStep.onComplete.run();
            if (currentStep.isLooping) {
                // treat as 1 tick loop if you really want, or just bail
                ticksRemaining = 0;
                ticksElapsed = 0;
            } else {
                currentStep = null;
            }
            return;
        }

        // Advance time
        ticksElapsed++;

        // Compute progress 0..1 and invoke progress callback
        if (currentStep.onProgress != null) {
            float progress = Math.min(1.0f, (float) ticksElapsed / currentStep.durationTicks);
            currentStep.onProgress.accept(currentStep.animation, progress);
        }

        // Handle completion
        if (--ticksRemaining <= 0) {
            // Snapshot the step BEFORE running callbacks
            AnimationStep<T> finished = currentStep;

            if (finished.onComplete != null) finished.onComplete.run();

            // If callback reset/changed the chainer, stop safely
            if (currentStep != finished) {
                return;
            }

            if (finished.isLooping) {
                ticksRemaining = finished.durationTicks;
                ticksElapsed = 0;

                transitionFunction.accept(finished.animation);
                if (finished.onStart != null) finished.onStart.run();

                // If callback reset/changed the chainer, stop safely
                if (currentStep != finished) {
                    return;
                }

                if (finished.onProgress != null && finished.durationTicks > 0) {
                    finished.onProgress.accept(finished.animation, 0.0f);
                }
            } else {
                currentStep = null;
            }
        }
    }

    public void forceNext(Consumer<T> transitionFunction) {
        if (currentStep != null && currentStep.isLooping) {
            if (currentStep.onComplete != null) currentStep.onComplete.run();
            currentStep = null;
            ticksRemaining = 0;
            ticksElapsed = 0;
            tick(transitionFunction);
        }
    }

    public void reset() {
        animationQueue.clear();
        currentStep = null;
        ticksRemaining = 0;
        ticksElapsed = 0;
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

    /**
     * Returns the current step's normalized progress (0..1), or -1 if no step is active
     * or the step is hanging / zero-duration.
     */
    public float getCurrentProgress() {
        if (currentStep == null || currentStep.isHanging || currentStep.durationTicks <= 0) {
            return -1.0f;
        }
        return Math.min(1.0f, (float) ticksElapsed / currentStep.durationTicks);
    }
}