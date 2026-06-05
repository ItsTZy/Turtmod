package com.turtmod.ui;

import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;

public class TurtUIModuleCard {
   public final int x;
   public final int y;
   public final int width;
   public final int height;
   public final String label;
   public boolean enabled;
   public final Consumer<Boolean> onToggle;
   public final Runnable onRightClick;
   public final TurtUITheme theme;
   private float glow = 0f;
   private float onAnim;
   private long lastTickNs = System.nanoTime();

   public TurtUIModuleCard(int x, int y, int width, int height, String label, boolean initial, TurtUITheme theme, Consumer<Boolean> onToggle, Runnable onRightClick) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.label = label;
      this.enabled = initial;
      this.onAnim = initial ? 1f : 0f;
      this.theme = theme;
      this.onToggle = onToggle;
      this.onRightClick = onRightClick;
   }

   public void render(class_332 context, int mx, int my, class_327 textRenderer) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastTickNs) / 1_000_000_000f, 0.1f);
      lastTickNs = now;

      boolean hovered = TurtUIUtils.isHovered(mx, my, this.x, this.y, this.width, this.height);
      glow = TurtUIUtils.lerp01(glow, hovered ? 1.0F : 0.0F, dt, 14.0F);
      onAnim = TurtUIUtils.lerp01(onAnim, this.enabled ? 1.0F : 0.0F, dt, 16.0F);

      // Clean flat surface: a defined dark card that lifts subtly on hover (no shadow stack / glow rings).
      Color fill = mix(Palette.CARD_BG, Palette.CARD_HOVER, glow);
      TurtUIUtils.drawRoundedRect(context, this.x, this.y, this.width, this.height, 4, fill);

      // Hairline border, tinting toward green as the module turns on.
      Color border = mix(Palette.CARD_BORDER, Palette.alpha(Palette.GREEN, 160), onAnim);
      TurtUIUtils.drawBorder(context, this.x, this.y, this.width, this.height, border);

      // Label: bright when on, muted when off (brightens slightly on hover).
      float lit = Math.max(onAnim, glow * 0.5f);
      Color textCol = mix(Palette.TEXT_MUTED, Color.WHITE, lit);
      TurtUIUtils.drawText(context, textRenderer, this.label, this.x + 9,
         this.y + (this.height - 8) / 2, textCol, false, false, false);

      // Toggle switch on the right — track fades grey→green, knob slides across.
      int trackW = 20;
      int trackH = 10;
      int trackX = this.x + this.width - trackW - 8;
      int trackY = this.y + (this.height - trackH) / 2;
      Color track = mix(Palette.TOGGLE_OFF, Palette.GREEN, onAnim);
      TurtUIUtils.drawRoundedRect(context, trackX, trackY, trackW, trackH, trackH / 2, track);
      int knob = trackH - 2;
      int knobX = trackX + 1 + Math.round((trackW - knob - 2) * onAnim);
      TurtUIUtils.drawRoundedRect(context, knobX, trackY + 1, knob, knob, knob / 2, Palette.TOGGLE_KNOB);
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (TurtUIUtils.isHovered((int)mx, (int)my, this.x, this.y, this.width, this.height)) {
         if (button == 0) {
            this.enabled = !this.enabled;
            this.onToggle.accept(this.enabled);
            this.playSound();
            return true;
         }
         if (button == 1 && this.onRightClick != null) {
            this.onRightClick.run();
            this.playSound();
            return true;
         }
      }
      return false;
   }

   private static Color mix(Color a, Color b, float t) {
      t = t < 0f ? 0f : (t > 1f ? 1f : t);
      int r = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
      int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
      int bl = (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
      int al = (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
      return new Color(r, g, bl, al);
   }

   private void playSound() {
      try {
         class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
      } catch (Exception var2) {
      }
   }
}
