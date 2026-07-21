package com.turtmod.cosmetics;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * A LabyMod-style cape changer for the capes your Mojang account actually owns. Lists them in a grid (each
 * card shows the real cape texture), and applying one switches your ACTIVE cape on your account via
 * {@link CapeService} — a real, persistent change everyone sees. Mirrors {@link CosmeticsScreen}'s chrome
 * and logical-space scaling so it feels like part of the same suite.
 */
public class CapeScreen extends class_437 {
   private final class_437 parent;
   private List<CapeService.Cape> capes = new ArrayList<>();
   private boolean loading = true;
   private boolean loadFailed = false;
   private int selected = -1;
   private float scrollPx = 0f;
   private String statusMessage = "";
   private int messageTicks = 0;
   private float openFade = 0f;
   private long lastFrameNs = System.nanoTime();

   private final List<TurtUIButton> buttons = new ArrayList<>();
   private static final int LOGICAL_W = 580;
   private static final int LOGICAL_H = 400;
   private final TurtUIScale uiScale = new TurtUIScale();
   private int panelX, panelY, panelW, panelH;
   private int gridX, gridY, gridW, gridH;

   private static final Color PANEL_BORDER = new Color(9289311, true);
   private static final Color ACCENT_GREEN = new Color(9289311, false);
   private static final Color ACCENT_PINK = new Color(16752046, false);
   private static final Color TEXT_MAIN = new Color(16775399, false);
   private static final Color BTN_BG = new Color(2433054, true);
   private static final Color BTN_HOVER = new Color(3482400, true);

   // Grid metrics (logical space).
   private static final int CARD_W = 78;
   private static final int CARD_H = 118;   // cape front is 10:16, plus a label strip
   private static final int CARD_GAP = 10;

   public CapeScreen(class_437 parent) {
      super(class_2561.method_43470("TurtMod Capes"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.layoutPanels();
      TurtUITheme btnTheme = new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
      this.buttons.clear();

      int sx = this.panelX + 6;
      int sw = TurtLauncher.SIDEBAR_W - 12;
      int sy = this.panelY + TurtLauncher.HEADER_H + 8;
      int gap = 4;
      int bh = 18;
      int group = 10;

      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Apply Cape", btnTheme, this::applySelected)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "No Cape", btnTheme, this::removeCape)); sy += bh + group;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Refresh", btnTheme, this::reload));

      int backY = this.panelY + this.panelH - TurtLauncher.FOOTER_H - bh - 6;
      this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back", btnTheme, this::method_25419));

      if (this.capes.isEmpty() && this.loading) {
         this.reload();
      }
   }

   private void layoutPanels() {
      this.panelW = LOGICAL_W;
      this.panelH = LOGICAL_H;
      this.panelX = 0;
      this.panelY = 0;
      this.gridX = TurtLauncher.contentX(this.panelX);
      this.gridY = TurtLauncher.contentY(this.panelY);
      this.gridW = TurtLauncher.contentW(this.panelW);
      this.gridH = TurtLauncher.contentH(this.panelH);
   }

   /** Fetch owned capes off-thread, then hop back to update the list. */
   private void reload() {
      this.loading = true;
      this.loadFailed = false;
      this.setStatus("Loading your capes...");
      new Thread(() -> {
         List<CapeService.Cape> owned = CapeService.fetchOwnedCapes();
         this.field_22787.execute(() -> {
            this.loading = false;
            if (owned == null) {
               this.loadFailed = true;
               this.capes = new ArrayList<>();
               this.setStatus("Couldn't load capes - are you signed in online?");
               return;
            }
            this.capes = owned;
            this.loadFailed = false;
            // Preselect the currently active cape.
            this.selected = -1;
            for (int i = 0; i < owned.size(); i++) {
               if (owned.get(i).active) {
                  this.selected = i;
                  break;
               }
            }
            this.setStatus(owned.isEmpty() ? "Your account owns no capes." : owned.size() + " cape(s) found.");
         });
      }, "turtmod-cape-list").start();
   }

   private void applySelected() {
      if (this.selected < 0 || this.selected >= this.capes.size()) {
         this.setStatus("Pick a cape first.");
         return;
      }
      CapeService.Cape cape = this.capes.get(this.selected);
      this.setStatus("Applying " + cape.alias + "...");
      new Thread(() -> {
         boolean ok = CapeService.setActive(cape.id);
         this.field_22787.execute(() -> {
            if (ok) {
               this.setStatus(cape.alias + " set on your account - rejoin to see it.");
               this.reload();
            } else {
               this.setStatus("Couldn't set that cape.");
            }
         });
      }, "turtmod-cape-apply").start();
   }

