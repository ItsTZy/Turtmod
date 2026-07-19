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
 * Animated toast notifications shown when a module toggles on/off in-game. Mirrors the
 * {@link com.turtmod.chat.ScreenshotPreview} animation style: a small rounded card slides in from the
 * configured corner, holds, then fades/slides out. Fed by {@code notify(label, on)} from the toggle sites.
 * Rendered each frame from {@code TurtModClient.onHudRender}. Tick + render both run on the client thread,
 * so the toast list needs no synchronization.
 */
public final class ModuleToastFeature {
   private static final long ENTER_MS = 220, EXIT_MS = 300;
   private static final int MAX_TOASTS = 5;
   private static final List<Toast> TOASTS = new ArrayList<>();

   private ModuleToastFeature() {
   }

   private static final class Toast {
      final String label;
      boolean on;
      long shownAt;
      boolean started;   // timer starts on first render, so toggles made inside a menu show once it closes

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
            t.started = false;   // restart its appear animation
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
      final int margin = 8, gap = 6, h = 24;

      // Newest toast sits closest to the corner; older ones stack away from it.
      int slot = 0;
      for (int idx = TOASTS.size() - 1; idx >= 0; idx--) {
         Toast t = TOASTS.get(idx);
         if (!t.started) {   // begin the timer the first frame this toast is actually drawn
            t.started = true;
            t.shownAt = now;
         }
         long life = now - t.shownAt;
         if (life >= total) {
            TOASTS.remove(idx);
            continue;
         }

         String status = t.on ? "ON" : "OFF";
         int labelW = tr.method_1727(t.label);
         int pillW = tr.method_1727(status) + 14;
         int w = 14 + labelW + 10 + pillW + 8; // bar+pad + label + gap + pill + pad

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

         int x = (right ? sw - margin - w : margin) + Math.round(slideX);
         int y = top ? margin + slot * (h + gap) : sh - margin - h - slot * (h + gap);

         Color accent = t.on ? Palette.GREEN : Palette.TEXT_MUTED;
         // Glass card + accent-tinted rounded border (mod-menu style, no drop shadow).
         TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 6, Palette.alpha(Palette.PANEL_BG, a * 240 / 255));
         TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 6, Palette.alpha(Palette.PANEL_BORDER, a));
         // Left accent bar.
         TurtUIUtils.drawRoundedRect(ctx, x + 5, y + 5, 3, h - 10, 1, Palette.alpha(accent, a));
         // Module name.
         int ty = y + (h - 8) / 2;
         ctx.method_51433(tr, t.label, x + 13, ty, argb(Palette.TEXT, a), false);
         // ON/OFF pill on the right.
         int px = x + w - 8 - pillW, py = y + (h - 12) / 2;
         if (t.on) {
            TurtUIUtils.drawRoundedRect(ctx, px, py, pillW, 12, 6, Palette.alpha(accent, a));
            ctx.method_25300(tr, status, px + pillW / 2, py + 2, argb(Palette.PANEL_BG, a));
         } else {
            TurtUIUtils.drawRoundedRect(ctx, px, py, pillW, 12, 6, Palette.alpha(accent, a * 40 / 255));
            TurtUIUtils.drawRoundedBorder(ctx, px, py, pillW, 12, 6, Palette.alpha(accent, a));
            ctx.method_25300(tr, status, px + pillW / 2, py + 2, argb(accent, a));
         }

         slot++;
      }
   }
}
