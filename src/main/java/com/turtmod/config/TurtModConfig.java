package com.turtmod.config;

import com.turtmod.visual.HitColorConfig;
import java.util.HashMap;
import java.util.Map;

public final class TurtModConfig {
   public final Visual visual = new Visual();
   public final Combat combat = new Combat();
   public final Hud hud = new Hud();
   public final CustomTheme theme = new CustomTheme();
   public final Misc misc = new Misc();
   public final Map<String, ShieldColorConfig> perPlayerShieldColors = new HashMap();

   public static class ShieldColorConfig {
      public int usableColor = -16711936;
      public int brokenColor = -53200;
      public boolean useCustomColors = false;

      public ShieldColorConfig() {
      }

      public ShieldColorConfig(int usableColor, int brokenColor, boolean useCustomColors) {
         this.usableColor = usableColor;
         this.brokenColor = brokenColor;
         this.useCustomColors = useCustomColors;
      }
   }

   public static final class Visual {
      public final ToggleWithSlider fullbright = new ToggleWithSlider(true, (double)16.0F, (double)1.0F, (double)32.0F);
      public boolean disablePumpkinBlur = true;
      public boolean disablePowderSnowOverlay = true;
      public boolean disableDarknessOverlay = true;
      public boolean disableNauseaDistortion = false;
       public boolean disableLavaFog = true;
       public boolean disableWaterFog = true;
       public boolean disableNetherFog = true;
       public boolean disableAllFog = true;
       public boolean disableBlindnessFog = true;
       public boolean disablePowderSnowFog = false;
       public boolean disableAtmosphericFog = false;
       public int fogDensityPercent = 100;
       public boolean disableDarknessFog = false;
       public int fogDistance = 8;
       public float waterFogStart = -1.0F;
       public float waterFogEnd = -1.0F;
       public float lavaFogStart = -1.0F;
       public float lavaFogEnd = -1.0F;
       public float netherFogStart = -1.0F;
       public float netherFogEnd = -1.0F;
       public float blindnessFogStart = -1.0F;
       public float blindnessFogEnd = -1.0F;
       public float powderSnowFogStart = -1.0F;
       public float powderSnowFogEnd = -1.0F;
       public float darknessFogStart = -1.0F;
       public float darknessFogEnd = -1.0F;
      public int fireYOffset = -40;
      public int shieldYOffset = -1;
      public boolean shieldStatusRecolor = true;
      public boolean shieldUseUsableColor = true;
      public boolean shieldUseBrokenColor = true;
      public int shieldUsableColor = -16711936;
      public int shieldBrokenColor = -53200;
      public int shieldUsingColor = -11141291;
      public boolean shieldUseUsingColor = false;
      public boolean shieldColorInterpolation = true;
      public boolean shieldGrayscaleTexture = false;
      public boolean shieldSelfOnly = false;
      public boolean customShieldSize = false;
      public float selfShieldScale = 1.0F;
      public float selfShieldOffsetX = 0.0F;
      public float selfShieldOffsetY = 0.0F;
      public float othersShieldScale = 1.0F;
      public boolean showEatingInThirdPerson = false;
      public boolean onlyShowShieldWhenBlocking = false;
      public float attackCooldownHandOffset = 0.0F;
      public boolean hideFishingBobber = true;
      public int heldItemScalePercent = 100;
      public int totemPopScalePercent = 36;
      public int totemPopOffsetX = 1;
      public int totemPopOffsetY = -34;
      public int totemPopOffsetZ = 1;
      public boolean disableTotemPopRotation = true;
      public boolean disableTotemPopAnimation = false;
      public int explosionParticleScalePercent = 100;
      public int crystalFlashScalePercent = 100;
      public boolean removeTotemSwirlParticles = false;
      public boolean hurtCamEnabled = true;
      public int hurtCameraShakePercent = 0;
      public boolean oldHurtCameraStyle = false;
      public HurtCamMode hurtCamMode;
      public boolean armorDamageTint = true;
      public boolean armorDamageTintTrim = true;
      public boolean disableHeartBlink = true;
      public boolean disableScreenShake;
       public boolean recolorBlockOutline;
       public int blockOutlineColor;
       public int blockOutlineWidth = 2;
       public int blockOutlineAlpha = 255;
       public boolean blockOutlineInvertDepth = false;
       public boolean blockOutlineRainbow = false;
       public float blockOutlineRainbowSpeed = 5.0F;
      public boolean recolorEnchantGlint;
      public int enchantGlintColor;
      public boolean freelookEnabled;
      public int freelookSensitivityPercent;
      public boolean projectileTrails;
      public int projectileTrailColor;
      public int projectileTrailMaxPoints;
      public int projectileTrailLifetimeTicks;
      public int projectileTrailMinDistancePercent;
      public int projectileTrailAlphaPercent;
      public boolean pearlDetectorEnabled = false;
      public boolean pearlPlaySound = true;
      public float pearlSoundVolume = 1.0F;
      public float pearlSoundPitch = 1.0F;
      public float pearlMinDistance = 3.5F;
      public float pearlMaxDistance = 1000.0F;
      public boolean tntTimer;
      public int tntTimerScalePercent;
      public int tntTimerMaxDistance;
      public int tntTimerDecimals;
      public boolean tntTimerBackground;
      public boolean disableFireOverlay;
      public boolean hideFireOverlay;
      public boolean zoomEnabled;
      public boolean zoomToggleMode;
      public float zoomBaseLevel;
      public float zoomInPerScroll;
      public float zoomOutPerScroll;
      public boolean zoomSmoothInOut;
      public boolean zoomHideArms;
      public boolean zoomNormalizeSensitivity;
      public boolean zoomSmoothCamera;
      public boolean zoomResetOnStop;
      public int zoomLevel;
      public boolean cleanF3HideFPS;
      public boolean cleanF3HidePing;
      public boolean cleanF3HideCoordinates;
      public boolean cleanF3HideChunkUpdates;
      public boolean cleanF3HideBiome;
      public boolean cleanF3CompactMode;
      public boolean hideScoreboard;
      public boolean smoothSneak;
      public boolean customNameTags;
      public final HitColorConfig hitColor;
      public boolean enableSmallTotem;
      public float totemScale;
      public float totemOffsetX;
      public float totemOffsetY;
      public float totemOffsetZ;
      public boolean heldItemTweaksEnabled;
      public boolean customHeldItemSize;
      public float heldItemScaleX;
      public float heldItemScaleY;
      public float heldItemScaleZ;
      public float heldItemPosX;
      public float heldItemPosY;
      public float heldItemPosZ;
      public boolean customOffhandHeldItemSize;
      public float offhandHeldItemScaleX;
      public float offhandHeldItemScaleY;
      public float offhandHeldItemScaleZ;
      public float offhandHeldItemPosX;
      public float offhandHeldItemPosY;
      public float offhandHeldItemPosZ;
      public boolean fishingRodOverlay;
      public int fishingRodOverlayColor;
      public float fishingRodOverlayAlpha;
      public boolean hidePortalOverlay = true;
      public boolean disableSpyglassOverlay = false;
      public boolean disableVignette = false;
      public boolean showOwnNametag = true;
      public boolean elytraPitchHud = false;
      public boolean elytraPitchShowYaw = false;

