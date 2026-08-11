package com.turtmod.hud;

import com.turtmod.config.GradientKeys;
import com.turtmod.config.TurtModConfig;
import java.util.Map;

/**
 * Drives "chroma" gradients for NON-text colour fields: each frame it samples the current (animated) colour
 * from a field's gradient and writes it back into that field's plain {@code int}, so every render site that
 * already reads the int (hitboxes, block outline, keystrokes, …) shows the flowing gradient with no per-site
 * changes. The gradient stops are the saved source of truth; the int is a derived, non-persisted cache.
 *
 * <p>Text/background theme colours are NOT handled here — they render as spatial per-glyph / vertical
 * gradients directly in {@link CustomThemeRenderer}.
 */
public final class GradientRuntime {
   private GradientRuntime() {
   }

   public static void tick(TurtModConfig cfg) {
      if (cfg == null || cfg.gradients == null || cfg.gradients.isEmpty()) {
         return;
      }
      long now = System.currentTimeMillis();
      for (Map.Entry<String, TurtModConfig.GradientDef> e : cfg.gradients.entrySet()) {
         TurtModConfig.GradientDef g = e.getValue();
         if (g == null || !g.isGradient()) {
            continue;
         }
         // These fields are a single int (wireframe / outline / text colour) — a static "gradient" would just
         // show one stop, which reads as "it doesn't work". So they ALWAYS flow (chroma-cycle) whenever a
         // gradient is set, whether or not the Animate box is ticked.
         apply(cfg, e.getKey(), g.flowColor(now));
      }
   }

   private static void apply(TurtModConfig cfg, String key, int col) {
      switch (key) {
         case GradientKeys.HITBOX -> cfg.hud.hitboxColor = col;
         case GradientKeys.HITBOX_TARGET -> cfg.hud.hitboxTargetColor = col;
         case GradientKeys.HITBOX_HURT -> cfg.hud.hitboxHurtColor = col;
         case GradientKeys.BLOCK_OUTLINE -> cfg.visual.blockOutlineColor = col;
         case GradientKeys.KEYSTROKES_PRESSED -> cfg.hud.keystrokesPressedColor = col;
         case GradientKeys.KEYSTROKES_PRESSED_TEXT -> cfg.hud.keystrokesPressedTextColor = col;
         case GradientKeys.CLEAN_F3_LABEL -> cfg.hud.cleanF3LabelColor = col;
         case GradientKeys.CLEAN_F3_VALUE -> cfg.hud.cleanF3ValueColor = col;
         case GradientKeys.FISHING_LINE -> cfg.visual.fishingRodOverlayColor = col;
         case GradientKeys.PING_TAB -> cfg.hud.pingTabColor = col;
         // HUD_TEXT / HUD_ACCENT / HUD_BG / HIT_COLOR are rendered spatially elsewhere, not via the int.
         default -> {
         }
      }
   }
}
