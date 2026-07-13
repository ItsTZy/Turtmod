package com.turtmod.gallery;

import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUIPanel;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import com.turtmod.utils.ImageClipboardUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class ScreenshotGalleryScreen extends class_437 {
   private final class_437 parent;
   private final Path currentDir;
   private final List<File> screenshots = new ArrayList();
   private final Map<String, class_2960> thumbCache = new HashMap();
   private static final int THUMB_W = 160;   // 16:9 thumbnail texture (no distortion for screenshots)
   private static final int THUMB_H = 90;
   private static final Color PANEL_BG = new Color(1709588, true);
   private static final Color PANEL_BORDER = new Color(9289311, true);
   private static final Color ACCENT_GREEN = new Color(9289311, false);
   private static final Color ACCENT_PINK = new Color(16752046, false);
   private static final Color TEXT_MAIN = new Color(16775399, false);
   private static final Color BTN_BG = new Color(2433054, true);
   private static final Color BTN_HOVER = new Color(3482400, true);
   private TurtUIPanel mainPanel;
   private final List<TurtUIButton> buttons = new ArrayList();
   // Fixed logical layout scaled to fit any resolution / GUI scale.
   private static final int LOGICAL_W = 720;
   private static final int LOGICAL_H = 460;
   private final TurtUIScale uiScale = new TurtUIScale();
   private int panelX;
   private int panelY;
   private int panelW;
   private int panelH;
   private int gridX;
   private int gridY;
   private int gridW;
   private int gridH;
   private int footerY;
   private int scrollOffset = 0;
   private int selectedIdx = -1;
   private String statusMessage = "";
   private int messageTicks = 0;
   private float openFade = 0f;
   private long lastFrameNs = System.nanoTime();
   private float frameDt = 0f;
   private final Map<Integer, Float> thumbHover = new HashMap();

   private int getCols() {
      return Math.max(2, Math.min(6, this.gridW / 98));
   }

   public ScreenshotGalleryScreen(class_437 parent) {
      super(class_2561.method_43470("Screenshot Gallery"));
      this.parent = parent;
      this.currentDir = class_310.method_1551().field_1697.toPath().resolve("screenshots");
   }

   protected void method_25426() {
      this.layoutPanels();
      this.loadScreenshots();
      TurtUITheme btnTheme = new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
      this.buttons.clear();

      // Actions now live inline on each thumbnail (hover → view/copy/delete chips), so the
      // sidebar only carries the two screen-level actions.
      int sx = this.panelX + 6;
      int sw = com.turtmod.ui.TurtLauncher.SIDEBAR_W - 12;
      int sy = this.panelY + com.turtmod.ui.TurtLauncher.HEADER_H + 8;
      int gap = 4, bh = 18;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Open Folder", btnTheme, this::openFolder)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Refresh", btnTheme, this::loadScreenshots));
      int backY = this.panelY + this.panelH - com.turtmod.ui.TurtLauncher.FOOTER_H - bh - 6;
      this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back", btnTheme, this::method_25419));
   }

   private void layoutPanels() {
      // Fixed logical size; method_25394 scales the whole panel to fit the screen.
      this.panelW = LOGICAL_W;
      this.panelH = LOGICAL_H;
      this.panelX = 0;
      this.panelY = 0;
      // Content area = right of sidebar (strict safe zone, never under chrome)
      this.gridX = com.turtmod.ui.TurtLauncher.contentX(this.panelX);
      this.gridY = com.turtmod.ui.TurtLauncher.contentY(this.panelY);
      this.gridW = com.turtmod.ui.TurtLauncher.contentW(this.panelW);
      this.gridH = com.turtmod.ui.TurtLauncher.contentH(this.panelH);
      this.footerY = this.panelY + this.panelH - com.turtmod.ui.TurtLauncher.FOOTER_H - 4;
   }

   private void loadScreenshots() {
      this.screenshots.clear();
      File dir = this.currentDir.toFile();
      if (dir.exists() && dir.isDirectory()) {
         File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
         if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            Collections.addAll(this.screenshots, files);
         }
      }

      this.selectedIdx = -1;
   }

   private class_2960 getThumbnail(File file) {
      String key = file.getAbsolutePath();
      if (this.thumbCache.containsKey(key)) {
         return (class_2960)this.thumbCache.get(key);
      } else {
         try {
            class_1011 img = class_1011.method_4309(new FileInputStream(file));
            if (img == null) {
               return null;
            } else {
               class_1011 thumb = new class_1011(img.method_4318(), THUMB_W, THUMB_H, false);

               for(int x = 0; x < THUMB_W; ++x) {
                  for(int y = 0; y < THUMB_H; ++y) {
                     int sx = Math.min((int)((float)x / (float)THUMB_W * (float)img.method_4307()), img.method_4307() - 1);
                     int sy = Math.min((int)((float)y / (float)THUMB_H * (float)img.method_4323()), img.method_4323() - 1);

                     try {
                        thumb.method_61941(x, y, img.method_61940(sx, sy));
                     } catch (Exception var10) {
                     }
                  }
               }

               String thumbName = "gallery_" + key.hashCode();
               class_2960 thumbId = class_2960.method_60655("turtmod", "gallery/" + thumbName);
               class_310.method_1551().method_1531().method_4616(thumbId, new class_1043(() -> "turtmod:" + thumbName, thumb));
               this.thumbCache.put(key, thumbId);
               img.close();
               return thumbId;
            }
         } catch (IOException var11) {
            return null;
         }
      }
   }

   private void setStatus(String msg) {
      this.statusMessage = msg;
      this.messageTicks = 80;
   }

   private void openSelected() {
      if (this.selectedIdx >= 0 && this.selectedIdx < this.screenshots.size()) {
         class_156.method_668().method_672((File)this.screenshots.get(this.selectedIdx));
         this.setStatus("Opened image.");
      } else {
         this.setStatus("Select an image.");
      }

   }

   private void viewSelected() {
      if (this.selectedIdx >= 0 && this.selectedIdx < this.screenshots.size()) {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new ScreenshotViewScreen(this, (File)this.screenshots.get(this.selectedIdx)));
         }
      } else {
         this.setStatus("Select an image.");
      }

   }

   private void copySelected() {
      if (this.selectedIdx >= 0 && this.selectedIdx < this.screenshots.size()) {
         if (ImageClipboardUtils.copyImageToClipboard((File)this.screenshots.get(this.selectedIdx))) {
            this.setStatus("Copied to clipboard!");
         } else {
            this.setStatus("Failed to copy.");
         }
      } else {
         this.setStatus("Select an image.");
      }

   }

   private void deleteSelected() {
      if (this.selectedIdx >= 0 && this.selectedIdx < this.screenshots.size()) {
         try {
            Files.delete(((File)this.screenshots.get(this.selectedIdx)).toPath());
            this.setStatus("Deleted image.");
            this.loadScreenshots();
         } catch (IOException var2) {
            this.setStatus("Failed to delete.");
         }
      } else {
         this.setStatus("Select an image.");
      }

   }

   private void openFolder() {
      class_156.method_668().method_672(this.currentDir.toFile());
      this.setStatus("Opened folder.");
   }

   // Uniform grid layout: columns adapt to width, every card is the same 16:9 size.
   private static final int CELL_GAP = 10;
   private static final int LABEL_H  = 12;
   // Inline hover action chips (view / copy / upload / delete), drawn on the hovered card.
   private static final int CHIP = 14, CHIP_GAP = 3;
   private static final Color CHIP_BG = new Color(0xD8121A16, true);

   private int chipX(int tx, int tw, int i) {
      int total = 3 * CHIP + 2 * CHIP_GAP;
      return tx + tw - total - 4 + i * (CHIP + CHIP_GAP);
   }

   private int chipY(int ty) {
      return ty + 4;
   }

   private void chipAction(int i, File file) {
      switch (i) {
         case 0 -> { if (this.field_22787 != null) this.field_22787.method_1507(new ScreenshotViewScreen(this, file)); }
         case 1 -> setStatus(ImageClipboardUtils.copyImageToClipboard(file) ? "Copied to clipboard!" : "Failed to copy.");
         case 2 -> {
            try { Files.delete(file.toPath()); loadScreenshots(); setStatus("Deleted."); }
            catch (java.io.IOException e) { setStatus("Failed to delete."); }
         }
         default -> { }
      }
   }

   private void drawBorder(class_332 ctx, int x, int y, int w, int h, int color, int t) {
      ctx.method_25294(x, y, x + w, y + t, color);
      ctx.method_25294(x, y + h - t, x + w, y + h, color);
      ctx.method_25294(x, y, x + t, y + h, color);
      ctx.method_25294(x + w - t, y, x + w, y + h, color);
   }

   private void drawChipIcon(class_332 ctx, int type, int cx, int cy, int c, int hole) {
      int x = cx + 4, y = cy + 4;
      switch (type) {
         case 0 -> {
            ctx.method_25294(x, y + 2, x + 7, y + 5, c);
            ctx.method_25294(x + 2, y + 1, x + 5, y + 6, c);
            ctx.method_25294(x + 2, y + 2, x + 5, y + 5, hole);
            ctx.method_25294(x + 3, y + 3, x + 4, y + 4, c);
         }
         case 1 -> {
            drawBorder(ctx, x + 2, y, 5, 5, c, 1);
            drawBorder(ctx, x, y + 2, 5, 5, c, 1);
         }
         case 2 -> {
            ctx.method_25294(x, y + 1, x + 7, y + 2, c);
            ctx.method_25294(x + 2, y, x + 5, y + 1, c);
            drawBorder(ctx, x + 1, y + 2, 5, 5, c, 1);
         }
         default -> { }
      }
   }

   private int cols() {
      return Math.max(2, Math.min(4, (this.gridW + CELL_GAP) / 180));
   }

   private int cellW() {
      int c = cols();
      return (this.gridW - CELL_GAP * (c - 1)) / c;
   }

   private int imgH() {
      return cellW() * 9 / 16;
   }

   private int cellStride() {
      return imgH() + LABEL_H + 6 + CELL_GAP;
   }

   private int maxScrollPx() {
      int rows = (this.screenshots.size() + cols() - 1) / Math.max(1, cols());
      return Math.max(0, rows * cellStride() - CELL_GAP - this.gridH);
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      long now = System.nanoTime();
      this.frameDt = Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f);
      this.lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, this.frameDt, 12f);

      TurtUIUtils.drawMenuBackdrop(context, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(context, mouseX, mouseY);
      TurtUIUtils.update();

      // Fit the fixed logical layout to the screen, then work in logical mouse coords.
      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      mouseX = (int)this.uiScale.toLogicalX(mouseX);
      mouseY = (int)this.uiScale.toLogicalY(mouseY);
      this.uiScale.push(context);

      // subtle slide-up on open
      context.method_51448().pushMatrix();
      float introE = TurtUIUtils.ease(this.openFade);
      context.method_51448().translate(LOGICAL_W / 2f, LOGICAL_H / 2f + (1f - introE) * 12f);
      context.method_51448().scale(0.97f + 0.03f * introE, 0.97f + 0.03f * introE);
      context.method_51448().translate(-LOGICAL_W / 2f, -LOGICAL_H / 2f);

      String player = this.field_22787 != null && this.field_22787.method_1548() != null
         ? this.field_22787.method_1548().method_1676() : "Player";
      TurtLauncher.drawChrome(context, this.field_22793, this.panelX, this.panelY, this.panelW, this.panelH,
         "Gallery", player, "Screenshots");

      // ── CONTENT LAYER (strictly within content area) ──
      TurtLauncher.drawContentPanel(context, this.field_22793, this.gridX - 6, this.gridY - 6, this.gridW + 12, this.gridH + 12, null);
      if (this.screenshots.isEmpty()) {
         context.method_25300(this.field_22793, "No screenshots found",
            this.gridX + this.gridW / 2, this.gridY + this.gridH / 2, -5592406);
      } else {
         renderFeaturedRow(context, mouseX, mouseY);
      }

      if (this.messageTicks > 0) {
         context.method_25300(this.field_22793, this.statusMessage,
            this.gridX + this.gridW / 2, this.gridY - 1, ACCENT_GREEN.getRGB());
         --this.messageTicks;
      }

      // ── INTERACTION LAYER (sidebar buttons, drawn last = always on top) ──
      for(TurtUIButton btn : this.buttons) {
         btn.render(context, mouseX, mouseY, this.field_22793);
      }

      context.method_51448().popMatrix();
      this.uiScale.pop(context);
      TurtUIUtils.drawOpenFade(context, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   /** Clean uniform grid of equal-size 16:9 cards, vertically scrolled. */
   private void renderFeaturedRow(class_332 context, int mouseX, int mouseY) {
      if (this.screenshots.isEmpty()) return;
      int cols = cols();
      int cellW = cellW();
      int imgH = imgH();
      int stride = cellStride();
      int cardH = imgH + LABEL_H + 6;

      // Count readout, top-right of the grid.
      context.method_25303(this.field_22793, this.screenshots.size() + " screenshots",
         this.gridX, this.gridY - 11, 0x88CCCCCC);

      context.method_44379(this.gridX, this.gridY, this.gridX + this.gridW, this.gridY + this.gridH);
      for (int idx = 0; idx < this.screenshots.size(); idx++) {
         int col = idx % cols;
         int row = idx / cols;
         int tx = this.gridX + col * (cellW + CELL_GAP);
         int ty = this.gridY + row * stride - this.scrollOffset;
         if (ty + cardH < this.gridY || ty > this.gridY + this.gridH) continue; // off-view
         renderThumb(context, mouseX, mouseY, this.screenshots.get(idx), idx, tx, ty, cellW, imgH);
      }
      context.method_44380();

      // ── Scrollbar (right of grid)
      int maxScroll = maxScrollPx();
      if (maxScroll > 0) {
         int sbX = this.gridX + this.gridW + 3;
         int sbH = this.gridH;
         TurtUIUtils.drawRoundedRect(context, sbX, this.gridY, 3, sbH, 1, new Color(0x33353535, true));
         float ratio = (float) this.gridH / (this.gridH + maxScroll);
         int tbH = Math.max(16, (int)(sbH * ratio));
         int tbY = this.gridY + (int)((sbH - tbH) * ((float)this.scrollOffset / maxScroll));
         TurtUIUtils.drawRoundedRect(context, sbX, tbY, 3, tbH, 1, ACCENT_GREEN);
      }
   }

   private void renderThumb(class_332 ctx, int mx, int my,
                            File file, int idx, int tx, int ty, int tw, int imgH) {
      int cardH = imgH + LABEL_H + 6;
      boolean sel = idx == this.selectedIdx;
      boolean hov = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + cardH
         && my >= this.gridY && my <= this.gridY + this.gridH;

      float anim = this.thumbHover.getOrDefault(idx, 0f);
      anim = TurtUIUtils.lerp01(anim, hov ? 1f : 0f, this.frameDt, 12f);
      this.thumbHover.put(idx, anim);

      // Lunar-style hover lift: gently raise + glow the card while hovered.
      float lift = TurtUIUtils.ease(anim);
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(0f, -lift * 2.5f);
      if (lift > 0.01f) {
         TurtUIUtils.drawHoverGlow(ctx, tx, ty, tw, cardH, 3, lift * 0.7f, ACCENT_PINK);
      }

      // Card background.
      int bg = sel ? 0x55000000 : (hov ? 0x33000000 : 0x22000000);
      TurtUIUtils.drawRoundedRect(ctx, tx, ty, tw, cardH, 3, new Color(bg, true));

      // Thumbnail image (16:9, clipped to its rounded top).
      class_2960 tid = this.getThumbnail(file);
      if (tid != null) {
         ctx.method_44379(tx + 1, ty + 1, tx + tw - 1, ty + 1 + imgH);
         ctx.method_25290(class_10799.field_56883, tid, tx + 1, ty + 1, 0f, 0f, tw - 2, imgH, tw - 2, imgH);
         ctx.method_44380();
      } else {
         ctx.method_25294(tx + 1, ty + 1, tx + tw - 1, ty + 1 + imgH, 0x44555555);
      }

      // Filename strip.
      String name = file.getName();
      int maxChars = Math.max(8, tw / 6);
      if (name.length() > maxChars) name = name.substring(0, maxChars - 2) + "..";
      ctx.method_25300(this.field_22793, name, tx + tw / 2, ty + imgH + 5,
         sel ? ACCENT_GREEN.getRGB() : 0xFFC8C8C8);

      // Selection / hover border on top.
      if (sel) {
         TurtUIUtils.drawRoundedBorder(ctx, tx, ty, tw, cardH, 3, ACCENT_GREEN);
      } else if (anim > 0.01f) {
         int a = (int)(180 * Math.min(1f, anim));
         TurtUIUtils.drawRoundedBorder(ctx, tx, ty, tw, cardH, 3,
            new Color(ACCENT_PINK.getRed(), ACCENT_PINK.getGreen(), ACCENT_PINK.getBlue(), a));
      }

      // Inline action chips (view / copy / delete) — appear on hover, top-right of the image.
      if (hov) {
         for (int i = 0; i < 3; i++) {
            int cxp = chipX(tx, tw, i);
            int cyp = chipY(ty);
            boolean chov = mx >= cxp && mx <= cxp + CHIP && my >= cyp && my <= cyp + CHIP;
            ctx.method_25294(cxp, cyp, cxp + CHIP, cyp + CHIP, chov ? (0xFF000000 | ACCENT_GREEN.getRGB()) : CHIP_BG.getRGB());
            drawBorder(ctx, cxp, cyp, CHIP, CHIP, 0xFF000000 | (chov ? ACCENT_GREEN : PANEL_BORDER).getRGB(), 1);
            drawChipIcon(ctx, i, cxp, cyp, chov ? (0xFF000000 | PANEL_BG.getRGB()) : 0xFFE6E6E6, 0xFF000000 | PANEL_BG.getRGB());
         }
      }

      ctx.method_51448().popMatrix();
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      double mouseX = this.uiScale.toLogicalX(click.comp_4798());
      double mouseY = this.uiScale.toLogicalY(click.comp_4799());
      int button = click.method_74245();
      // Sidebar buttons first (highest priority — top layer)
      for (TurtUIButton btn : this.buttons) {
         if (btn.mouseClicked(mouseX, mouseY, button)) return true;
      }
      // Uniform-grid click detection (mirrors renderFeaturedRow).
      if (mouseX >= this.gridX && mouseX <= this.gridX + this.gridW
          && mouseY >= this.gridY && mouseY <= this.gridY + this.gridH) {
         int cols = cols();
         int cellW = cellW();
         int imgH = imgH();
         int stride = cellStride();
         int cardH = imgH + LABEL_H + 6;
         int relX = (int)(mouseX - this.gridX);
         int col = relX / (cellW + CELL_GAP);
         int inCol = relX - col * (cellW + CELL_GAP);
         if (col >= 0 && col < cols && inCol <= cellW) {
            int relY = (int)(mouseY - this.gridY) + this.scrollOffset;
            int row = relY / stride;
            int inRow = relY - row * stride;
            if (inRow <= cardH) {
               int idx = row * cols + col;
               if (idx >= 0 && idx < this.screenshots.size()) {
                  File file = this.screenshots.get(idx);
                  int tx = this.gridX + col * (cellW + CELL_GAP);
                  int ty = this.gridY + row * stride - this.scrollOffset;
                  // Inline action chip?
                  for (int i = 0; i < 3; i++) {
                     int cxp = chipX(tx, cellW, i);
                     int cyp = chipY(ty);
                     if (mouseX >= cxp && mouseX <= cxp + CHIP && mouseY >= cyp && mouseY <= cyp + CHIP) {
                        chipAction(i, file);
                        return true;
                     }
                  }
                  // Otherwise the card body opens the fullscreen viewer.
                  if (this.field_22787 != null) {
                     this.field_22787.method_1507(new ScreenshotViewScreen(this, file));
                  }
                  return true;
               }
            }
         }
      }

      return super.method_25402(click, bl);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int maxScroll = maxScrollPx();
      if (maxScroll > 0) {
         this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int)(verticalAmount * 40)));
         return true;
      }
      return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }
}
