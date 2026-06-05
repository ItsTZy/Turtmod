package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;

public final class ElytraPitchFeature {
   private ElytraPitchFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (config.visual.elytraPitchHud && client.field_1724 != null && client.field_1724.method_6128()) {
         int pitch = (int)client.field_1724.method_36455();
         String text = pitch + "°";
         if (config.visual.elytraPitchShowYaw) {
            int yaw = Math.round(class_3532.method_15393(client.field_1724.method_36454()));
            text = text + "  " + yaw + "°";
         }

         int w = client.field_1772.method_1727(text);
         int x = context.method_51421() / 2 - w / 2;
         int y = context.method_51443() / 2 + 16;
         context.method_27535(client.field_1772, class_2561.method_43470(text), x, y, -1);
      }
   }
}