      public Visual() {
         this.hurtCamMode = TurtModConfig.HurtCamMode.OLD_NON_DIRECTIONAL;
         this.disableScreenShake = false;
          this.recolorBlockOutline = true;
          this.blockOutlineColor = -14803426;
          this.blockOutlineWidth = 10;
          this.blockOutlineAlpha = 175;
          this.blockOutlineInvertDepth = false;
          this.blockOutlineRainbow = false;
          this.blockOutlineRainbowSpeed = 5.0F;
         this.recolorEnchantGlint = false;
         this.enchantGlintColor = -4626433;
         this.freelookEnabled = true;
         this.freelookSensitivityPercent = 85;
         this.pearlDetectorEnabled = false;
         this.pearlPlaySound = true;
         this.pearlSoundVolume = 3.0F;
         this.pearlSoundPitch = 3.0F;
         this.pearlMinDistance = 2.0183487F;
         this.pearlMaxDistance = 1000.0F;
         this.projectileTrails = true;
         this.projectileTrailColor = -11141291;
         this.projectileTrailMaxPoints = 8;
         this.projectileTrailLifetimeTicks = 14;
         this.projectileTrailMinDistancePercent = 8;
         this.projectileTrailAlphaPercent = 100;
         this.tntTimer = false;
         this.tntTimerScalePercent = 100;
         this.tntTimerMaxDistance = 64;
         this.tntTimerDecimals = 2;
         this.tntTimerBackground = false;
         this.disableFireOverlay = true;
         this.zoomEnabled = true;
         this.zoomToggleMode = false;
         this.zoomBaseLevel = 2.733945F;
         this.zoomInPerScroll = 0.09273395F;
         this.zoomOutPerScroll = 0.06427523F;
         this.zoomSmoothInOut = true;
         this.zoomHideArms = true;
         this.zoomNormalizeSensitivity = true;
         this.zoomSmoothCamera = false;
         this.zoomResetOnStop = true;
         this.zoomLevel = 5;
         this.cleanF3HideFPS = false;
         this.cleanF3HidePing = false;
         this.cleanF3HideCoordinates = false;
         this.cleanF3HideChunkUpdates = false;
         this.cleanF3HideBiome = false;
         this.cleanF3CompactMode = false;
         this.hideScoreboard = false;
         this.smoothSneak = true;
         this.customNameTags = true;
         this.hitColor = new HitColorConfig();
         this.enableSmallTotem = true;
         this.totemScale = 0.7F;
         this.totemOffsetX = 0.01F;
         this.totemOffsetY = 0.01F;
         this.totemOffsetZ = 0.01F;
         this.heldItemTweaksEnabled = false;
         this.customHeldItemSize = false;
         this.heldItemScaleX = 1.0045872F;
         this.heldItemScaleY = 1.0F;
         this.heldItemScaleZ = 1.0F;
         this.heldItemPosX = 0.0F;
         this.heldItemPosY = 0.0F;
         this.heldItemPosZ = 0.0F;
         this.customOffhandHeldItemSize = false;
         this.offhandHeldItemScaleX = 1.0311927F;
         this.offhandHeldItemScaleY = 1.0F;
         this.offhandHeldItemScaleZ = 1.0F;
         this.offhandHeldItemPosX = 0.0F;
         this.offhandHeldItemPosY = 0.0F;
         this.offhandHeldItemPosZ = 0.0F;
         this.fishingRodOverlay = true;
         this.fishingRodOverlayColor = -16711936;
         this.fishingRodOverlayAlpha = 0.3F;
      }
   }

public static final class Combat {
       public boolean customAttackCooldownIndicator = true;
       public boolean chargedSound = true;
       public boolean chargedColor = true;
       public int chargedColorArgb = -12326533;
       public int unchargedColorArgb = -5195837;
       public int cooldownOffsetX = -14;
       public int cooldownOffsetY = 14;
       public int cooldownWidth = 28;
       public int cooldownHeight = 2;
       public int cooldownScalePercent = 100;
       public boolean cooldownVertical = false;
       public boolean cooldownOutline = true;
       public boolean cooldownOnlyWeapon = false;
       public boolean totemCounterHud = false;
       public TotemLabelStyle totemLabelStyle;
       public boolean totemColorByCount = true;
       public boolean totemNametagPops = true;
       public boolean totemShowPopCounter = true;
       public boolean totemColoredXpBar = false;
       public boolean totemAlwaysShowXpBar = false;
       public boolean totemShowInTab = false;
       public boolean totemSeparator = false;
       public boolean totemCounterColors = false;
       public boolean potionThrowCounterHud = false;
       public boolean potionThrowNametagPots = true;
       public boolean potionThrowShowInTab = true;
       public boolean potionThrowColoredXpBar = false;
       public boolean potionThrowAlwaysShowXpBar = true;
       public boolean potionThrowSeparator = false;
       public boolean potionThrowCounterColors = false;
       public boolean playerHealthIndicator;
       public boolean playerHealthIndicatorInvisible;
       public PlayerHealthIndicatorStyle playerHealthIndicatorStyle;
       public int playerHealthIndicatorMaxHearts;
       public boolean showExactHealthNumber;
       public int healthOffsetX;
       public int healthOffsetY;
       public int healthScalePercent;
       public float playerHealthIndicatorYOffset;
       public int playerHealthIndicatorSpriteScalePercent;
       public boolean compactHeartsMode;

