package com.turtmod.mixin.client;

import com.turtmod.hud.ReachDisplayFeature;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_746.class})
public abstract class ClientPlayerEntityBlockBreakMixin {
   @Inject(
      method = {"method_5773"},
      at = {@At("HEAD")}
   )
   private void turtmod$trackBlockBreakReach(CallbackInfo ci) {
      class_746 player = (class_746)(Object)this;
      class_310 client = class_310.method_1551();
      if (client != null && player.method_6115()) {
         class_239 var5 = client.field_1765;
         if (var5 instanceof class_3965) {
            class_3965 blockHit = (class_3965)var5;
            ReachDisplayFeature.onBlockBreak(client, blockHit.method_17784());
         }
      }

   }
}
