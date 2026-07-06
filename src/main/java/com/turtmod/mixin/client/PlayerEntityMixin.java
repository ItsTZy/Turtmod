package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.turtmod.TurtModClient;
import com.turtmod.combat.PotionThrowTracker;
import com.turtmod.combat.TotemPopTracker;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Appends the totem-pop and potion-throw "-N" counters to a player's display name (method_5476 =
 * getDisplayName), so they show above heads. Each counter is gated by its own nametag toggle
 * (totemNametagPops / potionThrowNametagPots). Mirrors the original mods' MixinPlayer.
 */
@Mixin({class_1657.class})
public abstract class PlayerEntityMixin {
   @ModifyReturnValue(
      method = {"method_5476"},
      at = {@At("RETURN")}
   )
   private class_2561 turtmod$appendPops(class_2561 original) {
      class_1657 self = (class_1657)(Object)this;
      TurtModConfig config = TurtModClient.getConfig();
      class_2561 result = original;
      if (config == null || !config.misc.enabled) {
         return result;
      }
      if (config.combat.totemNametagPops) {
         result = TotemPopTracker.appendPops(self, result);
      }
      if (config.combat.potionThrowNametagPots) {
         result = PotionThrowTracker.appendPots(self, result);
      }
      return result;
   }
}