       public Combat() {
          this.totemLabelStyle = TurtModConfig.TotemLabelStyle.TOTEMS;
          this.playerHealthIndicator = false;
          this.playerHealthIndicatorInvisible = true;
          this.playerHealthIndicatorStyle = TurtModConfig.PlayerHealthIndicatorStyle.SPRITE;
          this.playerHealthIndicatorMaxHearts = 40;
          this.showExactHealthNumber = false;
          this.healthOffsetX = -320;
          this.healthOffsetY = -156;
          this.healthScalePercent = 54;
          this.playerHealthIndicatorYOffset = 0.47339442F;
          this.playerHealthIndicatorSpriteScalePercent = 75;
          this.compactHeartsMode = false;
       }
    }

   public static final class Hud {
      public boolean hudEditMode = false;
      public boolean compactChat = true;
      public boolean hideChat = false;
      public boolean chatTimestamps = true;
      public String chatTimestampFormat = "HH:mm";
      public int compactChatDistance = 1;
      public boolean compactChatCheckStyle = true;
      public boolean betterScreenshotActions = true;
      public boolean inventoryHudEnabled = false;
      public int inventoryHudX = 0;
      public int inventoryHudY = 203;
      public int inventoryHudScalePercent = 65;
      public boolean inventoryHudBackground = true;
      public boolean coordinatesHud = false;
      public int coordinatesHudX = 5;
      public int coordinatesHudY = 200;
      public int coordinatesHudScalePercent = 100;
      public CoordinatesHudMode coordinatesHudMode = CoordinatesHudMode.DEFAULT;
      public boolean coordsShowChunk = true;
      public boolean coordsShowDirection = true;
      public boolean coordsShowBiome = true;
      public boolean coordsShowDimension = true;
      public boolean coordsShowDay = true;
      public boolean coordsShowBackground = true;
      public boolean snapToGrid = false;
      public int gridSize = 16;
      public boolean snapToCenter = true;
      public int centerSnapRange = 8;
      public boolean movableArmorHud = true;
      public int armorHudX = 413;
      public int armorHudY = 271;
      public boolean armorHudVertical = true;
      public boolean armorHudHotbarStyle = true;
      public boolean armorHudShowDurability = true;
      public int armorHudScalePercent = 100;
      public ArmorHudStyle armorHudStyle = ArmorHudStyle.HOTBAR;
      public ArmorHudSide armorHudSide = ArmorHudSide.RIGHT;
      public ArmorHudDurabilityMode armorHudDurabilityMode = ArmorHudDurabilityMode.REMAINING;
      public boolean armorHudShowMainHand = false;
      public boolean armorHudShowOffhand = false;
      public boolean armorHudReserveOffhandSpace = true;
      public boolean armorHudWarnings = true;
      public int armorHudWarningThresholdPercent = 20;
      public boolean movablePotionHud = true;
      public int potionHudX = 613;
      public int potionHudY = 0;
      public int potionMaxRows = 6;
      public int potionHudColumns = 1;
      public int potionRowSpacing = 28;
      public boolean potionShowFlags = true;
      public boolean potionTimerCompact = true;
      public boolean potionTimerShowAmplifier = true;
      public boolean hideVanillaPotionHud = true;
      public PotionSortMode potionSortMode;
      public PotionHudStyle potionHudStyle;
      public int potionHudScalePercent;
      public boolean minimalFpsPingOverlay;
      public int minimalOverlayX;
      public int minimalOverlayY;
      public int overlayScalePercent;
      public boolean cleanF3Mode;
      public boolean cleanF3ShowFpsPing;
      public boolean cleanF3ShowPosition;
      public boolean cleanF3ShowFacing;
      public boolean cleanF3ShowBiome;
      public boolean cleanF3ShowLookingAt;
      public boolean cleanF3ShowLight;
      public boolean cleanF3ShowChunk;
      public boolean cleanF3ShowBackground;
      public boolean cleanF3ShowSpeed;
      public boolean cleanF3ShowDayTime;
      public boolean cleanF3ShowMemory;
      public boolean cleanF3ShowHeldItem;
      public int cleanF3X;
      public int cleanF3Y;
      public int cleanF3ScalePercent;
      public boolean customHitboxes;
      public boolean hitboxPlayers;
      public boolean hitboxHostile;
      public boolean hitboxPassive;
      public boolean hitboxOthers;
      public boolean hitboxSelf;
      public int hitboxMaxDistance;
      public int hitboxColor;
      public boolean hitboxChangeTargetColor;
      public int hitboxTargetColor;
      public boolean hitboxHurtColorEnabled;
      public int hitboxHurtColor;
      public boolean hitboxHideFireworks;
      public boolean cleanDebugHitboxes = false;
      public int totemHudX;
      public int totemHudY;
      public int totemHudScalePercent;
      public boolean reachDisplay;
      public int reachHudX;
      public int reachHudY;
      public int reachHudScalePercent;
      public boolean reachShowTypeTag;
      public boolean reachShowEntityName;
      public boolean reachTrackNearestPlayer;
      public int reachMaxSearchDistance;
      public int reachDecimals;
      public int reachDisplayTicks;
      public boolean toggleSprintHud;
      public int toggleSprintHudX;
      public int toggleSprintHudY;
      public int toggleSprintHudScalePercent;
      public boolean keystrokesHud;
      public boolean keystrokesShowCps;
      public int keystrokesHudX;
      public int keystrokesHudY;
      public int keystrokesHudScalePercent;
      public boolean keystrokesUsePressedColor = true;
      public int keystrokesPressedColor = -7815081;
      public int keystrokesPressedTextColor = -1;
      public boolean cpsCounterHud;
      public int cpsCounterX;
      public int cpsCounterY;
      public int cpsCounterScalePercent;
      public boolean cpsShowBoth;
      public boolean cpsShowRightClick;
      public boolean cpsShowBackground;
      public boolean cpsRainbow;
      public int globalHudScalePercent;
      public int globalHudOpacityPercent;

