package dev.hexnowloading.dungeonnowloading.screen;

import dev.hexnowloading.dungeonnowloading.data.WaveIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class GauntletWavesScreen extends Screen {
    private static final int MAX_WAVES = 5;
    private static final int MAX_SUGGESTIONS = 10;

    // Model: simple list of wave names (labels). Starts empty.
    private final List<String> waves = new ArrayList<>();

    // Row widgets are rebuilt whenever model changes
    private final List<Button> rowButtons = new ArrayList<>();

    // Footer buttons
    private Button addWaveBtn;
    private Button doneBtn;
    private Button cancelBtn;

    // Popup/modal state
    private boolean popupOpen = false;
    private boolean popupIsLoad = true; // if false -> Save
    private int popupWaveIndex = -1;
    private EditBox popupSearch;
    private Button popupOkBtn;
    private Button popupCancelBtn;
    private List<String> popupAllEntries = new ArrayList<>();
    private List<String> popupFiltered = new ArrayList<>();
    private int popupSelected = -1; // index in filtered
    private int popupScroll = 0; // simple wheel scroll over filtered list

    private List<WaveIO.MobNode> nodes = new ArrayList<>();

    public GauntletWavesScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        this.clearWidgets();
        rowButtons.clear();

        int startY = 50;
        int rowH = 22;
        int rowGap = 4;

        // Build rows for existing waves
        for (int i = 0; i < waves.size(); i++) {
            buildRow(i, startY + i * (rowH + rowGap), rowH);
        }

        // Add button
        int btnW = 80, btnH = 20;
        int yFooter = this.height - 28;
        addWaveBtn = this.addRenderableWidget(Button.builder(Component.literal("Add Wave"), b -> {
            if (waves.size() < MAX_WAVES) {
                waves.add(""); // empty label to start
                rebuildRows();
            }
        }).bounds(20, yFooter, btnW, btnH).build());
        addWaveBtn.active = waves.size() < MAX_WAVES;

        // Done / Cancel
        doneBtn = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onDone()).bounds(this.width - 20 - btnW * 2 - 6, yFooter, btnW, btnH).build());
        cancelBtn = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> this.onCancel()).bounds(this.width - 20 - btnW, yFooter, btnW, btnH).build());

        // Popup controls created only when needed
        if (popupOpen) buildPopupWidgets();
    }

    private void rebuildRows() {
        // Re-init to rebuild all widgets
        this.init();
    }

    private void buildRow(int index, int y, int h) {
        // Layout: [Label area] [Load][Save][Down][Up][Delete]
        int left = 20;
        int nameAreaW = Math.max(120, this.width / 2 - 80);
        String name = labelFor(index);

        // We render the label manually in render(); no widget here for the label

        int x = left + nameAreaW + 6;
        int bw = 44;
        int smallW = 24;
        int delW = 60;

        Button load = Button.builder(Component.literal("Load"), b -> openLoadPopup(index)).bounds(x, y, bw, h).build();
        x += bw + 4;
        Button save = Button.builder(Component.literal("Save"), b -> openSavePopup(index)).bounds(x, y, bw, h).build();
        x += bw + 4;
        Button down = Button.builder(Component.literal("↓"), b -> moveDown(index)).bounds(x, y, smallW, h).build();
        down.active = index < waves.size() - 1;
        x += smallW + 4;
        Button up = Button.builder(Component.literal("↑"), b -> moveUp(index)).bounds(x, y, smallW, h).build();
        up.active = index > 0;
        x += smallW + 4;
        Button del = Button.builder(Component.literal("Delete"), b -> deleteRow(index)).bounds(x, y, delW, h).build();

        this.addRenderableWidget(load);
        this.addRenderableWidget(save);
        this.addRenderableWidget(down);
        this.addRenderableWidget(up);
        this.addRenderableWidget(del);

        // Track for later enable/disable if we need
        rowButtons.add(load);
        rowButtons.add(save);
        rowButtons.add(down);
        rowButtons.add(up);
        rowButtons.add(del);
    }

    private String labelFor(int i) {
        String n = (i >= 0 && i < waves.size()) ? waves.get(i) : "";
        if (n == null || n.isBlank()) return "<empty>";
        return n;
    }

    private void moveUp(int i) {
        if (i <= 0 || i >= waves.size()) return;
        Collections.swap(waves, i, i - 1);
        rebuildRows();
    }

    private void moveDown(int i) {
        if (i < 0 || i >= waves.size() - 1) return;
        Collections.swap(waves, i, i + 1);
        rebuildRows();
    }

    private void deleteRow(int i) {
        if (i < 0 || i >= waves.size()) return;
        waves.remove(i);
        rebuildRows();
    }

    private void openLoadPopup(int waveIndex) {
        popupOpen = true;
        popupIsLoad = true;
        popupWaveIndex = waveIndex;
        popupSelected = -1;
        popupScroll = 0;
        popupAllEntries = listAvailableWaves();
        if (popupAllEntries == null) popupAllEntries = List.of();
        popupFiltered = new ArrayList<>(popupAllEntries);
//        disableUnderlying(true);
        buildPopupWidgets();
    }

    private void openSavePopup(int waveIndex) {
        popupOpen = true;
        popupIsLoad = false;
        popupWaveIndex = waveIndex;
        popupSelected = -1;
        popupScroll = 0;
        popupAllEntries = listAvailableWaves();
        if (popupAllEntries == null) popupAllEntries = List.of();
        popupFiltered = new ArrayList<>(popupAllEntries);
//        disableUnderlying(true);
        buildPopupWidgets();
        // Pre-fill with current wave label
        if (popupSearch != null) popupSearch.setValue(safeGet(waves, waveIndex));
    }

    private static String safeGet(List<String> list, int idx) {
        if (idx < 0 || idx >= list.size()) return "";
        return Objects.toString(list.get(idx), "");
    }

    private void closePopup() {
        popupOpen = false;
        popupWaveIndex = -1;
        popupSelected = -1;
        popupAllEntries = List.of();
        popupFiltered = List.of();
//        disableUnderlying(false);
        rebuildRows();
    }

    private void buildPopupWidgets() {
        // Remove existing popup widgets (search/ok/cancel) if any by reinitializing them
        if (popupSearch != null) {
            // nothing specific to remove; init() clears all anyway when called
        }
        int panelW = Math.min(300, this.width - 40);
        int panelH = Math.min(200, this.height - 60);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Search box at top inside popup
        popupSearch = new EditBox(this.font, panelX + 10, panelY + 28, panelW - 20, 18, Component.literal("Search"));
        popupSearch.setMaxLength(64);
        popupSearch.setResponder(s -> updatePopupFilter());
        this.addRenderableWidget(popupSearch);

        // OK / Cancel
        int btnW = 70, btnH = 20;
        popupOkBtn = this.addRenderableWidget(Button.builder(popupIsLoad ? Component.literal("Load") : Component.literal("Save"), b -> onPopupConfirm()).bounds(panelX + panelW - btnW * 2 - 10 - 6, panelY + panelH - btnH - 10, btnW, btnH).build());
        popupCancelBtn = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> closePopup()).bounds(panelX + panelW - btnW - 10, panelY + panelH - btnH - 10, btnW, btnH).build());

        updatePopupFilter();
        if (!popupIsLoad) {
            // For Save, enable OK if there is a non-blank name in the box
            popupOkBtn.active = popupSearch.getValue() != null && !popupSearch.getValue().isBlank();
        }
    }

    private void updatePopupFilter() {
        String q = popupSearch != null ? popupSearch.getValue().trim().toLowerCase(Locale.ROOT) : "";
        List<String> base = popupAllEntries != null ? popupAllEntries : List.of();
        popupFiltered = base.stream()
                .filter(s -> q.isEmpty() || s.toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        popupSelected = -1;
        popupScroll = 0;
        if (popupOkBtn != null && popupIsLoad) {
            // Load requires a selection
            popupOkBtn.active = popupSelected >= 0 && popupSelected < popupFiltered.size();
        }
    }

    private void onPopupConfirm() {
        if (popupWaveIndex < 0 || popupWaveIndex >= waves.size()) {
            closePopup();
            return;
        }
        if (popupIsLoad) {
            // Require a selection
            if (popupSelected >= 0 && popupSelected < popupFiltered.size()) {
                String chosen = popupFiltered.get(popupSelected);
                waves.set(popupWaveIndex, chosen);
                List<WaveIO.MobNode> loaded = WaveIO.load(chosen);
                if (loaded != null) nodes = loaded;
            }
        } else {
            // Save placeholder: just take the search box content as the name
            String name = popupSearch != null ? popupSearch.getValue().trim() : "";
            if (!name.isBlank()) {
                waves.set(popupWaveIndex, name);
                WaveIO.save(name, nodes);
            }
        }
        closePopup();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        // Title
        g.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Headers or hint
        int left = 20;
        int nameAreaW = Math.max(120, this.width / 2 - 80);
        g.drawString(this.font, Component.literal("Wave Name"), left, 36, 0xA0A0A0, false);

        // Draw row labels
        int startY = 50;
        int rowH = 22;
        int rowGap = 4;
        for (int i = 0; i < waves.size(); i++) {
            int y = startY + i * (rowH + rowGap);
            String label = labelFor(i);
            g.drawString(this.font, Component.literal("" + (i + 1) + ") " + label), left, y + (rowH - this.font.lineHeight) / 2, 0xFFFFFF, false);
        }

        if (waves.isEmpty()) {
            g.drawCenteredString(this.font, Component.literal("No waves. Click 'Add Wave' to begin."), this.width / 2, startY + 4, 0xAAAAAA);
        }

        // Popup overlay
        if (popupOpen) {
            renderPopup(g, mouseX, mouseY, partialTick);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPopup(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        int panelW = Math.min(300, this.width - 40);
        int panelH = Math.min(200, this.height - 60);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Panel background
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF101010);

        // Title
        String title = popupIsLoad ? "Load Wave" : "Save Wave";
        g.drawCenteredString(this.font, Component.literal(title), panelX + panelW / 2, panelY + 8, 0xFFFFFF);

        // Search box already rendered by super, but draw label
        g.drawString(this.font, Component.literal("Search:"), panelX + 10, panelY + 18, 0xA0A0A0, false);

        // List area
        int listX = panelX + 10;
        int listY = panelY + 52;
        int listW = panelW - 20;
        int listH = panelH - 52 - 40; // leave space for buttons

        // Border
        g.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, 0xFF303030);
        g.fill(listX, listY, listX + listW, listY + listH, 0xFF202020);

        // Clamp scroll and selection enable
        int visible = Math.max(1, Math.min(MAX_SUGGESTIONS, listH / (this.font.lineHeight + 4)));
        int maxStart = Math.max(0, Math.max(0, popupFiltered.size() - visible));
        if (popupScroll > maxStart) popupScroll = maxStart;
        if (popupScroll < 0) popupScroll = 0;

        // Draw items
        int y = listY + 2;
        for (int i = 0; i < visible && (i + popupScroll) < popupFiltered.size(); i++) {
            int idx = i + popupScroll;
            String entry = popupFiltered.get(idx);
            int itemH = this.font.lineHeight + 4;
            int y0 = y + i * itemH;
            boolean selected = idx == popupSelected;
            int bg = selected ? 0xFF3A3A60 : 0x00000000;
            if (bg != 0) g.fill(listX + 1, y0, listX + listW - 1, y0 + itemH, bg);
            g.drawString(this.font, Component.literal(entry), listX + 4, y0 + 2, 0xFFFFFF, false);
        }

        // Footer hint
        if (popupIsLoad) {
            boolean en = popupSelected >= 0 && popupSelected < popupFiltered.size();
            if (popupOkBtn != null) popupOkBtn.active = en;
        } else {
            boolean en = popupSearch != null && popupSearch.getValue() != null && !popupSearch.getValue().isBlank();
            if (popupOkBtn != null) popupOkBtn.active = en;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popupOpen) {
            // Handle list selection within popup
            int panelW = Math.min(300, this.width - 40);
            int panelH = Math.min(200, this.height - 60);
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;

            int listX = panelX + 10;
            int listY = panelY + 52;
            int listW = panelW - 20;
            int listH = panelH - 52 - 40;

            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                int visible = Math.max(1, Math.min(MAX_SUGGESTIONS, listH / (this.font.lineHeight + 4)));
                int relY = (int) mouseY - listY;
                int indexInView = relY / (this.font.lineHeight + 4);
                if (indexInView >= 0 && indexInView < visible) {
                    int idx = popupScroll + indexInView;
                    if (idx >= 0 && idx < popupFiltered.size()) {
                        popupSelected = idx;
                        if (!popupIsLoad) {
                            // Fill search field with clicked name for save convenience
                            if (popupSearch != null) popupSearch.setValue(popupFiltered.get(idx));
                        }
                        return true;
                    }
                }
            }
            // Let super handle buttons/editbox
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (popupOpen) {
            int panelW = Math.min(300, this.width - 40);
            int panelH = Math.min(200, this.height - 60);
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;

            int listX = panelX + 10;
            int listY = panelY + 52;
            int listW = panelW - 20;
            int listH = panelH - 52 - 40;
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                int visible = Math.max(1, Math.min(MAX_SUGGESTIONS, listH / (this.font.lineHeight + 4)));
                int maxStart = Math.max(0, Math.max(0, popupFiltered.size() - visible));
                popupScroll -= (delta > 0 ? 1 : -1);
                if (popupScroll < 0) popupScroll = 0;
                if (popupScroll > maxStart) popupScroll = maxStart;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (popupOpen) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                closePopup();
                return true;
            }
        } else {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                this.onCancel();
                return true;
            }
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public void tick() {
        if (popupSearch != null) popupSearch.tick();
        super.tick();
    }

    private void onDone() {
        // For now, just close the screen. Real integration can send to server or persist as needed.
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }

    private void onCancel() {
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------- Wave listing helpers -------------

    private List<String> listAvailableWaves() {
        File dir = resolveWavesDir();
        if (dir == null) return List.of();
        if (!dir.exists() || !dir.isDirectory()) return List.of();
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return List.of();

        List<String> names = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) {
                names.add(f.getName());
            } else if (f.isFile()) {
                String n = f.getName();
                if (n.toLowerCase(Locale.ROOT).endsWith(".json")) {
                    names.add(n.substring(0, n.length() - 5));
                }
            }
        }
        // De-dup and sort
        return names.stream().distinct().sorted().collect(Collectors.toList());
    }

    private File resolveWavesDir() {
        // Prefer current world root if in singleplayer/integrated
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            MinecraftServer server = mc.getSingleplayerServer();
            if (server != null) {
                try {
                    File worldRoot = server.getWorldPath(LevelResource.ROOT).toFile();
                    return new File(worldRoot, "dnl/waves");
                } catch (Throwable ignored) { }
            }
            // Fallback to game directory
            try {
                File gameDir = mc.gameDirectory;
                return new File(gameDir, "dnl/waves");
            } catch (Throwable ignored) { }
        }
        return null;
    }
}
