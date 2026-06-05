package com.turtmod.utils;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_3532;

public class Renderer2D {
   public static void drawRoundedRect(class_332 context, int x, int y, int width, int height, int radius, int color) {
      context.method_25294(x + radius, y, x + width - radius, y + height, color);
      context.method_25294(x, y + radius, x + width, y + height - radius, color);
      fillCorner(context, x, y, radius, radius, color, true, true);
      fillCorner(context, x + width - radius, y, radius, radius, color, false, true);
      fillCorner(context, x, y + height - radius, radius, radius, color, true, false);
      fillCorner(context, x + width - radius, y + height - radius, radius, radius, color, false, false);
   }

   public static void drawRoundedRectOutline(class_332 context, int x, int y, int width, int height, int radius, int thickness, int color) {
      for(int i = 0; i < thickness; ++i) {
         drawRoundedRect(context, x + i, y + i, width - i * 2, height - i * 2, radius - i, color);
      }

   }

   public static void drawRoundedRectGradient(class_332 context, int x, int y, int width, int height, int radius, int colorTop, int colorBottom) {
      for(int dy = 0; dy < height; ++dy) {
         float t = (float)dy / (float)height;
         int color = lerpColor(colorTop, colorBottom, t);
         context.method_25294(x, y + dy, x + width, y + dy + 1, color);
      }

      drawRoundedRect(context, x, y, width, height, radius, 0);
   }

   private static void fillCorner(class_332 context, int x, int y, int w, int h, int color, boolean left, boolean top) {
      for(int dy = 0; dy < h; ++dy) {
         for(int dx = 0; dx < w; ++dx) {
            float distSq = (float)((left ? dx : w - 1 - dx) * (left ? dx : w - 1 - dx) + (top ? dy : h - 1 - dy) * (top ? dy : h - 1 - dy));
            if (distSq <= (float)(w * h)) {
               context.method_25294(x + dx, y + dy, x + dx + 1, y + dy + 1, color);
            }
         }
      }

   }

   public static void drawVerticalGradientLine(class_332 context, int x, int y, int height, int colorTop, int colorBottom, int thickness) {
      for(int dy = 0; dy < height; ++dy) {
         float t = (float)dy / (float)height;
         int color = lerpColor(colorTop, colorBottom, t);
         context.method_25294(x, y + dy, x + thickness, y + dy + 1, color);
      }

   }

   public static void drawHorizontalGradientLine(class_332 context, int x, int y, int width, int colorLeft, int colorRight, int thickness) {
      for(int dx = 0; dx < width; ++dx) {
         float t = (float)dx / (float)width;
         int color = lerpColor(colorLeft, colorRight, t);
         context.method_25294(x + dx, y, x + dx + 1, y + thickness, color);
      }

   }

   public static void drawMiniArrow(class_332 context, int x, int y, float scale, ArrowDirection direction, int color) {
      int[][] lines = new int[][]{{-3, 0, 3, 1}, {-2, 1, 2, 2}, {-1, 2, 1, 3}};

      for(int[] line : lines) {
         float x1 = (float)line[0] * scale;
         float y1 = (float)line[1] * scale;
         float x2 = (float)line[2] * scale;
         float y2 = (float)line[3] * scale;
         switch (direction.ordinal()) {
            case 0:
               context.method_25294((int)((float)x + x1), (int)((float)y - y2), (int)((float)x + x2), (int)((float)y - y1), color);
               break;
            case 1:
               context.method_25294((int)((float)x + x1), (int)((float)y + y1), (int)((float)x + x2), (int)((float)y + y2), color);
               break;
            case 2:
               context.method_25294((int)((float)x - y2), (int)((float)y + x1), (int)((float)x - y1), (int)((float)y + x2), color);
               break;
            case 3:
               context.method_25294((int)((float)x + y1), (int)((float)y + x1), (int)((float)x + y2), (int)((float)y + x2), color);
         }
      }

   }

   public static int lerpColor(int color1, int color2, float t) {
      int a1 = color1 >> 24 & 255;
      int r1 = color1 >> 16 & 255;
      int g1 = color1 >> 8 & 255;
      int b1 = color1 & 255;
      int a2 = color2 >> 24 & 255;
      int r2 = color2 >> 16 & 255;
      int g2 = color2 >> 8 & 255;
      int b2 = color2 & 255;
      int a = class_3532.method_48781(t, a1, a2);
      int r = class_3532.method_48781(t, r1, r2);
      int g = class_3532.method_48781(t, g1, g2);
      int b = class_3532.method_48781(t, b1, b2);
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static void drawShadow(class_332 context, int x, int y, int width, int height, int blurRadius, int shadowAlpha) {
      for(int i = 1; i <= blurRadius; ++i) {
         int alpha = (int)((double)shadowAlpha * ((double)1.0F - (double)i / (double)blurRadius));
         if (alpha > 0) {
            Color shadowColor = new Color(0, 0, 0, alpha);
            drawRoundedRect(context, x - i, y - i, width + i * 2, height + i * 2, 4, shadowColor.getRGB());
         }
      }

   }

   public static void drawCheckerboard(class_332 context, int x, int y, int width, int height, int checkSize, int color1, int color2) {
      for(int dy = 0; dy < height; dy += checkSize) {
         for(int dx = 0; dx < width; dx += checkSize) {
            boolean isDark = (dx / checkSize + dy / checkSize) % 2 == 0;
            context.method_25294(x + dx, y + dy, x + dx + checkSize, y + dy + checkSize, isDark ? color1 : color2);
         }
      }

   }

   public static enum ArrowDirection {
      UP,
      DOWN,
      LEFT,
      RIGHT;

      // $FF: synthetic method
      private static ArrowDirection[] $values() {
         return new ArrowDirection[]{UP, DOWN, LEFT, RIGHT};
      }
   }
}
