package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.event.OverlayReloadListener;
import com.turtmod.hud.HudEditorScreen;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.turtmod.ui.config.TurtNativeConfigScreen;
import com.turtmod.ui.config.model.Category;
import com.turtmod.ui.config.model.ConfigColor;
import com.turtmod.ui.config.model.LocalConfig;
import com.turtmod.ui.config.model.Option;
import com.turtmod.ui.config.model.OptionDescription;
import com.turtmod.ui.config.model.OptionGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import net.minecraft.class_437;

public final class TurtModConfigScreenFactory {
   private TurtModConfigScreenFactory() {
   }

   public static class_437 create(class_437 parent) {
      TurtModConfig cfg = TurtModClient.getConfig();
      LocalConfig config = LocalConfig.createBuilder("TurtMod")
         .path(getConfigPath())
         .category(buildVisualCategory(cfg))
         .category(buildHudCategory(cfg))
         .category(buildCombatCategory(cfg))
         .category(buildThemeCategory(cfg))
         .category(buildMiscCategory(cfg))
         .category(buildEditorCategory(cfg))
         .onSave(() -> ConfigManager.save(cfg))
         .build();
      config.load();
      return new TurtNativeConfigScreen(parent, config);
   }

   public static class_437 createForModule(class_437 parent, ModuleKind kind) {
      if (kind == null) {
         return create(parent);
      }

      LocalConfig config = LocalConfig.createBuilder("TurtMod")
         .path(getConfigPath())
         .category(buildModuleCategory(kind, parent))
         .onSave(() -> ConfigManager.save(TurtModClient.getConfig()))
         .build();
      config.load();
      TurtNativeConfigScreen screen = new TurtNativeConfigScreen(parent, config);
      // (Live "preview box" disabled — the framed box wasn't the look wanted. The renderPreview() hooks
      //  are kept for a future frameless "see your real HUDs live" pass, done properly. To re-enable the
      //  box: attach screen.setPreview(...) with HudPreview.render for kinds where HudPreview.has(kind).)
      return screen;
   }

   public static String getModuleDisplayName(ModuleKind kind) {
      return switch (kind) {
         case FULLBRIGHT -> "Fullbright";
         case FREELOOK -> "Freelook";
         case HIT_COLOR -> "Hit Color";
         case LOW_FIRE -> "Low Fire";
         case LOW_SHIELD -> "Shield Tweaks";
         case FOG_CONTROLS -> "Fog Tweaks";
         case OVERLAYS -> "Overlays";
         case CAMERA_SETTINGS -> "Hurt Cam";
         case BLOCK_OUTLINE -> "Block Outline";
         case SMALL_TOTEM -> "Totem Tweaks";
         case DISCORD_RPC -> "Discord RPC";
         case HELD_ITEM -> "Held Item Tweaks";
         case ARMOR_HUD -> "Armor HUD";
         case POTION_HUD -> "Potion HUD";
         case FPS_PING -> "FPS/Ping";
         case REACH -> "Reach Display";
         case KEYSTROKES -> "Keystrokes";
         case CPS_COUNTER -> "CPS Counter";
         case SPRINT_HUD -> "Sprint Display";
         case INVENTORY_HUD -> "Inventory HUD";
         case CUSTOM_HITBOXES -> "Hitboxes";
         case SCOREBOARD -> "Scoreboard Tweaks";
         case BETTER_SCREENSHOT -> "Screenshot Tools";
         case CLEAN_F3 -> "Clean F3";
         case HEALTH_INDICATOR -> "Health Indicator";
         case ZOOM -> "Zoom";
         case THEME_SETTINGS -> "Theme Settings";
         case ELYTRA_HUD -> "Elytra Pitch HUD";
         case OWN_NAMETAG -> "Own Nametag";
         case COORDINATES_HUD -> "Coordinates HUD";
         case PING_DISPLAY -> "Ping Display";
         case DEATH_COORDS -> "Death Coords";
         case MUTE_SOUNDS -> "Mute Sounds";
         case HIDE_PARTICLES -> "Particle Tweaks";
         case CLEAR_VIEW -> "Clear View";
         case CHAT_TWEAKS -> "Chat Tweaks";
         case COMMAND_KEYS -> "Command Keys";
         case KIT_LOADER -> "Kit Loader";
         case GAMEMODE_SWITCHER -> "Gamemode Switcher";
         case MODULE_TOASTS -> "Module Notifications";
         case FISHING_LINE -> "Fishing Line";
      };
   }

   private static Category buildVisualCategory(TurtModConfig cfg) {
      return Category.createBuilder("Visual")
         .group(OptionGroup.createBuilder("Lighting")
            .addOption(bool("Enable Fullbright", () -> cfg.visual.fullbright.enabled, v -> cfg.visual.fullbright.enabled = v))
            .addOption(intOpt("Gamma Level", () -> (int)Math.round(cfg.visual.fullbright.value), v -> cfg.visual.fullbright.value = v, 1, 32, 1))
            .build())
         .group(OptionGroup.createBuilder("Camera")
            .addOption(bool("Freelook", () -> cfg.visual.freelookEnabled, v -> cfg.visual.freelookEnabled = v))
            .addOption(intOpt("Freelook Sensitivity %", () -> cfg.visual.freelookSensitivityPercent, v -> cfg.visual.freelookSensitivityPercent = v, 25, 200, 5))
            .addOption(bool("Hurt Cam", () -> cfg.visual.hurtCamEnabled, v -> cfg.visual.hurtCamEnabled = v))
            .addOption(enumOpt("Hurt Cam Mode", () -> cfg.visual.hurtCamMode, v -> cfg.visual.hurtCamMode = v, TurtModConfig.HurtCamMode.class))
            .addOption(intOpt("Hurt Camera Shake %", () -> cfg.visual.hurtCameraShakePercent, v -> cfg.visual.hurtCameraShakePercent = v, 0, 150, 5))
            .build())
         .group(OptionGroup.createBuilder("Overlays & Fog")
            .addOption(intOpt("Fire Offset Y", () -> cfg.visual.fireYOffset, v -> cfg.visual.fireYOffset = v, -100, 100, 5))
            .addOption(bool("Disable Fire Overlay", () -> cfg.visual.disableFireOverlay, v -> cfg.visual.disableFireOverlay = v))
            .addOption(bool("Disable Pumpkin Blur", () -> cfg.visual.disablePumpkinBlur, v -> cfg.visual.disablePumpkinBlur = v))
            .addOption(bool("Disable Powder Snow", () -> cfg.visual.disablePowderSnowOverlay, v -> cfg.visual.disablePowderSnowOverlay = v))
            .addOption(bool("Disable Darkness", () -> cfg.visual.disableDarknessOverlay, v -> cfg.visual.disableDarknessOverlay = v))
            .addOption(bool("Disable All Fog", () -> cfg.visual.disableAllFog, v -> cfg.visual.disableAllFog = v))
            .addOption(bool("Disable Water Fog", () -> cfg.visual.disableWaterFog, v -> cfg.visual.disableWaterFog = v))
            .addOption(bool("Disable Lava Fog", () -> cfg.visual.disableLavaFog, v -> cfg.visual.disableLavaFog = v))
            .addOption(bool("Disable Nether Fog", () -> cfg.visual.disableNetherFog, v -> cfg.visual.disableNetherFog = v))
            .addOption(intOpt("Fog Distance %", () -> cfg.visual.fogDensityPercent, v -> cfg.visual.fogDensityPercent = v, 25, 400, 5))
            .build())
         .group(OptionGroup.createBuilder("Items")
            .addOption(bool("Held Item Tweaks", () -> cfg.visual.heldItemTweaksEnabled, v -> cfg.visual.heldItemTweaksEnabled = v))
            .addOption(intOpt("Held Item Scale %", () -> cfg.visual.heldItemScalePercent, v -> cfg.visual.heldItemScalePercent = v, 50, 150, 5))
            .addOption(bool("Enable Small Totem", () -> cfg.visual.enableSmallTotem, v -> cfg.visual.enableSmallTotem = v))
            .addOption(intOpt("Totem Scale %", () -> Math.round(cfg.visual.totemScale * 100.0F), v -> cfg.visual.totemScale = (float)v / 100.0F, 25, 150, 5))
            .addOption(bool("Fishing Line Color", () -> cfg.visual.fishingRodOverlay, v -> cfg.visual.fishingRodOverlay = v))
            .addOption(color("Line Color", () -> cfg.visual.fishingRodOverlayColor, v -> cfg.visual.fishingRodOverlayColor = v, GradientKeys.FISHING_LINE))
            .addOption(numOpt("Line Opacity", () -> cfg.visual.fishingRodOverlayAlpha, v -> cfg.visual.fishingRodOverlayAlpha = v, 0.1F, 1.0F, 0.05F))
            .build())
         .group(OptionGroup.createBuilder("Block Outline")
            .addOption(bool("Recolor Block Outline", () -> cfg.visual.recolorBlockOutline, v -> cfg.visual.recolorBlockOutline = v))
            .addOption(color("Block Outline Color", () -> cfg.visual.blockOutlineColor, v -> cfg.visual.blockOutlineColor = v))
            .addOption(intOpt("Block Outline Width", () -> cfg.visual.blockOutlineWidth, v -> cfg.visual.blockOutlineWidth = v, 1, 10, 1))
            .addOption(intOpt("Block Outline Opacity", () -> cfg.visual.blockOutlineAlpha, v -> cfg.visual.blockOutlineAlpha = v, 0, 255, 5))
            .addOption(bool("Rainbow Block Outline", () -> cfg.visual.blockOutlineRainbow, v -> cfg.visual.blockOutlineRainbow = v))
            .build())
         .build();
   }

