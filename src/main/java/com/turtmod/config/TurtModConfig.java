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

   /** Per-colour-field gradient definitions, keyed by a stable colour id (see GradientKeys). A field with a
    *  gradient here (>=2 stops) renders as a multi-stop gradient/chroma instead of its flat colour. */
   public final Map<String, GradientDef> gradients = new HashMap<>();

   /** User-saved swatches (ARGB) shown in the colour picker's "Saved" row — persist across sessions. */
   public final java.util.List<Integer> savedPickerColors = new java.util.ArrayList<>();

   /** A multi-stop gradient: ARGB stops evenly spaced, optionally animated (flowing chroma). */
   public static class GradientDef {
      public int[] stops = new int[0];
      public boolean animate = false;
      public float speed = 1.0f;

      public GradientDef() {
      }

      public GradientDef(int[] stops, boolean animate, float speed) {
         this.stops = stops;
         this.animate = animate;
         this.speed = speed;
      }

      public boolean isGradient() {
         return this.stops != null && this.stops.length >= 2;
      }

      /**
       * SPATIAL sampler: colour at position t across the (evenly-spaced) stops, clean linear blend.
       * t is CLAMPED to [0,1] so t=0 is the first stop and t=1 is the LAST stop (no wrap-around) —
       * this is what the bg fill / per-glyph text / hit-colour rows want, so the end never snaps back
       * to the first colour.
       */
      public int colorAt(float t) {
         if (this.stops == null || this.stops.length == 0) {
            return 0xFFFFFFFF;
         }
         if (this.stops.length == 1) {
            return this.stops[0];
         }
         t = Math.max(0f, Math.min(1f, t));          // clamp into [0,1]
         float scaled = t * (this.stops.length - 1);
         int i = (int) Math.floor(scaled);
         if (i >= this.stops.length - 1) {
            return this.stops[this.stops.length - 1];
         }
         float f = scaled - i;
         return lerpArgb(this.stops[i], this.stops[i + 1], f);
      }

      /**
       * CYCLIC sampler for animation: a seamless loop that treats the stops as
       * {@code [s0, s1, … sN-1, s0]} (N segments, wraps) so chroma flows smoothly back to the start.
       * Always cycles over time regardless of the {@code animate} flag.
       */
      public int flowColor(long timeMs) {
         if (this.stops == null || this.stops.length == 0) {
            return 0xFFFFFFFF;
         }
         if (this.stops.length == 1) {
            return this.stops[0];
         }
         float period = Math.max(500f, 6000f / Math.max(0.1f, this.speed));
         float t = (timeMs % (long) period) / period;   // [0,1)
         int n = this.stops.length;                      // N segments back to s0
         float scaled = t * n;
         int i = (int) Math.floor(scaled) % n;
         float f = scaled - (float) Math.floor(scaled);
         int next = (i + 1) % n;
         return lerpArgb(this.stops[i], this.stops[next], f);
      }

      /** Current animated colour (flows through the stops over time when animate is on; else stops[0]). */
      public int animatedColor(long timeMs) {
         if (!this.animate) {
            return this.stops.length > 0 ? this.stops[0] : 0xFFFFFFFF;
         }
         return flowColor(timeMs);
      }

      private static int lerpArgb(int a, int b, float t) {
         t = Math.max(0f, Math.min(1f, t));
         int aa = a >>> 24 & 255, ar = a >> 16 & 255, ag = a >> 8 & 255, ab = a & 255;
         int ba = b >>> 24 & 255, br = b >> 16 & 255, bg = b >> 8 & 255, bb = b & 255;
         int oa = Math.round(aa + (ba - aa) * t);
         int or = Math.round(ar + (br - ar) * t);
         int og = Math.round(ag + (bg - ag) * t);
         int ob = Math.round(ab + (bb - ab) * t);
         return (oa << 24) | (or << 16) | (og << 8) | ob;
      }
   }

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
      public boolean shieldFixBlockingAnim = true;
      public boolean shieldFixSounds = true;
      public boolean shieldFix5TickDelay = false;
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
      public boolean scoreboardHideNumbers = false;
      public boolean scoreboardHideBackground = false;
      public int scoreboardScalePercent = 100;
      public int scoreboardOffsetX = 0;
      public int scoreboardOffsetY = 0;
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
      public float heldItemRotX;
      public float heldItemRotY;
      public float heldItemRotZ;
      public boolean customOffhandHeldItemSize;
      public float offhandHeldItemScaleX;
      public float offhandHeldItemScaleY;
      public float offhandHeldItemScaleZ;
      public float offhandHeldItemPosX;
      public float offhandHeldItemPosY;
      public float offhandHeldItemPosZ;
      public float offhandHeldItemRotX;
      public float offhandHeldItemRotY;
      public float offhandHeldItemRotZ;
      public boolean fishingRodOverlay;
      public int fishingRodOverlayColor;
      public float fishingRodOverlayAlpha;
      public boolean hidePortalOverlay = true;
      public boolean disableSpyglassOverlay = false;
      public boolean disableVignette = false;
      public boolean showOwnNametag = true;
      public boolean elytraPitchHud = false;
      public boolean elytraPitchShowYaw = false;
      public boolean pingOnNametag = false;
      public boolean pingNametagAutoColor = true;
      public String pingNametagFormat = "%dms";
      public PingTextPosition pingNametagPosition = TurtModConfig.PingTextPosition.RIGHT;

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
         this.scoreboardHideNumbers = false;
         this.scoreboardHideBackground = false;
         this.scoreboardScalePercent = 100;
         this.scoreboardOffsetX = 0;
         this.scoreboardOffsetY = 0;
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
         this.heldItemRotX = 0.0F;
         this.heldItemRotY = 0.0F;
         this.heldItemRotZ = 0.0F;
         this.offhandHeldItemRotX = 0.0F;
         this.offhandHeldItemRotY = 0.0F;
         this.offhandHeldItemRotZ = 0.0F;
         this.fishingRodOverlay = false;
         this.fishingRodOverlayColor = -16711936;
         this.fishingRodOverlayAlpha = 1.0F;
      }
   }

