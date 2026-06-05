package com.turtmod.ui;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;

/**
 * A vanilla-style button that displays the TurtMod logo instead of text.
 * Used for the Hub launcher buttons on the title and pause screens.
 * Extends the concrete button (class_12231) so renderWidget is implemented.
 */
public class TurtLogoButton extends class_4185.class_12231 {

   public TurtLogoButton(int x, int y, int w, int h, class_4185.class_4241 onPress) {
      super(x, y, w, h, class_2561.method_43473(), onPress, field_40754);
   }

   @Override
   protected void method_75752(class_332 ctx, int mouseX, int mouseY, float delta) {
      // Draw the vanilla button background/hover first
      super.method_75752(ctx, mouseX, mouseY, delta);
      // Draw the logo centred inside, with a small inset
      int inset = 3;
      BrandingRenderer.drawLogo(ctx,
         this.method_46426() + inset, this.method_46427() + inset,
         this.method_25368() - inset * 2, this.method_25364() - inset * 2);
   }
}