   private static Category buildHudCategory(TurtModConfig cfg) {
      return Category.createBuilder("HUD")
         .group(OptionGroup.createBuilder("Actions")
            .addOption(button("Open HUD Editor", () -> {
               class_310 mc = class_310.method_1551();
               mc.method_1507(new HudEditorScreen(mc.field_1755));
            }))
            .build())
         .group(OptionGroup.createBuilder("Armor & Potions")
            .addOption(bool("Movable Armor HUD", () -> cfg.hud.movableArmorHud, v -> cfg.hud.movableArmorHud = v))
            .addOption(bool("Armor Vertical", () -> cfg.hud.armorHudVertical, v -> cfg.hud.armorHudVertical = v))
            .addOption(enumOpt("Armor Style", () -> cfg.hud.armorHudStyle, v -> {
               cfg.hud.armorHudStyle = v;
               cfg.hud.armorHudHotbarStyle = v == TurtModConfig.ArmorHudStyle.HOTBAR;
            }, TurtModConfig.ArmorHudStyle.class))
            .addOption(enumOpt("Durability Mode", () -> cfg.hud.armorHudDurabilityMode, v -> {
               cfg.hud.armorHudDurabilityMode = v;
               cfg.hud.armorHudShowDurability = v != TurtModConfig.ArmorHudDurabilityMode.OFF;
            }, TurtModConfig.ArmorHudDurabilityMode.class))
            .addOption(bool("Low Durability Warning", () -> cfg.hud.armorHudWarnings, v -> cfg.hud.armorHudWarnings = v))
            .addOption(bool("Text Color at Full Durability", () -> cfg.hud.armorHudFullDurabilityTextColor, v -> cfg.hud.armorHudFullDurabilityTextColor = v))
            .addOption(intOpt("Armor Scale %", () -> cfg.hud.armorHudScalePercent, v -> cfg.hud.armorHudScalePercent = v, 50, 300, 5))
            .addOption(bool("Movable Potion HUD", () -> cfg.hud.movablePotionHud, v -> cfg.hud.movablePotionHud = v))
            .addOption(enumOpt("Potion Style", () -> cfg.hud.potionHudStyle, v -> cfg.hud.potionHudStyle = v, TurtModConfig.PotionHudStyle.class))
            .addOption(enumOpt("Potion Sort By", () -> cfg.hud.potionSortMode, v -> cfg.hud.potionSortMode = v, TurtModConfig.PotionSortMode.class))
            .addOption(bool("Potion Horizontal", () -> cfg.hud.potionHudHorizontal, v -> cfg.hud.potionHudHorizontal = v))
            .addOption(intOpt("Potion Columns", () -> cfg.hud.potionHudColumns, v -> cfg.hud.potionHudColumns = v, 1, 4, 1))
            .addOption(bool("Potion Show Flags", () -> cfg.hud.potionShowFlags, v -> cfg.hud.potionShowFlags = v))
            .addOption(intOpt("Potion Scale %", () -> cfg.hud.potionHudScalePercent, v -> cfg.hud.potionHudScalePercent = v, 50, 300, 5))
            .build())
         .group(OptionGroup.createBuilder("Counters & Info")
            .addOption(bool("FPS/Ping", () -> cfg.hud.minimalFpsPingOverlay || cfg.hud.pingHudEnabled, v -> { cfg.hud.minimalFpsPingOverlay = v; cfg.hud.pingHudEnabled = v; }))
            .addOption(bool("Reach Display", () -> cfg.hud.reachDisplay, v -> cfg.hud.reachDisplay = v))
            .addOption(bool("Inventory HUD", () -> cfg.hud.inventoryHudEnabled, v -> cfg.hud.inventoryHudEnabled = v))
            .addOption(bool("Keystrokes", () -> cfg.hud.keystrokesHud, v -> cfg.hud.keystrokesHud = v))
            .addOption(bool("CPS Counter", () -> cfg.hud.cpsCounterHud, v -> cfg.hud.cpsCounterHud = v))
            .addOption(bool("Coordinates", () -> cfg.hud.coordinatesHud, v -> cfg.hud.coordinatesHud = v))
            .addOption(bool("Elytra Pitch HUD", () -> cfg.visual.elytraPitchHud, v -> cfg.visual.elytraPitchHud = v))
            .build())
         .group(OptionGroup.createBuilder("Hitboxes")
            .addOption(bool("Custom Hitboxes", () -> cfg.hud.customHitboxes, v -> cfg.hud.customHitboxes = v))
            .addOption(bool("Clean Debug Hitboxes", () -> cfg.hud.cleanDebugHitboxes, v -> cfg.hud.cleanDebugHitboxes = v))
            .addOption(bool("Players", () -> cfg.hud.hitboxPlayers, v -> cfg.hud.hitboxPlayers = v))
            .addOption(bool("Hostile", () -> cfg.hud.hitboxHostile, v -> cfg.hud.hitboxHostile = v))
            .addOption(bool("Passive", () -> cfg.hud.hitboxPassive, v -> cfg.hud.hitboxPassive = v))
            .addOption(bool("Others", () -> cfg.hud.hitboxOthers, v -> cfg.hud.hitboxOthers = v))
            .addOption(bool("Self", () -> cfg.hud.hitboxSelf, v -> cfg.hud.hitboxSelf = v))
            .addOption(intOpt("Max Distance", () -> cfg.hud.hitboxMaxDistance, v -> cfg.hud.hitboxMaxDistance = v, 8, 256, 1))
            .addOption(color("Hitbox Color", () -> cfg.hud.hitboxColor, v -> cfg.hud.hitboxColor = v))
            .addOption(button("Entity Colors...", () -> class_310.method_1551().method_1507(new com.turtmod.hud.HitboxColorsScreen(class_310.method_1551().field_1755))))
            .addOption(bool("Target Color", () -> cfg.hud.hitboxChangeTargetColor, v -> cfg.hud.hitboxChangeTargetColor = v))
            .addOption(color("Target Hitbox Color", () -> cfg.hud.hitboxTargetColor, v -> cfg.hud.hitboxTargetColor = v))
            .addOption(bool("Hurt Color", () -> cfg.hud.hitboxHurtColorEnabled, v -> cfg.hud.hitboxHurtColorEnabled = v))
            .addOption(color("Hurt Hitbox Color", () -> cfg.hud.hitboxHurtColor, v -> cfg.hud.hitboxHurtColor = v))
            .addOption(bool("Show Invisible Armored Players", () -> cfg.hud.hitboxShowInvisible, v -> cfg.hud.hitboxShowInvisible = v))
            .addOption(bool("Hide Fireworks", () -> cfg.hud.hitboxHideFireworks, v -> cfg.hud.hitboxHideFireworks = v))
            .build())
         .group(OptionGroup.createBuilder("Clean F3")
            .addOption(bool("Clean F3 Mode", () -> cfg.hud.cleanF3Mode, v -> cfg.hud.cleanF3Mode = v))
            .addOption(intOpt("Clean F3 Scale %", () -> cfg.hud.cleanF3ScalePercent, v -> cfg.hud.cleanF3ScalePercent = v, 50, 300, 5))
            .addOption(bool("Show FPS/Ping", () -> cfg.hud.cleanF3ShowFpsPing, v -> cfg.hud.cleanF3ShowFpsPing = v))
            .addOption(bool("Show Position", () -> cfg.hud.cleanF3ShowPosition, v -> cfg.hud.cleanF3ShowPosition = v))
            .addOption(bool("Show Facing", () -> cfg.hud.cleanF3ShowFacing, v -> cfg.hud.cleanF3ShowFacing = v))
            .addOption(bool("Show Biome", () -> cfg.hud.cleanF3ShowBiome, v -> cfg.hud.cleanF3ShowBiome = v))
            .build())
         .build();
   }

   private static Category buildCombatCategory(TurtModConfig cfg) {
      return Category.createBuilder("Combat")
         .group(OptionGroup.createBuilder("Health Indicator")
            .addOption(bool("Player Health Indicator", () -> cfg.combat.playerHealthIndicator, v -> cfg.combat.playerHealthIndicator = v))
            .addOption(enumOpt("Health Indicator Style", () -> cfg.combat.playerHealthIndicatorStyle, v -> cfg.combat.playerHealthIndicatorStyle = v, TurtModConfig.PlayerHealthIndicatorStyle.class))
            .addOption(intOpt("Health Indicator Max Hearts", () -> cfg.combat.playerHealthIndicatorMaxHearts, v -> cfg.combat.playerHealthIndicatorMaxHearts = v, 1, 40, 1))
            .addOption(bool("Exact Health Number", () -> cfg.combat.showExactHealthNumber, v -> cfg.combat.showExactHealthNumber = v))
            .addOption(bool("Own Nametag", () -> cfg.visual.showOwnNametag, v -> cfg.visual.showOwnNametag = v))
            .build())
         .group(OptionGroup.createBuilder("Hit Color")
            .addOption(bool("Hit Color", () -> cfg.visual.hitColor.enabled, v -> cfg.visual.hitColor.enabled = v))
            .addOption(color("Hit Color", () -> cfg.visual.hitColor.color, v -> cfg.visual.hitColor.setColor(v)))
            .addOption(intOpt("Hit Alpha", () -> cfg.visual.hitColor.alpha, v -> cfg.visual.hitColor.setAlpha(v), 0, 255, 5))
            .addOption(bool("Armor Damage Tint", () -> cfg.visual.armorDamageTint, v -> cfg.visual.armorDamageTint = v))
            .addOption(bool("Armor Trim Tint", () -> cfg.visual.armorDamageTintTrim, v -> cfg.visual.armorDamageTintTrim = v))
            .build())
         .build();
   }

   private static Category buildThemeCategory(TurtModConfig cfg) {
      return Category.createBuilder("Theme")
         .group(OptionGroup.createBuilder("HUD Theme")
            // Clean, Lunar-style theme: background, text, accent, shadow. Background Opacity 0 = text-only.
            .addOption(color("Background Color", () -> cfg.theme.hudBackgroundColor, v -> cfg.theme.hudBackgroundColor = v, GradientKeys.HUD_BG))
            .addOption(intOpt("Background Opacity", () -> cfg.theme.hudBackgroundAlpha, v -> cfg.theme.hudBackgroundAlpha = v, 0, 255, 5))
            .addOption(color("Text Color", () -> cfg.theme.hudTextColor, v -> cfg.theme.hudTextColor = v, GradientKeys.HUD_TEXT))
            .addOption(color("Accent Color", () -> cfg.theme.hudAccentColor, v -> cfg.theme.hudAccentColor = v, GradientKeys.HUD_ACCENT))
            .addOption(bool("Text Shadow", () -> cfg.theme.enableShadows, v -> cfg.theme.enableShadows = v))
            .addOption(button("Reset Theme", () -> resetThemeDefaults(cfg)))
            .build())
         .build();
   }

   private static void resetThemeDefaults(TurtModConfig cfg) {
      TurtModConfig.CustomTheme d = new TurtModConfig.CustomTheme();
      cfg.theme.hudBackgroundColor = d.hudBackgroundColor;
      cfg.theme.hudBackgroundAlpha = d.hudBackgroundAlpha;
      cfg.theme.hudBorderColor = d.hudBorderColor;
      cfg.theme.hudBorderThickness = d.hudBorderThickness;
      cfg.theme.hudShowBorders = d.hudShowBorders;
      cfg.theme.slotOutlines = d.slotOutlines;
      cfg.theme.hudTextColor = d.hudTextColor;
      cfg.theme.hudAccentColor = d.hudAccentColor;
      cfg.theme.enableShadows = d.enableShadows;
      cfg.theme.hudTextBold = d.hudTextBold;
      cfg.theme.cornerRadius = d.cornerRadius;
      cfg.theme.hudGlass = d.hudGlass;
      cfg.theme.hudAccentBar = d.hudAccentBar;
      cfg.theme.themeAlphaPercent = d.themeAlphaPercent;
      cfg.gradients.remove(GradientKeys.HUD_TEXT);
      cfg.gradients.remove(GradientKeys.HUD_ACCENT);
      cfg.gradients.remove(GradientKeys.HUD_BG);
      ConfigManager.save(cfg);
   }

