package com.turtmod.combat;

import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CustomThemeRenderer;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class HealthNumberFeature {
   private HealthNumberFeature() {
   }

   /** Live top-left X of the widget (screen-centre + configured offset). Used by the HUD editor. */
   public static int getX(class_310 client, TurtModConfig config) {
      return client.method_22683().method_4486() / 2 + config.combat.healthOffsetX;
   }

   /** Live top-left Y of the widget. healthOffsetY is normally negative (anchored to the bottom). */
   public static int getY(class_310 client, TurtModConfig config) {
      return client.method_22683().method_4502() + config.combat.healthOffsetY;
   }

   /** Convert an absolute editor position back into the stored centre/bottom-relative offsets. */
   public static void setPosition(class_310 client, TurtModConfig config, int x, int y) {
      config.combat.healthOffsetX = x - client.method_22683().method_4486() / 2;
      config.combat.healthOffsetY = y - client.method_22683().method_4502();
   }

   public static int getScaledWidth(TurtModConfig config) {
      float scale = com.turtmod.hud.CustomThemeRenderer.getHudScale(config, config.combat.healthScalePercent);
      class_310 client = class_310.method_1551();
      int base = 80;
      if (client != null && client.field_1724 != null && client.field_1772 != null) {
         float hp = client.field_1724.method_6032();
         float maxHp = client.field_1724.method_6063();
         int hearts = (int)(hp / 2.0F) + ((hp % 2.0F >= 1.0F) ? 1 : 0);
         StringBuilder sb = new StringBuilder();
         for(int i = 0; i < hearts; ++i) {
            sb.append("❤");
         }
         int heartsW = CustomThemeRenderer.textWidth(client.field_1772, sb.toString(), config);
         int valueW = CustomThemeRenderer.textWidth(client.field_1772, String.format("%.1f / %.1f", hp, maxHp), config);
         base = Math.max(heartsW, valueW) + 12;
      }
      return Math.round((float)base * scale);
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round(28.0F * com.turtmod.hud.CustomThemeRenderer.getHudScale(config, config.combat.healthScalePercent));
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.combat.showExactHealthNumber) {
         float hp = client.field_1724.method_6032();
         float maxHp = client.field_1724.method_6063();
         float absorption = client.field_1724.method_6067();
         int x = client.method_22683().method_4486() / 2 + config.combat.healthOffsetX;
         int y = client.method_22683().method_4502() + config.combat.healthOffsetY;
         float scale = CustomThemeRenderer.getHudScale(config, config.combat.healthScalePercent);
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         StringBuilder heartsText = new StringBuilder();
         int fullHearts = (int)(hp / 2.0F);
         boolean halfHeart = hp % 2.0F >= 1.0F;

         for(int i = 0; i < fullHearts; ++i) {
            heartsText.append("❤");
         }

         if (halfHeart) {
            heartsText.append("\ud83d\udc94");
         }

         int absFull = (int)(absorption / 2.0F);
         boolean absHalf = absorption % 2.0F >= 1.0F;

         for(int i = 0; i < absFull; ++i) {
            heartsText.append("\ud83d\udc9b");
         }

         if (absHalf) {
            heartsText.append("\ud83d\udc9b");
         }

         String hearts = heartsText.toString();
         String value = String.format("%.1f / %.1f", hp, maxHp);
         int heartsWidth = CustomThemeRenderer.textWidth(client.field_1772, hearts, config);
         int valueWidth = CustomThemeRenderer.textWidth(client.field_1772, value, config);
         int width = Math.max(heartsWidth, valueWidth) + 12;
         CustomThemeRenderer.renderThemedBox(context, x, y, width, 28, config);
         CustomThemeRenderer.drawHudLabel(context, client.field_1772, hearts, x + 6, y + 5, -43691, config);
         CustomThemeRenderer.drawHudLabel(context, client.field_1772, value, x + 6, y + 17, CustomThemeRenderer.getTextColor(config), config);
         context.method_51448().popMatrix();
      }
   }
}
