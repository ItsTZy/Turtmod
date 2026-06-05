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

   // Rounded corner rect (corner-cut style, no shader needed)
   public static void drawRoundedRect(class_332 context, int x, int y, int w, int h, int radius, Color c) {
      int r = Math.min(radius, Math.min(w, h) / 2);
      int argb = c.getRGB();
      // center fill
      context.method_25294(x + r, y, x + w - r, y + h, argb);
      // left/right strips
      context.method_25294(x, y + r, x + r, y + h - r, argb);
      context.method_25294(x + w - r, y + r, x + w, y + h - r, argb);
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

   private static void drawSoftGlow(class_332 context, int cx, int cy, Color color) {
      for (int r = 96; r >= 12; r -= 14) {
         int a = (int)(6.0f * (1.0f - (float) r / 96f)) + 1;
         drawRoundedRect(context, cx - r, cy - r, r * 2, r * 2, r / 2,
            new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
      }
   }

   // Exponential lerp — widget stores a float, calls this each frame
   public static float lerp01(float current, float target, float dtSeconds, float speed) {
      float t = 1.0f - (float) Math.exp(-speed * dtSeconds);
      return current + (target - current) * t;
   }
}
