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
 * Hide-particles (ported from fireclient). "Hide All" is the master switch; otherwise only registry ids in
 * {@code hiddenParticleIds} (managed via {@code /turtmod particle}) are hidden. class_702 = ParticleManager,
 * method_3056 = createParticle.
 *
 * <p>We must NOT cancel {@code method_3056} and return {@code null}: vanilla code that spawns particles
 * internally dereferences the returned particle — e.g. the firework spark particle ({@code class_677}) creates
 * its explosion sub-particles this way and calls a method on the result, so a null return crashes the game the
 * moment a firework goes off. Instead we let the particle be created and immediately mark it dead
 * ({@code method_3085}) — it isn't rendered, but the caller still gets a valid (non-null) particle back.
 */
@Mixin(class_702.class)
public class HideParticlesMixin {
   @Inject(method = "method_3056", at = @At("RETURN"))
   private void turtmod$hideParticles(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.hideParticlesEnabled) {
         return;
      }
      class_703 created = cir.getReturnValue();
      if (created == null) {
         return;
      }
      boolean hide = cfg.misc.hideParticles;
      if (!hide && !cfg.misc.hiddenParticleIds.isEmpty() && effect != null) {
         class_2960 id = class_7923.field_41180.method_10221(effect.method_10295());
         hide = id != null && cfg.misc.hiddenParticleIds.contains(id.toString());
      }
      if (hide) {
         created.method_3085();   // mark dead → not rendered, but returned non-null so callers don't NPE
      }
   }
}
