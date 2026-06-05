package com.turtmod.mixin.client;

import com.turtmod.combat.TotemPopTracker;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_759;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_759.class})
public class TotemPopGameRendererMixin {
   @Shadow @Final
   private class_310 field_3884;

   @Inject(method = {"method_3198"}, at = {@At("HEAD")})
   private void turtmod$onDisplayItemActivation(class_1799 floatingItem, CallbackInfo ci) {
      if (this.field_3884.field_1724 == null) return;
      if (floatingItem.method_31574(class_1802.field_8288)) {
         TotemPopTracker.increment(this.field_3884.field_1724.method_5667());
      }
   }
}