   /** Restore just one module's settings to their defaults. Reuses the dedicated reset helpers
    *  where they exist, and resets the relevant fields (from fresh default instances) otherwise. */
   private static void resetModuleDefaults(ModuleKind kind, TurtModConfig cfg) {
      TurtModConfig.Visual dv = new TurtModConfig.Visual();
      TurtModConfig.Hud dh = new TurtModConfig.Hud();
      TurtModConfig.Combat dc = new TurtModConfig.Combat();
      TurtModConfig.CustomTheme dtheme = new TurtModConfig.CustomTheme();
      TurtModConfig.DiscordRpc drpc = new TurtModConfig.DiscordRpc();
      TurtModConfig.Misc dm = new TurtModConfig.Misc();
      switch (kind) {
         case FULLBRIGHT -> {
            cfg.visual.fullbright.enabled = dv.fullbright.enabled;
            cfg.visual.fullbright.value = dv.fullbright.value;
         }
         case FREELOOK -> {
            cfg.visual.freelookEnabled = dv.freelookEnabled;
            cfg.visual.freelookSensitivityPercent = dv.freelookSensitivityPercent;
         }
         case HIT_COLOR -> resetHitColorDefaults(cfg);
         case LOW_FIRE -> {
            cfg.visual.disableFireOverlay = dv.disableFireOverlay;
            cfg.visual.fireYOffset = dv.fireYOffset;
         }
         case LOW_SHIELD -> resetShieldDefaults(cfg);
         case FOG_CONTROLS -> {
            cfg.visual.disableAllFog = dv.disableAllFog;
            cfg.visual.disableWaterFog = dv.disableWaterFog;
            cfg.visual.disableLavaFog = dv.disableLavaFog;
            cfg.visual.disableNetherFog = dv.disableNetherFog;
            cfg.visual.fogDensityPercent = dv.fogDensityPercent;
         }
         case OVERLAYS -> {
            cfg.visual.disablePumpkinBlur = dv.disablePumpkinBlur;
            cfg.visual.disablePowderSnowOverlay = dv.disablePowderSnowOverlay;
            cfg.visual.disableDarknessOverlay = dv.disableDarknessOverlay;
            cfg.visual.hidePortalOverlay = dv.hidePortalOverlay;
         }
         case CAMERA_SETTINGS -> {
            cfg.visual.hurtCamEnabled = dv.hurtCamEnabled;
            cfg.visual.hurtCamMode = dv.hurtCamMode;
            cfg.visual.hurtCameraShakePercent = dv.hurtCameraShakePercent;
         }
         case BLOCK_OUTLINE -> {
            cfg.visual.recolorBlockOutline = dv.recolorBlockOutline;
            cfg.visual.blockOutlineColor = dv.blockOutlineColor;
            cfg.visual.blockOutlineWidth = dv.blockOutlineWidth;
            cfg.visual.blockOutlineAlpha = dv.blockOutlineAlpha;
            cfg.visual.blockOutlineRainbow = dv.blockOutlineRainbow;
         }
         case SMALL_TOTEM -> {
            cfg.visual.enableSmallTotem = dv.enableSmallTotem;
            cfg.visual.totemScale = dv.totemScale;
            cfg.visual.totemOffsetX = dv.totemOffsetX;
            cfg.visual.totemOffsetY = dv.totemOffsetY;
            cfg.visual.totemOffsetZ = dv.totemOffsetZ;
            cfg.visual.totemPopScalePercent = dv.totemPopScalePercent;
            cfg.visual.totemPopOffsetX = dv.totemPopOffsetX;
            cfg.visual.totemPopOffsetY = dv.totemPopOffsetY;
            cfg.visual.totemPopOffsetZ = dv.totemPopOffsetZ;
            cfg.visual.disableTotemPopRotation = dv.disableTotemPopRotation;
            cfg.visual.disableTotemPopAnimation = dv.disableTotemPopAnimation;
         }
         case DISCORD_RPC -> {
            cfg.misc.discordRpc.enabled = drpc.enabled;
            cfg.misc.discordRpc.showUsername = drpc.showUsername;
            cfg.misc.discordRpc.showServerName = drpc.showServerName;
            cfg.misc.discordRpc.showDimension = drpc.showDimension;
         }
         case HELD_ITEM -> resetHeldItemDefaults(cfg);
         case ARMOR_HUD -> resetArmorHudDefaults(cfg);
         case POTION_HUD -> {
            cfg.hud.movablePotionHud = dh.movablePotionHud;
            cfg.hud.potionHudStyle = dh.potionHudStyle;
            cfg.hud.potionSortMode = dh.potionSortMode;
            cfg.hud.potionMaxRows = dh.potionMaxRows;
            cfg.hud.potionHudColumns = dh.potionHudColumns;
            cfg.hud.potionHudHorizontal = dh.potionHudHorizontal;
            cfg.hud.potionHudScalePercent = dh.potionHudScalePercent;
         }
         case FPS_PING -> {
            cfg.hud.minimalFpsPingOverlay = dh.minimalFpsPingOverlay;
            cfg.hud.pingHudEnabled = dh.pingHudEnabled;
            cfg.hud.pingHudScalePercent = dh.pingHudScalePercent;
            cfg.hud.overlayScalePercent = dh.overlayScalePercent;
         }
         case REACH -> {
            cfg.hud.reachDisplay = dh.reachDisplay;
            cfg.hud.reachShowTypeTag = dh.reachShowTypeTag;
            cfg.hud.reachShowEntityName = dh.reachShowEntityName;
            cfg.hud.reachDecimals = dh.reachDecimals;
         }
         case KEYSTROKES -> {
            cfg.hud.keystrokesHud = dh.keystrokesHud;
            cfg.hud.keystrokesShowCps = dh.keystrokesShowCps;
            cfg.hud.keystrokesUsePressedColor = dh.keystrokesUsePressedColor;
            cfg.hud.keystrokesPressedColor = dh.keystrokesPressedColor;
            cfg.hud.keystrokesPressedTextColor = dh.keystrokesPressedTextColor;
            cfg.hud.keystrokesHudScalePercent = dh.keystrokesHudScalePercent;
         }
         case CPS_COUNTER -> {
            cfg.hud.cpsCounterHud = dh.cpsCounterHud;
            cfg.hud.cpsShowBoth = dh.cpsShowBoth;
            cfg.hud.cpsShowBackground = dh.cpsShowBackground;
            cfg.hud.cpsCounterScalePercent = dh.cpsCounterScalePercent;
         }
         case INVENTORY_HUD -> {
            cfg.hud.inventoryHudEnabled = dh.inventoryHudEnabled;
            cfg.hud.inventoryHudBackground = dh.inventoryHudBackground;
            cfg.hud.inventoryHudScalePercent = dh.inventoryHudScalePercent;
         }
         case CUSTOM_HITBOXES -> {
            cfg.hud.customHitboxes = dh.customHitboxes;
            cfg.hud.cleanDebugHitboxes = dh.cleanDebugHitboxes;
            cfg.hud.hitboxPlayers = dh.hitboxPlayers;
            cfg.hud.hitboxHostile = dh.hitboxHostile;
            cfg.hud.hitboxPassive = dh.hitboxPassive;
            cfg.hud.hitboxOthers = dh.hitboxOthers;
            cfg.hud.hitboxSelf = dh.hitboxSelf;
            cfg.hud.hitboxMaxDistance = dh.hitboxMaxDistance;
            cfg.hud.hitboxColor = dh.hitboxColor;
            cfg.hud.hitboxChangeTargetColor = dh.hitboxChangeTargetColor;
            cfg.hud.hitboxTargetColor = dh.hitboxTargetColor;
            cfg.hud.hitboxHurtColorEnabled = dh.hitboxHurtColorEnabled;
            cfg.hud.hitboxHurtColor = dh.hitboxHurtColor;
            cfg.hud.hitboxHideFireworks = dh.hitboxHideFireworks;
            cfg.hud.hitboxShowInvisible = dh.hitboxShowInvisible;
         }
         case SCOREBOARD -> {
            cfg.visual.hideScoreboard = dv.hideScoreboard;
            cfg.visual.scoreboardHideNumbers = dv.scoreboardHideNumbers;
            cfg.visual.scoreboardHideBackground = dv.scoreboardHideBackground;
            cfg.visual.scoreboardScalePercent = dv.scoreboardScalePercent;
            cfg.visual.scoreboardOffsetX = dv.scoreboardOffsetX;
            cfg.visual.scoreboardOffsetY = dv.scoreboardOffsetY;
         }
         case BETTER_SCREENSHOT -> {
            cfg.hud.betterScreenshotActions = dh.betterScreenshotActions;
            cfg.hud.screenshotPreview = dh.screenshotPreview;
            cfg.hud.screenshotPreviewCorner = dh.screenshotPreviewCorner;
            cfg.hud.screenshotPreviewSeconds = dh.screenshotPreviewSeconds;
            cfg.hud.screenshotPreviewScalePercent = dh.screenshotPreviewScalePercent;
            cfg.hud.screenshotShutterSound = dh.screenshotShutterSound;
            cfg.hud.screenshotFlash = dh.screenshotFlash;
            cfg.hud.screenshotMenuButton = dh.screenshotMenuButton;
         }
         case MODULE_TOASTS -> {
            cfg.hud.moduleToasts = dh.moduleToasts;
            cfg.hud.moduleToastCorner = dh.moduleToastCorner;
            cfg.hud.moduleToastSeconds = dh.moduleToastSeconds;
            cfg.hud.moduleToastMaxVisible = dh.moduleToastMaxVisible;
            cfg.hud.moduleToastOffsetX = dh.moduleToastOffsetX;
            cfg.hud.moduleToastOffsetY = dh.moduleToastOffsetY;
         }
         case CLEAN_F3 -> resetCleanF3Defaults(cfg);
         case HEALTH_INDICATOR -> resetHealthIndicatorDefaults(cfg);
         case ZOOM -> resetZoomDefaults(cfg);
         case THEME_SETTINGS -> resetThemeDefaults(cfg);
         case ELYTRA_HUD -> {
            cfg.visual.elytraPitchHud = dv.elytraPitchHud;
            cfg.visual.elytraPitchShowYaw = dv.elytraPitchShowYaw;
         }
         case OWN_NAMETAG -> cfg.visual.showOwnNametag = dv.showOwnNametag;
         case COORDINATES_HUD -> {
            cfg.hud.coordinatesHud = dh.coordinatesHud;
            cfg.hud.coordinatesHudMode = dh.coordinatesHudMode;
            cfg.hud.coordinatesHudScalePercent = dh.coordinatesHudScalePercent;
            cfg.hud.coordsShowBackground = dh.coordsShowBackground;
            cfg.hud.coordsShowChunk = dh.coordsShowChunk;
            cfg.hud.coordsShowDirection = dh.coordsShowDirection;
            cfg.hud.coordsShowBiome = dh.coordsShowBiome;
            cfg.hud.coordsShowDimension = dh.coordsShowDimension;
            cfg.hud.coordsShowDay = dh.coordsShowDay;
         }
         case PING_DISPLAY -> {
            cfg.hud.pingInTab = dh.pingInTab;
            cfg.hud.pingTabAutoColor = dh.pingTabAutoColor;
            cfg.hud.pingTabFormat = dh.pingTabFormat;
            cfg.hud.pingTabColor = dh.pingTabColor;
            cfg.visual.pingOnNametag = dv.pingOnNametag;
            cfg.visual.pingNametagAutoColor = dv.pingNametagAutoColor;
            cfg.visual.pingNametagFormat = dv.pingNametagFormat;
            cfg.visual.pingNametagPosition = dv.pingNametagPosition;
         }
         case SPRINT_HUD -> {
            cfg.hud.toggleSprintHud = dh.toggleSprintHud;
            cfg.hud.sprintDisplayStyle = dh.sprintDisplayStyle;
            cfg.hud.sprintShowSneaking = dh.sprintShowSneaking;
            cfg.hud.sprintShowSwimming = dh.sprintShowSwimming;
            cfg.hud.toggleSprintHudScalePercent = dh.toggleSprintHudScalePercent;
         }
         case DEATH_COORDS -> cfg.misc.deathCoords = dm.deathCoords;
         case GAMEMODE_SWITCHER -> cfg.misc.noOpGamemodeSwitcher = dm.noOpGamemodeSwitcher;
         case MUTE_SOUNDS -> {
            cfg.misc.muteSoundsEnabled = dm.muteSoundsEnabled;
            cfg.misc.muteAnvil = dm.muteAnvil;
            cfg.misc.muteNoteBlocks = dm.muteNoteBlocks;
            cfg.misc.muteTotemPop = dm.muteTotemPop;
            cfg.misc.muteXpOrb = dm.muteXpOrb;
            cfg.misc.mutedSoundIds.clear();
         }
         case HIDE_PARTICLES -> {
            cfg.misc.hideParticlesEnabled = dm.hideParticlesEnabled;
            cfg.misc.hideParticles = dm.hideParticles;
            cfg.misc.particlesFast = dm.particlesFast;
            cfg.misc.particleLifePercent = dm.particleLifePercent;
            cfg.misc.hiddenParticleIds.clear();
         }
         case CLEAR_VIEW -> {
            cfg.misc.clearViewEnabled = dm.clearViewEnabled;
            cfg.misc.clearViewHidePotionParticles = dm.clearViewHidePotionParticles;
            cfg.misc.clearViewReduceEatingParticles = dm.clearViewReduceEatingParticles;
            cfg.misc.clearViewHideEatingParticles = dm.clearViewHideEatingParticles;
         }
         case CHAT_TWEAKS -> {
            cfg.hud.chatTweaksEnabled = dh.chatTweaksEnabled;
            cfg.hud.chatHistoryLength = dh.chatHistoryLength;
         }
         case COMMAND_KEYS -> {
            cfg.misc.commandKeysEnabled = dm.commandKeysEnabled;
            cfg.misc.commandKeys = new String[]{"", "", "", "", ""};
         }
         case FISHING_LINE -> {
            cfg.visual.fishingRodOverlay = dv.fishingRodOverlay;
            cfg.visual.fishingRodOverlayColor = dv.fishingRodOverlayColor;
            cfg.visual.fishingRodOverlayAlpha = dv.fishingRodOverlayAlpha;
         }
      }
      ConfigManager.save(cfg);
   }

