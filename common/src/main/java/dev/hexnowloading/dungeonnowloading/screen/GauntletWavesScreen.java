package dev.hexnowloading.dungeonnowloading.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GauntletWavesScreen extends Screen {
    private static final int MAX_WAVES = 5;

    private final GauntletEditScreen parent;
    private final BlockPos pos;

    private final List<String> names; // working copy
    private final List<EditBox> nameBoxes = new ArrayList<>();

    // Layout bases
    private int cx;
    private int top;

    public GauntletWavesScreen(GauntletEditScreen parent, BlockPos pos, List<String> waveNames) {
        super(Component.literal("Gauntlet Block: Edit Waves"));
        this.parent = parent;
        this.pos = pos.immutable();
        this.names = new ArrayList<>(waveNames != null ? waveNames : List.of("Boss"));
        if (this.names.isEmpty()) this.names.add("Boss");
        if (this.names.size() > MAX_WAVES) this.names.subList(MAX_WAVES, this.names.size()).clear();
    }

    @Override
    protected void init() {
        this.cx = (this.width / 2);
        // Start near the top of the screen to keep all controls visible even with 5 waves
        this.top = 30;
        rebuildRows();

        int y = top + 28 + rowsHeight();
        // Add Wave button
        this.addRenderableWidget(Button.builder(Component.literal("Add Wave"), b -> {
            if (names.size() < MAX_WAVES) {
                names.add(names.size() == MAX_WAVES - 1 ? "Boss" : defaultName(names.size()));
                rebuildAndRefocus();
            }
        }).bounds(cx - 100, y, 200, 20).build());
        y += 30;

        // Edit General (back to parent without applying changes)
        this.addRenderableWidget(Button.builder(Component.literal("Edit General"), b -> this.minecraft.setScreen(parent))
                .bounds(cx - 100, y, 200, 20).build());
        y += 30;

        // Done
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            applyToNamesFromBoxes();
            parent.setWaveNamesFromChild(names);
            this.minecraft.setScreen(parent);
            parent.onWaveNamesUpdated();
        }).bounds(cx - 100, y, 100, 20).build());

        // Cancel
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> this.minecraft.setScreen(parent))
                .bounds(cx, y, 100, 20).build());
    }

    private int rowsHeight() { return names.size() * (20 + 12); }

    private String defaultName(int idx) { return idx == MAX_WAVES - 1 ? "Boss" : "Wave " + (idx + 1); }

    private void rebuildAndRefocus() {
        this.clearWidgets();
        this.nameBoxes.clear();
        init();
    }

    private void applyToNamesFromBoxes() {
        for (int i = 0; i < nameBoxes.size(); i++) {
            String s = nameBoxes.get(i).getValue().trim();
            if (s.isEmpty()) s = defaultName(i);
            names.set(i, s);
        }
    }

    private void rebuildRows() {
        nameBoxes.clear();
        int rowY = top + 20;
        for (int i = 0; i < names.size(); i++) {
            final int idx = i;
            int xLeft = cx - 180;

            EditBox box = new EditBox(this.font, xLeft + 46, rowY, 120, 20, Component.empty());
            box.setMaxLength(64);
            box.setValue(names.get(i));
            this.addRenderableWidget(box);
            nameBoxes.add(box);

            // Save
            this.addRenderableWidget(Button.builder(Component.literal("Save Wave"), b -> onSave(idx))
                    .bounds(xLeft + 46 + 120 + 6, rowY, 70, 20).build());
            // Load
            this.addRenderableWidget(Button.builder(Component.literal("Load Wave"), b -> onLoad(idx))
                    .bounds(xLeft + 46 + 120 + 6 + 70 + 6, rowY, 70, 20).build());

            // Up / Down / Delete
            int btnX = xLeft + 46 + 120 + 6 + 70 + 6 + 70 + 8;
            Button up = Button.builder(Component.literal("∧"), b -> move(idx, -1)).bounds(btnX, rowY, 20, 20).build();
            Button down = Button.builder(Component.literal("∨"), b -> move(idx, +1)).bounds(btnX + 24, rowY, 20, 20).build();
            Button del = Button.builder(Component.literal("✖"), b -> remove(idx)).bounds(btnX + 48, rowY, 20, 20).build();
            up.active = (idx > 0);
            down.active = (idx < names.size() - 1);
            del.active = (names.size() > 1);
            this.addRenderableWidget(up);
            this.addRenderableWidget(down);
            this.addRenderableWidget(del);

            rowY += 20 + 12;
        }
    }

    private void move(int idx, int delta) {
        int j = idx + delta;
        if (j < 0 || j >= names.size()) return;
        applyToNamesFromBoxes();
        Collections.swap(names, idx, j);
        rebuildAndRefocus();
    }

    private void remove(int idx) {
        if (names.size() <= 1) return;
        applyToNamesFromBoxes();
        names.remove(idx);
        rebuildAndRefocus();
    }

    private void onSave(int idx) {
        // Placeholder to save current wave to disk
    }

    private void onLoad(int idx) {
        // Placeholder to load current wave to disk
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, cx, top - 10, 0xFFFFFF);

        int labelColor = 0xA0A0A0;
        // Wave labels
        for (int i = 0; i < names.size(); i++) {
            int rowY = top + 20 + i * (20 + 12); // y of the EditBox
            String wave = "Wave " + (i + 1) + ":";
            g.drawString(this.font, Component.literal(wave), cx - 140 - this.font.width(wave), rowY + 5, labelColor);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }
}
