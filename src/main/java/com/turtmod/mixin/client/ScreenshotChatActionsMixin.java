package com.turtmod.mixin.client;

import com.turtmod.chat.BetterScreenshotFeature;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_338.class})
public abstract class ScreenshotChatActionsMixin {
   @Shadow
   public abstract void method_44811(class_2561 var1, @Nullable class_7469 var2, @Nullable class_7591 var3);

   @Inject(
      method = {"method_44811"},
      at = {@At("TAIL")}
   )
   private void turtmod$addScreenshotActions(class_2561 message, @Nullable class_7469 signature, @Nullable class_7591 indicator, CallbackInfo ci) {
      class_2561 actions = BetterScreenshotFeature.buildActionMessage(message);
      if (actions != null) {
         this.method_44811(actions, (class_7469)null, class_7591.method_44751());
      }

   }
}
