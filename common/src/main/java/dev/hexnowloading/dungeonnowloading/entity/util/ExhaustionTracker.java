package dev.hexnowloading.dungeonnowloading.entity.util;

public class ExhaustionTracker {

    private float exhaustion = 0.0F;
    private float maxExhaustion;

    public ExhaustionTracker(float maxExhaustion) {
        this.maxExhaustion = maxExhaustion;
    }

    public void addExhaustion(float amount) {
        exhaustion += amount;
        exhaustion = Math.min(exhaustion, maxExhaustion); // clamp
    }

    public void reduceExhaustion(float amount) {
        exhaustion = Math.max(0.0F, exhaustion - amount); // optional if you want recovery
    }

    public void resetExhaustion() {
        exhaustion = 0.0F;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public float getMaxExhaustion() {
        return maxExhaustion;
    }

    public float getExhaustionPercent() {
        return exhaustion / maxExhaustion;
    }

    public boolean isExhausted() {
        return exhaustion >= maxExhaustion;
    }

    public void setMaxExhaustion(float maxExhaustion) {
        this.maxExhaustion = maxExhaustion;
    }
}
