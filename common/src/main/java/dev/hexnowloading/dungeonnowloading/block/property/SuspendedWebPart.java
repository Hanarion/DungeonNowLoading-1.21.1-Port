package dev.hexnowloading.dungeonnowloading.block.property;

import net.minecraft.util.StringRepresentable;

public enum SuspendedWebPart implements StringRepresentable {
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    A("a"),
    B("b"),
    C("c");

    private final String name;

    SuspendedWebPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
