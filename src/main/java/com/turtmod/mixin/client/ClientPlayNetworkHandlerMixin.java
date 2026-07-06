package com.turtmod.mixin.client;

import com.turtmod.combat.TotemPopTracker;
import com.turtmod.utils.ShieldTracker;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2663;
import net.minecraft.class_2767;
import net.minecraft.class_310;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_634.class})
public class ClientPlayNetworkHandlerMixin {
   @Inject(
      method = {"method_11146"},
      at = {@At("HEAD")},
      require = 0
   )
   private void turtmod$onPlaySound(class_2767 packet, CallbackInfo ci) {
      String id = packet.method_11894().method_55840();
      if (id != null && id.toLowerCase().contains("shield.break")) {
         ShieldTracker.handleBreakSound(packet.method_11890(), packet.method_11889(), packet.method_11893());
      }

   }

   @Inject(
      method = {"method_11148"},
      at = {@At("HEAD")},
      require = 0
   )
   private void turtmod$onEntityStatus(class_2663 packet, CallbackInfo ci) {
      if (class_310.method_1551().field_1687 != null) {
         byte status = packet.method_11470();
         class_1297 entity = packet.method_11469(class_310.method_1551().field_1687);
         if (status == 30 && entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            ShieldTracker.handleEntityStatus(player, status);
            com.turtmod.combat.ShieldFixFeature.onDisable(player);
         }

         if (status == 35 && entity instanceof class_1657) {
            TotemPopTracker.increment(((class_1657)entity).method_5667());
         }

         // status 3 = entity death — reset that player's pop count so it's per-life
         if (status == 3 && entity instanceof class_1657) {
            TotemPopTracker.remove(((class_1657)entity).method_5667());
         }

      }
   }
}
