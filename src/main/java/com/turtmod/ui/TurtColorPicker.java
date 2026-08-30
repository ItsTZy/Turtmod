package com.turtmod.ui;

import com.turtmod.ui.config.model.ConfigColor;
import java.awt.Color;
import java.util.function.IntConsumer;
import net.minecraft.class_327;
import net.minecraft.class_332;

/**
 * A reusable, Photoshop-style colour picker rendered as a self-contained modal panel. Used anywhere the
 * mod edits a colour so every picker looks and behaves identically (SV square, hue bar, alpha bar with a
 * transparency checker, a live preview showing new-vs-original, editable Hex / R,G,B / H,S,B / A fields,
 * preset + saved swatches). The host screen converts raw mouse coords to its own logical space, pushes its
 * UI scale, then forwards events here; the widget centres itself in the logical screen size passed to
 * {@link #render}.
 */
public final class TurtColorPicker {
   private float h, s, b;   // 0..1
   private int a;           // 0..255
   private final int originalArgb;
   private final String title;
   private final boolean allowAlpha;
   private final IntConsumer onChange;   // argb, called live on every change
   private final Runnable onDone;

   public TurtColorPicker(int argb, String title, boolean allowAlpha, IntConsumer onChange, Runnable onDone) {
      this.originalArgb = argb;
      this.title = title;
      this.allowAlpha = allowAlpha;
      this.onChange = onChange;
      this.onDone = onDone;
      float[] hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
      this.h = hsb[0]; this.s = hsb[1]; this.b = hsb[2];
      this.a = allowAlpha ? ((argb >>> 24) & 0xFF) : 255;
   }

   public int argb() {
      return (this.a << 24) | (ConfigColor.HSBtoRGB(this.h, this.s, this.b) & 0xFFFFFF);
   }

   private void changed() {
      if (this.onChange != null) this.onChange.accept(argb());
   }

   // ── Layout (recomputed each frame in render) ──
   private int panelX, panelY, panelW, panelH;
   private int svX, svY, svW, svH;
   private int hueX, hueW;
   private int alphaX, alphaY, alphaW, alphaH;
   private int doneX, doneY, doneW, doneH;
   private int presetY, savedY, presetSize = 15, presetGap = 4, presetX0;
   private int savedAddX;
   private int dragMode = 0;   // 1 SV, 2 hue, 3 alpha

   // Numeric fields: 0 hex, 1 R, 2 G, 3 B, 4 H, 5 S, 6 Bri, 7 A
   private static final int F_HEX = 0, F_R = 1, F_G = 2, F_B = 3, F_H = 4, F_S = 5, F_BRI = 6, F_A = 7;
   private final int[] fx = new int[8], fy = new int[8], fw = new int[8], fh = new int[8];
   private int editingField = -1;
   private String buf = "";

   private static final int[] PRESETS = {
      0xFFFFFF, 0x000000, 0xFF5555, 0xFFA640, 0xFFE03B,
      0x8DC95F, 0x3BE0C8, 0x5599FF, 0xB36BFF, 0xFF6BB0
   };

