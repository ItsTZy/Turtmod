package com.turtmod.utils;

import java.util.UUID;
import net.minecraft.class_1309;

public final class ShieldEntityContext {
   private static final ThreadLocal<class_1309> CURRENT_ENTITY = new ThreadLocal();
   private static final ThreadLocal<UUID> CURRENT_UUID = new ThreadLocal();

   private ShieldEntityContext() {
   }

   public static void set(class_1309 entity) {
      CURRENT_ENTITY.set(entity);
      if (entity != null) {
         CURRENT_UUID.set(entity.method_5667());
      } else {
         CURRENT_UUID.remove();
      }

   }

   public static void setUuid(UUID uuid) {
      CURRENT_UUID.set(uuid);
   }

   public static class_1309 get() {
      return (class_1309)CURRENT_ENTITY.get();
   }

   public static UUID getUuid() {
      UUID uuid = (UUID)CURRENT_UUID.get();
      return uuid == null && CURRENT_ENTITY.get() != null ? ((class_1309)CURRENT_ENTITY.get()).method_5667() : uuid;
   }

   public static void clear() {
      CURRENT_ENTITY.remove();
      CURRENT_UUID.remove();
   }
}
