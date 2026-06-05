package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public abstract class PotionHudMixin {
   @Inject(
      method = {"method_1765"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$hideVanillaPotionHud(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled) {
         if (config.hud.movablePotionHud && config.hud.hideVanillaPotionHud) {
            ci.cancel();
         }

      }
   }
}
