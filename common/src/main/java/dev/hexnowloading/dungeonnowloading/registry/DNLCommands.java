package dev.hexnowloading.dungeonnowloading.registry;

import com.mojang.brigadier.CommandDispatcher;
import dev.hexnowloading.dungeonnowloading.command.StatueCommand;
import net.minecraft.commands.CommandSourceStack;

public class DNLCommands {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        StatueCommand.register(dispatcher);
        // add more commands here later
    }
}
