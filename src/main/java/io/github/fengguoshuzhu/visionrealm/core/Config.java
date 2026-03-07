package io.github.fengguoshuzhu.visionrealm.core;

import io.github.fengguoshuzhu.visionrealm.gui.InGameHud;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder CLIENT_CONFIGS = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SERVER_CONFIGS = new ModConfigSpec.Builder();

    // ========================= 客户端配置 =========================
    // --- 屏幕效果配置 ---
    static {CLIENT_CONFIGS.push("screenEffects");}
    public static final ModConfigSpec.BooleanValue ENABLE_SCREEN_OVERLAY = CLIENT_CONFIGS
            .comment(
                    "启用屏幕叠加层效果",
                    "Whether to enable the screen overlay effect"
            )
            .define("enableScreenOverlay", true);
    public static final ModConfigSpec.IntValue SCREEN_OVERLAY_OPACITY = CLIENT_CONFIGS
            .comment(
                    "屏幕叠加层的不透明度",
                    "范围：70-125，值越高越不透明",
                    "实际不透明度 = 此值 / 100",
                    "Screen overlay opacity",
                    "Range: 70-125, higher values make it more opaque",
                    "Actual opacity = this value / 100"
            )
            .defineInRange("screenOverlayOpacity", 90, 80, 100);
    static {CLIENT_CONFIGS.pop();}

    // --- UI配置 ---
    static {CLIENT_CONFIGS.push("gameHud");}
    public static final ModConfigSpec.EnumValue<InGameHud.HudType> HUD_TYPE = CLIENT_CONFIGS
            .comment(
                    "游戏内HUD的显示类型",
                    "Default - 默认样式，ModernizeHud - 现代化条形UI",
                    "In-game HUD type",
                    "Default - Classic style, ModernizeHud - Modern bar UI"
            )
            .defineEnum("hudType", InGameHud.HudType.Default);
    public static final ModConfigSpec.EnumValue<InGameHud.HudPosition> HUD_POSITION = CLIENT_CONFIGS
            .comment(
                    "HUD在屏幕上的显示位置",
                    "注意：Default样式不支持Custom位置",
                    "AboveTheInventory - 显示在物品栏上方（默认位置）",
                    "TopLeft - 屏幕左上角",
                    "TopRight - 屏幕右上角",
                    "Custom - 自定义位置（需配合坐标设置）",
                    "Position of HUD on screen",
                    "Note: Custom position is not available for Default style",
                    "AboveTheInventory - Above inventory (default)",
                    "TopLeft - Top-left corner of screen",
                    "TopRight - Top-right corner of screen",
                    "Custom - Custom position (requires coordinate settings)"
            )
            .defineEnum("hudPosition", InGameHud.HudPosition.AboveTheInventory);
    public static final ModConfigSpec.BooleanValue HUD_SHOW_VALUES = CLIENT_CONFIGS
            .comment(
                    "[其他HUD类型通用] 是否显示数值",
                    "适用于Default、LargeIcon等非ModernizeHud样式",
                    "开启：显示具体数值",
                    "关闭：只显示图标/进度条，不显示数字",
                    "[Other HUD types] Show values",
                    "Applies to Default, LargeIcon and other non-ModernizeHud styles",
                    "Enabled: Shows specific values",
                    "Disabled: Shows icons/progress bars without numbers"
            )
            .define("hudShowValues", true);
    public static final ModConfigSpec.BooleanValue MODERNIZE_HUD_SHOWS_VALUE = CLIENT_CONFIGS
            .comment(
                    "[ModernizeHud专用] 是否在进度条上显示具体数值",
                    "开启：显示精确数值（如：85/100）",
                    "关闭：只显示进度条，不显示数字",
                    "[ModernizeHud only] Show values on progress bars",
                    "Enabled: Shows exact values (e.g., 85/100)",
                    "Disabled: Shows progress bars without numbers"
            )
            .define("modernizeHudShowValues", true);
    static {CLIENT_CONFIGS.pop();}

    // ========================= 服务端配置 =========================
    // --- 游戏规则配置 ---
    static {SERVER_CONFIGS.push("gameRules");}
    public static final ModConfigSpec.BooleanValue ADMINISTRATOR_VULNERABLE = SERVER_CONFIGS
            .comment(
                    "管理员实体是否可以被攻击",
                    "如果禁用，管理员仍然免疫绝大多数伤害",
                    "某些特殊伤害（如虚空伤害）仍然有效",
                    "Whether Administrator entities can be attacked",
                    "If disabled, Administrators will be immune to most damage types",
                    "Some special damage (e.g., void damage) still affect them"
            )
            .define("administratorVulnerable", false);
    static {SERVER_CONFIGS.pop();}

    static final ModConfigSpec CLIENT_CONFIGS_SPEC = CLIENT_CONFIGS.build();
    static final ModConfigSpec SERVER_CONFIGS_SPEC = SERVER_CONFIGS.build();
}
