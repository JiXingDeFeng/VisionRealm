package io.github.jixingdefeng.visionrealm.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jixingdefeng.visionrealm.core.Config;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
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
    private static final int BINARY_TEXT_LENGTH = 8;
    private static final int BACKGROUND_FILTER_BLIT_OFFSET = -50;
    private static final int NOISE_FILTER_BLIT_OFFSET = -50;
    private static final int BINARY_TEXT_BLIT_OFFSET = 500;
    private static final int RECTANGULAR_BLIT_OFFSET = 500;
    private static final int BINARY_TEXT_BASE_COLOR = 0xFF0000;
    private static final int[] RECTANGULAR_BASE_COLORS = {
            0x0000FF, 0x00FF00, 0x00FFFF, 0xFF00FF, 0xFFFF00, 0xFF0000
    };
    private static final float BACKGROUND_FILTER_ALPHA_BASE_VALUE = 0.95F;
    private static final float NOISE_FILTERS_ALPHA_BASE_VALUE = 0.25F;
    private static final float BINARY_TEXT_ALPHA_BASE_VALUE = 0.12F;
    private static final float RECTANGULAR_ALPHA_BASE_VALUE = 0.02F;

    public static void render(RenderGuiEvent event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Player player = getCameraEntity();

        if (display(player)) {
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();

            ScreenEffects.renderScreenFilters(guiGraphics, screenWidth, screenHeight);
            ScreenEffects.renderScreenParticles(guiGraphics, screenWidth, screenHeight);
        }
    }

    private static boolean display(Player player) {
        return Config.ENABLE_SCREEN_OVERLAY.get()
                && player != null
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

            float opacity = 0.01F * Config.SCREEN_OVERLAY_OPACITY.get();

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

    private static void renderScreenParticles(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        int count = random.nextInt(5, 15);
        for (int i = 0; i < count; i++) {
            boolean bl = random.nextBoolean();
            int x = random.nextInt(screenWidth + 50) - 50;
            int y = random.nextInt(screenHeight);
            float opacity = 0.01F * Config.SCREEN_OVERLAY_OPACITY.get();
            float alpha = (bl ? BINARY_TEXT_ALPHA_BASE_VALUE : RECTANGULAR_ALPHA_BASE_VALUE) * opacity;
            if (bl) {
                StringBuilder text = new StringBuilder(Integer.toString(random.nextInt((int) Math.pow(2, BINARY_TEXT_LENGTH)), 2));
                if (text.length() < BINARY_TEXT_LENGTH) {
                    int zeros = BINARY_TEXT_LENGTH - text.length();
                    text.insert(0, "0".repeat(Math.max(0, zeros)));
                }

                int color = ((int) (alpha * 255) << 24) | BINARY_TEXT_BASE_COLOR;
                guiGraphics.pose().translate(0, 0, BINARY_TEXT_BLIT_OFFSET);
                guiGraphics.drawString(minecraft.fontFilterFishy, text.toString(), x, y, color);
                guiGraphics.pose().translate(0, 0, 0);
            } else {
                int baseColor = RECTANGULAR_BASE_COLORS[random.nextInt(RECTANGULAR_BASE_COLORS.length)];
                int color = ((int) (alpha * 255) << 24) | baseColor;
                guiGraphics.fill(x, y, x + 100, y + 10, RECTANGULAR_BLIT_OFFSET, color);
            }
        }
    }
}
