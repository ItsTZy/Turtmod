package com.turtmod.mixin.client;

import com.turtmod.hud.ReachDisplayFeature;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1657.class})
public abstract class PlayerEntityAttackMixin {
   @Inject(
      method = {"method_7324"},
      at = {@At("HEAD")}
   )
   private void turtmod$trackAttack(class_1297 target, CallbackInfo ci) {
      class_310 client = class_310.method_1551();
      if (client != null && (Object)this == client.field_1724) {
         ReachDisplayFeature.onAttackEntity(client, target);
      }

   }
}