public static final class Combat {
       public boolean totemCounterHud = false;
       public TotemLabelStyle totemLabelStyle;
       public boolean totemColorByCount = true;
       public boolean totemNametagPops = true;
       public boolean totemShowPopCounter = false;
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
       public boolean potionThrowShowPotCounter = false;
       public boolean potionThrowColorByCount = true;
       public boolean playerHealthIndicator;
       public boolean playerHealthIndicatorArmorOnly = false;
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
      // Animated corner preview shown after taking a screenshot (F2), with inline view/copy/upload/delete.
      public boolean screenshotPreview = true;
      public ScreenshotCorner screenshotPreviewCorner = ScreenshotCorner.BOTTOM_RIGHT;
      public int screenshotPreviewSeconds = 4;
      public int screenshotPreviewScalePercent = 100;   // size of the corner preview card
      public boolean screenshotShutterSound = true;
      public boolean screenshotFlash = true;
      public boolean screenshotMenuButton = true;
      public boolean skinChangerMenuButton = true;
      // Animated toast shown when a module toggles on/off in-game.
      public boolean moduleToasts = true;
      public ScreenshotCorner moduleToastCorner = ScreenshotCorner.TOP_RIGHT;
      public int moduleToastSeconds = 2;
      public int moduleToastMaxVisible = 4;
      // Nudge the toast stack away from its corner, for lining it up with other HUDs.
      public int moduleToastOffsetX = 0;
      public int moduleToastOffsetY = 0;
      public boolean inventoryHudEnabled = false;
      public int inventoryHudX = 445;
      public int inventoryHudY = 291;
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
      // Per-HUD frame toggles, so individual HUDs can go frameless without dropping the global alpha.
      public boolean fpsPingShowBackground = true;
      public boolean reachShowBackground = true;
      public boolean sprintShowBackground = true;
      public boolean pingInTab = false;
      public boolean pingTabAutoColor = true;
      public String pingTabFormat = "%dms";
      public int pingTabColor = -1;
      public boolean snapToGrid = false;
      public int gridSize = 16;
      public boolean snapToCenter = true;
      public int centerSnapRange = 8;
      public boolean movableArmorHud = true;
      public int armorHudX = 67;
      public int armorHudY = 316;
      public boolean armorHudVertical = true;
      public boolean armorHudHotbarStyle = true;
      public boolean armorHudShowDurability = true;
      // When on, armour pieces at FULL durability show their number in the theme's Text colour instead of the
      // vanilla green bar-colour; damaged pieces still fade green→red so wear is still visible at a glance.
      public boolean armorHudFullDurabilityTextColor = true;
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
      public int potionHudX = 640;
      public int potionHudY = 0;
      public int potionMaxRows = 6;
      public int potionHudColumns = 1;
      public boolean potionHudHorizontal = false;   // false = vertical (stacked), true = single row
      public int potionRowSpacing = 28;
      public boolean potionShowFlags = true;
      public boolean potionTimerCompact = true;
      public boolean potionTimerClock = false;   // true = "1:30" (m:ss) instead of "1m"/"30"
      public boolean potionTimerShowAmplifier = true;
      public boolean hideVanillaPotionHud = true;
      public PotionSortMode potionSortMode;
      public PotionHudStyle potionHudStyle;
      public int potionHudScalePercent;
      public boolean minimalFpsPingOverlay;
      public boolean fpsColorCoded;
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
      public boolean cleanF3ShowBlock = true;   // block coords line (split from XYZ so each is independent)
      public boolean cleanF3ShowChunk;
      public boolean cleanF3ShowBackground;
      public boolean cleanF3ShowSpeed;
      public boolean cleanF3ShowDayTime;
      public boolean cleanF3ShowMemory;
      public boolean cleanF3ShowHeldItem;
      public boolean cleanF3ShowDimension;
      public boolean cleanF3ShowFpsExtremes;
      // Vanilla-style right-hand column (Java / memory / CPU / display / what you are looking at).
      public boolean cleanF3RightColumn = true;
      // Use the HUD theme's accent/text colours for F3 instead of the custom cleanF3 colours.
      public boolean cleanF3ThemeColors = true;
      // BetterF3-style two-tone colouring + the line order (list of line keys, see DebugHudMixin).
      public int cleanF3LabelColor = 0xFF7FE08A;
      public int cleanF3ValueColor = 0xFFE6E6E6;
      public java.util.List<String> cleanF3Order;
      public int cleanF3X;
      public int cleanF3Y;
      public int cleanF3ScalePercent;

