package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.combat.PotionThrowTracker;
import com.turtmod.combat.TotemPopTracker;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Appends the totem-pop / potion-throw "-N" counters to a player's OVER-HEAD nametag. The nametag is
 * built from class_897.method_62426 (EntityRenderer.getNameTag) — the same hook the Ping-on-nametag
 * feature uses — NOT Player.getDisplayName, which is why the counters never showed above heads before.
 * Each counter is gated by its own toggle (totemNametagPops / potionThrowNametagPots).
 */
@Mixin({class_897.class})
public class PlayerNameTagCounterMixin {
   @Inject(
      method = {"method_62426"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void turtmod$appendCounters(class_1297 entity, CallbackInfoReturnable<class_2561> cir) {
      TurtModConfig config = TurtModClient.getConfig();
      class_2561 original = cir.getReturnValue();
      if (config == null || !config.misc.enabled || original == null || !(entity instanceof class_1657 player)) {
         return;
      }
      class_2561 result = original;
      if (config.combat.totemNametagPops) {
         result = TotemPopTracker.appendPops(player, result);
      }
      if (config.combat.potionThrowNametagPots) {
         result = PotionThrowTracker.appendPots(player, result);
      }
      if (result != original) {
         cir.setReturnValue(result);
      }
   }
}
