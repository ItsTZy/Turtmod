package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CleanF3Feature;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_340;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({class_340.class})
public abstract class DebugHudMixin {
   @Shadow
   @Final
   private class_310 field_2079;

   @Shadow
   protected abstract void method_51745(class_332 arg, List<String> list, boolean bl);

   /**
    * Clean F3 replaces only the debug TEXT, not the whole debug render. The vanilla {@code method_1846}
    * draws its two text columns via {@link #method_51745} and then draws the FPS/TPS/bandwidth charts and
    * the profiler pie. Cancelling the whole method (the old approach) also removed those charts — the user
    * lost the pie chart. Instead we redirect just the two text-draw calls: when Clean F3 is active we skip
    * the vanilla text and draw our own (once), leaving every chart to render normally.
    */
   @Redirect(
      method = "method_1846",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_340;method_51745(Lnet/minecraft/class_332;Ljava/util/List;Z)V")
   )
   private void turtmod$cleanF3Text(class_340 self, class_332 context, List<String> list, boolean left) {
      TurtModConfig config = TurtModClient.getConfig();
      if (turtmod$cleanActive(config)) {
         if (left) {   // draw our clean text once, on the left column's call
            CleanF3Feature.render(context, this.field_2079, config);
         }
         return;   // skip the vanilla text so only the clean text shows (charts still render after)
      }
      this.method_51745(context, list, left);   // normal vanilla text
   }

   private boolean turtmod$cleanActive(TurtModConfig config) {
      // Gate on method_72776() — the F3 debug *text* toggle — not the debug-overlay flag, so pressing
      // F3+B (hitboxes) etc. never triggers Clean F3 and a plain F3 dismisses it normally.
      return config != null && config.misc.enabled && config.hud.cleanF3Mode
         && this.field_2079.field_1724 != null && !this.field_2079.field_1690.field_1842
         && this.field_2079.field_61504.method_72776();
   }
}
