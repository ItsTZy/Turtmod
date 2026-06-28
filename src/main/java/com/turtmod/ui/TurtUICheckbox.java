package com.turtmod.ui;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;

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

   // Lunar-style row mode: when rowWidth>0 the widget draws a full-width row card with the name on
   // the left and a sliding pill toggle on the right (set by the host screen after construction).
   public int rowWidth = 0;
   public int rowHeight = 18;
   /** When set (search active), the matching substring of the label is drawn in the accent pink. */
   public String highlightQuery = null;
   private float knobAnim = -1f;     // -1 = uninitialised (snaps to state on first frame)
   private float hoverAnim = 0f;     // eased 0..1 hover for the Lunar lift/glow
   private long lastNs = System.nanoTime();

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
      if (this.rowWidth > 0) {
         this.renderRow(context, mx, my);
         return;
      }
      boolean hovered = this.isHovered((double)mx, (double)my);
      Color textColor = hovered ? this.theme.highlighted() : this.theme.text();
      int boxX = this.x;
      int boxY = this.y;
      Color boxBorder = this.checked ? this.theme.highlighted() : this.theme.border();
      Color boxFill = this.checked ? this.theme.highlighted() : this.theme.background();
      TurtUIUtils.drawBorder(context, boxX, boxY, this.size, this.size, boxBorder);
      if (this.checked) {
         TurtUIUtils.drawRectangle(context, boxX + 2, boxY + 2, this.size - 4, this.size - 4, boxFill);
      } else if (hovered) {
         TurtUIUtils.drawRectangle(context, boxX + 2, boxY + 2, this.size - 4, this.size - 4, this.theme.hovered());
      }
      int textX = this.x + this.size + this.gap;
      Objects.requireNonNull(this.textRenderer);
      TurtUIUtils.drawText(context, this.textRenderer, this.label, textX, this.y + (this.size - 9) / 2 + 1, textColor, false, false, false);
   }

   /** Lunar-style module row: card + name + sliding pill toggle. */
   private void renderRow(class_332 ctx, int mx, int my) {
      long now = System.nanoTime();
      float dt = Math.min((now - this.lastNs) / 1_000_000_000f, 0.1f);
      this.lastNs = now;
      float target = this.checked ? 1f : 0f;
      this.knobAnim = this.knobAnim < 0f ? target : TurtUIUtils.lerp01(this.knobAnim, target, dt, 16f);

      boolean hovered = this.isHovered(mx, my);
      this.hoverAnim = TurtUIUtils.lerp01(this.hoverAnim, hovered ? 1f : 0f, dt, 14f);
      Color accent = this.theme.highlighted();
      int w = this.rowWidth;
      int h = this.rowHeight;

      // Lunar-style hover lift: nudge the whole row up/right a touch while hovered.
      float lift = TurtUIUtils.ease(this.hoverAnim);
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(lift * 2.5f, -lift * 1.0f);

      // Soft accent glow behind the card on hover.
      if (lift > 0.01f) {
         TurtUIUtils.drawHoverGlow(ctx, this.x, this.y, w, h, 4, lift * 0.8f, accent);
      }

      // Row card.
      int bg = this.checked ? 0x33000000 : (hovered ? 0x2BFFFFFF : 0x16FFFFFF);
      TurtUIUtils.drawRoundedRect(ctx, this.x, this.y, w, h, 4, new Color(bg, true));
      if (this.checked) {
         ctx.method_25294(this.x + 2, this.y + 3, this.x + 4, this.y + h - 3, accent.getRGB());
      } else if (lift > 0.01f) {
         TurtUIUtils.drawRoundedBorder(ctx, this.x, this.y, w, h, 4, new Color(255, 255, 255, (int)(36 * lift)));
      }

      // Name (with optional search-match highlight in accent pink so it reads on any row state).
      Color textColor = this.checked ? accent : (hovered ? new Color(0xFFFFFFFF, true) : this.theme.text());
      int nx = this.x + 8;
      int ny = this.y + (h - 8) / 2;
      String q = this.highlightQuery;
      int idx = (q == null || q.isEmpty()) ? -1 : this.label.toLowerCase(java.util.Locale.ROOT).indexOf(q);
      if (idx < 0) {
         TurtUIUtils.drawText(ctx, this.textRenderer, this.label, nx, ny, textColor, false, false, false);
      } else {
         String pre = this.label.substring(0, idx);
         String mid = this.label.substring(idx, idx + q.length());
         String post = this.label.substring(idx + q.length());
         TurtUIUtils.drawText(ctx, this.textRenderer, pre, nx, ny, textColor, false, false, false);
         int cx = nx + this.textRenderer.method_1727(pre);
         TurtUIUtils.drawText(ctx, this.textRenderer, mid, cx, ny, Palette.PINK, false, false, false);
         cx += this.textRenderer.method_1727(mid);
         TurtUIUtils.drawText(ctx, this.textRenderer, post, cx, ny, textColor, false, false, false);
      }

      // Sliding pill toggle on the right (larger radius reads as a cleaner pill).
      int ph = Math.min(12, h - 4);
      int pw = ph * 2;
      int px = this.x + w - pw - 6;
      int py = this.y + (h - ph) / 2;
      int r = ph / 2;
      TurtUIUtils.drawRoundedRect(ctx, px, py, pw, ph, r, new Color(0x66262626, true)); // off track
      TurtUIUtils.drawRoundedBorder(ctx, px, py, pw, ph, r, new Color(255, 255, 255, 20));
      int aa = (int)(this.knobAnim * 255f);
      if (aa > 0) {
         TurtUIUtils.drawRoundedRect(ctx, px, py, pw, ph, r,
            new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), aa));   // on track fades in
      }
      int knob = ph - 4;
      int travel = pw - knob - 4;
      int kx = px + 2 + Math.round(travel * this.knobAnim);
      TurtUIUtils.drawRoundedRect(ctx, kx, py + 2, knob, knob, knob / 2, new Color(250, 250, 250, 255));

      ctx.method_51448().popMatrix();
   }

   private boolean isHovered(double mx, double my) {
      if (this.rowWidth > 0) {
         return mx >= this.x && mx <= this.x + this.rowWidth && my >= this.y && my <= this.y + this.rowHeight;
      }
      int totalWidth = this.textRenderer.method_1727(this.label) + this.gap + this.size;
      Objects.requireNonNull(this.textRenderer);
      int height = Math.max(this.size, 9);
      return mx >= (double)this.x && mx <= (double)(this.x + totalWidth) && my >= (double)this.y && my <= (double)(this.y + height);
   }

   public boolean isHoveredPublic(double mx, double my) {
      return this.isHovered(mx, my);
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (button == 0 && this.isHovered(mx, my)) {
         this.checked = !this.checked;
         this.onChange.accept(this.checked);
         TurtSounds.toggle(this.checked);
         return true;
      } else {
         return false;
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
