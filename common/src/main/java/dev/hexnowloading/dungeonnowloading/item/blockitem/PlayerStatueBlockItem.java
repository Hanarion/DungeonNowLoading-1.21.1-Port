package dev.hexnowloading.dungeonnowloading.item.blockitem;

import dev.hexnowloading.dungeonnowloading.util.StackNbt;
import dev.hexnowloading.dungeonnowloading.util.ProfileNbt;
import com.mojang.authlib.GameProfile;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PlayerStatueBlockItem extends BlockItem {

    public PlayerStatueBlockItem() {
        super(DNLBlocks.PLAYER_STATUE.get(), new Properties().rarity(Rarity.RARE));
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null) return null;

        int rot16 = (Mth.floor((ctx.getRotation() * 16.0F / 360.0F) + 0.5F)) & 15;
        rot16 = (rot16 + 8) & 15;

        if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
            state = state.setValue(BlockStateProperties.ROTATION_16, rot16);
        }
        return state;
    }

    @Override
    public Component getName(ItemStack stack) {
        String owner = readOwnerName(stack);
        if (owner != null && !owner.isEmpty()) {
            // Uses "block.<modid>.<statue>.named" → "%s's Stone Statue"
            return Component.translatable(getDescriptionId() + ".named", Component.literal(owner));
        }
        return super.getName(stack); // "Stone Statue"
    }

    private static String readOwnerName(ItemStack stack) {
        CompoundTag tag = StackNbt.getTag(stack);
        if (tag == null) return null;

        // Accept either "SkullOwner" (string or compound) or "Owner" (compound)
        if (tag.contains("SkullOwner", 8)) { // string
            return tag.getString("SkullOwner");
        }
        if (tag.contains("Owner", 10)) { // compound
            GameProfile gp = ProfileNbt.read(tag.getCompound("Owner"));
            if (gp != null && gp.getName() != null) return gp.getName();
        }
        if (tag.contains("SkullOwner", 10)) { // compound
            GameProfile gp = ProfileNbt.read(tag.getCompound("SkullOwner"));
            if (gp != null && gp.getName() != null) return gp.getName();
        }
        return null;
    }
}
