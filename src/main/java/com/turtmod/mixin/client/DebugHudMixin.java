package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CleanF3Feature;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_340;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_340.class})
public abstract class DebugHudMixin {
   @Shadow
   @Final
   private class_310 field_2079;

   @Inject(
      method = {"method_1846"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$cleanF3(class_332 context, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      // Gate on method_72776() — the actual F3 debug *text screen* toggle — NOT method_53536(),
      // which also returns true whenever a debug *overlay* (F3+B hitboxes, F3+G chunk borders…)
      // is active. Using method_53536() made Clean F3 pop up the moment you pressed F3+B, and kept
      // it stuck on while hitboxes were enabled (a plain F3 couldn't dismiss it). Tying it to the
      // text-screen toggle makes F3 behave normally and keeps hitboxes independent.
      if (config != null && config.misc.enabled && config.hud.cleanF3Mode && this.field_2079.field_1724 != null && !this.field_2079.field_1690.field_1842 && this.field_2079.field_61504.method_72776()) {
         CleanF3Feature.render(context, this.field_2079, config);
         ci.cancel();
      }
   }
}
