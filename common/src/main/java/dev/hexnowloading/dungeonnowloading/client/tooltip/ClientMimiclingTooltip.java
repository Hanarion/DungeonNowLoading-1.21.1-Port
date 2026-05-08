package dev.hexnowloading.dungeonnowloading.client.tooltip;

import dev.hexnowloading.dungeonnowloading.item.MimiclingTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClientMimiclingTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BUNDLE_TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
    private static final ResourceLocation MIMICLING_TEXTURE_LOCATION = new ResourceLocation("dungeonnowloading", "textures/gui/mimicling_gui.png");
    private static final int SLOT_COUNT = 5;
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int FOOD_ROW_HEIGHT = 14;
    private static final int FOOD_ICON_SIZE = 12;
    private static final int FOOD_TEXT_GAP = 3;
    private static final int FOOD_TEXT_Y_OFFSET = 3;
    private static final int FOOD_SELECTION_PADDING = 1;
    private static final float FOOD_ICON_SCALE = 0.75F;
    private static final int PREVIEW_GAP = 3;
    private static final int PREVIEW_LINE_HEIGHT = 10;
    private static final int FOOD_TEXT_COLOR = 0xAAAAAA;
    private static final int FOOD_DESCRIPTION_COLOR = 0x777777;
    private static final int TEXTURE_SIZE = 128;
    private static final int MARGIN_Y = 4;
    private final NonNullList<ItemStack> items;
    private final List<MimiclingTooltip.ActiveFood> activeFoods;
    private final ItemStack previewFood;
    private final List<Component> previewFoodLines;
    private final int selectedSlot;
    private final int selectedFoodSlot;
    private final int capacity;

    public ClientMimiclingTooltip(MimiclingTooltip tooltip) {
        this.items = tooltip.getItems();
        this.activeFoods = tooltip.getActiveFoods();
        this.previewFood = tooltip.getPreviewFood();
        this.previewFoodLines = tooltip.getPreviewFoodLines();
        this.selectedSlot = tooltip.getSelectedSlot();
        this.selectedFoodSlot = tooltip.getSelectedFoodSlot();
        this.capacity = tooltip.getCapacity();
    }

    @Override
    public int getHeight() {
        int slotHeight = hasStorageSlots() ? SLOT_HEIGHT + 2 : 0;
        int foodHeight = activeFoods.size() * FOOD_ROW_HEIGHT;
        int gap = hasStorageSlots() && !activeFoods.isEmpty() ? MARGIN_Y : 0;
        int previewHeight = hasPreviewFood() ? PREVIEW_GAP + Math.max(FOOD_ICON_SIZE, getPreviewLines().size() * PREVIEW_LINE_HEIGHT) : 0;
        return slotHeight + gap + foodHeight + previewHeight + MARGIN_Y;
    }

    @Override
    public int getWidth(Font font) {
        int width = hasStorageSlots() ? SLOT_COUNT * SLOT_WIDTH + 2 : 0;
        for (MimiclingTooltip.ActiveFood activeFood : activeFoods) {
            width = Math.max(width, FOOD_ICON_SIZE + FOOD_TEXT_GAP + font.width(getFoodUsesText(activeFood)));
        }
        if (hasPreviewFood()) {
            for (PreviewLine line : getPreviewLines()) {
                width = Math.max(width, FOOD_ICON_SIZE + FOOD_TEXT_GAP + font.width(line.component()));
            }
        }
        return width;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int contentY = y;
        if (hasStorageSlots()) {
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
            contentY += SLOT_HEIGHT + 2 + (!activeFoods.isEmpty() ? MARGIN_Y : 0);
        }

        for (int i = 0; i < activeFoods.size(); i++) {
            MimiclingTooltip.ActiveFood activeFood = activeFoods.get(i);
            if (!activeFood.stack().isEmpty()) {
                renderSmallItem(guiGraphics, activeFood.stack(), x, contentY);
            }
            if (i == selectedFoodSlot) {
                guiGraphics.fill(
                        x - FOOD_SELECTION_PADDING,
                        contentY - FOOD_SELECTION_PADDING,
                        x + FOOD_ICON_SIZE + FOOD_SELECTION_PADDING,
                        contentY + FOOD_ICON_SIZE + FOOD_SELECTION_PADDING,
                        0x80FFFFFF
                );
            }
            guiGraphics.drawString(font, getFoodUsesText(activeFood), x + FOOD_ICON_SIZE + FOOD_TEXT_GAP, contentY + FOOD_TEXT_Y_OFFSET, FOOD_TEXT_COLOR, false);
            contentY += FOOD_ROW_HEIGHT;
        }

        if (hasPreviewFood()) {
            contentY += PREVIEW_GAP;
            renderSmallItem(guiGraphics, previewFood, x, contentY);
            List<PreviewLine> lines = getPreviewLines();
            for (int i = 0; i < lines.size(); i++) {
                PreviewLine line = lines.get(i);
                int color = line.description() ? FOOD_DESCRIPTION_COLOR : FOOD_TEXT_COLOR;
                guiGraphics.drawString(font, line.component(), x + FOOD_ICON_SIZE + FOOD_TEXT_GAP, contentY + FOOD_TEXT_Y_OFFSET + i * PREVIEW_LINE_HEIGHT, color, false);
            }
        }
    }

    private List<PreviewLine> getPreviewLines() {
        List<PreviewLine> lines = new ArrayList<>();
        for (int i = 0; i < previewFoodLines.size(); i++) {
            Component component = previewFoodLines.get(i);
            String text = component.getString();
            String[] split = text.split("\\R", -1);
            if (split.length == 1) {
                lines.add(new PreviewLine(component, i > 0));
                continue;
            }
            for (String line : split) {
                lines.add(new PreviewLine(Component.literal(line).withStyle(component.getStyle()), i > 0));
            }
        }
        return lines;
    }

    private record PreviewLine(Component component, boolean description) {
    }

    private boolean hasStorageSlots() {
        return !items.isEmpty();
    }

    private String getFoodUsesText(MimiclingTooltip.ActiveFood activeFood) {
        return activeFood.infinite() ? "∞" : activeFood.uses() + "/" + activeFood.maxUses();
    }

    private boolean hasPreviewFood() {
        return !previewFood.isEmpty() && !previewFoodLines.isEmpty();
    }

    private void renderSmallItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(FOOD_ICON_SCALE, FOOD_ICON_SCALE, 1.0F);
        guiGraphics.renderItem(stack, Math.round(x / FOOD_ICON_SCALE), Math.round(y / FOOD_ICON_SCALE), 0);
        guiGraphics.pose().popPose();
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