      // Chat Tweaks: keep more chat history than vanilla's 100-message cap.
      public boolean chatTweaksEnabled = true;
      public int chatHistoryLength = 250;

      /** Fills in Clean F3 fields that GSON left unset (older configs) so colours/order are valid. */
      public void ensureCleanF3() {
         if (this.cleanF3Order == null || this.cleanF3Order.isEmpty()) {
            this.cleanF3Order = defaultCleanF3Order();
         }
         if ((this.cleanF3LabelColor & 0xFF000000) == 0) {
            this.cleanF3LabelColor = 0xFF7FE08A;
         }
         if ((this.cleanF3ValueColor & 0xFF000000) == 0) {
            this.cleanF3ValueColor = 0xFFE6E6E6;
         }
      }

      /** Canonical default order of Clean F3 line keys. */
      public static java.util.List<String> defaultCleanF3Order() {
         return new java.util.ArrayList<>(java.util.List.of(
            "fps", "pos", "block", "chunk", "light", "facing", "speed",
            "biome", "dim", "daytime", "held", "mem", "look"));
      }
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
      // Reveal an invisible player's hitbox ONLY while they wear armour (armored-invisible reveal).
      public boolean hitboxShowInvisible = false;
      public boolean cleanDebugHitboxes = false;
      // User-picked per-entity hitbox colours: entity-type id (e.g. "minecraft:zombie") -> ARGB colour.
      // Any entity in this map draws its hitbox in the chosen colour instead of the default one.
      public final java.util.Map<String, Integer> hitboxEntityColors = new java.util.LinkedHashMap<>();
      public int totemHudX;
      public int totemHudY;
      public int totemHudScalePercent;
      public int potionThrowHudX;
      public int potionThrowHudY;
      public int potionThrowHudScalePercent;
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
      public SprintDisplayStyle sprintDisplayStyle = SprintDisplayStyle.VERBOSE;
      public boolean sprintShowSneaking = true;
      public boolean sprintShowSwimming = true;
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
         this.coordinatesHudX = 267;
         this.coordinatesHudY = 302;
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
         this.fpsColorCoded = false;
         this.minimalOverlayX = 0;
         this.minimalOverlayY = 352;
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
         this.cleanF3ShowDimension = false;
         this.cleanF3ShowFpsExtremes = false;
         this.cleanF3LabelColor = 0xFF7FE08A;
         this.cleanF3ValueColor = 0xFFE6E6E6;
         this.cleanF3Order = defaultCleanF3Order();
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
         this.potionThrowHudX = 505;
         this.potionThrowHudY = 190;
         this.potionThrowHudScalePercent = 50;
         this.reachDisplay = false;
         this.reachHudX = 0;
         this.reachHudY = 182;
         this.reachHudScalePercent = 100;
         this.reachShowTypeTag = true;
         this.reachShowEntityName = true;
         this.reachTrackNearestPlayer = false;
         this.reachMaxSearchDistance = 50;
         this.reachDecimals = 2;
         this.reachDisplayTicks = 60;
         this.toggleSprintHud = false;
         this.toggleSprintHudX = 0;
         this.toggleSprintHudY = 0;
         this.toggleSprintHudScalePercent = 100;
         this.keystrokesHud = false;
         this.keystrokesShowCps = false;
         this.keystrokesHudX = 582;
         this.keystrokesHudY = 171;
         this.keystrokesHudScalePercent = 85;
         this.cpsCounterHud = false;
         this.cpsCounterX = 597;
         this.cpsCounterY = 249;
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
      // Clean, Lunar-style look: the user-facing theme is just background (colour + opacity), text colour,
      // accent colour, and a text shadow. Everything below marked "removed" is no longer user-facing and is
      // hard-forced to a clean value by forceCleanTheme() so old configs can't keep the HUD cluttered.
      public int hudBackgroundColor = 0xFF14181F;
      public int hudBackgroundAlpha = 170;
      public int hudTextColor = -1;
      public int hudAccentColor = -11296965;
      public boolean enableShadows = true;
      // ── removed from the UI (kept for code refs, forced clean) ──
      public int hudBorderColor = 0xFFFFFFFF;
      public int hudBorderThickness = 0;
      public boolean hudShowBorders = false;
      public boolean slotOutlines = false;
      public boolean hudTextBold = false;
      public int shadowSize = 4;
      public int cornerRadius = 3;
      public boolean hudGlass = false;
      public boolean hudAccentBar = false;
      public int themeAlphaPercent = 100;
      public int keyBackgroundColor = -14799074;
      public int keyActiveColor = -9724347;
      public int keyTextColor = -6704999;
      public int keyActiveTextColor = -1;

