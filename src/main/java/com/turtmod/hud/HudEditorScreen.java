package com.turtmod.hud;

import com.turtmod.TurtModClient;
import com.turtmod.config.ConfigManager;
import com.turtmod.config.TurtModConfig;
import com.turtmod.ui.BrandingRenderer;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class HudEditorScreen extends class_437 {
   private final class_437 parent;
   private final List<TurtUIButton> buttons = new ArrayList();
   private TurtModConfig cfg;

   // Per-element hover glow state
   private final float[] elemGlow = new float[HudEditorFeature.Anchor.values().length];
   private long lastFrameNs = System.nanoTime();
   private float openFade = 0.0F;

   // Colours come from the central Palette so a re-theme is one-file (values are identical).
   private static final Color PANEL_BORDER = Palette.PANEL_BORDER;
   private static final Color ACCENT_GREEN = Palette.GREEN;
   private static final Color ACCENT_PINK  = Palette.PINK;
   private static final Color TEXT_MAIN    = Palette.TEXT;
   private static final Color BTN_BG       = Palette.BTN_BG;
   private static final Color BTN_HOVER    = Palette.BTN_HOVER;
   private float resetFlash = 0f;  // green confirmation flash after Reset

   // Compact floating toolbar geometry (centered along the bottom edge).
   private int barX, barY, barW, barH;

   public HudEditorScreen(class_437 parent) {
      super(class_2561.method_43470("HUD Editor"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.cfg = TurtModClient.getConfig();
      com.turtmod.ui.TurtUITheme btnTheme = new com.turtmod.ui.TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
      this.buttons.clear();

      // One compact, auto-sized, centered toolbar — no full-width chrome, so the
      // whole screen stays visible for placing HUD elements.
      String[] labels = {
         this.cfg.hud.snapToGrid   ? "Grid: On"   : "Grid: Off",
         this.cfg.hud.snapToCenter ? "Center: On" : "Center: Off",
         "Grid " + this.cfg.hud.gridSize,
         "Reset",
         "Done"
      };
      Runnable[] actions = {
         () -> { this.cfg.hud.snapToGrid   = !this.cfg.hud.snapToGrid;   ConfigManager.save(this.cfg); this.method_25426(); },
         () -> { this.cfg.hud.snapToCenter = !this.cfg.hud.snapToCenter; ConfigManager.save(this.cfg); this.method_25426(); },
         () -> { this.cfg.hud.gridSize = this.cfg.hud.gridSize >= 32 ? 4 : this.cfg.hud.gridSize + 4; ConfigManager.save(this.cfg); this.method_25426(); },
         () -> { HudEditorFeature.resetAllPositions(TurtModClient.getConfig()); HudEditorFeature.resetSelectedScale(TurtModClient.getConfig()); this.resetFlash = 1f; },
         this::method_25419
      };

      int btnH = 13;
      int gap = 3;
      int hpad = 6; // text padding inside each button

      int[] widths = new int[labels.length];
      int total = 0;
      for (int i = 0; i < labels.length; i++) {
         widths[i] = this.field_22793.method_1727(labels[i]) + hpad * 2;
         total += widths[i];
      }
      total += gap * (labels.length - 1);

      // Centered on screen (both axes) — most people don't place HUDs dead-center,
      // and the row is small enough to leave room if they do.
      int startX = (this.field_22789 - total) / 2;
      int btnY = (this.field_22790 - btnH) / 2;

      this.barW = total;
      this.barH = btnH;
      this.barX = startX;
      this.barY = btnY;

      int x = startX;
      for (int i = 0; i < labels.length; i++) {
         this.buttons.add(new TurtUIButton(x, btnY, widths[i], btnH, labels[i], btnTheme, actions[i]));
         x += widths[i] + gap;
      }
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastFrameNs) / 1_000_000_000f, 0.1f);
      lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1.0f, dt, 12f);
      this.resetFlash = TurtUIUtils.lerp01(this.resetFlash, 0f, dt, 3.5f);

      TurtUIUtils.update();
      int sw = this.field_22787.method_22683().method_4486();
      int sh = this.field_22787.method_22683().method_4502();

      // ── 1. Full-screen desaturated backdrop
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, 0xBB000000);

      // ── 2. Dot-grid overlay (clean, minimal)
      int gridSize = this.cfg.hud.snapToGrid ? this.cfg.hud.gridSize : 24;
      for (int x = 0; x <= sw; x += gridSize)
         for (int y = 0; y <= sh; y += gridSize)
            ctx.method_25294(x, y, x + 1, y + 1, 0x18FFFFFF);

      // ── 3. Center guide lines
      int cx = sw / 2, cy = sh / 2;
      ctx.method_25294(cx, 0, cx + 1, sh, 0x30FFFFFF);
      ctx.method_25294(0, cy, sw, cy + 1, 0x30FFFFFF);

      // ── 4. Per-element cards (group backgrounds + labels)
      renderElementCards(ctx, mx, my, dt);

      // ── 4a. Figma-style alignment guides + snap pulse + live position chip while dragging.
      drawDragGuides(ctx);

      // ── 4b. Small TurtMod logo, top-left corner (no header bar)
      BrandingRenderer.drawLogo(ctx, 6, 6, 16, 16);

      // ── 4b2. Special (HUD Editor): live FPS readout, top-right, so you can gauge cost while editing.
      String fpsStr = this.field_22787.method_47599() + " fps";
      int fpsW = this.field_22793.method_1727(fpsStr) + 12;
      int fpsX = this.field_22789 - fpsW - 6;
      TurtUIUtils.drawRoundedRect(ctx, fpsX, 8, fpsW, 14, 4, new Color(0x66000000, true));
      TurtUIUtils.drawRoundedBorder(ctx, fpsX, 8, fpsW, 14, 4, new Color(255, 255, 255, 22));
      ctx.method_51433(this.field_22793, fpsStr, fpsX + 6, 11, 0xFF8CE05B, false);

      // ── 4c. Hint chip near the top so the editor is self-explanatory (Lunar-style helper).
      String hint = "Drag to move  •  Scroll to scale  •  Esc to save";
      int hintW = this.field_22793.method_1727(hint) + 14;
      int hintX = (this.field_22789 - hintW) / 2;
      TurtUIUtils.drawRoundedRect(ctx, hintX, 8, hintW, 14, 4, new Color(0x66000000, true));
      TurtUIUtils.drawRoundedBorder(ctx, hintX, 8, hintW, 14, 4, new Color(255, 255, 255, 22));
      ctx.method_51433(this.field_22793, hint, hintX + 7, 11, 0xFFCCCCCC, false);

      // ── 5. Floating rounded toolbar panel with animated (rounded/hover/press) buttons.
      int pad = 4;
      TurtUIUtils.drawShadow(ctx, this.barX - pad, this.barY - pad, this.barW + pad * 2, this.barH + pad * 2, 6);
      TurtUIUtils.drawRoundedRect(ctx, this.barX - pad, this.barY - pad, this.barW + pad * 2, this.barH + pad * 2, 5, new Color(0xCC10131A, true));
      TurtUIUtils.drawRoundedBorder(ctx, this.barX - pad, this.barY - pad, this.barW + pad * 2, this.barH + pad * 2, 5, PANEL_BORDER);
      for (TurtUIButton btn : this.buttons) {
         btn.render(ctx, mx, my, this.field_22793);
      }

      // Inline "reset" confirmation: a brief green wash over the screen (no toast).
      if (this.resetFlash > 0.02f) {
         ctx.method_25294(0, 0, this.field_22789, this.field_22790,
            ((int) (55 * this.resetFlash) << 24) | (ACCENT_GREEN.getRGB() & 0xFFFFFF));
      }

      // Smooth fade-in from black when the editor opens (~180ms)
      if (this.openFade < 0.99f) {
         int a = (int)((1.0f - this.openFade) * 255.0f) & 0xFF;
         ctx.method_25294(0, 0, this.field_22789, this.field_22790, a << 24);
      }

      super.method_25394(ctx, mx, my, delta);
   }

   /** Draws alignment guides (vs other elements), a snap pulse, and a position chip for the drag. */
   private void drawDragGuides(class_332 ctx) {
      HudEditorFeature.Anchor d = HudEditorFeature.getDragging();
      if (d == null) {
         return;
      }
      class_310 c = this.field_22787;
      int sw = this.field_22789;
      int sh = this.field_22790;
      int dx = HudEditorFeature.getX(d, c, this.cfg);
      int dy = HudEditorFeature.getY(d, c, this.cfg);
      int dw = HudEditorFeature.getWidth(d, c, this.cfg);
      int dh = HudEditorFeature.getHeight(d, c, this.cfg);
      int[] dXs = { dx, dx + dw / 2, dx + dw };
      int[] dYs = { dy, dy + dh / 2, dy + dh };
      int guide = (0xCC << 24) | (ACCENT_PINK.getRGB() & 0xFFFFFF);

      for (HudEditorFeature.Anchor o : HudEditorFeature.Anchor.values()) {
         if (o == d || o == HudEditorFeature.Anchor.ZOOM || !HudEditorFeature.isEnabled(o, this.cfg)) {
            continue;
         }
         int ox = HudEditorFeature.getX(o, c, this.cfg);
         int oy = HudEditorFeature.getY(o, c, this.cfg);
         int ow = HudEditorFeature.getWidth(o, c, this.cfg);
         int oh = HudEditorFeature.getHeight(o, c, this.cfg);
         int[] oXs = { ox, ox + ow / 2, ox + ow };
         int[] oYs = { oy, oy + oh / 2, oy + oh };
         for (int a : dXs) {
            for (int b : oXs) {
               if (Math.abs(a - b) <= 4) {
                  ctx.method_25294(b, Math.min(dy, oy) - 4, b + 1, Math.max(dy + dh, oy + oh) + 4, guide);
               }
            }
         }
         for (int a : dYs) {
            for (int b : oYs) {
               if (Math.abs(a - b) <= 4) {
                  ctx.method_25294(Math.min(dx, ox) - 4, b, Math.max(dx + dw, ox + ow) + 4, b + 1, guide);
               }
            }
         }
      }

      // Snap pulse: an expanding white border flash shortly after a center-snap.
      long age = System.nanoTime() - HudEditorFeature.snapPulseNs;
      if (age >= 0L && age < 220_000_000L) {
         float p = 1f - age / 220_000_000f;
         int exp = (int) (6 * (1f - p));
         ctx.method_73198(dx - 2 - exp, dy - 2 - exp, dw + 4 + exp * 2, dh + 4 + exp * 2, ((int) (220 * p) << 24) | 0xFFFFFF);
      }

      // Live position chip near the dragged element.
      String pos = dx + ", " + dy + "  (" + dw + "×" + dh + ")";
      int pw = this.field_22793.method_1727(pos) + 10;
      int pxc = Math.max(2, Math.min(dx, sw - pw - 2));
      int pyc = (dy + dh + 13 <= sh) ? dy + dh + 2 : dy - 13;
      TurtUIUtils.drawRoundedRect(ctx, pxc, pyc, pw, 11, 3, new Color(0xCC10131A, true));
      TurtUIUtils.drawRoundedBorder(ctx, pxc, pyc, pw, 11, 3, ACCENT_PINK);
      ctx.method_51433(this.field_22793, pos, pxc + 5, pyc + 2, 0xFFFFFFFF, false);
   }

   private void renderElementCards(class_332 ctx, int mx, int my, float dt) {
      class_310 client = this.field_22787;
      HudEditorFeature.Anchor[] anchors = HudEditorFeature.Anchor.values();
      for (int i = 0; i < anchors.length; i++) {
         HudEditorFeature.Anchor anchor = anchors[i];
         if (!HudEditorFeature.isEnabled(anchor, this.cfg)) continue;
         int ex = HudEditorFeature.getX(anchor, client, this.cfg);
         int ey = HudEditorFeature.getY(anchor, client, this.cfg);
         int ew = HudEditorFeature.getWidth(anchor, client, this.cfg);
         int eh = HudEditorFeature.getHeight(anchor, client, this.cfg);
         boolean hov = mx >= ex - 6 && mx <= ex + ew + 6 && my >= ey - 6 && my <= ey + eh + 6;
         boolean sel = HudEditorFeature.isSelected(anchor);

         // Animate per-element glow
         elemGlow[i] = TurtUIUtils.lerp01(elemGlow[i], (hov || sel) ? 1f : 0f, dt, 10f);
         float g = elemGlow[i];

         // Card background
         int bg = sel ? 0x60000000 : (hov ? 0x40FFFFFF : 0x25000000);
         TurtUIUtils.drawRoundedRect(ctx, ex - 6, ey - 6, ew + 12, eh + 12, 4, new Color(bg, true));

         // Glow border
         if (sel) {
            // Bright green border for selected
            TurtUIUtils.drawHoverGlow(ctx, ex - 4, ey - 4, ew + 8, eh + 8, 4, g, ACCENT_GREEN);
            ctx.method_73198(ex - 4, ey - 4, ew + 8, eh + 8, ACCENT_GREEN.getRGB());
         } else if (g > 0.02f) {
            TurtUIUtils.drawHoverGlow(ctx, ex - 4, ey - 4, ew + 8, eh + 8, 4, g, ACCENT_PINK);
            ctx.method_73198(ex - 4, ey - 4, ew + 8, eh + 8, new Color(
               ACCENT_PINK.getRed(), ACCENT_PINK.getGreen(), ACCENT_PINK.getBlue(), (int)(g * 180)
            ).getRGB());
         } else {
            ctx.method_73198(ex - 4, ey - 4, ew + 8, eh + 8, 0x40666666);
         }

         // Name label chip above element
         String name = getAnchorName(anchor);
         int chipW = client.field_1772.method_1727(name) + 8;
         int chipX = ex - 4 + (ew + 8) / 2 - chipW / 2;
         int chipY = ey - 17;
         int chipBg = sel ? ACCENT_GREEN.getRGB() : (hov ? 0xBB333333 : 0x88222222);
         TurtUIUtils.drawRoundedRect(ctx, chipX, chipY, chipW, 11, 2, new Color(chipBg, true));
         ctx.method_25300(client.field_1772, name, chipX + chipW / 2, chipY + 2,
            sel ? 0xFF000000 : (int)(g * 255) << 24 | (TEXT_MAIN.getRGB() & 0xFFFFFF) | 0xFF000000);
      }
      // Let HudEditorFeature draw the actual HUD widgets + selection handles
      HudEditorFeature.render(ctx, client, this.cfg, mx, my);
   }

   private static String getAnchorName(HudEditorFeature.Anchor a) {
      return switch (a) {
         case ARMOR         -> "Armor";
         case POTION        -> "Potions";
         case OVERLAY       -> "FPS/Ping";
         case DEBUG         -> "Clean F3";
         case REACH         -> "Reach";
         case SPRINT        -> "Sprint";
         case KEYSTROKES    -> "Keystrokes";
         case CPS_COUNTER   -> "CPS";
         case INVENTORY     -> "Inventory";
         case COORDINATES   -> "Coords";
         default            -> a.name();
      };
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = click.comp_4798(), my = click.comp_4799();
      int button = click.method_74245();
      // Check UI buttons FIRST — otherwise clicking "Reset Scale"/"Reset All" would fall
      // through to HudEditorFeature.mouseClicked, which clears the current selection before
      // the button's handler runs (so Reset Scale would have nothing selected to reset).
      if (button == 0)
         for (TurtUIButton btn : this.buttons)
            if (btn.mouseClicked((int)mx, (int)my, button)) return true;
      if (HudEditorFeature.mouseClicked(mx, my, button, this.field_22787, TurtModClient.getConfig())) return true;
      return super.method_25402(click, bl);
   }

   public boolean method_25406(class_11909 click) {
      HudEditorFeature.mouseReleased(click.comp_4798(), click.comp_4799(), click.method_74245());
      return super.method_25406(click);
   }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      HudEditorFeature.mouseDragged(click.comp_4798(), click.comp_4799(), click.method_74245(), dx, dy, this.field_22787, TurtModClient.getConfig());
      return super.method_25403(click, dx, dy);
   }

   public boolean method_25401(double mx, double my, double ha, double va) {
      return HudEditorFeature.mouseScrolled(mx, my, va, TurtModClient.getConfig()) || super.method_25401(mx, my, ha, va);
   }

   public boolean method_25404(class_11908 input) {
      if (input.comp_4795() == 256) { this.method_25419(); return true; }
      return HudEditorFeature.keyPressed(input, TurtModClient.getConfig()) || super.method_25404(input);
   }

   public void method_25419() {
      ConfigManager.save(TurtModClient.getConfig());
      if (this.field_22787 != null) this.field_22787.method_1507(this.parent);
   }

   public boolean method_25421() { return false; }
}
