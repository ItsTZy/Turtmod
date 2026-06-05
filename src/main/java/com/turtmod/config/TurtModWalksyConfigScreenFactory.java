package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.event.OverlayReloadListener;
import com.turtmod.hud.HudEditorScreen;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;
import main.walksy.lib.core.config.impl.LocalConfig;
import main.walksy.lib.core.config.local.Category;
import main.walksy.lib.core.config.local.Option;
import main.walksy.lib.core.config.local.OptionDescription;
import main.walksy.lib.core.config.local.options.BooleanOption;
import main.walksy.lib.core.config.local.options.ButtonOption;
import main.walksy.lib.core.config.local.options.ColorOption;
import main.walksy.lib.core.config.local.options.EnumOption;
import main.walksy.lib.core.config.local.options.NumericalOption;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import main.walksy.lib.core.gui.impl.WalksyLibConfigScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import net.minecraft.class_437;

public final class TurtModWalksyConfigScreenFactory {
   private TurtModWalksyConfigScreenFactory() {
   }

   public static class_437 create(class_437 parent) {
      TurtModConfig cfg = TurtModClient.getConfig();
      LocalConfig walksyConfig = LocalConfig.createBuilder("TurtMod")
         .path(getConfigPath())
         .category(buildVisualCategory(cfg))
         .category(buildHudCategory(cfg))
         .category(buildCombatCategory(cfg))
         .category(buildThemeCategory(cfg))
         .category(buildMiscCategory(cfg))
         .category(buildEditorCategory(cfg))
         .onSave(() -> ConfigManager.save(cfg))
         .build();
      walksyConfig.load();
      return new WalksyLibConfigScreen(parent, walksyConfig);
   }

