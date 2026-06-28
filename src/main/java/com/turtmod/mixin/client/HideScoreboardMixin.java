package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_266;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_2561;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Scoreboard Tweaks (and full hide). Hooks the sidebar render ({@code class_329.method_1757}):
 * <ul>
 *   <li>full hide — cancels the render;</li>
 *   <li>hide numbers — skips the right-aligned score draw ({@code comp_2132}, the 3rd text draw);</li>
 *   <li>hide background — skips both {@code method_25294} fills;</li>
 *   <li>scale + X/Y offset — pushes a 2D matrix anchored to the screen's right edge.</li>
 * </ul>
 */
@Mixin({class_329.class})
public abstract class HideScoreboardMixin {
   private static boolean turtmod$pushed = false;

   @Inject(
      method = {"method_55803"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$hideScoreboardRoot(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.hideScoreboard) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1757"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$scoreboardHead(class_332 context, class_266 objective, CallbackInfo ci) {
      turtmod$pushed = false;
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) {
         return;
      }
      if (config.visual.hideScoreboard) {
         ci.cancel();
         return;
      }
      int scalePct = config.visual.scoreboardScalePercent;
      float s = scalePct <= 0 ? 1.0F : scalePct / 100.0F;
      int ox = config.visual.scoreboardOffsetX;
      int oy = config.visual.scoreboardOffsetY;
      if (s != 1.0F || ox != 0 || oy != 0) {
         // Anchor scaling to the right edge so the sidebar stays attached to the right side.
         int sw = context.method_51421();
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)(sw + ox), (float)oy);
         context.method_51448().scale(s, s);
         context.method_51448().translate((float)(-sw), 0.0F);
         turtmod$pushed = true;
      }
   }

   @Inject(
      method = {"method_1757"},
      at = {@At("RETURN")},
      require = 0
   )
   private void turtmod$scoreboardReturn(class_332 context, class_266 objective, CallbackInfo ci) {
      if (turtmod$pushed) {
         context.method_51448().popMatrix();
         turtmod$pushed = false;
      }
   }

   // Hide background: skip both translucent fills.
   @Redirect(
      method = {"method_1757"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_25294(IIIII)V"),
      require = 0
   )
   private void turtmod$scoreboardBackground(class_332 ctx, int x1, int y1, int x2, int y2, int color) {
      com.turtmod.hud.HudPanelsFeature.captureScoreboard(x1, y1, x2, y2); // feed HUD-editor sync
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.scoreboardHideBackground) {
         return;
      }
      ctx.method_25294(x1, y1, x2, y2, color);
   }

   // Hide numbers: skip the right-aligned score draw (3rd method_51439: title, name, score).
   @Redirect(
      method = {"method_1757"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V", ordinal = 2),
      require = 0
   )
   private void turtmod$scoreboardNumber(class_332 ctx, class_327 font, class_2561 text, int x, int y, int color, boolean shadow) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.scoreboardHideNumbers) {
         return;
      }
      ctx.method_51439(font, text, x, y, color, shadow);
   }
}
