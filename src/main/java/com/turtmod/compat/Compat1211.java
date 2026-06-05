package com.turtmod.compat;

import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3417;

public final class Compat1211 {
   private Compat1211() {
   }

   public static void drawBorder(class_332 ctx, int x, int y, int w, int h, int color) {
      ctx.method_73198(x, y, x + w, y + h, color);
   }

   public static void playButtonSound() {
      class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
   }
}