      /** Force the removed decorative options to their clean values so pre-revamp configs load clean. */
      public void forceCleanTheme() {
         this.hudShowBorders = false;
         this.slotOutlines = false;
         this.hudGlass = false;
         this.hudAccentBar = false;
         this.hudTextBold = false;
         this.hudBorderThickness = 0;
         this.themeAlphaPercent = 100;
         this.cornerRadius = 3;
      }
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

   /** One line of a command-key macro: text to send (or "/cmd") plus a delay (in ticks) before it. */
   public static class CmdMsg {
      public String text = "";
      public int delay = 0;

      public CmdMsg() {
      }

      public CmdMsg(String text, int delay) {
         this.text = text;
         this.delay = delay;
      }
   }

   /** A bindable command-key macro: an ordered list of messages, a fire mode, and a send/type flag. */
   public static class CommandKey {
      public java.util.List<CmdMsg> messages = new java.util.ArrayList<>();
      // SEND = fire all messages once (honouring delays); CYCLE = each press fires the next message;
      // REPEAT = keep firing the whole sequence on a loop until the key is pressed again.
      public String mode = "SEND";
      // When true, the (first) message is typed into the chat box instead of being sent immediately.
      public boolean typeInChat = false;
   }

   public static enum PotionSortMode {
      DURATION_DESC,
      DURATION_ASC,
      AMPLIFIER_DESC,
      AMPLIFIER_ASC,
      NAME_ASC,
      NAME_DESC;

