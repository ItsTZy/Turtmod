package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_2561;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Title Tweaks: hide, scale, or reposition the server on-screen Title + Subtitle. Wraps the vanilla title
 * render ({@code class_329.method_55801}) — pushes a scale/offset matrix at HEAD (only when a title is
 * actually showing, so the stack stays balanced) and pops it at TAIL; cancels the whole thing to hide.
 */
@Mixin(class_329.class)
public abstract class TitleTweaksMixin {
   @Shadow private class_2561 field_2016;   // current title text (null when none)
   @Shadow private int field_2023;          // title remaining ticks

   private boolean turtmod$pushedTitle;

   @Inject(method = "method_55801", at = @At("HEAD"), cancellable = true)
   private void turtmod$titleHead(class_332 ctx, class_9779 tick, CallbackInfo ci) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.hud.titleTweaksEnabled) {
         return;
      }
      if (this.field_2016 == null || this.field_2023 <= 0) {
         return;   // no title on screen — let vanilla take its own early return
      }
      if (cfg.hud.titleHide) {
         ci.cancel();
         return;
      }
      float s = cfg.hud.titleScalePercent / 100f;
      if (s == 1f && cfg.hud.titleOffsetX == 0 && cfg.hud.titleOffsetY == 0) {
         return;   // nothing to transform
      }
      int w = ctx.method_51421(), h = ctx.method_51443();
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(w / 2f + cfg.hud.titleOffsetX, h / 2f + cfg.hud.titleOffsetY);
      ctx.method_51448().scale(s, s);
      ctx.method_51448().translate(-w / 2f, -h / 2f);
      this.turtmod$pushedTitle = true;
   }

   @Inject(method = "method_55801", at = @At("TAIL"))
   private void turtmod$titleTail(class_332 ctx, class_9779 tick, CallbackInfo ci) {
      if (this.turtmod$pushedTitle) {
         ctx.method_51448().popMatrix();
         this.turtmod$pushedTitle = false;
      }
   }
}
