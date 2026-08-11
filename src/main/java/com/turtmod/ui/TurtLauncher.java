package com.turtmod.ui;

import java.awt.Color;
import net.minecraft.class_10799;
import net.minecraft.class_327;
import net.minecraft.class_332;

/**
 * Shared chrome renderer — every TurtMod screen calls drawChrome() for the same
 * Lunar-Client-inspired frame: header + logo + sidebar + content area + footer.
 *
 * Layout (panelW x panelH):
 * ┌────────────────────────────────────────────────────────┐  ← HEADER_H
 * │  [LOGO 16x16]  SCREEN TITLE              v1.21.11     │
 * │━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│
 * ├──────────┬─────────────────────────────────────────────┤
 * │ sidebar  │  content area                               │
 * │ 130px    │                                             │
 * ├──────────┴─────────────────────────────────────────────┤  ← FOOTER_H
 * │  🐢 playerName                                         │
 * └────────────────────────────────────────────────────────┘
 */
public final class TurtLauncher {

   public static final int SIDEBAR_W    = 130;
   public static final int HEADER_H     = 32;
   public static final int FOOTER_H     = 24;
   public static final int NAV_ITEM_H   = 28;
   public static final int NAV_ITEM_PAD = 4;
   public static final int CONTENT_PAD  = 12;

   // Colours come from the central Palette so re-theming happens in one place.
   public static final Color BG     = Palette.PANEL_BG;
   public static final Color BORDER  = Palette.PANEL_BORDER;
   public static final Color GREEN   = Palette.GREEN;
   public static final Color PINK    = Palette.PINK;
   public static final Color TEXT    = Palette.TEXT;
   public static final Color BTN_BG  = Palette.BTN_BG;
   public static final Color BTN_HOV = Palette.BTN_HOVER;

   private static final Color SIDEBAR_BG = new Color(0x25000000, true);
   private static final Color FOOTER_BG  = new Color(0x30000000, true);
   private static final Color HEADER_BG  = new Color(0x30000000, true);

   // Logo size in screen headers
   private static final int LOGO_SZ = 18;

   /**
    * Draws the full chrome frame for any TurtMod screen.
    * @param title      screen title shown next to logo
    * @param playerName shown in footer (null to hide footer player name)
    * @param version    shown in header right corner (null to hide)
    */
   public static void drawChrome(class_332 ctx, class_327 tr,
                                 int panelX, int panelY, int panelW, int panelH,
                                 String title, String playerName, String version) {
      drawChrome(ctx, tr, panelX, panelY, panelW, panelH, title, playerName, version, true);
   }

