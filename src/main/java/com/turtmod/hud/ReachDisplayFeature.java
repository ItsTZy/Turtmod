package com.turtmod.hud;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class ReachDisplayFeature {
   private static double lastReach = (double)0.0F;
   private static double maxReach = (double)0.0F;
   private static int displayTicks = 0;
   private static String lastHitType = "";
   private static class_1297 lastHitEntity = null;

   private ReachDisplayFeature() {
   }

   private static class_243 entityPos(class_1297 e) {
      return new class_243(e.method_23317(), e.method_23318(), e.method_23321());
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.reachDisplay) {
         maxReach = client.field_1724.method_68878() ? (double)5.0F : (double)3.0F;
         if (config.hud.reachTrackNearestPlayer && client.field_1687 != null) {
            class_1657 nearest = client.field_1687.method_8604(client.field_1724.method_23317(), client.field_1724.method_23318(), client.field_1724.method_23321(), (double)Math.max(1, config.hud.reachMaxSearchDistance), (player) -> player != null && player.method_5805() && player != client.field_1724);
            if (nearest != null) {
               lastHitEntity = nearest;
               lastHitType = "P";
               lastReach = round2(client.field_1724.method_5836(1.0F).method_1022(entityPos(nearest)) - (double)(nearest.method_17681() / 2.0F));
               displayTicks = Math.max(10, configTicks());
            }
         }

         if (displayTicks > 0) {
            --displayTicks;
         } else if (!config.hud.reachTrackNearestPlayer) {
            lastReach = (double)0.0F;
            lastHitEntity = null;
         }

      } else {
         reset();
      }
   }

   public static void onAttackEntity(class_310 client, class_1297 target) {
      if (client.field_1724 != null && target != null) {
         class_243 cameraPos = client.field_1724.method_5836(1.0F);
         double distance = cameraPos.method_1022(entityPos(target));
         if (target instanceof class_1309) {
            class_1309 living = (class_1309)target;
            distance -= (double)(living.method_17681() / 2.0F);
         }

         lastReach = round2(distance);
         displayTicks = Math.max(10, configTicks());
         lastHitEntity = target;
         lastHitType = target instanceof class_1657 ? "P" : "E";
      }
   }

   public static void onBlockBreak(class_310 client, class_243 hitPos) {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.reachDisplay) {
         int x = config.hud.reachHudX;
         int y = config.hud.reachHudY;
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.reachHudScalePercent);
         boolean transparentText = CustomThemeRenderer.isTransparentTextMode(config) || !config.hud.reachShowBackground;
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         int decimals = Math.max(0, Math.min(3, config.hud.reachDecimals));
         boolean showDetails = displayTicks > 0 || config.hud.reachTrackNearestPlayer;
         double shownReach = showDetails ? lastReach : (double)0.0F;
         String number = String.format("%." + decimals + "f", shownReach);
         boolean validType = "P".equals(lastHitType) || "E".equals(lastHitType);
         String typeTag = showDetails && config.hud.reachShowTypeTag && validType ? " [" + lastHitType + "]" : "";
         String entityName = "";
         if (showDetails && lastHitEntity != null && config.hud.reachShowEntityName) {
            String name = lastHitEntity.method_5477().getString();
            if (name.length() > 12) {
               name = name.substring(0, 10) + "..";
            }

            entityName = " [" + name + "]";
         }

         String value = number + typeTag + entityName;
         int textColor = CustomThemeRenderer.getTextColor(config);
         int valueColor = showDetails && shownReach > (double)0.0F ? CustomThemeRenderer.applyHudOpacity(config, getReachColor(shownReach, maxReach)) : textColor;
         if (transparentText) {
            CustomThemeRenderer.renderBracketedText(context, client.field_1772, value, x, y, valueColor, config);
         } else {
            int width = CustomThemeRenderer.textWidth(client.field_1772, value, config) + 12;
            CustomThemeRenderer.renderThemedBox(context, x, y, width, 14, config);
            CustomThemeRenderer.drawHudLabelCentered(context, client.field_1772, value, x, y, width, 14, valueColor, config);
         }

         context.method_51448().popMatrix();
      }
   }

   public static double getLastReach() {
      return lastReach;
   }

   public static int getDisplayTicks() {
      return displayTicks;
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      int baseWidth = 72;
      if (client != null && client.field_1772 != null) {
         baseWidth = CustomThemeRenderer.textWidth(client.field_1772, "3.50 [P] [Player]", config) + 12;
      }

      return Math.round((float)baseWidth * CustomThemeRenderer.getHudScale(config, config.hud.reachHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round(14.0F * CustomThemeRenderer.getHudScale(config, config.hud.reachHudScalePercent));
   }

   private static int configTicks() {
      TurtModConfig cfg = TurtModClient.getConfig();
      return cfg == null ? 60 : Math.max(10, Math.min(200, cfg.hud.reachDisplayTicks));
   }

   private static int getReachColor(double reach, double maxReach) {
      if (reach >= maxReach * 0.95) {
         return -11751600;
      } else {
         return reach >= maxReach * 0.8 ? -13987 : -42406;
      }
   }

   private static double round2(double value) {
      return (double)Math.round(value * (double)100.0F) / (double)100.0F;
   }

   private static void reset() {
      lastReach = (double)0.0F;
      displayTicks = 0;
      lastHitEntity = null;
      lastHitType = "";
   }
}
