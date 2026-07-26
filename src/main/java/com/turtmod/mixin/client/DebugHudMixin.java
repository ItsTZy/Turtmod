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

/**
 * Clean F3: replaces the vanilla debug overlay with our compact version. Uses a simple HEAD-cancel inject on
 * the debug render ({@code class_340.method_1846}) — this is conflict-free in heavy modpacks (an earlier
 * attempt to @Redirect the two internal text-draw calls to preserve the profiler pie clashed with mods like
 * ImmediatelyFast that also touch those calls, which crashed on load). Trade-off: the vanilla profiler pie /
 * FPS charts are hidden while Clean F3 is on, same as before.
 */
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
      // Gate on method_72776() — the F3 debug *text* toggle — not the debug-overlay flag, so pressing
      // F3+B (hitboxes) etc. never triggers Clean F3 and a plain F3 dismisses it normally.
      if (config != null && config.misc.enabled && config.hud.cleanF3Mode && this.field_2079.field_1724 != null
            && !this.field_2079.field_1690.field_1842 && this.field_2079.field_61504.method_72776()) {
         CleanF3Feature.render(context, this.field_2079, config);
         ci.cancel();
      }
   }
}