   private static Category buildMiscCategory(TurtModConfig cfg) {
      return Category.createBuilder("Misc")
         .group(OptionGroup.createBuilder("General")
            .addOption(bool("Enable TurtMod", () -> cfg.misc.enabled, v -> cfg.misc.enabled = v))
            .addOption(bool("Container Buttons", () -> cfg.misc.containerButtons, v -> cfg.misc.containerButtons = v))
            .addOption(bool("Hide Scoreboard", () -> cfg.visual.hideScoreboard, v -> cfg.visual.hideScoreboard = v))
            .addOption(bool("Screenshot Tools", () -> cfg.hud.betterScreenshotActions, v -> cfg.hud.betterScreenshotActions = v))
            .addOption(bool("No-Op Gamemode Switcher", () -> cfg.misc.noOpGamemodeSwitcher, v -> cfg.misc.noOpGamemodeSwitcher = v))
            .build())
         .group(OptionGroup.createBuilder("Actions")
            .addOption(button("Manage Kits", () -> {
               class_310 mc = class_310.method_1551();
               mc.method_1507(new com.turtmod.kit.KitManagerScreen(mc.field_1755));
            }))
            .addOption(button("Command Keys", () -> {
               class_310 mc = class_310.method_1551();
               mc.method_1507(new com.turtmod.config.CommandKeysScreen(mc.field_1755));
            }))
            .build())
         .group(OptionGroup.createBuilder("Discord RPC")
            .addOption(bool("Enable Discord RPC", () -> cfg.misc.discordRpc.enabled, v -> cfg.misc.discordRpc.enabled = v))
            .addOption(bool("Show Username", () -> cfg.misc.discordRpc.showUsername, v -> cfg.misc.discordRpc.showUsername = v))
            .addOption(bool("Show Server Name", () -> cfg.misc.discordRpc.showServerName, v -> cfg.misc.discordRpc.showServerName = v))
            .addOption(bool("Show Dimension", () -> cfg.misc.discordRpc.showDimension, v -> cfg.misc.discordRpc.showDimension = v))
            .build())
         .build();
   }

   private static Category buildEditorCategory(TurtModConfig cfg) {
      return Category.createBuilder("Editor")
         .group(OptionGroup.createBuilder("HUD Editor")
            .addOption(bool("Snap To Grid", () -> cfg.hud.snapToGrid, v -> cfg.hud.snapToGrid = v))
            .addOption(intOpt("Grid Size", () -> cfg.hud.gridSize, v -> cfg.hud.gridSize = v, 1, 32, 1))
            .addOption(bool("Snap To Center", () -> cfg.hud.snapToCenter, v -> cfg.hud.snapToCenter = v))
            .addOption(intOpt("Center Snap Range", () -> cfg.hud.centerSnapRange, v -> cfg.hud.centerSnapRange = v, 1, 32, 1))
            .addOption(button("Open HUD Editor", () -> {
               class_310 mc = class_310.method_1551();
               if (mc != null) {
                  mc.method_1507(new HudEditorScreen(mc.field_1755));
               }
            }))
            .build())
         .build();
   }

