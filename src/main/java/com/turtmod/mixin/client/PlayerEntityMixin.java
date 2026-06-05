package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.turtmod.combat.TotemPopTracker;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_1657.class})
public abstract class PlayerEntityMixin {
   @ModifyReturnValue(
      method = {"method_5476"},
      at = {@At("RETURN")}
   )
   private class_2561 turtmod$appendTotemPops(class_2561 original) {
      return TotemPopTracker.appendPops((class_1657)(Object)this, original);
   }
}
