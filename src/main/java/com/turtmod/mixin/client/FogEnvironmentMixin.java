package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_11398;
import net.minecraft.class_11401;
import net.minecraft.class_11402;
import net.minecraft.class_11403;
import net.minecraft.class_1309;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7283;
import net.minecraft.class_7284;
import net.minecraft.class_7285;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Per-environment fog control for 1.21.11. Each fog kind is its own FogEnvironment
 * (class_11400) implementation; setupFog == method_42591. Pushing every distance
 * field past the far plane removes that environment's fog entirely.
 *
 * Verified mapping against decompiled method_42593 / method_42594 (1.21.11):
 *   class_11401 = Lava        (CameraSubmersionType field_27885)
 *   class_11402 = PowderSnow  (CameraSubmersionType field_27887)
 *   class_11403 = Water       (CameraSubmersionType field_27886)
 *   class_7283  = Blindness   (MobEffect class_1294.field_5919)
 *   class_7284  = Darkness    (MobEffect class_1294.field_38092; darkens via method_42592)
 *   class_11398 = Atmospheric (always applicable — normal distance fog)
 */
@Mixin({class_11401.class, class_11402.class, class_7283.class, class_7284.class, class_11403.class, class_11398.class})
public abstract class FogEnvironmentMixin {
   @Inject(
      method = {"method_42591"},
      at = {@At("TAIL")},
      require = 0
   )
   private void turtmod$customizeFog(class_7285 fog, class_4184 camera, class_638 level, float renderDistance, class_9779 tick, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) {
         return;
      }
      Object self = this;
      boolean disable;
      if (self instanceof class_11401) {
         disable = config.visual.disableLavaFog;
      } else if (self instanceof class_11402) {
         disable = config.visual.disablePowderSnowFog;
      } else if (self instanceof class_11403) {
         disable = config.visual.disableWaterFog;
      } else if (self instanceof class_7283) {
         disable = config.visual.disableBlindnessFog;
      } else if (self instanceof class_7284) {
         disable = config.visual.disableDarknessOverlay;
      } else if (self instanceof class_11398) {
         disable = config.visual.disableAllFog || config.visual.disableAtmosphericFog;
      } else {
         return;
      }
      if (disable) {
         fog.field_60582 = Float.MAX_VALUE;
         fog.field_60583 = Float.MAX_VALUE;
         fog.field_60584 = Float.MAX_VALUE;
         fog.field_60585 = Float.MAX_VALUE;
      } else if (self instanceof class_11398 && config.visual.fogDensityPercent != 100) {
         // Density style: scale atmospheric fog distances. >100% = closer/thicker, <100% = farther/thinner.
         float f = 100.0F / (float)Math.max(1, config.visual.fogDensityPercent);
         fog.field_60582 *= f;
         fog.field_60583 *= f;
         fog.field_60584 *= f;
         fog.field_60585 *= f;
      }
   }

   // Darkness (class_7284) also dims the whole screen via method_42592. Cancel that
   // multiplier so "Disable Darkness" actually clears the Warden/sculk blackout.
   @Inject(
      method = {"method_42592"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$noDarkening(class_1309 entity, float f, float g, CallbackInfoReturnable<Float> cir) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.disableDarknessOverlay
            && (Object)this instanceof class_7284) {
         cir.setReturnValue(f);
      }
   }
}
