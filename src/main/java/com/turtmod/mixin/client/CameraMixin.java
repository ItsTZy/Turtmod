package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.turtmod.visual.FreeLookFeature;
import net.minecraft.class_1297;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_4184.class})
public abstract class CameraMixin {
   @WrapOperation(
      method = {"method_19321"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_1297;method_5705(F)F"
)}
   )
   private float turtmod$freelookYaw(class_1297 entity, float tickDelta, Operation<Float> original) {
      return FreeLookFeature.isActive() ? FreeLookFeature.getYaw() : original.call(entity, tickDelta);
   }

   @WrapOperation(
      method = {"method_19321"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_1297;method_5695(F)F"
)}
   )
   private float turtmod$freelookPitch(class_1297 entity, float tickDelta, Operation<Float> original) {
      return FreeLookFeature.isActive() ? FreeLookFeature.getPitch() : original.call(entity, tickDelta);
   }
}
