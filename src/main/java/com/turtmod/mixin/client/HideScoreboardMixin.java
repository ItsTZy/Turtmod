package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_266;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public abstract class HideScoreboardMixin {
   @Inject(
      method = {"method_55803"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$hideScoreboardRoot(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.hideScoreboard) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"method_1757"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$hideScoreboardLeaf(class_332 context, class_266 objective, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.hideScoreboard) {
         ci.cancel();
      }

   }
}
