package com.turtmod.mixin.client;

import com.turtmod.ui.BrandingRenderer;
import com.turtmod.ui.TurtUIUtils;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.awt.Color;

@Mixin({class_442.class})
public abstract class TitleScreenVisualsMixin {
   @Inject(method = {"method_25394"}, at = {@At("TAIL")})
   private void turtmod$renderBranding(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      int sw = context.method_51421();
      int sh = context.method_51443();
      java.awt.Color green = new java.awt.Color(9289311, false);
      java.awt.Color pink  = new java.awt.Color(16752046, false);

      // ── Clean top-left brand panel (no glow) ──
      int logoSz = 40;
      int pad = 12;
      int bx = pad;
      int by = pad;

      // Logo
      BrandingRenderer.drawLogo(context, bx, by, logoSz, logoSz);

      // "TurtMod" gradient title to the right of logo
      TurtUIUtils.drawGradientText(context, class_310.method_1551().field_1772,
         "TurtMod", bx + logoSz + 8, by + 8, green, pink, false, true);
      // Subtitle
      context.method_25303(class_310.method_1551().field_1772, "Made by Tzy  •  v1.21.11",
         bx + logoSz + 8, by + 20, new java.awt.Color(200, 200, 200, 220).getRGB());
      // Accent underline
      context.method_25294(bx + logoSz + 8, by + 32, bx + logoSz + 8 + 70, by + 33, green.getRGB());
   }
}
