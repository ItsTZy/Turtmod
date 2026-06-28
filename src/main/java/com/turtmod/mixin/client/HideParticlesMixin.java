package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_2394;
import net.minecraft.class_2960;
import net.minecraft.class_702;
import net.minecraft.class_703;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hide-particles (ported from fireclient): cancels particle creation at the source. "Hide All" is the
 * master switch; otherwise only registry ids in {@code hiddenParticleIds} (managed via {@code /turtmod
 * particle}) are cancelled. class_702 = ParticleManager, method_3056 = createParticle.
 */
@Mixin(class_702.class)
public class HideParticlesMixin {
   @Inject(method = "method_3056", at = @At("HEAD"), cancellable = true)
   private void turtmod$hideParticles(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.hideParticlesEnabled) {
         return;
      }
      if (cfg.misc.hideParticles) {
         cir.setReturnValue(null);
         return;
      }
      if (!cfg.misc.hiddenParticleIds.isEmpty() && effect != null) {
         class_2960 id = class_7923.field_41180.method_10221(effect.method_10295());
         if (id != null && cfg.misc.hiddenParticleIds.contains(id.toString())) {
            cir.setReturnValue(null);
         }
      }
   }
}
