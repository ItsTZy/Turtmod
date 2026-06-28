package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1536;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import net.minecraft.class_4604;
import net.minecraft.class_906;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_906.class})
public abstract class FishingBobberRendererMixin {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_62442"},
      cancellable = true
   )
   private void turtmod$hideFishingBobber(class_1536 fishingBobberEntity, class_4604 frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.visual.hideFishingBobber) {
         class_1657 player = class_310.method_1551().field_1724;
         boolean isFirstPerson = class_310.method_1551().field_1690.method_31044().method_31034();
         if (isFirstPerson && fishingBobberEntity.method_26957() == player) {
            cir.setReturnValue(false);
         }

      }
   }

   /**
    * Fishing line recolor. The line segments are emitted in {@code method_23172} via
    * {@code class_4588.method_39415(int)} (vanilla passes 0xFF000000 / black). When the feature is on
    * we swap that for the configured colour + alpha so the cast line is easy to see / styled.
    */
   @ModifyArg(
      method = {"method_23172"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4588;method_39415(I)Lnet/minecraft/class_4588;"),
      index = 0,
      require = 0
   )
   private static int turtmod$fishingLineColor(int original) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.fishingRodOverlay) {
         float af = config.visual.fishingRodOverlayAlpha;
         af = af < 0f ? 0f : (af > 1f ? 1f : af);
         int a = Math.round(af * 255f);
         return (a << 24) | (config.visual.fishingRodOverlayColor & 0xFFFFFF);
      }
      return original;
   }
}
