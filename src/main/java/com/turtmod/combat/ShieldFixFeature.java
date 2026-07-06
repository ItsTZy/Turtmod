package com.turtmod.combat;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.ShieldTracker;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_6880;

/**
 * Port of Walksy's ShieldFixes (repos/ShieldFixes-26.1): fixes two Mojang shield bugs client-side.
 *
 * - MC-238293 (blocking animation): other players' shields often don't show the blocking pose. The
 *   arm-pose fix lives in {@code AvatarRendererMixin}; this class supplies the "is this other player
 *   actually blocking" decision, reusing {@link ShieldTracker}'s synced-flag heuristic (which already
 *   solves the "their useItem isn't synced" problem without a duck interface). With the 5-tick delay
 *   factored in it uses the ≥5-tick threshold; without, it triggers as soon as they raise the shield.
 * - MC-105068 (shield sounds): plays the SHIELD_BLOCK / SHIELD_BREAK sounds the vanilla client omits
 *   when a shield blocks a hit or gets disabled.
 */
public final class ShieldFixFeature {
   private ShieldFixFeature() {
   }

   /** True when {@code player} is blocking with a usable shield. {@code source}, if non-null, requires
    *  the block to face that position (directional, like vanilla LivingEntity#applyItemBlocking). */
   public static boolean isBlockingShield(class_1657 player, class_243 source, boolean delay) {
      boolean using = delay
         ? ShieldTracker.isUsingShield(player)
         : (ShieldTracker.isHoldingUsableShield(player) && player.method_6115());
      if (!using) {
         return false;
      }
      if (source == null) {
         return true;
      }
      class_243 view = player.method_5828(1.0F);
      class_243 playerPos = new class_243(player.method_23317(), player.method_23318(), player.method_23321());
      class_243 to = playerPos.method_1020(source).method_1029();
      class_243 flat = new class_243(to.field_1352, 0.0, to.field_1350);
      return flat.method_1026(view) < 0.0;
   }

   /** Local player attacked {@code target}: if they're blocking toward us and we're not holding an axe
    *  (which would disable the shield instead), play the block sound the client normally skips. */
   public static void onAttack(class_1657 target) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || !config.visual.shieldFixSounds) {
         return;
      }
      class_310 mc = class_310.method_1551();
      if (mc == null || mc.field_1724 == null) {
         return;
      }
      class_243 selfPos = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
      if (isBlockingShield(target, selfPos, true) && !ShieldTracker.disablesShield(mc.field_1724)) {
         playSound(target, class_3417.field_15150);
      }
   }

   /** A player's shield was disabled (entity status 30): play the break sound. */
   public static void onDisable(class_1657 entity) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || !config.visual.shieldFixSounds) {
         return;
      }
      playSound(entity, class_3417.field_15239);
   }

   private static void playSound(class_1657 entity, class_6880<class_3414> sound) {
      class_310 mc = class_310.method_1551();
      if (mc == null || mc.field_1687 == null) {
         return;
      }
      float pitch = 0.8F + mc.field_1687.method_8409().method_43057() * 0.4F;
      mc.field_1687.method_8486(
         entity.method_23317(), entity.method_23318(), entity.method_23321(),
         sound.comp_349(), entity.method_5634(), 1.0F, pitch, false);
   }
}
