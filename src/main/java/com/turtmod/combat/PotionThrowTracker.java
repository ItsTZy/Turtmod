package com.turtmod.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CustomThemeRenderer;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5250;

public final class PotionThrowTracker {
   private static final Map<UUID, Integer> POTS = new HashMap();

   private PotionThrowTracker() {
   }

   public static void increment(UUID id) {
      POTS.merge(id, 1, Integer::sum);
   }

   public static int get(UUID id) {
      return (Integer)POTS.getOrDefault(id, 0);
   }

   public static Map<UUID, Integer> getPots() {
      return POTS;
   }

   public static void reset() {
      POTS.clear();
   }

   public static void remove(UUID id) {
      POTS.remove(id);
   }

   public static class_2561 appendPots(class_1657 player, class_2561 original) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) {
         return original;
      }
      int pots = get(player.method_5667());
      if (pots <= 0) {
         return original;
      }
      boolean showSeparator = config.combat.potionThrowSeparator;
      boolean showColors = config.combat.potionThrowCounterColors;
      class_5250 result = class_2561.method_43473().method_10852(original);
      result.method_10852(class_2561.method_43470(" ").method_27692(class_124.field_1080));
      if (showSeparator) {
         result.method_10852(class_2561.method_43470("| ").method_27692(class_124.field_1060));
      }
      int color = showColors ? potsColor(pots) : 0xFFFFFF;
      result.method_10852(class_2561.method_43470("-" + pots).method_54663(color));
      return result;
   }

   public static int potsColor(int pots) {
      return switch (pots) {
         case 1, 2 -> 0x55FF55;
         case 3, 4 -> 0x00AA00;
         case 5, 6 -> 0xFFFF55;
         case 7, 8 -> 0xFFAA00;
         default   -> 0xFF5555;
      };
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 == null) return;
      if (!config.misc.enabled || !config.combat.potionThrowCounterHud) return;

      int count = get(client.field_1724.method_5667());
      String label = "Pots";
      String countText = String.valueOf(count);

      int x = config.hud.totemHudX;
      int y = config.hud.totemHudY + 24;

      int textWidth = countText.isEmpty()
         ? CustomThemeRenderer.textWidth(client.field_1772, "99", config)
         : Math.max(CustomThemeRenderer.textWidth(client.field_1772, label, config), CustomThemeRenderer.textWidth(client.field_1772, countText, config));

      context.method_51448().pushMatrix();
      CustomThemeRenderer.drawHudLabel(context, client.field_1772, countText, x + 28, y + 13, countColor(count, config), config);
      context.method_51448().popMatrix();
   }

   private static int countColor(int count, TurtModConfig config) {
      if (!config.combat.potionThrowCounterColors) return 0xFFFFFFFF;
      return potsColor(count);
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      String label = "Pots";
      int textWidth = 24;
      if (client != null && client.field_1772 != null) {
         textWidth = label.isEmpty() ? CustomThemeRenderer.textWidth(client.field_1772, "99", config) : Math.max(CustomThemeRenderer.textWidth(client.field_1772, label, config), CustomThemeRenderer.textWidth(client.field_1772, "99", config));
      }
      return Math.round((26 + textWidth + 8) * CustomThemeRenderer.getHudScale(config, config.hud.totemHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round(24.0F * CustomThemeRenderer.getHudScale(config, config.hud.totemHudScalePercent));
   }
}
