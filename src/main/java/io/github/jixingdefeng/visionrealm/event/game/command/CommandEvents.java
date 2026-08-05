package io.github.jixingdefeng.visionrealm.event.game.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jixingdefeng.visionrealm.command.IncidentCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CommandEvents {

    public static void onRegisterCommand(final RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        IncidentCommand.register(dispatcher);
    }
}
