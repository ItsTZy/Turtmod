package com.turtmod.visual;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_310;

public final class HurtCamFeature {
   private static double cachedDamageTilt = (double)-1.0F;

   private HurtCamFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client.field_1690 != null) {
         if (config.visual.hurtCamEnabled && config.misc.enabled) {
            double baseTilt = Math.max((double)0.0F, Math.min((double)1.5F, (double)config.visual.hurtCameraShakePercent / (double)100.0F));
            boolean oldStyle = config.visual.oldHurtCameraStyle || config.visual.hurtCamMode == TurtModConfig.HurtCamMode.OLD_NON_DIRECTIONAL;
            double targetDamageTilt = config.visual.hurtCamMode == TurtModConfig.HurtCamMode.OFF ? (double)0.0F : (oldStyle ? Math.min((double)1.5F, baseTilt * (double)1.25F) : baseTilt);
            if (cachedDamageTilt < (double)0.0F) {
               cachedDamageTilt = (Double)client.field_1690.method_48974().method_41753();
            }

            client.field_1690.method_48974().method_41748(targetDamageTilt);
         } else if (cachedDamageTilt >= (double)0.0F) {
            client.field_1690.method_48974().method_41748(cachedDamageTilt);
            cachedDamageTilt = (double)-1.0F;
         }

      }
   }

   public static void restore(class_310 client) {
      if (cachedDamageTilt >= (double)0.0F) {
         client.field_1690.method_48974().method_41748(cachedDamageTilt);
         cachedDamageTilt = (double)-1.0F;
      }

   }
}
