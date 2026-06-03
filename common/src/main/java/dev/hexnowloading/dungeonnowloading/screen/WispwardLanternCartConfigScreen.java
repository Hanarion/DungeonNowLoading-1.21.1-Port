package dev.hexnowloading.dungeonnowloading.screen;

import dev.hexnowloading.dungeonnowloading.block.entity.WispwardLanternBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.C2SWispwardLanternCartConfigPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class WispwardLanternCartConfigScreen extends Screen {
    private static final int FIELD_WIDTH = 300;
    private static final int FIELD_HEIGHT = 20;
    private static final int TITLE_Y = 20;
    private static final int FORM_TOP = 48;

    private final int entityId;
    private final int initialTimerSeconds;
    private EditBox durationEdit;
    private Button doneButton;
    private Button cancelButton;

    public WispwardLanternCartConfigScreen(int entityId, int timerSeconds) {
        super(Component.translatable("screen.dungeonnowloading.wispward_lantern_cart_config"));
        this.entityId = entityId;
        this.initialTimerSeconds = clampTimerSeconds(timerSeconds);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - FIELD_WIDTH / 2;
        int top = FORM_TOP;

        this.durationEdit = new EditBox(this.font, left, top + 20, FIELD_WIDTH, FIELD_HEIGHT, Component.translatable("screen.dungeonnowloading.wispward_lantern_cart_config.duration"));
        this.durationEdit.setMaxLength(6);
        this.durationEdit.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        this.durationEdit.setValue(Integer.toString(this.initialTimerSeconds));

        this.addRenderableWidget(this.durationEdit);
        this.setInitialFocus(this.durationEdit);

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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onDone() {
        int seconds = WispwardLanternBlockEntity.MIN_TIMER_SECONDS;
        try {
            seconds = Integer.parseInt(this.durationEdit.getValue().trim());
        } catch (NumberFormatException ignored) {
        }

        Services.NETWORK.sendToServer(new C2SWispwardLanternCartConfigPacket(this.entityId, clampTimerSeconds(seconds)));
        this.onCancel();
    }

    private void onCancel() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, TITLE_Y, 0xFFFFFF);

        int labelLeft = this.width / 2 - FIELD_WIDTH / 2;
        int top = FORM_TOP;
        graphics.drawString(this.font, Component.literal("Duration (seconds)"), labelLeft, top + 8, 0xA0A0A0, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int clampTimerSeconds(int seconds) {
        return Math.max(WispwardLanternBlockEntity.MIN_TIMER_SECONDS, Math.min(WispwardLanternBlockEntity.MAX_TIMER_SECONDS, seconds));
    }
}