   private static Category buildModuleCategory(ModuleKind kind, class_437 parent) {
      TurtModConfig cfg = TurtModClient.getConfig();
      OptionGroup.Builder group = OptionGroup.createBuilder("Settings");

      switch (kind) {
         case FULLBRIGHT -> group
            .addOption(bool("Enabled", () -> cfg.visual.fullbright.enabled, v -> cfg.visual.fullbright.enabled = v))
            .addOption(intOpt("Gamma Level", () -> (int)Math.round(cfg.visual.fullbright.value), v -> cfg.visual.fullbright.value = v, 1, 32, 1));
         case FREELOOK -> group
            .addOption(bool("Enabled", () -> cfg.visual.freelookEnabled, v -> cfg.visual.freelookEnabled = v))
            .addOption(intOpt("Sensitivity %", () -> cfg.visual.freelookSensitivityPercent, v -> cfg.visual.freelookSensitivityPercent = v, 25, 200, 5));
         case HIT_COLOR -> group
            .addOption(bool("Enabled", () -> cfg.visual.hitColor.enabled, v -> cfg.visual.hitColor.enabled = v))
            .addOption(color("Color", () -> cfg.visual.hitColor.color, v -> cfg.visual.hitColor.setColor(v)))
            .addOption(intOpt("Alpha", () -> cfg.visual.hitColor.alpha, v -> cfg.visual.hitColor.setAlpha(v), 0, 255, 5))
            .addOption(bool("Armor Damage Tint", () -> cfg.visual.armorDamageTint, v -> cfg.visual.armorDamageTint = v))
            .addOption(bool("Armor Trim Tint", () -> cfg.visual.armorDamageTintTrim, v -> cfg.visual.armorDamageTintTrim = v));
         case LOW_FIRE -> group
            .addOption(bool("Disable Fire Overlay", () -> cfg.visual.disableFireOverlay, v -> cfg.visual.disableFireOverlay = v))
            .addOption(intOpt("Fire Offset Y", () -> cfg.visual.fireYOffset, v -> cfg.visual.fireYOffset = v, -100, 100, 5));
         case LOW_SHIELD -> addShieldOptions(group, cfg);
         case FOG_CONTROLS -> group
            .addOption(bool("Disable All Fog", () -> cfg.visual.disableAllFog, v -> cfg.visual.disableAllFog = v))
            .addOption(bool("Disable Water Fog", () -> cfg.visual.disableWaterFog, v -> cfg.visual.disableWaterFog = v))
            .addOption(bool("Disable Lava Fog", () -> cfg.visual.disableLavaFog, v -> cfg.visual.disableLavaFog = v))
            .addOption(bool("Disable Nether Fog", () -> cfg.visual.disableNetherFog, v -> cfg.visual.disableNetherFog = v))
            .addOption(bool("Disable Powder Snow Fog", () -> cfg.visual.disablePowderSnowFog, v -> cfg.visual.disablePowderSnowFog = v))
            .addOption(bool("Disable Blindness Fog", () -> cfg.visual.disableBlindnessFog, v -> cfg.visual.disableBlindnessFog = v))
            .addOption(intOpt("Fog Distance %", () -> cfg.visual.fogDensityPercent, v -> cfg.visual.fogDensityPercent = v, 25, 400, 5));
         case OVERLAYS -> group
            .addOption(bool("Disable Pumpkin Blur", () -> cfg.visual.disablePumpkinBlur, v -> cfg.visual.disablePumpkinBlur = v))
            .addOption(bool("Disable Powder Snow", () -> cfg.visual.disablePowderSnowOverlay, v -> cfg.visual.disablePowderSnowOverlay = v))
            .addOption(bool("Disable Darkness", () -> cfg.visual.disableDarknessOverlay, v -> cfg.visual.disableDarknessOverlay = v))
            .addOption(bool("Hide Portal Overlay", () -> cfg.visual.hidePortalOverlay, v -> cfg.visual.hidePortalOverlay = v));
         case CAMERA_SETTINGS -> group
            .addOption(bool("Enabled", () -> cfg.visual.hurtCamEnabled, v -> cfg.visual.hurtCamEnabled = v))
            .addOption(enumOpt("Mode", () -> cfg.visual.hurtCamMode, v -> cfg.visual.hurtCamMode = v, TurtModConfig.HurtCamMode.class))
            .addOption(intOpt("Shake %", () -> cfg.visual.hurtCameraShakePercent, v -> cfg.visual.hurtCameraShakePercent = v, 0, 150, 5));
         case BLOCK_OUTLINE -> group
            .addOption(bool("Enabled", () -> cfg.visual.recolorBlockOutline, v -> cfg.visual.recolorBlockOutline = v))
            .addOption(color("Color", () -> cfg.visual.blockOutlineColor, v -> cfg.visual.blockOutlineColor = v))
            .addOption(intOpt("Width", () -> cfg.visual.blockOutlineWidth, v -> cfg.visual.blockOutlineWidth = v, 1, 10, 1))
            .addOption(intOpt("Opacity", () -> cfg.visual.blockOutlineAlpha, v -> cfg.visual.blockOutlineAlpha = v, 0, 255, 5))
            .addOption(bool("Rainbow", () -> cfg.visual.blockOutlineRainbow, v -> cfg.visual.blockOutlineRainbow = v));
         case SMALL_TOTEM -> group
            .addOption(bool("Enabled", () -> cfg.visual.enableSmallTotem, v -> cfg.visual.enableSmallTotem = v))
            .addOption(intOpt("Totem Scale %", () -> Math.round(cfg.visual.totemScale * 100.0F), v -> cfg.visual.totemScale = (float)v / 100.0F, 25, 150, 5))
            .addOption(intOpt("Totem Offset X", () -> Math.round(cfg.visual.totemOffsetX * 100.0F), v -> cfg.visual.totemOffsetX = (float)v / 100.0F, -100, 100, 5))
            .addOption(intOpt("Totem Offset Y", () -> Math.round(cfg.visual.totemOffsetY * 100.0F), v -> cfg.visual.totemOffsetY = (float)v / 100.0F, -100, 100, 5))
            .addOption(intOpt("Totem Offset Z", () -> Math.round(cfg.visual.totemOffsetZ * 100.0F), v -> cfg.visual.totemOffsetZ = (float)v / 100.0F, -100, 100, 5))
            .addOption(intOpt("Totem Pop Scale %", () -> cfg.visual.totemPopScalePercent, v -> cfg.visual.totemPopScalePercent = v, 25, 200, 5))
            .addOption(intOpt("Totem Pop Offset X", () -> cfg.visual.totemPopOffsetX, v -> cfg.visual.totemPopOffsetX = v, -100, 100, 1))
            .addOption(intOpt("Totem Pop Offset Y", () -> cfg.visual.totemPopOffsetY, v -> cfg.visual.totemPopOffsetY = v, -100, 100, 1))
            .addOption(intOpt("Totem Pop Offset Z", () -> cfg.visual.totemPopOffsetZ, v -> cfg.visual.totemPopOffsetZ = v, -100, 100, 1))
            .addOption(bool("Disable Pop Rotation", () -> cfg.visual.disableTotemPopRotation, v -> cfg.visual.disableTotemPopRotation = v))
            .addOption(bool("Disable Pop Animation", () -> cfg.visual.disableTotemPopAnimation, v -> cfg.visual.disableTotemPopAnimation = v));
         case DISCORD_RPC -> group
            .addOption(bool("Enabled", () -> cfg.misc.discordRpc.enabled, v -> cfg.misc.discordRpc.enabled = v))
            .addOption(bool("Show Username", () -> cfg.misc.discordRpc.showUsername, v -> cfg.misc.discordRpc.showUsername = v))
            .addOption(bool("Show Server Name", () -> cfg.misc.discordRpc.showServerName, v -> cfg.misc.discordRpc.showServerName = v))
            .addOption(bool("Show Dimension", () -> cfg.misc.discordRpc.showDimension, v -> cfg.misc.discordRpc.showDimension = v));
         case HELD_ITEM -> group
            .addOption(bool("Held Item Tweaks", () -> cfg.visual.heldItemTweaksEnabled, v -> cfg.visual.heldItemTweaksEnabled = v))
            .addOption(intOpt("Held Item Scale %", () -> cfg.visual.heldItemScalePercent, v -> cfg.visual.heldItemScalePercent = v, 50, 150, 5))
            .addOption(bool("Custom Main Hand XYZ", () -> cfg.visual.customHeldItemSize, v -> cfg.visual.customHeldItemSize = v))
            .addOption(numOpt("Main Scale X", () -> cfg.visual.heldItemScaleX, v -> cfg.visual.heldItemScaleX = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Main Scale Y", () -> cfg.visual.heldItemScaleY, v -> cfg.visual.heldItemScaleY = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Main Scale Z", () -> cfg.visual.heldItemScaleZ, v -> cfg.visual.heldItemScaleZ = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Main Pos X", () -> cfg.visual.heldItemPosX, v -> cfg.visual.heldItemPosX = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Main Pos Y", () -> cfg.visual.heldItemPosY, v -> cfg.visual.heldItemPosY = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Main Pos Z", () -> cfg.visual.heldItemPosZ, v -> cfg.visual.heldItemPosZ = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Main Rotate X", () -> cfg.visual.heldItemRotX, v -> cfg.visual.heldItemRotX = v, -180.0F, 180.0F, 5.0F))
            .addOption(numOpt("Main Rotate Y", () -> cfg.visual.heldItemRotY, v -> cfg.visual.heldItemRotY = v, -180.0F, 180.0F, 5.0F))
            .addOption(numOpt("Main Rotate Z", () -> cfg.visual.heldItemRotZ, v -> cfg.visual.heldItemRotZ = v, -180.0F, 180.0F, 5.0F))
            .addOption(bool("Custom Offhand XYZ", () -> cfg.visual.customOffhandHeldItemSize, v -> cfg.visual.customOffhandHeldItemSize = v))
            .addOption(numOpt("Offhand Scale X", () -> cfg.visual.offhandHeldItemScaleX, v -> cfg.visual.offhandHeldItemScaleX = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Offhand Scale Y", () -> cfg.visual.offhandHeldItemScaleY, v -> cfg.visual.offhandHeldItemScaleY = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Offhand Scale Z", () -> cfg.visual.offhandHeldItemScaleZ, v -> cfg.visual.offhandHeldItemScaleZ = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Offhand Pos X", () -> cfg.visual.offhandHeldItemPosX, v -> cfg.visual.offhandHeldItemPosX = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Offhand Pos Y", () -> cfg.visual.offhandHeldItemPosY, v -> cfg.visual.offhandHeldItemPosY = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Offhand Pos Z", () -> cfg.visual.offhandHeldItemPosZ, v -> cfg.visual.offhandHeldItemPosZ = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Offhand Rotate X", () -> cfg.visual.offhandHeldItemRotX, v -> cfg.visual.offhandHeldItemRotX = v, -180.0F, 180.0F, 5.0F))
            .addOption(numOpt("Offhand Rotate Y", () -> cfg.visual.offhandHeldItemRotY, v -> cfg.visual.offhandHeldItemRotY = v, -180.0F, 180.0F, 5.0F))
            .addOption(numOpt("Offhand Rotate Z", () -> cfg.visual.offhandHeldItemRotZ, v -> cfg.visual.offhandHeldItemRotZ = v, -180.0F, 180.0F, 5.0F));
         case ARMOR_HUD -> group
            .addOption(bool("Enabled", () -> cfg.hud.movableArmorHud, v -> cfg.hud.movableArmorHud = v))
            .addOption(bool("Vertical", () -> cfg.hud.armorHudVertical, v -> cfg.hud.armorHudVertical = v))
            .addOption(enumOpt("Style", () -> cfg.hud.armorHudStyle, v -> {
               cfg.hud.armorHudStyle = v;
               cfg.hud.armorHudHotbarStyle = v == TurtModConfig.ArmorHudStyle.HOTBAR;
            }, TurtModConfig.ArmorHudStyle.class))
            .addOption(enumOpt("Side", () -> cfg.hud.armorHudSide, v -> cfg.hud.armorHudSide = v, TurtModConfig.ArmorHudSide.class))
            .addOption(enumOpt("Durability Mode", () -> cfg.hud.armorHudDurabilityMode, v -> {
               cfg.hud.armorHudDurabilityMode = v;
               cfg.hud.armorHudShowDurability = v != TurtModConfig.ArmorHudDurabilityMode.OFF;
            }, TurtModConfig.ArmorHudDurabilityMode.class))
            .addOption(bool("Show Main Hand", () -> cfg.hud.armorHudShowMainHand, v -> cfg.hud.armorHudShowMainHand = v))
            .addOption(bool("Show Offhand", () -> cfg.hud.armorHudShowOffhand, v -> cfg.hud.armorHudShowOffhand = v))
            .addOption(bool("Low Durability Warning", () -> cfg.hud.armorHudWarnings, v -> cfg.hud.armorHudWarnings = v))
            .addOption(bool("Text Color at Full Durability", () -> cfg.hud.armorHudFullDurabilityTextColor, v -> cfg.hud.armorHudFullDurabilityTextColor = v))
            .addOption(intOpt("Warning Threshold %", () -> cfg.hud.armorHudWarningThresholdPercent, v -> cfg.hud.armorHudWarningThresholdPercent = v, 1, 100, 1))
            .addOption(intOpt("Scale %", () -> cfg.hud.armorHudScalePercent, v -> cfg.hud.armorHudScalePercent = v, 50, 300, 5));
         case POTION_HUD -> group
            .addOption(bool("Enabled", () -> cfg.hud.movablePotionHud, v -> cfg.hud.movablePotionHud = v))
            .addOption(enumOpt("Style", () -> cfg.hud.potionHudStyle, v -> cfg.hud.potionHudStyle = v, TurtModConfig.PotionHudStyle.class))
            .addOption(enumOpt("Sort By", () -> cfg.hud.potionSortMode, v -> cfg.hud.potionSortMode = v, TurtModConfig.PotionSortMode.class))
            .addOption(bool("Horizontal Layout", () -> cfg.hud.potionHudHorizontal, v -> cfg.hud.potionHudHorizontal = v))
            .addOption(intOpt("Columns", () -> cfg.hud.potionHudColumns, v -> cfg.hud.potionHudColumns = v, 1, 4, 1))
            .addOption(bool("Show Flags", () -> cfg.hud.potionShowFlags, v -> cfg.hud.potionShowFlags = v))
            .addOption(bool("Compact Timer", () -> cfg.hud.potionTimerCompact, v -> cfg.hud.potionTimerCompact = v))
            .addOption(bool("Clock Timer (1:30)", () -> cfg.hud.potionTimerClock, v -> cfg.hud.potionTimerClock = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.potionHudScalePercent, v -> cfg.hud.potionHudScalePercent = v, 50, 300, 5));
         case FPS_PING -> group
            .addOption(bool("FPS HUD", () -> cfg.hud.minimalFpsPingOverlay, v -> cfg.hud.minimalFpsPingOverlay = v))
            .addOption(bool("Ping HUD", () -> cfg.hud.pingHudEnabled, v -> cfg.hud.pingHudEnabled = v))
            .addOption(bool("Color-Coded FPS", () -> cfg.hud.fpsColorCoded, v -> cfg.hud.fpsColorCoded = v))
            .addOption(intOpt("FPS Scale %", () -> cfg.hud.overlayScalePercent, v -> cfg.hud.overlayScalePercent = v, 50, 300, 5))
            .addOption(intOpt("Ping Scale %", () -> cfg.hud.pingHudScalePercent, v -> cfg.hud.pingHudScalePercent = v, 50, 300, 5))
            .addOption(bool("Background", () -> cfg.hud.fpsPingShowBackground, v -> cfg.hud.fpsPingShowBackground = v));
         case REACH -> group
            .addOption(bool("Enabled", () -> cfg.hud.reachDisplay, v -> cfg.hud.reachDisplay = v))
            .addOption(bool("Reach Type Tag", () -> cfg.hud.reachShowTypeTag, v -> cfg.hud.reachShowTypeTag = v))
            .addOption(bool("Reach Entity Name", () -> cfg.hud.reachShowEntityName, v -> cfg.hud.reachShowEntityName = v))
            .addOption(intOpt("Reach Decimals", () -> cfg.hud.reachDecimals, v -> cfg.hud.reachDecimals = v, 0, 3, 1))
            .addOption(bool("Background", () -> cfg.hud.reachShowBackground, v -> cfg.hud.reachShowBackground = v));
         case KEYSTROKES -> group
            .addOption(bool("Enabled", () -> cfg.hud.keystrokesHud, v -> cfg.hud.keystrokesHud = v))
            .addOption(bool("Show CPS", () -> cfg.hud.keystrokesShowCps, v -> cfg.hud.keystrokesShowCps = v))
            .addOption(bool("Custom Pressed Color", () -> cfg.hud.keystrokesUsePressedColor, v -> cfg.hud.keystrokesUsePressedColor = v))
            .addOption(color("Pressed Color", () -> cfg.hud.keystrokesPressedColor, v -> cfg.hud.keystrokesPressedColor = v, GradientKeys.KEYSTROKES_PRESSED))
            .addOption(color("Pressed Text Color", () -> cfg.hud.keystrokesPressedTextColor, v -> cfg.hud.keystrokesPressedTextColor = v, GradientKeys.KEYSTROKES_PRESSED_TEXT))
            .addOption(intOpt("Scale %", () -> cfg.hud.keystrokesHudScalePercent, v -> cfg.hud.keystrokesHudScalePercent = v, 50, 300, 5));
         case CPS_COUNTER -> group
            .addOption(bool("Enabled", () -> cfg.hud.cpsCounterHud, v -> cfg.hud.cpsCounterHud = v))
            .addOption(bool("Show Both", () -> cfg.hud.cpsShowBoth, v -> cfg.hud.cpsShowBoth = v))
            .addOption(bool("Show Background", () -> cfg.hud.cpsShowBackground, v -> cfg.hud.cpsShowBackground = v))
            .addOption(bool("Rainbow Text", () -> cfg.hud.cpsRainbow, v -> cfg.hud.cpsRainbow = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.cpsCounterScalePercent, v -> cfg.hud.cpsCounterScalePercent = v, 50, 300, 5));
         case SPRINT_HUD -> group
            .addOption(bool("Enabled", () -> cfg.hud.toggleSprintHud, v -> cfg.hud.toggleSprintHud = v))
            .addOption(enumOpt("Style", () -> cfg.hud.sprintDisplayStyle, v -> cfg.hud.sprintDisplayStyle = v, TurtModConfig.SprintDisplayStyle.class))
            .addOption(bool("Show Sneaking", () -> cfg.hud.sprintShowSneaking, v -> cfg.hud.sprintShowSneaking = v))
            .addOption(bool("Show Swimming", () -> cfg.hud.sprintShowSwimming, v -> cfg.hud.sprintShowSwimming = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.toggleSprintHudScalePercent, v -> cfg.hud.toggleSprintHudScalePercent = v, 50, 300, 5))
            .addOption(bool("Background", () -> cfg.hud.sprintShowBackground, v -> cfg.hud.sprintShowBackground = v));
         case INVENTORY_HUD -> group
            .addOption(bool("Enabled", () -> cfg.hud.inventoryHudEnabled, v -> cfg.hud.inventoryHudEnabled = v))
            .addOption(bool("Show Background", () -> cfg.hud.inventoryHudBackground, v -> cfg.hud.inventoryHudBackground = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.inventoryHudScalePercent, v -> cfg.hud.inventoryHudScalePercent = v, 50, 150, 5));
         case CUSTOM_HITBOXES -> addHitboxOptions(group, cfg);
         case SCOREBOARD -> group
            .addOption(bool("Hide Scoreboard", () -> cfg.visual.hideScoreboard, v -> cfg.visual.hideScoreboard = v))
            .addOption(bool("Hide Numbers", () -> cfg.visual.scoreboardHideNumbers, v -> cfg.visual.scoreboardHideNumbers = v))
            .addOption(bool("Hide Background", () -> cfg.visual.scoreboardHideBackground, v -> cfg.visual.scoreboardHideBackground = v))
            .addOption(intOpt("Scale %", () -> cfg.visual.scoreboardScalePercent, v -> cfg.visual.scoreboardScalePercent = v, 30, 200, 5))
            .addOption(intOpt("Offset X", () -> cfg.visual.scoreboardOffsetX, v -> cfg.visual.scoreboardOffsetX = v, -400, 200, 2))
            .addOption(intOpt("Offset Y", () -> cfg.visual.scoreboardOffsetY, v -> cfg.visual.scoreboardOffsetY = v, -200, 200, 2));
         case BETTER_SCREENSHOT -> group
            .addOption(bool("Chat Actions", () -> cfg.hud.betterScreenshotActions, v -> cfg.hud.betterScreenshotActions = v))
            .addOption(bool("Corner Preview", () -> cfg.hud.screenshotPreview, v -> cfg.hud.screenshotPreview = v))
            .addOption(enumOpt("Preview Corner", () -> cfg.hud.screenshotPreviewCorner, v -> cfg.hud.screenshotPreviewCorner = v, TurtModConfig.ScreenshotCorner.class))
            .addOption(intOpt("Preview Seconds", () -> cfg.hud.screenshotPreviewSeconds, v -> cfg.hud.screenshotPreviewSeconds = v, 1, 15, 1))
            .addOption(intOpt("Preview Size %", () -> cfg.hud.screenshotPreviewScalePercent, v -> cfg.hud.screenshotPreviewScalePercent = v, 40, 200, 5))
            .addOption(bool("Shutter Sound", () -> cfg.hud.screenshotShutterSound, v -> cfg.hud.screenshotShutterSound = v))
            .addOption(bool("Camera Flash", () -> cfg.hud.screenshotFlash, v -> cfg.hud.screenshotFlash = v))
            .addOption(bool("Pause-Menu Gallery Button", () -> cfg.hud.screenshotMenuButton, v -> cfg.hud.screenshotMenuButton = v));
         case MODULE_TOASTS -> group
            .addOption(bool("Enabled", () -> cfg.hud.moduleToasts, v -> cfg.hud.moduleToasts = v))
            .addOption(enumOpt("Corner", () -> cfg.hud.moduleToastCorner, v -> cfg.hud.moduleToastCorner = v, TurtModConfig.ScreenshotCorner.class))
            .addOption(intOpt("Duration (seconds)", () -> cfg.hud.moduleToastSeconds, v -> cfg.hud.moduleToastSeconds = v, 1, 10, 1))
            .addOption(intOpt("Max Visible", () -> cfg.hud.moduleToastMaxVisible, v -> cfg.hud.moduleToastMaxVisible = v, 1, 8, 1))
            .addOption(intOpt("Offset X", () -> cfg.hud.moduleToastOffsetX, v -> cfg.hud.moduleToastOffsetX = v, -200, 200, 2))
            .addOption(intOpt("Offset Y", () -> cfg.hud.moduleToastOffsetY, v -> cfg.hud.moduleToastOffsetY = v, -200, 200, 2));
         case CLEAN_F3 -> group
            .addOption(bool("Enabled", () -> cfg.hud.cleanF3Mode, v -> cfg.hud.cleanF3Mode = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.cleanF3ScalePercent, v -> cfg.hud.cleanF3ScalePercent = v, 50, 300, 5))
            .addOption(bool("Background", () -> cfg.hud.cleanF3ShowBackground, v -> cfg.hud.cleanF3ShowBackground = v))
            .addOption(bool("Right Column (system info)", () -> cfg.hud.cleanF3RightColumn, v -> cfg.hud.cleanF3RightColumn = v))
            .addOption(bool("Theme Colours", () -> cfg.hud.cleanF3ThemeColors, v -> cfg.hud.cleanF3ThemeColors = v))
            .addOption(color("Label Color", () -> cfg.hud.cleanF3LabelColor, v -> cfg.hud.cleanF3LabelColor = v, GradientKeys.CLEAN_F3_LABEL))
            .addOption(color("Value Color", () -> cfg.hud.cleanF3ValueColor, v -> cfg.hud.cleanF3ValueColor = v, GradientKeys.CLEAN_F3_VALUE))
            .addOption(button("Reorder Lines", () -> {
               class_310 mc = class_310.method_1551();
               if (mc != null) {
                  mc.method_1507(new CleanF3OrderScreen(mc.field_1755));
               }
            }))
            .addOption(bool("Show FPS/Ping", () -> cfg.hud.cleanF3ShowFpsPing, v -> cfg.hud.cleanF3ShowFpsPing = v))
            .addOption(bool("Show FPS Min/Max", () -> cfg.hud.cleanF3ShowFpsExtremes, v -> cfg.hud.cleanF3ShowFpsExtremes = v))
            .addOption(bool("Show Position", () -> cfg.hud.cleanF3ShowPosition, v -> cfg.hud.cleanF3ShowPosition = v))
            .addOption(bool("Show Chunk", () -> cfg.hud.cleanF3ShowChunk, v -> cfg.hud.cleanF3ShowChunk = v))
            .addOption(bool("Show Light", () -> cfg.hud.cleanF3ShowLight, v -> cfg.hud.cleanF3ShowLight = v))
            .addOption(bool("Show Facing", () -> cfg.hud.cleanF3ShowFacing, v -> cfg.hud.cleanF3ShowFacing = v))
            .addOption(bool("Show Speed", () -> cfg.hud.cleanF3ShowSpeed, v -> cfg.hud.cleanF3ShowSpeed = v))
            .addOption(bool("Show Biome", () -> cfg.hud.cleanF3ShowBiome, v -> cfg.hud.cleanF3ShowBiome = v))
            .addOption(bool("Show Dimension", () -> cfg.hud.cleanF3ShowDimension, v -> cfg.hud.cleanF3ShowDimension = v))
            .addOption(bool("Show Day/Time", () -> cfg.hud.cleanF3ShowDayTime, v -> cfg.hud.cleanF3ShowDayTime = v))
            .addOption(bool("Show Held Item", () -> cfg.hud.cleanF3ShowHeldItem, v -> cfg.hud.cleanF3ShowHeldItem = v))
            .addOption(bool("Show Memory", () -> cfg.hud.cleanF3ShowMemory, v -> cfg.hud.cleanF3ShowMemory = v))
            .addOption(bool("Show Looking At", () -> cfg.hud.cleanF3ShowLookingAt, v -> cfg.hud.cleanF3ShowLookingAt = v));
         case HEALTH_INDICATOR -> group
            .addOption(bool("Enabled", () -> cfg.combat.playerHealthIndicator, v -> cfg.combat.playerHealthIndicator = v))
            .addOption(bool("Show Invisible Armored Players", () -> cfg.combat.playerHealthIndicatorArmorOnly, v -> cfg.combat.playerHealthIndicatorArmorOnly = v))
            .addOption(enumOpt("Style", () -> cfg.combat.playerHealthIndicatorStyle, v -> cfg.combat.playerHealthIndicatorStyle = v, TurtModConfig.PlayerHealthIndicatorStyle.class))
            .addOption(intOpt("Max Hearts", () -> cfg.combat.playerHealthIndicatorMaxHearts, v -> cfg.combat.playerHealthIndicatorMaxHearts = v, 1, 40, 1))
            .addOption(numOpt("Vertical Offset", () -> cfg.combat.playerHealthIndicatorYOffset, v -> cfg.combat.playerHealthIndicatorYOffset = v, -2.0F, 3.0F, 0.1F))
            .addOption(intOpt("Sprite Scale %", () -> cfg.combat.playerHealthIndicatorSpriteScalePercent, v -> cfg.combat.playerHealthIndicatorSpriteScalePercent = v, 25, 300, 5))
            .addOption(bool("Exact Health Widget", () -> cfg.combat.showExactHealthNumber, v -> cfg.combat.showExactHealthNumber = v))
            .addOption(intOpt("Widget Offset X", () -> cfg.combat.healthOffsetX, v -> cfg.combat.healthOffsetX = v, -400, 400, 5))
            .addOption(intOpt("Widget Offset Y", () -> cfg.combat.healthOffsetY, v -> cfg.combat.healthOffsetY = v, -400, 400, 5))
            .addOption(intOpt("Widget Scale %", () -> cfg.combat.healthScalePercent, v -> cfg.combat.healthScalePercent = v, 50, 300, 5));
         case ZOOM -> group
            .addOption(bool("Enabled", () -> cfg.visual.zoomEnabled, v -> cfg.visual.zoomEnabled = v))
            .addOption(bool("Toggle Mode", () -> cfg.visual.zoomToggleMode, v -> cfg.visual.zoomToggleMode = v))
            .addOption(numOpt("Zoom Level", () -> cfg.visual.zoomBaseLevel, v -> cfg.visual.zoomBaseLevel = v, 1.0F, 10.0F, 0.5F))
            .addOption(bool("Smooth Zoom", () -> cfg.visual.zoomSmoothInOut, v -> cfg.visual.zoomSmoothInOut = v))
            .addOption(bool("Hide Arms", () -> cfg.visual.zoomHideArms, v -> cfg.visual.zoomHideArms = v));
         case THEME_SETTINGS -> group
            .addOption(color("Background Color", () -> cfg.theme.hudBackgroundColor, v -> cfg.theme.hudBackgroundColor = v, GradientKeys.HUD_BG))
            .addOption(intOpt("Background Opacity", () -> cfg.theme.hudBackgroundAlpha, v -> cfg.theme.hudBackgroundAlpha = v, 0, 255, 5))
            .addOption(color("Text Color", () -> cfg.theme.hudTextColor, v -> cfg.theme.hudTextColor = v, GradientKeys.HUD_TEXT))
            .addOption(color("Accent Color", () -> cfg.theme.hudAccentColor, v -> cfg.theme.hudAccentColor = v, GradientKeys.HUD_ACCENT))
            .addOption(bool("Text Shadow", () -> cfg.theme.enableShadows, v -> cfg.theme.enableShadows = v));
         case ELYTRA_HUD -> group
            .addOption(bool("Enabled", () -> cfg.visual.elytraPitchHud, v -> cfg.visual.elytraPitchHud = v))
            .addOption(bool("Show Yaw", () -> cfg.visual.elytraPitchShowYaw, v -> cfg.visual.elytraPitchShowYaw = v));
         case OWN_NAMETAG -> group.addOption(bool("Show Own Nametag", () -> cfg.visual.showOwnNametag, v -> cfg.visual.showOwnNametag = v));
         case COORDINATES_HUD -> group
            .addOption(bool("Enabled", () -> cfg.hud.coordinatesHud, v -> cfg.hud.coordinatesHud = v))
            .addOption(enumOpt("Display Mode", () -> cfg.hud.coordinatesHudMode, v -> cfg.hud.coordinatesHudMode = v, TurtModConfig.CoordinatesHudMode.class))
            .addOption(intOpt("Scale %", () -> cfg.hud.coordinatesHudScalePercent, v -> cfg.hud.coordinatesHudScalePercent = v, 50, 300, 10))
            .addOption(bool("Show Background", () -> cfg.hud.coordsShowBackground, v -> cfg.hud.coordsShowBackground = v))
            .addOption(bool("Show Chunk", () -> cfg.hud.coordsShowChunk, v -> cfg.hud.coordsShowChunk = v))
            .addOption(bool("Show Direction", () -> cfg.hud.coordsShowDirection, v -> cfg.hud.coordsShowDirection = v))
            .addOption(bool("Show Biome", () -> cfg.hud.coordsShowBiome, v -> cfg.hud.coordsShowBiome = v));
         case PING_DISPLAY -> group
            .addOption(bool("Show In Tab List", () -> cfg.hud.pingInTab, v -> cfg.hud.pingInTab = v))
            .addOption(bool("Tab Auto Color", () -> cfg.hud.pingTabAutoColor, v -> cfg.hud.pingTabAutoColor = v))
            .addOption(color("Tab Ping Color", () -> cfg.hud.pingTabColor, v -> cfg.hud.pingTabColor = v))
            .addOption(bool("Show On Nametag", () -> cfg.visual.pingOnNametag, v -> cfg.visual.pingOnNametag = v))
            .addOption(bool("Nametag Auto Color", () -> cfg.visual.pingNametagAutoColor, v -> cfg.visual.pingNametagAutoColor = v))
            .addOption(enumOpt("Nametag Position", () -> cfg.visual.pingNametagPosition, v -> cfg.visual.pingNametagPosition = v, TurtModConfig.PingTextPosition.class));
         case DEATH_COORDS -> group
            .addOption(bool("Show Death Coordinates", () -> cfg.misc.deathCoords, v -> cfg.misc.deathCoords = v));
         case MUTE_SOUNDS -> group
            .addOption(bool("Enabled", () -> cfg.misc.muteSoundsEnabled, v -> cfg.misc.muteSoundsEnabled = v))
            .addOption(bool("Mute Anvil", () -> cfg.misc.muteAnvil, v -> cfg.misc.muteAnvil = v))
            .addOption(bool("Mute Note Blocks", () -> cfg.misc.muteNoteBlocks, v -> cfg.misc.muteNoteBlocks = v))
            .addOption(bool("Mute Totem Pop", () -> cfg.misc.muteTotemPop, v -> cfg.misc.muteTotemPop = v))
            .addOption(bool("Mute XP Orb Pickup", () -> cfg.misc.muteXpOrb, v -> cfg.misc.muteXpOrb = v))
            .addOption(button("Pick Sounds to Mute...", () -> openPicker(
               "Mute Sounds", "Click a sound to mute/unmute (plays a preview)",
               registryIds(net.minecraft.class_7923.field_41172), cfg.misc.mutedSoundIds,
               RegistryPickerScreen.PreviewType.SOUND)));
         case HIDE_PARTICLES -> group
            .addOption(bool("Enabled", () -> cfg.misc.hideParticlesEnabled, v -> cfg.misc.hideParticlesEnabled = v))
            .addOption(bool("Hide All Particles", () -> cfg.misc.hideParticles, v -> cfg.misc.hideParticles = v))
            .addOption(bool("Fast Particles", () -> cfg.misc.particlesFast, v -> cfg.misc.particlesFast = v))
            .addOption(intOpt("Particle Lifetime %", () -> cfg.misc.particleLifePercent, v -> cfg.misc.particleLifePercent = v, 0, 100, 5))
            .addOption(button("Pick Particles to Hide...", () -> openPicker(
               "Hide Particles", "Click a particle to hide/show (spawns a preview)",
               registryIds(net.minecraft.class_7923.field_41180), cfg.misc.hiddenParticleIds,
               RegistryPickerScreen.PreviewType.PARTICLE)));
         case CLEAR_VIEW -> group
            .addOption(bool("Enabled", () -> cfg.misc.clearViewEnabled, v -> cfg.misc.clearViewEnabled = v))
            .addOption(bool("Hide Own Potion Particles", () -> cfg.misc.clearViewHidePotionParticles, v -> cfg.misc.clearViewHidePotionParticles = v))
            .addOption(bool("Shrink Eating Particles", () -> cfg.misc.clearViewReduceEatingParticles, v -> cfg.misc.clearViewReduceEatingParticles = v))
            .addOption(bool("Hide Eating Particles", () -> cfg.misc.clearViewHideEatingParticles, v -> cfg.misc.clearViewHideEatingParticles = v));
         case CHAT_TWEAKS -> group
            .addOption(bool("Enabled", () -> cfg.hud.chatTweaksEnabled, v -> cfg.hud.chatTweaksEnabled = v))
            .addOption(intOpt("Chat History", () -> cfg.hud.chatHistoryLength, v -> cfg.hud.chatHistoryLength = v, 100, 1000, 50));
         case COMMAND_KEYS -> group
            .addOption(bool("Enabled", () -> cfg.misc.commandKeysEnabled, v -> cfg.misc.commandKeysEnabled = v))
            .addOption(button("Edit Command Keys...", () -> {
               class_310 mc = class_310.method_1551();
               if (mc != null) {
                  mc.method_1507(new CommandKeysScreen(mc.field_1755));
               }
            }));
         case KIT_LOADER -> group
            .addOption(button("Open Kit Manager...", () -> {
               class_310 mc = class_310.method_1551();
               if (mc != null) {
                  mc.method_1507(new com.turtmod.kit.KitManagerScreen(mc.field_1755));
               }
            }))
            .addOption(button("Save/equip inventories. Needs creative or /gamemode perms.", () -> {}));
         case GAMEMODE_SWITCHER -> group
            .addOption(bool("Enabled", () -> cfg.misc.noOpGamemodeSwitcher, v -> cfg.misc.noOpGamemodeSwitcher = v))
            .addOption(button("F3+F4 opens the switcher even without local op.", () -> {}))
            .addOption(button("Applies via /gamemode — you still need server permission.", () -> {}));
         case FISHING_LINE -> group
            .addOption(bool("Enabled", () -> cfg.visual.fishingRodOverlay, v -> cfg.visual.fishingRodOverlay = v))
            .addOption(color("Line Color", () -> cfg.visual.fishingRodOverlayColor, v -> cfg.visual.fishingRodOverlayColor = v, GradientKeys.FISHING_LINE))
            .addOption(numOpt("Line Opacity", () -> cfg.visual.fishingRodOverlayAlpha, v -> cfg.visual.fishingRodOverlayAlpha = v, 0.1F, 1.0F, 0.05F));
      }

      // Modules that have a keybind get an in-config rebind button (also listed in MC Controls).
      if (TurtModClient.getModuleToggleKey(kind) != null) {
         group.addOption(keybindButton(kind, parent));
      }

      // Every module page gets a "Reset to Defaults" button that restores just that module's settings.
      group.addOption(button("Reset to Defaults", () -> resetModuleDefaults(kind, TurtModClient.getConfig())));

      return Category.createBuilder(getModuleDisplayName(kind)).group(group.build()).build();
   }

