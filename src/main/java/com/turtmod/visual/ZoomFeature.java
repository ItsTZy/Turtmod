package com.turtmod.visual;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_307;

public class ZoomFeature {
   private static float zoomModifier = 0.1F;
   private static boolean toggleZoomActive = false;
   private static boolean wasZooming = false;
   private static boolean initialized = false;
   private static final class_304 zoomKey;

   public static boolean isZooming() {
      TurtModConfig config = TurtModClient.getConfig();
      class_310 client = class_310.method_1551();
      if (config != null && client != null && config.visual.zoomEnabled) {
         if (client.field_1755 != null) {
            return false;   // a screen is open
         } else {
            // Works in all perspectives (first person + both third-person views)
            return config.visual.zoomToggleMode ? toggleZoomActive : zoomKey.method_1434();
         }
      } else {
         return false;
      }
   }

   public static boolean isZoomActive() {
      return isZooming();
   }

   public static float getFovModifier() {
      if (zoomModifier > 1.0F) {
         zoomModifier = 1.0F;
      }

      if (zoomModifier <= 0.0F) {
         zoomModifier = 1.0E-10F;
      }

      return zoomModifier;
   }

   public static boolean shouldUseSmoothZoom() {
      TurtModConfig config = TurtModClient.getConfig();
      return config != null && config.visual.zoomEnabled && config.visual.zoomSmoothInOut;
   }

   public static boolean shouldHideArmsWhenZooming() {
      TurtModConfig config = TurtModClient.getConfig();
      return isZooming() && config != null && config.visual.zoomHideArms;
   }

   public static class_304 getZoomKey() {
      return zoomKey;
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (config == null) {
         toggleZoomActive = false;
         wasZooming = false;
         zoomModifier = 0.1F;
      } else {
         if (config.visual.zoomToggleMode) {
            while(zoomKey.method_1436()) {
               toggleZoomActive = !toggleZoomActive;
               if (config.visual.zoomEnabled) {
                  com.turtmod.hud.ModuleToastFeature.notify("Zoom", toggleZoomActive);
               }
            }
         } else {
            toggleZoomActive = false;
         }

         if (!config.visual.zoomEnabled && toggleZoomActive) {
            toggleZoomActive = false;
         }

         if (zoomModifier <= 0.0F || Float.isNaN(zoomModifier)) {
            zoomModifier = getBaseZoomLevel(config);
         }

         boolean zooming = isZooming();
         // Rising edge: sync to the configured base level when zoom STARTS, not only when it stops.
         // Without this the very first zoom after launch uses the stale initial modifier instead of
         // the level set in settings (subsequent zooms looked fine because the stop-reset had run).
         if (zooming && !wasZooming) {
            if (config.visual.zoomResetOnStop || !initialized) {
               zoomModifier = getBaseZoomLevel(config);
            }
            initialized = true;
         }
         if (!zooming && wasZooming && config.visual.zoomResetOnStop) {
            zoomModifier = getBaseZoomLevel(config);
         }

         wasZooming = zooming;
      }
   }

   public static boolean onMouseScroll(double delta) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && isZooming()) {
         if (delta < (double)0.0F) {
            zoomModifier += config.visual.zoomOutPerScroll;
         } else if (delta > (double)0.0F) {
            zoomModifier -= config.visual.zoomInPerScroll;
         }

         zoomModifier = Math.max(0.01F, Math.min(1.0F, zoomModifier));
         return true;
      } else {
         return false;
      }
   }

   public static float getMouseSensitivityMultiplier() {
      double zoomRatio = (double)getFovModifier();
      double scale = zoomRatio;
      if (zoomRatio < (double)0.5F) {
         scale = zoomRatio * zoomRatio;
      }

      if (zoomRatio < 0.1) {
         scale = Math.pow(zoomRatio, (double)3.0F);
      }

      return (float)scale;
   }

   private static float getBaseZoomLevel(TurtModConfig config) {
      // Slider is "zoom strength" (higher = more zoom). The FOV modifier is the inverse,
      // so a bigger strength yields a smaller FOV multiplier = more zoom.
      float strength = Math.max(1.0F, config.visual.zoomBaseLevel);
      return 1.0F / strength;
   }

   static {
      zoomKey = new class_304("key.turtmod.zoom", class_307.field_1668, 67, class_11900.method_74698(class_2960.method_60655("turtmod", "zoom")));
   }
}
