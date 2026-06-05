package com.turtmod.hud;

import com.turtmod.TurtModClient;
import com.turtmod.config.ConfigManager;
import com.turtmod.config.TurtModConfig;
import com.turtmod.ui.BrandingRenderer;
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

   private static final Color PANEL_BG    = new Color(1709588, true);
   private static final Color PANEL_BORDER= new Color(9289311, true);
   private static final Color ACCENT_GREEN= new Color(9289311, false);
   private static final Color ACCENT_PINK = new Color(16752046, false);
   private static final Color TEXT_MAIN   = new Color(16775399, false);
   private static final Color BTN_BG      = new Color(2433054, true);
   private static final Color BTN_HOVER   = new Color(3482400, true);

   // Sidebar panel geometry (left side, top-anchored)
   private static final int SIDE_W    = 130;
   private static final int SIDE_PAD  = 8;
   private static final int SIDE_ITEM = 20;

   // Compact chrome: smaller header + footer so the editor keeps more screen space
   private static final int HEADER_H  = 18;
   private static final int FOOTER_H  = 22;

   public HudEditorScreen(class_437 parent) {
      super(class_2561.method_43470("HUD Editor"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.cfg = TurtModClient.getConfig();
      com.turtmod.ui.TurtUITheme btnTheme = new com.turtmod.ui.TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
      int btnH = 14;
      int btnY = this.field_22790 - FOOTER_H + (FOOTER_H - btnH) / 2;
      this.buttons.clear();

      // Left side: snap controls
      int bx = SIDE_W + SIDE_PAD + 4;
      this.buttons.add(new TurtUIButton(bx, btnY, 72, btnH,
         this.cfg.hud.snapToGrid ? "Grid ON" : "Grid OFF", btnTheme, () -> {
            this.cfg.hud.snapToGrid = !this.cfg.hud.snapToGrid;
            ConfigManager.save(this.cfg); this.method_25426();
         }));
      this.buttons.add(new TurtUIButton(bx + 76, btnY, 88, btnH,
         this.cfg.hud.snapToCenter ? "Center ON" : "Center OFF", btnTheme, () -> {
            this.cfg.hud.snapToCenter = !this.cfg.hud.snapToCenter;
            ConfigManager.save(this.cfg); this.method_25426();
         }));
      this.buttons.add(new TurtUIButton(bx + 168, btnY, 60, btnH,
         "Grid " + this.cfg.hud.gridSize, btnTheme, () -> {
            this.cfg.hud.gridSize = this.cfg.hud.gridSize >= 32 ? 4 : this.cfg.hud.gridSize + 4;
            ConfigManager.save(this.cfg); this.method_25426();
         }));

      // Right side: actions — right-aligned with gaps so nothing overlaps.
      int gap = 4;
      int doneW = 50, resetW = 78;
      int doneX = this.field_22789 - 6 - doneW;
      int resetAllX = doneX - gap - resetW;
      int resetScaleX = resetAllX - gap - resetW;
      this.buttons.add(new TurtUIButton(resetScaleX, btnY, resetW, btnH, "Reset Scale", btnTheme,
         () -> HudEditorFeature.resetSelectedScale(TurtModClient.getConfig())));
      this.buttons.add(new TurtUIButton(resetAllX, btnY, resetW, btnH, "Reset All", btnTheme,
         () -> HudEditorFeature.resetAllPositions(TurtModClient.getConfig())));
      this.buttons.add(new TurtUIButton(doneX, btnY, doneW, btnH, "Done", btnTheme,
         this::method_25419));
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastFrameNs) / 1_000_000_000f, 0.1f);
      lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1.0f, dt, 12f);

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

      // ── 5. Left sidebar panel
      renderSidebar(ctx, mx, my);

      // ── 6. Bottom control strip
      int stripY = this.field_22790 - FOOTER_H;
      ctx.method_25294(0, stripY, this.field_22789, this.field_22790, 0xDD000000);
      ctx.method_25294(0, stripY, this.field_22789, stripY + 1, ACCENT_GREEN.getRGB());

      for (TurtUIButton btn : this.buttons)
         btn.render(ctx, mx, my, this.field_22793);

      // Smooth fade-in from black when the editor opens (~180ms)
      if (this.openFade < 0.99f) {
         int a = (int)((1.0f - this.openFade) * 255.0f) & 0xFF;
         ctx.method_25294(0, 0, this.field_22789, this.field_22790, a << 24);
      }

      super.method_25394(ctx, mx, my, delta);
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

   private void renderSidebar(class_332 ctx, int mx, int my) {
      // Sidebar removed — use full screen for HUD editing space
      // Just draw a compact top bar with logo + title
      ctx.method_25294(0, 0, this.field_22789, HEADER_H, 0xDD000000);
      ctx.method_25294(0, HEADER_H, this.field_22789, HEADER_H + 1, ACCENT_GREEN.getRGB());
      int logoSz = HEADER_H - 6;
      BrandingRenderer.drawLogo(ctx, 5, 3, logoSz, logoSz);
      int textY = (HEADER_H - 8) / 2;
      TurtUIUtils.drawGradientText(ctx, this.field_22793, "TurtMod  HUD Editor",
         5 + logoSz + 5, textY, ACCENT_GREEN, ACCENT_PINK, false, false);
      TurtUIUtils.drawText(ctx, this.field_22793, "Drag to move  ·  Scroll = scale  ·  R = reset",
         this.field_22789 - 6, textY, new Color(0x99AAAAAA, true), true, false);
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