      public Hud() {
         this.coordinatesHud = false;
         this.coordinatesHudX = 253;
         this.coordinatesHudY = 291;
         this.coordinatesHudScalePercent = 89;
         this.coordinatesHudMode = TurtModConfig.CoordinatesHudMode.COMPACT;
         this.coordsShowChunk = true;
         this.coordsShowDirection = true;
         this.coordsShowBiome = true;
         this.coordsShowDimension = true;
         this.coordsShowDay = true;
         this.coordsShowBackground = true;
         this.potionSortMode = TurtModConfig.PotionSortMode.DURATION_DESC;
         this.potionHudStyle = TurtModConfig.PotionHudStyle.ICONS_ONLY;
         this.potionHudScalePercent = 105;
         this.minimalFpsPingOverlay = true;
         this.minimalOverlayX = 0;
         this.minimalOverlayY = 0;
         this.overlayScalePercent = 100;
         this.cleanF3Mode = false;
         this.cleanF3ShowFpsPing = true;
         this.cleanF3ShowPosition = true;
         this.cleanF3ShowFacing = true;
         this.cleanF3ShowBiome = true;
         this.cleanF3ShowLookingAt = true;
         this.cleanF3ShowLight = true;
         this.cleanF3ShowChunk = true;
         this.cleanF3ShowBackground = true;
         this.cleanF3ShowSpeed = false;
         this.cleanF3ShowDayTime = false;
         this.cleanF3ShowMemory = false;
         this.cleanF3ShowHeldItem = false;
         this.cleanF3X = 0;
         this.cleanF3Y = 25;
         this.cleanF3ScalePercent = 90;
         this.customHitboxes = true;
         this.hitboxPlayers = true;
         this.hitboxHostile = true;
         this.hitboxPassive = true;
         this.hitboxOthers = true;
         this.hitboxSelf = true;
         this.hitboxMaxDistance = 158;
         this.hitboxColor = -1;
         this.hitboxChangeTargetColor = true;
         this.hitboxTargetColor = -43691;
         this.hitboxHurtColorEnabled = true;
         this.hitboxHurtColor = -10158080;
         this.hitboxHideFireworks = true;
         this.cleanDebugHitboxes = true;
         this.totemHudX = 505;
         this.totemHudY = 162;
         this.totemHudScalePercent = 50;
         this.reachDisplay = false;
         this.reachHudX = 0;
         this.reachHudY = 180;
         this.reachHudScalePercent = 100;
         this.reachShowTypeTag = true;
         this.reachShowEntityName = true;
         this.reachTrackNearestPlayer = false;
         this.reachMaxSearchDistance = 50;
         this.reachDecimals = 2;
         this.reachDisplayTicks = 60;
         this.toggleSprintHud = false;
         this.toggleSprintHudX = 170;
         this.toggleSprintHudY = 342;
         this.toggleSprintHudScalePercent = 100;
         this.keystrokesHud = false;
         this.keystrokesShowCps = false;
         this.keystrokesHudX = 594;
         this.keystrokesHudY = 41;
         this.keystrokesHudScalePercent = 85;
         this.cpsCounterHud = false;
         this.cpsCounterX = 602;
         this.cpsCounterY = 210;
         this.cpsCounterScalePercent = 105;
         this.cpsShowBoth = true;
         this.cpsShowRightClick = false;
         this.cpsShowBackground = true;
         this.cpsRainbow = false;
         this.globalHudScalePercent = 100;
         this.globalHudOpacityPercent = 100;
      }
   }

