package io.github.jixingdefeng.visionrealm.event.bus.game;

import io.github.jixingdefeng.visionrealm.api.controller.entity.EntityErosionController;
import io.github.jixingdefeng.visionrealm.api.controller.entity.player.PlayerSanityController;
import io.github.jixingdefeng.visionrealm.api.event.erosion.block.BlockErosionLoaderRegisterEvent;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.infection.block.BlockErosionEntryManager;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.data.erosion.biome.BiomeErosionReloadListener;
import io.github.jixingdefeng.visionrealm.core.data.erosion.block.BlockErosionReloadListener;
import io.github.jixingdefeng.visionrealm.core.incident.IncidentHandler;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import io.github.jixingdefeng.visionrealm.event.bus.game.data.erosion.block.BlockErosionEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.block.BlockEntityTypeEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.EntitySpawnEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.EntityTickEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.LivingTickEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.player.PlayerEvents;
import io.github.jixingdefeng.visionrealm.event.bus.game.world.entity.player.PlayerTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

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
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            PlayerEvents.initAttributes(player, event.isEndConquered());
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(final LivingDamageEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            ((EntityErosionController) livingEntity).onHurtUpdateErosion(event.getSource(), event.getNewDamage());
            if (livingEntity instanceof PlayerSanityController updatable) {
                updatable.onHurtUpdateSanity(event.getSource(), event.getNewDamage());
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            LivingTickEvents.updateAttribute(livingEntity);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        EntityTickEvents.updateParticles(entity);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        PlayerTickEvents.updateAttribute(player);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        IncidentHandler.onServerTickEvent(event);
    }

    @SubscribeEvent
    public static void addBlockEntityTypeEvent(BlockEntityTypeAddBlocksEvent event) {
        BlockEntityTypeEvents.addSignBlockEvents(event);
    }

    @SubscribeEvent
    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        ModRegistries.registryDataPackRegistry(event);
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(AddReloadListenerEvent event) {
        event.addListener(new BiomeErosionReloadListener(new BiomeErosionManager()));
        event.addListener(new BlockErosionReloadListener(new BlockErosionEntryManager()));
    }

    @SubscribeEvent
    public static void onBlockErosionLoaderRegister(BlockErosionLoaderRegisterEvent event) {
        BlockErosionEvents.onBlockErosionLoaderRegister(event);
    }
}
