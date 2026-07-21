package io.github.jixingdefeng.visionrealm.event.bus.game;

import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.erosion.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.core.incident.IncidentHandler;
import io.github.jixingdefeng.visionrealm.core.network.protocol.BiomeErosionTypePayload;
import io.github.jixingdefeng.visionrealm.core.particle.config.ParticleConfigStore;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistry;
import io.github.jixingdefeng.visionrealm.core.server.packs.resources.erosion.biome.BiomeErosionReloadListener;
import io.github.jixingdefeng.visionrealm.core.server.packs.resources.erosion.block.BlockErosionReloadListener;
import io.github.jixingdefeng.visionrealm.core.server.packs.resources.particle.ParticleConfigReloadListener;
import io.github.jixingdefeng.visionrealm.event.bus.game.command.CommandEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.data.erosion.block.BlockErosionEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.block.BlockEntityTypeEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.EntitySpawnEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.EntityTickEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.player.PlayerEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
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

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = VisionRealm.MOD_ID)
public class GameBusEvents {

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
        EntitySpawnEvents.onRegisterSpawnPlacements(event);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(final EntityJoinLevelEvent event) {
        EntitySpawnEvents.onEntityJoinLevel(event);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!event.isEndConquered() && player instanceof ServerPlayer serverPlayer) {
            PlayerEvents.initAttributes(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogged(final PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        BiomeErosionManager.getInstance().ifPresent(manager -> manager.sync(player));
    }

    @SubscribeEvent
    public static void onEntityHurt(final LivingDamageEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            PlayerEvents.onHurt(player, event.getSource(), event.getNewDamage());
        }
    }

    @SubscribeEvent
    public static void onEntityTick(final EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        EntityTickEvents.updateParticles(entity);
    }

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        PlayerEvents.updateAttribute(player);
    }

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Pre event) {
        IncidentHandler.onServerTickEvent(event);
    }

    @SubscribeEvent
    public static void addBlockEntityTypeEvent(final BlockEntityTypeAddBlocksEvent event) {
        BlockEntityTypeEvents.addSignBlockEvents(event);
    }

    @SubscribeEvent
    public static void onNewRegistry(final DataPackRegistryEvent.NewRegistry event) {
        ModRegistry.registryDataPackRegistry(event);
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(final AddReloadListenerEvent event) {
        RegistryAccess registryAccess = event.getRegistryAccess();
        ParticleConfigStore.create();
        event.addListener(new ParticleConfigReloadListener());
        event.addListener(new BiomeErosionReloadListener(registryAccess));
        event.addListener(new BlockErosionReloadListener(registryAccess));
    }

    @SubscribeEvent
    public static void onBlockErosionLoaderRegister(final BlockErosionLoaderRegisterEvent event) {
        BlockErosionEvents.onBlockErosionLoaderRegister(event);
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar(VisionRealm.MOD_ID)
                .playToClient(
                        BiomeErosionTypePayload.TYPE,
                        BiomeErosionTypePayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            RegistryAccess access = context.player().level().registryAccess();
                            Map<ResourceKey<Biome>, BiomeErosionManager.BiomeConfig> map = new HashMap<>();
                            payload.map().forEach((key, config) -> map.put(key, config.converted(access)));
                            BiomeErosionManager.start(map);
                        })
                );
    }

    @SubscribeEvent
    public static void onRegisterCommand(final RegisterCommandsEvent event) {
        CommandEvents.onRegisterCommand(event);
    }
}
