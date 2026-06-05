package com.turtmod.ui;

import java.awt.Color;
import net.minecraft.class_332;

public class TurtUIPanel {
   public final int x;
   public final int y;
   public final int width;
   public final int height;
   public final TurtUITheme theme;
   public final boolean gradient;
   public final boolean glass;

   public TurtUIPanel(int x, int y, int width, int height, TurtUITheme theme, boolean gradient) {
      this(x, y, width, height, theme, gradient, false);
   }

   public TurtUIPanel(int x, int y, int width, int height, TurtUITheme theme, boolean gradient, boolean glass) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.theme = theme;
      this.gradient = gradient;
      this.glass = glass;
   }

   public void render(class_332 context, int mouseX, int mouseY) {
      // Soft drop shadow for depth (under every panel).
      TurtUIUtils.drawShadow(context, this.x, this.y, this.width, this.height, 6);
      if (this.glass) {
         TurtUIUtils.drawGlassPanel(context, this.x, this.y, this.width, this.height, this.theme.background());
         return;
      }
      Color baseBg = this.theme.background();
      Color baseBorder = this.theme.border();
      Color bgStart = this.gradient ? baseBg.brighter().brighter() : baseBg;
      Color borderStart = this.gradient ? baseBorder.brighter() : baseBorder;
      if (this.gradient) {
         TurtUIUtils.drawGradientRectangle(context, this.x, this.y, this.width, this.height, bgStart, baseBg);
         TurtUIUtils.drawGradientBorder(context, this.x, this.y, this.width, this.height, borderStart, baseBorder);
      } else {
         TurtUIUtils.drawRoundedRect(context, this.x, this.y, this.width, this.height, 4, baseBg);
         TurtUIUtils.drawBorder(context, this.x, this.y, this.width, this.height, baseBorder);
      }
   }

   public boolean isHovered(int mx, int my) {
      return TurtUIUtils.isHovered(mx, my, this.x, this.y, this.width, this.height);
   }
}
