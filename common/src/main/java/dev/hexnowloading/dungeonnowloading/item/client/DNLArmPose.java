package dev.hexnowloading.dungeonnowloading.item.client;

public enum DNLArmPose {
    EMPTY(false, "empty"),
    SCORCHER(true, "scorcher"),
    WISPLIGHT_ROD(false, "wisplight_rod");

    private final boolean twoHanded;
    private final String id;

    DNLArmPose(boolean twoHanded, String id) {
        this.twoHanded = twoHanded;
        this.id = id;
    }

    public boolean isTwoHanded() {
        return this.twoHanded;
    }

    public String getId() {
        return id;
    }

    public static DNLArmPose fromId(String id) {
        for (DNLArmPose pose : values()) {
            if (pose.id.equals(id)) {
                return pose;
            }
        }
        return EMPTY; // Default
    }
}
