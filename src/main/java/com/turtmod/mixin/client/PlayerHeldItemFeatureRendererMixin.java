package com.turtmod.mixin.client;

import com.turtmod.utils.ShieldEntityContext;
import net.minecraft.class_10055;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_5697;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_5697.class})
public abstract class PlayerHeldItemFeatureRendererMixin {
   @Inject(
      method = {"method_62594"},
      at = {@At("HEAD")},
      remap = false,
      require = 0
   )
   private void turtmod$capturePlayerForShieldRender(class_10055 playerState, class_10444 itemState, class_1799 stack, class_1306 arm, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      class_310 client = class_310.method_1551();
      if (client.field_1687 == null) {
         ShieldEntityContext.clear();
      } else {
         int entityId = playerState.field_53528;
         class_1297 var11 = client.field_1687.method_8469(entityId);
         if (var11 instanceof class_1657) {
            class_1657 player = (class_1657)var11;
            ShieldEntityContext.set(player);
         } else {
            ShieldEntityContext.clear();
         }

      }
   }

   @Inject(
      method = {"method_62594"},
      at = {@At("RETURN")},
      remap = false,
      require = 0
   )
   private void turtmod$clearPlayerForShieldRender(CallbackInfo ci) {
      ShieldEntityContext.clear();
   }
}
