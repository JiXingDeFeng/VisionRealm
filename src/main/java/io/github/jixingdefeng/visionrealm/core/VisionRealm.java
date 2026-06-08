package io.github.jixingdefeng.visionrealm.core;

import com.mojang.logging.LogUtils;
import io.github.jixingdefeng.visionrealm.api.incident.RegisteredIncident;
import io.github.jixingdefeng.visionrealm.api.selector.game.TargetSelector;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.common.erosion.manager.infection.block.BlockErosionEntryManager;
import io.github.jixingdefeng.visionrealm.common.particle.ParticleConfigLoader;
import io.github.jixingdefeng.visionrealm.common.selector.StateSelection;
import io.github.jixingdefeng.visionrealm.common.util.selector.TargetSelectors;
import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import io.github.jixingdefeng.visionrealm.core.entity.Entities;
import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import io.github.jixingdefeng.visionrealm.core.incident.IncidentHandler;
import io.github.jixingdefeng.visionrealm.core.item.ItemGroups;
import io.github.jixingdefeng.visionrealm.core.item.ModItems;
import io.github.jixingdefeng.visionrealm.core.particle.ModParticleTypes;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistries;
import io.github.jixingdefeng.visionrealm.core.registry.ModRegistryKeys;
import io.github.jixingdefeng.visionrealm.core.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.util.List;

@Mod(VisionRealm.MOD_ID)
public class VisionRealm {
    public static final String INLINE_DATA_ID = "inline_data/";
    public static final String MOD_ID = "visionrealm";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VisionRealm(final IEventBus modEventBus, final ModContainer modContainer) {
        // 注册 commonSetup(常见配置) 方法进行 modLoading(模组加载)
        modEventBus.addListener((FMLCommonSetupEvent event) -> this.commonSetup(event, modEventBus));
        NeoForge.EVENT_BUS.register(this);

        // 注册注册表
        modEventBus.addListener(ModRegistries::registerRegistries);

        // 注册音效
        ModSounds.register(modEventBus);

        // 注册方块
        ModBlocks.registry(modEventBus);

        // 注册物品
        ModItems.registry(modEventBus);

        // 注册物品标签页
        ItemGroups.registry(modEventBus);

        // 注册实体属性
        ModAttributes.registry(modEventBus);

        // 注册实体
        Entities.registry(modEventBus);

        // 注册粒子
        ModParticleTypes.register(modEventBus);

        // 注册配置文件
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIGS_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIGS_SPEC);

        // 订阅事件
//        modEventBus.addListener(BlockErosionEntryManager::cacheRegistry);

        // 其他初始化

        modEventBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(ModRegistryKeys.INCIDENT)) {
                event.register(
                        ModRegistryKeys.INCIDENT,
                        ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "test_incident"),
                        () -> new RegisteredIncident.Builder<>(
                                context -> {
                                    Level level = context.getLevel();
                                    LOGGER.warn("\n\n—————————————— TestIncident ——————————————\nLevel: {}\n", level);
                                    if (level instanceof ServerLevel serverLevel) {
                                        for (BlockPos pos : context.getAllSource()) {
                                            LOGGER.warn("\n\nBlockPos: {}\n", pos);
                                            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                                        }
                                    }
                                    return true;
                                },
                                (level, random) -> TargetSelectors.block(level)
                                        .air(StateSelection.NOT)
                                        .fluid(StateSelection.NOT)
                                        .randomObtain(random),
                                List.of(Level.OVERWORLD)
                        ).extractor(TargetSelector::toList).build()
                );
            }
        });

        LOGGER.info("VisionRealm has been initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event, final IEventBus modEventBus) {
        event.enqueueWork(() -> ParticleConfigLoader.initialize(modEventBus));
    }

    @SubscribeEvent
    public void onServerStartingEvent(ServerStartingEvent event) {
        IncidentHandler.create(null).open();
        ParticleConfigLoader.start(event);
    }

    @SubscribeEvent
    public void onServerStoppingEvent(ServerStoppingEvent event) {
        ParticleConfigLoader.close(event);
        BiomeErosionManager.close(event);
        BlockErosionEntryManager.close(event);
        IncidentHandler.close(event);
    }
}
