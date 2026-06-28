package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
//? if >=1.21.11 {
import net.minecraft.class_12155;
//?}
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
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
import org.spongepowered.asm.mixin.injection.Redirect;

//? if >=1.21.11 {
@Mixin({class_12155.class})
//?} else {
/*@Mixin({net.minecraft.class_898.class})
*///?}
public abstract class EntityRenderDispatcherMixin {
   //? if >=1.21.11 {
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

   /**
    * The debug-hitbox loop ({@code class_12155.method_23109}) skips any entity whose {@code method_5767()}
    * (isInvisible) is true. When "Show On Invisible" is on we make that check report invisible PLAYERS as
    * visible, so their hitbox is rendered (and the recolor injector above still colours it). Other
    * call-effects of isInvisible are unaffected because the redirect is scoped to this method only.
    */
   @Redirect(
      method = "method_23109",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5767()Z")
   )
   private boolean turtmod$showInvisibleHitboxes(class_1297 entity) {
      boolean invisible = entity.method_5767();
      TurtModConfig cfg = TurtModClient.getConfig();
      if (!invisible || cfg == null || !cfg.misc.enabled || !cfg.hud.customHitboxes || !cfg.hud.hitboxShowInvisible) {
         return invisible;
      }
      if (entity instanceof class_1657 player) {
         if (!cfg.hud.hitboxPlayers) {
            return invisible;
         }
         // "Armor Only": keep invisible players hidden unless they're wearing armour.
         if (cfg.hud.hitboxShowInvisibleArmorOnly && !turtmod$hasVisibleArmor(player)) {
            return invisible;
         }
         return false;
      }
      // Non-player entities: only when "Include Mobs/Entities" is on. The recolor injector's per-type
      // filters (hostile/passive/others/distance) still apply by zeroing the colour of unwanted types.
      if (cfg.hud.hitboxShowInvisibleEntities) {
         return false;
      }
      return invisible;
   }

   private static boolean turtmod$hasVisibleArmor(class_1657 player) {
      return !player.method_6118(class_1304.field_6169).method_7960()
         || !player.method_6118(class_1304.field_6174).method_7960()
         || !player.method_6118(class_1304.field_6172).method_7960()
         || !player.method_6118(class_1304.field_6166).method_7960();
   }
   //?}

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
