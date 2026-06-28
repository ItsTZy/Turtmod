package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_640;

public final class FpsPingOverlayFeature {
   private FpsPingOverlayFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.minimalFpsPingOverlay) {
         int fps = client.method_47599();
         int ping = -1;
         if (client.method_1562() != null) {
            class_640 entry = client.method_1562().method_2871(client.field_1724.method_5667());
            if (entry != null) {
               ping = entry.method_2959();
            }
         }

         int x = config.hud.minimalOverlayX;
         int y = config.hud.minimalOverlayY;
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.overlayScalePercent);
         int scaledWidth = client.method_22683().method_4486();
         int scaledHeight = client.method_22683().method_4502();
         int hudWidth = Math.round((float)getBaseWidth(client, config) * scale);
         int hudHeight = Math.round(40.0F * scale);
         x = Math.max(0, Math.min(x, scaledWidth - hudWidth));
         y = Math.max(0, Math.min(y, scaledHeight - hudHeight));
         boolean transparentText = CustomThemeRenderer.isTransparentTextMode(config);
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
          context.method_51448().translate((float)(-x), (float)(-y));
          int textColor = CustomThemeRenderer.getTextColor(config);
          int fpsColor = config.hud.fpsColorCoded ? fpsColor(fps) : textColor;
          String fpsText = "Fps " + fps;
          String pingText = ping >= 0 ? "Ping " + ping + "ms" : "Ping --";
          int w = getBaseWidth(client, config);
          if (transparentText) {
             int cursor = CustomThemeRenderer.renderBracketedText(context, client.field_1772, fpsText, x, y, fpsColor, config);
             cursor = CustomThemeRenderer.renderBracketedText(context, client.field_1772, pingText, cursor + 4, y, textColor, config);
          } else {
             CustomThemeRenderer.renderThemedBox(context, x, y, w, getBaseHeight(), config);
             String pingPart = ping >= 0 ? "Ping " + ping + "ms" : "Ping --";
             int fpsW = CustomThemeRenderer.textWidth(client.field_1772, fpsText, config);
             CustomThemeRenderer.drawHudLabel(context, client.field_1772, fpsText, x + 6, y + 3, fpsColor, config);
             CustomThemeRenderer.drawHudLabel(context, client.field_1772, "  " + pingPart, x + 6 + fpsW, y + 3, textColor, config);
          }

         context.method_51448().popMatrix();
      }
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      return Math.round((float)getBaseWidth(client, config) * CustomThemeRenderer.getHudScale(config, config.hud.overlayScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round((float)getBaseHeight() * CustomThemeRenderer.getHudScale(config, config.hud.overlayScalePercent));
   }

   private static int getBaseWidth(class_310 client, TurtModConfig config) {
      if (client != null && client.field_1772 != null) {
         int compact = CustomThemeRenderer.textWidth(client.field_1772, "Fps 999  Ping 999ms", config);
         int transparent = CustomThemeRenderer.getBracketedTextWidth(client.field_1772, "Fps 999", config) + 4
            + CustomThemeRenderer.getBracketedTextWidth(client.field_1772, "Ping 999ms", config);
         return Math.max(compact, transparent) + 12;
      } else {
         return 160;
      }
   }

   private static int getBaseHeight() {
      return 14;
   }

   /** Green ≥120, lime ≥60, yellow ≥30, red below — a quick visual FPS-health cue. */
   private static int fpsColor(int fps) {
      if (fps >= 120) return 0xFF55E08A;
      if (fps >= 60)  return 0xFF8CE05B;
      if (fps >= 30)  return 0xFFE0C24E;
      return 0xFFE0556B;
   }
}
