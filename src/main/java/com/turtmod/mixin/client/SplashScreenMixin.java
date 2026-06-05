package com.turtmod.mixin.client;

import com.turtmod.ui.BrandingRenderer;
import com.turtmod.ui.TurtUIUtils;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_425;
import java.awt.Color;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_425.class})
public abstract class SplashScreenMixin {
   private float turtmod$fade = 0f;
   private long turtmod$lastNs = System.nanoTime();

   @Inject(method = {"method_25394"}, at = {@At("TAIL")})
   private void turtmod$renderLogoOnLoading(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      long now = System.nanoTime();
      float dt = Math.min((now - turtmod$lastNs) / 1_000_000_000f, 0.1f);
      turtmod$lastNs = now;
      turtmod$fade = TurtUIUtils.lerp01(turtmod$fade, 1f, dt, 6f);

      int w = context.method_51421();

      // Logo at the TOP of the loading screen
      int logoW = 76, logoH = 76;
      int lx = w / 2 - logoW / 2;
      int ly = 20;

      int alpha = (int)(turtmod$fade * 220);
      if (alpha <= 0) return;

      // Logo
      BrandingRenderer.drawLogo(context, lx, ly, logoW, logoH);

      // "TurtMod" gradient text below logo
      Color green = new Color(9289311, false);
      Color pink  = new Color(16752046, false);
      TurtUIUtils.drawGradientText(context, class_310.method_1551().field_1772,
         "TurtMod", w / 2, ly + logoH + 4, green, pink, true, true);

      // subtitle
      Color sub = new Color(180, 180, 180, alpha);
      context.method_25300(class_310.method_1551().field_1772, "Made by Tzy", w / 2, ly + logoH + 16, sub.getRGB());
   }
}
