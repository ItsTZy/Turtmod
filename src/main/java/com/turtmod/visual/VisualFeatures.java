package com.turtmod.visual;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_310;

public final class VisualFeatures {
   private VisualFeatures() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client != null && config != null) {
         FullbrightFeature.tick(client, config);
         ScreenEffectsFeature.tick(client, config);
      }
   }
}
