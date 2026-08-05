package io.github.jixingdefeng.visionrealm.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jixingdefeng.visionrealm.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

public class ScreenEffects {
    public static final List<ResourceLocation> NOISE_FILTERS = List.of(
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_0.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_1.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_2.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_3.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_4.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_5.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_6.png"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/noise_filter_7.png")
    );
    public static final ResourceLocation BACKGROUND_FILTER = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "textures/gui/screen_filter/background_filter.png");
    public static final Minecraft minecraft = Minecraft.getInstance();
    private static final RandomSource random = RandomSource.create();
    private static final int BACKGROUND_FILTER_BLIT_OFFSET = -50;
    private static final int NOISE_FILTER_BLIT_OFFSET = -50;
    private static final float BACKGROUND_FILTER_ALPHA_BASE_VALUE = 0.95F;
    private static final float NOISE_FILTERS_ALPHA_BASE_VALUE = 0.25F;

    public static void render(final RenderGuiEvent event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Player player = getCameraEntity();

        if (display(player)) {
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();

            if (Config.ENABLE_SCREEN_OVERLAY.get()) {
                ScreenEffects.renderScreenFilters(guiGraphics, screenWidth, screenHeight);
            }
        }
    }

    private static boolean display(Player player) {
        return player != null
                && !player.isSpectator()
                && !minecraft.options.hideGui;
    }

    private static Player getCameraEntity() {
        return minecraft.getCameraEntity() instanceof Player player ? player : null;
    }

    private static void renderScreenFilters(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            float opacity = 0.0175F * Config.SCREEN_OVERLAY_OPACITY.get();

            guiGraphics.pose().pushPose();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, BACKGROUND_FILTER_ALPHA_BASE_VALUE * opacity);
            guiGraphics.blit(
                    BACKGROUND_FILTER,
                    0,
                    0,
                    BACKGROUND_FILTER_BLIT_OFFSET,
                    0,
                    0,
                    screenWidth,
                    screenHeight,
                    screenWidth,
                    screenHeight
            );

            guiGraphics.setColor(1.0F, 1.0F, 1.0F, NOISE_FILTERS_ALPHA_BASE_VALUE * opacity);
            guiGraphics.blit(
                    NOISE_FILTERS.get(random.nextInt(NOISE_FILTERS.size())),
                    0,
                    0,
                    NOISE_FILTER_BLIT_OFFSET,
                    0,
                    0,
                    screenWidth,
                    screenHeight,
                    screenWidth,
                    screenHeight
            );
        } finally {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.pose().popPose();
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
        }
    }
}