   /** Chrome variant; pass {@code sidebar=false} to omit the left nav strip for full-width content. */
   public static void drawChrome(class_332 ctx, class_327 tr,
                                 int panelX, int panelY, int panelW, int panelH,
                                 String title, String playerName, String version, boolean sidebar) {
      // ── Drop shadow
      TurtUIUtils.drawShadow(ctx, panelX, panelY, panelW, panelH, 12);

      // ── Outer panel (rounded) with subtle vertical depth gradient
      TurtUIUtils.drawRoundedRect(ctx, panelX, panelY, panelW, panelH, 4, BG);
      Color bgTop = new Color(Math.min(255, BG.getRed() + 8), Math.min(255, BG.getGreen() + 10), Math.min(255, BG.getBlue() + 12), BG.getAlpha());
      ctx.method_25296(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH / 2,
         bgTop.getRGB(), 0x00000000);
      TurtUIUtils.drawRoundedBorder(ctx, panelX, panelY, panelW, panelH, 4, BORDER);
      // Soft top highlight
      ctx.method_25294(panelX + 2, panelY + 1, panelX + panelW - 2, panelY + 2, 0x18FFFFFF);

      // ── Header strip
      TurtUIUtils.drawRoundedRect(ctx, panelX, panelY, panelW, HEADER_H, 4, HEADER_BG);
      // Logo in header left (static — a floating bob read as restless/busy).
      BrandingRenderer.drawLogo(ctx, panelX + 8, panelY + (HEADER_H - LOGO_SZ) / 2, LOGO_SZ, LOGO_SZ);
      // Title text (gradient, after logo)
      TurtUIUtils.drawGradientText(ctx, tr, title, panelX + 10 + LOGO_SZ + 6, panelY + (HEADER_H - 8) / 2, GREEN, PINK, false, true);
      // Version right
      if (version != null)
         TurtUIUtils.drawText(ctx, tr, version, panelX + panelW - 8, panelY + (HEADER_H - 8) / 2, new Color(0x88AAAAAA, true), true, false);
      // Clean gradient divider below header (no sweeping "shine" — it made the frame feel busy).
      TurtUIUtils.drawHGradientLine(ctx, panelX + 6, panelY + HEADER_H, panelW - 12, GREEN, PINK, 1);

      // ── Sidebar bg + separator
      if (sidebar) {
         ctx.method_25294(panelX, panelY + HEADER_H + 1,
            panelX + SIDEBAR_W, panelY + panelH - FOOTER_H - 1, SIDEBAR_BG.getRGB());
         ctx.method_25294(panelX + SIDEBAR_W, panelY + HEADER_H + 1,
            panelX + SIDEBAR_W + 1, panelY + panelH - FOOTER_H - 1, BORDER.getRGB());
      }

      // ── Footer
      ctx.method_25294(panelX, panelY + panelH - FOOTER_H,
         panelX + panelW, panelY + panelH, FOOTER_BG.getRGB());
      ctx.method_25294(panelX, panelY + panelH - FOOTER_H,
         panelX + panelW, panelY + panelH - FOOTER_H + 1, BORDER.getRGB());
      if (playerName != null)
         TurtUIUtils.drawText(ctx, tr, "🐢 " + playerName,
            panelX + 10, panelY + panelH - 15, new Color(0xAABBBBBB, true), false, false);
      // Credit on footer right
      TurtUIUtils.drawText(ctx, tr, "TurtMod • Made by Tzy",
         panelX + panelW - 8, panelY + panelH - 15, new Color(0x66AAAAAA, true), true, false);
   }

   // Sliding active-tab indicator state (one launcher screen open at a time, so shared is fine).
   private static float navIndicatorY = -1f;
   private static long  navIndicatorNs = System.nanoTime();

