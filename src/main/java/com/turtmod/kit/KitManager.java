package com.turtmod.kit;

import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_1934;
import net.minecraft.class_310;

/**
 * Saves/loads client kits (ported from ClientKits). Applying a kit needs creative mode (it uses the
 * creative set-slot packet), so if the player isn't creative this switches them to creative, applies
 * on the next tick once the switch lands, then restores the original game mode. Requires permission
 * to run /gamemode on the server (singleplayer or op'd); otherwise the switch silently fails.
 */
public final class KitManager {
   private static State state = State.IDLE;
   private static class_1934 originalGameMode = null;
   private static String pendingData = null;
   private static int restoreTimer = 0;

   private KitManager() {
   }

   public static boolean saveKit(String name) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || KitIO.exists(name)) {
         return false;
      }
      String data = KitSerializer.serialize(mc.field_1724.method_31548());
      if (!KitSerializer.isValid(data)) {
         return false;
      }
      try {
         KitIO.save(name, data);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   /** @return null on success/in-progress, or an error message. */
   public static String loadKit(String name) {
      String data = KitIO.load(name);
      if (!KitSerializer.isValid(data)) {
         return "Kit data is corrupted or missing";
      }
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || mc.field_1761 == null) {
         return "Not in a world";
      }
      if (isCreative(mc)) {
         return applyData(data);
      }
      if (mc.field_1724.field_3944 == null) {
         return "Not connected";
      }
      pendingData = data;
      originalGameMode = mc.field_1761.method_2920();
      state = State.SWITCHING_TO_CREATIVE;
      mc.field_1724.field_3944.method_45730("gamemode creative");
      return null;
   }

   public static void tick() {
      if (state == State.IDLE) {
         return;
      }
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || mc.field_1761 == null) {
         reset();
         return;
      }
      switch (state) {
         case SWITCHING_TO_CREATIVE -> {
            if (isCreative(mc) && pendingData != null) {
               applyData(pendingData);
               pendingData = null;
               restoreTimer = 3;
               state = State.RESTORING_GAMEMODE;
            }
         }
         case RESTORING_GAMEMODE -> {
            if (--restoreTimer <= 0) {
               if (originalGameMode != null && mc.field_1724.field_3944 != null) {
                  mc.field_1724.field_3944.method_45730("gamemode " + originalGameMode.method_8381());
               }
               reset();
            }
         }
         default -> {
         }
      }
   }

   private static boolean isCreative(class_310 mc) {
      return mc.field_1761.method_2920() == class_1934.field_9220;
   }

   private static String applyData(String data) {
      try {
         class_310 mc = class_310.method_1551();
         class_1661 loaded = KitSerializer.deserialize(data);
         class_1661 playerInv = mc.field_1724.method_31548();
         class_1703 menu = mc.field_1724.field_7512;

         for (int i = 0; i < playerInv.method_5439(); i++) {
            playerInv.method_5447(i, class_1799.field_8037);
         }
         for (int i = 0; i < loaded.method_5439(); i++) {
            playerInv.method_5447(i, loaded.method_5438(i).method_7972());
         }
         // Push every screen-handler slot to the server via the creative set-slot packet.
         for (int i = 0; i < menu.field_7761.size(); i++) {
            mc.field_1761.method_2909(menu.field_7761.get(i).method_7677(), i);
         }
         menu.method_7623();
         return null;
      } catch (Exception e) {
         return "Failed to apply kit: " + e.getMessage();
      }
   }

   private static void reset() {
      state = State.IDLE;
      originalGameMode = null;
      pendingData = null;
      restoreTimer = 0;
   }

   private enum State {
      IDLE,
      SWITCHING_TO_CREATIVE,
      RESTORING_GAMEMODE
   }
}