   public void render(class_332 ctx, class_327 font, int screenW, int screenH, int mx, int my) {
      this.panelW = 344;
      this.panelH = this.allowAlpha ? 292 : 276;
      this.panelX = (screenW - this.panelW) / 2;
      this.panelY = (screenH - this.panelH) / 2;

      // Dim behind + panel surface.
      ctx.method_25294(-4000, -4000, screenW + 4000, screenH + 4000, 0xB4000000);
      TurtUIUtils.drawRoundedRect(ctx, this.panelX + 2, this.panelY + 3, this.panelW, this.panelH, 8, new Color(0, 0, 0, 90));
      TurtUIUtils.drawRoundedRect(ctx, this.panelX, this.panelY, this.panelW, this.panelH, 8, new Color(13, 16, 23, 252));
      TurtUIUtils.drawRoundedBorder(ctx, this.panelX, this.panelY, this.panelW, this.panelH, 8, Palette.alpha(Palette.GREEN, 140));
      TurtUIUtils.drawGradientText(ctx, font, this.title, this.panelX + 14, this.panelY + 11, Palette.GREEN, Palette.PINK, false, true);
      ctx.method_25294(this.panelX + 12, this.panelY + 24, this.panelX + this.panelW - 12, this.panelY + 25, Palette.alpha(Palette.GREEN, 40).getRGB());

      // SV square.
      this.svX = this.panelX + 14;
      this.svY = this.panelY + 32;
      this.svW = 150;
      this.svH = 150;
      int hueRgb = ConfigColor.HSBtoRGB(this.h, 1f, 1f);
      TurtUIUtils.drawHorizontalGradient(ctx, this.svX, this.svY, this.svW, this.svH, Color.WHITE, new Color(hueRgb));
      TurtUIUtils.drawGradientRectangle(ctx, this.svX, this.svY, this.svW, this.svH, new Color(0, 0, 0, 0), new Color(0, 0, 0, 255));
      TurtUIUtils.drawBorder(ctx, this.svX, this.svY, this.svW, this.svH, new Color(0, 0, 0, 120));
      drawRing(ctx, this.svX + Math.round(this.s * this.svW), this.svY + Math.round((1f - this.b) * this.svH), 4);

      // Hue bar.
      this.hueX = this.svX + this.svW + 10;
      this.hueW = 14;
      for (int i = 0; i < this.svH; i++) {
         int rgb = ConfigColor.HSBtoRGB((float) i / this.svH, 1f, 1f);
         ctx.method_25294(this.hueX, this.svY + i, this.hueX + this.hueW, this.svY + i + 1, 0xFF000000 | rgb);
      }
      TurtUIUtils.drawBorder(ctx, this.hueX, this.svY, this.hueW, this.svH, new Color(0, 0, 0, 120));
      drawMarkerH(ctx, this.hueX, this.svY + Math.round(this.h * this.svH), this.hueW);

      // Alpha bar under the SV square (with checker).
      this.alphaX = this.svX;
      this.alphaW = this.hueX + this.hueW - this.svX;
      this.alphaY = this.svY + this.svH + 16;
      this.alphaH = 12;
      int baseRgb = ConfigColor.HSBtoRGB(this.h, this.s, this.b) & 0xFFFFFF;
      if (this.allowAlpha) {
         ctx.method_51433(font, "Alpha", this.alphaX, this.alphaY - 10, Palette.TEXT_MUTED.getRGB(), false);
         drawChecker(ctx, this.alphaX, this.alphaY, this.alphaW, this.alphaH);
         TurtUIUtils.drawHorizontalGradient(ctx, this.alphaX, this.alphaY, this.alphaW, this.alphaH, new Color(baseRgb, false), new Color(0xFF000000 | baseRgb));
         TurtUIUtils.drawBorder(ctx, this.alphaX, this.alphaY, this.alphaW, this.alphaH, new Color(0, 0, 0, 120));
         drawMarkerV(ctx, this.alphaX + Math.round(this.a / 255f * this.alphaW), this.alphaY, this.alphaH);
      }

      // ── Right column: preview + numeric fields (Photoshop-style) ──
      int colX = this.hueX + this.hueW + 12;
      int colW = this.panelX + this.panelW - 14 - colX;
      int y = this.svY;
      int argb = argb();
      // New-over-original preview swatch.
      drawChecker(ctx, colX, y, colW, 22);
      TurtUIUtils.drawRoundedRect(ctx, colX, y, colW / 2, 22, 0, new Color(argb, true));
      TurtUIUtils.drawRoundedRect(ctx, colX + colW / 2, y, colW - colW / 2, 22, 0, new Color(this.originalArgb, true));
      TurtUIUtils.drawBorder(ctx, colX, y, colW, 22, new Color(255, 255, 255, 90));
      ctx.method_51433(font, "new", colX + 2, y - 9, Palette.TEXT_MUTED.getRGB(), false);
      ctx.method_51433(font, "old", colX + colW / 2 + 2, y - 9, Palette.TEXT_MUTED.getRGB(), false);
      y += 34;

      int r = (baseRgb >> 16) & 0xFF, g = (baseRgb >> 8) & 0xFF, bl = baseRgb & 0xFF;
      int fieldH = 15;
      // Hex spans the full column.
      drawField(ctx, font, F_HEX, colX, y, colW, fieldH, "Hex", "#" + String.format(this.allowAlpha ? "%08X" : "%06X", this.allowAlpha ? argb : (argb & 0xFFFFFF)));
      y += fieldH + 5;
      // R G B on one row.
      int third = (colW - 8) / 3;
      drawField(ctx, font, F_R, colX, y, third, fieldH, "R", String.valueOf(r));
      drawField(ctx, font, F_G, colX + third + 4, y, third, fieldH, "G", String.valueOf(g));
      drawField(ctx, font, F_B, colX + (third + 4) * 2, y, third, fieldH, "B", String.valueOf(bl));
      y += fieldH + 5;
      // H S B on one row (H in degrees, S/B in %).
      drawField(ctx, font, F_H, colX, y, third, fieldH, "H", String.valueOf(Math.round(this.h * 360f)));
      drawField(ctx, font, F_S, colX + third + 4, y, third, fieldH, "S", String.valueOf(Math.round(this.s * 100f)));
      drawField(ctx, font, F_BRI, colX + (third + 4) * 2, y, third, fieldH, "B", String.valueOf(Math.round(this.b * 100f)));
      y += fieldH + 5;
      if (this.allowAlpha) {
         drawField(ctx, font, F_A, colX, y, third, fieldH, "A%", String.valueOf(Math.round(this.a / 255f * 100f)));
      } else {
         this.fw[F_A] = 0;
      }

      // ── Preset + saved swatches under the SV square ──
      this.presetX0 = this.svX;
      this.presetY = this.alphaY + (this.allowAlpha ? this.alphaH + 14 : 6);
      ctx.method_51433(font, "Presets", this.svX, this.presetY - 9, Palette.TEXT_MUTED.getRGB(), false);
      for (int i = 0; i < PRESETS.length; i++) {
         int px = this.presetX0 + i * (this.presetSize + this.presetGap);
         boolean ph = TurtUIUtils.isHovered(mx, my, px, this.presetY, this.presetSize, this.presetSize);
         TurtUIUtils.drawRoundedRect(ctx, px, this.presetY, this.presetSize, this.presetSize, 3, new Color(0xFF000000 | PRESETS[i]));
         TurtUIUtils.drawRoundedBorder(ctx, px, this.presetY, this.presetSize, this.presetSize, 3, ph ? Palette.PINK : new Color(255, 255, 255, 70));
      }
      java.util.List<Integer> saved = com.turtmod.TurtModClient.getConfig().savedPickerColors;
      this.savedY = this.presetY + this.presetSize + 14;
      ctx.method_51433(font, "Saved", this.svX, this.savedY - 9, Palette.TEXT_MUTED.getRGB(), false);
      int si = 0;
      for (; si < saved.size() && si < 8; si++) {
         int px = this.presetX0 + si * (this.presetSize + this.presetGap);
         boolean ph = TurtUIUtils.isHovered(mx, my, px, this.savedY, this.presetSize, this.presetSize);
         drawChecker(ctx, px, this.savedY, this.presetSize, this.presetSize);
         TurtUIUtils.drawRoundedRect(ctx, px, this.savedY, this.presetSize, this.presetSize, 3, new Color(saved.get(si), true));
         TurtUIUtils.drawRoundedBorder(ctx, px, this.savedY, this.presetSize, this.presetSize, 3, ph ? Palette.PINK : new Color(255, 255, 255, 70));
      }
      this.savedAddX = this.presetX0 + si * (this.presetSize + this.presetGap);
      boolean addHov = TurtUIUtils.isHovered(mx, my, this.savedAddX, this.savedY, this.presetSize, this.presetSize);
      TurtUIUtils.drawRoundedRect(ctx, this.savedAddX, this.savedY, this.presetSize, this.presetSize, 3, Palette.alpha(Palette.GREEN, addHov ? 90 : 45));
      TurtUIUtils.drawRoundedBorder(ctx, this.savedAddX, this.savedY, this.presetSize, this.presetSize, 3, Palette.alpha(Palette.GREEN, 150));
      ctx.method_25300(font, "+", this.savedAddX + this.presetSize / 2, this.savedY + 4, Palette.GREEN.getRGB());

      // Done.
      this.doneW = 52; this.doneH = 18;
      this.doneX = this.panelX + this.panelW - this.doneW - 12;
      this.doneY = this.panelY + this.panelH - this.doneH - 10;
      boolean dh = TurtUIUtils.isHovered(mx, my, this.doneX, this.doneY, this.doneW, this.doneH);
      TurtUIUtils.drawRoundedRect(ctx, this.doneX, this.doneY, this.doneW, this.doneH, 5, Palette.alpha(Palette.GREEN, dh ? 235 : 165));
      ctx.method_25300(font, "Done", this.doneX + this.doneW / 2, this.doneY + 5, new Color(8, 12, 10).getRGB());
   }

