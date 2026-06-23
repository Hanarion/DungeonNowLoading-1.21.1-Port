package dev.hexnowloading.dungeonnowloading.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.ResolvableProfile;

/**
 * 1.21 removed NbtUtils.writeGameProfile/readGameProfile. GameProfile NBT now goes
 * through the ResolvableProfile codec.
 */
public final class ProfileNbt {
    private ProfileNbt() {}

    public static Tag write(GameProfile profile) {
        return ResolvableProfile.CODEC
                .encodeStart(NbtOps.INSTANCE, new ResolvableProfile(profile))
                .result()
                .orElseGet(CompoundTag::new);
    }

    public static GameProfile read(CompoundTag tag) {
        return ResolvableProfile.CODEC
                .parse(NbtOps.INSTANCE, tag)
                .result()
                .map(ResolvableProfile::gameProfile)
                .orElse(null);
    }
}
