package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.event.OverlayReloadListener;
import com.turtmod.utils.TurtLogger;
import com.turtmod.visual.HitColorConfig;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_4608;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4608.class})
public abstract class HitColorMixin implements OverlayReloadListener {
   @Shadow @Final
   private class_1043 field_21013;
   @Unique
   private static class_4608 turtmod$instance;
   @Unique
   private HitColorConfig hitColorConfig;
   @Unique
   private boolean turtmod$registered = false;

   @Inject(method = {"<init>"}, at = {@At("TAIL")}, require = 0)
   private void turtmod$initOverlay(CallbackInfo ci) {
      turtmod$instance = (class_4608)(Object)this;
      TurtLogger.info("[turtmod] HitColorMixin <init> fired");
      this.turtmod$ensureRegistered();
      this.turtmod$reloadOverlay();
   }

   public void turtmod$onOverlayReload() {
      this.turtmod$ensureRegistered();
      this.turtmod$reloadOverlay();
   }

   @Unique
   private void turtmod$ensureRegistered() {
      if (this.turtmod$registered) return;
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null) {
         this.hitColorConfig = config.visual.hitColor;
         OverlayReloadListener.register(this);
         this.turtmod$registered = true;
      }
   }

   @Unique
   private static int turtmod$getColorInt(int red, int green, int blue, int alpha) {
      alpha = 255 - alpha;
      return alpha << 24 | blue << 16 | green << 8 | red;
   }

   @Unique
   private void turtmod$reloadOverlay() {
      if (this.hitColorConfig == null) {
         TurtModConfig config = TurtModClient.getConfig();
         if (config == null) return;
         this.hitColorConfig = config.visual.hitColor;
      }

      // Respect the feature's own on/off switch: when Hit Color is disabled, leave the shared overlay
      // texture as vanilla so nothing else that samples it (e.g. the TNT/minecart white flash) is touched.
      if (!this.hitColorConfig.enabled) {
         return;
      }

      class_1011 nativeImage = this.field_21013 != null ? this.field_21013.method_4525() : null;
      if (nativeImage == null) {
         TurtLogger.info("[turtmod] HitColor reload skipped: nativeImage=null (texture=" + (this.field_21013 != null) + ")");
         return;
      }

      TurtLogger.info("[turtmod] HitColor reload: writing tint color=" + Integer.toHexString(this.hitColorConfig.getTintColor()));
      int color = this.hitColorConfig.getTintColor();
      int alpha = color >> 24 & 255;

      // When Hit Color is a gradient, paint the 8 top rows as a clean vertical multi-stop blend.
      // The gradient supplies the RGB per row; the flash's alpha stays whatever the tint computed.
      TurtModConfig cfg = TurtModClient.getConfig();
      TurtModConfig.GradientDef grad = (cfg != null && cfg.gradients != null)
         ? cfg.gradients.get(com.turtmod.config.GradientKeys.HIT_COLOR) : null;
      boolean gradient = grad != null && grad.isGradient();

      for (int i = 0; i < 8; ++i) {
         int rgb;
         if (gradient) {
            // colorAt clamps, so i=7 → t=1.0 → the last stop (clean top→bottom blend, no wrap-around).
            rgb = grad.colorAt((float) i / 7.0F);
         } else {
            rgb = color;
         }
         int red = rgb >> 16 & 255;
         int green = rgb >> 8 & 255;
         int blue = rgb & 255;
         for (int j = 0; j < 16; ++j) {
            nativeImage.method_4305(j, i, turtmod$getColorInt(red, green, blue, alpha));
         }
      }
      this.field_21013.method_4524();
   }
}
