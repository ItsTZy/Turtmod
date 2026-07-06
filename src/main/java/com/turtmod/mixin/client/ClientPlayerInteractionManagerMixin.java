package com.turtmod.mixin.client;

import com.turtmod.hud.ReachDisplayFeature;
import com.turtmod.utils.ShieldTracker;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_636.class})
public class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"method_2918"},
      at = {@At("HEAD")}
   )
   private void turtmod$onAttackEntity(class_1657 player, class_1297 target, CallbackInfo ci) {
      if (target instanceof class_1657 targetPlayer) {
         ShieldTracker.handlePlayerAttack(targetPlayer);
         com.turtmod.combat.ShieldFixFeature.onAttack(targetPlayer);
      }

      // Feed the reach display (its own PlayerEntityAttackMixin was never registered, so the
      // reach number never updated). attackEntity is the client-side attack path.
      class_310 client = class_310.method_1551();
      if (client != null && player == client.field_1724) {
         ReachDisplayFeature.onAttackEntity(client, target);
      }

   }
}
