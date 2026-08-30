package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.combat.ShieldFixFeature;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_572;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes MC-238293 (repos/ShieldFixes-26.1 AvatarRendererMixin): other players' shields frequently
 * fail to show the blocking arm pose because their item-use state isn't fully synced. When our tracker
 * says an OTHER player is blocking with the shield held in this hand, force the BLOCK pose
 * ({@code class_572$class_573.field_3406}). class_1007 = PlayerRenderer; method_4210 = getArmPose.
 */
@Mixin(class_1007.class)
public class AvatarRendererMixin {
   @Inject(method = "method_4210", at = @At("HEAD"), cancellable = true)
   private static void turtmod$forceShieldBlockPose(class_11890 avatar, class_1799 stack, class_1268 hand, CallbackInfoReturnable<class_572.class_573> cir) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || !config.visual.shieldFixBlockingAnim) {
         return;
      }
      class_310 mc = class_310.method_1551();
      if (!(avatar instanceof class_1657 player) || player == mc.field_1724) {
         return;
      }
      if (!stack.method_31574(class_1802.field_8255)) {
         return;
      }
      if (ShieldFixFeature.isBlockingShield(player, null, config.visual.shieldFix5TickDelay)) {
         cir.setReturnValue(class_572.class_573.field_3406);
      }
   }
}