   private static void addHitboxOptions(OptionGroup.Builder group, TurtModConfig cfg) {
      group
         .addOption(bool("Enabled", () -> cfg.hud.customHitboxes, v -> cfg.hud.customHitboxes = v))
         .addOption(bool("Clean Debug Hitboxes", () -> cfg.hud.cleanDebugHitboxes, v -> cfg.hud.cleanDebugHitboxes = v))
         .addOption(bool("Players", () -> cfg.hud.hitboxPlayers, v -> cfg.hud.hitboxPlayers = v))
         .addOption(bool("Hostile", () -> cfg.hud.hitboxHostile, v -> cfg.hud.hitboxHostile = v))
         .addOption(bool("Passive", () -> cfg.hud.hitboxPassive, v -> cfg.hud.hitboxPassive = v))
         .addOption(bool("Others", () -> cfg.hud.hitboxOthers, v -> cfg.hud.hitboxOthers = v))
         .addOption(bool("Self", () -> cfg.hud.hitboxSelf, v -> cfg.hud.hitboxSelf = v))
         .addOption(intOpt("Max Distance", () -> cfg.hud.hitboxMaxDistance, v -> cfg.hud.hitboxMaxDistance = v, 8, 256, 1))
         .addOption(color("Hitbox Color", () -> cfg.hud.hitboxColor, v -> cfg.hud.hitboxColor = v))
         .addOption(button("Entity Colors...", () -> class_310.method_1551().method_1507(new com.turtmod.hud.HitboxColorsScreen(class_310.method_1551().field_1755))))
         .addOption(bool("Target Color", () -> cfg.hud.hitboxChangeTargetColor, v -> cfg.hud.hitboxChangeTargetColor = v))
         .addOption(color("Target Hitbox Color", () -> cfg.hud.hitboxTargetColor, v -> cfg.hud.hitboxTargetColor = v))
         .addOption(bool("Hurt Color", () -> cfg.hud.hitboxHurtColorEnabled, v -> cfg.hud.hitboxHurtColorEnabled = v))
         .addOption(color("Hurt Hitbox Color", () -> cfg.hud.hitboxHurtColor, v -> cfg.hud.hitboxHurtColor = v))
         .addOption(bool("Show Invisible Armored Players", () -> cfg.hud.hitboxShowInvisible, v -> cfg.hud.hitboxShowInvisible = v))
         .addOption(bool("Hide Fireworks", () -> cfg.hud.hitboxHideFireworks, v -> cfg.hud.hitboxHideFireworks = v));
   }

