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
         TurtLauncher.drawNavItem(ctx, this.field_22793, panelX, panelY, i, NAV_LABELS[i], false, hov);
      }

      // ── Content: large logo + title + description
      int cx2 = TurtLauncher.contentX(panelX);
      int cy2 = TurtLauncher.contentY(panelY);
      int cw  = TurtLauncher.contentW(panelW);
      int ch  = TurtLauncher.contentH(panelH);
      int centerCX = cx2 + cw / 2;

      // Big logo centered in content
      int logoW = 80, logoH = 80;
      int logoX = centerCX - logoW / 2;
      int logoY = cy2 + ch / 2 - logoH / 2 - 28;
      // Gentle "breathing" pulse so the turtle feels alive.
      float breathe = 1f + 0.025f * (float)Math.sin(System.currentTimeMillis() / 900.0);
      float lcx = logoX + logoW / 2f, lcy = logoY + logoH / 2f;
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(lcx, lcy);
      ctx.method_51448().scale(breathe, breathe);
      ctx.method_51448().translate(-lcx, -lcy);
      BrandingRenderer.drawLogo(ctx, logoX, logoY, logoW, logoH);
      ctx.method_51448().popMatrix();

      // "TurtMod" title gradient below logo
      TurtUIUtils.drawGradientText(ctx, this.field_22793, "TurtMod",
         centerCX, logoY + logoH + 4, TurtLauncher.GREEN, TurtLauncher.PINK, true, true);

      // Credit line only — kept simple per request (no cycling puns).
      TurtUIUtils.drawText(ctx, this.field_22793, "Made by Tzy  •  1.21.11",
         centerCX, logoY + logoH + 16, new Color(0x88BBBBBB, true), true, false);

      // Gradient divider
      TurtUIUtils.drawHGradientLine(ctx, cx2, logoY + logoH + 26, cw, TurtLauncher.GREEN, TurtLauncher.PINK, 1);

      // Feature pills row
      String[] features = {"HUD", "Combat", "Visual", "Skins"};
      int pillW = (cw - 6) / features.length;
      int pillY = logoY + logoH + 32;
      for (int i = 0; i < features.length; i++) {
         int px2 = cx2 + i * (pillW + 2);
         TurtUIUtils.drawRoundedRect(ctx, px2, pillY, pillW, 18, 3, TurtLauncher.SIDEBAR_W > 0 ? new Color(0x22FFFFFF, true) : TurtLauncher.BG);
         TurtUIUtils.drawText(ctx, this.field_22793, features[i], px2 + pillW / 2, pillY + 5, TurtLauncher.GREEN, true, false);
      }

      // ── In memory of Turt — gentle pulsing memorial badge at the bottom of the content area
      String memorial = "In loving memory of Turt";
      int memW = this.field_22793.method_1727(memorial) + 22;
      int memX = centerCX - memW / 2;
      int memY = cy2 + ch - 20;
      float pulse = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() / 1100.0);
      // soft pink glow that breathes slowly
      Color glow = new Color(TurtLauncher.PINK.getRed(), TurtLauncher.PINK.getGreen(), TurtLauncher.PINK.getBlue(),
         (int)(26 + 26 * pulse));
      TurtUIUtils.drawRoundedRect(ctx, memX, memY, memW, 16, 4, new Color(0x22000000, true));
      ctx.method_73198(memX, memY, memW, 16, glow.getRGB());
      TurtUIUtils.drawGradientText(ctx, this.field_22793, memorial,
         centerCX, memY + 4, TurtLauncher.PINK, TurtLauncher.GREEN, true, false);

      ctx.method_51448().popMatrix();
      this.uiScale.pop(ctx);
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
