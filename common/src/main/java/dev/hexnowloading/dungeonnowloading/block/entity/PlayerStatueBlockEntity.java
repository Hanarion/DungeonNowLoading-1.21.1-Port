package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.authlib.GameProfile;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerStatueBlockEntity extends BlockEntity {

    // --- config (match your UI) ---------------------------------------------
    public static final int LINES = 4;              // set to 1 if you want a single line plaque
    public static final int MAX_TEXT_LINE_WIDTH = 90;
    public static final int TEXT_LINE_HEIGHT = 10;

    // --- statue data ---------------------------------------------------------
    private @Nullable GameProfile owner;
    private int poseVariant = 0;

    // --- plaque text ---------------------------------------------------------
    private final Component[] text = new Component[]{Component.empty(), Component.empty(), Component.empty(), Component.empty()};
    private DyeColor textColor = DyeColor.BLACK;
    private boolean glowingText = false;

    // --- sign-like edit state ------------------------------------------------
    private @Nullable UUID allowedEditor;   // who may edit right now
    private boolean waxed = false;          // lock like waxed sign

    public PlayerStatueBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.PLAYER_STATUE.get(), pos, state);
    }


    // --- offering slot (single item) ---
    private ItemStack offering = ItemStack.EMPTY;

    public boolean hasOffering() { return !offering.isEmpty(); }
    public ItemStack getOfferingCopy() { return offering.copy(); }

    // ===== basic getters/setters ============================================

    public @Nullable GameProfile getOwner() { return owner; }
    public void setOwner(@Nullable GameProfile gp) { this.owner = gp; setChanged(); sync(); }

    public int getPoseVariant() { return poseVariant; }
    public void setPoseVariant(int v) { this.poseVariant = v; setChanged(); sync(); }

    public Component getLine(int i) { return text[i]; }
    public Component[] getLines() { return text; } // NOTE: array is mutable; treat as read-only outside!
    public DyeColor getTextColor() { return textColor; }
    public boolean isGlowingText() { return glowingText; }

    public int getMaxTextLineWidth() { return MAX_TEXT_LINE_WIDTH; }
    public int getTextLineHeight() { return TEXT_LINE_HEIGHT; }

    // Set a single line
    public void setLine(int i, Component c) {
        text[i] = Objects.requireNonNullElse(c, Component.empty());
        setChanged(); sync();
    }

    // Replace all lines + style
    public void setAllText(List<Component> lines, DyeColor color, boolean glow) {
        for (int i = 0; i < LINES; i++) {
            Component c = (i < lines.size() && lines.get(i) != null) ? lines.get(i) : Component.empty();
            text[i] = c;
        }
        textColor = (color != null) ? color : DyeColor.BLACK;
        glowingText = glow;
        setChanged(); sync();
    }

    // ===== sign-like edit/wax helpers =======================================

    public boolean isWaxed() { return waxed; }
    public boolean setWaxed(boolean wax) {
        if (this.waxed != wax) {
            this.waxed = wax;
            setChanged(); sync();
            return true;
        }
        return false;
    }

    public void setAllowedEditor(@Nullable UUID id) { this.allowedEditor = id; setChanged(); }
    public @Nullable UUID getAllowedEditor() { return allowedEditor; }

    public boolean playerIsTooFarAwayToEdit(UUID playerId) {
        if (level == null) return true;
        var p = level.getPlayerByUUID(playerId);
        return p == null || p.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) > 64.0;
    }

    /** Server tick: clear editor if they walked away. Hook via block#getTicker. */
    public static void serverTick(Level lvl, BlockPos pos, BlockState state, PlayerStatueBlockEntity be) {
        if (!(lvl instanceof ServerLevel)) return;
        UUID lock = be.allowedEditor;
        if (lock != null && be.playerIsTooFarAwayToEdit(lock)) {
            be.setAllowedEditor(null);
        }
    }

    /**
     * Called from your C2S packet on close/Done. Mirrors SignBlockEntity’s safety checks.
     * Applies text and unlocks the editor.
     */
    public void applyTextUpdateFromClient(ServerPlayer sender, List<Component> newLines, DyeColor color, boolean glow) {
        if (!(level instanceof ServerLevel)) return;
        if (isWaxed()) { sender.displayClientMessage(Component.literal("Statue is waxed"), false); return; }
        if (!sender.getUUID().equals(allowedEditor)) {
            sender.displayClientMessage(Component.literal("No edit lock / wrong editor"), false);
            return;
        }
        if (playerIsTooFarAwayToEdit(sender.getUUID())) {
            sender.displayClientMessage(Component.literal("Too far to edit"), false);
            return;
        }

        setAllText(newLines, color, glow);
        setAllowedEditor(null);
        setChanged(); sync();
    }

    // ===== saving / syncing ==================================================

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        // owner
        if (owner != null) {
            CompoundTag o = new CompoundTag();
            NbtUtils.writeGameProfile(o, owner);
            tag.put("Owner", o);
        }

        // pose
        tag.putInt("PoseVariant", poseVariant);

        // text
        for (int i = 0; i < LINES; i++) {
            tag.putString("Text" + (i + 1), Component.Serializer.toJson(text[i]));
        }
        tag.putString("TextColor", textColor.getName());
        tag.putBoolean("TextGlowing", glowingText);

        // sign-like extras
        if (allowedEditor != null) tag.putUUID("AllowedEditor", allowedEditor);
        tag.putBoolean("Waxed", waxed);

        // tier only (no Offering tag)
        tag.putString("NotchTier", notchTier.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // owner
        if (tag.contains("Owner", 10)) {
            owner = NbtUtils.readGameProfile(tag.getCompound("Owner"));
        } else {
            owner = null;
        }

        // pose
        poseVariant = tag.getInt("PoseVariant");

        // text
        for (int i = 0; i < LINES; i++) {
            String key = "Text" + (i + 1);
            text[i] = tag.contains(key, 8) ? Component.Serializer.fromJson(tag.getString(key)) : Component.empty();
        }
        if (tag.contains("TextColor", 8)) {
            try { textColor = DyeColor.byName(tag.getString("TextColor"), DyeColor.BLACK); } catch (Exception ignored) {}
        }
        glowingText = tag.getBoolean("TextGlowing");

        // sign-like extras
        allowedEditor = (tag.contains("AllowedEditor", 11) || tag.contains("AllowedEditor", 12))
                ? tag.getUUID("AllowedEditor") : null;
        waxed = tag.getBoolean("Waxed");

        // tier
        this.notchTier = NotchTier.fromString(tag.getString("NotchTier"));

        // 🔁 Back-compat: if a legacy "Offering" tag exists, derive the tier once.
        if (this.notchTier == NotchTier.NONE && tag.contains("Offering", 10)) {
            ItemStack legacy = ItemStack.of(tag.getCompound("Offering"));
            NotchTier legacyTier = tierFromItem(legacy);
            if (legacyTier != NotchTier.NONE) this.notchTier = legacyTier;
        }
    }

    // client sync
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    /** Sends a block update to clients (server only). */
    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static NotchTier tierFromItem(ItemStack s) {
        if (s.is(Items.COPPER_INGOT))  return NotchTier.COPPER;
        if (s.is(Items.IRON_INGOT))    return NotchTier.IRON;
        if (s.is(Items.GOLD_INGOT))    return NotchTier.GOLD;
        if (s.is(Items.DIAMOND))       return NotchTier.DIAMOND;
        return NotchTier.NONE;
    }

    public static ItemStack defaultItemForTier(NotchTier t) {
        return switch (t) {
            case COPPER  -> new ItemStack(Items.COPPER_INGOT);
            case IRON    -> new ItemStack(Items.IRON_INGOT);
            case GOLD    -> new ItemStack(Items.GOLD_INGOT);
            case DIAMOND -> new ItemStack(Items.DIAMOND);
            default      -> ItemStack.EMPTY;
        };
    }

    @Nullable
    public String ensureServerSideOwnerName() {
        if (owner == null) return null;
        if (owner.getName() != null && !owner.getName().isEmpty()) return owner.getName();
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl)) return null;
        java.util.UUID id = owner.getId();
        if (id == null) return null;

        var cache = sl.getServer().getProfileCache();
        if (cache != null) {
            var opt = cache.get(id); // Optional<GameProfile>
            if (opt.isPresent()) {
                this.owner = opt.get();    // update BE with the named profile
                setChanged();
                return this.owner.getName();
            }
        }
        return null;
    }

    /** Short hint like 8 chars of UUID when name is unknown. */
    public static String shortUuid(@Nullable java.util.UUID id) {
        return (id == null) ? "Someone" : id.toString().substring(0, 8);
    }

    public boolean isOccupied() {
        return notchTier != NotchTier.NONE;
    }

    public boolean placeMaterial(ItemStack oneItem) {
        NotchTier t = tierFromItem(oneItem);
        if (t == NotchTier.NONE) return false;
        setNotchTier(t); // will setChanged() + sync()
        return true;
    }

    public ItemStack takeMaterial() {
        if (!isOccupied()) return ItemStack.EMPTY;
        ItemStack out = defaultItemForTier(notchTier);
        setNotchTier(NotchTier.NONE); // will setChanged() + sync()
        return out;
    }

    private NotchTier notchTier = NotchTier.NONE;

    public NotchTier getNotchTier() { return notchTier; }
    public void setNotchTier(NotchTier tier) {
        if (tier == null) tier = NotchTier.NONE;
        if (tier != this.notchTier) {
            this.notchTier = tier;
            setChanged(); sync(); // make sure clients update
        }
    }

    public enum NotchTier {
        NONE, COPPER, IRON, GOLD, DIAMOND;

        public static NotchTier fromString(String s) {
            if (s == null) return NONE;
            try { return valueOf(s.toUpperCase()); } catch (Exception e) { return NONE; }
        }
    }
}
