package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_640;

/**
 * FPS and Ping are TWO SEPARATE, independently-placeable HUDs (Lunar-style): the FPS HUD draws "Fps N" at its
 * own position, the Ping HUD draws "Ping Nms" at its own position, each with its own enable + scale, and each
 * is its own draggable box in the HUD editor.
 */
public final class FpsPingOverlayFeature {
   private FpsPingOverlayFeature() {
   }

   /** FPS HUD. */
   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.minimalFpsPingOverlay) {
         int x = HudEditorFeature.clampToScreenX(client, config.hud.minimalOverlayX, getScaledWidth(config));
         int y = HudEditorFeature.clampToScreenY(client, config.hud.minimalOverlayY, getScaledHeight(config));
         drawAt(context, client, config, x, y, true, config.hud.overlayScalePercent);
      }
   }

   /** Ping HUD (separate). */
   public static void renderPing(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.pingHudEnabled) {
         int x = HudEditorFeature.clampToScreenX(client, config.hud.pingHudX, getPingScaledWidth(config));
         int y = HudEditorFeature.clampToScreenY(client, config.hud.pingHudY, getPingScaledHeight(config));
         drawAt(context, client, config, x, y, false, config.hud.pingHudScalePercent);
      }
   }

   /** Live-settings preview (FPS). */
   public static void renderPreview(class_332 context, class_310 client, TurtModConfig config, int x, int y) {
      drawAt(context, client, config, x, y, true, config.hud.overlayScalePercent);
   }

   private static int currentPing(class_310 client) {
      if (client.method_1562() != null && client.field_1724 != null) {
         class_640 entry = client.method_1562().method_2871(client.field_1724.method_5667());
         if (entry != null) {
            return entry.method_2959();
         }
      }
      return -1;
   }

   private static void drawAt(class_332 context, class_310 client, TurtModConfig config, int x, int y, boolean isFps, int scalePercent) {
      int textColor = CustomThemeRenderer.getTextColor(config);
      String text;
      int color;
      if (isFps) {
         int fps = client.method_47599();
         text = "Fps " + fps;
         color = config.hud.fpsColorCoded ? fpsColor(fps) : textColor;
      } else {
         int ping = currentPing(client);
         text = ping >= 0 ? "Ping " + ping + "ms" : "Ping --";
         color = textColor;
      }
      float scale = CustomThemeRenderer.getHudScale(config, scalePercent);
      boolean transparentText = CustomThemeRenderer.isTransparentTextMode(config) || !config.hud.fpsPingShowBackground;
      context.method_51448().pushMatrix();
      context.method_51448().translate((float) x, (float) y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float) (-x), (float) (-y));
      if (transparentText) {
         CustomThemeRenderer.renderBracketedText(context, client.field_1772, text, x, y, color, config);
      } else {
         int w = getBaseWidth(client, config, isFps);
         CustomThemeRenderer.renderThemedBox(context, x, y, w, getBaseHeight(), config);
         int tw = CustomThemeRenderer.textWidth(client.field_1772, text, config);
         int tx = x + Math.max(0, (w - tw) / 2);
         int ty = CustomThemeRenderer.centeredTextY(y, getBaseHeight());
         CustomThemeRenderer.drawHudLabel(context, client.field_1772, text, tx, ty, color, config);
      }
      context.method_51448().popMatrix();
   }

   // ── FPS HUD sizing ──
   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      return Math.round((float) getBaseWidth(client, config, true) * CustomThemeRenderer.getHudScale(config, config.hud.overlayScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round((float) getBaseHeight() * CustomThemeRenderer.getHudScale(config, config.hud.overlayScalePercent));
   }

   // ── Ping HUD sizing ──
   public static int getPingScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      return Math.round((float) getBaseWidth(client, config, false) * CustomThemeRenderer.getHudScale(config, config.hud.pingHudScalePercent));
   }

   public static int getPingScaledHeight(TurtModConfig config) {
      return Math.round((float) getBaseHeight() * CustomThemeRenderer.getHudScale(config, config.hud.pingHudScalePercent));
   }

   private static int getBaseWidth(class_310 client, TurtModConfig config, boolean isFps) {
      if (client != null && client.field_1772 != null) {
         String compactStr = isFps ? "Fps 999" : "Ping 999ms";
         int compact = CustomThemeRenderer.textWidth(client.field_1772, compactStr, config);
         int transparent = CustomThemeRenderer.getBracketedTextWidth(client.field_1772, compactStr, config);
         return Math.max(compact, transparent) + 12;
      }
      return isFps ? 70 : 96;
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
