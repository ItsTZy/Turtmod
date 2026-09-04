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

   /**
    * Restores the perspective Freelook replaced and clears every piece of Freelook state.
    *
    * <p>Safe to call more than once: the restore is gated on {@code active}, so a second call is a
    * no-op and can never fight the user's own F5 choice. It always puts back the perspective the
    * user actually had - it never blindly forces first person.
    */
   public static void reset(class_310 client) {
      if (active && previousPerspective != null && client != null && client.field_1690 != null) {
         client.field_1690.method_31043(previousPerspective);
      }
      active = false;
      previousPerspective = null;
      yaw = 0.0F;
      pitch = 0.0F;
   }

   public static void updateActive(class_310 client, TurtModConfig config, boolean shouldBeActive) {
      // No options to restore through - just make sure we don't keep reporting ourselves as active.
      if (client == null || client.field_1690 == null) {
         active = false;
         previousPerspective = null;
         return;
      }

      // No player: disconnect, title screen or world swap. GameOptions is client-level and is still
      // valid at this point, so we CAN - and must - put the camera back here. Merely clearing
      // `active` (what this used to do) left the game stuck in the third-person view Freelook forced,
      // and that stale perspective then got saved as the next "previous" perspective.
      if (client.field_1724 == null) {
         reset(client);
         return;
      }

      if (shouldBeActive == active) {
         return;
      }

      if (shouldBeActive) {
         active = true;
         yaw = client.field_1724.method_36454();
         pitch = client.field_1724.method_36455();
         previousPerspective = client.field_1690.method_31044();
         client.field_1690.method_31043(class_5498.field_26665);
      } else {
         // Normal release goes through the same single cleanup path.
         reset(client);
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
