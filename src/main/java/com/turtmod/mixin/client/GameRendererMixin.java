package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.visual.ZoomFeature;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_742;
import net.minecraft.class_757;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_757.class})
public abstract class GameRendererMixin {
   @Shadow
   private float field_4019;
   @Shadow
   private float field_3999;

   @Inject(
      method = {"method_3196"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void turtmod$modifyFov(class_4184 camera, float tickProgress, boolean useFovSetting, CallbackInfoReturnable<Float> info) {
      if (ZoomFeature.isZooming()) {
         float normalFov = Math.max(1.0F, Math.min(170.0F, (Float)info.getReturnValue()));
         float modifiedFov = Math.max(1.0F, Math.min(170.0F, normalFov * ZoomFeature.getFovModifier()));
         if (!ZoomFeature.shouldUseSmoothZoom()) {
            info.setReturnValue(modifiedFov);
         }
      }

   }

   @Inject(
      method = {"method_3199"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$handleSmoothZoomFov(CallbackInfo ci) {
      if (ZoomFeature.shouldUseSmoothZoom()) {
         ci.cancel();
         class_310 client = class_310.method_1551();
         float target = 1.0F;
         class_1297 cameraEntity = client.method_1560();
         if (cameraEntity instanceof class_742) {
            class_742 player = (class_742)cameraEntity;
            boolean firstPerson = client.field_1690.method_31044().method_31034();
            float fovEffectScale = ((Double)client.field_1690.method_42454().method_41753()).floatValue();
            target = player.method_3118(firstPerson, fovEffectScale);
         }

         if (ZoomFeature.isZooming()) {
            target *= ZoomFeature.getFovModifier();
         }

         this.field_3999 = this.field_4019;
         this.field_4019 += (target - this.field_4019) * 0.5F;
      }
   }

   @WrapWithCondition(
      method = {"method_3188"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_757;method_3172(FZLorg/joml/Matrix4f;)V"
)}
   )
   private boolean turtmod$hideArmsWhileZooming(class_757 instance, float tickProgress, boolean bobView, Matrix4f matrix) {
      return !ZoomFeature.shouldHideArmsWhenZooming();
   }

   @WrapOperation(
      method = {"method_3198"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_1309;method_48157()F"
)}
   )
   private float turtmod$oldHurtCamDirection(class_1309 entity, Operation<Float> original) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.hurtCamEnabled && (config.visual.oldHurtCameraStyle || config.visual.hurtCamMode == TurtModConfig.HurtCamMode.OLD_NON_DIRECTIONAL)) {
         return 0.0F;
      }
      return original.call(entity);
   }
}
