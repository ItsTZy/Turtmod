package com.turtmod.ui;

import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;

public class TurtUISlider {
   public final int x;
   public final int y;
   public final int textX;
   public final int width;
   public final float min;
   public final float max;
   public float value;
   public final int gap;
   public final float precision;
   public final String text;
   public final String prefix;
   public final String suffix;
   public final class_327 textRenderer;
   private final float initialValue;
   public boolean dragging;
   public final Consumer<Float> onDrag;
   public final Consumer<Integer> onStop;
   public final TurtUITheme theme;
   public final boolean gradient;
   public final boolean toggle;

   public TurtUISlider(int x, int y, int textX, int width, float min, float max, float initial, int gap, float precision, String text, String prefix, String suffix, class_327 textRenderer, TurtUITheme theme, boolean gradient, boolean toggle, Consumer<Float> onDrag, Consumer<Integer> onStop) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.min = min;
      this.max = max;
      this.gap = gap;
      this.precision = precision;
      this.prefix = prefix;
      this.suffix = suffix;
      this.text = text;
      this.textX = textX;
      this.textRenderer = textRenderer;
      this.onDrag = onDrag;
      this.onStop = onStop;
      this.initialValue = initial;
      this.value = initial;
      this.theme = theme;
      this.gradient = gradient;
      this.toggle = toggle;
   }

   public void render(class_332 context, int mx, int my) {
      boolean hovered = mx >= this.x && mx <= this.x + this.width && my >= this.y - 3 && my <= this.y + 4;
      Color baseText = this.theme.text();
      Color baseTrack = this.theme.background();
      Color baseThumb = hovered ? this.theme.border().brighter() : this.theme.border();
      TurtUIUtils.drawHorizontalGradient(context, this.x, this.y - 1, this.width, 2, baseTrack, baseTrack);
      double clampedValue = (double)Math.max(this.min, Math.min(this.max, this.value));
      double normalized = (clampedValue - (double)this.min) / (double)(this.max - this.min);
      int tx = (int)((double)this.x + normalized * (double)(this.width - 2));
      TurtUIUtils.drawRectangle(context, tx, this.y - 4, 2, 8, baseThumb);
      boolean off = this.toggle && this.value <= this.min;
      String result = off ? "OFF" : this.prefix + (int)this.value + this.suffix;
      if (this.gradient && !off) {
         TurtUIUtils.drawGradientText(context, this.textRenderer, this.text, this.textX, this.y - 4, this.theme.highlighted().brighter(), this.theme.highlighted().darker(), false);
         TurtUIUtils.drawGradientText(context, this.textRenderer, result, this.x + this.width + this.gap, this.y - 4, this.theme.highlighted().brighter(), this.theme.highlighted().darker(), true);
      } else {
         TurtUIUtils.drawText(context, this.textRenderer, this.text, this.textX, this.y - 4, baseText, false, false);
         TurtUIUtils.drawText(context, this.textRenderer, result, this.x + this.width + this.gap, this.y - 4, baseText, true, false);
      }

   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (button == 0 && mx >= (double)this.x && mx <= (double)(this.x + this.width) && my >= (double)(this.y - 3) && my <= (double)(this.y + 4)) {
         this.dragging = true;
         this.updateValue(mx);
         this.onDrag.accept(this.value);
         this.playSound();
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
      if (this.dragging) {
         this.updateValue(mx);
         this.onDrag.accept(this.value);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double mx, double my, int button) {
      if (this.dragging && button == 0) {
         this.dragging = false;
         this.onStop.accept((int)this.value);
         return true;
      } else {
         return false;
      }
   }

   private void updateValue(double mx) {
      double fraction = Math.max((double)0.0F, Math.min((double)1.0F, (mx - (double)this.x) / ((double)this.width - (double)2.0F)));
      double raw = (double)this.min + fraction * (double)(this.max - this.min);
      this.value = Math.max(this.min, Math.min(this.max, (float)Math.round(raw / (double)this.precision) * this.precision));
   }

   public float getValue() {
      return this.value;
   }

   private void playSound() {
      try {
         class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
      } catch (Exception var2) {
      }

   }

   public int getWidth() {
      return this.width + this.gap + this.textRenderer.method_1727("OFF") + 10;
   }

   public int getHeight() {
      return 12;
   }
}
