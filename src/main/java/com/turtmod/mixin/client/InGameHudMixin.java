package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_329;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public abstract class InGameHudMixin {
   @Inject(
      method = {"method_1746"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$hidePortal(class_332 context, float strength, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.hidePortalOverlay) {
         ci.cancel();
      }
   }

   // Carved-pumpkin blur AND powder-snow frosted border both render through the
   // generic camera texture-overlay (Gui.renderTextureOverlay). Filter by texture.
   @Inject(
      method = {"method_31977"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$hideTextureOverlay(class_332 context, class_2960 texture, float alpha, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || texture == null) {
         return;
      }
      String path = texture.method_12832();
      if (config.visual.disablePumpkinBlur && path.contains("pumpkin")) {
         ci.cancel();
      } else if (config.visual.disablePowderSnowOverlay && path.contains("powder_snow")) {
         ci.cancel();
      }
   }

   // Spyglass scope overlay (Gui.method_32598 -> spyglass_scope.png)
   @Inject(
      method = {"method_32598"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$hideSpyglass(class_332 context, float scale, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.disableSpyglassOverlay) {
         ci.cancel();
      }
   }

   // Vignette darkening at screen edges (Gui.method_1735 -> vignette.png)
   @Inject(
      method = {"method_1735"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$hideVignette(class_332 context, class_1297 entity, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.disableVignette) {
         ci.cancel();
      }
   }

   @ModifyArg(
      method = {"method_1760"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_329;method_37298(Lnet/minecraft/class_332;Lnet/minecraft/class_1657;IIIIFIIIZ)V"
      ),
      index = 10
   )
   private boolean turtmod$disableHeartBlink(boolean blinking) {
      TurtModConfig config = TurtModClient.getConfig();
      return config != null && config.misc.enabled && config.visual.disableHeartBlink ? false : blinking;
   }
}