   private void removeCape() {
      this.setStatus("Removing cape...");
      new Thread(() -> {
         boolean ok = CapeService.removeActive();
         this.field_22787.execute(() -> {
            if (ok) {
               this.setStatus("Cape removed - rejoin to see it.");
               this.reload();
            } else {
               this.setStatus("Couldn't remove the cape.");
            }
         });
      }, "turtmod-cape-remove").start();
   }

   private void setStatus(String msg) {
      this.statusMessage = msg;
      this.messageTicks = 100;
   }

   private int cols() {
      return Math.max(1, (this.gridW + CARD_GAP) / (CARD_W + CARD_GAP));
   }

   private int viewTop() { return this.gridY + 22; }
   private int viewH()   { return this.gridH - 28; }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastFrameNs) / 1_000_000_000f, 0.1f);
      lastFrameNs = now;
      openFade = TurtUIUtils.lerp01(openFade, 1f, dt, 12f);

      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(ctx, mx, my);
      TurtUIUtils.update();

      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      mx = (int) this.uiScale.toLogicalX(mx);
      my = (int) this.uiScale.toLogicalY(my);
      this.uiScale.push(ctx);

      ctx.method_51448().pushMatrix();
      float introE = TurtUIUtils.ease(openFade);
      ctx.method_51448().translate(LOGICAL_W / 2f, LOGICAL_H / 2f + (1f - introE) * 12f);
      ctx.method_51448().scale(0.97f + 0.03f * introE, 0.97f + 0.03f * introE);
      ctx.method_51448().translate(-LOGICAL_W / 2f, -LOGICAL_H / 2f);

      String player = this.field_22787 != null && this.field_22787.method_1548() != null
         ? this.field_22787.method_1548().method_1676() : "Player";

      TurtLauncher.drawChrome(ctx, this.field_22793, this.panelX, this.panelY, this.panelW, this.panelH,
         "Capes", player, "Cosmetics");

      renderGrid(ctx, mx, my);

      if (this.messageTicks > 0) {
         ctx.method_25300(this.field_22793, this.statusMessage, this.gridX + this.gridW / 2, this.gridY - 1, ACCENT_GREEN.getRGB());
         --this.messageTicks;
      }

      for (TurtUIButton btn : this.buttons) btn.render(ctx, mx, my, this.field_22793);

      ctx.method_51448().popMatrix();
      this.uiScale.pop(ctx);
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   private void renderGrid(class_332 ctx, int mx, int my) {
      TurtLauncher.drawContentPanel(ctx, this.field_22793, this.gridX, this.gridY, this.gridW, this.gridH, "YOUR CAPES");

      int viewTop = this.viewTop();
      int viewH = this.viewH();

      if (this.loading) {
         long t = System.currentTimeMillis() / 300 % 3;
         String dots = t == 0 ? "." : t == 1 ? ".." : "...";
         ctx.method_25300(this.field_22793, "Loading" + dots, this.gridX + this.gridW / 2, viewTop + viewH / 2 - 4, 0xFF888888);
         return;
      }
      if (this.loadFailed) {
         ctx.method_25300(this.field_22793, "Couldn't reach your account", this.gridX + this.gridW / 2, viewTop + viewH / 2 - 8, 0xFFFF6666);
         ctx.method_25300(this.field_22793, "Sign in online, then Refresh", this.gridX + this.gridW / 2, viewTop + viewH / 2 + 4, 0xFF888888);
         return;
      }
      if (this.capes.isEmpty()) {
         ctx.method_25300(this.field_22793, "No capes on this account", this.gridX + this.gridW / 2, viewTop + viewH / 2 - 4, 0xFF888888);
         return;
      }

      int cols = cols();
      int usedW = cols * CARD_W + (cols - 1) * CARD_GAP;
      int startX = this.gridX + (this.gridW - usedW) / 2;

      ctx.method_44379(this.gridX + 1, viewTop, this.gridX + this.gridW - 1, viewTop + viewH);
      for (int i = 0; i < this.capes.size(); i++) {
         int col = i % cols, row = i / cols;
         int cardX = startX + col * (CARD_W + CARD_GAP);
         int cardY = viewTop + row * (CARD_H + CARD_GAP) - (int) this.scrollPx;
         if (cardY + CARD_H <= viewTop || cardY >= viewTop + viewH) continue;
         renderCard(ctx, this.capes.get(i), cardX, cardY, i == this.selected, mx, my);
      }
      ctx.method_44380();
   }

   private void renderCard(class_332 ctx, CapeService.Cape cape, int x, int y, boolean selected, int mx, int my) {
      boolean hovered = mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H;
      int bg = selected ? 0x48000000 : (hovered ? 0x33000000 : 0x1E000000);
      TurtUIUtils.drawRoundedRect(ctx, x, y, CARD_W, CARD_H, 5, new Color(bg, true));

      // Cape image area (front of the cape), preserving the 10:16 aspect.
      int imgW = CARD_W - 24;
      int imgH = imgW * 16 / 10;
      int imgX = x + (CARD_W - imgW) / 2;
      int imgY = y + 8;
      TurtUIUtils.drawRoundedRect(ctx, imgX - 2, imgY - 2, imgW + 4, imgH + 4, 3, new Color(0, 0, 0, 140));

      CapeTextureCache.Entry tex = cape.url != null ? CapeTextureCache.get(cape.url) : null;
      if (tex != null && tex.id != null) {
         float sx = tex.texW / 64f, sy = tex.texH / 32f;
         ctx.method_25293(class_10799.field_56883, tex.id, imgX, imgY, 1f * sx, 1f * sy,
            imgW, imgH, Math.max(1, (int) (10 * sx)), Math.max(1, (int) (16 * sy)), tex.texW, tex.texH, -1);
      } else {
         ctx.method_25300(this.field_22793, "...", imgX + imgW / 2, imgY + imgH / 2 - 4, 0xFF666666);
      }

      // Name strip.
      String name = cape.alias;
      if (name.length() > 12) name = name.substring(0, 11) + "..";
      ctx.method_25300(this.field_22793, name, x + CARD_W / 2, y + CARD_H - 14,
         selected ? ACCENT_GREEN.getRGB() : TEXT_MAIN.getRGB());

      if (cape.active) {
         // Small "ACTIVE" badge in the corner.
         ctx.method_51433(this.field_22793, "●", imgX + 2, imgY + 2, ACCENT_GREEN.getRGB(), false);
      }
      if (selected) {
         TurtUIUtils.drawRoundedBorder(ctx, x, y, CARD_W, CARD_H, 5, ACCENT_GREEN);
      } else {
         TurtUIUtils.drawRoundedBorder(ctx, x, y, CARD_W, CARD_H, 5, new Color(255, 255, 255, 24));
      }
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = this.uiScale.toLogicalX(click.comp_4798()), my = this.uiScale.toLogicalY(click.comp_4799());
      int button = click.method_74245();
      for (TurtUIButton btn : this.buttons) if (btn.mouseClicked(mx, my, button)) return true;

      int viewTop = this.viewTop();
      int viewH = this.viewH();
      if (!this.capes.isEmpty() && mx >= this.gridX && mx <= this.gridX + this.gridW && my >= viewTop && my <= viewTop + viewH) {
         int cols = cols();
         int usedW = cols * CARD_W + (cols - 1) * CARD_GAP;
         int startX = this.gridX + (this.gridW - usedW) / 2;
         for (int i = 0; i < this.capes.size(); i++) {
            int col = i % cols, row = i / cols;
            int cardX = startX + col * (CARD_W + CARD_GAP);
            int cardY = viewTop + row * (CARD_H + CARD_GAP) - (int) this.scrollPx;
            if (mx >= cardX && mx <= cardX + CARD_W && my >= cardY && my <= cardY + CARD_H) {
               this.selected = i;
               this.setStatus("Selected: " + this.capes.get(i).alias);
               return true;
            }
         }
      }
      return super.method_25402(click, bl);
   }

   public boolean method_25401(double mx, double my, double ha, double va) {
      int cols = cols();
      int rows = (this.capes.size() + cols - 1) / cols;
      float maxScroll = Math.max(0f, rows * (CARD_H + CARD_GAP) - this.viewH());
      if (maxScroll > 0f) {
         this.scrollPx -= (float) va * 30f;
         this.scrollPx = Math.max(0f, Math.min(maxScroll, this.scrollPx));
         return true;
      }
      return super.method_25401(mx, my, ha, va);
   }

   public void method_25419() {
      if (this.field_22787 != null) this.field_22787.method_1507(this.parent);
   }
}
