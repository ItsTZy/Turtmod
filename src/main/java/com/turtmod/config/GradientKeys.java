package com.turtmod.config;

/**
 * Stable ids for colour fields that support gradients (keys into {@link TurtModConfig#gradients}). Keep these
 * constant across versions so saved gradients keep matching their colour after updates.
 */
public final class GradientKeys {
   private GradientKeys() {
   }

   // Theme (drive the whole HUD).
   public static final String HUD_TEXT = "theme.hudTextColor";
   public static final String HUD_ACCENT = "theme.hudAccentColor";
   public static final String HUD_BG = "theme.hudBackgroundColor";

   // Combat / visual entity colours.
   public static final String HIT_COLOR = "visual.hitColor";
   public static final String HITBOX = "hud.hitboxColor";
   public static final String HITBOX_TARGET = "hud.hitboxTargetColor";
   public static final String HITBOX_HURT = "hud.hitboxHurtColor";
   public static final String BLOCK_OUTLINE = "visual.blockOutlineColor";

   // HUD element colours.
   public static final String KEYSTROKES_PRESSED = "hud.keystrokesPressedColor";
   public static final String KEYSTROKES_PRESSED_TEXT = "hud.keystrokesPressedTextColor";
   public static final String CLEAN_F3_LABEL = "hud.cleanF3LabelColor";
   public static final String CLEAN_F3_VALUE = "hud.cleanF3ValueColor";
   public static final String FISHING_LINE = "visual.fishingRodOverlayColor";
   public static final String PING_TAB = "hud.pingTabColor";
}
