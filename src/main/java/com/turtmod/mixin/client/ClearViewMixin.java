package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2394;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clear View — a cleaner first-person look, affecting ONLY the local player (others / third-person keep
 * their particles). Two things:
 *  - Skips the player's own potion "swirl" particles (spawned in {@code class_1309.method_6050}).
 *  - Trims or removes the eating "crumbs" ({@code class_1309.method_6037}).
 */
@Mixin(class_1309.class)
public abstract class ClearViewMixin {

   private boolean turtmod$isSelf() {
      class_310 mc = class_310.method_1551();
      return mc != null && (Object) this == mc.field_1724;
   }

   private static boolean turtmod$on(TurtModConfig cfg) {
      return cfg != null && cfg.misc.enabled && cfg.misc.clearViewEnabled;
   }

   /** Drop the local player's own potion swirl emission (the {@code level.addParticle} call). */
   @Redirect(
      method = "method_6050",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1937;method_8406(Lnet/minecraft/class_2394;DDDDDD)V"),
      require = 0
   )
   private void turtmod$hideOwnPotionParticles(class_1937 level, class_2394 particle, double x, double y, double z, double vx, double vy, double vz) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (turtmod$on(cfg) && cfg.misc.clearViewHidePotionParticles && turtmod$isSelf()) {
         return;   // skip your own swirls
      }
      level.method_8406(particle, x, y, z, vx, vy, vz);
   }

   /** Cancel eating crumbs entirely (local player) when "hide" is on. */
   @Inject(method = "method_6037", at = @At("HEAD"), cancellable = true, require = 0)
   private void turtmod$hideEatingParticles(net.minecraft.class_1799 stack, int count, CallbackInfo ci) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (turtmod$on(cfg) && cfg.misc.clearViewHideEatingParticles && turtmod$isSelf()) {
         ci.cancel();
      }
   }

   /** Otherwise trim the crumb count so they stay but are far fewer / less visible (local player). */
   @ModifyVariable(method = "method_6037", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 0)
   private int turtmod$reduceEatingParticles(int count) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (turtmod$on(cfg) && cfg.misc.clearViewReduceEatingParticles && !cfg.misc.clearViewHideEatingParticles && turtmod$isSelf()) {
         return Math.max(1, count / 4);
      }
      return count;
   }
}
