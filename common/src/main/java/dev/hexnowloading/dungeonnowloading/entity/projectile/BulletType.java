package dev.hexnowloading.dungeonnowloading.entity.projectile;

public enum BulletType {
    GOLD(
            7.0F,     // damage (higher than arrow)
            4.0F,     // velocity
            0.0F,     // inaccuracy
            0.005F,   // gravity (low drop)
            80        // lifetime (ticks) ~4s
    ),
    IRON(
            4.0F,     // damage (slightly less than arrow)
            3.0F,     // velocity
            0.02F,    // inaccuracy
            0.03F,    // gravity (more drop)
            60        // lifetime ~3s
    ),
    COPPER(
            4.0F,     // same damage as iron
            1.0F,     // velocity
            0.03F,    // inaccuracy
            0.5F,    // gravity (even more drop)
            20        // **shortest** lifetime ~1.5s
    );

    private final float damage;
    private final float velocity;
    private final float inaccuracy;
    private final float gravity;
    private final int maxLifeTicks;

    BulletType(float damage, float velocity, float inaccuracy, float gravity, int maxLifeTicks) {
        this.damage = damage;
        this.velocity = velocity;
        this.inaccuracy = inaccuracy;
        this.gravity = gravity;
        this.maxLifeTicks = maxLifeTicks;
    }

    public float getDamage() {
        return damage;
    }

    public float getVelocity() {
        return velocity;
    }

    public float getInaccuracy() {
        return inaccuracy;
    }

    public float getGravity() {
        return gravity;
    }

    public int getMaxLifeTicks() {
        return maxLifeTicks;
    }

    public static BulletType byName(String name) {
        try {
            return BulletType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return IRON; // fallback
        }
    }
}
