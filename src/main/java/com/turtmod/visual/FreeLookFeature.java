package com.turtmod.visual;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5498;

public final class FreeLookFeature {
   private static boolean active;
   private static float yaw;
   private static float pitch;
   private static class_5498 previousPerspective;

   private FreeLookFeature() {
   }

   public static void updateActive(class_310 client, TurtModConfig config, boolean shouldBeActive) {
      if (client != null && client.field_1724 != null && client.field_1690 != null) {
         if (shouldBeActive != active) {
            if (shouldBeActive) {
               active = true;
               yaw = client.field_1724.method_36454();
               pitch = client.field_1724.method_36455();
               previousPerspective = client.field_1690.method_31044();
               client.field_1690.method_31043(class_5498.field_26665);
            } else {
               active = false;
               if (previousPerspective != null) {
                  client.field_1690.method_31043(previousPerspective);
               }

               previousPerspective = null;
            }

         }
      } else {
         active = false;
      }
   }

   public static boolean handleMouseLook(class_1297 entity, double cursorDeltaX, double cursorDeltaY, TurtModConfig config) {
      if (!active) {
         return false;
      } else {
         class_310 client = class_310.method_1551();
         if (client.field_1724 != null && entity == client.field_1724) {
            float scale = (float)config.visual.freelookSensitivityPercent / 100.0F;
            yaw += (float)(cursorDeltaX * (double)0.15F * (double)scale);
            pitch += (float)(cursorDeltaY * (double)0.15F * (double)scale);
            pitch = class_3532.method_15363(pitch, -90.0F, 90.0F);
            return true;
         } else {
            return false;
         }
      }
   }

   public static boolean isActive() {
      return active;
   }

   public static float getYaw() {
      return yaw;
   }

   public static float getPitch() {
      return pitch;
   }
}
