package dev.hexnowloading.dungeonnowloading.screen;

import dev.hexnowloading.dungeonnowloading.network.packets.S2CGauntletOpenEditorPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.data.GauntletIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GauntletScreen extends Screen {
    private final BlockPos origin;

    // Data model
    private String gauntletName; // testWave used as initial name from opener
    private int relX, relY, relZ;
    private int sizeX, sizeY, sizeZ;
    private int activationRange;
    private String lootTable;

    // Widgets
    private EditBox relXBox, relYBox, relZBox;
    private EditBox sizeXBox, sizeYBox, sizeZBox;
    private EditBox lootTableBox;
    private ActivationRangeSlider activationSlider;
    private Button showAreaBtn, showNodesBtn, showRangeBtn;
    private Button saveBtn, loadBtn, editWavesBtn;

    // Toggle states
    private boolean showArea = false;
    private boolean showNodes = false;
    private boolean showRange = false;

    // Popup state (similar to waves screen)
    private boolean popupOpen = false;
    private boolean popupIsLoad = true; // false => save
    private EditBox popupSearch;
    private Button popupOkBtn, popupCancelBtn;
    private List<String> popupEntries = new ArrayList<>();
    private List<String> popupFiltered = new ArrayList<>();
    private int popupSelected = -1;
    private int popupScroll = 0;
    private static final int POPUP_MAX_VISIBLE = 10;

    // Layout constants
    private static final int MARGIN_LEFT = 20;
    private static final int TITLE_Y = 15;
    private static final int FIELD_W = 60;
    private static final int FIELD_H = 20;
    private static final int GAP = 6;
    private static final int SECTION_GAP = 24;
    private static final int LABEL_COLOR = 0xA0A0A0;

    // Constructor matching existing call signature in ClientScreens.openGauntletEditor
    public GauntletScreen(BlockPos pos,
                          int wavesTotal, int wavesCurrent, boolean active,
                          int relX, int relY, int relZ,
                          int sizeX, int sizeY, int sizeZ,
                          int activationRange, String lootTable, String gauntletName) {
        super(Component.translatable("screen.dnl.gauntlet"));
        this.origin = pos;
        this.relX = relX;
        this.relY = relY;
        this.relZ = relZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.activationRange = Math.max(0, Math.min(32, activationRange));
        this.lootTable = lootTable == null ? "" : lootTable;
        this.gauntletName = gauntletName == null ? "" : gauntletName;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int x = MARGIN_LEFT;
        int y = TITLE_Y + 25; // start a bit below title

        // Relative Position section
        int relLabelY = y;
        // rel fields row baseline
        int relFieldsY = relLabelY + 16; // label + small gap
        relXBox = new EditBox(this.font, x, relFieldsY, FIELD_W, FIELD_H, Component.literal("relX"));
        relYBox = new EditBox(this.font, x + (FIELD_W + GAP), relFieldsY, FIELD_W, FIELD_H, Component.literal("relY"));
        relZBox = new EditBox(this.font, x + 2 * (FIELD_W + GAP), relFieldsY, FIELD_W, FIELD_H, Component.literal("relZ"));
        relXBox.setValue(String.valueOf(relX));
        relYBox.setValue(String.valueOf(relY));
        relZBox.setValue(String.valueOf(relZ));
        this.addRenderableWidget(relXBox);
        this.addRenderableWidget(relYBox);
        this.addRenderableWidget(relZBox);

        // Activation range slider to the right of relative position
        int sliderX = x + 3 * (FIELD_W + GAP) + 20;
        activationSlider = new ActivationRangeSlider(sliderX, relFieldsY, 160, FIELD_H, activationRange);
        this.addRenderableWidget(activationSlider);

        // Gauntlet Area section below Relative Position
        int areaLabelY = relFieldsY + FIELD_H + SECTION_GAP;
        int areaFieldsY = areaLabelY + 16;
        sizeXBox = new EditBox(this.font, x, areaFieldsY, FIELD_W, FIELD_H, Component.literal("sizeX"));
        sizeYBox = new EditBox(this.font, x + (FIELD_W + GAP), areaFieldsY, FIELD_W, FIELD_H, Component.literal("sizeY"));
        sizeZBox = new EditBox(this.font, x + 2 * (FIELD_W + GAP), areaFieldsY, FIELD_W, FIELD_H, Component.literal("sizeZ"));
        sizeXBox.setValue(String.valueOf(sizeX));
        sizeYBox.setValue(String.valueOf(sizeY));
        sizeZBox.setValue(String.valueOf(sizeZ));
        this.addRenderableWidget(sizeXBox);
        this.addRenderableWidget(sizeYBox);
        this.addRenderableWidget(sizeZBox);

        // Toggle buttons to the right of area fields
        int togglesX = sliderX;
        showAreaBtn = Button.builder(Component.literal(toggleLabel("Area", showArea)), b -> { showArea = !showArea; updateToggleTexts(); }).bounds(togglesX, areaFieldsY, 140, FIELD_H).build();
        showNodesBtn = Button.builder(Component.literal(toggleLabel("Nodes", showNodes)), b -> { showNodes = !showNodes; updateToggleTexts(); }).bounds(togglesX, areaFieldsY + FIELD_H + GAP, 140, FIELD_H).build();
        showRangeBtn = Button.builder(Component.literal(toggleLabel("Range", showRange)), b -> { showRange = !showRange; updateToggleTexts(); }).bounds(togglesX, areaFieldsY + 2 * (FIELD_H + GAP), 140, FIELD_H).build();
        this.addRenderableWidget(showAreaBtn);
        this.addRenderableWidget(showNodesBtn);
        this.addRenderableWidget(showRangeBtn);

        // Loot table input below toggles
        int lootLabelY = areaFieldsY + 2 * (FIELD_H + GAP) + FIELD_H + SECTION_GAP;
        int lootFieldY = lootLabelY + 16;
        lootTableBox = new EditBox(this.font, x, lootFieldY, this.width - x * 2, FIELD_H, Component.literal("Loot Table"));
        lootTableBox.setMaxLength(256);
        lootTableBox.setValue(lootTable);
        this.addRenderableWidget(lootTableBox);

        // Bottom action buttons below loot table
        int buttonsY = lootFieldY + FIELD_H + SECTION_GAP;
        saveBtn = this.addRenderableWidget(Button.builder(Component.literal("Save"), b -> openSavePopup()).bounds(x, buttonsY, 80, FIELD_H).build());
        loadBtn = this.addRenderableWidget(Button.builder(Component.literal("Load"), b -> openLoadPopup()).bounds(x + 80 + GAP, buttonsY, 80, FIELD_H).build());
        editWavesBtn = this.addRenderableWidget(Button.builder(Component.literal("Edit Waves"), b -> openWaves()).bounds(x + 2 * (80 + GAP), buttonsY, 140, FIELD_H).build());

        if (popupOpen) buildPopupWidgets();
    }

    private void updateToggleTexts() {
        showAreaBtn.setMessage(Component.literal(toggleLabel("Area", showArea)));
        showNodesBtn.setMessage(Component.literal(toggleLabel("Nodes", showNodes)));
        showRangeBtn.setMessage(Component.literal(toggleLabel("Range", showRange)));
    }

    private String toggleLabel(String base, boolean on) { return base + ": " + (on ? "ON" : "OFF"); }

    private void openWaves() {
        this.minecraft.setScreen(new GauntletWavesScreen(Component.translatable("screen.dnl.gauntlet_waves")));
    }

    // Popup logic
    private void openLoadPopup() {
        popupOpen = true; popupIsLoad = true; popupSelected = -1; popupScroll = 0;
        popupEntries = listGauntletNames();
        popupFiltered = new ArrayList<>(popupEntries);
        setUnderlyingEnabled(false);
        buildPopupWidgets();
    }
    private void openSavePopup() {
        popupOpen = true; popupIsLoad = false; popupSelected = -1; popupScroll = 0;
        popupEntries = listGauntletNames();
        popupFiltered = new ArrayList<>(popupEntries);
        setUnderlyingEnabled(false);
        buildPopupWidgets();
        if (popupSearch != null) popupSearch.setValue(gauntletName == null ? "" : gauntletName);
    }

    private void buildPopupWidgets() {
        int panelW = Math.min(320, this.width - 40);
        int panelH = Math.min(220, this.height - 60);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        popupSearch = new EditBox(this.font, panelX + 10, panelY + 30, panelW - 20, 18, Component.literal("Search"));
        popupSearch.setResponder(s -> updatePopupFilter());
        popupSearch.setMaxLength(64);
        this.addRenderableWidget(popupSearch);
        popupOkBtn = this.addRenderableWidget(Button.builder(Component.literal(popupIsLoad ? "Load" : "Save"), b -> confirmPopup()).bounds(panelX + panelW - 10 - 150, panelY + panelH - 10 - 20, 70, 20).build());
        popupCancelBtn = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> closePopup()).bounds(panelX + panelW - 10 - 70, panelY + panelH - 10 - 20, 70, 20).build());
        updatePopupFilter();
        updatePopupOkActive();
    }

    private void updatePopupFilter() {
        String q = popupSearch.getValue().trim().toLowerCase(Locale.ROOT);
        popupFiltered = popupEntries.stream().filter(s -> q.isEmpty() || s.toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        popupSelected = -1;
        popupScroll = 0;
        updatePopupOkActive();
    }

    private void updatePopupOkActive() {
        if (popupOkBtn == null) return;
        if (popupIsLoad) popupOkBtn.active = popupSelected >= 0 && popupSelected < popupFiltered.size();
        else popupOkBtn.active = !popupSearch.getValue().trim().isEmpty();
    }

    private void confirmPopup() {
        if (popupIsLoad) {
            if (popupSelected >= 0 && popupSelected < popupFiltered.size()) {
                String chosen = popupFiltered.get(popupSelected);
                gauntletName = chosen;
                // Load data
                var loaded = GauntletIO.load(chosen);
                if (loaded != null) {
                    relX = loaded.relX(); relY = loaded.relY(); relZ = loaded.relZ();
                    sizeX = loaded.sizeX(); sizeY = loaded.sizeY(); sizeZ = loaded.sizeZ();
                    activationRange = loaded.activationRange();
                    lootTable = loaded.lootTable();
                }
            }
        } else {
            String name = popupSearch.getValue().trim();
            if (!name.isEmpty()) {
                gauntletName = name;
                // Save data (waves not yet wired from this screen; keep empty for now)
                GauntletIO.save(name, relX, relY, relZ, sizeX, sizeY, sizeZ, activationRange, java.util.List.of(), lootTable);
            }
        }
        closePopup();
    }

    private void closePopup() {
        popupOpen = false;
        popupSearch = null;
        popupOkBtn = null;
        popupCancelBtn = null;
        popupEntries = List.of();
        popupFiltered = List.of();
        popupSelected = -1; popupScroll = 0;
        this.init(); // rebuild normal widgets only; automatically re-enables
    }

    private void setUnderlyingEnabled(boolean enabled) {
        if (relXBox != null) { relXBox.setVisible(enabled); relXBox.setEditable(enabled); }
        if (relYBox != null) { relYBox.setVisible(enabled); relYBox.setEditable(enabled); }
        if (relZBox != null) { relZBox.setVisible(enabled); relZBox.setEditable(enabled); }
        if (sizeXBox != null) { sizeXBox.setVisible(enabled); sizeXBox.setEditable(enabled); }
        if (sizeYBox != null) { sizeYBox.setVisible(enabled); sizeYBox.setEditable(enabled); }
        if (sizeZBox != null) { sizeZBox.setVisible(enabled); sizeZBox.setEditable(enabled); }
        if (lootTableBox != null) { lootTableBox.setVisible(enabled); lootTableBox.setEditable(enabled); }
        if (activationSlider != null) { activationSlider.active = enabled; activationSlider.visible = enabled; }
        if (showAreaBtn != null) { showAreaBtn.active = enabled; showAreaBtn.visible = enabled; }
        if (showNodesBtn != null) { showNodesBtn.active = enabled; showNodesBtn.visible = enabled; }
        if (showRangeBtn != null) { showRangeBtn.active = enabled; showRangeBtn.visible = enabled; }
        if (saveBtn != null) { saveBtn.active = enabled; saveBtn.visible = enabled; }
        if (loadBtn != null) { loadBtn.active = enabled; loadBtn.visible = enabled; }
        if (editWavesBtn != null) { editWavesBtn.active = enabled; editWavesBtn.visible = enabled; }
    }

    private List<String> listGauntletNames() {
        File dir = resolveGauntletDir();
        if (dir == null || !dir.exists() || !dir.isDirectory()) return List.of();
        File[] subs = dir.listFiles(File::isDirectory);
        if (subs == null) return List.of();
        List<String> names = new ArrayList<>();
        for (File f : subs) names.add(f.getName());
        return names.stream().distinct().sorted().collect(Collectors.toList());
    }

    private File resolveGauntletDir() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            try {
                File gameDir = mc.gameDirectory;
                return new File(gameDir, "dnl/gauntlet");
            } catch (Throwable ignored) {}
        }
        return null;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        String title = "Gauntlet: " + (gauntletName == null || gauntletName.isBlank() ? "<unnamed>" : gauntletName);
        g.drawCenteredString(this.font, Component.literal(title), this.width / 2, TITLE_Y, 0xFFFFFF);

        // Section labels
        int x = MARGIN_LEFT;
        int relLabelY = TITLE_Y + 25; // matches init
        int areaLabelY = relLabelY + 16 + FIELD_H + SECTION_GAP;
        int lootLabelY = areaLabelY + 16 + FIELD_H + (SECTION_GAP * 3); // keep spacing consistent

        g.drawString(this.font, Component.literal("Relative Position"), x, relLabelY, LABEL_COLOR, false);
        g.drawString(this.font, Component.literal("Activation Range"), x + 3 * (FIELD_W + GAP) + 20, relLabelY, LABEL_COLOR, false);
        g.drawString(this.font, Component.literal("Gauntlet Area"), x, areaLabelY, LABEL_COLOR, false);
        g.drawString(this.font, Component.literal("Loot Table:"), x, lootLabelY, LABEL_COLOR, false);

        if (popupOpen) renderPopup(g, mouseX, mouseY, partialTick); // draw overlay and list first
        super.render(g, mouseX, mouseY, partialTick); // then draw widgets (only popup widgets are visible/enabled)
    }

    private void renderPopup(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x88000000);
        int panelW = Math.min(320, this.width - 40);
        int panelH = Math.min(220, this.height - 60);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF101010);
        g.drawCenteredString(this.font, Component.literal(popupIsLoad ? "Load Gauntlet" : "Save Gauntlet"), panelX + panelW / 2, panelY + 8, 0xFFFFFF);
        g.drawString(this.font, Component.literal("Search:"), panelX + 10, panelY + 20, 0xA0A0A0, false);

        // List area
        int listX = panelX + 10;
        int listY = panelY + 60;
        int listW = panelW - 20;
        int listH = panelH - 60 - 40;
        g.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, 0xFF303030);
        g.fill(listX, listY, listX + listW, listY + listH, 0xFF202020);

        int visible = Math.max(1, Math.min(POPUP_MAX_VISIBLE, listH / (this.font.lineHeight + 4)));
        int maxStart = Math.max(0, popupFiltered.size() - visible);
        if (popupScroll < 0) popupScroll = 0; if (popupScroll > maxStart) popupScroll = maxStart;
        for (int i = 0; i < visible && (i + popupScroll) < popupFiltered.size(); i++) {
            int idx = i + popupScroll;
            String entry = popupFiltered.get(idx);
            int itemH = this.font.lineHeight + 4;
            int y0 = listY + 2 + i * itemH;
            boolean sel = idx == popupSelected;
            if (sel) g.fill(listX + 1, y0, listX + listW - 1, y0 + itemH, 0xFF3A3A60);
            g.drawString(this.font, Component.literal(entry), listX + 4, y0 + 2, 0xFFFFFF, false);
        }

        updatePopupOkActive();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popupOpen) {
            int panelW = Math.min(320, this.width - 40);
            int panelH = Math.min(220, this.height - 60);
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;
            int listX = panelX + 10;
            int listY = panelY + 60;
            int listW = panelW - 20;
            int listH = panelH - 60 - 40;
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                int itemH = this.font.lineHeight + 4;
                int relY = (int) mouseY - (listY + 2);
                int indexInView = relY / itemH;
                int visible = Math.max(1, Math.min(POPUP_MAX_VISIBLE, listH / itemH));
                if (indexInView >= 0 && indexInView < visible) {
                    int idx = popupScroll + indexInView;
                    if (idx >= 0 && idx < popupFiltered.size()) {
                        popupSelected = idx;
                        if (!popupIsLoad && popupSearch != null) popupSearch.setValue(popupFiltered.get(idx));
                        updatePopupOkActive();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (popupOpen) {
            int panelW = Math.min(320, this.width - 40);
            int panelH = Math.min(220, this.height - 60);
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;
            int listX = panelX + 10;
            int listY = panelY + 60;
            int listW = panelW - 20;
            int listH = panelH - 60 - 40;
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                popupScroll -= (delta > 0 ? 1 : -1);
                int visible = Math.max(1, Math.min(POPUP_MAX_VISIBLE, listH / (this.font.lineHeight + 4)));
                int maxStart = Math.max(0, popupFiltered.size() - visible);
                if (popupScroll < 0) popupScroll = 0; if (popupScroll > maxStart) popupScroll = maxStart;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (popupOpen) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { closePopup(); return true; }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) { if (popupOkBtn != null && popupOkBtn.active) { confirmPopup(); return true; } }
        } else {
            if (key == GLFW.GLFW_KEY_ESCAPE) { this.minecraft.setScreen(null); return true; }
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public void tick() {
        if (relXBox != null) relXBox.tick();
        if (relYBox != null) relYBox.tick();
        if (relZBox != null) relZBox.tick();
        if (sizeXBox != null) sizeXBox.tick();
        if (sizeYBox != null) sizeYBox.tick();
        if (sizeZBox != null) sizeZBox.tick();
        if (lootTableBox != null) lootTableBox.tick();
        if (popupSearch != null) popupSearch.tick();

        // Parse numeric boxes defensively
        relX = parseIntSafe(relXBox.getValue(), relX);
        relY = parseIntSafe(relYBox.getValue(), relY);
        relZ = parseIntSafe(relZBox.getValue(), relZ);
        sizeX = parseIntSafe(sizeXBox.getValue(), sizeX);
        sizeY = parseIntSafe(sizeYBox.getValue(), sizeY);
        sizeZ = parseIntSafe(sizeZBox.getValue(), sizeZ);
        activationRange = activationSlider.getIntValue();
        lootTable = lootTableBox.getValue();
        super.tick();
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception ignored) { return fallback; }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // Simple slider implementation
    private static class ActivationRangeSlider extends AbstractSliderButton {
        ActivationRangeSlider(int x, int y, int w, int h, int current) {
            super(x, y, w, h, Component.literal(""), current / 32.0); // value between 0 and 1
            updateMessage();
        }
        @Override
        protected void updateMessage() {
            int v = getIntValue();
            this.setMessage(Component.literal("Range: " + v));
        }
        int getIntValue() { return (int) Math.round(this.value * 32.0); }
        @Override
        protected void applyValue() { updateMessage(); }
    }

    public static void openScreen(ServerPlayer player, BlockPos pos) {
        // Open with completely empty/default values as requested
        Services.NETWORK.sendToPlayer(new S2CGauntletOpenEditorPacket(
                pos,
                0, 0, false,
                0, 0, 0,
                0, 0, 0,
                0, "", ""
        ), player);
    }
}
