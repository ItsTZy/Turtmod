package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_640;

/**
 * Shared Clean F3 layout + rendering (BetterF3-style two-tone "name: value" lines). Centralised so
 * the in-game renderer ({@code DebugHudMixin}) and the HUD editor bounding box ({@code
 * HudEditorFeature}) compute the exact same size — previously the editor used a fixed 160x52 box
 * that no longer matched the (now variable) number of lines.
 */
public final class CleanF3Feature {
   // BetterF3 uses 9px lines, a 2px top/left margin, and a per-line translucent background.
   public static final int LINE_H = 9;
   private static final int TOP_PAD = 2;
   private static final int LEFT_PAD = 2;
   private static final int BF3_BG = 0x6F505050; // BetterF3's default background colour

   private CleanF3Feature() {
   }

   // Rolling FPS min/max for the optional extremes readout.
   private static int minFps = Integer.MAX_VALUE;
   private static int maxFps = 0;
   private static long fpsResetAt = 0L;

   /** Builds the ordered, enabled lines as {label, value} pairs. */
   public static List<String[]> buildLines(class_310 client, TurtModConfig config) {
      List<String[]> out = new ArrayList<>();
      if (client == null || client.field_1724 == null) {
         return out;
      }
      int fps = client.method_47599();
      long now = System.currentTimeMillis();
      if (now - fpsResetAt > 3000L) {
         fpsResetAt = now;
         minFps = fps;
         maxFps = fps;
      } else {
         minFps = Math.min(minFps, fps);
         maxFps = Math.max(maxFps, fps);
      }

      int ping = -1;
      if (client.method_1562() != null) {
         class_640 entry = client.method_1562().method_2871(client.field_1724.method_5667());
         if (entry != null) {
            ping = entry.method_2959();
         }
      }

      class_2338 pos = client.field_1724.method_24515();
      class_1923 chunkPos = new class_1923(pos);
      class_243 p = new class_243(client.field_1724.method_23317(), client.field_1724.method_23318(), client.field_1724.method_23321());

      LinkedHashMap<String, String[]> avail = new LinkedHashMap<>();
      if (config.hud.cleanF3ShowFpsPing) {
         String fpsVal = config.hud.cleanF3ShowFpsExtremes
            ? fps + " (" + minFps + "-" + maxFps + ") | Ping: " + (ping >= 0 ? ping + "ms" : "--")
            : fps + " | Ping: " + (ping >= 0 ? ping + "ms" : "--");
         avail.put("fps", new String[]{"FPS", fpsVal});
      }
      if (config.hud.cleanF3ShowPosition) {
         avail.put("pos", new String[]{"XYZ", String.format("%.3f / %.3f / %.3f", p.field_1352, p.field_1351, p.field_1350)});
         avail.put("block", new String[]{"Block", String.format("%d %d %d", pos.method_10263(), pos.method_10264(), pos.method_10260())});
      }
      if (config.hud.cleanF3ShowChunk) {
         avail.put("chunk", new String[]{"Chunk", String.format("%d %d | Local %d %d %d", chunkPos.field_9181, chunkPos.field_9180, pos.method_10263() & 15, pos.method_10264() & 15, pos.method_10260() & 15)});
      }
      if (config.hud.cleanF3ShowLight && client.field_1687 != null) {
         int blockLight = client.field_1687.method_8314(class_1944.field_9282, pos);
         int skyLight = client.field_1687.method_8314(class_1944.field_9284, pos);
         avail.put("light", new String[]{"Light", String.format("%d block, %d sky", blockLight, skyLight)});
      }
      if (config.hud.cleanF3ShowFacing) {
         avail.put("facing", new String[]{"Facing", String.format("%s (%.1f / %.1f)", client.field_1724.method_5735().method_15434().toUpperCase(), client.field_1724.method_36454(), client.field_1724.method_36455())});
      }
      if (config.hud.cleanF3ShowSpeed) {
         double dx = client.field_1724.method_23317() - client.field_1724.field_6014;
         double dz = client.field_1724.method_23321() - client.field_1724.field_5969;
         double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
         avail.put("speed", new String[]{"Speed", String.format("%.2f m/s", speed)});
      }
      if (config.hud.cleanF3ShowBiome && client.field_1687 != null) {
         Optional<String> biome = client.field_1687.method_23753(pos).method_40230().map((key) -> key.method_29177().toString());
         avail.put("biome", new String[]{"Biome", biome.orElse("unknown")});
      }
      if (config.hud.cleanF3ShowDimension && client.field_1687 != null) {
         avail.put("dim", new String[]{"Dimension", client.field_1687.method_27983().method_29177().toString()});
      }
      if (config.hud.cleanF3ShowDayTime && client.field_1687 != null) {
         long dayTime = client.field_1687.method_8532() % 24000L;
         long day = client.field_1687.method_8532() / 24000L;
         int hour = (int)((dayTime / 1000L + 6L) % 24L);
         int minute = (int)((dayTime % 1000L) * 60L / 1000L);
         avail.put("daytime", new String[]{"Time", String.format("Day %d | %02d:%02d", day, hour, minute)});
      }
      if (config.hud.cleanF3ShowHeldItem) {
         net.minecraft.class_1799 held = client.field_1724.method_6047();
         if (!held.method_7960()) {
            avail.put("held", new String[]{"Held", held.method_7964().getString() + (held.method_7947() > 1 ? " x" + held.method_7947() : "")});
         }
      }
      if (config.hud.cleanF3ShowMemory) {
         long max = Runtime.getRuntime().maxMemory();
         long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
         avail.put("mem", new String[]{"Mem", String.format("%d%% %dMB/%dMB", used * 100L / max, used / 1048576L, max / 1048576L)});
      }
      if (config.hud.cleanF3ShowLookingAt) {
         class_239 hit = client.field_1765;
         if (hit instanceof class_3965 blockHit) {
            class_2338 lp = blockHit.method_17777();
            avail.put("look", new String[]{"Looking at", String.format("block %d %d %d", lp.method_10263(), lp.method_10264(), lp.method_10260())});
         } else if (hit instanceof class_3966 entityHit) {
            avail.put("look", new String[]{"Looking at", "entity " + entityHit.method_17782().method_5477().getString()});
         }
      }

      List<String> order = config.hud.cleanF3Order != null && !config.hud.cleanF3Order.isEmpty()
         ? config.hud.cleanF3Order : TurtModConfig.Hud.defaultCleanF3Order();
      for (String key : order) {
         String[] line = avail.remove(key);
         if (line != null) {
            out.add(line);
         }
      }
      out.addAll(avail.values());
      return out;
   }

