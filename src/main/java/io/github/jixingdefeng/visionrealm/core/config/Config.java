package io.github.jixingdefeng.visionrealm.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder CLIENT_CONFIGS = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_CONFIGS = new ModConfigSpec.Builder();

    // ========================= 客户端配置 =========================
    // --- 屏幕效果配置 ---
    static {CLIENT_CONFIGS.push("ScreenEffects");}
    public static final ModConfigSpec.BooleanValue ENABLE_SCREEN_OVERLAY = CLIENT_CONFIGS
            .comment(
                    "Whether to enable the screen overlay effect",
                    "When enabled, displays a dim filter and noise overlay on screen."
            )
            .define("enableScreenOverlay", true);
    public static final ModConfigSpec.IntValue SCREEN_OVERLAY_OPACITY = CLIENT_CONFIGS
            .comment(
                    "Screen overlay opacity",
                    "Range: 10-50, higher values make it more opaque",
                    "Actual opacity = this value * 0.0175"
            )
            .defineInRange("screenOverlayOpacity", 30, 10, 50);
    static {CLIENT_CONFIGS.pop();}

    // --- UI配置 ---
    static {CLIENT_CONFIGS.push("Hud");}
    public static final ModConfigSpec.BooleanValue HUD_SHOW_VALUES = CLIENT_CONFIGS
            .comment(
                    "Master switch for displaying numeric information on HUD elements.",
                    "Enabled: Shows numerical values or percentages.",
                    "Disabled: Hides all numeric information, showing only icons."
            )
            .define("hudShowValues", true);
    static {CLIENT_CONFIGS.pop();}

    // ========================= 服务端配置 =========================
    // --- 游戏规则配置 ---
    static {SERVER_CONFIGS.push("GameRules");}
    public static final ModConfigSpec.BooleanValue ADMINISTRATOR_VULNERABLE = SERVER_CONFIGS
            .comment(
                    "Whether Administrator entities can be attacked",
                    "If disabled, Administrators will be immune to most damage types",
                    "Some special damage (e.g., void damage) still affect them"
            )
            .define("administratorVulnerable", false);
    static {SERVER_CONFIGS.pop();}

    public static final ModConfigSpec CLIENT_CONFIGS_SPEC = CLIENT_CONFIGS.build();
    public static final ModConfigSpec SERVER_CONFIGS_SPEC = SERVER_CONFIGS.build();
}
