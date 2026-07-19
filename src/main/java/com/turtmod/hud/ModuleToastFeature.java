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

      Toast(String label, boolean on, long shownAt) {
         this.label = label;
         this.on = on;
         this.shownAt = shownAt;
      }
   }

   /** Enqueue a toast for a module that just toggled. Re-toggling the same module refreshes its card. */
   public static void notify(String label, boolean on) {
      if (label == null || label.isEmpty()) {
         return;
      }
      long now = System.currentTimeMillis();
      for (Toast t : TOASTS) {
         if (t.label.equals(label)) {
            t.on = on;
            t.shownAt = now;
            return;
         }
      }
      TOASTS.add(new Toast(label, on, now));
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
      final int margin = 8, gap = 5, h = 22;

      // Newest toast sits closest to the corner; older ones stack away from it.
      int slot = 0;
      for (int idx = TOASTS.size() - 1; idx >= 0; idx--) {
         Toast t = TOASTS.get(idx);
         long life = now - t.shownAt;
         if (life >= total) {
            TOASTS.remove(idx);
            continue;
         }

         String status = t.on ? "ON" : "OFF";
         int labelW = tr.method_1727(t.label);
         int statusW = tr.method_1727(status);
         int w = 14 + 8 + labelW + 10 + statusW + 12; // dot + gaps + label + status + pads

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
         // Drop shadow, glass panel, accent-tinted border.
         ctx.method_25294(x - 2, y + 2, x + w + 2, y + h + 4, argb(Color.BLACK, a * 55 / 255));
         TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 5, Palette.alpha(Palette.PANEL_BG, a));
         TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 5, Palette.alpha(accent, a));
         // Status dot.
         int dx = x + 9, dy = y + h / 2 - 3;
         ctx.method_25294(dx, dy, dx + 6, dy + 6, argb(accent, a));
         ctx.method_25294(dx + 1, dy - 1, dx + 5, dy, argb(accent, a));
         ctx.method_25294(dx + 1, dy + 6, dx + 5, dy + 7, argb(accent, a));
         ctx.method_25294(dx - 1, dy + 1, dx, dy + 5, argb(accent, a));
         ctx.method_25294(dx + 6, dy + 1, dx + 7, dy + 5, argb(accent, a));
         // Label + status text.
         int ty = y + (h - 8) / 2;
         ctx.method_51433(tr, t.label, x + 22, ty, argb(Palette.TEXT, a), false);
         ctx.method_51433(tr, status, x + w - statusW - 12, ty, argb(accent, a), false);

         slot++;
      }
   }
}
