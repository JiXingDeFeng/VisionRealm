package io.github.jixingdefeng.visionrealm.event.game;

import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.content.data.packs.resources.block_erosion.BlockErosionReloadListener;
import io.github.jixingdefeng.visionrealm.content.data.packs.resources.particle.ParticleConfigReloadListener;
import io.github.jixingdefeng.visionrealm.content.registry.ModRegistry;
import io.github.jixingdefeng.visionrealm.core.incident.IncidentScheduler;
import io.github.jixingdefeng.visionrealm.event.events.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.jixingdefeng.visionrealm.event.game.command.CommandEvents;
import io.github.jixingdefeng.visionrealm.event.game.datagen.GatherDataEvents;
import io.github.jixingdefeng.visionrealm.event.game.network.PacketPayloadEvents;
import io.github.jixingdefeng.visionrealm.event.game.resource.BlockErosionEvents;
import io.github.jixingdefeng.visionrealm.event.game.world.dimension.DimensionEvents;
import io.github.jixingdefeng.visionrealm.event.game.world.entity.EntityEvents;
import io.github.jixingdefeng.visionrealm.event.game.world.entity.player.PlayerEvents;
import io.github.jixingdefeng.visionrealm.event.game.world.item.ItemEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = VisionRealm.MOD_ID)
public class GameBusEvents {

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        EntityEvents.onRegisterSpawnPlacements(event);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(final EntityJoinLevelEvent event) {
        EntityEvents.onEntityJoinLevel(event);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!event.isEndConquered() && player instanceof ServerPlayer serverPlayer) {
            PlayerEvents.initAttributes(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(final LivingDamageEvent.Post event) {
        EntityEvents.onEntityHurt(event);
    }

    @SubscribeEvent
    public static void onEntityHurt(final LivingDamageEvent.Pre event) {
        EntityEvents.onEntityHurt(event);
    }

    @SubscribeEvent
    public static void onEntityTick(final EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        EntityEvents.updateParticles(entity);
    }

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        PlayerEvents.updateAttribute(player);
    }

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Pre event) {
        IncidentScheduler.tick(event);
    }

    @SubscribeEvent
    public static void onNewRegistry(final DataPackRegistryEvent.NewRegistry event) {
        ModRegistry.registryDataPackRegistry(event);
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddReloadListenerEvent event) {
        RegistryAccess registryAccess = event.getRegistryAccess();
        event.addListener(new ParticleConfigReloadListener());
        event.addListener(new BlockErosionReloadListener(registryAccess));
    }

    @SubscribeEvent
    public static void onBlockErosionLoaderRegister(final BlockErosionLoaderRegisterEvent event) {
        BlockErosionEvents.onBlockErosionLoaderRegister(event);
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PacketPayloadEvents.registerPayloads(event);
    }

    @SubscribeEvent
    public static void onRegisterCommand(final RegisterCommandsEvent event) {
        CommandEvents.onRegisterCommand(event);
    }

    @SubscribeEvent
    public static void onRegistryDimensionSpecialEffects(final RegisterDimensionSpecialEffectsEvent event) {
        DimensionEvents.onRegistryDimensionSpecialEffects(event);
    }

    @SubscribeEvent
    public static void onRegisterCapabilitiesEvent(final RegisterCapabilitiesEvent event) {
        ItemEvents.registerCapabilities(event);
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        GatherDataEvents.addDimension(event);
    }
}
