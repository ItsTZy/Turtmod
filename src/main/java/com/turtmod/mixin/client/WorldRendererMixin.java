package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.GradientKeys;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.GradientBoxRenderer;
import net.minecraft.class_265;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_761;
import net.minecraft.class_9974;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

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

   /**
    * Block-outline gradient. Vanilla draws the outline via {@code class_9974.method_62296(..., int color, width)}
    * — a single flat colour, so a gradient is impossible through the colour int. When the outline colour is a
    * gradient we redirect that call and draw the voxel-shape edges ourselves with a vertical per-vertex blend
    * (bottom stop → top stop), static, no animation. Otherwise we pass straight through to vanilla.
    */
   @Redirect(
      method = {"method_22712"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_9974;method_62296(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;Lnet/minecraft/class_265;DDDIF)V"),
      require = 0
   )
   private static void turtmod$outlineGradient(class_4587 matrices, class_4588 vc, class_265 shape, double dx, double dy, double dz, int color, float width) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg != null && cfg.misc.enabled && cfg.visual.recolorBlockOutline && !cfg.visual.blockOutlineRainbow && cfg.gradients != null) {
         TurtModConfig.GradientDef g = cfg.gradients.get(GradientKeys.BLOCK_OUTLINE);
         if (g != null && g.isGradient()) {
            GradientBoxRenderer.drawShapeGradient(matrices, vc, shape, dx, dy, dz, g, width);
            return;
         }
      }
      class_9974.method_62296(matrices, vc, shape, dx, dy, dz, color, width);
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
