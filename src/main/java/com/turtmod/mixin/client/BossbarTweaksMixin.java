package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import java.util.Map;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_337;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bossbar Tweaks: hide the server boss bars, hide them only while F3 is open, or scale/reposition them.
 * Hooks the boss-health overlay render ({@code class_337.method_1796}, called from {@code class_329}).
 * The matrix push at HEAD is gated on there being at least one boss (so it matches the method's own empty
 * early-return) and popped at TAIL, keeping the stack balanced.
 */
@Mixin(class_337.class)
public abstract class BossbarTweaksMixin {
   @Shadow @SuppressWarnings("rawtypes") private Map field_2060;   // active boss events

   private boolean turtmod$pushedBoss;

   @Inject(method = "method_1796", at = @At("HEAD"), cancellable = true)
   private void turtmod$bossHead(class_332 ctx, CallbackInfo ci) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.hud.bossbarTweaksEnabled) {
         return;
      }
      if (this.field_2060 == null || this.field_2060.isEmpty()) {
         return;   // no bosses — let vanilla take its own early return
      }
      if (cfg.hud.bossbarHide) {
         ci.cancel();
         return;
      }
      if (cfg.hud.bossbarHideInF3) {
         class_310 mc = class_310.method_1551();
         if (mc != null && mc.field_61504 != null && mc.field_61504.method_72776()) {
            ci.cancel();
            return;
         }
      }
      float s = cfg.hud.bossbarScalePercent / 100f;
      if (s == 1f && cfg.hud.bossbarOffsetX == 0 && cfg.hud.bossbarOffsetY == 0) {
         return;   // nothing to transform
      }
      int w = ctx.method_51421();
      // Boss bars anchor to the top-centre; scale around top-centre + apply the offset.
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(w / 2f + cfg.hud.bossbarOffsetX, cfg.hud.bossbarOffsetY);
      ctx.method_51448().scale(s, s);
      ctx.method_51448().translate(-w / 2f, 0f);
      this.turtmod$pushedBoss = true;
   }

   @Inject(method = "method_1796", at = @At("TAIL"))
   private void turtmod$bossTail(class_332 ctx, CallbackInfo ci) {
      if (this.turtmod$pushedBoss) {
         ctx.method_51448().popMatrix();
         this.turtmod$pushedBoss = false;
      }
   }
}
