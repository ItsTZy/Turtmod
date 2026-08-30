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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hide-particles. class_702 = ParticleManager; method_3056 = createParticle, which CREATES the particle
 * (method_3055) then ADDS it to the render list (method_3058) and returns it.
 *
 * <p><b>True hide</b> ("Hide All" or a per-id match): we {@code @Redirect} the {@code method_3058} add-call and
 * simply skip it — the particle is created and still RETURNED (non-null), so vanilla code that dereferences the
 * result (e.g. the firework spark particle spawning its explosion sub-particles) doesn't NPE, but the particle
 * is never added to the render list, so it never shows.
 *
 * <p><b>Fast disappear</b> ({@code particlesFast}): particles that aren't fully hidden are marked dead right
 * after creation ({@code method_3085}) so they fade out almost immediately — a "quick particles" look.
 */
@Mixin(class_702.class)
public class HideParticlesMixin {
   @Redirect(
      method = "method_3056",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_702;method_3058(Lnet/minecraft/class_703;)V")
   )
   private void turtmod$maybeSkipAdd(class_702 manager, class_703 particle,
                                     class_2394 effect, double x, double y, double z, double vx, double vy, double vz) {
      if (turtmod$shouldHide(effect) || turtmod$shouldHideClearView(effect)) {
         return;   // don't add to the render list → fully hidden (but method_3056 still returns the particle)
      }
      manager.method_3058(particle);
   }

   @Inject(method = "method_3056", at = @At("RETURN"))
   private void turtmod$fastDisappear(class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.hideParticlesEnabled || !cfg.misc.particlesFast) {
         return;
      }
      class_703 created = cir.getReturnValue();
      if (created != null && !turtmod$shouldHide(effect)) {
         // Shorten this particle's lifetime to the configured % (slider) so it fades out quickly.
         ParticleAgeAccessor acc = (ParticleAgeAccessor) (Object) created;
         int max = acc.turtmod$getMaxAge();
         if (max > 0) {
            int pct = Math.max(0, Math.min(100, cfg.misc.particleLifePercent));
            acc.turtmod$setMaxAge(Math.max(0, Math.round((float) max * pct / 100.0F)));
         }
      }
   }

   private static boolean turtmod$shouldHide(class_2394 effect) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.hideParticlesEnabled) {
         return false;
      }
      if (cfg.misc.hideParticles) {
         return true;
      }
      if (!cfg.misc.hiddenParticleIds.isEmpty() && effect != null) {
         class_2960 id = class_7923.field_41180.method_10221(effect.method_10295());
         return id != null && cfg.misc.hiddenParticleIds.contains(id.toString());
      }
      return false;
   }

   /**
    * Clear View's crit/enchant-hit hiding. Independent of the Hide Particles module so it works on its own.
    * crit = the stars on a critical hit; enchanted_hit = the cyan Sharpness/enchant sparks. These particles
    * are server-spawned, so this hides the particle TYPE (can't isolate your own from another player's).
    */
   private static boolean turtmod$shouldHideClearView(class_2394 effect) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.clearViewEnabled || effect == null) {
         return false;
      }
      if (!cfg.misc.clearViewHideCritParticles && !cfg.misc.clearViewHideEnchantHitParticles) {
         return false;
      }
      class_2960 id = class_7923.field_41180.method_10221(effect.method_10295());
      if (id == null) {
         return false;
      }
      String path = id.method_12832();
      return (cfg.misc.clearViewHideCritParticles && "crit".equals(path))
         || (cfg.misc.clearViewHideEnchantHitParticles && "enchanted_hit".equals(path));
   }
}
