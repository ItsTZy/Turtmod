package com.turtmod.combat;

import java.util.List;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CustomThemeRenderer;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_7923;

/**
 * Totem HUD counter: a totem icon + count drawn near the hotbar, plus the shared totem-count helpers
 * used by the coloured XP bar. Ported to parity with uku3lig's TotemCounter (repos/totemcounter-1.21.11):
 * counts the inventory totems (or the pop count when {@code totemShowPopCounter}), colours by count,
 * and — like the original — recognises modded totems (voidtotem) in addition to the vanilla one.
 *
 * Layout: [totem icon 16px][gap][count]; the ICON style shows just the count, TOTEM/TOTEMS prefix a
 * word. Drawn at {@code totemHudX/Y} scaled by {@code totemHudScalePercent}. The HUD editor sizes its
 * box from {@link #getScaledWidth}/{@link #getScaledHeight}, which mirror this layout.
 */
public final class TotemCounterFeature {
   private static final int ICON = 16;
   private static final int GAP = 4;
   private static final int PAD = 4;
   private static final class_1799 TOTEM_STACK = new class_1799(class_1802.field_8288);
   // Modded totems recognised in addition to the vanilla totem of undying (matches the original mod).
   private static final List<String> CUSTOM_TOTEMS = List.of("voidtotem:totem_of_void_undying");

   private TotemCounterFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 == null || !config.misc.enabled || !config.combat.totemCounterHud) {
         return;
      }
      int count = config.combat.totemShowPopCounter
         ? TotemPopTracker.get(client.field_1724.method_5667())
         : countTotems(client.field_1724);
      if (count <= 0) {
         return;
      }

      class_327 font = client.field_1772;
      String text = labelText(config, count);
      int color = config.combat.totemColorByCount
         ? (config.combat.totemShowPopCounter ? TotemPopTracker.popColor(count) : colorForCount(count))
         : CustomThemeRenderer.getTextColor(config);

      float scale = CustomThemeRenderer.getHudScale(config, config.hud.totemHudScalePercent);
      // Clamp on-screen so a stale/large saved position can't hide it off an edge (the "HUD doesn't
      // show" bug at high GUI scale).
      int sw = client.method_22683().method_4486();
      int sh = client.method_22683().method_4502();
      int cw = getScaledWidth(config);
      int ch = getScaledHeight(config);
      int x = Math.max(0, Math.min(config.hud.totemHudX, sw - cw));
      int y = Math.max(0, Math.min(config.hud.totemHudY, sh - ch));

      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float)(-x), (float)(-y));

      context.method_51427(TOTEM_STACK, x, y);
      int textY = y + (ICON - font.field_2000) / 2;
      CustomThemeRenderer.drawHudLabel(context, font, text, x + ICON + GAP, textY, color, config);

      context.method_51448().popMatrix();
   }

   /** The text drawn after the totem icon for the current label style. */
   private static String labelText(TurtModConfig config, int count) {
      String countText = config.combat.totemShowPopCounter ? "-" + count : String.valueOf(count);
      return switch (config.combat.totemLabelStyle) {
         case TOTEM -> "Totem " + countText;
         case TOTEMS -> "Totems " + countText;
         default -> countText; // ICON = count only
      };
   }

   public static boolean isTotem(class_1799 stack) {
      if (stack.method_31574(class_1802.field_8288)) {
         return true;
      }
      class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
      return id != null && CUSTOM_TOTEMS.contains(id.toString());
   }

   public static int countTotems(class_1657 player) {
      int count = 0;
      for(class_1799 stack : player.method_31548().method_67533()) {
         if (isTotem(stack)) {
            count += stack.method_7947();
         }
      }
      class_1799 offhand = player.method_6079();
      if (isTotem(offhand)) {
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

   /** Unscaled width of the widest content this element can draw at the current style (icon + text). */
   private static int contentWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      String sample = switch (config.combat.totemLabelStyle) {
         case TOTEM -> "Totem 99";
         case TOTEMS -> "Totems 99";
         default -> "99";
      };
      int textWidth = client != null && client.field_1772 != null
         ? CustomThemeRenderer.textWidth(client.field_1772, sample, config)
         : sample.length() * 6;
      return ICON + GAP + textWidth + PAD;
   }

   public static int getScaledWidth(TurtModConfig config) {
      return Math.round((float)contentWidth(config) * CustomThemeRenderer.getHudScale(config, config.hud.totemHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round((float)(ICON + PAD) * CustomThemeRenderer.getHudScale(config, config.hud.totemHudScalePercent));
   }
}
