package io.github.jixingdefeng.visionrealm.core;

import com.mojang.logging.LogUtils;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.biome.BiomeErosionManager;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.config.block.BlockErosionConfigManager;
import io.github.jixingdefeng.visionrealm.common.manager.erosion.infection.block.BlockErosionKeyManager;
import io.github.jixingdefeng.visionrealm.common.manager.particle.ParticleConfigManager;
import io.github.jixingdefeng.visionrealm.common.registry.ModRegistries;
import io.github.jixingdefeng.visionrealm.core.block.ModBlocks;
import io.github.jixingdefeng.visionrealm.core.entity.Entities;
import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import io.github.jixingdefeng.visionrealm.core.item.ItemGroups;
import io.github.jixingdefeng.visionrealm.core.item.ModItems;
import io.github.jixingdefeng.visionrealm.core.particle.ModParticleTypes;
import io.github.jixingdefeng.visionrealm.core.sound.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

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
        ModRegistries.registry(modEventBus);

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
        modEventBus.addListener(BlockErosionKeyManager::cacheRegistry);

        // 其他初始化

        LOGGER.info("VisionRealm has been initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event, final IEventBus modEventBus) {
        event.enqueueWork(() -> ParticleConfigManager.initialize(modEventBus));
    }

    @SubscribeEvent
    public void onServerStartingEvent(ServerStartingEvent event) {
        ParticleConfigManager.serverStart(event);
    }

    @SubscribeEvent
    public void onServerStoppingEvent(ServerStoppingEvent event) {
        ParticleConfigManager.serverStopping(event);
        BiomeErosionManager.serverStopping(event);
        BlockErosionKeyManager.serverStopping(event);
        BlockErosionConfigManager.serverStopping(event);
    }
}
