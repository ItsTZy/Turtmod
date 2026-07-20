package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

/**
 * Toast shown when a module toggles on/off in-game. Deliberately drawn as a copy of the mod menu's
 * module card (rounded card, 2px accent bar, accent label, sliding pill toggle with knob — see
 * {@code TurtUICheckbox}) so the in-game notification reads as the same UI. Slides in from the
 * configured corner, holds, then fades out.
 *
 * <p>The hold timer starts on the toast's first rendered frame, not when {@code notify} is called —
 * HUD rendering is suspended while a screen is open, so a module toggled inside the config menu still
 * shows its toast once the menu closes. Tick + render are both on the client thread, so no locking.
 */
public final class ModuleToastFeature {
   private static final long ENTER_MS = 220, EXIT_MS = 300;
   private static final int MAX_TOASTS = 8;   // hard cap; the visible count is configurable
   private static final List<Toast> TOASTS = new ArrayList<>();

   private ModuleToastFeature() {
   }

   private static final class Toast {
      final String label;
      boolean on;
      long shownAt;
      boolean started;

      Toast(String label, boolean on) {
         this.label = label;
         this.on = on;
      }
   }

   /** Enqueue a toast for a module that just toggled. Re-toggling the same module refreshes its card. */
   public static void notify(String label, boolean on) {
      if (label == null || label.isEmpty()) {
         return;
      }
      for (Toast t : TOASTS) {
         if (t.label.equals(label)) {
            t.on = on;
            t.started = false;   // replay the appear animation
            return;
         }
      }
      TOASTS.add(new Toast(label, on));
      while (TOASTS.size() > MAX_TOASTS) {
         TOASTS.remove(0);
      }
   }

   private static int argb(Color c, int a) {
      return (Math.max(0, Math.min(255, a)) << 24) | (c.getRGB() & 0xFFFFFF);
   }

   /** Same colour, its alpha scaled by the toast's current fade. */
   private static Color fade(Color c, int a) {
      return Palette.alpha(c, c.getAlpha() * Math.max(0, Math.min(255, a)) / 255);
   }

   private static float easeOut(float t) {
      return 1f - (float) Math.pow(1f - t, 3);
   }

   private static float easeIn(float t) {
      return t * t * t;
   }

   public static void render(class_332 ctx, class_310 client, TurtModConfig cfg) {
      if (cfg == null || !cfg.misc.enabled || !cfg.hud.moduleToasts || TOASTS.isEmpty() || client.field_1772 == null) {
         return;
      }
      class_327 tr = client.field_1772;
      long now = System.currentTimeMillis();
      long hold = Math.max(1, cfg.hud.moduleToastSeconds) * 1000L;
      long total = ENTER_MS + hold + EXIT_MS;

      TurtModConfig.ScreenshotCorner corner = cfg.hud.moduleToastCorner;
      boolean right = corner == TurtModConfig.ScreenshotCorner.TOP_RIGHT || corner == TurtModConfig.ScreenshotCorner.BOTTOM_RIGHT;
      boolean top = corner == TurtModConfig.ScreenshotCorner.TOP_RIGHT || corner == TurtModConfig.ScreenshotCorner.TOP_LEFT;

      int sw = ctx.method_51421();
      int sh = ctx.method_51443();
      final int margin = 8, gap = 4, h = 22;
      final int pillH = 12, pillW = pillH * 2;

      int maxVisible = Math.max(1, Math.min(MAX_TOASTS, cfg.hud.moduleToastMaxVisible));
      int slot = 0;
      for (int idx = TOASTS.size() - 1; idx >= 0; idx--) {
         Toast t = TOASTS.get(idx);
         if (!t.started) {   // start the clock the first frame it's actually drawn
            t.started = true;
            t.shownAt = now;
         }
         long life = now - t.shownAt;
         if (life >= total) {
            TOASTS.remove(idx);
            continue;
         }

         if (slot >= maxVisible) {
            break;   // older toasts stay queued but off-screen
         }
         int labelW = tr.method_1727(t.label);
         int w = 10 + labelW + 12 + pillW + 6;

         float alpha = 1f, slideX = 0f;
         float slideSpan = w + margin + 20;
         if (life < ENTER_MS) {
            float p = easeOut((float) life / ENTER_MS);
            alpha = p;
            slideX = (1f - p) * slideSpan * (right ? 1 : -1);
         } else if (life > ENTER_MS + hold) {
            float p = easeIn((float) (life - ENTER_MS - hold) / EXIT_MS);
            alpha = 1f - p;
            slideX = p * slideSpan * (right ? 1 : -1);
         }
         int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));

         int x = (right ? sw - margin - w : margin) + Math.round(slideX) + cfg.hud.moduleToastOffsetX;
         int y = (top ? margin + slot * (h + gap) : sh - margin - h - slot * (h + gap)) + cfg.hud.moduleToastOffsetY;

         Color accent = Palette.GREEN;
         // Card + hairline border, exactly like a module row in the menu.
         TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 4, fade(Palette.CARD_BG, a));
         TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 4, fade(Palette.CARD_BORDER, a));
         // 2px accent bar down the left edge (the menu's "enabled" marker).
         if (t.on) {
            ctx.method_25294(x + 2, y + 3, x + 4, y + h - 3, argb(accent, a));
         }
         // Label: accent when on, muted when off — same rule the module rows use.
         TurtUIUtils.drawText(ctx, tr, t.label, x + 10, y + (h - 8) / 2,
            fade(t.on ? accent : Palette.TEXT_MUTED, a), false, false, false);
         // Sliding pill toggle with knob.
         int px = x + w - pillW - 6, py = y + (h - pillH) / 2, r = pillH / 2;
         TurtUIUtils.drawRoundedRect(ctx, px, py, pillW, pillH, r, fade(new Color(0x66262626, true), a));
         TurtUIUtils.drawRoundedBorder(ctx, px, py, pillW, pillH, r, fade(new Color(255, 255, 255, 20), a));
         if (t.on) {
            TurtUIUtils.drawRoundedRect(ctx, px, py, pillW, pillH, r, fade(accent, a));
         }
         int knob = pillH - 4;
         int travel = pillW - knob - 4;
         int kx = px + 2 + (t.on ? travel : 0);
         TurtUIUtils.drawRoundedRect(ctx, kx, py + 2, knob, knob, knob / 2, fade(new Color(250, 250, 250, 255), a));

         slot++;
      }
   }
}
