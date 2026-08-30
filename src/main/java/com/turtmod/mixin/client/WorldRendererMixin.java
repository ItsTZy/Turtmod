package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_761.class})
public abstract class WorldRendererMixin {
   @ModifyVariable(
      method = {"method_22712"},
      at = @At("HEAD"),
      argsOnly = true,
      require = 0
   )
   private static int turtmod$recolorBlockOutline(int originalColor) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.visual.recolorBlockOutline) return originalColor;
      int alpha = cfg.visual.blockOutlineAlpha;
      if (alpha < 0 || alpha > 255) alpha = 255;
      int rgb;
      if (cfg.visual.blockOutlineRainbow) {
         // Animated chroma hue (ported from CustomBlockHighlight's rainbow outline).
         float speed = Math.max(0.1F, cfg.visual.blockOutlineRainbowSpeed);
         float hue = (float)((System.currentTimeMillis() % (long)(6000.0F / speed * 6.0F)) / (double)(6000.0F / speed * 6.0F));
         rgb = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0xFFFFFF;
      } else {
         rgb = cfg.visual.blockOutlineColor & 0xFFFFFF;
      }
      return (alpha << 24) | rgb;
   }

   @ModifyArg(
      method = {"method_22712"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_9782;method_61102(F)V", ordinal = 0),
      require = 0
   )
   private static float turtmod$modifyOutlineWidth(float originalWidth) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled) return originalWidth;
      float width = cfg.visual.blockOutlineWidth;
      return width > 0.0F ? width : originalWidth;
   }
}
