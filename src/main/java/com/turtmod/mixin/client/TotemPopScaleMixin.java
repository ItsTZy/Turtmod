package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_4587;
import net.minecraft.class_4603;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({class_4603.class})
public abstract class TotemPopScaleMixin {

   // Disable the spinning/wobble rotations of the totem pop animation
   @WrapOperation(
      method = {"method_70939"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionf;)V"),
      require = 0
   )
   private void turtmod$maybeRotate(class_4587 matrices, Quaternionf q, Operation<Void> original) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.disableTotemPopRotation) {
         return; // skip rotation
      }
      original.call(matrices, q);
   }
   @ModifyArgs(
      method = {"method_70939"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4587;method_46416(FFF)V"
),
      require = 0
   )
   private void turtmod$offsetTotemPop(Args args) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled) {
         float ox = (float)config.visual.totemPopOffsetX / 100.0F;
         float oy = (float)config.visual.totemPopOffsetY / 100.0F;
         float oz = (float)config.visual.totemPopOffsetZ / 100.0F;
         if (ox != 0.0F || oy != 0.0F || oz != 0.0F) {
            args.set(0, (Float)args.get(0) + ox);
            args.set(1, (Float)args.get(1) + oy);
            args.set(2, (Float)args.get(2) + oz);
         }
      }
   }

   @ModifyArgs(
      method = {"method_70939"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4587;method_22905(FFF)V"
),
      require = 0
   )
   private void turtmod$scaleTotemPop(Args args) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled) {
         float cfgScale = (float)Math.max(25, Math.min(200, config.visual.totemPopScalePercent)) / 100.0F;
         if (cfgScale != 1.0F) {
            args.set(0, (Float)args.get(0) * cfgScale);
            args.set(1, (Float)args.get(1) * cfgScale);
            args.set(2, (Float)args.get(2) * cfgScale);
         }
      }
   }
}
