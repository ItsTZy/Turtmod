package com.turtmod.visual;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_310;

public final class FullbrightFeature {
   private static boolean wasFullbrightActive = false;
   private static double cachedGamma = (double)-1.0F;
   private static Double cachedDarknessScale = null;

   private FullbrightFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client.field_1690 != null) {
         boolean shouldBeActive = config.misc.enabled && config.visual.fullbright.enabled;
         if (shouldBeActive) {
            if (!wasFullbrightActive) {
               cachedGamma = (Double)client.field_1690.method_42473().method_41753();
               cachedDarknessScale = getDarknessScale(client);
            }

            double gammaValue = Math.min((double)1.0F, config.visual.fullbright.value / (double)32.0F);
            client.field_1690.method_42473().method_41748(gammaValue);
            setDarknessScale(client, (double)0.0F);
         } else if (wasFullbrightActive && cachedGamma >= (double)0.0F) {
            client.field_1690.method_42473().method_41748(cachedGamma);
            if (cachedDarknessScale != null) {
               setDarknessScale(client, cachedDarknessScale);
            }

            cachedGamma = (double)-1.0F;
            cachedDarknessScale = null;
         }

         wasFullbrightActive = shouldBeActive;
      }
   }

   public static void onDisable() {
      class_310 client = class_310.method_1551();
      if (client != null && client.field_1690 != null && cachedGamma >= (double)0.0F) {
         client.field_1690.method_42473().method_41748(cachedGamma);
      }

      if (client != null && cachedDarknessScale != null) {
         setDarknessScale(client, cachedDarknessScale);
      }

      cachedGamma = (double)-1.0F;
      cachedDarknessScale = null;
      wasFullbrightActive = false;
   }

   private static void setDarknessScale(class_310 client, double value) {
      try {
         Object option = client.field_1690.getClass().getMethod("getDarknessEffectScale").invoke(client.field_1690);
         option.getClass().getMethod("setValue", Object.class).invoke(option, value);
      } catch (Throwable var4) {
      }

   }

   private static Double getDarknessScale(class_310 client) {
      try {
         Object option = client.field_1690.getClass().getMethod("getDarknessEffectScale").invoke(client.field_1690);
         Object current = option.getClass().getMethod("getValue").invoke(option);
         if (current instanceof Number number) {
            return number.doubleValue();
         }
      } catch (Throwable var4) {
      }

      return null;
   }
}
