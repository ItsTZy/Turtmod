package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_758;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_758.class})
public abstract class FogRendererMixin {
   @ModifyVariable(
      method = {"method_3211"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private int turtmod$adjustFogDistanceChunks(int viewDistanceChunks) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.disableAllFog) {
         return Math.max(viewDistanceChunks, 64);
      }
      return viewDistanceChunks;
   }

   @Inject(
      method = {"method_71652"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void turtmod$overrideSubmersionFog(class_4184 camera, CallbackInfoReturnable<class_5636> cir) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled) {
         if (config.visual.disableAllFog) {
            cir.setReturnValue(class_5636.field_60563);
         } else {
            class_5636 type = (class_5636)cir.getReturnValue();
            if (type == class_5636.field_27886 && config.visual.disableWaterFog) {
               cir.setReturnValue(class_5636.field_60563);
            } else if (type == class_5636.field_27885 && config.visual.disableLavaFog) {
               cir.setReturnValue(class_5636.field_60563);
            }
         }
      }
   }
}
