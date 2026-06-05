package com.turtmod.mixin.client;

import com.turtmod.combat.PotionThrowTracker;
import com.turtmod.combat.TotemPopTracker;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_338.class})
public class TotemPopRoundEndMixin {
   @Inject(method = {"method_44811"}, at = {@At("HEAD")})
   private void turtmod$checkRoundEnd(class_2561 message, class_7469 signature, class_7591 indicator, CallbackInfo ci) {
      String text = message.getString();
      for (String trigger : TotemPopTracker.ROUND_END_MESSAGES) {
         if (text.contains(trigger)) {
            TotemPopTracker.reset();
            PotionThrowTracker.reset();
            return;
         }
      }
   }
}