   /** Unscaled pixel width of the widest "label: value" line (incl. the per-line bg padding). */
   public static int boxWidth(class_310 client, List<String[]> lines) {
      if (client == null || lines.isEmpty()) {
         return 0;
      }
      class_327 font = client.field_1772;
      int sep = font.method_1727(": ");
      int w = 0;
      for (String[] line : lines) {
         w = Math.max(w, font.method_1727(line[0]) + sep + font.method_1727(line[1]));
      }
      return w + LEFT_PAD + 2;
   }

   /** Unscaled pixel height of the panel. */
   public static int boxHeight(List<String[]> lines) {
      return lines.isEmpty() ? 0 : lines.size() * LINE_H + TOP_PAD + 1;
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      List<String[]> lines = buildLines(client, config);
      if (lines.isEmpty()) {
         return;
      }
      class_327 font = client.field_1772;
      int x = config.hud.cleanF3X;
      int y = config.hud.cleanF3Y;
      int sep = font.method_1727(": ");
      float scale = CustomThemeRenderer.getHudScale(config, config.hud.cleanF3ScalePercent);
      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float)(-x), (float)(-y));

      int textX = x + LEFT_PAD;
      for (int i = 0; i < lines.size(); i++) {
         String[] line = lines.get(i);
         int ly = y + TOP_PAD + i * LINE_H;
         int labelW = font.method_1727(line[0]) + sep;
         int lineW = labelW + font.method_1727(line[1]);
         // BetterF3-style: each line gets its own translucent rectangle sized to its text.
         if (config.hud.cleanF3ShowBackground) {
            context.method_25294(textX - 1, ly - 1, textX + lineW + 1, ly + LINE_H - 1, BF3_BG);
         }
         context.method_27535(font, class_2561.method_43470(line[0] + ": "), textX, ly, config.hud.cleanF3LabelColor);
         context.method_27535(font, class_2561.method_43470(line[1]), textX + labelW, ly, config.hud.cleanF3ValueColor);
      }
      context.method_51448().popMatrix();
   }
}
