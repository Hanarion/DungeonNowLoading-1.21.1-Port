package dev.hexnowloading.dungeonnowloading.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * DEV-ONLY diagnostic + one-time datafix for structure templates.
 *
 * /dnl dev structures report        -> log palette/jigsaw counts per template (does the old-format
 *                                      parse correctly? are jigsaw blocks present?)
 * /dnl dev structures datafix       -> load each template via the game parser, re-save as current
 *                                      NBT into ./dnl_datafix_structures (no source overwrite)
 *
 * Not shipped to players (permission 2, dev-only).
 */
public class DevStructureDatafixCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(DungeonNowLoading.MOD_ID)
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("dev")
                        .then(Commands.literal("structures")
                                .then(Commands.literal("report").executes(DevStructureDatafixCommand::report))
                                .then(Commands.literal("datafix").executes(DevStructureDatafixCommand::datafix)))));
    }

    private static List<ResourceLocation> dnlTemplates(StructureTemplateManager mgr) {
        List<ResourceLocation> out = new ArrayList<>();
        mgr.listTemplates().filter(rl -> DungeonNowLoading.MOD_ID.equals(rl.getNamespace())).forEach(out::add);
        return out;
    }

    private static int report(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        StructureTemplateManager mgr = level.getStructureManager();
        var src = ctx.getSource();
        int checked = 0, empty = 0, zeroSize = 0;
        for (ResourceLocation rl : dnlTemplates(mgr)) {
            checked++;
            StructureTemplate t = mgr.get(rl).orElse(null);
            if (t == null) { empty++; continue; }
            var size = t.getSize();
            boolean parsed = size.getX() > 0 || size.getY() > 0 || size.getZ() > 0;
            if (!parsed) zeroSize++;
            ResourceLocation rl1 = rl; var sz = size; boolean ok = parsed;
            if (checked <= 60) {
                src.sendSuccess(() -> Component.literal(rl1 + " size=" + sz.getX() + "x" + sz.getY() + "x" + sz.getZ() + " parsed=" + ok), false);
            }
        }
        final int c = checked, e = empty, z = zeroSize;
        src.sendSuccess(() -> Component.literal("Checked " + c + " templates; missing=" + e + " unparsed=" + z), true);
        return c;
    }

    private static int datafix(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        StructureTemplateManager mgr = level.getStructureManager();
        // Write into the game's run directory (dev: neoforge/run). Copy these over the source
        // structures/ dir manually after verifying.
        Path outDir = Path.of("dnl_datafix_structures");
        var src = ctx.getSource();
        int ok = 0, fail = 0;
        for (ResourceLocation rl : dnlTemplates(mgr)) {
            try {
                StructureTemplate t = mgr.get(rl).orElse(null);
                if (t == null) { fail++; continue; }
                CompoundTag saved = t.save(new CompoundTag());
                Path rel = outDir.resolve(rl.getNamespace()).resolve(rl.getPath() + ".nbt");
                Files.createDirectories(rel.getParent());
                NbtIo.writeCompressed(saved, rel);
                ok++;
            } catch (Throwable th) {
                fail++;
                final ResourceLocation frl = rl;
                final String msg = th.toString();
                src.sendFailure(Component.literal("FAIL " + frl + ": " + msg));
            }
        }
        final int o = ok, f = fail;
        src.sendSuccess(() -> Component.literal("Datafix ok=" + o + " fail=" + f + " -> " + outDir.toAbsolutePath()), true);
        return ok;
    }
}
