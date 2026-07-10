package com.turtmod.mixin.client;

import com.turtmod.utils.ShieldTracker;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1309.class})
public class LivingEntityStatusMixin {
   @Inject(
      method = {"method_5711"},
      at = {@At("HEAD")}
   )
   private void turtmod$onHandleStatus(byte status, CallbackInfo ci) {
      class_1309 entity = (class_1309)(Object)this;
      if (entity instanceof class_1657 player) {
         ShieldTracker.handleEntityStatus(player, status);
      }

   }
}
