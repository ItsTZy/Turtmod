package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.visual.FreeLookFeature;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1297.class})
public abstract class EntityMixin {
   @Inject(
      method = {"method_5872"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$applyFreelook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg != null && cfg.misc.enabled && cfg.visual.freelookEnabled) {
         if (FreeLookFeature.handleMouseLook((class_1297)(Object)this, cursorDeltaX, cursorDeltaY, cfg)) {
            ci.cancel();
         }

      }
   }
}
