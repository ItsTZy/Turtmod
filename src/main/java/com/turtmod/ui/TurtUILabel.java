package com.turtmod.ui;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.class_327;
import net.minecraft.class_332;

public class TurtUILabel {
   public final int x;
   public final int y;
   public final class_327 textRenderer;
   public final String text;
   public final boolean centered;
   public final TurtUITheme theme;
   public final boolean gradient;
   public final boolean bold;
   public final boolean shadow;

   public TurtUILabel(int x, int y, class_327 textRenderer, String text, TurtUITheme theme, boolean gradient, boolean centered) {
      this(x, y, textRenderer, text, theme, gradient, centered, false, false);
   }

   public TurtUILabel(int x, int y, class_327 textRenderer, String text, TurtUITheme theme, boolean gradient, boolean centered, boolean bold, boolean shadow) {
      this.x = x;
      this.y = y;
      this.textRenderer = textRenderer;
      this.text = text;
      this.centered = centered;
      this.theme = theme;
      this.gradient = gradient;
      this.bold = bold;
      this.shadow = shadow;
   }

   public void render(class_332 context, int mx, int my) {
      Color base = this.theme.text();
      if (base.getRed() < 100 && base.getGreen() < 100 && base.getBlue() < 100) {
         base = new Color(16777215);
      }

      if (this.gradient) {
         TurtUIUtils.drawGradientText(context, this.textRenderer, this.text, this.x, this.y, base.brighter(), base.darker(), this.centered, this.bold);
      } else {
         TurtUIUtils.drawText(context, this.textRenderer, this.text, this.x, this.y, base, this.centered, this.shadow, this.bold);
      }

   }

   public int getWidth() {
      return this.textRenderer.method_1727(this.text);
   }

   public int getHeight() {
      Objects.requireNonNull(this.textRenderer);
      return 9;
   }
}
