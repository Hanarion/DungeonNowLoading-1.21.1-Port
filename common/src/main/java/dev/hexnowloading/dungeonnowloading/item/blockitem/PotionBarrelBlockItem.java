package dev.hexnowloading.dungeonnowloading.item.blockitem;

import dev.hexnowloading.dungeonnowloading.block.entity.PotionBarrelBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class PotionBarrelBlockItem extends BlockItem {

    public PotionBarrelBlockItem() {
        super(DNLBlocks.POTION_BARREL.get(), new Properties());
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, Player player, ItemStack stack, BlockState state) {
        if (stack.hasTag() && stack.getTag().contains("BlockEntityTag")) {
            CompoundTag beTag = stack.getTag().getCompound("BlockEntityTag");
            try {
                if (world.getBlockEntity(pos) instanceof PotionBarrelBlockEntity be) {
                    if (beTag.contains("Effect", 8)) {
                        ResourceLocation id = new ResourceLocation(beTag.getString("Effect"));
                        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
                        be.setEffect(effect);
                    } else {
                        be.setEffect(null);
                    }
                    if (world != null && !world.isClientSide) {
                        world.sendBlockUpdated(pos, state, state, 3);
                    } else if (world != null && world.isClientSide) {
                        world.sendBlockUpdated(pos, state, state, 3);
                    }
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }
}
