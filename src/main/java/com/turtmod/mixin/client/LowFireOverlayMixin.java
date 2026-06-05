package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4603.class})
public abstract class LowFireOverlayMixin {
   @Inject(
      method = {"method_23070"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void turtmod$cancelFireOverlay(CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      // Only fully hide fire when the explicit "Hide Fire Completely" option is on.
      if (config != null && config.misc.enabled && config.visual.hideFireOverlay) {
         ci.cancel();
      }

   }

   @ModifyArg(
      method = {"method_23070"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4587;method_46416(FFF)V"
),
      index = 1,
      require = 0
   )
   private static float turtmod$offsetFireY(float originalY) {
      TurtModConfig config = TurtModClient.getConfig();
      // Lower/raise the fire only when the Low Fire module is enabled.
      if (config != null && config.misc.enabled && config.visual.disableFireOverlay) {
         float dy = (float)config.visual.fireYOffset / 100.0F;
         return originalY + dy;
      } else {
         return originalY;
      }
   }
}