   public static final class CustomTheme {
      public int hudBackgroundColor = -14540254;
      public int hudBackgroundAlpha = 49;
      public int hudBorderColor = -9790395;
      public int hudBorderThickness = 1;
      public boolean hudShowBorders = true;
      public boolean slotOutlines = true;
      public int hudTextColor = -1;
      public int hudAccentColor = -11296965;
      public boolean enableShadows = true;
      public boolean hudTextBold = false;
      public int shadowSize = 4;
      public int cornerRadius = 7;
      public int themeAlphaPercent = 100;
      public int keyBackgroundColor = -14799074;
      public int keyActiveColor = -9724347;
      public int keyTextColor = -6704999;
      public int keyActiveTextColor = -1;
   }

   public static final class DiscordRpc {
      public boolean enabled = true;
      public boolean showUsername = false;
      public boolean showServerName = false;
      public boolean showDimension = false;
   }

   public static enum HurtCamMode {
      VANILLA,
      OLD_NON_DIRECTIONAL,
      OFF;

      // $FF: synthetic method
      private static HurtCamMode[] $values() {
         return new HurtCamMode[]{VANILLA, OLD_NON_DIRECTIONAL, OFF};
      }
   }

   public static enum PotionSortMode {
      DURATION_DESC,
      DURATION_ASC,
      AMPLIFIER_DESC,
      NAME_ASC;

