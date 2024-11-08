package dev.hexnowloading.dungeonnowloading.block.property;

import net.minecraft.util.StringRepresentable;

public enum BallistaGolemStatueStates implements StringRepresentable {
    TOP_N("top_n"),
    TOP_NE("top_ne"),
    TOP_E("top_e"),
    TOP_SE("top_se"),
    TOP_S("top_s"),
    TOP_SW("top_sw"),
    TOP_W("top_w"),
    TOP_NW("top_nw"),
    TOP_C("top_c"),
    MIDDLE_N("middle_n"),
    MIDDLE_NE("middle_ne"),
    MIDDLE_E("middle_e"),
    MIDDLE_SE("middle_se"),
    MIDDLE_S("middle_s"),
    MIDDLE_SW("middle_sw"),
    MIDDLE_W("middle_w"),
    MIDDLE_NW("middle_nw"),
    MIDDLE_C("middle_c"),
    BOTTOM_N("bottom_n"),
    BOTTOM_NE("bottom_ne"),
    BOTTOM_E("bottom_e"),
    BOTTOM_SE("bottom_se"),
    BOTTOM_S("bottom_s"),
    BOTTOM_SW("bottom_sw"),
    BOTTOM_W("bottom_w"),
    BOTTOM_NW("bottom_nw");

    private final String name;

    private BallistaGolemStatueStates(String string) { this.name = string; }

    public String toString() { return this.name; }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
