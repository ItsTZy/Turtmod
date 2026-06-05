package com.turtmod.ui;

import net.minecraft.class_332;
import net.minecraft.class_3532;

public final class ClientUiTheme {
   public static final int BG_TOP = -16053492;
   public static final int BG_BOTTOM = -15395561;
   public static final int PANEL_BG = -871230956;
   public static final int PANEL_BORDER = -13882321;
   public static final int ACCENT = -1118479;
   public static final int ACCENT_DIM = -9474187;
   public static final int TEXT = -723722;
   public static final int MUTED = -5197643;

   private ClientUiTheme() {
   }

   public static void drawBackdrop(class_332 context, int width, int height, float tick) {
      context.method_25296(0, 0, width, height, -16053492, -15395561);
      int grid = 12;
      int line = 402653184;

      for(int x = 0; x < width; x += grid) {
         context.method_25294(x, 0, x + 1, height, line);
      }

      for(int y = 0; y < height; y += grid) {
         context.method_25294(0, y, width, y + 1, line);
      }

      float wave = (class_3532.method_15374((double)(tick * 0.015F)) + 1.0F) * 0.5F;
      int glow = (int)(70.0F + wave * 40.0F) << 24 | 15658737;
      context.method_25294(0, height - 2, width, height, glow);
   }

   public static void drawPanel(class_332 context, int x, int y, int w, int h, float alpha) {
      int bg = (int)(alpha * 255.0F) << 24 | 1184276;
      context.method_25294(x, y, x + w, y + h, bg);
      int border = (int)(alpha * 255.0F) << 24 | 2894895;
      context.method_73198(x, y, w, h, border);
      int accent = (int)(alpha * 180.0F) << 24 | 7303029;
      context.method_25294(x + 1, y + 1, x + w - 1, y + 2, accent);
   }

   public static void drawCard(class_332 context, int x, int y, int w, int h, float alpha, boolean hovered) {
      int base = hovered ? -15000801 : -15461353;
      int bg = (int)(alpha * 220.0F) << 24 | base & 16777215;
      context.method_25294(x, y, x + w, y + h, bg);
      int border = (int)(alpha * 255.0F) << 24 | (hovered ? -1118479 : -13882321);
      context.method_73198(x, y, w, h, border);
      if (hovered) {
         int glow = (int)(alpha * 90.0F) << 24 | 15658737;
         context.method_25294(x + 1, y + 1, x + w - 1, y + 2, glow);
      }

   }
}
