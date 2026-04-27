package dev.hexnowloading.dungeonnowloading.client.tooltip;

import dev.hexnowloading.dungeonnowloading.item.MimiclingTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ClientMimiclingTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BUNDLE_TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
    private static final ResourceLocation MIMICLING_TEXTURE_LOCATION = new ResourceLocation("dungeonnowloading", "textures/gui/mimicling_gui.png");
    private static final int SLOT_COUNT = 5;
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int TEXTURE_SIZE = 128;
    private static final int MARGIN_Y = 4;
    private final NonNullList<ItemStack> items;
    private final int selectedSlot;
    private final int capacity;

    public ClientMimiclingTooltip(MimiclingTooltip tooltip) {
        this.items = tooltip.getItems();
        this.selectedSlot = tooltip.getSelectedSlot();
        this.capacity = tooltip.getCapacity();
    }

    @Override
    public int getHeight() {
        return SLOT_HEIGHT + 2 + MARGIN_Y;
    }

    @Override
    public int getWidth(Font font) {
        return SLOT_COUNT * SLOT_WIDTH + 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            int slotX = x + i * SLOT_WIDTH + 1;
            int slotY = y + 1;
            ItemStack stack = i < items.size() ? items.get(i) : ItemStack.EMPTY;
            blitSlot(guiGraphics, slotX, slotY, i, stack.isEmpty() && isBellyFull());
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, slotX + 1, slotY + 1, i);
                guiGraphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
            }
            if (hasStoredItem() && i == selectedSlot) {
                AbstractContainerScreen.renderSlotHighlight(guiGraphics, slotX + 1, slotY + 1, 0);
            }
        }
        drawBorder(x, y, guiGraphics);
    }

    private boolean hasStoredItem() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean isBellyFull() {
        int storedCount = 0;
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                storedCount++;
            }
        }

        return storedCount >= capacity;
    }

    private void blitSlot(GuiGraphics guiGraphics, int x, int y, int dedicatedSlot, boolean disabled) {
        int textureY = disabled ? SLOT_HEIGHT : 0;
        guiGraphics.blit(MIMICLING_TEXTURE_LOCATION, x, y, 0, (dedicatedSlot + 1) * SLOT_WIDTH, textureY, SLOT_WIDTH, SLOT_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawBorder(int x, int y, GuiGraphics guiGraphics) {
        blitBundle(guiGraphics, x, y, Texture.BORDER_CORNER_TOP);
        blitBundle(guiGraphics, x + SLOT_COUNT * SLOT_WIDTH + 1, y, Texture.BORDER_CORNER_TOP);
        blitBundle(guiGraphics, x, y + SLOT_HEIGHT, Texture.BORDER_CORNER_BOTTOM);
        blitBundle(guiGraphics, x + SLOT_COUNT * SLOT_WIDTH + 1, y + SLOT_HEIGHT, Texture.BORDER_CORNER_BOTTOM);
        blitBundle(guiGraphics, x, y + 1, Texture.BORDER_VERTICAL);
        blitBundle(guiGraphics, x + SLOT_COUNT * SLOT_WIDTH + 1, y + 1, Texture.BORDER_VERTICAL);
        for (int i = 0; i < SLOT_COUNT; i++) {
            blitBundle(guiGraphics, x + 1 + i * SLOT_WIDTH, y, Texture.BORDER_HORIZONTAL_TOP);
            blitBundle(guiGraphics, x + 1 + i * SLOT_WIDTH, y + SLOT_HEIGHT, Texture.BORDER_HORIZONTAL_BOTTOM);
        }
    }

    private void blitBundle(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        guiGraphics.blit(BUNDLE_TEXTURE_LOCATION, x, y, 0, texture.x, texture.y, texture.width, texture.height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private enum Texture {
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        private final int x;
        private final int y;
        private final int width;
        private final int height;

        Texture(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
