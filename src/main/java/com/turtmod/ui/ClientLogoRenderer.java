package com.turtmod.ui;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class ClientLogoRenderer {
   public static final class_2960 LOGO = class_2960.method_60655("turtmod", "textures/gui/logo.png");
   private static final int TEX_W = 640;
   private static final int TEX_H = 640;

   private ClientLogoRenderer() {
   }

   public static void drawLogo(class_332 context, int x, int y, int width, int height) {
      try {
         context.method_25290(class_10799.field_56883, LOGO, x, y, 0.0F, 0.0F, width, height, 640, 640);
      } catch (Throwable var6) {
         context.method_25294(x, y, x + width, y + height, -1724895184);
      }

   }

   public static void drawLogoCentered(class_332 context, int x, int y, int boxW, int boxH, int padding) {
      int maxW = Math.max(1, boxW - padding * 2);
      int maxH = Math.max(1, boxH - padding * 2);
      int size = Math.min(maxW, maxH);
      int drawX = x + (boxW - size) / 2;
      int drawY = y + (boxH - size) / 2;
      drawLogo(context, drawX, drawY, size, size);
   }
}
