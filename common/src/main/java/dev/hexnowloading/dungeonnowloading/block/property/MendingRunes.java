package dev.hexnowloading.dungeonnowloading.block.property;

import net.minecraft.util.StringRepresentable;

public enum MendingRunes implements StringRepresentable {
    M_ON("m_on"),
    E_ON("e_on"),
    N_ON("n_on"),
    D_ON("d_on"),
    I_ON("i_on"),
    G_ON("g_on"),
    M_OFF("m_off"),
    E_OFF("e_off"),
    N_OFF("n_off"),
    D_OFF("d_off"),
    I_OFF("i_off"),
    G_OFF("g_off");

    private final String name;

    private MendingRunes(String string) { this.name = string; }

    public String toString() { return this.name; }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
