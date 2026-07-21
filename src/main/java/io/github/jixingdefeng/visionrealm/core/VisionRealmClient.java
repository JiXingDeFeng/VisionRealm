package io.github.jixingdefeng.visionrealm.core;

import io.github.jixingdefeng.visionrealm.core.entity.ModEntities;
import io.github.jixingdefeng.visionrealm.core.entity.renderer.AdministratorRenderer;
import io.github.jixingdefeng.visionrealm.core.entity.renderer.ForgottenShadowRenderer;
import io.github.jixingdefeng.visionrealm.core.entity.renderer.NightmareApostleRenderer;
import io.github.jixingdefeng.visionrealm.core.particle.BloodCorrodedCherryParticle;
import io.github.jixingdefeng.visionrealm.core.particle.ErrorParticles;
import io.github.jixingdefeng.visionrealm.core.particle.ModParticleTypes;
import io.github.jixingdefeng.visionrealm.gui.InGameHud;
import io.github.jixingdefeng.visionrealm.gui.ScreenEffects;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = VisionRealm.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = VisionRealm.MOD_ID, value = Dist.CLIENT)
public class VisionRealmClient {
    public VisionRealmClient(final ModContainer container) {
        // 注册配置文件
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        // 注册实体渲染器
        EntityRenderers.register(ModEntities.ADMINISTRATOR.get(), AdministratorRenderer::new);
        EntityRenderers.register(ModEntities.FORGOTTEN_SHADOW.get(), ForgottenShadowRenderer::new);
        EntityRenderers.register(ModEntities.NIGHTMARE_APOSTLE.get(), NightmareApostleRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(final RegisterParticleProvidersEvent event) {
        // 注册粒子
        event.registerSpriteSet(ModParticleTypes.ERROR_PARTICLE_1.get(), ErrorParticles.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BLOOD_CORRODED_CHERRY.get(), BloodCorrodedCherryParticle.Provider::new);
    }

    // 渲染屏幕效果
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderScreenEffects(final RenderGuiEvent.Pre renderGuiEvent) {
        ScreenEffects.render(renderGuiEvent);
    }

    // 渲染图形界面
    @SubscribeEvent
    public static void renderHud(final RenderGuiLayerEvent.Pre renderGuiEvent) {
        InGameHud.render(renderGuiEvent);
    }
}
