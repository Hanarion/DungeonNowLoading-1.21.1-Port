package dev.hexnowloading.dungeonnowloading.screen;

import dev.hexnowloading.dungeonnowloading.block.entity.WispwardChestBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.C2SWispwardChestConfigPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class WispwardChestConfigScreen extends Screen {
    private static final int FIELD_WIDTH = 300;
    private static final int FIELD_HEIGHT = 20;
    private static final int TITLE_Y = 20;
    private static final int FORM_TOP = 48;

    private final BlockPos pos;
    private final ResourceLocation initialLootTable;
    private final int initialRequiredLitLanterns;
    private EditBox lootTableEdit;
    private EditBox countEdit;
    private Button doneButton;
    private Button cancelButton;
    private Button resetButton;

    public WispwardChestConfigScreen(BlockPos pos, ResourceLocation lootTable, int requiredLitLanterns) {
        super(Component.translatable("screen.dungeonnowloading.wispward_chest_config"));
        this.pos = pos.immutable();
        this.initialLootTable = lootTable == null ? WispwardChestBlockEntity.DEFAULT_LOOT_TABLE : lootTable;
        this.initialRequiredLitLanterns = Math.max(1, requiredLitLanterns);
        this.lootTableEdit = null;
        this.countEdit = null;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - FIELD_WIDTH / 2;
        int top = FORM_TOP;

        this.lootTableEdit = new EditBox(this.font, left, top + 20, FIELD_WIDTH, FIELD_HEIGHT, Component.translatable("screen.dungeonnowloading.wispward_chest_config.loot_table"));
        this.lootTableEdit.setMaxLength(256);

        this.countEdit = new EditBox(this.font, left, top + 70, FIELD_WIDTH / 2 - 4, FIELD_HEIGHT, Component.translatable("screen.dungeonnowloading.wispward_chest_config.count"));
        this.countEdit.setMaxLength(6);
        this.countEdit.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        this.lootTableEdit.setValue(this.initialLootTable.toString());
        this.countEdit.setValue(Integer.toString(this.initialRequiredLitLanterns));

        if (this.minecraft != null && this.minecraft.level != null
                && this.minecraft.level.getBlockEntity(this.pos) instanceof WispwardChestBlockEntity chest) {
            this.lootTableEdit.setValue(chest.getConfiguredLootTable().toString());
            this.countEdit.setValue(Integer.toString(Math.max(1, chest.getEffectiveRequiredLitLanterns())));
        }

        this.addRenderableWidget(this.lootTableEdit);
        this.addRenderableWidget(this.countEdit);
        this.setInitialFocus(this.lootTableEdit);

        this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.dungeonnowloading.wispward_chest_config.reset"), button -> this.onReset())
                .bounds(left + FIELD_WIDTH / 2 + 4, top + 70, FIELD_WIDTH / 2 - 4, FIELD_HEIGHT).build());

        int buttonY = this.height / 4 + 120;
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone())
                .bounds(this.width / 2 - 154, buttonY, 150, 20).build());

        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel())
                .bounds(this.width / 2 + 4, buttonY, 150, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.onDone();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.doneButton != null && this.doneButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.cancelButton != null && this.cancelButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.resetButton != null && this.resetButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onDone() {
        this.sendUpdate();
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    private void onCancel() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    private void onReset() {
        this.sendUpdate(true);
    }

    private void sendUpdate() {
        this.sendUpdate(false);
    }

    private void sendUpdate(boolean resetReward) {
        ResourceLocation lootTable = ResourceLocation.tryParse(this.lootTableEdit.getValue().trim());
        if (lootTable == null) {
            lootTable = WispwardChestBlockEntity.DEFAULT_LOOT_TABLE;
        }

        int count = 1;
        try {
            count = Integer.parseInt(this.countEdit.getValue().trim());
        } catch (NumberFormatException ignored) {
        }

        Services.NETWORK.sendToServer(new C2SWispwardChestConfigPacket(this.pos, lootTable, Math.max(1, count), resetReward));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, TITLE_Y, 0xFFFFFF);

        int labelLeft = this.width / 2 - FIELD_WIDTH / 2;
        int top = FORM_TOP;
        graphics.drawString(this.font, Component.literal("Loot Table"), labelLeft, top + 8, 0xA0A0A0, false);
        graphics.drawString(this.font, Component.literal("Count"), labelLeft, top + 58, 0xA0A0A0, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
