package com.turtmod.ui;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.class_10799;
import net.minecraft.class_1109;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;

public class TurtUICheckbox {
   /** Flip to false to fall back to the classic square checkbox. While true, the module toggles
    *  draw the turtle-shell sprites below (grey = off, green = on). */
   public static boolean USE_TURTLE_SHELL = false;
   // GUI sprites: place the PNGs at assets/turtmod/textures/gui/sprites/<name>.png
   private static final class_2960 SHELL_OFF = class_2960.method_60655("turtmod", "turtle_shell_off");
   private static final class_2960 SHELL_ON = class_2960.method_60655("turtmod", "turtle_shell_on");

   /** Per-module icons (your turtle-skin screenshots). Keyed by the module's display name. Only
    *  modules registered here render an icon; everything else keeps the shell/checkbox so you can
    *  add art one module at a time without breaking the rest. Icon files go in
    *  assets/turtmod/textures/gui/sprites/modules/&lt;slug&gt;.png and are referenced as
    *  turtmod:modules/&lt;slug&gt; (slug = lower-case name, non-alphanumerics → "_"). The icon shows
    *  dimmed when the module is OFF and full-colour when ON. */
   private static final java.util.Map<String, class_2960> MODULE_ICONS = new java.util.HashMap<>();
   /** Render size of a module icon (px). The 12px toggle hitbox is unchanged; the icon just draws
    *  a touch larger into the gap before the label. */
   private static final int ICON_SIZE = 16;

   /** Turn the slug "fullbright" out of the display name "Fullbright". */
   public static String iconSlug(String displayName) {
      return displayName.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
   }

   /** Register a module to use its own turtle icon. Call once you've dropped its PNG in place. */
   public static void enableIcon(String displayName) {
      MODULE_ICONS.put(displayName, class_2960.method_60655("turtmod", "modules/" + iconSlug(displayName)));
   }

   // NOTE: per-row module icons are intentionally NOT registered here. The module list shows the
   // classic checkbox. enableIcon(...) remains available if you ever want a one-off row icon back.
   public final int x;
   public final int y;
   public final int size;
   public final int gap;
   public final class_327 textRenderer;
   public final String label;
   public boolean checked;
   public final Consumer<Boolean> onChange;
   public final TurtUITheme theme;
   public final boolean gradient;

   public TurtUICheckbox(int x, int y, int size, int gap, class_327 textRenderer, String label, TurtUITheme theme, boolean gradient, boolean initial, Consumer<Boolean> onChange) {
      this.x = x;
      this.y = y;
      this.size = size;
      this.gap = 10;
      this.textRenderer = textRenderer;
      this.label = label;
      this.checked = initial;
      this.onChange = onChange;
      this.theme = theme;
      this.gradient = gradient;
   }

   public void render(class_332 context, int mx, int my) {
      boolean hovered = this.isHovered((double)mx, (double)my);
      Color textColor = hovered ? this.theme.highlighted() : this.theme.text();
      int boxX = this.x;
      int boxY = this.y;
      class_2960 moduleIcon = MODULE_ICONS.get(this.label);
      if (moduleIcon != null) {
         // Per-module turtle icon. Centre the (slightly larger) icon on the toggle footprint so the
         // label position is untouched. Dim/grey tint when off, full colour when on.
         int s = ICON_SIZE + (hovered ? 2 : 0);
         int sx = boxX + (this.size - s) / 2;
         int sy = boxY + (this.size - s) / 2;
         int tint = this.checked ? -1 : (hovered ? -7895161 /*0xFF8A8A8A*/ : -10921639 /*0xFF595959*/);
         context.method_52707(class_10799.field_56883, moduleIcon, sx, sy, s, s, tint);
      } else if (USE_TURTLE_SHELL) {
         // Turtle-shell toggle: grey shell when off, green shell when on. Sprite is drawn at the
         // exact checkbox footprint (size x size) so layout/hitbox are unchanged.
         class_2960 tex = this.checked ? SHELL_ON : SHELL_OFF;
         int s = this.size;
         int sx = boxX;
         int sy = boxY;
         if (hovered) { // tiny pop on hover
            s += 2;
            sx -= 1;
            sy -= 1;
         }
         context.method_52706(class_10799.field_56883, tex, sx, sy, s, s);
      } else {
         Color boxBorder = this.checked ? this.theme.highlighted() : this.theme.border();
         Color boxFill = this.checked ? this.theme.highlighted() : this.theme.background();
         TurtUIUtils.drawBorder(context, boxX, boxY, this.size, this.size, boxBorder);
         if (this.checked) {
            TurtUIUtils.drawRectangle(context, boxX + 2, boxY + 2, this.size - 4, this.size - 4, boxFill);
         } else if (hovered) {
            TurtUIUtils.drawRectangle(context, boxX + 2, boxY + 2, this.size - 4, this.size - 4, this.theme.hovered());
         }
      }

      int textX = this.x + this.size + this.gap;
      class_327 var10001 = this.textRenderer;
      String var10002 = this.label;
      int var10004 = this.y;
      int var10005 = this.size;
      Objects.requireNonNull(this.textRenderer);
      TurtUIUtils.drawText(context, var10001, var10002, textX, var10004 + (var10005 - 9) / 2 + 1, textColor, false, false, false);
   }

   private boolean isHovered(double mx, double my) {
      int totalWidth = this.textRenderer.method_1727(this.label) + this.gap + this.size;
      int var10000 = this.size;
      Objects.requireNonNull(this.textRenderer);
      int height = Math.max(var10000, 9);
      return mx >= (double)this.x && mx <= (double)(this.x + totalWidth) && my >= (double)this.y && my <= (double)(this.y + height);
   }

   public boolean isHoveredPublic(double mx, double my) {
      return this.isHovered(mx, my);
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (button == 0 && this.isHovered(mx, my)) {
         this.checked = !this.checked;
         this.onChange.accept(this.checked);
         this.playSound();
         return true;
      } else {
         return false;
      }
   }

   private void playSound() {
      try {
         class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
      } catch (Exception var2) {
      }

   }

   public int getWidth() {
      return this.textRenderer.method_1727(this.label) + this.gap + this.size + 5;
   }

   public int getHeight() {
      int var10000 = this.size;
      Objects.requireNonNull(this.textRenderer);
      return Math.max(var10000, 9) + 4;
   }
}
