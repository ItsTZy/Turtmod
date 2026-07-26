package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

/**
 * Clean F3 that KEEPS the profiler pie / FPS charts. The vanilla debug render ({@code class_340.method_1846})
 * draws its two text columns via {@code method_51745}, then draws the charts. We wrap those two text-draw
 * calls with MixinExtras {@link WrapOperation} — which CHAINS with other mods (e.g. ImmediatelyFast) that
 * touch the same calls, instead of clashing like {@code @Redirect} did (that crashed on load). When Clean F3
 * is active we draw our compact text and skip the vanilla text; the charts (drawn afterwards) are untouched.
 * {@code require = 0}: if another mod fully claims the call, this silently no-ops instead of crashing.
 */
@Mixin({class_340.class})
public abstract class DebugHudMixin {
   @Shadow
   @Final
   private class_310 field_2079;

   @WrapOperation(
      method = "method_1846",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_340;method_51745(Lnet/minecraft/class_332;Ljava/util/List;Z)V"),
      require = 0
   )
   private void turtmod$cleanF3Text(class_340 self, class_332 ctx, List<String> lines, boolean left, Operation<Void> original) {
      TurtModConfig config = TurtModClient.getConfig();
      if (turtmod$cleanActive(config)) {
         if (left) {   // draw our compact text once, on the left column's call; skip both vanilla columns
            CleanF3Feature.render(ctx, this.field_2079, config);
         }
         return;
      }
      original.call(self, ctx, lines, left);
   }

   private boolean turtmod$cleanActive(TurtModConfig config) {
      // Gate on method_72776() — the F3 debug *text* toggle — not the debug-overlay flag, so F3+B etc.
      // never triggers Clean F3 and a plain F3 dismisses it normally.
      return config != null && config.misc.enabled && config.hud.cleanF3Mode
         && this.field_2079.field_1724 != null && !this.field_2079.field_1690.field_1842
         && this.field_2079.field_61504.method_72776();
   }
}
