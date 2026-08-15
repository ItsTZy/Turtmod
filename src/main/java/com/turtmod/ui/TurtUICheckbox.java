package com.turtmod.ui;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;

public class TurtUICheckbox {
   // Module rows now render a real Minecraft item icon (see TurtModuleIcons), with the TurtIcons pixel glyph
   // as the fallback. The old turtle-shell sprites / per-module AI PNGs were removed (dead art).
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
   /** Optional pixel-art icon (12x12 grid, see TurtIcons) drawn left of the label in row mode. */
   public String[] icon = null;
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

      // Row card. (No hover glow — its square corners clashed with the rounded card.)
      int bg = this.checked ? 0x33000000 : (hovered ? 0x2BFFFFFF : 0x16FFFFFF);
      TurtUIUtils.drawRoundedRect(ctx, this.x, this.y, w, h, 4, new Color(bg, true));
      // Checked rows get a clean subtle accent outline (replaces the old left-edge accent bar that read ugly).
      if (this.checked) {
         TurtUIUtils.drawRoundedBorder(ctx, this.x, this.y, w, h, 4, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110));
      } else if (lift > 0.01f) {
         TurtUIUtils.drawRoundedBorder(ctx, this.x, this.y, w, h, 4, new Color(255, 255, 255, (int)(36 * lift)));
      }

      // Per-module icon, drawn left of the name; label shifts to make room. Prefer a real Minecraft item icon
      // (crisp/native); fall back to the pixel glyph for modules without an item mapping.
      net.minecraft.class_1799 itemIcon = TurtModuleIcons.forModule(this.label);
      if (itemIcon != null) {
         TurtModuleIcons.drawItem(ctx, itemIcon, this.x + 3, this.y + (h - 16) / 2);
      } else if (this.icon != null) {
         TurtIcons.drawFit(ctx, this.icon, this.x + 5, this.y + (h - 12) / 2, 12);
      }
      boolean hasIcon = itemIcon != null || this.icon != null;

      // Name (with optional search-match highlight in accent pink so it reads on any row state).
      Color textColor = this.checked ? accent : (hovered ? new Color(0xFFFFFFFF, true) : this.theme.text());
      int nx = this.x + (hasIcon ? 21 : 8);
      int ny = this.y + (h - 8) / 2;
      String q = this.highlightQuery;
      int idx = (q == null || q.isEmpty()) ? -1 : this.label.toLowerCase(java.util.Locale.ROOT).indexOf(q);
      if (idx < 0) {
         // Trim long names so they never run under the toggle pill (fits icon + label + toggle in the card).
         int avail = (this.x + w - 33) - nx;
         String shown = this.textRenderer.method_1727(this.label) <= avail
            ? this.label : this.textRenderer.method_27523(this.label, Math.max(0, avail - 4)) + "…";
         TurtUIUtils.drawText(ctx, this.textRenderer, shown, nx, ny, textColor, false, false, false);
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

      // Sliding toggle on the right — a softly-rounded rectangle (not a full pill; the full pill read too round).
      int ph = Math.min(12, h - 4);
      int pw = ph * 2;
      int px = this.x + w - pw - 6;
      int py = this.y + (h - ph) / 2;
      int r = 4;
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
      TurtUIUtils.drawRoundedRect(ctx, kx, py + 2, knob, knob, 3, new Color(250, 250, 250, 255));

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
