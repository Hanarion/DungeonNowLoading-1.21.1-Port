package dev.hexnowloading.dungeonnowloading.screen;

import dev.hexnowloading.dungeonnowloading.network.packets.C2SGauntletUpdatePacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class GauntletEditScreen extends Screen {
    private static final int MAX_ACTIVATION_RANGE = 32; // reasonable default; adjust as needed

    private final BlockPos pos;
    private int wavesTotal;   // 1..5
    private int wavesCurrent; // 0..wavesTotal
    private boolean active;
    private boolean sent = false;

    // advanced config
    private int relX, relY, relZ;
    private int sizeX, sizeY, sizeZ;
    private int activationRange;
    private String lootTable = "";
    private String testWave = "N/A";

    // widgets
    private Button minusTotal, plusTotal, minusCurrent, plusCurrent, toggleActive; // used in waves screen
    private Button highlightNodesBtn, editWavesBtn;
    private EditBox relXBox, relYBox, relZBox;
    private EditBox sizeXBox, sizeYBox, sizeZBox;
    private EditBox lootTableBox;
    private CycleButton<String> testWaveDropdown;
    private Button executeWaveBtn;
    private AbstractSliderButton activationRangeSlider;

    private List<String> waveNames = new ArrayList<>();

    public GauntletEditScreen(BlockPos pos, int wavesTotal, int wavesCurrent, boolean active) {
        super(Component.literal("Gauntlet Block"));
        this.pos = pos.immutable();
        this.wavesTotal = clampTotal(wavesTotal);
        this.wavesCurrent = clampCurrent(wavesCurrent, this.wavesTotal);
        this.active = active;
        initDefaultWaveNames();
    }

    public GauntletEditScreen(BlockPos pos, int wavesTotal, int wavesCurrent, boolean active,
                              int relX, int relY, int relZ,
                              int sizeX, int sizeY, int sizeZ,
                              int activationRange, String lootTable, String testWave) {
        this(pos, wavesTotal, wavesCurrent, active);
        this.relX = relX; this.relY = relY; this.relZ = relZ;
        this.sizeX = sizeX; this.sizeY = sizeY; this.sizeZ = sizeZ;
        this.activationRange = activationRange;
        this.lootTable = lootTable == null ? "" : lootTable;
        this.testWave = (testWave == null || testWave.isEmpty()) ? "Boss" : testWave;
    }

    private int clampTotal(int v) { return Math.max(1, Math.min(5, v)); }
    private int clampCurrent(int v, int total) { return Math.max(0, Math.min(total, v)); }

    private void initDefaultWaveNames() {
        waveNames.clear();
        for (int i = 0; i < this.wavesTotal; i++) waveNames.add(i == this.wavesTotal - 1 ? "Boss" : "Wave " + (i + 1));
    }

    List<String> getWaveNamesCopy() { return new ArrayList<>(waveNames); }
    void setWaveNamesFromChild(List<String> names) {
        if (names == null) return;
        this.waveNames = new ArrayList<>(names);
        this.wavesTotal = clampTotal(this.waveNames.size());
        this.wavesCurrent = clampCurrent(this.wavesCurrent, this.wavesTotal);
        // ensure selected testWave remains valid
        if (!this.waveNames.contains(this.testWave)) {
            this.testWave = this.waveNames.get(Math.max(0, this.waveNames.size() - 1));
        }
    }

    // Allow the child waves screen to request a UI rebuild so dropdown gets updated
    public void onWaveNamesUpdated() {
        applyFromFields();
        this.clearWidgets();
        this.init();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = 30; // start near top to avoid cramped layout on small screens
        int labelClr = 0xA0A0A0;

        int row = top;
        int fieldH = 20;
        int smallW = 54;
        int gap = 6;

        // Labels above Relative Position fields
        // Group label
        // X/Y/Z labels positioned above each box
        // Row 1: Relative Position (X/Y/Z) + Activation Range slider
        // Labels
        // Relative group title
        // draw in render(); here we only place widgets
        relXBox = makeIntBox(cx - 40 - smallW - gap - smallW - gap, row + 12, smallW, relX);
        relYBox = makeIntBox(cx - 40 - smallW - gap, row + 12, smallW, relY);
        relZBox = makeIntBox(cx - 40, row + 12, smallW, relZ);
        this.addRenderableWidget(relXBox);
        this.addRenderableWidget(relYBox);
        this.addRenderableWidget(relZBox);

        // Activation Range slider instead of textbox
        int sliderX = cx + 50;
        int sliderW = 180;
        activationRange = Math.max(0, Math.min(MAX_ACTIVATION_RANGE, activationRange));
        activationRangeSlider = new AbstractSliderButton(sliderX, row + 12, sliderW, fieldH, Component.empty(), activationRange / (double) MAX_ACTIVATION_RANGE) {
            @Override
            protected void updateMessage() { /* label rendered above; keep empty to avoid overlap */ }
            @Override
            protected void applyValue() {
                activationRange = (int) Math.round(this.value * MAX_ACTIVATION_RANGE);
            }
        };
        this.addRenderableWidget(activationRangeSlider);
        row += fieldH + 24; // 12 label + 20 field + padding

        // Row 2: Arena Size (X/Y/Z) + Highlight Nodes
        sizeXBox = makeIntBox(cx - 40 - smallW - gap - smallW - gap, row + 12, smallW, sizeX);
        sizeYBox = makeIntBox(cx - 40 - smallW - gap, row + 12, smallW, sizeY);
        sizeZBox = makeIntBox(cx - 40, row + 12, smallW, sizeZ);
        this.addRenderableWidget(sizeXBox);
        this.addRenderableWidget(sizeYBox);
        this.addRenderableWidget(sizeZBox);

        highlightNodesBtn = Button.builder(Component.literal("Highlight Nodes"), b -> {
            // TODO: client-side visualization hook (no-op for now)
        }).bounds(cx + 50, row + 12, 180, fieldH).build();
        this.addRenderableWidget(highlightNodesBtn);
        row += fieldH + 24;

        // Row 3: Loot table label above wide box
        lootTableBox = new EditBox(this.font, cx - 150, row + 12, 300, fieldH, Component.empty());
        lootTableBox.setMaxLength(256);
        lootTableBox.setValue(lootTable);
        this.addRenderableWidget(lootTableBox);
        row += fieldH + 24;

        // Row 4: Test wave dropdown + Execute button
        List<String> waves = getWaveNamesCopy();
        if (waves.isEmpty()) waves.add("Test");
        String selected = waves.contains(testWave) ? testWave : waves.get(waves.size() - 1);
        int ddX = cx - 150;
        int ddW = 220;
        int execW = 70;
        int execX = ddX + ddW + 8;
        testWaveDropdown = CycleButton.builder((String s) -> Component.literal(s))
                .withValues(waves)
                .withInitialValue(selected)
                .create(ddX, row + 12, ddW, fieldH, Component.literal("Test Wave"), (btn, value) -> testWave = value);
        this.addRenderableWidget(testWaveDropdown);
        executeWaveBtn = Button.builder(Component.literal("Execute"), b -> {
            // TODO: placeholder for execution (trigger test wave)
        }).bounds(execX, row + 12, execW, fieldH).build();
        this.addRenderableWidget(executeWaveBtn);
        row += fieldH + 24;

        // Row 5: Edit Waves button (optional heading drawn above)
        editWavesBtn = Button.builder(Component.literal("Edit Waves"), b -> {
            this.minecraft.setScreen(new dev.hexnowloading.dungeonnowloading.screen.GauntletWavesScreen(this, pos, getWaveNamesCopy()));
        }).bounds(cx - 60, row + 12, 120, fieldH).build();
        this.addRenderableWidget(editWavesBtn);
        row += fieldH + 28;

        // Bottom buttons
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            applyFromFields();
            sendUpdate();
            this.minecraft.setScreen(null);
        }).bounds(cx - 150, row + 12, 120, fieldH).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> this.minecraft.setScreen(null))
                .bounds(cx + 30, row + 12, 120, fieldH).build());
    }

    private EditBox makeIntBox(int x, int y, int w, int val) {
        EditBox box = new EditBox(this.font, x, y, w, 20, Component.empty());
        box.setMaxLength(12);
        box.setValue(Integer.toString(val));
        return box;
    }

    void setWavesFromChild(int wavesTotal, int wavesCurrent, boolean active) {
        this.wavesTotal = clampTotal(wavesTotal);
        this.wavesCurrent = clampCurrent(wavesCurrent, this.wavesTotal);
        this.active = active;
    }

    private void applyFromFields() {
        try { this.relX = Integer.parseInt(relXBox.getValue().trim()); } catch (Exception ignored) {}
        try { this.relY = Integer.parseInt(relYBox.getValue().trim()); } catch (Exception ignored) {}
        try { this.relZ = Integer.parseInt(relZBox.getValue().trim()); } catch (Exception ignored) {}
        try { this.sizeX = Math.max(0, Integer.parseInt(sizeXBox.getValue().trim())); } catch (Exception ignored) {}
        try { this.sizeY = Math.max(0, Integer.parseInt(sizeYBox.getValue().trim())); } catch (Exception ignored) {}
        try { this.sizeZ = Math.max(0, Integer.parseInt(sizeZBox.getValue().trim())); } catch (Exception ignored) {}
        // activationRange is updated via slider's applyValue();
        this.lootTable = lootTableBox.getValue().trim();
        // testWave comes from dropdown; ensure non-empty
        if (this.testWave == null || this.testWave.isEmpty()) {
            List<String> waves = getWaveNamesCopy();
            if (waves.isEmpty()) waves.add("Boss");
            this.testWave = waves.get(waves.size() - 1);
        }
    }

    private void sendUpdate() {
        if (sent) return;
        sent = true;
        Services.NETWORK.sendToServer(new C2SGauntletUpdatePacket(
                pos,
                wavesTotal, wavesCurrent, active,
                relX, relY, relZ,
                sizeX, sizeY, sizeZ,
                activationRange, lootTable, testWave
        ));
    }

    @Override
    public void removed() {
        applyFromFields();
        sendUpdate();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int top = 30;
        int labelClr = 0xA0A0A0;

        g.drawCenteredString(this.font, this.title, cx, top - 20, 0xFFFFFF);

        int row = top;
        // Row 1 labels
        g.drawString(this.font, Component.literal("Relative Position"), cx - 250, row + 16, labelClr);
        g.drawString(this.font, Component.literal("X"), cx - 40 - 54 - 6 - 54 - 6, row, labelClr);
        g.drawString(this.font, Component.literal("Y"), cx - 40 - 54 - 6, row, labelClr);
        g.drawString(this.font, Component.literal("Z"), cx - 40, row, labelClr);
        g.drawString(this.font, Component.literal("Activation Range: " + activationRange), cx + 50, row, labelClr);
        row += 40 + 4;

        // Row 2 labels
        g.drawString(this.font, Component.literal("Arena Size"), cx - 220, row + 16, labelClr);
        g.drawString(this.font, Component.literal("X"), cx - 40 - 54 - 6 - 54 - 6, row, labelClr);
        g.drawString(this.font, Component.literal("Y"), cx - 40 - 54 - 6, row, labelClr);
        g.drawString(this.font, Component.literal("Z"), cx - 40, row, labelClr);

        row += 40 + 4;

        // Row 3 label
        g.drawString(this.font, Component.literal("Loot table"), cx - 150, row, labelClr);
        row += 40 + 4;

        // Row 4 label
//        g.drawString(this.font, Component.literal("Test Wave:"), cx - 150, row, labelClr);

        super.render(g, mouseX, mouseY, partialTick);
    }
}