      // $FF: synthetic method
      private static PotionSortMode[] $values() {
         return new PotionSortMode[]{DURATION_DESC, DURATION_ASC, AMPLIFIER_DESC, AMPLIFIER_ASC, NAME_ASC, NAME_DESC};
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

   // Durability-text placement. LEFT/RIGHT apply to the VERTICAL layout (text beside the column);
   // TOP/BOTTOM apply to the HORIZONTAL layout (text above/below the row). Each layout falls back
   // to its natural default if given a position that doesn't fit it.
   public static enum ArmorHudSide {
      LEFT,
      RIGHT,
      TOP,
      BOTTOM;
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

   /** Which screen corner the post-screenshot preview animates into. */
   public static enum ScreenshotCorner {
      BOTTOM_RIGHT,
      BOTTOM_LEFT,
      TOP_RIGHT,
      TOP_LEFT;
   }

   // Sprint-display text style (ported from toggle-sprint-display). VERBOSE = "Sprint Held/Toggled",
   // SHORT = "Sprinting", ICON = emoji glyphs merged onto one line.
   public static enum SprintDisplayStyle {
      VERBOSE,
      SHORT,
      ICON;
   }

   public static enum PingTextPosition {
      LEFT,
      RIGHT;
   }

   public static final class Misc {
      public boolean enabled = true;
      public boolean containerButtons = true;
      // Lets the F3+F4 gamemode switcher work without the local op-permission gate (sends a
      // /gamemode command). Server still decides — works in singleplayer / where you have perms.
      public boolean noOpGamemodeSwitcher = false;
      public final DiscordRpc discordRpc = new DiscordRpc();
      // Utility features (ported from fireclient / command-key mods).
      public boolean deathCoords = true;
      // Module master switches: when false the whole module does nothing (incl. the per-id lists).
      public boolean hideParticlesEnabled = true;
      public boolean muteSoundsEnabled = true;
      public boolean hideParticles = false;
      public boolean muteAnvil = false;
      public boolean muteNoteBlocks = false;
      public boolean muteTotemPop = false;
      public boolean muteXpOrb = false;
      // Clear View: a cleaner first-person look. Hides YOUR OWN potion swirls (first person only) and
      // trims/hides the eating "crumbs". Others / third-person are unaffected.
      public boolean clearViewEnabled = true;
      public boolean clearViewHidePotionParticles = true;
      public boolean clearViewReduceEatingParticles = true;
      public boolean clearViewHideEatingParticles = false;
      public boolean commandKeysEnabled = true;
      // Legacy: one command/message per slot (migrated into commandKeyMacros on load; kept for
      // backwards compatibility so old configs don't get wiped). "/" prefix = command, else chat.
      public String[] commandKeys = new String[]{"", "", "", "", ""};
      // Rich macros: each bindable slot can fire a sequence of messages with per-message delays,
      // in one of several modes. Populated by ensureCommandKeys() (migrates legacy commandKeys).
      public CommandKey[] commandKeyMacros;

      /** Ensures commandKeyMacros has one entry per slot and migrates any legacy single-string slot. */
      public void ensureCommandKeys() {
         int slots = com.turtmod.hud.CommandKeysFeature.SLOTS;
         if (this.commandKeyMacros == null) {
            this.commandKeyMacros = new CommandKey[slots];
         } else if (this.commandKeyMacros.length < slots) {
            CommandKey[] grown = new CommandKey[slots];
            System.arraycopy(this.commandKeyMacros, 0, grown, 0, this.commandKeyMacros.length);
            this.commandKeyMacros = grown;
         }
         for (int i = 0; i < slots; i++) {
            if (this.commandKeyMacros[i] == null) {
               this.commandKeyMacros[i] = new CommandKey();
            }
            CommandKey key = this.commandKeyMacros[i];
            if (key.messages == null) {
               key.messages = new java.util.ArrayList<>();
            }
            if (key.mode == null) {
               key.mode = "SEND";
            }
            // Migrate a legacy single-string slot into the first message.
            if (key.messages.isEmpty() && this.commandKeys != null && i < this.commandKeys.length
                  && this.commandKeys[i] != null && !this.commandKeys[i].trim().isEmpty()) {
               CmdMsg m = new CmdMsg();
               m.text = this.commandKeys[i].trim();
               key.messages.add(m);
            }
         }
      }
      // Per-id granular lists (managed via /turtmod particle|sound commands). hideParticles above is
      // the "hide everything" master switch; these hide/mute only the listed registry ids.
      public java.util.List<String> hiddenParticleIds = new java.util.ArrayList<>();
      public java.util.List<String> mutedSoundIds = new java.util.ArrayList<>();
      // Module display names the user pinned; they float to a "★ Pinned" group at the top of the grid.
      public java.util.List<String> pinnedModules = new java.util.ArrayList<>();

      /** Null-guards the pinned list for configs written before this field existed. */
      public void ensurePinned() {
         if (this.pinnedModules == null) {
            this.pinnedModules = new java.util.ArrayList<>();
         }
      }
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