   /**
    * Draws the sliding highlight behind the active nav item. Call once, before the per-item
    * {@link #drawNavItem} loop, so the highlight sits under the labels and glides between tabs.
    */
   public static void drawNavIndicator(class_332 ctx, int panelX, int panelY, int activeIndex) {
      int x = panelX + 3;
      int w = SIDEBAR_W - 6;
      int h = NAV_ITEM_H;
      float target = panelY + HEADER_H + 4 + activeIndex * (NAV_ITEM_H + NAV_ITEM_PAD);

      long now = System.nanoTime();
      float dt = Math.min((now - navIndicatorNs) / 1_000_000_000f, 0.1f);
      navIndicatorNs = now;
      navIndicatorY = navIndicatorY < 0f ? target : TurtUIUtils.lerp01(navIndicatorY, target, dt, 18f);
      int y = Math.round(navIndicatorY);

      // Steady, quiet highlight (no breathing pulse — it read as restless).
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 4,
         new Color(GREEN.getRed(), GREEN.getGreen(), GREEN.getBlue(), 34));
      TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 4,
         new Color(GREEN.getRed(), GREEN.getGreen(), GREEN.getBlue(), 70));
      // Rounded accent bar on the left edge.
      TurtUIUtils.drawRoundedRect(ctx, x, y + 4, 3, h - 8, 1, GREEN);
   }

   /** Draws a single sidebar nav item's label (highlight handled by {@link #drawNavIndicator}). */
   public static void drawNavItem(class_332 ctx, class_327 tr,
                                  int panelX, int panelY, int index,
                                  String label, boolean active, boolean hovered) {
      drawNavItem(ctx, tr, panelX, panelY, index, label, null, active, hovered);
   }

   /** Nav item with an optional pixel icon drawn left of the label. */
   public static void drawNavItem(class_332 ctx, class_327 tr,
                                  int panelX, int panelY, int index,
                                  String label, String[] icon, boolean active, boolean hovered) {
      int x = panelX + 3;
      int y = panelY + HEADER_H + 4 + index * (NAV_ITEM_H + NAV_ITEM_PAD);
      int w = SIDEBAR_W - 6;
      int h = NAV_ITEM_H;
      if (hovered && !active) {
         TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 3, new Color(0x1EFFFFFF, true));
      }
      if (icon != null) {
         com.turtmod.ui.TurtIcons.drawFit(ctx, icon, x + 7, y + (h - 14) / 2, 14);
      }
      int labelX = x + (icon != null ? 25 : 10);
      Color c = active ? GREEN : (hovered ? TEXT : new Color(0xBBCCCCCC, true));
      TurtUIUtils.drawText(ctx, tr, label, labelX, y + (h - 8) / 2, c, false, active);
   }

   /** Returns whether the mouse is over a sidebar nav item. */
   public static boolean navItemHovered(int panelX, int panelY, int index, int mx, int my) {
      int x = panelX + 3;
      int y = panelY + HEADER_H + 4 + index * (NAV_ITEM_H + NAV_ITEM_PAD);
      return mx >= x && mx <= x + SIDEBAR_W - 6 && my >= y && my <= y + NAV_ITEM_H;
   }

   /** Nav item top-y. */
   public static int navItemY(int panelY, int index) {
      return panelY + HEADER_H + 4 + index * (NAV_ITEM_H + NAV_ITEM_PAD);
   }

   /** Left edge of the content area. */
   public static int contentX(int panelX) { return panelX + SIDEBAR_W + CONTENT_PAD; }
   /** Top edge of the content area (below header). */
   public static int contentY(int panelY) { return panelY + HEADER_H + CONTENT_PAD; }
   /** Width of the content area. */
   public static int contentW(int panelW) { return panelW - SIDEBAR_W - CONTENT_PAD * 2; }
   /** Height of the content area (above footer). */
   public static int contentH(int panelH) { return panelH - HEADER_H - FOOTER_H - CONTENT_PAD * 2; }

   /**
    * Draws a polished content sub-panel (rounded, faint top sheen, border, optional title strip) so
    * every screen's inner panels match the chrome instead of looking like flat boxes.
    */
   public static void drawContentPanel(class_332 ctx, class_327 tr, int x, int y, int w, int h, String title) {
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 4, new Color(0x3A05080A, true));
      ctx.method_25296(x + 1, y + 1, x + w - 1, y + h / 2, 0x12FFFFFF, 0x00000000);
      TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 4, BORDER);
      ctx.method_25294(x + 2, y + 1, x + w - 2, y + 2, 0x14FFFFFF); // top highlight
      if (title != null) {
         TurtUIUtils.drawText(ctx, tr, title, x + 7, y + 6, GREEN, false, true);
         TurtUIUtils.drawHGradientLine(ctx, x + 7, y + 16, w - 14, GREEN, PINK, 1);
      }
   }

   /** Draws a section header label + subtle line in the content area. */
   public static void drawSectionTitle(class_332 ctx, class_327 tr, int x, int y, int maxW, String label) {
      TurtUIUtils.drawText(ctx, tr, label, x, y, new Color(0xCCAABBBB, true), false, false);
      int lw = tr.method_1727(label);
      TurtUIUtils.drawHLine(ctx, x + lw + 4, y + 4, maxW - lw - 4, BORDER, 1);
   }

   private TurtLauncher() {}
}
