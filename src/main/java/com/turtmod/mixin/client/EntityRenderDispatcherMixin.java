package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_12155;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1671;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_12155.class})
public abstract class EntityRenderDispatcherMixin {
   @ModifyVariable(
      method = {"method_75432"},
      at = @At("STORE"),
      index = 7
   )
   private int turtmod$recolorVanillaHitboxes(int originalColor, class_1297 entity, float tickProgress, boolean includeInvisible) {
      TurtModConfig cfg = TurtModClient.getConfig();
      class_310 mc = class_310.method_1551();
      if (cfg != null && cfg.misc.enabled && cfg.hud.customHitboxes && mc != null && mc.field_1724 != null && entity != null) {
         if (cfg.hud.hitboxHideFireworks && entity instanceof class_1671) {
            return 0;
         } else if (!turtmod$shouldRenderEntity(entity, mc.field_1724, cfg)) {
            return 0;
         } else {
            class_1297 targeted = null;
            class_239 var9 = mc.field_1765;
            if (var9 instanceof class_3966) {
               class_3966 ehr = (class_3966)var9;
               targeted = ehr.method_17782();
            }

            return turtmod$getColor(entity, targeted, cfg);
         }
      } else {
         return originalColor;
      }
   }

   private static boolean turtmod$shouldRenderEntity(class_1297 entity, class_746 player, TurtModConfig config) {
      if (entity == player && !config.hud.hitboxSelf) {
         return false;
      } else {
         double maxDistSq = (double)config.hud.hitboxMaxDistance * (double)config.hud.hitboxMaxDistance;
         if (entity.method_5858(player) > maxDistSq) {
            return false;
         } else if (entity instanceof class_1657) {
            return config.hud.hitboxPlayers;
         } else if (entity instanceof class_1588) {
            return config.hud.hitboxHostile;
         } else {
            return entity instanceof class_1296 ? config.hud.hitboxPassive : config.hud.hitboxOthers;
         }
      }
   }

   private static int turtmod$getColor(class_1297 entity, class_1297 targeted, TurtModConfig config) {
      if (config.hud.hitboxHurtColorEnabled && entity instanceof class_1309 living) {
         if (living.field_6235 > 0) {
            return config.hud.hitboxHurtColor;
         }
      }

      return config.hud.hitboxChangeTargetColor && targeted == entity ? config.hud.hitboxTargetColor : config.hud.hitboxColor;
   }
}
