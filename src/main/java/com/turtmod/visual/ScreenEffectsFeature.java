package com.turtmod.visual;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_310;

public final class ScreenEffectsFeature {
   private static double cachedDistortion = (double)-1.0F;
   private static double cachedDarkness = (double)-1.0F;
   private static double cachedDamageTilt = (double)-1.0F;
   private static Boolean cachedBobView = null;

   private ScreenEffectsFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client.field_1690 != null) {
         if (!config.misc.enabled) {
            restore(client);
         } else {
            if (config.visual.disableNauseaDistortion) {
               if (cachedDistortion < (double)0.0F) {
                  cachedDistortion = (Double)client.field_1690.method_42453().method_41753();
               }

               client.field_1690.method_42453().method_41748((double)0.0F);
            } else if (cachedDistortion >= (double)0.0F) {
               client.field_1690.method_42453().method_41748(cachedDistortion);
               cachedDistortion = (double)-1.0F;
            }

            if (config.visual.disableDarknessOverlay) {
               if (cachedDarkness < (double)0.0F) {
                  cachedDarkness = getDarknessScale(client);
               }

               setDarknessScale(client, (double)0.0F);
            } else if (cachedDarkness >= (double)0.0F) {
               setDarknessScale(client, cachedDarkness);
               cachedDarkness = (double)-1.0F;
            }

            if (config.visual.disableScreenShake) {
               if (cachedBobView == null) {
                  cachedBobView = (Boolean)client.field_1690.method_42448().method_41753();
               }

               client.field_1690.method_42448().method_41748(false);
            } else if (cachedBobView != null) {
               client.field_1690.method_42448().method_41748(cachedBobView);
               cachedBobView = null;
            }

         }
      }
   }

   private static void restore(class_310 client) {
      if (cachedDistortion >= (double)0.0F) {
         client.field_1690.method_42453().method_41748(cachedDistortion);
         cachedDistortion = (double)-1.0F;
      }

      if (cachedDarkness >= (double)0.0F) {
         setDarknessScale(client, cachedDarkness);
         cachedDarkness = (double)-1.0F;
      }

      if (cachedBobView != null) {
         client.field_1690.method_42448().method_41748(cachedBobView);
         cachedBobView = null;
      }

   }

   private static double getDarknessScale(class_310 client) {
      try {
         return (Double)client.field_1690.method_42472().method_41753();
      } catch (Throwable var6) {
         try {
            Object option = client.field_1690.getClass().getMethod("getDarknessEffectScale").invoke(client.field_1690);
            Object current = option.getClass().getMethod("getValue").invoke(option);
            double var10000;
            if (current instanceof Number number) {
               var10000 = number.doubleValue();
            } else {
               var10000 = (double)1.0F;
            }

            return var10000;
         } catch (Throwable var5) {
            return (double)1.0F;
         }
      }
   }

   private static void setDarknessScale(class_310 client, double value) {
      try {
         client.field_1690.method_42472().method_41748(value);
      } catch (Throwable var6) {
         try {
            Object option = client.field_1690.getClass().getMethod("getDarknessEffectScale").invoke(client.field_1690);
            option.getClass().getMethod("setValue", Object.class).invoke(option, value);
         } catch (Throwable var5) {
         }
      }

   }
}
