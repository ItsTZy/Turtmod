package com.turtmod.combat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CustomThemeRenderer;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_7923;
import net.minecraft.class_9334;

/**
 * Tracks splash-healing-potion throws per player (the pop map) and renders the pot HUD counter, plus
 * the shared helpers used by the coloured XP bar and the name/tab/text-display appenders. Ported to
 * parity with skalpha's PotionCounter (repos/potcounter-1.21.11): counts instant-health splash potions
 * in the inventory (or the live throw count when {@code potionThrowShowPotCounter}), colours by count,
 * and recognises the modded pot as well as the vanilla one.
 */
public final class PotionThrowTracker {
   private static final int ICON = 16;
   private static final int GAP = 4;
   private static final int PAD = 4;
   private static final Map<UUID, Integer> POTS = new HashMap();
   // Strong-healing splash potion, used as the HUD icon.
   private static final class_1799 POTION_ICON = class_1844.method_57400(class_1802.field_8436, class_1847.field_8980);
   // Modded potions recognised in addition to the vanilla instant-health splash (matches the original).
   private static final List<String> CUSTOM_POTIONS = List.of("skypot:totem_of_void_undying");

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

   /** True for an instant-health splash potion (vanilla) or a recognised modded pot. */
   public static boolean isPotion(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      }
      if (stack.method_31574(class_1802.field_8436)) {
         class_1844 contents = (class_1844) stack.method_58695(class_9334.field_49651, class_1844.field_49274);
         if (contents != null && hasInstantHealth(contents)) {
            return true;
         }
      }
      class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
      return id != null && CUSTOM_POTIONS.contains(id.toString());
   }

   private static boolean hasInstantHealth(class_1844 contents) {
      for (class_1293 effect : contents.method_57397()) {
         class_1291 type = (class_1291) effect.method_5579().method_40229().right().orElse(null);
         if (type == class_1294.field_5915) {
            return true;
         }
      }
      return false;
   }

   public static int countPotions(class_1657 player) {
      int count = 0;
      for (class_1799 stack : player.method_31548().method_67533()) {
         if (isPotion(stack)) {
            count += stack.method_7947();
         }
      }
      class_1799 offhand = player.method_6079();
      if (isPotion(offhand)) {
         count += offhand.method_7947();
      }
      return count;
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 == null || !config.misc.enabled || !config.combat.potionThrowCounterHud) {
         return;
      }
      int count = config.combat.potionThrowShowPotCounter
         ? get(client.field_1724.method_5667())
         : countPotions(client.field_1724);
      if (count <= 0) {
         return;
      }

      class_327 font = client.field_1772;
      String text = config.combat.potionThrowShowPotCounter ? "-" + count : String.valueOf(count);
      int color = config.combat.potionThrowColorByCount
         ? (config.combat.potionThrowShowPotCounter ? potsColor(count) : potionColor(count))
         : CustomThemeRenderer.getTextColor(config);

      int x = config.hud.potionThrowHudX;
      int y = config.hud.potionThrowHudY;
      float scale = CustomThemeRenderer.getHudScale(config, config.hud.potionThrowHudScalePercent);

      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float)(-x), (float)(-y));

      context.method_51427(POTION_ICON, x, y);
      int textY = y + (ICON - font.field_2000) / 2;
      CustomThemeRenderer.drawHudLabel(context, font, text, x + ICON + GAP, textY, color, config);

      context.method_51448().popMatrix();
   }

   /** Colour when showing the LIVE throw count (green when low, escalating to red). */
   public static int potsColor(int pots) {
      return switch (pots) {
         case 1, 2 -> 0x55FF55;
         case 3, 4 -> 0x00AA00;
         case 5, 6 -> 0xFFFF55;
         case 7, 8 -> 0xFFAA00;
         default   -> 0xFF5555;
      };
   }

   /** Colour when showing the INVENTORY potion count (red when low, escalating to green — more is better). */
   public static int potionColor(int amount) {
      if (amount <= 0) {
         return 0xFFAAAAAA;
      } else if (amount <= 2) {
         return 0xFFFF5555;
      } else if (amount <= 4) {
         return 0xFFFFAA00;
      } else if (amount <= 6) {
         return 0xFFFFFF55;
      } else if (amount <= 8) {
         return 0xFF55AA55;
      } else {
         return 0xFF55FF55;
      }
   }

   private static int contentWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      String sample = config.combat.potionThrowShowPotCounter ? "-99" : "99";
      int textWidth = client != null && client.field_1772 != null
         ? CustomThemeRenderer.textWidth(client.field_1772, sample, config)
         : sample.length() * 6;
      return ICON + GAP + textWidth + PAD;
   }

   public static int getScaledWidth(TurtModConfig config) {
      return Math.round((float)contentWidth(config) * CustomThemeRenderer.getHudScale(config, config.hud.potionThrowHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round((float)(ICON + PAD) * CustomThemeRenderer.getHudScale(config, config.hud.potionThrowHudScalePercent));
   }
}
