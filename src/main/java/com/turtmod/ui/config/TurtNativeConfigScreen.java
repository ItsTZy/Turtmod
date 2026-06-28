package com.turtmod.ui.config;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtSounds;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUIUtils;
import com.turtmod.ui.config.model.Category;
import com.turtmod.ui.config.model.ConfigColor;
import com.turtmod.ui.config.model.LocalConfig;
import com.turtmod.ui.config.model.Option;
import com.turtmod.ui.config.model.OptionDescription;
import com.turtmod.ui.config.model.OptionGroup;
import java.awt.Color;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * TurtMod's native config screen. Renders the {@link LocalConfig} tree the factory builds inside the
 * shared {@link TurtLauncher} chrome (transparent panel, green/pink branding), but with categories as
 * a full-width horizontal tab strip rather than a sidebar, so the option list gets the whole panel
 * width. All the mod's UI animations apply: open slide-up, staggered row reveal, animated toggles,
 * search underline, divider shine, logo bob, cursor glow. Per-option widgets key off {@link
 * Option#getType()}; colours open a built-in HSB picker.
 */
public class TurtNativeConfigScreen extends class_437 {
   private static final int LOGICAL_W = 540;
   private static final int LOGICAL_H = 350;

   private static final int GROUP_H = 16;
   private static final int ROW_H = 18;

   private final class_437 parent;
   private final LocalConfig config;
   private final List<Category> categories;
   private final String title;
   private int activeCat;

   private final TurtUIScale uiScale = new TurtUIScale();

   private long lastFrameNs = System.nanoTime();
   private float openFade;
   private float catFade;
   private float searchFocusAnim;

   // Inline "Saved ✓" feedback on Done: pulse then close (no toast).
   private float savedPulse;
   private long closeAtNs;
   // Eased hover/press for the Done button (premium tactile feel).
   private float doneHover;
   private float donePress;

   private String search = "";
   private boolean searchFocused;
   private float scrollY;

   // Layout (logical px).
   private int contentX;
   private int contentW;
   private int tabY;
   private int listTop;
   private int listBottom;
   private int searchX;
   private int searchY;
   private int searchW;
   private int doneX;
   private int btnY;
   private int btnW;
   private int btnH;
   private final int[] tabX = new int[32];
   private final int[] tabW = new int[32];
   private final float[] tabHover = new float[32];
   private float dtThisFrame;

   private final Map<Option<?>, Float> toggleAnim = new IdentityHashMap<>();
   private final Map<Option<?>, Float> rowHover = new IdentityHashMap<>();

   private Option<?> dragNumeric;
   private int dragTrackX;
   private int dragTrackW;

   // Click-to-type number field.
   private Option<?> editingNum;
   private String numBuf = "";

   // Enum dropdown picker.
   private Option<?> dropdownOpt;
   private List<Object> ddValues;
   private int ddX;
   private int ddY;
   private int ddW;
   private static final int DD_ITEM_H = 14;

   // Colour picker.
   private Option<?> pickerOpt;
   private float pH;
   private float pS;
   private float pB;
   private int pA = 255;
   private int pickX;
   private int pickY;
   private int pickW;
   private int pickH;
   private int svX;
   private int svY;
   private int svW;
   private int svH;
   private int hueX;
   private int hueW;
   private int alphaY;
   private int alphaH;
   private int dragPicker;

   // Click-to-type hex field + recent-colour memory (persists for the session).
   private boolean editingHex;
   private String hexBuf = "";
   private int hexFieldX;
   private int hexFieldY;
   private int hexFieldW;
   private int hexFieldH;
   private static final java.util.ArrayDeque<Integer> RECENT = new java.util.ArrayDeque<>();
   private int recentY;

   private static void pushRecent(int argb) {
      RECENT.remove(argb);
      RECENT.addFirst(argb);
      while (RECENT.size() > 8) {
         RECENT.removeLast();
      }
   }

   public TurtNativeConfigScreen(class_437 parent, LocalConfig config) {
      super(class_2561.method_43470("TurtMod"));
      this.parent = parent;
      this.config = config;
      this.categories = config.categories();
      this.title = this.categories.size() == 1 ? this.categories.get(0).name().toUpperCase() : "CONFIG";
   }

   protected void method_25426() {
      this.openFade = 0.0F;
      this.catFade = 0.0F;
   }

   // ── Render ────────────────────────────────────────────────────────────────
   public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f);
      this.lastFrameNs = now;
      this.dtThisFrame = dt;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, dt, 12f);
      this.catFade = TurtUIUtils.lerp01(this.catFade, 1f, dt, 14f);
      this.searchFocusAnim = TurtUIUtils.lerp01(this.searchFocusAnim, this.searchFocused ? 1f : 0f, dt, 14f);
      this.savedPulse = TurtUIUtils.lerp01(this.savedPulse, 0f, dt, 3.2f);
      this.donePress = TurtUIUtils.lerp01(this.donePress, 0f, dt, 9f);
      // Deferred close after the "Saved ✓" pulse has been shown.
      if (this.closeAtNs != 0L && now >= this.closeAtNs) {
         this.closeAtNs = 0L;
         this.method_25419();
         return;
      }

      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(ctx, mouseX, mouseY);

      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      int mx = (int) this.uiScale.toLogicalX(mouseX);
      int my = (int) this.uiScale.toLogicalY(mouseY);
      this.layout();
      this.doneHover = TurtUIUtils.lerp01(this.doneHover, TurtUIUtils.isHovered(mx, my, this.doneX, this.btnY, this.btnW, this.btnH) ? 1f : 0f, dt, 13f);

      this.uiScale.push(ctx);
      ctx.method_51448().pushMatrix();
      float introE = TurtUIUtils.ease(this.openFade);
      ctx.method_51448().translate(LOGICAL_W / 2f, LOGICAL_H / 2f + (1f - introE) * 12f);
      ctx.method_51448().scale(0.97f + 0.03f * introE, 0.97f + 0.03f * introE);
      ctx.method_51448().translate(-LOGICAL_W / 2f, -LOGICAL_H / 2f);

      TurtLauncher.drawChrome(ctx, this.field_22793, 0, 0, LOGICAL_W, LOGICAL_H, this.title, null, "v1.21.11", false);

      this.drawTabs(ctx, mx, my);
      this.drawSearchBar(ctx, mx, my);
      this.drawList(ctx, mx, my);
      this.drawButtons(ctx, mx, my);

      ctx.method_51448().popMatrix();
      this.uiScale.pop(ctx);

      if (this.dropdownOpt != null) {
         this.uiScale.push(ctx);
         this.renderDropdown(ctx, mx, my);
         this.uiScale.pop(ctx);
      }
      if (this.pickerOpt != null) {
         this.uiScale.push(ctx);
         this.renderColorPicker(ctx, mx, my);
         this.uiScale.pop(ctx);
      }
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mouseX, mouseY, delta);
   }

   private void layout() {
      this.contentX = TurtLauncher.CONTENT_PAD;
      this.contentW = LOGICAL_W - TurtLauncher.CONTENT_PAD * 2;
      this.tabY = TurtLauncher.HEADER_H + 7;
      this.searchW = 150;
      this.searchX = this.contentX + this.contentW - this.searchW;
      this.searchY = this.tabY;
      this.listTop = this.tabY + 16 + 8;
      this.btnH = 18;
      this.btnW = 84;
      this.btnY = LOGICAL_H - TurtLauncher.FOOTER_H - 22;
      this.listBottom = this.btnY - 4;
      this.doneX = this.contentX + this.contentW - this.btnW;

      // Tab pill geometry.
      int x = this.contentX;
      for (int i = 0; i < this.categories.size(); i++) {
         int w = this.field_22793.method_1727(this.categories.get(i).name()) + 16;
         this.tabX[i] = x;
         this.tabW[i] = w;
         x += w + 4;
      }
   }

   private void drawTabs(class_332 ctx, int mx, int my) {
      for (int i = 0; i < this.categories.size(); i++) {
         int x = this.tabX[i];
         int w = this.tabW[i];
         boolean active = i == this.activeCat;
         boolean hov = TurtUIUtils.isHovered(mx, my, x, this.tabY, w, 16);
         this.tabHover[i] = TurtUIUtils.lerp01(this.tabHover[i], hov && !active ? 1f : 0f, this.dtThisFrame, 13f);
         float he = TurtUIUtils.ease(this.tabHover[i]);
         if (active) {
            TurtUIUtils.drawRoundedRect(ctx, x, this.tabY, w, 16, 5, Palette.alpha(Palette.GREEN, 55));
            TurtUIUtils.drawRoundedBorder(ctx, x, this.tabY, w, 16, 5, Palette.alpha(Palette.GREEN, 160));
         } else if (he > 0.01f) {
            TurtUIUtils.drawRoundedRect(ctx, x, this.tabY, w, 16, 5, Palette.alpha(Palette.BTN_HOVER, (int) (Palette.BTN_HOVER.getAlpha() * he)));
         }
         Color tc = active ? Palette.GREEN : (he > 0.4f ? Palette.TEXT : Palette.TEXT_MUTED);
         ctx.method_51433(this.field_22793, this.categories.get(i).name(), x + 8, this.tabY + 4, tc.getRGB(), false);
      }
   }

   private void drawSearchBar(class_332 ctx, int mx, int my) {
      int x = this.searchX;
      int y = this.searchY;
      int w = this.searchW;
      int h = 16;
      boolean hov = TurtUIUtils.isHovered(mx, my, x, y, w, h);
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 5, Palette.SEARCH_BG);
      TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 5,
         this.searchFocused ? Palette.PINK : (hov ? Palette.GREEN : Palette.SEARCH_BORDER));
      float fe = TurtUIUtils.ease(this.searchFocusAnim);
      if (fe > 0.01f) {
         int uw = (int) ((w - 6) * fe);
         int ux = x + w / 2 - uw / 2;
         ctx.method_25294(ux, y + h - 1, ux + uw, y + h, Palette.PINK.getRGB());
      }
      ctx.method_51433(this.field_22793, "🔍", x + 6, y + 5, Palette.TEXT_MUTED.getRGB(), false);
      if (this.search.isEmpty() && !this.searchFocused) {
         ctx.method_51433(this.field_22793, "Search...", x + 17, y + 5, 0x88909CA8, false);
      } else {
         ctx.method_51433(this.field_22793, this.search, x + 17, y + 5, -1, false);
      }
      if (this.searchFocused && System.currentTimeMillis() / 500L % 2L == 0L) {
         int cx = x + 17 + this.field_22793.method_1727(this.search);
         ctx.method_25294(cx, y + 4, cx + 1, y + 13, -1);
      }
   }

   private void drawList(class_332 ctx, int mx, int my) {
      int total = this.measureContent();
      int viewport = this.listBottom - this.listTop;
      this.scrollY = Math.max(0f, Math.min(this.scrollY, Math.max(0, total - viewport)));

      ctx.method_44379(this.contentX - 2, this.listTop, this.contentX + this.contentW + 2, this.listBottom);
      String[] tip = new String[1];
      int[] idx = {0};
      float fade = this.catFade;
      this.walkRows((kind, group, opt, y, h) -> {
         if (y + h < this.listTop || y > this.listBottom) {
            idx[0]++;
            return false;
         }
         float rp = fade * 2.0f - idx[0] * 0.06f;
         rp = rp < 0f ? 0f : (rp > 1f ? 1f : rp);
         float e = TurtUIUtils.ease(rp);
         ctx.method_51448().pushMatrix();
         ctx.method_51448().translate((1f - e) * 22f, 0f);
         if (kind == 0) {
            this.renderGroupHeader(ctx, group, y);
         } else {
            boolean hov = this.pickerOpt == null && TurtUIUtils.isHovered(mx, my, this.contentX, y, this.contentW, h)
               && my >= this.listTop && my <= this.listBottom;
            this.renderOptionRow(ctx, opt, y, h, mx, my, hov);
            if (hov) {
               String d = describe(opt);
               if (d != null) {
                  tip[0] = d;
               }
            }
         }
         ctx.method_51448().popMatrix();
         idx[0]++;
         return false;
      });
      ctx.method_44380();

      if (total > viewport) {
         int trackX = this.contentX + this.contentW - 2;
         int thumbH = Math.max(16, Math.round((float) viewport * viewport / total));
         int thumbY = this.listTop + Math.round((this.scrollY / (total - viewport)) * (viewport - thumbH));
         TurtUIUtils.drawRoundedRect(ctx, trackX, this.listTop, 3, viewport, 1, new Color(0x22FFFFFF, true));
         TurtUIUtils.drawRoundedRect(ctx, trackX, thumbY, 3, thumbH, 1, Palette.GREEN);
      }

      if (this.measureContent() <= 6) {
         TurtUIUtils.drawText(ctx, this.field_22793, "🐢  Nothing matches",
            this.contentX + this.contentW / 2, (this.listTop + this.listBottom) / 2 - 4, Palette.TEXT_MUTED, true, false);
      }

      if (tip[0] != null && this.pickerOpt == null) {
         this.renderTooltip(ctx, tip[0], mx, my);
      }
   }

   private void renderGroupHeader(class_332 ctx, OptionGroup group, int y) {
      boolean expanded = !this.search.trim().isEmpty() || group.isExpanded();
      ctx.method_51433(this.field_22793, expanded ? "▾" : "▸", this.contentX + 1, y + 5, Palette.PINK.getRGB(), false);
      TurtUIUtils.drawText(ctx, this.field_22793, group.getName().toUpperCase(), this.contentX + 12, y + 5,
         Palette.alpha(Palette.GREEN, 235), false, false, false);
      int lineX = this.contentX + 16 + this.field_22793.method_1727(group.getName().toUpperCase());
      ctx.method_25294(lineX, y + 8, this.contentX + this.contentW - 6, y + 9, 0x18FFFFFF);
   }

   private void renderOptionRow(class_332 ctx, Option<?> opt, int y, int h, int mx, int my, boolean hov) {
      int rowX = this.contentX + 10;
      float rh = this.rowHover.getOrDefault(opt, 0f);
      rh = TurtUIUtils.lerp01(rh, hov ? 1f : 0f, this.dtThisFrame, 14f);
      this.rowHover.put(opt, rh);
      if (rh > 0.01f) {
         TurtUIUtils.drawRoundedRect(ctx, this.contentX, y, this.contentW - 6, h, 3, new Color(255, 255, 255, (int) (20 * rh)));
      }
      Class<?> t = opt.getType();
      boolean isButton = t == Runnable.class;
      boolean on = t == Boolean.class && Boolean.TRUE.equals(opt.getValue());
      Color nameColor = t == Boolean.class ? (on ? Palette.TEXT : Palette.TEXT_MUTED) : Palette.TEXT;
      int textY = y + (h - 8) / 2;
      this.drawSearchName(ctx, opt.getName(), rowX, textY, nameColor);

      int ctrlRight = this.contentX + this.contentW - 12;
      if (t == Boolean.class) {
         this.renderToggle(ctx, opt, ctrlRight - 24, y + (h - 12) / 2, on);
      } else if (Number.class.isAssignableFrom(t)) {
         this.renderSlider(ctx, opt, ctrlRight, y, h);
      } else if (t == ConfigColor.class) {
         int sw = 26;
         int sx = ctrlRight - sw;
         int sy = y + (h - 12) / 2;
         TurtUIUtils.drawRoundedRect(ctx, sx, sy, sw, 12, 3, new Color(currentArgb(opt), true));
         TurtUIUtils.drawRoundedBorder(ctx, sx, sy, sw, 12, 3, new Color(255, 255, 255, 110 + (int) (110 * rh)));
      } else if (t.isEnum()) {
         String label = String.valueOf(opt.getValue());
         int pw = this.field_22793.method_1727(label) + 12;
         int px = ctrlRight - pw;
         int py = y + (h - 12) / 2;
         boolean ph = TurtUIUtils.isHovered(mx, my, px, py, pw, 12);
         TurtUIUtils.drawRoundedRect(ctx, px, py, pw, 12, 4, ph ? Palette.BTN_HOVER : Palette.BTN_BG);
         ctx.method_51433(this.field_22793, label, px + 6, py + 2, Palette.GREEN.getRGB(), false);
      } else if (isButton) {
         ctx.method_51433(this.field_22793, "❯", ctrlRight - 6, textY, Palette.PINK.getRGB(), false);
      }
   }

   private void renderToggle(class_332 ctx, Option<?> opt, int tx, int ty, boolean on) {
      float anim = this.toggleAnim.getOrDefault(opt, on ? 1.0F : 0.0F);
      anim += ((on ? 1.0F : 0.0F) - anim) * 0.25F;
      this.toggleAnim.put(opt, anim);
      Color track = TurtUIUtils.blend(Palette.TOGGLE_OFF, Palette.GREEN, anim);
      TurtUIUtils.drawRoundedRect(ctx, tx, ty, 24, 12, 6, track);
      int knobX = tx + 2 + (int) (anim * 12.0F);
      TurtUIUtils.drawRoundedRect(ctx, knobX, ty + 2, 8, 8, 4, Palette.TOGGLE_KNOB);
   }

   private void renderSlider(class_332 ctx, Option<?> opt, int ctrlRight, int y, int h) {
      int trackW = 90;
      int trackX = ctrlRight - trackW;
      int trackY = y + h / 2;
      double min = ((Number) opt.getMin()).doubleValue();
      double max = ((Number) opt.getMax()).doubleValue();
      double val = ((Number) opt.getValue()).doubleValue();
      float frac = max > min ? (float) ((val - min) / (max - min)) : 0f;
      frac = Math.max(0f, Math.min(1f, frac));
      ctx.method_25294(trackX, trackY - 1, trackX + trackW, trackY + 1, Palette.TOGGLE_OFF.getRGB());
      ctx.method_25294(trackX, trackY - 1, trackX + (int) (trackW * frac), trackY + 1, Palette.GREEN.getRGB());
      int knobX = trackX + (int) (trackW * frac);
      TurtUIUtils.drawRoundedRect(ctx, knobX - 2, trackY - 4, 5, 8, 2, Palette.TOGGLE_KNOB);

      // Editable value field to the left of the track (click to type an exact value).
      int fieldW = 38;
      int fieldX = trackX - fieldW - 6;
      int fieldY = y + (h - 12) / 2;
      boolean editing = this.editingNum == opt;
      TurtUIUtils.drawRoundedRect(ctx, fieldX, fieldY, fieldW, 12, 3, Palette.SEARCH_BG);
      TurtUIUtils.drawRoundedBorder(ctx, fieldX, fieldY, fieldW, 12, 3, editing ? Palette.PINK : Palette.alpha(Palette.GREEN, 60));
      String label = editing ? this.numBuf : (opt.getType() == Integer.class ? String.valueOf((int) Math.round(val)) : trimFloat(val));
      ctx.method_51433(this.field_22793, label, fieldX + 4, fieldY + 2, (editing ? Palette.TEXT : Palette.TEXT).getRGB(), false);
      if (editing && System.currentTimeMillis() / 500L % 2L == 0L) {
         int cx = fieldX + 4 + this.field_22793.method_1727(this.numBuf);
         ctx.method_25294(cx, fieldY + 2, cx + 1, fieldY + 10, -1);
      }
   }

   private void drawButtons(class_332 ctx, int mx, int my) {
      float cx = this.doneX + this.btnW / 2f;
      float cy = this.btnY + this.btnH / 2f;
      boolean squish = this.donePress > 0.01f;
      if (squish) {
         ctx.method_51448().pushMatrix();
         ctx.method_51448().translate(cx, cy);
         float s = 1f - 0.06f * this.donePress;
         ctx.method_51448().scale(s, s);
         ctx.method_51448().translate(-cx, -cy);
      }
      boolean saved = this.savedPulse > 0.02f;
      int a = saved ? 255 : 175 + (int) (55 * TurtUIUtils.ease(this.doneHover));
      TurtUIUtils.drawRoundedRect(ctx, this.doneX, this.btnY, this.btnW, this.btnH, 5, Palette.alpha(Palette.GREEN, a));
      if (saved) {
         TurtUIUtils.drawRoundedBorder(ctx, this.doneX, this.btnY, this.btnW, this.btnH, 5,
            Palette.alpha(Color.WHITE, (int) (200 * this.savedPulse)));
      }
      drawCentered(ctx, saved ? "Saved ✓" : "Done", this.doneX + this.btnW / 2, this.btnY + (this.btnH - 8) / 2, new Color(8, 12, 10));
      if (squish) {
         ctx.method_51448().popMatrix();
      }
   }

   // ── Colour picker ───────────────────────────────────────────────────────-─
   /** Quick-pick swatches (opaque RGB). */
   private static final int[] PRESETS = {
      0xFFFFFF, 0x000000, 0xFF5555, 0xFFA640, 0xFFE03B,
      0x8DC95F, 0x3BE0C8, 0x5599FF, 0xB36BFF, 0xFF6BB0
   };
   private int presetX0;
   private int presetY;
   private int presetSize;
   private int presetGap;

   private void renderColorPicker(class_332 ctx, int mx, int my) {
      ctx.method_25294(-2000, -2000, LOGICAL_W + 2000, LOGICAL_H + 2000, 0xB0000000);
      this.pickW = 200;
      this.pickH = 252;
      this.pickX = (LOGICAL_W - this.pickW) / 2;
      this.pickY = (LOGICAL_H - this.pickH) / 2;
      // Soft accent halo + panel.
      TurtUIUtils.drawHoverGlow(ctx, this.pickX, this.pickY, this.pickW, this.pickH, 8, 1f, Palette.alpha(Palette.PINK, 120));
      TurtUIUtils.drawRoundedRect(ctx, this.pickX, this.pickY, this.pickW, this.pickH, 8, new Color(13, 16, 23, 252));
      TurtUIUtils.drawRoundedBorder(ctx, this.pickX, this.pickY, this.pickW, this.pickH, 8, Palette.alpha(Palette.GREEN, 140));
      TurtUIUtils.drawGradientText(ctx, this.field_22793, this.pickerOpt.getName(), this.pickX + 12, this.pickY + 10,
         Palette.GREEN, Palette.PINK, false, true);
      ctx.method_25294(this.pickX + 10, this.pickY + 22, this.pickX + this.pickW - 10, this.pickY + 23, Palette.alpha(Palette.GREEN, 40).getRGB());

      this.svX = this.pickX + 12;
      this.svY = this.pickY + 30;
      this.svW = 130;
      this.svH = 96;
      int hueRgb = ConfigColor.HSBtoRGB(this.pH, 1f, 1f);
      TurtUIUtils.drawHorizontalGradient(ctx, this.svX, this.svY, this.svW, this.svH, Color.WHITE, new Color(hueRgb));
      TurtUIUtils.drawGradientRectangle(ctx, this.svX, this.svY, this.svW, this.svH, new Color(0, 0, 0, 0), new Color(0, 0, 0, 255));
      TurtUIUtils.drawBorder(ctx, this.svX, this.svY, this.svW, this.svH, new Color(0, 0, 0, 120));
      int selX = this.svX + Math.round(this.pS * this.svW);
      int selY = this.svY + Math.round((1f - this.pB) * this.svH);
      this.drawRing(ctx, selX, selY, 4);

      // Hue bar (rounded ends).
      this.hueX = this.svX + this.svW + 8;
      this.hueW = 12;
      for (int i = 0; i < this.svH; i++) {
         int rgb = ConfigColor.HSBtoRGB((float) i / this.svH, 1f, 1f);
         ctx.method_25294(this.hueX, this.svY + i, this.hueX + this.hueW, this.svY + i + 1, 0xFF000000 | rgb);
      }
      TurtUIUtils.drawBorder(ctx, this.hueX, this.svY, this.hueW, this.svH, new Color(0, 0, 0, 120));
      int hy = this.svY + Math.round(this.pH * this.svH);
      this.drawMarkerH(ctx, this.hueX, hy, this.hueW);

      // Alpha slider with a transparency checkerboard behind it.
      this.alphaY = this.svY + this.svH + 12;
      this.alphaH = 12;
      int baseRgb = ConfigColor.HSBtoRGB(this.pH, this.pS, this.pB) & 0xFFFFFF;
      this.drawChecker(ctx, this.svX, this.alphaY, this.svW, this.alphaH);
      TurtUIUtils.drawHorizontalGradient(ctx, this.svX, this.alphaY, this.svW, this.alphaH, new Color(baseRgb & 0xFFFFFF, false), new Color(0xFF000000 | baseRgb));
      TurtUIUtils.drawBorder(ctx, this.svX, this.alphaY, this.svW, this.alphaH, new Color(0, 0, 0, 120));
      int ax = this.svX + Math.round(this.pA / 255f * this.svW);
      this.drawMarkerV(ctx, ax, this.alphaY, this.alphaH);

      // Preview chip (checkerboard behind for alpha) + editable hex field.
      int argb = (this.pA << 24) | baseRgb;
      int chipY = this.alphaY + this.alphaH + 8;
      this.drawChecker(ctx, this.svX, chipY, 28, 14);
      TurtUIUtils.drawRoundedRect(ctx, this.svX, chipY, 28, 14, 3, new Color(argb, true));
      TurtUIUtils.drawRoundedBorder(ctx, this.svX, chipY, 28, 14, 3, new Color(255, 255, 255, 90));
      this.hexFieldX = this.svX + 32;
      this.hexFieldY = chipY;
      this.hexFieldW = 74;
      this.hexFieldH = 14;
      TurtUIUtils.drawRoundedRect(ctx, this.hexFieldX, this.hexFieldY, this.hexFieldW, this.hexFieldH, 3, Palette.SEARCH_BG);
      TurtUIUtils.drawRoundedBorder(ctx, this.hexFieldX, this.hexFieldY, this.hexFieldW, this.hexFieldH, 3,
         this.editingHex ? Palette.PINK : Palette.alpha(Palette.GREEN, 60));
      String hexLabel = this.editingHex ? ("#" + this.hexBuf) : String.format("#%08X", argb);
      ctx.method_51433(this.field_22793, hexLabel, this.hexFieldX + 4, this.hexFieldY + 3, Palette.TEXT.getRGB(), false);
      if (this.editingHex && System.currentTimeMillis() / 500L % 2L == 0L) {
         int cx = this.hexFieldX + 4 + this.field_22793.method_1727("#" + this.hexBuf);
         ctx.method_25294(cx, this.hexFieldY + 3, cx + 1, this.hexFieldY + 11, -1);
      }

      // Preset swatches.
      this.presetSize = 14;
      this.presetGap = 3;
      this.presetX0 = this.svX;
      this.presetY = chipY + 20;
      for (int i = 0; i < PRESETS.length; i++) {
         int px = this.presetX0 + i * (this.presetSize + this.presetGap);
         boolean ph = TurtUIUtils.isHovered(mx, my, px, this.presetY, this.presetSize, this.presetSize);
         TurtUIUtils.drawRoundedRect(ctx, px, this.presetY, this.presetSize, this.presetSize, 3, new Color(0xFF000000 | PRESETS[i]));
         TurtUIUtils.drawRoundedBorder(ctx, px, this.presetY, this.presetSize, this.presetSize, 3,
            ph ? Palette.PINK : new Color(255, 255, 255, 70));
      }

      // Recent colours (carry alpha → checkerboard behind each).
      this.recentY = this.presetY + this.presetSize + 13;
      if (!RECENT.isEmpty()) {
         ctx.method_51433(this.field_22793, "Recent", this.svX, this.recentY - 9, Palette.TEXT_MUTED.getRGB(), false);
         int i = 0;
         for (Integer c : RECENT) {
            int px = this.presetX0 + i * (this.presetSize + this.presetGap);
            boolean ph = TurtUIUtils.isHovered(mx, my, px, this.recentY, this.presetSize, this.presetSize);
            this.drawChecker(ctx, px, this.recentY, this.presetSize, this.presetSize);
            TurtUIUtils.drawRoundedRect(ctx, px, this.recentY, this.presetSize, this.presetSize, 3, new Color(c, true));
            TurtUIUtils.drawRoundedBorder(ctx, px, this.recentY, this.presetSize, this.presetSize, 3,
               ph ? Palette.PINK : new Color(255, 255, 255, 70));
            i++;
         }
      }

      // Done.
      boolean dh = TurtUIUtils.isHovered(mx, my, this.pickX + this.pickW - 58, this.pickY + this.pickH - 26, 46, 18);
      TurtUIUtils.drawRoundedRect(ctx, this.pickX + this.pickW - 58, this.pickY + this.pickH - 26, 46, 18, 5, Palette.alpha(Palette.GREEN, dh ? 235 : 165));
      drawCentered(ctx, "Done", this.pickX + this.pickW - 35, this.pickY + this.pickH - 21, new Color(8, 12, 10));
   }

   /** White ring with a dark halo so the SV selector reads on any colour. */
   private void drawRing(class_332 ctx, int cx, int cy, int r) {
      TurtUIUtils.drawRoundedBorder(ctx, cx - r - 1, cy - r - 1, (r + 1) * 2, (r + 1) * 2, r + 1, new Color(0, 0, 0, 160));
      TurtUIUtils.drawRoundedBorder(ctx, cx - r, cy - r, r * 2, r * 2, r, Color.WHITE);
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

   /** Grey transparency checkerboard, drawn behind alpha-bearing previews. */
   private void drawChecker(class_332 ctx, int x, int y, int w, int h) {
      int cell = 4;
      for (int yy = 0; yy < h; yy += cell) {
         for (int xx = 0; xx < w; xx += cell) {
            boolean dark = ((xx / cell) + (yy / cell)) % 2 == 0;
            ctx.method_25294(x + xx, y + yy, Math.min(x + xx + cell, x + w), Math.min(y + yy + cell, y + h),
               dark ? 0xFF606060 : 0xFFA0A0A0);
         }
      }
   }

   private void applyPicker() {
      if (this.pickerOpt == null) {
         return;
      }
      int rgb = ConfigColor.HSBtoRGB(this.pH, this.pS, this.pB) & 0xFFFFFF;
      setOption(this.pickerOpt, new ConfigColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, this.pA));
   }

   // ── Input ───────────────────────────────────────────────────────────────-─
   public boolean method_25400(class_11905 input) {
      char chr = (char) input.comp_4793();
      if (this.editingNum != null) {
         if ((chr >= '0' && chr <= '9') || chr == '.' || chr == '-') {
            this.numBuf += chr;
         }
         return true;
      }
      if (this.editingHex) {
         if (this.hexBuf.length() < 8 && Character.digit(chr, 16) >= 0) {
            this.hexBuf += Character.toUpperCase(chr);
         }
         return true;
      }
      if (this.searchFocused) {
         if (Character.isLetterOrDigit(chr) || chr == ' ' || chr == '_' || chr == '-') {
            this.search += chr;
            this.scrollY = 0f;
            return true;
         }
         return super.method_25400(input);
      }
      // Auto-focus search the moment the user starts typing (Steam/Discord-style); '/' focuses empty.
      if (this.pickerOpt == null && this.dropdownOpt == null) {
         if (chr == '/') {
            this.searchFocused = true;
            return true;
         }
         if (Character.isLetterOrDigit(chr)) {
            this.searchFocused = true;
            this.search += chr;
            this.scrollY = 0f;
            return true;
         }
      }
      return super.method_25400(input);
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (this.editingNum != null) {
         if (key == 257 || key == 335) { // Enter / numpad Enter
            this.commitNumEdit();
         } else if (key == 256) { // Esc cancels
            this.editingNum = null;
         } else if (key == 259 && !this.numBuf.isEmpty()) { // Backspace
            this.numBuf = this.numBuf.substring(0, this.numBuf.length() - 1);
         }
         return true;
      }
      if (this.editingHex) {
         if (key == 257 || key == 335) { // Enter
            this.commitHex();
         } else if (key == 256) { // Esc cancels
            this.editingHex = false;
         } else if (key == 259 && !this.hexBuf.isEmpty()) { // Backspace
            this.hexBuf = this.hexBuf.substring(0, this.hexBuf.length() - 1);
         }
         return true;
      }
      if (this.dropdownOpt != null && key == 256) {
         this.dropdownOpt = null;
         return true;
      }
      if (this.pickerOpt != null && key == 256) {
         this.closePicker();
         return true;
      }
      if (this.searchFocused) {
         if (key == 259) {
            if (!this.search.isEmpty()) {
               this.search = this.search.substring(0, this.search.length() - 1);
            }
            return true;
         }
         if (key == 256 || key == 257) {
            this.searchFocused = false;
            return true;
         }
      }
      return super.method_25404(input);
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      int button = click.method_74245();
      int mx = (int) this.uiScale.toLogicalX(click.comp_4798());
      int my = (int) this.uiScale.toLogicalY(click.comp_4799());

      // Enum dropdown captures clicks while open.
      if (this.dropdownOpt != null) {
         int n = this.ddValues.size();
         for (int i = 0; i < n; i++) {
            int iy = this.ddY + 2 + i * DD_ITEM_H;
            if (TurtUIUtils.isHovered(mx, my, this.ddX + 2, iy, this.ddW - 4, DD_ITEM_H)) {
               setOption(this.dropdownOpt, this.ddValues.get(i));
               this.dropdownOpt = null;
               TurtSounds.tick();
               return true;
            }
         }
         this.dropdownOpt = null;
         return true;
      }

      // A click anywhere commits an in-progress number edit (before processing the click).
      if (this.editingNum != null) {
         this.commitNumEdit();
      }

      if (this.pickerOpt != null) {
         if (TurtUIUtils.isHovered(mx, my, this.hexFieldX, this.hexFieldY, this.hexFieldW, this.hexFieldH)) {
            this.startHexEdit();
            TurtSounds.click();
            return true;
         }
         if (this.editingHex) {
            this.commitHex();
         }
         if (TurtUIUtils.isHovered(mx, my, this.pickX + this.pickW - 58, this.pickY + this.pickH - 26, 46, 18)) {
            this.closePicker();
            TurtSounds.confirm();
            return true;
         }
         int ri = 0;
         for (Integer c : RECENT) {
            int rpx = this.presetX0 + ri * (this.presetSize + this.presetGap);
            if (TurtUIUtils.isHovered(mx, my, rpx, this.recentY, this.presetSize, this.presetSize)) {
               int v = c;
               this.pA = (v >>> 24) & 0xFF;
               float[] hsb = ConfigColor.RGBtoHSB((v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF, null);
               this.pH = hsb[0];
               this.pS = hsb[1];
               this.pB = hsb[2];
               this.applyPicker();
               TurtSounds.tick();
               return true;
            }
            ri++;
         }
         for (int i = 0; i < PRESETS.length; i++) {
            int px = this.presetX0 + i * (this.presetSize + this.presetGap);
            if (TurtUIUtils.isHovered(mx, my, px, this.presetY, this.presetSize, this.presetSize)) {
               float[] hsb = ConfigColor.RGBtoHSB((PRESETS[i] >> 16) & 0xFF, (PRESETS[i] >> 8) & 0xFF, PRESETS[i] & 0xFF, null);
               this.pH = hsb[0];
               this.pS = hsb[1];
               this.pB = hsb[2];
               this.applyPicker();
               TurtSounds.tick();
               return true;
            }
         }
         if (TurtUIUtils.isHovered(mx, my, this.svX, this.svY, this.svW, this.svH)) {
            this.dragPicker = 1;
            this.updatePickerSV(mx, my);
            return true;
         }
         if (TurtUIUtils.isHovered(mx, my, this.hueX, this.svY, this.hueW, this.svH)) {
            this.dragPicker = 2;
            this.updatePickerHue(my);
            return true;
         }
         if (TurtUIUtils.isHovered(mx, my, this.svX, this.alphaY - 2, this.svW, this.alphaH + 4)) {
            this.dragPicker = 3;
            this.updatePickerAlpha(mx);
            return true;
         }
         if (!TurtUIUtils.isHovered(mx, my, this.pickX, this.pickY, this.pickW, this.pickH)) {
            this.closePicker();
         }
         return true;
      }

      if (TurtUIUtils.isHovered(mx, my, this.searchX, this.searchY, this.searchW, 16)) {
         this.searchFocused = true;
         return true;
      }
      this.searchFocused = false;

      if (TurtUIUtils.isHovered(mx, my, this.doneX, this.btnY, this.btnW, this.btnH)) {
         this.requestDone();
         return true;
      }

      for (int i = 0; i < this.categories.size(); i++) {
         if (TurtUIUtils.isHovered(mx, my, this.tabX[i], this.tabY, this.tabW[i], 16)) {
            if (i != this.activeCat) {
               this.activeCat = i;
               this.scrollY = 0f;
               this.catFade = 0f;
               TurtSounds.tab();
            }
            return true;
         }
      }

      if (my < this.listTop || my > this.listBottom) {
         return super.method_25402(click, bl);
      }
      boolean[] handled = new boolean[1];
      this.walkRows((kind, group, opt, y, h) -> {
         if (!TurtUIUtils.isHovered(mx, my, this.contentX, y, this.contentW, h)) {
            return false;
         }
         if (kind == 0) {
            group.toggleExpanded();
            handled[0] = true;
            return true;
         }
         handled[0] = this.clickOption(opt, button, mx, y, h);
         return true;
      });
      return handled[0] || super.method_25402(click, bl);
   }

   private boolean clickOption(Option<?> opt, int button, int mx, int y, int h) {
      Class<?> t = opt.getType();
      int ctrlRight = this.contentX + this.contentW - 12;
      if (t == Boolean.class) {
         boolean nv = !Boolean.TRUE.equals(opt.getValue());
         setOption(opt, nv);
         TurtSounds.toggle(nv);
         return true;
      }
      if (Number.class.isAssignableFrom(t)) {
         int trackX = ctrlRight - 90;
         int fieldX = trackX - 44;
         if (mx >= fieldX && mx < trackX - 6) {
            this.startNumEdit(opt);
            TurtSounds.click();
         } else {
            this.editingNum = null;
            this.dragNumeric = opt;
            this.dragTrackW = 90;
            this.dragTrackX = trackX;
            this.updateNumericFromMouse(mx);
            TurtSounds.tick();
         }
         return true;
      }
      if (t == ConfigColor.class) {
         this.openPicker(opt);
         TurtSounds.click();
         return true;
      }
      if (t.isEnum()) {
         if (button == 1) {
            this.cycleEnum(opt, -1);
            TurtSounds.tick();
         } else {
            this.openDropdown(opt, y, h, ctrlRight);
            TurtSounds.click();
         }
         return true;
      }
      if (t == Runnable.class) {
         if (opt.getValue() instanceof Runnable r) {
            r.run();
         }
         TurtSounds.click();
         return true;
      }
      return false;
   }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      int mx = (int) this.uiScale.toLogicalX(click.comp_4798());
      int my = (int) this.uiScale.toLogicalY(click.comp_4799());
      if (this.dragPicker != 0) {
         if (this.dragPicker == 1) {
            this.updatePickerSV(mx, my);
         } else if (this.dragPicker == 2) {
            this.updatePickerHue(my);
         } else {
            this.updatePickerAlpha(mx);
         }
         return true;
      }
      if (this.dragNumeric != null) {
         this.updateNumericFromMouse(mx);
         return true;
      }
      return super.method_25403(click, dx, dy);
   }

   public boolean method_25406(class_11909 click) {
      if (this.dragNumeric != null) {
         TurtSounds.tick();
      }
      this.dragNumeric = null;
      this.dragPicker = 0;
      return super.method_25406(click);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.pickerOpt != null) {
         return true;
      }
      int total = this.measureContent();
      int viewport = this.listBottom - this.listTop;
      int lx = (int) this.uiScale.toLogicalX(mouseX);
      int ly = (int) this.uiScale.toLogicalY(mouseY);
      if (total > viewport && lx >= this.contentX && lx <= this.contentX + this.contentW
            && ly >= this.listTop && ly <= this.listBottom) {
         this.scrollY -= (float) verticalAmount * 20f;
         this.scrollY = Math.max(0f, Math.min(this.scrollY, total - viewport));
         return true;
      }
      return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   // ── Value helpers ─────────────────────────────────────────────────────────
   private void updateNumericFromMouse(int mx) {
      Option<?> opt = this.dragNumeric;
      if (opt == null) {
         return;
      }
      double min = ((Number) opt.getMin()).doubleValue();
      double max = ((Number) opt.getMax()).doubleValue();
      double step = ((Number) opt.getIncrement()).doubleValue();
      float frac = Math.max(0f, Math.min(1f, (float) (mx - this.dragTrackX) / this.dragTrackW));
      double raw = min + frac * (max - min);
      if (step > 0) {
         raw = min + Math.round((raw - min) / step) * step;
      }
      raw = Math.max(min, Math.min(max, raw));
      Class<?> t = opt.getType();
      Object boxed = t == Integer.class ? (Object) (int) Math.round(raw)
         : t == Float.class ? (Object) (float) raw : (Object) raw;
      setOption(opt, boxed);
   }

   private void startNumEdit(Option<?> opt) {
      this.editingNum = opt;
      this.dragNumeric = null;
      this.searchFocused = false;
      double val = ((Number) opt.getValue()).doubleValue();
      this.numBuf = opt.getType() == Integer.class ? String.valueOf((int) Math.round(val)) : trimFloat(val);
   }

   private void commitNumEdit() {
      Option<?> opt = this.editingNum;
      this.editingNum = null;
      if (opt == null) {
         return;
      }
      double raw;
      try {
         raw = Double.parseDouble(this.numBuf.trim());
      } catch (NumberFormatException e) {
         return;
      }
      double min = ((Number) opt.getMin()).doubleValue();
      double max = ((Number) opt.getMax()).doubleValue();
      raw = Math.max(min, Math.min(max, raw));
      Class<?> t = opt.getType();
      Object boxed = t == Integer.class ? (Object) (int) Math.round(raw)
         : t == Float.class ? (Object) (float) raw : (Object) raw;
      setOption(opt, boxed);
   }

   private void openDropdown(Option<?> opt, int rowY, int rowH, int ctrlRight) {
      Object[] consts = opt.getType().getEnumConstants();
      if (consts == null || consts.length == 0) {
         return;
      }
      this.dropdownOpt = opt;
      this.ddValues = new ArrayList<>(List.of(consts));
      int maxW = 60;
      for (Object o : consts) {
         maxW = Math.max(maxW, this.field_22793.method_1727(String.valueOf(o)) + 16);
      }
      this.ddW = maxW;
      this.ddX = Math.min(ctrlRight - maxW, LOGICAL_W - maxW - 4);
      int below = rowY + rowH;
      int needed = consts.length * DD_ITEM_H + 4;
      // Flip above the row if it would overflow the panel bottom.
      this.ddY = below + needed > LOGICAL_H - 6 ? rowY - needed : below;
   }

   private void renderDropdown(class_332 ctx, int mx, int my) {
      int n = this.ddValues.size();
      int h = n * DD_ITEM_H + 4;
      TurtUIUtils.drawRoundedRect(ctx, this.ddX, this.ddY, this.ddW, h, 5, new Color(13, 16, 23, 252));
      TurtUIUtils.drawRoundedBorder(ctx, this.ddX, this.ddY, this.ddW, h, 5, Palette.alpha(Palette.GREEN, 140));
      Object cur = this.dropdownOpt.getValue();
      for (int i = 0; i < n; i++) {
         Object v = this.ddValues.get(i);
         int iy = this.ddY + 2 + i * DD_ITEM_H;
         boolean hov = TurtUIUtils.isHovered(mx, my, this.ddX + 2, iy, this.ddW - 4, DD_ITEM_H);
         boolean sel = v.equals(cur);
         if (hov) {
            TurtUIUtils.drawRoundedRect(ctx, this.ddX + 2, iy, this.ddW - 4, DD_ITEM_H, 3, Palette.BTN_HOVER);
         }
         ctx.method_51433(this.field_22793, String.valueOf(v), this.ddX + 8, iy + 3,
            (sel ? Palette.GREEN : (hov ? Palette.TEXT : Palette.TEXT_MUTED)).getRGB(), false);
         if (sel) {
            ctx.method_51433(this.field_22793, "✓", this.ddX + this.ddW - 12, iy + 3, Palette.GREEN.getRGB(), false);
         }
      }
   }

   private void cycleEnum(Option<?> opt, int dir) {
      Object[] consts = opt.getType().getEnumConstants();
      if (consts == null || consts.length == 0) {
         return;
      }
      Object cur = opt.getValue();
      int idx = 0;
      for (int i = 0; i < consts.length; i++) {
         if (consts[i].equals(cur)) {
            idx = i;
            break;
         }
      }
      idx = (idx + dir + consts.length) % consts.length;
      setOption(opt, consts[idx]);
   }

   private void openPicker(Option<?> opt) {
      this.pickerOpt = opt;
      this.editingHex = false;
      int argb = currentArgb(opt);
      this.pA = (argb >>> 24) & 0xFF;
      float[] hsb = ConfigColor.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
      this.pH = hsb[0];
      this.pS = hsb[1];
      this.pB = hsb[2];
   }

   /** Closes the picker, recording the chosen colour in the recent-colours strip. */
   private void closePicker() {
      if (this.pickerOpt != null) {
         pushRecent((this.pA << 24) | (ConfigColor.HSBtoRGB(this.pH, this.pS, this.pB) & 0xFFFFFF));
      }
      this.pickerOpt = null;
      this.editingHex = false;
   }

   private void startHexEdit() {
      this.editingHex = true;
      int argb = (this.pA << 24) | (ConfigColor.HSBtoRGB(this.pH, this.pS, this.pB) & 0xFFFFFF);
      this.hexBuf = String.format("%08X", argb);
   }

   private void commitHex() {
      String s = this.hexBuf.trim();
      this.editingHex = false;
      if (s.isEmpty()) {
         return;
      }
      try {
         long v = Long.parseLong(s, 16);
         int argb = s.length() <= 6 ? (0xFF000000 | (int) (v & 0xFFFFFF)) : (int) v;
         this.pA = (argb >>> 24) & 0xFF;
         float[] hsb = ConfigColor.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
         this.pH = hsb[0];
         this.pS = hsb[1];
         this.pB = hsb[2];
         this.applyPicker();
      } catch (NumberFormatException ignored) {
      }
   }

   private void updatePickerSV(int mx, int my) {
      this.pS = Math.max(0f, Math.min(1f, (float) (mx - this.svX) / this.svW));
      this.pB = Math.max(0f, Math.min(1f, 1f - (float) (my - this.svY) / this.svH));
      this.applyPicker();
   }

   private void updatePickerHue(int my) {
      this.pH = Math.max(0f, Math.min(1f, (float) (my - this.svY) / this.svH));
      this.applyPicker();
   }

   private void updatePickerAlpha(int mx) {
      this.pA = Math.max(0, Math.min(255, Math.round((float) (mx - this.svX) / this.svW * 255f)));
      this.applyPicker();
   }

   @SuppressWarnings("unchecked")
   private static void setOption(Option<?> opt, Object value) {
      try {
         ((Option<Object>) opt).setValue(value);
      } catch (RuntimeException ignored) {
      }
   }

   private static int currentArgb(Option<?> opt) {
      Object v = opt.getValue();
      if (v instanceof ConfigColor c) {
         return c.getRGB();
      }
      if (v instanceof Color c) {
         return c.getRGB();
      }
      return 0xFFFFFFFF;
   }

   private static String describe(Option<?> opt) {
      OptionDescription d = opt.getDescription();
      if (d != null && d.getStringSupplier() != null) {
         String s = d.getStringSupplier().get();
         return s == null || s.isBlank() ? null : s;
      }
      return null;
   }

   private static String trimFloat(double v) {
      String s = String.format("%.2f", v);
      if (s.contains(".")) {
         s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
      }
      return s;
   }

   // ── Layout walking ────────────────────────────────────────────────────────
   private interface RowVisitor {
      boolean visit(int kind, OptionGroup group, Option<?> opt, int y, int h);
   }

   private void walkRows(RowVisitor v) {
      Category cat = this.categories.get(this.activeCat);
      String q = this.search.trim().toLowerCase();
      boolean searching = !q.isEmpty();
      int y = this.listTop + 2 - (int) this.scrollY;
      for (Option<?> o : cat.options()) {
         if (matches(o, q)) {
            if (v.visit(1, null, o, y, ROW_H)) {
               return;
            }
            y += ROW_H;
         }
      }
      for (OptionGroup g : cat.optionGroups()) {
         List<Option<?>> shown = new ArrayList<>();
         for (Option<?> o : g.getOptions()) {
            if (matches(o, q)) {
               shown.add(o);
            }
         }
         if (searching && shown.isEmpty()) {
            continue;
         }
         if (v.visit(0, g, null, y, GROUP_H)) {
            return;
         }
         y += GROUP_H;
         if (searching || g.isExpanded()) {
            for (Option<?> o : shown) {
               if (v.visit(1, g, o, y, ROW_H)) {
                  return;
               }
               y += ROW_H;
            }
         }
      }
   }

   private int measureContent() {
      Category cat = this.categories.get(this.activeCat);
      String q = this.search.trim().toLowerCase();
      boolean searching = !q.isEmpty();
      int h = 2;
      for (Option<?> o : cat.options()) {
         if (matches(o, q)) {
            h += ROW_H;
         }
      }
      for (OptionGroup g : cat.optionGroups()) {
         int shown = 0;
         for (Option<?> o : g.getOptions()) {
            if (matches(o, q)) {
               shown++;
            }
         }
         if (searching && shown == 0) {
            continue;
         }
         h += GROUP_H;
         if (searching || g.isExpanded()) {
            h += shown * ROW_H;
         }
      }
      return h + 4;
   }

   private static boolean matches(Option<?> o, String q) {
      return q.isEmpty() || o.getName().toLowerCase().contains(q);
   }

   private void renderTooltip(class_332 ctx, String text, int mx, int my) {
      List<String> lines = wrap(text, 200);
      int w = 0;
      for (String l : lines) {
         w = Math.max(w, this.field_22793.method_1727(l));
      }
      int bw = w + 10;
      int bh = lines.size() * 10 + 6;
      int bx = Math.min(mx + 12, LOGICAL_W - bw - 4);
      int by = Math.min(my + 12, LOGICAL_H - bh - 4);
      TurtUIUtils.drawRoundedRect(ctx, bx, by, bw, bh, 4, new Color(14, 17, 24, 248));
      TurtUIUtils.drawRoundedBorder(ctx, bx, by, bw, bh, 4, Palette.alpha(Palette.GREEN, 120));
      int ty = by + 4;
      for (String l : lines) {
         ctx.method_51433(this.field_22793, l, bx + 5, ty, Palette.TEXT.getRGB(), false);
         ty += 10;
      }
   }

   private List<String> wrap(String text, int maxW) {
      List<String> out = new ArrayList<>();
      StringBuilder line = new StringBuilder();
      for (String word : text.split(" ")) {
         String test = line.length() == 0 ? word : line + " " + word;
         if (this.field_22793.method_1727(test) > maxW && line.length() > 0) {
            out.add(line.toString());
            line = new StringBuilder(word);
         } else {
            line = new StringBuilder(test);
         }
      }
      if (line.length() > 0) {
         out.add(line.toString());
      }
      return out;
   }

   private void drawCentered(class_332 ctx, String s, int cx, int y, Color c) {
      ctx.method_51433(this.field_22793, s, cx - this.field_22793.method_1727(s) / 2, y, c.getRGB(), false);
   }

   /** Draws an option name, colouring the part that matches the active search query in green. */
   private void drawSearchName(class_332 ctx, String name, int x, int y, Color base) {
      String q = this.search.trim().toLowerCase();
      int idx = q.isEmpty() ? -1 : name.toLowerCase().indexOf(q);
      if (idx < 0) {
         ctx.method_51433(this.field_22793, name, x, y, base.getRGB(), false);
         return;
      }
      String pre = name.substring(0, idx);
      String mid = name.substring(idx, idx + q.length());
      String post = name.substring(idx + q.length());
      int cx = x;
      ctx.method_51433(this.field_22793, pre, cx, y, base.getRGB(), false);
      cx += this.field_22793.method_1727(pre);
      ctx.method_51433(this.field_22793, mid, cx, y, Palette.GREEN.getRGB(), false);
      cx += this.field_22793.method_1727(mid);
      ctx.method_51433(this.field_22793, post, cx, y, base.getRGB(), false);
   }

   /** Done button: save, show a brief "Saved ✓" pulse, then close. */
   private void requestDone() {
      this.commitNumEdit();
      this.config.runSave();
      this.savedPulse = 1f;
      this.closeAtNs = System.nanoTime() + 600_000_000L;
      this.donePress = 1f;
      TurtSounds.confirm();
   }

   public void method_25419() {
      this.commitNumEdit();
      this.config.runSave();
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
