package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_640;

public final class CleanF3Feature {
   private CleanF3Feature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (config.hud.cleanF3Mode && !client.field_1690.field_1842) {
         int x = config.hud.cleanF3X;
         int y = config.hud.cleanF3Y;
         List<String> lines = new ArrayList();
         if (config.hud.cleanF3ShowFpsPing) {
            int fps = client.method_47599();
            int ping = -1;
            if (client.method_1562() != null) {
               class_640 entry = client.method_1562().method_2871(client.field_1724.method_5667());
               if (entry != null) {
                  ping = entry.method_2959();
               }
            }

            lines.add("FPS " + fps + " | Ping " + (ping >= 0 ? ping + "ms" : "--"));
         }

         if (config.hud.cleanF3ShowPosition && client.field_1724 != null) {
            class_2338 pos = client.field_1724.method_24515();
            class_1923 chunkPos = new class_1923(pos);
            lines.add(String.format("XYZ %.3f / %.3f / %.3f", client.field_1724.method_23317(), client.field_1724.method_23318(), client.field_1724.method_23321()));
            int var10001 = pos.method_10263();
            lines.add("Block " + var10001 + " " + pos.method_10264() + " " + pos.method_10260());
            if (config.hud.cleanF3ShowChunk) {
               var10001 = chunkPos.field_9181;
               lines.add("Chunk " + var10001 + " " + chunkPos.field_9180 + " | Local " + (pos.method_10263() & 15) + " " + (pos.method_10264() & 15) + " " + (pos.method_10260() & 15));
            }

            if (config.hud.cleanF3ShowLight && client.field_1687 != null) {
               int blockLight = client.field_1687.method_8314(class_1944.field_9282, pos);
               int skyLight = client.field_1687.method_8314(class_1944.field_9284, pos);
               lines.add("Light " + blockLight + " block, " + skyLight + " sky");
            }
         }

         if (config.hud.cleanF3ShowFacing && client.field_1724 != null) {
            lines.add(String.format("Facing %s | Yaw %.1f | Pitch %.1f", client.field_1724.method_5735().method_15434().toUpperCase(), client.field_1724.method_36454(), client.field_1724.method_36455()));
         }

         if (config.hud.cleanF3ShowBiome && client.field_1687 != null) {
            class_2338 pos = client.field_1724.method_24515();
            Optional var28 = client.field_1687.method_23753(pos).method_40230().map((key) -> key.method_29177().toString());
            lines.add("Biome " + (String)var28.orElse("unknown"));
         }

         if (config.hud.cleanF3ShowLookingAt && client.field_1765 != null) {
            class_239 hitResult = client.field_1765;
            if (hitResult instanceof class_3965) {
               class_3965 blockHit = (class_3965)hitResult;
               class_2338 lookedPos = blockHit.method_17777();
               int var29 = lookedPos.method_10263();
               lines.add("Looking at block " + var29 + " " + lookedPos.method_10264() + " " + lookedPos.method_10260());
            } else if (hitResult instanceof class_3966) {
               class_3966 entityHit = (class_3966)hitResult;
               lines.add("Looking at entity " + entityHit.method_17782().method_5477().getString());
            }
         }

         if (!lines.isEmpty()) {
            int panelW = 0;

            for(String s : lines) {
               panelW = Math.max(panelW, CustomThemeRenderer.textWidth(client.field_1772, s, config));
            }

            panelW += 10;
            int panelH = lines.size() * 10 + 8;
            float scale = CustomThemeRenderer.getHudScale(config, config.hud.cleanF3ScalePercent);
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)x, (float)y);
            context.method_51448().scale(scale, scale);
            context.method_51448().translate((float)(-x), (float)(-y));
            if (config.hud.cleanF3ShowBackground) {
               CustomThemeRenderer.renderThemedBox(context, x, y, panelW, panelH, config);
            }

            int ty = y + 4;

            for(int i = 0; i < lines.size(); ++i) {
               int color = i == 0 ? CustomThemeRenderer.getTextColor(config) : CustomThemeRenderer.getMutedTextColor(config);
               CustomThemeRenderer.drawHudLabel(context, client.field_1772, (String)lines.get(i), x + 5, ty + i * 10, color, config);
            }

            context.method_51448().popMatrix();
         }
      }
   }
}
