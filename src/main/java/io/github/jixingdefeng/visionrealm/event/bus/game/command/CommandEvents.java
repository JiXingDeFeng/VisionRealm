package io.github.jixingdefeng.visionrealm.event.bus.game.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jixingdefeng.visionrealm.core.command.IncidentCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CommandEvents {

    @SubscribeEvent
    public static void onRegisterCommand(final RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        IncidentCommand.register(dispatcher);
    }
}
