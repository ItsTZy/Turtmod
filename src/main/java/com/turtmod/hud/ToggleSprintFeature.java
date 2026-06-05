package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class ToggleSprintFeature {
   private static boolean toggleSprintActive = false;
   private static boolean lastSprintState = false;
   private static long lastToggleTime = 0L;

   private ToggleSprintFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.toggleSprintHud) {
         boolean isSprinting = client.field_1724.method_5624();
         long currentTime = System.currentTimeMillis();
         if (isSprinting && !lastSprintState) {
            if (currentTime - lastToggleTime < 300L) {
               toggleSprintActive = true;
            }

            lastToggleTime = currentTime;
         }

         boolean movingForward = client.field_1724.field_6250 > 0.8F;
         if (!movingForward && isSprinting) {
            toggleSprintActive = true;
         }

         if (!isSprinting && lastSprintState) {
            toggleSprintActive = false;
         }

         lastSprintState = isSprinting;
      } else {
         toggleSprintActive = false;
         lastSprintState = false;
      }
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.toggleSprintHud) {
         String status;
         int statusColor;
         if (toggleSprintActive) {
            status = "sprint toggle";
            statusColor = CustomThemeRenderer.getAccentColor(config);
         } else if (client.field_1724.method_5624()) {
            status = "sprint active";
            statusColor = CustomThemeRenderer.applyHudOpacity(config, -733073);
         } else {
            status = "sprint off";
            statusColor = CustomThemeRenderer.applyHudOpacity(config, -1739917);
         }

         int x = config.hud.toggleSprintHudX;
         int y = config.hud.toggleSprintHudY;
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.toggleSprintHudScalePercent);
         boolean transparentText = CustomThemeRenderer.isTransparentTextMode(config);
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         if (transparentText) {
            CustomThemeRenderer.renderBracketedText(context, client.field_1772, status, x, y, statusColor, config);
         } else {
            int width = CustomThemeRenderer.textWidth(client.field_1772, status.toUpperCase(), config) + 12;
            CustomThemeRenderer.renderThemedBox(context, x, y, width, 14, config);
            CustomThemeRenderer.drawHudLabel(context, client.field_1772, status.toUpperCase(), x + 6, y + 3, statusColor, config);
         }

         context.method_51448().popMatrix();
      }
   }

   public static boolean isToggleSprintActive() {
      return toggleSprintActive;
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      int baseWidth = 68;
      if (client != null && client.field_1772 != null) {
         int boxed = CustomThemeRenderer.textWidth(client.field_1772, "SPRINT TOGGLE", config) + 12;
         int transparent = CustomThemeRenderer.getBracketedTextWidth(client.field_1772, "sprint toggle", config);
         baseWidth = Math.max(boxed, transparent);
      }

      return Math.round((float)baseWidth * CustomThemeRenderer.getHudScale(config, config.hud.toggleSprintHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round(14.0F * CustomThemeRenderer.getHudScale(config, config.hud.toggleSprintHudScalePercent));
   }
}
