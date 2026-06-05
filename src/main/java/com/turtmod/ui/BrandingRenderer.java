package com.turtmod.ui;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class BrandingRenderer {
   public static final class_2960 LOGO = class_2960.method_60655("turtmod", "textures/gui/logo.png");
   public static final class_2960 FALLBACK_LOGO = class_2960.method_60655("turtmod", "icon.png");
   private static final int DEFAULT_TEX_WIDTH = 640;
   private static final int DEFAULT_TEX_HEIGHT = 640;

   private BrandingRenderer() {
   }

   public static void drawLogo(class_332 context, int x, int y, int width, int height) {
      drawTextureSafe(context, LOGO, x, y, width, height);
   }

   private static void drawTextureSafe(class_332 context, class_2960 id, int x, int y, int width, int height) {
      try {
         context.method_25302(class_10799.field_56883, id, x, y, 0.0F, 0.0F, width, height, 640, 640, 640, 640);
      } catch (Throwable var9) {
         try {
            context.method_25302(class_10799.field_56883, FALLBACK_LOGO, x, y, 0.0F, 0.0F, width, height, 256, 256, 256, 256);
         } catch (Throwable var8) {
            context.method_25294(x, y, x + width, y + height, -1724503510);
         }
      }

   }
}