   private void drawField(class_332 ctx, class_327 font, int id, int x, int y, int w, int h, String label, String value) {
      this.fx[id] = x; this.fy[id] = y; this.fw[id] = w; this.fh[id] = h;
      boolean editing = this.editingField == id;
      int labW = font.method_1727(label) + 3;
      ctx.method_51433(font, label, x, y + (h - 8) / 2, Palette.TEXT_MUTED.getRGB(), false);
      int bx = x + labW, bw = w - labW;
      TurtUIUtils.drawRoundedRect(ctx, bx, y, bw, h, 3, Palette.SEARCH_BG);
      TurtUIUtils.drawRoundedBorder(ctx, bx, y, bw, h, 3, editing ? Palette.PINK : Palette.alpha(Palette.GREEN, 60));
      String shown = editing ? this.buf : value;
      ctx.method_51433(font, shown, bx + 4, y + (h - 8) / 2, Palette.TEXT.getRGB(), false);
      if (editing && System.currentTimeMillis() / 500L % 2L == 0L) {
         int cx = bx + 4 + font.method_1727(shown);
         ctx.method_25294(cx, y + 3, cx + 1, y + h - 3, -1);
      }
   }

   // ── Input (host passes logical coords) ──
   public boolean mouseClicked(double mx, double my, int button) {
      // Done.
      if (in(mx, my, this.doneX, this.doneY, this.doneW, this.doneH)) { commitField(); if (this.onDone != null) this.onDone.run(); return true; }
      // Numeric fields.
      for (int id = 0; id < 8; id++) {
         if (this.fw[id] <= 0) continue;
         if (in(mx, my, this.fx[id], this.fy[id], this.fw[id], this.fh[id])) { beginEdit(id); return true; }
      }
      commitField();
      // SV square.
      if (in(mx, my, this.svX, this.svY, this.svW, this.svH)) { this.dragMode = 1; updateSV(mx, my); return true; }
      // Hue bar.
      if (in(mx, my, this.hueX, this.svY, this.hueW, this.svH)) { this.dragMode = 2; updateHue(my); return true; }
      // Alpha bar.
      if (this.allowAlpha && in(mx, my, this.alphaX, this.alphaY, this.alphaW, this.alphaH)) { this.dragMode = 3; updateAlpha(mx); return true; }
      // Presets.
      for (int i = 0; i < PRESETS.length; i++) {
         int px = this.presetX0 + i * (this.presetSize + this.presetGap);
         if (in(mx, my, px, this.presetY, this.presetSize, this.presetSize)) { setRgbKeepAlpha(PRESETS[i]); return true; }
      }
      // Saved swatches (left click apply, right click remove) + add.
      java.util.List<Integer> saved = com.turtmod.TurtModClient.getConfig().savedPickerColors;
      for (int i = 0; i < saved.size() && i < 8; i++) {
         int px = this.presetX0 + i * (this.presetSize + this.presetGap);
         if (in(mx, my, px, this.savedY, this.presetSize, this.presetSize)) {
            if (button == 1) { saved.remove(i); com.turtmod.config.ConfigManager.save(com.turtmod.TurtModClient.getConfig()); }
            else { setArgb(saved.get(i)); }
            return true;
         }
      }
      if (in(mx, my, this.savedAddX, this.savedY, this.presetSize, this.presetSize)) {
         if (saved.size() < 8) { saved.add(argb()); com.turtmod.config.ConfigManager.save(com.turtmod.TurtModClient.getConfig()); }
         return true;
      }
      // Click anywhere inside the panel is swallowed (modal); outside is left for the host.
      return in(mx, my, this.panelX, this.panelY, this.panelW, this.panelH);
   }

