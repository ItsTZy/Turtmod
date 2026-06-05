package com.turtmod.combat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_5250;

public final class TotemPopTracker {
   private static final Map<UUID, Integer> POPS = new HashMap();
   private static final Map<UUID, Long> LAST_POP_TIME = new HashMap();
   private static final long POP_DEBOUNCE_MS = 500L;
   public static final List<String> ROUND_END_MESSAGES = Arrays.asList(
      "Winners:", "has won the round.", "has won the game!",
      "Winner: NONE!", "Match Complete"
   );

   private TotemPopTracker() {
   }

   public static void increment(UUID id) {
      long now = System.currentTimeMillis();
      Long last = LAST_POP_TIME.get(id);
      if (last != null && now - last < POP_DEBOUNCE_MS) {
         return;
      }
      POPS.merge(id, 1, Integer::sum);
      LAST_POP_TIME.put(id, now);
   }

   public static int get(UUID id) {
      return (Integer)POPS.getOrDefault(id, 0);
   }

   public static Map<UUID, Integer> getPops() {
      return POPS;
   }

   public static void reset() {
      POPS.clear();
      LAST_POP_TIME.clear();
   }

   public static void remove(UUID id) {
      POPS.remove(id);
   }

   public static class_2561 appendPops(class_1657 player, class_2561 original) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) {
         return original;
      }
      int pops = get(player.method_5667());
      if (pops <= 0) {
         return original;
      }
      boolean showSeparator = config.combat.totemSeparator;
      boolean showColors = config.combat.totemCounterColors;
      class_5250 result = class_2561.method_43473().method_10852(original);
      result.method_10852(class_2561.method_43470(" ").method_27692(class_124.field_1080));
      if (showSeparator) {
         result.method_10852(class_2561.method_43470("| ").method_27692(class_124.field_1060));
      }
      int color = showColors ? popColor(pops) : 0xFFFFFF;
      result.method_10852(class_2561.method_43470("-" + pops).method_54663(color));
      return result;
   }

   public static int popColor(int pops) {
      return switch (pops) {
         case 1, 2 -> 0x55FF55;
         case 3, 4 -> 0x00AA00;
         case 5, 6 -> 0xFFFF55;
         case 7, 8 -> 0xFFAA00;
         default   -> 0xFF5555;
      };
   }
}
