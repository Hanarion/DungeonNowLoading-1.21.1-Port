package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BrokenRecordItem extends DNLRecordItem {
    public BrokenRecordItem(int analogOutput, SoundEvent soundEvent, Properties properties, int lengthInSeconds) {
        super(analogOutput, soundEvent, properties, lengthInSeconds);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
        components.add(Component.translatable(this.getDescriptionId() + ".desc2").withStyle(ChatFormatting.DARK_GRAY));

    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.getDescriptionId() + ".desc");
    }
}
