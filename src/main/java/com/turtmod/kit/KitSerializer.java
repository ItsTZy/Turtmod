package com.turtmod.kit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import net.minecraft.class_10630;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_2522;
import net.minecraft.class_310;
import net.minecraft.class_6903;

/**
 * Converts a player inventory to/from an SNBT string (ported from ClientKits). Each non-empty slot
 * is encoded with {@code ItemStack.CODEC} plus its local slot index; on load the index routes the
 * item back to the right main/armor/offhand slot (class_1661 indexes equipment transparently).
 *
 * <p>Kits are intended for same-version use, so no DataFixer migration is applied — an item that
 * fails to decode simply comes back empty rather than aborting the whole kit.
 */
public final class KitSerializer {
   private KitSerializer() {
   }

   // Registries from the last world session — lets kit preview work in the main menu (no player).
   private static net.minecraft.class_5455 cachedRegistries;

   private static DynamicOps<class_2520> ops(class_310 mc) {
      net.minecraft.class_5455 ra = mc.field_1724 != null ? mc.field_1724.method_56673() : cachedRegistries;
      if (mc.field_1724 != null) {
         cachedRegistries = ra;
      }
      if (ra == null) {
         return class_2509.field_11560; // best-effort: plain NBT ops (simple items only)
      }
      return class_6903.method_46632(class_2509.field_11560, ra);
   }

   public static String serialize(class_1661 inventory) {
      class_310 mc = class_310.method_1551();
      DynamicOps<class_2520> ops = ops(mc);
      class_2487 root = new class_2487();
      class_2499 list = new class_2499();
      for (int i = 0; i < inventory.method_5439(); i++) {
         class_1799 stack = inventory.method_5438(i);
         if (stack.method_7960()) {
            continue;
         }
         class_2487 slotTag = new class_2487();
         slotTag.method_10567("Slot", (byte)i);
         class_1799.field_24671.encode(stack, ops, slotTag).result().ifPresent(list::add);
      }
      root.method_10566("inv", list);
      return root.toString();
   }

   public static class_1661 deserialize(String data) throws CommandSyntaxException {
      class_310 mc = class_310.method_1551();
      DynamicOps<class_2520> ops = ops(mc);
      class_2487 root = class_2522.method_67315(data);
      class_1661 inventory = new class_1661(mc.field_1724, new class_10630());
      inventory.method_5448();
      if (root.method_10580("inv") instanceof class_2499 list) {
         for (int i = 0; i < list.size(); i++) {
            class_2487 tag = list.method_68582(i);
            int slot = tag.method_10545("Slot") ? tag.method_68562("Slot", (byte)i) & 255 : i;
            class_1799 stack = class_1799.field_24671.parse(ops, tag).result().orElse(class_1799.field_8037);
            if (slot >= 0 && slot < inventory.method_5439()) {
               inventory.method_5447(slot, stack);
            }
         }
      }
      return inventory;
   }

   /**
    * Builds a slot array for the read-only preview. When world registries are available (in a world,
    * or cached after joining one this session) each slot is fully decoded with {@code ItemStack.CODEC}
    * so potions, enchantments and glint render correctly. If no registries are available (a cold main
    * menu before any world was joined), it falls back to an id-only icon so every item still shows.
    */
   public static net.minecraft.class_1799[] deserializeIcons(String data) {
      net.minecraft.class_1799[] items = new net.minecraft.class_1799[41];
      java.util.Arrays.fill(items, net.minecraft.class_1799.field_8037);
      class_310 mc = class_310.method_1551();
      boolean haveRegistries = mc != null && (mc.field_1724 != null || cachedRegistries != null);
      DynamicOps<class_2520> ops = haveRegistries ? ops(mc) : null;
      try {
         class_2487 root = class_2522.method_67315(data);
         if (root.method_10580("inv") instanceof class_2499 list) {
            for (int i = 0; i < list.size(); i++) {
               class_2487 tag = list.method_68582(i);
               int slot = tag.method_10545("Slot") ? tag.method_68562("Slot", (byte)i) & 255 : i;
               if (slot < 0 || slot >= items.length) {
                  continue;
               }
               net.minecraft.class_1799 stack = net.minecraft.class_1799.field_8037;
               if (ops != null) {
                  stack = class_1799.field_24671.parse(ops, tag).result().orElse(net.minecraft.class_1799.field_8037);
               }
               if (stack.method_7960()) {
                  stack = idOnlyStack(tag); // fallback: just the item + count
               }
               items[slot] = stack;
            }
         }
      } catch (Exception ignored) {
         // Preview is best-effort.
      }
      return items;
   }

   /** Reads only the {@code id} + {@code count} of a slot and builds a bare stack (no components). */
   private static net.minecraft.class_1799 idOnlyStack(class_2487 tag) {
      net.minecraft.class_2960 rid = net.minecraft.class_2960.method_12829(tag.method_68564("id", ""));
      if (rid == null) {
         return net.minecraft.class_1799.field_8037;
      }
      net.minecraft.class_1792 item = net.minecraft.class_7923.field_41178.method_10223(rid)
         .map(net.minecraft.class_6880.class_6883::comp_349).orElse(null);
      if (item == null || item == net.minecraft.class_1802.field_8162) {
         return net.minecraft.class_1799.field_8037;
      }
      return new net.minecraft.class_1799(item, Math.max(1, tag.method_68083("count", 1)));
   }

   /** True if {@code data} is a parseable kit (has an "inv" list). */
   public static boolean isValid(String data) {
      try {
         return data != null && !data.isEmpty() && class_2522.method_67315(data).method_10545("inv");
      } catch (Exception e) {
         return false;
      }
   }
}