   public boolean mouseDragged(double mx, double my) {
      switch (this.dragMode) {
         case 1 -> { updateSV(mx, my); return true; }
         case 2 -> { updateHue(my); return true; }
         case 3 -> { updateAlpha(mx); return true; }
         default -> { return false; }
      }
   }

   public void mouseReleased() { this.dragMode = 0; }

   public boolean isOutside(double mx, double my) {
      return !in(mx, my, this.panelX, this.panelY, this.panelW, this.panelH);
   }

   public boolean charTyped(char c) {
      if (this.editingField < 0) return false;
      boolean hexField = this.editingField == F_HEX;
      boolean ok = hexField ? (Character.digit(c, 16) >= 0 || c == '#') : (c >= '0' && c <= '9');
      if (ok && this.buf.length() < (hexField ? 9 : 5)) { this.buf += c; return true; }
      return true;   // swallow all typing while a field is focused
   }

   /** @return true if handled; caller should treat Escape specially via {@link #isEditingField()}. */
   public boolean keyPressed(int key) {
      if (this.editingField < 0) return false;
      if (key == 259) { if (!this.buf.isEmpty()) this.buf = this.buf.substring(0, this.buf.length() - 1); return true; }   // backspace
      if (key == 257 || key == 335) { commitField(); return true; }                                                       // enter
      if (key == 256) { this.editingField = -1; this.buf = ""; return true; }                                             // escape cancels field only
      return true;
   }

