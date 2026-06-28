package com.turtmod.ui;

import java.awt.Color;
import net.minecraft.class_327;
import net.minecraft.class_332;

public class TurtUIButton {
   public final int x;
   public final int y;
   public final int width;
   public final int height;
   public final String label;
   public final TurtUITheme theme;
   public final Runnable onClick;
   private float glow = 0f;
   private float press = 0f;          // 1 on click, decays to 0 → tactile squish
   private long lastTickNs = System.nanoTime();
   private static final Color PINK_GLOW = new Color(16752046, true);

   public TurtUIButton(int x, int y, int width, int height, String label, TurtUITheme theme, Runnable onClick) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.label = label;
      this.theme = theme;
      this.onClick = onClick;
   }

   public void render(class_332 context, int mouseX, int mouseY, class_327 textRenderer) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastTickNs) / 1_000_000_000f, 0.1f);
      lastTickNs = now;

      boolean isHovered = TurtUIUtils.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
      glow = TurtUIUtils.lerp01(glow, isHovered ? 1f : 0f, dt, 10f);
      this.press = TurtUIUtils.lerp01(this.press, 0f, dt, 9f);

      // Tactile press squish: scale down a touch around the centre while the press decays.
      boolean pressing = this.press > 0.01f;
      if (pressing) {
         float s = 1f - 0.06f * this.press;
         float cx = this.x + this.width / 2f;
         float cy = this.y + this.height / 2f;
         context.method_51448().pushMatrix();
         context.method_51448().translate(cx, cy);
         context.method_51448().scale(s, s);
         context.method_51448().translate(-cx, -cy);
      }

      // Smoothly interpolate every colour by the eased glow so hover doesn't snap.
      Color bg = mix(this.theme.background(), this.theme.hovered(), glow);
      Color border = mix(this.theme.border(), this.theme.highlighted(), glow);
      Color text = mix(this.theme.text(), new Color(16777215), glow);

      TurtUIUtils.drawHoverGlow(context, this.x, this.y, this.width, this.height, 4, glow, PINK_GLOW);
      TurtUIUtils.drawRoundedRect(context, this.x, this.y, this.width, this.height, 4, bg);
      // Lunar-style: a faint accent wash + glassy top highlight that brighten on hover.
      Color acc = this.theme.highlighted();
      int washA = (int)(34f * glow);
      if (washA > 0) {
         TurtUIUtils.drawRoundedRect(context, this.x, this.y, this.width, this.height, 4,
            new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), washA));
      }
      int hiA = (int)(38f + 46f * glow);
      context.method_25294(this.x + 4, this.y + 1, this.x + this.width - 4, this.y + 2, (hiA << 24) | 0xFFFFFF);
      TurtUIUtils.drawRoundedBorder(context, this.x, this.y, this.width, this.height, 4, border);

      int textWidth = textRenderer.method_1727(this.label);
      int textX = this.x + (this.width - textWidth) / 2;
      int textY = this.y + (this.height - 8) / 2;
      context.method_51433(textRenderer, this.label, textX, textY, text.getRGB(), glow > 0.5f);

      if (pressing) {
         context.method_51448().popMatrix();
      }
   }

   private static Color mix(Color a, Color b, float t) {
      t = t < 0f ? 0f : (t > 1f ? 1f : t);
      int r = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
      int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
      int bl = (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
      int al = (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
      return new Color(r, g, bl, al);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && TurtUIUtils.isHovered((int)mouseX, (int)mouseY, this.x, this.y, this.width, this.height)) {
         this.press = 1f;
         TurtSounds.click();
         this.onClick.run();
         return true;
      } else {
         return false;
      }
   }
}
