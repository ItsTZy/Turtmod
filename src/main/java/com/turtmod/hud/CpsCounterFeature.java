package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.CPSTracker;
import java.util.Objects;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class CpsCounterFeature {
   private static final int PADDING = 4;

   private CpsCounterFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.cpsCounterHud) {
         int x = config.hud.cpsCounterX;
         int y = config.hud.cpsCounterY;
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.cpsCounterScalePercent);
         int leftCps = CPSTracker.getLeftCPS();
         int rightCps = CPSTracker.getRightCPS();
         String cpsText;
         if (config.hud.cpsShowBoth) {
            cpsText = leftCps + " | " + rightCps;
         } else if (config.hud.cpsShowRightClick) {
            cpsText = String.valueOf(rightCps);
         } else {
            cpsText = String.valueOf(leftCps);
         }

         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         int textWidth = CustomThemeRenderer.textWidth(client.field_1772, cpsText, config);
         Objects.requireNonNull(client.field_1772);
         int textHeight = 9;
         int boxWidth = textWidth + 8;
         int boxHeight = textHeight + 8;
         if (config.hud.cpsShowBackground) {
            CustomThemeRenderer.renderThemedBox(context, x, y, boxWidth, boxHeight, config);
         }

         int textColor = CustomThemeRenderer.getTextColor(config);
         CustomThemeRenderer.drawHudLabel(context, client.field_1772, cpsText, x + 4, y + 4, textColor, config);
         context.method_51448().popMatrix();
      }
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      if (client != null && client.field_1772 != null) {
         int leftCps = CPSTracker.getLeftCPS();
         int rightCps = CPSTracker.getRightCPS();
         String cpsText;
         if (config.hud.cpsShowBoth) {
            cpsText = leftCps + " | " + rightCps;
         } else if (config.hud.cpsShowRightClick) {
            cpsText = String.valueOf(rightCps);
         } else {
            cpsText = String.valueOf(leftCps);
         }

         int textWidth = CustomThemeRenderer.textWidth(client.field_1772, cpsText, config);
         return textWidth + 8 + 8;
      } else {
         return 40;
      }
   }

   public static int getScaledHeight(TurtModConfig config) {
      return 20;
   }
}
