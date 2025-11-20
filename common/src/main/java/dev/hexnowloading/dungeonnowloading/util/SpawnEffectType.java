package dev.hexnowloading.dungeonnowloading.util;

public enum SpawnEffectType {
    NONE("none"),
    POOF_FIRE("poof_fire");

    private final String id;

    SpawnEffectType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static SpawnEffectType fromString(String s) {
        if (s == null) return NONE;
        for (SpawnEffectType type : values()) {
            if (type.id.equalsIgnoreCase(s)) {
                return type;
            }
        }
        return NONE;
    }
}