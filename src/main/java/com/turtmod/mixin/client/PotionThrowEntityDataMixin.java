package com.turtmod.mixin.client;

import com.turtmod.combat.PotionThrowTracker;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1686;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2739;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_9334;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_634.class})
public class PotionThrowEntityDataMixin {
   @Shadow
   private class_638 field_3699;

   @Unique
   private final Set<Integer> turtmod$processedPotions = new HashSet<>();

   @Inject(method = {"method_11093"}, at = {@At("TAIL")})
   private void turtmod$onEntityData(class_2739 packet, CallbackInfo ci) {
      if (this.field_3699 == null) return;
      class_1297 entity = this.field_3699.method_8469(packet.comp_1127());
      if (!(entity instanceof class_1686 potion)) return;
      if (turtmod$processedPotions.contains(entity.method_5628())) return;

      class_1799 stack = potion.method_7495();
      if (stack.method_7960()) return;

      class_1844 contents = (class_1844)stack.method_58695(class_9334.field_49651, class_1844.field_49274);
      if (contents == null) return;

      boolean isInstantHealth = false;
      for (class_1293 effect : contents.method_57397()) {
         class_1291 effectType = (class_1291)effect.method_5579().method_40229().right().orElse(null);
         if (effectType == class_1294.field_5915) {
            isInstantHealth = true;
            break;
         }
      }
      if (!isInstantHealth) return;

      turtmod$processedPotions.add(entity.method_5628());
      class_1297 owner = potion.method_24921();
      if (owner instanceof class_1657 player) {
         PotionThrowTracker.increment(player.method_5667());
      }
   }
}
