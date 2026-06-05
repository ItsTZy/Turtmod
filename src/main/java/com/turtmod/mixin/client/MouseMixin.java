package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.CPSTracker;
import com.turtmod.visual.FreeLookFeature;
import com.turtmod.visual.ZoomFeature;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_315;
import net.minecraft.class_7172;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public abstract class MouseMixin {
   @Inject(
      method = {"method_1601"},
      at = {@At("HEAD")}
   )
   private void turtmod$trackMouseClicks(long window, class_11910 mouseInput, int action, CallbackInfo ci) {
      int buttonId = mouseInput.comp_4801();
      if (action == 1) {
         if (buttonId == 0) {
            CPSTracker.onLeftClick();
         } else if (buttonId == 1) {
            CPSTracker.onRightClick();
         }
      }

   }

   @Inject(
      method = {"method_1598"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$adjustZoomOnScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
      class_310 client = class_310.method_1551();
      if (client.field_1690 != null) {
         double adjustedVertical = vertical;
         if ((Boolean)client.field_1690.method_42439().method_41753()) {
            adjustedVertical = Math.signum(vertical);
         }

         adjustedVertical *= (Double)client.field_1690.method_41806().method_41753();
         if (ZoomFeature.onMouseScroll(adjustedVertical)) {
            ci.cancel();
         }

      }
   }

   @WrapOperation(
      method = {"method_1606"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_746;method_5872(DD)V"
)}
   )
   private void turtmod$applyFreelookFromMouse(class_746 player, double cursorDeltaX, double cursorDeltaY, Operation<Void> original) {
      class_310 client = class_310.method_1551();
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || !config.visual.freelookEnabled || !FreeLookFeature.handleMouseLook(player, cursorDeltaX, cursorDeltaY, config)) {
         original.call(new Object[]{player, cursorDeltaX, cursorDeltaY});
      }
   }

   @WrapOperation(
      method = {"method_1606"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_7172;method_41753()Ljava/lang/Object;"
)}
   )
   private Object turtmod$normalizeSensitivity(class_7172<?> instance, Operation<Object> original) {
      Object value = original.call(new Object[]{instance});
      class_310 client = class_310.method_1551();
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && instance == client.field_1690.method_42495() && ZoomFeature.isZooming() && config.visual.zoomNormalizeSensitivity && value instanceof Double sensitivity) {
         return sensitivity * (double)ZoomFeature.getMouseSensitivityMultiplier();
      } else {
         return value;
      }
   }

   @WrapOperation(
      method = {"method_1606"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/class_315;field_1914:Z"
)}
   )
   private boolean turtmod$forceSmoothCamera(class_315 instance, Operation<Boolean> original) {
      TurtModConfig config = TurtModClient.getConfig();
      return config != null && config.visual.zoomSmoothCamera && ZoomFeature.isZooming() ? true : (Boolean)original.call(new Object[]{instance});
   }
}
