package com.turtmod.ui;

import java.awt.Color;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class TurtUIUtils {
   private static float shift = 0.0F;
   private static final float STEP = 0.01F;
   private static final long TICK_INTERVAL_NS = 50000000L;
   private static long lastTickTime = System.nanoTime();

   public static void update() {
      long now = System.nanoTime();
      if (now - lastTickTime >= 50000000L) {
         lastTickTime += 50000000L;
         shift += 0.01F;
         if (shift >= 1.0F) {
            --shift;
         }
      }

   }

   public static float getShift() {
      return shift;
   }

   public static void drawRectangle(class_332 context, int x, int y, int w, int h, Color c) {
      context.method_25294(x, y, x + w, y + h, c.getRGB());
   }

   public static void drawGradientRectangle(class_332 context, int x, int y, int w, int h, Color start, Color end) {
      drawGradient(context, x, y, w, h, start, end, true);
   }

   public static void drawHorizontalGradient(class_332 context, int x, int y, int w, int h, Color leftColor, Color rightColor) {
      drawGradient(context, x, y, w, h, leftColor, rightColor, false);
   }

   private static void drawGradient(class_332 context, int x, int y, int w, int h, Color start, Color end, boolean vertical) {
      int steps = Math.max(w, h);

      for(int i = 0; i < steps; ++i) {
         float t = (float)i / (float)steps;
         Color c = blend(start, end, t);
         if (vertical) {
            context.method_25294(x, y + i, x + w, y + i + 1, c.getRGB());
         } else {
            context.method_25294(x + i, y, x + i + 1, y + h, c.getRGB());
         }
      }

   }

   public static void drawBorder(class_332 context, int x, int y, int w, int h, Color c) {
      int thickness = 1;
      context.method_25294(x, y, x + w, y + thickness, c.getRGB());
      context.method_25294(x, y + h - thickness, x + w, y + h, c.getRGB());
      context.method_25294(x, y, x + thickness, y + h, c.getRGB());
      context.method_25294(x + w - thickness, y, x + w, y + h, c.getRGB());
   }

   public static void drawGradientBorder(class_332 context, int x, int y, int w, int h, Color start, Color end) {
      int thickness = 1;
      drawHorizontalGradient(context, x, y, w, thickness, start, end);
      drawHorizontalGradient(context, x, y + h - thickness, w, thickness, start, end);
      drawGradient(context, x, y, thickness, h, start, end, false);
      drawGradient(context, x + w - thickness, y, thickness, h, start, end, false);
   }

   public static void drawText(class_332 context, class_327 tr, String text, int x, int y, Color c, boolean center, boolean shadow, boolean bold) {
      String renderText = bold ? "§l" + text + "§r" : text;
      int drawX = center ? x - tr.method_1727(renderText) / 2 : x;
      if (shadow) {
         context.method_25303(tr, renderText, drawX, y, c.getRGB());
      } else {
         context.method_51433(tr, renderText, drawX, y, c.getRGB(), false);
      }

   }

   public static void drawText(class_332 context, class_327 tr, String text, int x, int y, Color c, boolean center, boolean shadow) {
      drawText(context, tr, text, x, y, c, center, shadow, false);
   }

   public static void drawShadow(class_332 context, int x, int y, int w, int h, int size) {
      for(int i = 1; i <= size; ++i) {
         int alpha = (int)((double)20.0F * ((double)1.0F - (double)i / (double)size));
         if (alpha > 0) {
            Color shadowColor = new Color(0, 0, 0, alpha);
            drawBorder(context, x - i, y - i, w + i * 2, h + i * 2, shadowColor);
         }
      }

   }

   public static void drawGradientText(class_332 context, class_327 tr, String text, int x, int y, Color start, Color end, boolean center, boolean bold) {
      // Render each character with interpolated color (no §l§r — they break char-by-char rendering)
      int drawX = center ? x - tr.method_1727(text) / 2 : x;
      int charWidth = 0;
      for (int i = 0; i < text.length(); i++) {
         String c = String.valueOf(text.charAt(i));
         float t = text.length() > 1 ? (float)i / (float)(text.length() - 1) : 0f;
         Color color = blend(start, end, t);
         if (bold) {
            context.method_25303(tr, c, drawX + charWidth, y, color.getRGB());
         } else {
            context.method_51433(tr, c, drawX + charWidth, y, color.getRGB(), false);
         }
         charWidth += tr.method_1727(c);
      }
   }

   public static void drawGradientText(class_332 context, class_327 tr, String text, int x, int y, Color start, Color end, boolean center) {
      drawGradientText(context, tr, text, x, y, start, end, center, false);
   }

   public static void drawHLine(class_332 context, int x, int y, int length, Color c, int thickness) {
      drawRectangle(context, x, y - thickness / 2, length, thickness, c);
   }

   public static void drawHGradientLine(class_332 context, int x, int y, int length, Color left, Color right, int thickness) {
      drawHorizontalGradient(context, x, y - thickness / 2, length, thickness, left, right);
   }

   public static Color blend(Color c1, Color c2, float t) {
      float inv = 1.0F - t;
      int a = (int)((float)c1.getAlpha() * inv + (float)c2.getAlpha() * t);
      int r = (int)((float)c1.getRed() * inv + (float)c2.getRed() * t);
      int g = (int)((float)c1.getGreen() * inv + (float)c2.getGreen() * t);
      int b = (int)((float)c1.getBlue() * inv + (float)c2.getBlue() * t);
      return new Color(r, g, b, a);
   }

   public static boolean isHovered(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   /**
    * Rounded rect with anti-aliased corners.
    *
    * <p>Per corner row we fill one solid span and add a single partial-coverage pixel at each end,
    * so cost stays O(radius) — about {@code 1 + 6r} fills. An earlier version shaded every pixel of
    * the r×r corner box (O(r²), ~576 fills at r=12); with the dozens of rounded elements a menu
    * draws that flooded the render thread and hung the game, so keep this loop O(r).
    */
   public static void drawRoundedRect(class_332 context, int x, int y, int w, int h, int radius, Color c) {
      int r = Math.min(radius, Math.min(w, h) / 2);
      int argb = c.getRGB();
      if (r <= 0) {
         context.method_25294(x, y, x + w, y + h, argb);
         return;
      }
      // Crisp corners: each arc row is filled to a single integer inset (no fractional-alpha fringe, which
      // smears/blurs once the UI is drawn inside a scaled matrix). Sharp at the small radii the mod uses.
      context.method_25294(x, y + r, x + w, y + h - r, argb);   // body between the two arc bands
      for (int i = 0; i < r; i++) {
         double dy = r - (i + 0.5);
         double dx = Math.sqrt((double) r * r - dy * dy);
         int inset = (int) Math.round(r - dx);
         int left = x + inset;
         int right = x + w - inset;
         if (right > left) {
            context.method_25294(left, y + i, right, y + i + 1, argb);
            context.method_25294(left, y + h - i - 1, right, y + h - i, argb);
         }
      }
   }

   /** Crisp 1px border matching {@link #drawRoundedRect} — hard-edged arcs, no blurry alpha fringe. */
   public static void drawRoundedBorder(class_332 context, int x, int y, int w, int h, int radius, Color c) {
      int r = Math.min(radius, Math.min(w, h) / 2);
      int argb = c.getRGB();
      if (r <= 0) {
         context.method_73198(x, y, w, h, argb);
         return;
      }
      // Straight edges between the corner arcs.
      context.method_25294(x + r, y, x + w - r, y + 1, argb);
      context.method_25294(x + r, y + h - 1, x + w - r, y + h, argb);
      context.method_25294(x, y + r, x + 1, y + h - r, argb);
      context.method_25294(x + w - 1, y + r, x + w, y + h - r, argb);
      // Corner arcs: draw a horizontal run per row that connects to the previous row's inset, so the staircase
      // stays continuous (no diagonal gaps) and crisp — no fractional-alpha pixels to blur when scaled.
      int prevInset = r;
      for (int i = 0; i < r; i++) {
         double dy = r - (i + 0.5);
         double dx = Math.sqrt((double) r * r - dy * dy);
         int inset = (int) Math.round(r - dx);
         int runL = x + inset;
         int runR = x + prevInset + 1;
         int rMirrorL = x + w - prevInset - 1;
         int rMirrorR = x + w - inset;
         if (runR > runL) {
            context.method_25294(runL, y + i, runR, y + i + 1, argb);            // top-left
            context.method_25294(runL, y + h - i - 1, runR, y + h - i, argb);    // bottom-left
         }
         if (rMirrorR > rMirrorL) {
            context.method_25294(rMirrorL, y + i, rMirrorR, y + i + 1, argb);          // top-right
            context.method_25294(rMirrorL, y + h - i - 1, rMirrorR, y + h - i, argb);  // bottom-right
         }
         prevInset = inset;
      }
   }

   // Glass-style panel: body at ~60% alpha, bright top strip, 1px outline
   public static void drawGlassPanel(class_332 context, int x, int y, int w, int h, Color base) {
      Color body = new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.max(0, base.getAlpha() - 60));
      Color highlight = new Color(255, 255, 255, 30);
      Color outline = new Color(255, 255, 255, 18);
      drawRoundedRect(context, x, y, w, h, 4, body);
      context.method_25294(x + 2, y, x + w - 2, y + 2, highlight.getRGB());
      context.method_73198(x, y, w, h, outline.getRGB());
   }

   // Pastel-pink hover glow (3 expanding rings outside bounds)
   public static void drawHoverGlow(class_332 context, int x, int y, int w, int h, int radius, float glowAmount, Color accentPink) {
      if (glowAmount <= 0.005f) return;
      for (int ring = 1; ring <= 3; ring++) {
         float falloff = (4 - ring) / 3.0f;
         int alpha = (int)(accentPink.getAlpha() * glowAmount * falloff * 0.45f);
         if (alpha <= 0) continue;
         Color glow = new Color(accentPink.getRed(), accentPink.getGreen(), accentPink.getBlue(), alpha);
         context.method_73198(x - ring, y - ring, w + ring * 2, h + ring * 2, glow.getRGB());
      }
   }

   // Premium menu backdrop: a soft vertical gradient + edge vignette for depth/focus.
   // Drawn over the vanilla blurred background, under the panel.
   public static void drawMenuBackdrop(class_332 context, int w, int h) {
      drawGradientRectangle(context, 0, 0, w, h, new Color(9, 11, 18, 140), new Color(3, 5, 9, 185));

      // Two gently drifting soft accent glows (pink + green) for a living, premium feel.
      double t = System.currentTimeMillis() * 0.00015;
      int px = (int)(w * 0.30 + Math.sin(t) * w * 0.16);
      int py = (int)(h * 0.40 + Math.cos(t * 0.8) * h * 0.13);
      drawSoftGlow(context, px, py, new Color(255, 105, 180));
      int gx = (int)(w * 0.72 + Math.sin(t * 0.7 + 2.0) * w * 0.15);
      int gy = (int)(h * 0.62 + Math.cos(t * 1.1 + 1.0) * h * 0.12);
      drawSoftGlow(context, gx, gy, new Color(120, 230, 120));

      int size = Math.max(24, Math.min(w, h) / 6);
      for (int i = 0; i < size; i++) {
         int a = (int)(3.2f * (1.0f - (float) i / size));
         if (a <= 0) continue;
         context.method_73198(i, i, w - i * 2, h - i * 2, new Color(0, 0, 0, a).getRGB());
      }
   }

   /**
    * Reusable "presentation stage" backdrop (the look used behind the skin preview): an accent-tinted
    * spotlight fading into a dark floor, plus a faint bright core near the top. Use behind any framed
    * content (model previews, icons, feature cards) for a premium, consistent feel.
    */
   public static void drawStage(class_332 context, int x, int y, int w, int h, Color accent) {
      int topGlow = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 42).getRGB();
      context.method_25296(x, y, x + w, y + h, topGlow, 0x00101418);
      // faint bright core near the top-centre
      int cx = x + w / 2;
      int coreY = y + h / 5;
      for (int r = Math.min(w, h) / 3; r > 4; r -= 6) {
         int a = (int)(10.0f * (1.0f - (float) r / (Math.min(w, h) / 3f))) + 1;
         drawRoundedRect(context, cx - r, coreY - r / 2, r * 2, r, r / 2, new Color(255, 255, 255, a));
      }
   }

   /** Subtle accent glow that follows the cursor — draw over the backdrop, under the panel. */
   public static void drawCursorGlow(class_332 context, int mx, int my) {
      // A small, very faint glow — the old 64px green halo trailing the cursor read as busy.
      for (int r = 22; r >= 8; r -= 7) {
         int a = (int)(3.0f * (1.0f - (float) r / 22f)) + 1;
         drawRoundedRect(context, mx - r, my - r, r * 2, r * 2, r / 2, new Color(120, 230, 160, a));
      }
   }

   private static void drawSoftGlow(class_332 context, int cx, int cy, Color color) {
      for (int r = 96; r >= 12; r -= 14) {
         int a = (int)(6.0f * (1.0f - (float) r / 96f)) + 1;
         drawRoundedRect(context, cx - r, cy - r, r * 2, r * 2, r / 2,
            new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
      }
   }

   /**
    * Consistent ~180 ms black fade-in overlay used when a screen opens. Pass the screen's eased
    * {@code openFade} (0→1); draw LAST, in screen-space, so the whole UI fades up together. Cheap
    * (one fill); keeps cross-screen motion uniform without a fade-OUT (which would need async).
    */
   public static void drawOpenFade(class_332 context, int w, int h, float openFade) {
      if (openFade < 0.99f) {
         int a = (int) ((1.0f - openFade) * 255.0f) & 0xFF;
         context.method_25294(0, 0, w, h, a << 24);
      }
   }

   // Exponential lerp — widget stores a float, calls this each frame
   public static float lerp01(float current, float target, float dtSeconds, float speed) {
      float t = 1.0f - (float) Math.exp(-speed * dtSeconds);
      return current + (target - current) * t;
   }

   // ----- Lunar-style screen intro animation (shared, keyed by screen) -----
   private static final java.util.Map<String, float[]> introVal = new java.util.HashMap<>();
   private static final java.util.Map<String, Long> introTs = new java.util.HashMap<>();

   /** Eased 0→1 intro progress for a screen, auto-advanced by real frame time. Call once per frame. */
   public static float intro(String key) {
      long now = System.nanoTime();
      float[] v = introVal.computeIfAbsent(key, k -> new float[]{0.0F});
      Long last = introTs.put(key, now);
      float dt = last == null ? 0.0F : Math.min((now - last) / 1.0E9F, 0.05F);
      v[0] = lerp01(v[0], 1.0F, dt, 11.0F);
      if (v[0] > 0.999F) {
         v[0] = 1.0F;
      }
      return v[0];
   }

   /** Restart a screen's intro (call from the screen's init()/onOpen). */
   public static void resetIntro(String key) {
      introVal.remove(key);
      introTs.remove(key);
   }

   /** Smoothstep ease for nicer accel/decel than linear progress. */
   public static float ease(float p) {
      p = p < 0 ? 0 : (p > 1 ? 1 : p);
      return p * p * (3.0F - 2.0F * p);
   }

   /**
    * Push a subtle scale-up + rise transform around a centre point for a Lunar-style screen entrance.
    * Call {@link #endIntro} after drawing the screen's content. {@code p} is the value from {@link #intro}.
    */
   public static void beginIntro(class_332 ctx, float p, int cx, int cy) {
      float e = ease(p);
      float scale = 0.97F + 0.03F * e;
      float rise = (1.0F - e) * 12.0F;
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate((float) cx, (float) cy + rise);
      ctx.method_51448().scale(scale, scale);
      ctx.method_51448().translate((float) -cx, (float) -cy);
   }

   public static void endIntro(class_332 ctx) {
      ctx.method_51448().popMatrix();
   }
}
