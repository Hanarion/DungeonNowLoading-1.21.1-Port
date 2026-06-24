package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.client.tooltip.ClientMimiclingTooltip;
import dev.hexnowloading.dungeonnowloading.item.MimiclingTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientActivePlayersTooltip;
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
     * Add the mimicling tooltip component to vanilla's tooltip component factory.
     *
     * This mirrors vanilla's create(TooltipComponent) instead of replacing it wholesale: unknown
     * components are delegated to the platform's tooltip-component manager (NeoForge:
     * ClientTooltipComponentManager; Fabric registers its own ClientTooltipComponent types).
     * Previously this overwrote the factory and threw on any non-bundle component, which crashed
     * modpacks whenever another mod's TooltipComponent (shulker previews, etc.) was hovered.
     */
    @Overwrite
    static ClientTooltipComponent create(TooltipComponent tooltipComponent) {
        if (tooltipComponent instanceof MimiclingTooltip mimiclingTooltip) {
            return new ClientMimiclingTooltip(mimiclingTooltip);
        }
        if (tooltipComponent instanceof BundleTooltip bundleTooltip) {
            return new ClientBundleTooltip(bundleTooltip.contents());
        }
        if (tooltipComponent instanceof ClientActivePlayersTooltip.ActivePlayersTooltip activePlayersTooltip) {
            return new ClientActivePlayersTooltip(activePlayersTooltip);
        }

        // Delegate to the platform's extension point (NeoForge ClientTooltipComponentManager) so
        // other mods' TooltipComponents resolve correctly. On Fabric this class is absent and the
        // reflective lookup returns null, falling through to the same throw vanilla would make.
        ClientTooltipComponent platform = TooltipComponentLookup.createPlatformComponent(tooltipComponent);
        if (platform != null) {
            return platform;
        }

        throw new IllegalArgumentException("Unknown TooltipComponent");
    }

    /**
     * Decouples this common mixin from the NeoForge-only ClientTooltipComponentManager class so the
     * file compiles on both loaders. Reflectively delegates on NeoForge; no-ops on Fabric.
     */
    final class TooltipComponentLookup {
        private TooltipComponentLookup() {}

        static ClientTooltipComponent createPlatformComponent(TooltipComponent component) {
            try {
                Class<?> manager = Class.forName("net.neoforged.neoforge.client.gui.ClientTooltipComponentManager");
                return (ClientTooltipComponent) manager.getMethod("createClientTooltipComponent", TooltipComponent.class)
                        .invoke(null, component);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}
