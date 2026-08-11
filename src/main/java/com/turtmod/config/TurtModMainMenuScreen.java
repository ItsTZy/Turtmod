package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.cosmetics.CosmeticsScreen;
import com.turtmod.gallery.ScreenshotGalleryScreen;
import com.turtmod.hud.HudEditorScreen;
import com.turtmod.ui.BrandingRenderer;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class TurtModMainMenuScreen extends class_437 {
   private final class_437 parent;
   private float openFade = 0f;
   private long lastFrameNs = System.nanoTime();
   private int panelX, panelY, panelW, panelH;
   // Fixed logical layout scaled to fit any resolution / GUI scale.
   private static final int LOGICAL_W = 460;
   private static final int LOGICAL_H = 310;
   private final TurtUIScale uiScale = new TurtUIScale();

   // Nav items — sidebar left
   private static final String[] NAV_LABELS = {
      "HUD Editor", "Skin Changer", "Settings", "Gallery", "─────", "Back"
   };
   private static final boolean[] NAV_IS_SEPARATOR = {false, false, false, false, true, false};

   public TurtModMainMenuScreen(class_437 parent) {
      super(class_2561.method_43470("TurtMod"));
      this.parent = parent;
   }

   protected void method_25426() {
      // Lay out at a fixed logical size; method_25394 scales it to fit the screen.
      panelX = 0;
      panelY = 0;
      panelW = LOGICAL_W;
      panelH = LOGICAL_H;
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastFrameNs) / 1_000_000_000f, 0.1f);
      lastFrameNs = now;
      openFade = TurtUIUtils.lerp01(openFade, 1f, dt, 12f);

      TurtUIUtils.update();
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);

      // Fit the fixed logical panel to the screen (any resolution / GUI scale), then translate mouse
      // into logical space for hover/clicks.
      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      mx = (int)this.uiScale.toLogicalX(mx);
      my = (int)this.uiScale.toLogicalY(my);
      this.uiScale.push(ctx);

      // Scale-in + slide-up on open (subtle, premium feel)
      float cx = panelX + panelW / 2f, cy = panelY + panelH / 2f;
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(0f, (1f - openFade) * 7f);
      ctx.method_51448().translate(cx, cy);
      float s = 0.95f + 0.05f * openFade;
      ctx.method_51448().scale(s, s);
      ctx.method_51448().translate(-cx, -cy);

      String player = this.field_22787 != null && this.field_22787.method_1548() != null
         ? this.field_22787.method_1548().method_1676() : "Player";

      // ── Chrome frame (logo auto-drawn in header)
      TurtLauncher.drawChrome(ctx, this.field_22793, panelX, panelY, panelW, panelH,
         "TurtMod", player, "1.21.11");

      // ── Sidebar nav
      for (int i = 0; i < NAV_LABELS.length; i++) {
         if (NAV_IS_SEPARATOR[i]) {
            int sy = TurtLauncher.navItemY(panelY, i) + TurtLauncher.NAV_ITEM_H / 2;
            ctx.method_25294(panelX + 8, sy, panelX + TurtLauncher.SIDEBAR_W - 8, sy + 1,
               TurtLauncher.BORDER.getRGB());
            continue;
         }
         boolean hov = TurtLauncher.navItemHovered(panelX, panelY, i, mx, my);
         String[] ic = switch (i) {
            case 0 -> com.turtmod.ui.TurtIcons.layout();     // HUD Editor
            case 1 -> com.turtmod.ui.TurtIcons.person();     // Skin Changer
            case 2 -> com.turtmod.ui.TurtIcons.gear();       // Settings
            case 3 -> com.turtmod.ui.TurtIcons.camera();     // Gallery
            case 5 -> com.turtmod.ui.TurtIcons.arrowBack();  // Back
            default -> null;
         };
         TurtLauncher.drawNavItem(ctx, this.field_22793, panelX, panelY, i, NAV_LABELS[i], ic, false, hov);
      }

      // ── Content: a calm, centered logo + title + tagline, with the memorial pinned at the bottom.
      // Clean/minimal (Lunar-style): plenty of breathing room, no decorative pills, no restless motion.
      int cx2 = TurtLauncher.contentX(panelX);
      int cy2 = TurtLauncher.contentY(panelY);
      int cw  = TurtLauncher.contentW(panelW);
      int ch  = TurtLauncher.contentH(panelH);
      int centerCX = cx2 + cw / 2;

      // Centered logo with a very subtle breathe so it feels alive without being busy.
      int logoW = 76, logoH = 76;
      int logoX = centerCX - logoW / 2;
      int logoY = cy2 + (ch - logoH) / 2 - 26;
      float breathe = 1f + 0.012f * (float)Math.sin(System.currentTimeMillis() / 1500.0);
      float lcx = logoX + logoW / 2f, lcy = logoY + logoH / 2f;
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(lcx, lcy);
      ctx.method_51448().scale(breathe, breathe);
      ctx.method_51448().translate(-lcx, -lcy);
      BrandingRenderer.drawLogo(ctx, logoX, logoY, logoW, logoH);
      ctx.method_51448().popMatrix();

      // "TurtMod" wordmark + a single quiet tagline underneath.
      TurtUIUtils.drawGradientText(ctx, this.field_22793, "TurtMod",
         centerCX, logoY + logoH + 8, TurtLauncher.GREEN, TurtLauncher.PINK, true, true);
      TurtUIUtils.drawText(ctx, this.field_22793, "Clean HUD & PvP utilities",
         centerCX, logoY + logoH + 22, new Color(0x99A9B4B0, true), true, false);

      // ── In loving memory of Turt — kept, but a clean STATIC badge (no pulse) so it reads as a quiet tribute.
      String memorial = "In loving memory of Turt";
      int memW = this.field_22793.method_1727(memorial) + 22;
      int memX = centerCX - memW / 2;
      int memY = cy2 + ch - 20;
      TurtUIUtils.drawRoundedRect(ctx, memX, memY, memW, 16, 4, new Color(0x2E000000, true));
      TurtUIUtils.drawRoundedBorder(ctx, memX, memY, memW, 16, 4,
         new Color(TurtLauncher.PINK.getRed(), TurtLauncher.PINK.getGreen(), TurtLauncher.PINK.getBlue(), 55));
      TurtUIUtils.drawGradientText(ctx, this.field_22793, memorial,
         centerCX, memY + 4, TurtLauncher.PINK, TurtLauncher.GREEN, true, false);

      ctx.method_51448().popMatrix();
      this.uiScale.pop(ctx);
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      if (click.method_74245() != 0) return super.method_25402(click, bl);
      int mx = (int)this.uiScale.toLogicalX(click.comp_4798());
      int my = (int)this.uiScale.toLogicalY(click.comp_4799());
      for (int i = 0; i < NAV_LABELS.length; i++) {
         if (NAV_IS_SEPARATOR[i]) continue;
         if (TurtLauncher.navItemHovered(panelX, panelY, i, mx, my)) {
            switch (i) {
               case 0 -> this.field_22787.method_1507(new HudEditorScreen(this));
               case 1 -> this.field_22787.method_1507(new CosmeticsScreen(this));
               case 2 -> this.field_22787.method_1507(new TurtModClientConfigScreen(this));
               case 3 -> this.field_22787.method_1507(new ScreenshotGalleryScreen(this));
               case 5 -> this.method_25419();
            }
            return true;
         }
      }
      return super.method_25402(click, bl);
   }

   public void method_25419() {
      ConfigManager.save(TurtModClient.getConfig());
      if (this.field_22787 != null) this.field_22787.method_1507(this.parent);
   }
}
