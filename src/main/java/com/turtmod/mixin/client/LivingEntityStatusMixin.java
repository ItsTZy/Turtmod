package com.turtmod.mixin.client;

import com.turtmod.combat.TotemPopTracker;
import com.turtmod.utils.ShieldTracker;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1309.class})
public class LivingEntityStatusMixin {
   // Vanilla entity-event id for the totem-of-undying "pop" (spawns the totem particles).
   private static final byte TOTEM_POP_STATUS = 35;

   @Inject(
      method = {"method_5711"},
      at = {@At("HEAD")}
   )
   private void turtmod$onHandleStatus(byte status, CallbackInfo ci) {
      class_1309 entity = (class_1309)(Object)this;
      if (entity instanceof class_1657 player) {
         ShieldTracker.handleEntityStatus(player, status);
         // Count a totem pop for this player (local + remote) — the server broadcasts entity-event 35
         // to every tracking client, so this fires uniformly for you and your opponents. Mirrors
         // uku3lig's TotemCounter, plus the local player so the "show pops" HUD mode works too.
         if (status == TOTEM_POP_STATUS) {
            TotemPopTracker.increment(player.method_5667());
         }
      }

   }
}
