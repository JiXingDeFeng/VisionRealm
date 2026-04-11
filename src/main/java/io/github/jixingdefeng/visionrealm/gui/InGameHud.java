package io.github.jixingdefeng.visionrealm.gui;

import io.github.jixingdefeng.visionrealm.core.Config;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import io.github.jixingdefeng.visionrealm.core.entity.ai.attributes.ModAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

public class InGameHud {
    public static final ResourceLocation EROSION_TEXTURE = ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "hud/erosion");
    public static final ResourceLocation[] SANE_TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "hud/sane_container"),
            ResourceLocation.fromNamespaceAndPath(VisionRealm.MOD_ID, "hud/sane")
    };
    public static final Minecraft minecraft = Minecraft.getInstance();
    private static final RandomSource random = RandomSource.create();
    private static final int TEXT_BLIT_OFFSET = 20;

    public static void render(RenderGuiLayerEvent event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Player player = getCameraEntity();
        if (display(player)) {
            renderErosionHud(guiGraphics, player);
            renderSaneHud(guiGraphics, player);
        }
    }

    private static boolean display(Player player) {
        return player != null && !(player.isCreative() || player.isSpectator()) && !minecraft.options.hideGui;
    }

    private static void renderSaneHud(GuiGraphics guiGraphics, Player player) {
        HudType type = Config.HUD_TYPE.get();
        if (type != HudType.ModernizeHud) {
            double saneValue = player.getAttributeValue(ModAttributes.SANITY);
            if (type == HudType.Default) {
                defaultSaneHud(guiGraphics, player, saneValue, Config.HUD_SHOW_VALUES.get());
            }
        }
    }

    private static void renderErosionHud(GuiGraphics guiGraphics, Player player) {
        HudType type = Config.HUD_TYPE.get();
        if (type != HudType.ModernizeHud) {
            double erosion = player.getAttributeValue(ModAttributes.EROSION);
            if (type == HudType.Default) {
                defaultErosionHud(guiGraphics, player, erosion, Config.HUD_SHOW_VALUES.get());
            }
        }
    }

    private static void defaultSaneHud(GuiGraphics guiGraphics, Player player, double saneValue, boolean displaysValues) {
        int x = guiGraphics.guiWidth() / 2 - 175;
        int y = guiGraphics.guiHeight() - 29;
        guiGraphics.blitSprite(SANE_TEXTURES[0], x, y, 50, 25);
        if (displaysValues) {
            renderText(guiGraphics, String.format("%.2f", saneValue), x, y - 10, 0xC0FFFFFF);
        }

        double length = saneValue >= 100 ? 1 : saneValue * 0.01;
        guiGraphics.blitSprite(SANE_TEXTURES[1], 44, 21, 0, 0, x + 3, y + 2, Mth.ceil(length * 44), 21);
    }

    private static void defaultErosionHud(GuiGraphics guiGraphics, Player player, double erosion, boolean displaysValues) {
        float maxHealth = (float) Math.max(player.getAttributeValue(Attributes.MAX_HEALTH), Mth.ceil(player.getHealth()));
        int absorptionAmount = Mth.ceil(player.getAbsorptionAmount());
        int height = Math.max(10 - (Mth.ceil((maxHealth + (float) absorptionAmount) / 2.0F / 10.0F) - 2), 3);
        int blinkFrame = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            blinkFrame = minecraft.gui.getGuiTicks() % Mth.ceil(maxHealth + 5.0F);
        }

        absorptionAmount = Mth.ceil(absorptionAmount / 2.0F);
        int healthCount = Mth.ceil(maxHealth / 2.0);
        float erosionCount = (float) (healthCount * erosion);
        int count = Mth.ceil(erosionCount);
        int baseX = guiGraphics.guiWidth() / 2 - 91;
        int baseY = guiGraphics.guiHeight() - 39;
        if (displaysValues) {
            renderText(guiGraphics, (int) (erosion * 100) + "%", baseX + 82, baseY - 8, 0xC0FF0000);
        }

        for (int i = 1; i <= count; i++) {
            int rowIndex = (i - 1) / 10;
            int columnIndex = (i - 1) % 10;
            int x = baseX + columnIndex * 8;
            int y = baseY - rowIndex * height;
            int length = 9;

            if (player.getHealth() + absorptionAmount <= 4) {
                y += random.nextInt(2);
            }

            if (i < rowIndex && i == blinkFrame) {
                y -= 2;
            }

            if (i == count && erosionCount < count) {
                length = Math.max(1, (int) (length * (erosionCount - (count - 1))));
            }

            guiGraphics.blitSprite(EROSION_TEXTURE, 10, 10, 0, 0, x, y, length, 9);
        }
    }

    private static void renderText(GuiGraphics guiGraphics, String value, int x, int y, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, TEXT_BLIT_OFFSET);
        guiGraphics.drawString(minecraft.font, value, x, y, color);
        guiGraphics.pose().translate(0, 0, 0);
        guiGraphics.pose().popPose();
    }

    private static Player getCameraEntity() {
        return minecraft.getCameraEntity() instanceof Player player ? player : null;
    }

    public enum HudType {
        Default,
        ModernizeHud
    }

    public enum HudPosition {
        TopLeftCorner,
        TopRightCorner,
        AboveTheInventory,
        Custom
    }
}
