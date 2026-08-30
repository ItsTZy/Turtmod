package com.turtmod.ui;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_6880;
import net.minecraft.class_7923;

/**
 * Real vanilla Minecraft item icons per module — crisp, native, instantly recognisable (replaces the crude
 * hand-drawn pixel glyphs that "looked AI generated"). Each module maps to a fitting item id; the ItemStack is
 * resolved once from the item registry and cached. Modules with no mapping return {@code null} so the caller
 * falls back to the {@link TurtIcons} pixel glyph.
 */
public final class TurtModuleIcons {
   private TurtModuleIcons() {
   }

   // Module display name (must match TurtIcons.MODULES keys) → vanilla item id.
   private static final Map<String, String> ITEM_IDS = new HashMap<>();
   static {
      ITEM_IDS.put("Enable TurtMod", "turtle_helmet");
      ITEM_IDS.put("Keystrokes", "lever");
      ITEM_IDS.put("FPS/Ping", "clock");
      ITEM_IDS.put("Ping Display", "redstone_torch");
      ITEM_IDS.put("CPS Counter", "lever");
      ITEM_IDS.put("Health Indicator", "golden_apple");
      ITEM_IDS.put("Fullbright", "glowstone");
      ITEM_IDS.put("Screenshot Tools", "map");
      ITEM_IDS.put("Hurt Cam", "redstone");
      ITEM_IDS.put("Potion HUD", "potion");
      ITEM_IDS.put("Zoom", "spyglass");
      ITEM_IDS.put("Armor HUD", "iron_chestplate");
      ITEM_IDS.put("Hit Color", "red_dye");
      ITEM_IDS.put("Reach Display", "stick");
      ITEM_IDS.put("Hitboxes", "armor_stand");
      ITEM_IDS.put("Inventory HUD", "chest");
      ITEM_IDS.put("Kit Loader", "bundle");
      ITEM_IDS.put("Coordinates", "compass");
      ITEM_IDS.put("Death Coords", "recovery_compass");
      ITEM_IDS.put("Low Fire", "fire_charge");
      ITEM_IDS.put("Fog Tweaks", "snowball");
      ITEM_IDS.put("Clear View", "glass");
      ITEM_IDS.put("Overlays", "item_frame");
      ITEM_IDS.put("Shield Tweaks", "shield");
      ITEM_IDS.put("Freelook", "ender_eye");
      ITEM_IDS.put("Own Nametag", "name_tag");
      ITEM_IDS.put("Clean F3", "paper");
      ITEM_IDS.put("Scoreboard Tweaks", "oak_sign");
      ITEM_IDS.put("Chat Tweaks", "writable_book");
      ITEM_IDS.put("Command Keys", "command_block");
      ITEM_IDS.put("Module Notifications", "bell");
      ITEM_IDS.put("Theme Settings", "painting");
      ITEM_IDS.put("Block Outline", "string");
      ITEM_IDS.put("Held Item Tweaks", "diamond_sword");
      ITEM_IDS.put("Sprint Display", "feather");
      ITEM_IDS.put("Elytra Pitch HUD", "elytra");
      ITEM_IDS.put("Discord RPC", "music_disc_cat");
      ITEM_IDS.put("Gamemode Switcher", "grass_block");
      ITEM_IDS.put("Container Buttons", "hopper");
      ITEM_IDS.put("Fishing Line", "fishing_rod");
      ITEM_IDS.put("Mute Sounds", "note_block");
      ITEM_IDS.put("Particle Tweaks", "gunpowder");
      ITEM_IDS.put("Totem Tweaks", "totem_of_undying");
      // Sidebar nav + category tabs (keyed by their label). "Back" stays a pixel arrow (no fitting item).
      ITEM_IDS.put("HUD Editor", "item_frame");
      ITEM_IDS.put("Skin Changer", "player_head");
      ITEM_IDS.put("Settings", "redstone");
      ITEM_IDS.put("Gallery", "painting");
      ITEM_IDS.put("Visuals", "ender_eye");
      ITEM_IDS.put("HUD", "item_frame");
      ITEM_IDS.put("Utility", "repeater");
      ITEM_IDS.put("Misc", "barrel");
   }

   // Resolved-and-cached stacks. A sentinel EMPTY marks "no mapping / unresolved" so we only look up once.
   private static final Map<String, class_1799> CACHE = new HashMap<>();

   /** The item icon for a module, or {@code null} if it has no mapping (caller falls back to the pixel glyph). */
   public static class_1799 forModule(String displayName) {
      if (displayName == null) {
         return null;
      }
      class_1799 cached = CACHE.get(displayName);
      if (cached != null) {
         return cached == class_1799.field_8037 ? null : cached;
      }
      String id = ITEM_IDS.get(displayName);
      class_1799 stack = class_1799.field_8037;
      if (id != null) {
         class_1792 item = class_7923.field_41178.method_10223(class_2960.method_60656(id))
            .map(class_6880.class_6883::comp_349).orElse(null);
         if (item != null && item != class_1802.field_8162) {
            stack = new class_1799(item);
         }
      }
      CACHE.put(displayName, stack);
      return stack == class_1799.field_8037 ? null : stack;
   }

   /** Draw a module's item icon (native 16px). Respects the current matrix stack (like the Armor HUD does). */
   public static void drawItem(class_332 ctx, class_1799 stack, int x, int y) {
      ctx.method_51427(stack, x, y);
   }
}
