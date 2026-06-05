package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * Coordinates HUD — a polished, movable position panel modelled on the look of
 * coordinates-display (repos/coordinates-display-dev). Reuses the same data the
 * CleanF3 feature reads and draws through {@link CustomThemeRenderer} so it matches
 * the rest of turtmod's HUD theme. Three layout presets: DEFAULT (multi-line),
 * COMPACT (XYZ only) and LINE (single inline row).
 */
public final class CoordinatesHudFeature {
   // Last rendered panel size (already scaled) so the HUD editor box can track it.
   private static int lastWidth = 80;
   private static int lastHeight = 20;

   private CoordinatesHudFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (!config.hud.coordinatesHud || client.field_1724 == null || client.field_1687 == null) {
         return;
      }
      if (client.field_1690 != null && client.field_1690.field_1842) {
         return; // hideGui
      }

      double px = client.field_1724.method_23317();
      double py = client.field_1724.method_23318();
      double pz = client.field_1724.method_23321();
      class_2338 block = client.field_1724.method_24515();
      class_1923 chunk = new class_1923(block);

      List<String[]> lines = new ArrayList();
      TurtModConfig.CoordinatesHudMode mode = config.hud.coordinatesHudMode;

      if (mode == TurtModConfig.CoordinatesHudMode.LINE) {
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("%.0f, %.0f, %.0f", px, py, pz));
         if (config.hud.coordsShowDirection) {
            sb.append("  ").append(facing(client));
         }
         lines.add(new String[]{"", sb.toString()});
      } else if (mode == TurtModConfig.CoordinatesHudMode.COMPACT) {
         lines.add(new String[]{"XYZ ", String.format("%.1f / %.1f / %.1f", px, py, pz)});
      } else {
         lines.add(new String[]{"XYZ ", String.format("%.2f / %.2f / %.2f", px, py, pz)});
         if (config.hud.coordsShowChunk) {
            lines.add(new String[]{"Chunk ", chunk.field_9181 + ", " + chunk.field_9180
               + "   Local " + (block.method_10263() & 15) + " " + (block.method_10264() & 15) + " " + (block.method_10260() & 15)});
         }
         if (config.hud.coordsShowDirection) {
            lines.add(new String[]{"Facing ", facing(client) + String.format("  (%.1f / %.1f)", client.field_1724.method_36454(), client.field_1724.method_36455())});
         }
         if (config.hud.coordsShowBiome || config.hud.coordsShowDimension) {
            StringBuilder sb = new StringBuilder();
            if (config.hud.coordsShowDimension) {
               sb.append(dimension(client));
            }
            if (config.hud.coordsShowBiome) {
               if (sb.length() > 0) sb.append("  |  ");
               sb.append(biome(client, block));
            }
            lines.add(new String[]{config.hud.coordsShowDimension ? "Dim " : "Biome ", sb.toString()});
         }
         if (config.hud.coordsShowDay) {
            lines.add(new String[]{"Day ", Long.toString(client.field_1687.method_8532() / 24000L)});
         }
      }

      if (lines.isEmpty()) {
         return;
      }

      int x = config.hud.coordinatesHudX;
      int y = config.hud.coordinatesHudY;
      int pad = 5;
      int panelW = 0;
      for (String[] pair : lines) {
         panelW = Math.max(panelW, CustomThemeRenderer.textWidth(client.field_1772, pair[0], config) + CustomThemeRenderer.textWidth(client.field_1772, pair[1], config));
      }
      panelW += pad * 2;
      int panelH = lines.size() * 10 + 6;

      float scale = CustomThemeRenderer.getHudScale(config, config.hud.coordinatesHudScalePercent);
      lastWidth = Math.round(panelW * scale);
      lastHeight = Math.round(panelH * scale);

      int muted = CustomThemeRenderer.getMutedTextColor(config);
      int main = CustomThemeRenderer.getTextColor(config);

      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float)(-x), (float)(-y));
      if (config.hud.coordsShowBackground) {
         CustomThemeRenderer.renderThemedBox(context, x, y, panelW, panelH, config);
      }
      int ty = y + 4;
      for (int i = 0; i < lines.size(); i++) {
         String[] pair = lines.get(i);
         int lx = x + pad;
         if (!pair[0].isEmpty()) {
            CustomThemeRenderer.drawHudLabel(context, client.field_1772, pair[0], lx, ty + i * 10, muted, config);
            lx += CustomThemeRenderer.textWidth(client.field_1772, pair[0], config);
         }
         CustomThemeRenderer.drawHudLabel(context, client.field_1772, pair[1], lx, ty + i * 10, main, config);
      }
      context.method_51448().popMatrix();
   }

   private static String facing(class_310 client) {
      return client.field_1724.method_5735().method_15434().toUpperCase();
   }

   private static String dimension(class_310 client) {
      return client.field_1687.method_27983().method_29177().method_12832();
   }

   private static String biome(class_310 client, class_2338 pos) {
      Optional<String> b = client.field_1687.method_23753(pos).method_40230().map((key) -> key.method_29177().method_12832());
      return b.orElse("unknown");
   }

   public static int getScaledWidth(TurtModConfig config) {
      return lastWidth;
   }

   public static int getScaledHeight(TurtModConfig config) {
      return lastHeight;
   }
}