      // $FF: synthetic method
      private static PotionSortMode[] $values() {
         return new PotionSortMode[]{DURATION_DESC, DURATION_ASC, AMPLIFIER_DESC, NAME_ASC};
      }
   }

   public static enum PotionHudStyle {
      FULL,
      ICONS_ONLY,
      COMPACT;

      // $FF: synthetic method
      private static PotionHudStyle[] $values() {
         return new PotionHudStyle[]{FULL, ICONS_ONLY, COMPACT};
      }
   }

   public static enum TotemLabelStyle {
      TOTEMS,
      TOTEM,
      ICON;

      // $FF: synthetic method
      private static TotemLabelStyle[] $values() {
         return new TotemLabelStyle[]{TOTEMS, TOTEM, ICON};
      }
   }

   public static enum PlayerHealthIndicatorStyle {
      HOTBAR,
      COMPACT,
      NUMBER,
      SPRITE;

      // $FF: synthetic method
      private static PlayerHealthIndicatorStyle[] $values() {
         return new PlayerHealthIndicatorStyle[]{HOTBAR, COMPACT, NUMBER, SPRITE};
      }
   }

   public static enum ArmorHudStyle {
      CLASSIC,
      HOTBAR,
      MINIMAL;
   }

   public static enum ArmorHudSide {
      LEFT,
      RIGHT;
   }

   public static enum ArmorHudDurabilityMode {
      OFF,
      REMAINING,
      PERCENT,
      DAMAGE;
   }

   public static enum CoordinatesHudMode {
      DEFAULT,
      COMPACT,
      LINE;
   }

   public static final class Misc {
      public boolean enabled = true;
      public boolean containerButtons = true;
      public final DiscordRpc discordRpc = new DiscordRpc();
   }

   public static final class ToggleWithSlider {
      public boolean enabled;
      public double value;
      public double min;
      public double max;

      public ToggleWithSlider(boolean enabled, double value, double min, double max) {
         this.enabled = enabled;
         this.value = value;
         this.min = min;
         this.max = max;
      }
   }
}
