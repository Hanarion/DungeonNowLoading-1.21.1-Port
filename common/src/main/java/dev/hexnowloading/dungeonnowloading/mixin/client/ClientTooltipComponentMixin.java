package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.client.tooltip.ClientMimiclingTooltip;
import dev.hexnowloading.dungeonnowloading.item.MimiclingTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
    /**
     * @author HexNowLoading
     * Add the mimicling tooltip component to vanilla's closed tooltip component factory.
     */
    @Overwrite
    static ClientTooltipComponent create(TooltipComponent tooltipComponent) {
        if (tooltipComponent instanceof MimiclingTooltip mimiclingTooltip) {
            return new ClientMimiclingTooltip(mimiclingTooltip);
        }
        if (tooltipComponent instanceof BundleTooltip bundleTooltip) {
            return new ClientBundleTooltip(bundleTooltip);
        }

        throw new IllegalArgumentException("Unknown TooltipComponent");
    }
}