   public static class_437 createForModule(class_437 parent, ModuleKind kind) {
      if (kind == null) {
         return create(parent);
      }

      LocalConfig walksyConfig = LocalConfig.createBuilder("TurtMod")
         .path(getConfigPath())
         .category(buildModuleCategory(kind, parent))
         .onSave(() -> ConfigManager.save(TurtModClient.getConfig()))
         .build();
      walksyConfig.load();
      return new WalksyLibConfigScreen(parent, walksyConfig);
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
         case INVENTORY_HUD -> "Inventory HUD";
         case CUSTOM_HITBOXES -> "Hitboxes";
         case SCOREBOARD -> "Hide Scoreboard";
         case BETTER_SCREENSHOT -> "Screenshot Tools";
         case CLEAN_F3 -> "Clean F3";
         case HEALTH_INDICATOR -> "Health Indicator";
         case ZOOM -> "Zoom";
         case THEME_SETTINGS -> "Theme Settings";
         case ELYTRA_HUD -> "Elytra Pitch HUD";
         case OWN_NAMETAG -> "Own Nametag";
         case COORDINATES_HUD -> "Coordinates HUD";
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
            .build())
         .group(OptionGroup.createBuilder("Items")
            .addOption(bool("Held Item Tweaks", () -> cfg.visual.heldItemTweaksEnabled, v -> cfg.visual.heldItemTweaksEnabled = v))
            .addOption(intOpt("Held Item Scale %", () -> cfg.visual.heldItemScalePercent, v -> cfg.visual.heldItemScalePercent = v, 50, 150, 5))
            .addOption(bool("Enable Small Totem", () -> cfg.visual.enableSmallTotem, v -> cfg.visual.enableSmallTotem = v))
            .addOption(intOpt("Totem Scale %", () -> Math.round(cfg.visual.totemScale * 100.0F), v -> cfg.visual.totemScale = (float)v / 100.0F, 25, 150, 5))
            .addOption(bool("Fishing Rod Overlay", () -> cfg.visual.fishingRodOverlay, v -> cfg.visual.fishingRodOverlay = v))
            .addOption(color("Fishing Overlay Color", () -> cfg.visual.fishingRodOverlayColor, v -> cfg.visual.fishingRodOverlayColor = v))
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
            .addOption(intOpt("Armor Scale %", () -> cfg.hud.armorHudScalePercent, v -> cfg.hud.armorHudScalePercent = v, 50, 300, 5))
            .addOption(bool("Movable Potion HUD", () -> cfg.hud.movablePotionHud, v -> cfg.hud.movablePotionHud = v))
            .addOption(enumOpt("Potion Style", () -> cfg.hud.potionHudStyle, v -> cfg.hud.potionHudStyle = v, TurtModConfig.PotionHudStyle.class))
            .addOption(enumOpt("Potion Sort", () -> cfg.hud.potionSortMode, v -> cfg.hud.potionSortMode = v, TurtModConfig.PotionSortMode.class))
            .addOption(intOpt("Potion Scale %", () -> cfg.hud.potionHudScalePercent, v -> cfg.hud.potionHudScalePercent = v, 50, 300, 5))
            .build())
         .group(OptionGroup.createBuilder("Counters & Info")
            .addOption(bool("FPS/Ping", () -> cfg.hud.minimalFpsPingOverlay, v -> cfg.hud.minimalFpsPingOverlay = v))
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
            .addOption(bool("Target Color", () -> cfg.hud.hitboxChangeTargetColor, v -> cfg.hud.hitboxChangeTargetColor = v))
            .addOption(color("Target Hitbox Color", () -> cfg.hud.hitboxTargetColor, v -> cfg.hud.hitboxTargetColor = v))
            .addOption(bool("Hurt Color", () -> cfg.hud.hitboxHurtColorEnabled, v -> cfg.hud.hitboxHurtColorEnabled = v))
            .addOption(color("Hurt Hitbox Color", () -> cfg.hud.hitboxHurtColor, v -> cfg.hud.hitboxHurtColor = v))
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
            .addOption(bool("Show Invisible Players", () -> cfg.combat.playerHealthIndicatorInvisible, v -> cfg.combat.playerHealthIndicatorInvisible = v))
            .addOption(enumOpt("Health Indicator Style", () -> cfg.combat.playerHealthIndicatorStyle, v -> cfg.combat.playerHealthIndicatorStyle = v, TurtModConfig.PlayerHealthIndicatorStyle.class))
            .addOption(intOpt("Health Indicator Max Hearts", () -> cfg.combat.playerHealthIndicatorMaxHearts, v -> cfg.combat.playerHealthIndicatorMaxHearts = v, 1, 40, 1))
            .addOption(bool("Exact Health Number", () -> cfg.combat.showExactHealthNumber, v -> cfg.combat.showExactHealthNumber = v))
            .addOption(bool("Own Nametag", () -> cfg.visual.showOwnNametag, v -> cfg.visual.showOwnNametag = v))
            .build())
         .group(OptionGroup.createBuilder("Cooldown")
            .addOption(bool("Cooldown Indicator", () -> cfg.combat.customAttackCooldownIndicator, v -> cfg.combat.customAttackCooldownIndicator = v))
            .addOption(bool("Charged Sound", () -> cfg.combat.chargedSound, v -> cfg.combat.chargedSound = v))
            .addOption(bool("Charged Color", () -> cfg.combat.chargedColor, v -> cfg.combat.chargedColor = v))
            .addOption(color("Charged Color", () -> cfg.combat.chargedColorArgb, v -> cfg.combat.chargedColorArgb = v))
            .addOption(color("Uncharged Color", () -> cfg.combat.unchargedColorArgb, v -> cfg.combat.unchargedColorArgb = v))
            .addOption(bool("Cooldown Outline", () -> cfg.combat.cooldownOutline, v -> cfg.combat.cooldownOutline = v))
            .build())
         .group(OptionGroup.createBuilder("Hit Color")
            .addOption(bool("Hit Color", () -> cfg.visual.hitColor.enabled, v -> cfg.visual.hitColor.enabled = v))
            .addOption(color("Hit Color", () -> cfg.visual.hitColor.color, v -> cfg.visual.hitColor.setColor(v)))
            .addOption(intOpt("Hit Alpha", () -> cfg.visual.hitColor.alpha, v -> cfg.visual.hitColor.setAlpha(v), 0, 255, 5))
            .addOption(bool("Armor Damage Tint", () -> cfg.visual.armorDamageTint, v -> cfg.visual.armorDamageTint = v))
            .build())
         .build();
   }

   private static Category buildThemeCategory(TurtModConfig cfg) {
      return Category.createBuilder("Theme")
         .group(OptionGroup.createBuilder("HUD Theme")
            .addOption(color("Background Color", () -> cfg.theme.hudBackgroundColor, v -> cfg.theme.hudBackgroundColor = v))
            .addOption(intOpt("Background Alpha", () -> cfg.theme.hudBackgroundAlpha, v -> cfg.theme.hudBackgroundAlpha = v, 0, 255, 5))
            .addOption(color("Border Color", () -> cfg.theme.hudBorderColor, v -> cfg.theme.hudBorderColor = v))
            .addOption(bool("Show Borders & Lines", () -> cfg.theme.hudShowBorders, v -> cfg.theme.hudShowBorders = v))
            .addOption(bool("Slot Outlines", () -> cfg.theme.slotOutlines, v -> cfg.theme.slotOutlines = v))
            .addOption(intOpt("Border Thickness", () -> cfg.theme.hudBorderThickness, v -> cfg.theme.hudBorderThickness = v, 0, 4, 1))
            .addOption(intOpt("Overall Opacity %", () -> cfg.theme.themeAlphaPercent, v -> cfg.theme.themeAlphaPercent = v, 0, 100, 5))
            .addOption(color("Text Color", () -> cfg.theme.hudTextColor, v -> cfg.theme.hudTextColor = v))
            .addOption(color("Accent Color", () -> cfg.theme.hudAccentColor, v -> cfg.theme.hudAccentColor = v))
            .addOption(bool("Text Shadows", () -> cfg.theme.enableShadows, v -> cfg.theme.enableShadows = v))
            .addOption(bool("Bold HUD Text", () -> cfg.theme.hudTextBold, v -> cfg.theme.hudTextBold = v))
            .addOption(intOpt("Corner Radius", () -> cfg.theme.cornerRadius, v -> cfg.theme.cornerRadius = v, 0, 12, 1))
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
      cfg.theme.themeAlphaPercent = d.themeAlphaPercent;
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
            cfg.visual.fogDistance = dv.fogDistance;
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
            cfg.hud.potionHudScalePercent = dh.potionHudScalePercent;
         }
         case FPS_PING -> {
            cfg.hud.minimalFpsPingOverlay = dh.minimalFpsPingOverlay;
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
         }
         case SCOREBOARD -> cfg.visual.hideScoreboard = dv.hideScoreboard;
         case BETTER_SCREENSHOT -> cfg.hud.betterScreenshotActions = dh.betterScreenshotActions;
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
            .addOption(bool("Armor Damage Tint", () -> cfg.visual.armorDamageTint, v -> cfg.visual.armorDamageTint = v));
         case LOW_FIRE -> group
            .addOption(bool("Disable Fire Overlay", () -> cfg.visual.disableFireOverlay, v -> cfg.visual.disableFireOverlay = v))
            .addOption(intOpt("Fire Offset Y", () -> cfg.visual.fireYOffset, v -> cfg.visual.fireYOffset = v, -100, 100, 5));
         case LOW_SHIELD -> addShieldOptions(group, cfg);
         case FOG_CONTROLS -> group
            .addOption(bool("Disable All Fog", () -> cfg.visual.disableAllFog, v -> cfg.visual.disableAllFog = v))
            .addOption(bool("Disable Water Fog", () -> cfg.visual.disableWaterFog, v -> cfg.visual.disableWaterFog = v))
            .addOption(bool("Disable Lava Fog", () -> cfg.visual.disableLavaFog, v -> cfg.visual.disableLavaFog = v))
            .addOption(bool("Disable Nether Fog", () -> cfg.visual.disableNetherFog, v -> cfg.visual.disableNetherFog = v))
            .addOption(intOpt("Fog Distance", () -> cfg.visual.fogDistance, v -> cfg.visual.fogDistance = v, 8, 256, 1));
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
            .addOption(bool("Custom Offhand XYZ", () -> cfg.visual.customOffhandHeldItemSize, v -> cfg.visual.customOffhandHeldItemSize = v))
            .addOption(numOpt("Offhand Scale X", () -> cfg.visual.offhandHeldItemScaleX, v -> cfg.visual.offhandHeldItemScaleX = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Offhand Scale Y", () -> cfg.visual.offhandHeldItemScaleY, v -> cfg.visual.offhandHeldItemScaleY = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Offhand Scale Z", () -> cfg.visual.offhandHeldItemScaleZ, v -> cfg.visual.offhandHeldItemScaleZ = v, 0.1F, 3.0F, 0.1F))
            .addOption(numOpt("Offhand Pos X", () -> cfg.visual.offhandHeldItemPosX, v -> cfg.visual.offhandHeldItemPosX = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Offhand Pos Y", () -> cfg.visual.offhandHeldItemPosY, v -> cfg.visual.offhandHeldItemPosY = v, -1.0F, 1.0F, 0.05F))
            .addOption(numOpt("Offhand Pos Z", () -> cfg.visual.offhandHeldItemPosZ, v -> cfg.visual.offhandHeldItemPosZ = v, -1.0F, 1.0F, 0.05F));
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
            .addOption(intOpt("Warning Threshold %", () -> cfg.hud.armorHudWarningThresholdPercent, v -> cfg.hud.armorHudWarningThresholdPercent = v, 1, 100, 1))
            .addOption(intOpt("Scale %", () -> cfg.hud.armorHudScalePercent, v -> cfg.hud.armorHudScalePercent = v, 50, 300, 5));
         case POTION_HUD -> group
            .addOption(bool("Enabled", () -> cfg.hud.movablePotionHud, v -> cfg.hud.movablePotionHud = v))
            .addOption(enumOpt("Style", () -> cfg.hud.potionHudStyle, v -> cfg.hud.potionHudStyle = v, TurtModConfig.PotionHudStyle.class))
            .addOption(enumOpt("Sort", () -> cfg.hud.potionSortMode, v -> cfg.hud.potionSortMode = v, TurtModConfig.PotionSortMode.class))
            .addOption(intOpt("Max Rows", () -> cfg.hud.potionMaxRows, v -> cfg.hud.potionMaxRows = v, 1, 12, 1))
            .addOption(intOpt("Columns", () -> cfg.hud.potionHudColumns, v -> cfg.hud.potionHudColumns = v, 1, 8, 1))
            .addOption(intOpt("Scale %", () -> cfg.hud.potionHudScalePercent, v -> cfg.hud.potionHudScalePercent = v, 50, 300, 5));
         case FPS_PING -> group
            .addOption(bool("Enabled", () -> cfg.hud.minimalFpsPingOverlay, v -> cfg.hud.minimalFpsPingOverlay = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.overlayScalePercent, v -> cfg.hud.overlayScalePercent = v, 50, 300, 5));
         case REACH -> group
            .addOption(bool("Enabled", () -> cfg.hud.reachDisplay, v -> cfg.hud.reachDisplay = v))
            .addOption(bool("Reach Type Tag", () -> cfg.hud.reachShowTypeTag, v -> cfg.hud.reachShowTypeTag = v))
            .addOption(bool("Reach Entity Name", () -> cfg.hud.reachShowEntityName, v -> cfg.hud.reachShowEntityName = v))
            .addOption(intOpt("Reach Decimals", () -> cfg.hud.reachDecimals, v -> cfg.hud.reachDecimals = v, 0, 3, 1));
         case KEYSTROKES -> group
            .addOption(bool("Enabled", () -> cfg.hud.keystrokesHud, v -> cfg.hud.keystrokesHud = v))
            .addOption(bool("Show CPS", () -> cfg.hud.keystrokesShowCps, v -> cfg.hud.keystrokesShowCps = v))
            .addOption(bool("Custom Pressed Color", () -> cfg.hud.keystrokesUsePressedColor, v -> cfg.hud.keystrokesUsePressedColor = v))
            .addOption(color("Pressed Color", () -> cfg.hud.keystrokesPressedColor, v -> cfg.hud.keystrokesPressedColor = v))
            .addOption(color("Pressed Text Color", () -> cfg.hud.keystrokesPressedTextColor, v -> cfg.hud.keystrokesPressedTextColor = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.keystrokesHudScalePercent, v -> cfg.hud.keystrokesHudScalePercent = v, 50, 300, 5));
         case CPS_COUNTER -> group
            .addOption(bool("Enabled", () -> cfg.hud.cpsCounterHud, v -> cfg.hud.cpsCounterHud = v))
            .addOption(bool("Show Both", () -> cfg.hud.cpsShowBoth, v -> cfg.hud.cpsShowBoth = v))
            .addOption(bool("Show Background", () -> cfg.hud.cpsShowBackground, v -> cfg.hud.cpsShowBackground = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.cpsCounterScalePercent, v -> cfg.hud.cpsCounterScalePercent = v, 50, 300, 5));
         case INVENTORY_HUD -> group
            .addOption(bool("Enabled", () -> cfg.hud.inventoryHudEnabled, v -> cfg.hud.inventoryHudEnabled = v))
            .addOption(bool("Show Background", () -> cfg.hud.inventoryHudBackground, v -> cfg.hud.inventoryHudBackground = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.inventoryHudScalePercent, v -> cfg.hud.inventoryHudScalePercent = v, 50, 150, 5));
         case CUSTOM_HITBOXES -> addHitboxOptions(group, cfg);
         case SCOREBOARD -> group.addOption(bool("Hide Scoreboard", () -> cfg.visual.hideScoreboard, v -> cfg.visual.hideScoreboard = v));
         case BETTER_SCREENSHOT -> group
            .addOption(bool("Enabled", () -> cfg.hud.betterScreenshotActions, v -> cfg.hud.betterScreenshotActions = v));
         case CLEAN_F3 -> group
            .addOption(bool("Enabled", () -> cfg.hud.cleanF3Mode, v -> cfg.hud.cleanF3Mode = v))
            .addOption(intOpt("Scale %", () -> cfg.hud.cleanF3ScalePercent, v -> cfg.hud.cleanF3ScalePercent = v, 50, 300, 5))
            .addOption(bool("Show FPS/Ping", () -> cfg.hud.cleanF3ShowFpsPing, v -> cfg.hud.cleanF3ShowFpsPing = v))
            .addOption(bool("Show Position", () -> cfg.hud.cleanF3ShowPosition, v -> cfg.hud.cleanF3ShowPosition = v))
            .addOption(bool("Show Facing", () -> cfg.hud.cleanF3ShowFacing, v -> cfg.hud.cleanF3ShowFacing = v))
            .addOption(bool("Show Biome", () -> cfg.hud.cleanF3ShowBiome, v -> cfg.hud.cleanF3ShowBiome = v));
         case HEALTH_INDICATOR -> group
            .addOption(bool("Enabled", () -> cfg.combat.playerHealthIndicator, v -> cfg.combat.playerHealthIndicator = v))
            .addOption(bool("Show Invisible Players", () -> cfg.combat.playerHealthIndicatorInvisible, v -> cfg.combat.playerHealthIndicatorInvisible = v))
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
            .addOption(color("Background Color", () -> cfg.theme.hudBackgroundColor, v -> cfg.theme.hudBackgroundColor = v))
            .addOption(intOpt("Background Alpha", () -> cfg.theme.hudBackgroundAlpha, v -> cfg.theme.hudBackgroundAlpha = v, 0, 255, 5))
            .addOption(intOpt("Overall Opacity %", () -> cfg.theme.themeAlphaPercent, v -> cfg.theme.themeAlphaPercent = v, 0, 100, 5))
            .addOption(color("Accent Color", () -> cfg.theme.hudAccentColor, v -> cfg.theme.hudAccentColor = v))
            .addOption(color("Text Color", () -> cfg.theme.hudTextColor, v -> cfg.theme.hudTextColor = v))
            .addOption(color("Border Color", () -> cfg.theme.hudBorderColor, v -> cfg.theme.hudBorderColor = v))
            .addOption(bool("Show Borders & Lines", () -> cfg.theme.hudShowBorders, v -> cfg.theme.hudShowBorders = v))
            .addOption(bool("Slot Outlines", () -> cfg.theme.slotOutlines, v -> cfg.theme.slotOutlines = v))
            .addOption(intOpt("Border Thickness", () -> cfg.theme.hudBorderThickness, v -> cfg.theme.hudBorderThickness = v, 0, 4, 1))
            .addOption(intOpt("Corner Radius", () -> cfg.theme.cornerRadius, v -> cfg.theme.cornerRadius = v, 0, 12, 1))
            .addOption(bool("Text Shadows", () -> cfg.theme.enableShadows, v -> cfg.theme.enableShadows = v))
            .addOption(bool("Bold HUD Text", () -> cfg.theme.hudTextBold, v -> cfg.theme.hudTextBold = v));
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
         .addOption(bool("Target Color", () -> cfg.hud.hitboxChangeTargetColor, v -> cfg.hud.hitboxChangeTargetColor = v))
         .addOption(color("Target Hitbox Color", () -> cfg.hud.hitboxTargetColor, v -> cfg.hud.hitboxTargetColor = v))
         .addOption(bool("Hurt Color", () -> cfg.hud.hitboxHurtColorEnabled, v -> cfg.hud.hitboxHurtColorEnabled = v))
         .addOption(color("Hurt Hitbox Color", () -> cfg.hud.hitboxHurtColor, v -> cfg.hud.hitboxHurtColor = v))
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
         .addOption(numOpt("Others Shield Scale", () -> cfg.visual.othersShieldScale, v -> cfg.visual.othersShieldScale = v, 0.5F, 2.0F, 0.1F));
   }

   private static Path getConfigPath() {
      return FabricLoader.getInstance().getConfigDir().resolve("turtmod.json");
   }

   private static Option<Boolean> bool(String name, Supplier<Boolean> getter, Consumer<Boolean> setter) {
      return withDescription(BooleanOption.createBuilder(name, getter, getter.get(), v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }).build(), autoDescription(name));
   }

   private static Option<Integer> intOpt(String name, Supplier<Integer> getter, Consumer<Integer> setter, int min, int max, int step) {
      return withDescription(NumericalOption.createBuilder(name, getter, getter.get(), v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }).values(min, max, step).build(), autoDescription(name));
   }

   private static Option<Float> numOpt(String name, Supplier<Float> getter, Consumer<Float> setter, float min, float max, float step) {
      return withDescription(NumericalOption.createBuilder(name, getter, getter.get(), v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }).values(min, max, step).build(), autoDescription(name));
   }

   private static Option<WalksyLibColor> color(String name, Supplier<Integer> getter, Consumer<Integer> setter) {
      WalksyLibColor[] state = new WalksyLibColor[]{toWalksyLibColor(getter.get())};
      return withDescription(ColorOption.createBuilder(name, () -> {
         int liveValue = getter.get();
         if (state[0].getRGB() != liveValue) {
            state[0] = toWalksyLibColor(liveValue);
         }
         return state[0];
      }, state[0], c -> {
         state[0] = c == null ? toWalksyLibColor(getter.get()) : c;
         if (c != null) {
            setter.accept(c.getRGB());
         }
         ConfigManager.save(TurtModClient.getConfig());
      }).build(), autoDescription(name));
   }

   private static Option<Runnable> button(String name, Runnable action) {
      return withDescription(ButtonOption.createBuilder(name, action).build(), autoDescription(name));
   }

   /** A button that shows the module's current keybind and opens the in-config rebind listener. */
   private static Option<Runnable> keybindButton(ModuleKind kind, class_437 parent) {
      net.minecraft.class_304 binding = TurtModClient.getModuleToggleKey(kind);
      String keyName = binding.method_16007().getString();
      return withDescription(ButtonOption.createBuilder("Keybind: " + keyName, () -> {
         class_310 mc = class_310.method_1551();
         if (mc != null) {
            mc.method_1507(new KeybindListenScreen(binding, () -> createForModule(parent, kind)));
         }
      }).build(), "Click, then press a key or mouse button to bind it. ESC to unbind.");
   }

   private static <E extends Enum<E>> Option<E> enumOpt(String name, Supplier<E> getter, Consumer<E> setter, Class<E> type) {
      return withDescription(EnumOption.createBuilder(name, getter, getter.get(), v -> {
         setter.accept(v);
         ConfigManager.save(TurtModClient.getConfig());
      }, type).build(), autoDescription(name));
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
      cfg.combat.playerHealthIndicatorInvisible = true;
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

   private static WalksyLibColor toWalksyLibColor(int argb) {
      int a = argb >>> 24 & 255;
      int r = argb >>> 16 & 255;
      int g = argb >>> 8 & 255;
      int b = argb & 255;
      return new WalksyLibColor(r, g, b, a);
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
      COORDINATES_HUD
   }
}
