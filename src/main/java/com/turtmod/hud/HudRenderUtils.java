package com.turtmod.hud;

import net.minecraft.class_332;

public final class HudRenderUtils {
   private HudRenderUtils() {
   }

   public static void withScaled(class_332 context, int x, int y, float scale, Runnable render) {
      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float)(-x), (float)(-y));

      try {
         render.run();
      } finally {
         context.method_51448().popMatrix();
      }

   }

   public static void withLocalScaled(class_332 context, int x, int y, float scale, Runnable render) {
      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);

      try {
         render.run();
      } finally {
         context.method_51448().popMatrix();
      }

   }
}