   private static void addShieldOptions(OptionGroup.Builder group, TurtModConfig cfg) {
      group
         .addOption(intOpt("Shield Offset Y", () -> cfg.visual.shieldYOffset, v -> cfg.visual.shieldYOffset = v, -100, 100, 5))
         .addOption(bool("Shield Status Recolor", () -> cfg.visual.shieldStatusRecolor, v -> cfg.visual.shieldStatusRecolor = v))
         .addOption(bool("Use Usable Color", () -> cfg.visual.shieldUseUsableColor, v -> cfg.visual.shieldUseUsableColor = v))
         .addOption(color("Usable Color", () -> cfg.visual.shieldUsableColor, v -> cfg.visual.shieldUsableColor = v))
         .addOption(bool("Use Broken Color", () -> cfg.visual.shieldUseBrokenColor, v -> cfg.visual.shieldUseBrokenColor = v))
         .addOption(color("Broken Color", () -> cfg.visual.shieldBrokenColor, v -> cfg.visual.shieldBrokenColor = v))
         .addOption(bool("Use Blocking Color", () -> cfg.visual.shieldUseUsingColor, v -> cfg.visual.shieldUseUsingColor = v))
         .addOption(color("Blocking Color", () -> cfg.visual.shieldUsingColor, v -> cfg.visual.shieldUsingColor = v))
         .addOption(bool("Self Only", () -> cfg.visual.shieldSelfOnly, v -> cfg.visual.shieldSelfOnly = v))
         .addOption(bool("Custom Shield Size", () -> cfg.visual.customShieldSize, v -> cfg.visual.customShieldSize = v))
         .addOption(numOpt("Self Shield Scale", () -> cfg.visual.selfShieldScale, v -> cfg.visual.selfShieldScale = v, 0.5F, 2.0F, 0.1F))
         .addOption(numOpt("Others Shield Scale", () -> cfg.visual.othersShieldScale, v -> cfg.visual.othersShieldScale = v, 0.5F, 2.0F, 0.1F))
         .addOption(bool("Fix Blocking Animation", () -> cfg.visual.shieldFixBlockingAnim, v -> cfg.visual.shieldFixBlockingAnim = v))
         .addOption(bool("Shield Sounds", () -> cfg.visual.shieldFixSounds, v -> cfg.visual.shieldFixSounds = v))
         .addOption(bool("Factor 5-Tick Delay", () -> cfg.visual.shieldFix5TickDelay, v -> cfg.visual.shieldFix5TickDelay = v));
   }

   /** Open a {@link RegistryPickerScreen} backed by the given config list, returning to the current
    *  module page when done. Each toggle saves immediately. */
   private static void openPicker(String title, String subtitle, java.util.List<String> allIds,
                                  java.util.List<String> selected, RegistryPickerScreen.PreviewType preview) {
      class_310 mc = class_310.method_1551();
      if (mc != null) {
         mc.method_1507(new RegistryPickerScreen(mc.field_1755, title, subtitle, allIds, selected,
            () -> ConfigManager.save(TurtModClient.getConfig()), preview));
      }
   }

   /** Sorted list of every id in a registry, as strings (e.g. "minecraft:flame"). */
   private static java.util.List<String> registryIds(net.minecraft.class_2378<?> registry) {
      return registry.method_10235().stream().map(net.minecraft.class_2960::toString).sorted().toList();
   }

