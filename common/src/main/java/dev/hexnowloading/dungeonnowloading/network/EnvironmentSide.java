package dev.hexnowloading.dungeonnowloading.network;

public enum EnvironmentSide {
    CLIENT("Client"),
    DEDICATED_SERVER("Dedicated Server");

    private final String sideName;

    EnvironmentSide(String sideName) {
        this.sideName = sideName;
    }

    public String getSideName() {
        return sideName;
    }

    /**
     * Whether this side is the physical client.
     *
     * @return Whether this side is the physical client.
     */
    public boolean isClient() {
        return this == CLIENT;
    }

    /**
     * Whether this side is the physical server.
     *
     * @return Whether this side is the physical server.
     */
    public boolean isDedicatedServer() {
        return this == DEDICATED_SERVER;
    }
}
