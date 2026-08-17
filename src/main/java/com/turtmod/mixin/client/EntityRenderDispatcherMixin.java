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
    * (isInvisible) is true. "Show Invisible Armored Players" reveals an invisible PLAYER's hitbox ONLY while
    * they are wearing armour (armour already gives them away, and it hides again the moment they strip) —
    * mirrors the Health Indicator's armored-invisible reveal. Non-player entities and unarmoured invisible
    * players stay hidden. The recolor injector above still colours whatever this reveals.
    */
   @Redirect(
      method = "method_23109",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5767()Z")
   )
   private boolean turtmod$showInvisibleHitboxes(class_1297 entity) {
      boolean invisible = entity.method_5767();
      TurtModConfig cfg = TurtModClient.getConfig();
      if (!invisible || cfg == null || !cfg.misc.enabled || !cfg.hud.customHitboxes) {
         return invisible;
      }
      if (entity instanceof class_1657 player
            && cfg.hud.hitboxPlayers
            && cfg.hud.hitboxShowInvisible
            && turtmod$hasVisibleArmor(player)) {
         return false; // report as visible so the hitbox renders
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
      // Hurt flash and target highlight take precedence — even for an entity with its own custom colour —
      // so those states still apply to per-entity hitboxes (previously the custom colour won outright and
      // the target/hurt colours never showed on entities you'd added to the list).
      if (config.hud.hitboxHurtColorEnabled && entity instanceof class_1309 living && living.field_6235 > 0) {
         return config.hud.hitboxHurtColor;
      }
      if (config.hud.hitboxChangeTargetColor && targeted == entity) {
         return config.hud.hitboxTargetColor;
      }
      // Otherwise a user-picked per-entity colour wins over the default hitbox colour.
      Integer custom = turtmod$customColorFor(entity, config);
      if (custom != null) {
         return custom;
      }
      return config.hud.hitboxColor;
   }

   /** The user-picked colour for this entity's type, or null if the type isn't in the custom list. */
   private static Integer turtmod$customColorFor(class_1297 entity, TurtModConfig config) {
      if (config.hud.hitboxEntityColors == null || config.hud.hitboxEntityColors.isEmpty()) {
         return null;
      }
      net.minecraft.class_2960 id = net.minecraft.class_7923.field_41177.method_10221(entity.method_5864());
      return id == null ? null : config.hud.hitboxEntityColors.get(id.toString());
   }
}