   private static Path getConfigPath() {
      return FabricLoader.getInstance().getConfigDir().resolve("turtmod.json");
   }

   private static Option<Boolean> bool(String name, Supplier<Boolean> getter, Consumer<Boolean> setter) {
      return withDescription(new Option<>(name, getter, v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }, Boolean.class, null, null, null), autoDescription(name));
   }

   private static Option<Integer> intOpt(String name, Supplier<Integer> getter, Consumer<Integer> setter, int min, int max, int step) {
      return withDescription(new Option<>(name, getter, v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }, Integer.class, min, max, step), autoDescription(name));
   }

   private static Option<Float> numOpt(String name, Supplier<Float> getter, Consumer<Float> setter, float min, float max, float step) {
      return withDescription(new Option<>(name, getter, v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }, Float.class, min, max, step), autoDescription(name));
   }

   private static Option<ConfigColor> color(String name, Supplier<Integer> getter, Consumer<Integer> setter) {
      return withDescription(new Option<>(name, () -> toConfigColor(getter.get()), c -> {
         if (c != null) {
            setter.accept(c.getRGB());
         }
         ConfigManager.save(TurtModClient.getConfig());
      }, ConfigColor.class, null, null, null), autoDescription(name));
   }

   /** Colour option that also supports a gradient (Solid/Gradient editor in the picker), stored under {@code key}. */
   private static Option<ConfigColor> color(String name, Supplier<Integer> getter, Consumer<Integer> setter, String key) {
      return color(name, getter, setter).gradientKey(key);
   }

   private static Option<Runnable> button(String name, Runnable action) {
      return withDescription(new Option<Runnable>(name, () -> action, null, Runnable.class, null, null, null), autoDescription(name));
   }

   /** A button that shows the module's current keybind and opens the in-config rebind listener. */
   private static Option<Runnable> keybindButton(ModuleKind kind, class_437 parent) {
      net.minecraft.class_304 binding = TurtModClient.getModuleToggleKey(kind);
      String keyName = binding.method_16007().getString();
      Runnable action = () -> {
         class_310 mc = class_310.method_1551();
         if (mc != null) {
            mc.method_1507(new KeybindListenScreen(binding, () -> createForModule(parent, kind)));
         }
      };
      return withDescription(new Option<Runnable>("Keybind: " + keyName, () -> action, null, Runnable.class, null, null, null),
         "Click, then press a key or mouse button to bind it. ESC to unbind.");
   }

   private static <E extends Enum<E>> Option<E> enumOpt(String name, Supplier<E> getter, Consumer<E> setter, Class<E> type) {
      return withDescription(new Option<>(name, getter, v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }, type, null, null, null), autoDescription(name));
   }

   private static <T> Option<T> withDescription(Option<T> option, String description) {
      return option.description(OptionDescription.ofOrderedString(() -> description));
   }

   private static String autoDescription(String name) {
      if (name.startsWith("Open ")) {
         return "Opens " + name.substring(5).toLowerCase() + ".";
      }
      if (name.contains("Color")) {
         return "Changes " + name.toLowerCase() + ".";
      }
      if (name.contains("%") || name.contains("Scale") || name.contains("Distance") || name.contains("Offset")) {
         return "Adjusts " + name.toLowerCase() + ".";
      }
      return "Toggles " + name.toLowerCase() + ".";
   }

   private static void resetHeldItemDefaults(TurtModConfig cfg) {
      cfg.visual.heldItemTweaksEnabled = false;
      cfg.visual.heldItemScalePercent = 100;
      cfg.visual.customHeldItemSize = false;
      cfg.visual.heldItemScaleX = 1.0045872F;
      cfg.visual.heldItemScaleY = 1.0F;
      cfg.visual.heldItemScaleZ = 1.0F;
      cfg.visual.heldItemPosX = 0.0F;
      cfg.visual.heldItemPosY = 0.0F;
      cfg.visual.heldItemPosZ = 0.0F;
      cfg.visual.customOffhandHeldItemSize = false;
      cfg.visual.offhandHeldItemScaleX = 1.0311927F;
      cfg.visual.offhandHeldItemScaleY = 1.0F;
      cfg.visual.offhandHeldItemScaleZ = 1.0F;
      cfg.visual.offhandHeldItemPosX = 0.0F;
      cfg.visual.offhandHeldItemPosY = 0.0F;
      cfg.visual.offhandHeldItemPosZ = 0.0F;
      cfg.visual.heldItemRotX = 0.0F;
      cfg.visual.heldItemRotY = 0.0F;
      cfg.visual.heldItemRotZ = 0.0F;
      cfg.visual.offhandHeldItemRotX = 0.0F;
      cfg.visual.offhandHeldItemRotY = 0.0F;
      cfg.visual.offhandHeldItemRotZ = 0.0F;
      ConfigManager.save(cfg);
   }

   private static void resetHitColorDefaults(TurtModConfig cfg) {
      cfg.visual.hitColor.enabled = true;
      cfg.visual.hitColor.color = 1308557312;
      cfg.visual.hitColor.alpha = 73;
      cfg.visual.armorDamageTint = true;
      cfg.visual.armorDamageTintTrim = true;
      OverlayReloadListener.callEvent();
      ConfigManager.save(cfg);
   }

   private static void resetShieldDefaults(TurtModConfig cfg) {
      cfg.visual.shieldYOffset = -1;
      cfg.visual.shieldStatusRecolor = true;
      cfg.visual.shieldUseUsableColor = true;
      cfg.visual.shieldUsableColor = -16711936;
      cfg.visual.shieldUseBrokenColor = true;
      cfg.visual.shieldBrokenColor = -65536;
      cfg.visual.shieldUseUsingColor = false;
      cfg.visual.shieldUsingColor = -256;
      cfg.visual.shieldColorInterpolation = false;
      cfg.visual.shieldSelfOnly = false;
      cfg.visual.customShieldSize = false;
      cfg.visual.selfShieldScale = 1.0F;
      cfg.visual.selfShieldOffsetX = 0.0F;
      cfg.visual.selfShieldOffsetY = 0.0F;
      cfg.visual.othersShieldScale = 1.0F;
      cfg.visual.onlyShowShieldWhenBlocking = false;
      ConfigManager.save(cfg);
   }

   private static void resetArmorHudDefaults(TurtModConfig cfg) {
      cfg.hud.movableArmorHud = true;
      cfg.hud.armorHudX = 413;
      cfg.hud.armorHudY = 271;
      cfg.hud.armorHudVertical = true;
      cfg.hud.armorHudHotbarStyle = true;
      cfg.hud.armorHudStyle = TurtModConfig.ArmorHudStyle.HOTBAR;
      cfg.hud.armorHudSide = TurtModConfig.ArmorHudSide.RIGHT;
      cfg.hud.armorHudShowDurability = true;
      cfg.hud.armorHudFullDurabilityTextColor = true;
      cfg.hud.armorHudDurabilityMode = TurtModConfig.ArmorHudDurabilityMode.REMAINING;
      cfg.hud.armorHudShowMainHand = false;
      cfg.hud.armorHudShowOffhand = false;
      cfg.hud.armorHudReserveOffhandSpace = true;
      cfg.hud.armorHudWarnings = true;
      cfg.hud.armorHudWarningThresholdPercent = 20;
      cfg.hud.armorHudScalePercent = 100;
      ConfigManager.save(cfg);
   }

   private static void resetHealthIndicatorDefaults(TurtModConfig cfg) {
      cfg.combat.playerHealthIndicator = false;
      cfg.combat.playerHealthIndicatorArmorOnly = false;
      cfg.combat.playerHealthIndicatorStyle = TurtModConfig.PlayerHealthIndicatorStyle.SPRITE;
      cfg.combat.playerHealthIndicatorMaxHearts = 40;
      cfg.combat.playerHealthIndicatorYOffset = 0.47339442F;
      cfg.combat.playerHealthIndicatorSpriteScalePercent = 75;
      cfg.combat.showExactHealthNumber = false;
      cfg.combat.healthOffsetX = -320;
      cfg.combat.healthOffsetY = -156;
      cfg.combat.healthScalePercent = 54;
      ConfigManager.save(cfg);
   }

   private static void resetCleanF3Defaults(TurtModConfig cfg) {
      cfg.hud.cleanF3ShowFpsPing = true;
      cfg.hud.cleanF3ShowPosition = true;
      cfg.hud.cleanF3ShowFacing = true;
      cfg.hud.cleanF3ShowBiome = true;
      cfg.hud.cleanF3ShowLookingAt = true;
      cfg.hud.cleanF3ShowLight = true;
      cfg.hud.cleanF3ShowChunk = true;
      cfg.hud.cleanF3ShowBackground = true;
      cfg.hud.cleanF3ShowSpeed = false;
      cfg.hud.cleanF3ShowDayTime = false;
      cfg.hud.cleanF3ShowMemory = false;
      cfg.hud.cleanF3ShowHeldItem = false;
      cfg.hud.cleanF3ShowDimension = false;
      cfg.hud.cleanF3ShowFpsExtremes = false;
      cfg.hud.cleanF3LabelColor = 0xFF7FE08A;
      cfg.hud.cleanF3ValueColor = 0xFFE6E6E6;
      cfg.hud.cleanF3Order = TurtModConfig.Hud.defaultCleanF3Order();
      cfg.visual.cleanF3HideFPS = false;
      cfg.visual.cleanF3HidePing = false;
      cfg.visual.cleanF3HideCoordinates = false;
      cfg.visual.cleanF3HideChunkUpdates = false;
      cfg.visual.cleanF3HideBiome = false;
      cfg.visual.cleanF3CompactMode = false;
      ConfigManager.save(cfg);
   }

   private static void resetZoomDefaults(TurtModConfig cfg) {
      cfg.visual.zoomEnabled = true;
      cfg.visual.zoomToggleMode = false;
      cfg.visual.zoomBaseLevel = 2.733945F;
      cfg.visual.zoomInPerScroll = 0.09273395F;
      cfg.visual.zoomOutPerScroll = 0.06427523F;
      cfg.visual.zoomSmoothInOut = true;
      cfg.visual.zoomHideArms = true;
      cfg.visual.zoomNormalizeSensitivity = true;
      cfg.visual.zoomSmoothCamera = false;
      cfg.visual.zoomResetOnStop = true;
      ConfigManager.save(cfg);
   }

   private static ConfigColor toConfigColor(int argb) {
      int a = argb >>> 24 & 255;
      int r = argb >>> 16 & 255;
      int g = argb >>> 8 & 255;
      int b = argb & 255;
      return new ConfigColor(r, g, b, a);
   }

   public enum ModuleKind {
      FULLBRIGHT,
      FREELOOK,
      HIT_COLOR,
      LOW_FIRE,
      LOW_SHIELD,
      FOG_CONTROLS,
      OVERLAYS,
      CAMERA_SETTINGS,
      BLOCK_OUTLINE,
      SMALL_TOTEM,
      DISCORD_RPC,
      HELD_ITEM,
      ARMOR_HUD,
      POTION_HUD,
      FPS_PING,
      REACH,
      KEYSTROKES,
      CPS_COUNTER,
      SPRINT_HUD,
      INVENTORY_HUD,
      CUSTOM_HITBOXES,
      SCOREBOARD,
      BETTER_SCREENSHOT,
      CLEAN_F3,
      HEALTH_INDICATOR,
      ZOOM,
      THEME_SETTINGS,
      ELYTRA_HUD,
      OWN_NAMETAG,
      COORDINATES_HUD,
      PING_DISPLAY,
      DEATH_COORDS,
      MUTE_SOUNDS,
      HIDE_PARTICLES,
      CLEAR_VIEW,
      CHAT_TWEAKS,
      COMMAND_KEYS,
      KIT_LOADER,
      GAMEMODE_SWITCHER,
      MODULE_TOASTS,
      FISHING_LINE
   }
}