   public boolean isEditingField() { return this.editingField >= 0; }

   private void beginEdit(int id) {
      commitField();
      this.editingField = id;
      this.buf = "";
   }

   private void commitField() {
      if (this.editingField < 0) return;
      int id = this.editingField;
      String txt = this.buf.replace("#", "").trim();
      this.editingField = -1;
      this.buf = "";
      if (txt.isEmpty()) return;
      try {
         int baseRgb = ConfigColor.HSBtoRGB(this.h, this.s, this.b) & 0xFFFFFF;
         switch (id) {
            case F_HEX -> {
               long v = Long.parseLong(txt, 16);
               if (txt.length() <= 6) setRgbKeepAlpha((int) (v & 0xFFFFFF));
               else setArgb((int) v);
            }
            case F_R -> setRgbKeepAlpha((clamp255(txt) << 16) | (baseRgb & 0x00FFFF));
            case F_G -> setRgbKeepAlpha((baseRgb & 0xFF00FF) | (clamp255(txt) << 8));
            case F_B -> setRgbKeepAlpha((baseRgb & 0xFFFF00) | clamp255(txt));
            case F_H -> { this.h = clamp(Integer.parseInt(txt), 0, 360) / 360f; changed(); }
            case F_S -> { this.s = clamp(Integer.parseInt(txt), 0, 100) / 100f; changed(); }
            case F_BRI -> { this.b = clamp(Integer.parseInt(txt), 0, 100) / 100f; changed(); }
            case F_A -> { this.a = Math.round(clamp(Integer.parseInt(txt), 0, 100) / 100f * 255f); changed(); }
         }
      } catch (NumberFormatException ignored) {
      }
   }

   private void setArgb(int argb) {
      float[] hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
      this.h = hsb[0]; this.s = hsb[1]; this.b = hsb[2];
      if (this.allowAlpha) this.a = (argb >>> 24) & 0xFF;
      changed();
   }

   private void setRgbKeepAlpha(int rgb) {
      float[] hsb = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
      this.h = hsb[0]; this.s = hsb[1]; this.b = hsb[2];
      changed();
   }

   private void updateSV(double mx, double my) {
      this.s = clamp01((float) (mx - this.svX) / this.svW);
      this.b = 1f - clamp01((float) (my - this.svY) / this.svH);
      changed();
   }
   private void updateHue(double my) { this.h = clamp01((float) (my - this.svY) / this.svH); changed(); }
   private void updateAlpha(double mx) { this.a = Math.round(clamp01((float) (mx - this.alphaX) / this.alphaW) * 255f); changed(); }

   private static int clamp255(String s) { return clamp(Integer.parseInt(s), 0, 255); }
   private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
   private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
   private static boolean in(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }

   private void drawRing(class_332 ctx, int cx, int cy, int rr) {
      TurtUIUtils.drawRoundedBorder(ctx, cx - rr - 1, cy - rr - 1, (rr + 1) * 2, (rr + 1) * 2, rr + 1, new Color(0, 0, 0, 160));
      TurtUIUtils.drawRoundedBorder(ctx, cx - rr, cy - rr, rr * 2, rr * 2, rr, Color.WHITE);
   }
   private void drawMarkerH(class_332 ctx, int barX, int y, int barW) {
      ctx.method_25294(barX - 2, y - 1, barX + barW + 2, y + 1, -1);
      ctx.method_25294(barX - 2, y - 2, barX + barW + 2, y - 1, 0xA0000000);
      ctx.method_25294(barX - 2, y + 1, barX + barW + 2, y + 2, 0xA0000000);
   }
   private void drawMarkerV(class_332 ctx, int x, int barY, int barH) {
      ctx.method_25294(x - 1, barY - 2, x + 1, barY + barH + 2, -1);
      ctx.method_25294(x - 2, barY - 2, x - 1, barY + barH + 2, 0xA0000000);
      ctx.method_25294(x + 1, barY - 2, x + 2, barY + barH + 2, 0xA0000000);
   }
   private void drawChecker(class_332 ctx, int x, int y, int w, int h) {
      int cell = 4;
      for (int yy = 0; yy < h; yy += cell) {
         for (int xx = 0; xx < w; xx += cell) {
            boolean dark = ((xx / cell) + (yy / cell)) % 2 == 0;
            ctx.method_25294(x + xx, y + yy, Math.min(x + xx + cell, x + w), Math.min(y + yy + cell, y + h),
               dark ? 0xFF808080 : 0xFFC0C0C0);
         }
      }
   }
}
