package io.github.jixingdefeng.visionrealm.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class IncidentCommand {
    private static final DynamicCommandExceptionType ERROR_INVALID_INCIDENT =
            new DynamicCommandExceptionType(key -> Component.translatableEscape("visionrealm.command.incident.notFound", key));
    private static final DynamicCommandExceptionType ERROR_INVALID_LEVEL =
            new DynamicCommandExceptionType(key -> Component.translatableEscape("visionrealm.command.incident.level.notFound", key));
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("incident")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("execute")
                                        .then(
                                                Commands.argument("incident", ResourceKeyArgument.key(ModRegistries.INCIDENT))
                                                        .executes(context ->
                                                                executeAllLevel(context, "incident")
                                                        )
                                                        .then(
                                                                Commands.argument("dimension", ResourceKeyArgument.key(Registries.DIMENSION))
                                                                        .executes(context ->
                                                                                execute(context, "incident", "dimension")
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int executeAllLevel(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
        RegisteredIncident<?, ?> incident = getHolder(context, argument, ModRegistries.INCIDENT, ERROR_INVALID_INCIDENT).value();
        MinecraftServer server = context.getSource().getServer();
        return incident.execute(server, server.overworld().getRandom()) ? 0 : 1;
    }

    private static int execute(CommandContext<CommandSourceStack> context, String argument, String dimension) throws CommandSyntaxException {
        RegisteredIncident<?, ?> incident = getHolder(context, argument, ModRegistries.INCIDENT, ERROR_INVALID_INCIDENT).value();
        ResourceKey<Level> levelKey = getResourceKey(context, dimension, Registries.DIMENSION, ERROR_INVALID_LEVEL);
        ServerLevel serverLevel = context.getSource().getServer().getLevel(levelKey);
        if (serverLevel == null) {
            throw ERROR_INVALID_LEVEL.create(levelKey);
        } else {
            return incident.execute(serverLevel, serverLevel.getRandom()) ? 0 : 1;
        }
    }

    private static <T> ResourceKey<T> getResourceKey(
            CommandContext<CommandSourceStack> context,
            String argument,
            ResourceKey<Registry<T>> registryKey,
            DynamicCommandExceptionType exception
    ) throws CommandSyntaxException {
        ResourceKey<?> resourceKey = context.getArgument(argument, ResourceKey.class);
        Optional<ResourceKey<T>> optional = resourceKey.cast(registryKey);
        return optional.orElseThrow(() -> exception.create(resourceKey));
    }

    private static <T> Holder<T> getHolder(
            CommandContext<CommandSourceStack> context,
            String argument,
            ResourceKey<Registry<T>> registryKey,
            DynamicCommandExceptionType exception
    ) throws CommandSyntaxException {
        ResourceKey<T> resourceKey = getResourceKey(context, argument, registryKey, exception);
        Registry<T> registry = context.getSource().getServer().registryAccess().registryOrThrow(registryKey);
        return registry.getHolder(resourceKey).orElseThrow(() -> exception.create(resourceKey));
    }
}
