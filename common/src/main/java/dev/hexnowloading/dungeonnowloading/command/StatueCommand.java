package dev.hexnowloading.dungeonnowloading.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.supporter.PatronRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public final class StatueCommand {
    private StatueCommand() {}

    // Keep the list here (or read keys from PatronRegistry.DATA if you expose them)
    private static final String CAMPAIGN_TOD = "temple_of_duality";
    private static final String CAMPAIGN_LAB = "labyrinth";

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_CAMPAIGNS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    java.util.List.of(CAMPAIGN_TOD, CAMPAIGN_LAB), builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("statue")
                .requires(src -> src.hasPermission(2))

                // /statue <campaign>
                .then(Commands.argument("campaign", StringArgumentType.word())
                        .suggests(SUGGEST_CAMPAIGNS)

                        // /statue <campaign>  -> yourself
                        .executes(ctx -> {
                            ServerPlayer giver = ctx.getSource().getPlayerOrException();
                            String campaign = StringArgumentType.getString(ctx, "campaign");

                            giveForProfile(ctx.getSource(), giver, campaign, giver.getGameProfile());
                            return 1;
                        })

                        // /statue <campaign> <player>
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer giver = ctx.getSource().getPlayerOrException();
                                    String campaign = StringArgumentType.getString(ctx, "campaign");
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

                                    giveForProfile(ctx.getSource(), giver, campaign, target.getGameProfile());
                                    return 1;
                                })
                        )

                        // /statue <campaign> name <patronName>
                        .then(Commands.literal("name")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer giver = ctx.getSource().getPlayerOrException();
                                            String campaign = StringArgumentType.getString(ctx, "campaign");
                                            String name = StringArgumentType.getString(ctx, "name");

                                            giveForName(ctx.getSource(), giver, campaign, name);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    private static void giveForProfile(CommandSourceStack src, ServerPlayer receiver, String campaign, GameProfile owner) {
        // Find patron entry in THIS campaign by UUID to compute notch tier
        PatronRegistry.Patron patron = (owner != null && owner.getId() != null)
                ? PatronRegistry.findByUuid(campaign, owner.getId())
                : null;

        PlayerStatueBlockEntity.NotchTier tier = (patron != null)
                ? PatronRegistry.tierFor(patron)
                : PlayerStatueBlockEntity.NotchTier.NONE;

        ItemStack stack = createStatueStack(owner, tier, /*pose*/ 0);

        boolean added = receiver.getInventory().add(stack);
        if (!added) receiver.drop(stack, false);

        String label = (owner != null && owner.getName() != null && !owner.getName().isBlank()) ? owner.getName() : "that player";
        src.sendSuccess(() -> Component.literal("Gave a " + campaign + " statue of " + label + "."), false);
    }

    private static void giveForName(CommandSourceStack src, ServerPlayer receiver, String campaign, String name) {
        PatronRegistry.Patron patron = PatronRegistry.findByName(campaign, name);

        GameProfile gp;
        PlayerStatueBlockEntity.NotchTier tier;

        if (patron != null && patron.uuid != null) {
            String display = (patron.name != null && !patron.name.isBlank()) ? patron.name : name;
            gp = new GameProfile(patron.uuid, display);
            tier = PatronRegistry.tierFor(patron);
        } else {
            gp = resolveProfile(src.getServer(), name);
            tier = PlayerStatueBlockEntity.NotchTier.NONE; // not a patron for that campaign
        }

        ItemStack stack = createStatueStack(gp, tier, /*pose*/ 0);

        boolean added = receiver.getInventory().add(stack);
        if (!added) receiver.drop(stack, false);

        String label = (gp.getName() != null && !gp.getName().isBlank()) ? gp.getName() : name;
        src.sendSuccess(() -> Component.literal("Gave a " + campaign + " statue of " + label + "."), false);
    }

    /**
     * Writes EXACT tags that PlayerStatueBlock#setPlacedBy reads:
     * - Owner (compound) or SkullOwner
     * - DNL_Notch
     * (and DNL_Pose if you later make setPlacedBy read it)
     */
    public static ItemStack createStatueStack(GameProfile owner,
                                              PlayerStatueBlockEntity.NotchTier tier,
                                              int poseVariant) {
        ItemStack stack = new ItemStack(DNLBlocks.PLAYER_STATUE.get());
        CompoundTag tag = StackNbt.getOrCreateTag(stack);

        if (owner != null) {
            CompoundTag ownerTag = new CompoundTag();
            NbtUtils.writeGameProfile(ownerTag, owner);
            tag.put("Owner", ownerTag);

            if (owner.getName() != null && !owner.getName().isBlank()) {
                tag.putString("SkullOwner", owner.getName()); // for item display name
            } else if (owner.getId() != null) {
                tag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), owner));
            }
        } else {
            tag.putString("SkullOwner", "MHF_Alex");
        }

        PlayerStatueBlockEntity.NotchTier safe = (tier == null) ? PlayerStatueBlockEntity.NotchTier.NONE : tier;
        tag.putString("DNL_Notch", safe.name());

        // Optional: you already store this on pick-block; setPlacedBy doesn’t read it yet.
        tag.putInt("DNL_Pose", poseVariant);

        StackNbt.setTag(stack, tag);
        return stack;
    }

    // ---- offline/patron fallback profile resolution ----
    private static GameProfile resolveProfile(MinecraftServer server, String inputName) {
        String name = inputName.trim();

        var cache = server.getProfileCache();
        if (cache != null) {
            Optional<GameProfile> cached = cache.get(name);
            if (cached.isPresent()) return cached.get();
        }

        UUID parsed = tryParseUuid(name);
        if (parsed != null) {
            if (cache != null) {
                Optional<GameProfile> byId = cache.get(parsed);
                if (byId.isPresent()) return byId.get();
            }
            return new GameProfile(parsed, name);
        }

        UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return new GameProfile(offline, name);
    }

    @Nullable
    private static UUID tryParseUuid(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }
}