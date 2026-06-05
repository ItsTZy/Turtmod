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
      // ── Drop shadow
      TurtUIUtils.drawShadow(ctx, panelX, panelY, panelW, panelH, 12);

      // ── Outer panel (rounded) with subtle vertical depth gradient
      TurtUIUtils.drawRoundedRect(ctx, panelX, panelY, panelW, panelH, 4, BG);
      Color bgTop = new Color(Math.min(255, BG.getRed() + 8), Math.min(255, BG.getGreen() + 10), Math.min(255, BG.getBlue() + 12), BG.getAlpha());
      ctx.method_25296(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH / 2,
         bgTop.getRGB(), 0x00000000);
      ctx.method_73198(panelX, panelY, panelW, panelH, BORDER.getRGB());
      // Soft top highlight
      ctx.method_25294(panelX + 2, panelY + 1, panelX + panelW - 2, panelY + 2, 0x18FFFFFF);

      // ── Header strip
      TurtUIUtils.drawRoundedRect(ctx, panelX, panelY, panelW, HEADER_H, 4, HEADER_BG);
      // Logo in header left (uses BrandingRenderer so correct 640x640 texture dims)
      BrandingRenderer.drawLogo(ctx, panelX + 8, panelY + (HEADER_H - LOGO_SZ) / 2, LOGO_SZ, LOGO_SZ);
      // Title text (gradient, after logo)
      TurtUIUtils.drawGradientText(ctx, tr, title, panelX + 10 + LOGO_SZ + 6, panelY + (HEADER_H - 8) / 2, GREEN, PINK, false, true);
      // Version right
      if (version != null)
         TurtUIUtils.drawText(ctx, tr, version, panelX + panelW - 8, panelY + (HEADER_H - 8) / 2, new Color(0x88AAAAAA, true), true, false);
      // Gradient divider below header
      TurtUIUtils.drawHGradientLine(ctx, panelX + 6, panelY + HEADER_H, panelW - 12, GREEN, PINK, 1);

      // ── Sidebar bg + separator
      ctx.method_25294(panelX, panelY + HEADER_H + 1,
         panelX + SIDEBAR_W, panelY + panelH - FOOTER_H - 1, SIDEBAR_BG.getRGB());
      ctx.method_25294(panelX + SIDEBAR_W, panelY + HEADER_H + 1,
         panelX + SIDEBAR_W + 1, panelY + panelH - FOOTER_H - 1, BORDER.getRGB());

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

   /** Draws a single sidebar nav item. */
   public static void drawNavItem(class_332 ctx, class_327 tr,
                                  int panelX, int panelY, int index,
                                  String label, boolean active, boolean hovered) {
      int x = panelX + 3;
      int y = panelY + HEADER_H + 4 + index * (NAV_ITEM_H + NAV_ITEM_PAD);
      int w = SIDEBAR_W - 6;
      int h = NAV_ITEM_H;

      if (active) {
         TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 3,
            new Color(GREEN.getRed(), GREEN.getGreen(), GREEN.getBlue(), 40));
         ctx.method_25294(x, y + 3, x + 3, y + h - 3, GREEN.getRGB());
         TurtUIUtils.drawText(ctx, tr, label, x + 10, y + (h - 8) / 2, GREEN, false, true);
      } else if (hovered) {
         TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 3, new Color(0x1EFFFFFF, true));
         TurtUIUtils.drawText(ctx, tr, label, x + 10, y + (h - 8) / 2, TEXT, false, false);
      } else {
         TurtUIUtils.drawText(ctx, tr, label, x + 10, y + (h - 8) / 2, new Color(0xBBCCCCCC, true), false, false);
      }
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

   /** Draws a section header label + subtle line in the content area. */
   public static void drawSectionTitle(class_332 ctx, class_327 tr, int x, int y, int maxW, String label) {
      TurtUIUtils.drawText(ctx, tr, label, x, y, new Color(0xCCAABBBB, true), false, false);
      int lw = tr.method_1727(label);
      TurtUIUtils.drawHLine(ctx, x + lw + 4, y + 4, maxW - lw - 4, BORDER, 1);
   }

   private TurtLauncher() {}
}
