package io.github.jixingdefeng.visionrealm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistries;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistry;
import io.github.jixingdefeng.visionrealm.core.incident.IncidentScheduler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class IncidentCommand {
    private static final String DEFAULT_MIN_INTERVAL = String.valueOf(IncidentScheduler.DEFAULT_MIN_INTERVAL);
    private static final String DEFAULT_MAX_INTERVAL = String.valueOf(IncidentScheduler.DEFAULT_MAX_INTERVAL);
    private static final DynamicCommandExceptionType ERROR_INVALID_INCIDENT =
            new DynamicCommandExceptionType(key -> Component.translatableEscape("visionrealm.command.incident.notFound", key));
    private static final DynamicCommandExceptionType ERROR_INVALID_LEVEL =
            new DynamicCommandExceptionType(key -> Component.translatableEscape("visionrealm.command.incident.level.notFound", key));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("incident")
                        .then(
                                Commands.literal("list")
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            source.sendSystemMessage(
                                                    Component.translatable("visionrealm.command.incident.list").withColor(0xFF00FF00)
                                            );
                                            for (ResourceLocation location : ModRegistry.INCIDENT.keySet()) {
                                                source.sendSystemMessage(
                                                        Component.literal("[" + location.toString() + "]")
                                                                .withColor(0xFF00FF00)
                                                );
                                            }

                                            return 0;
                                        })
                        )
                        .then(
                                Commands.literal("execute")
                                        .requires(source -> source.hasPermission(2))
                                        .then(
                                                Commands.argument("incident", ResourceKeyArgument.key(ModRegistries.INCIDENT))
                                                        .executes(IncidentCommand::executeAllLevel)
                                                        .then(
                                                                Commands.argument("dimension", ResourceKeyArgument.key(Registries.DIMENSION))
                                                                        .executes(IncidentCommand::execute)
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("handler")
                                        .requires(source -> source.hasPermission(2))
                                        .then(
                                                Commands.literal("execute")
                                                        .executes(context ->
                                                                IncidentScheduler.executeImmediately(context.getSource()
                                                                        .getServer()) ? 0 : 1
                                                        )
                                                        .then(
                                                                Commands.argument("dimension", ResourceKeyArgument.key(Registries.DIMENSION))
                                                                        .executes(IncidentCommand::handlerExecute)
                                                        )
                                        )
                                        .then(
                                                Commands.literal("reload")
                                                        .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            source.sendSystemMessage(Component.translatable(
                                                                    "visionrealm.command.incident.reload"
                                                            ));
                                                            return IncidentScheduler.reloadIncidents(source.getServer()) ? 0 : 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("interval")
                                                        .then(
                                                                Commands.literal("set")
                                                                        .then(
                                                                                Commands.argument("min", IntegerArgumentType.integer(0))
                                                                                        .suggests((context, builder) ->
                                                                                                SharedSuggestionProvider.suggest(new String[]{DEFAULT_MIN_INTERVAL}, builder)
                                                                                        )
                                                                                        .then(
                                                                                                Commands.argument("max", IntegerArgumentType.integer(0))
                                                                                                        .suggests((context, builder) ->
                                                                                                                SharedSuggestionProvider.suggest(new String[]{DEFAULT_MAX_INTERVAL}, builder)
                                                                                                        )
                                                                                                        .executes(context -> {
                                                                                                            CommandSourceStack source = context.getSource();
                                                                                                            int max = IntegerArgumentType.getInteger(context, "max");
                                                                                                            int min = IntegerArgumentType.getInteger(context, "min");
                                                                                                            IncidentScheduler.setInterval(min, max);
                                                                                                            source.sendSystemMessage(Component.translatable(
                                                                                                                    "visionrealm.command.incident.interval.set", min, max
                                                                                                            ));
                                                                                                            return 0;
                                                                                                        })
                                                                                        )
                                                                        )
                                                        )
                                                        .then(
                                                                Commands.literal("get")
                                                                        .executes(context -> {
                                                                            CommandSourceStack source = context.getSource();
                                                                            IntProvider provider = IncidentScheduler.getInterval();
                                                                            source.sendSystemMessage(Component.translatable(
                                                                                    "visionrealm.command.incident.interval.get",
                                                                                    provider.getMinValue(), provider.getMaxValue()
                                                                            ));
                                                                            return 0;
                                                                        })
                                                        )
                                        )
                        )
        );
    }

    private static int executeAllLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder<RegisteredIncident<?, ?>> incident = getHolder(context, "incident", ModRegistries.INCIDENT, ERROR_INVALID_INCIDENT);
        MinecraftServer server = context.getSource().getServer();
        return incident.value().execute(server, server.overworld().getRandom()) ? 0 : 1;
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder<RegisteredIncident<?, ?>> incident = getHolder(context, "incident", ModRegistries.INCIDENT, ERROR_INVALID_INCIDENT);
        ResourceKey<Level> levelKey = getResourceKey(context, "dimension", Registries.DIMENSION, ERROR_INVALID_LEVEL);
        ServerLevel serverLevel = context.getSource().getServer().getLevel(levelKey);
        if (serverLevel == null) {
            throw ERROR_INVALID_LEVEL.create(levelKey);
        } else {
            return incident.value().execute(serverLevel, serverLevel.getRandom()) ? 0 : 1;
        }
    }

    private static int handlerExecute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceKey<Level> levelKey = getResourceKey(context, "dimension", Registries.DIMENSION, ERROR_INVALID_LEVEL);
        ServerLevel serverLevel = context.getSource().getServer().getLevel(levelKey);
        if (serverLevel == null) {
            throw ERROR_INVALID_LEVEL.create(levelKey);
        } else {
            return IncidentScheduler.executeImmediately(serverLevel) ? 0 : 1;
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
