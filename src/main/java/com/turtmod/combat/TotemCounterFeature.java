package com.turtmod.combat;

import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CustomThemeRenderer;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class TotemCounterFeature {
   private TotemCounterFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.combat.totemCounterHud) {
         int count = TotemCounterFeature.countTotems(client.field_1724);
         String label = "";
         String countText = String.valueOf(count);
         int countColor = config.combat.totemColorByCount ? colorForCount(count) : CustomThemeRenderer.getTextColor(config);
         int x = 10;
         int y = 10;
         int textWidth = countText.isEmpty() ? CustomThemeRenderer.textWidth(client.field_1772, "99", config) : Math.max(CustomThemeRenderer.textWidth(client.field_1772, label, config), CustomThemeRenderer.textWidth(client.field_1772, countText, config));
          context.method_51448().pushMatrix();
         CustomThemeRenderer.drawHudLabel(context, client.field_1772, countText, x + 28, y + 13, countColor, config);
         context.method_51448().popMatrix();
      }
   }

   public static int countTotems(class_1657 player) {
      int count = 0;
      for(class_1799 stack : player.method_31548().method_67533()) {
         if (stack.method_31574(class_1802.field_8288)) {
            count += stack.method_7947();
         }
      }
      class_1799 offhand = player.method_6079();
      if (offhand.method_31574(class_1802.field_8288)) {
         count += offhand.method_7947();
      }
      return count;
   }

   public static int colorForCount(int count) {
      if (count <= 0) {
         return 0xFFAAAAAA;
      } else if (count <= 2) {
         return 0xFFFF5555;
      } else if (count <= 4) {
         return 0xFFFFAA00;
      } else if (count <= 6) {
         return 0xFFFFFF55;
      } else if (count <= 8) {
         return 0xFF55AA55;
      } else {
         return 0xFF55FF55;
      }
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      String label = "Totems";
      int textWidth = 24;
      if (client != null && client.field_1772 != null) {
         textWidth = label.isEmpty() ? CustomThemeRenderer.textWidth(client.field_1772, "99", config) : Math.max(CustomThemeRenderer.textWidth(client.field_1772, label, config), CustomThemeRenderer.textWidth(client.field_1772, "99", config));
      }

      int baseWidth = 26 + textWidth + 8;
      return Math.round((float)baseWidth * CustomThemeRenderer.getHudScale(config, config.hud.totemHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round(24.0F * CustomThemeRenderer.getHudScale(config, config.hud.totemHudScalePercent));
   }
}
