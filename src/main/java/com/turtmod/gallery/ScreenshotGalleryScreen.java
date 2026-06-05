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
   private static final int THUMB_SIZE = 90;
   private static final int THUMB_PAD = 8;
   private static final int ROW_H = 115;
   private static final int BUTTON_H = 20;
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

      // ── All actions live INSIDE the sidebar (chrome layer) ──
      int sx = this.panelX + 6;
      int sw = com.turtmod.ui.TurtLauncher.SIDEBAR_W - 12;
      int sy = this.panelY + com.turtmod.ui.TurtLauncher.HEADER_H + 8;
      int gap = 4, bh = 18;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "View", btnTheme, this::viewSelected)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Open", btnTheme, this::openSelected)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Copy", btnTheme, this::copySelected)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Delete", btnTheme, this::deleteSelected)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Folder", btnTheme, this::openFolder)); sy += bh + gap;
      // Back anchored to bottom of sidebar
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
               class_1011 thumb = new class_1011(img.method_4318(), 90, 90, false);

               for(int x = 0; x < 90; ++x) {
                  for(int y = 0; y < 90; ++y) {
                     int sx = Math.min((int)((float)x / 90.0F * (float)img.method_4307()), img.method_4307() - 1);
                     int sy = Math.min((int)((float)y / 90.0F * (float)img.method_4323()), img.method_4323() - 1);

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

   // Gallery layout constants — 3 top (large) + 3 bottom (medium)
   private static final int TOP_COUNT  = 3;
   private static final int BOT_COUNT  = 3;
   private static final int GAP        = 6;

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      long now = System.nanoTime();
      this.frameDt = Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f);
      this.lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, this.frameDt, 12f);

      TurtUIUtils.drawMenuBackdrop(context, this.field_22789, this.field_22790);
      TurtUIUtils.update();

      // Fit the fixed logical layout to the screen, then work in logical mouse coords.
      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      mouseX = (int)this.uiScale.toLogicalX(mouseX);
      mouseY = (int)this.uiScale.toLogicalY(mouseY);
      this.uiScale.push(context);

      // subtle slide-up on open
      context.method_51448().pushMatrix();
      context.method_51448().translate(0f, (1f - this.openFade) * 7f);

      String player = this.field_22787 != null && this.field_22787.method_1548() != null
         ? this.field_22787.method_1548().method_1676() : "Player";
      TurtLauncher.drawChrome(context, this.field_22793, this.panelX, this.panelY, this.panelW, this.panelH,
         "Gallery", player, "Screenshots");

      // ── CONTENT LAYER (strictly within content area) ──
      if (this.screenshots.isEmpty()) {
         context.method_25300(this.field_22793, "No screenshots found",
            this.gridX + this.gridW / 2, this.gridY + this.gridH / 2, -5592406);
      } else {
         renderFeaturedRow(context, mouseX, mouseY);
         renderSmallGrid(context, mouseX, mouseY);
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
      super.method_25394(context, mouseX, mouseY, delta);
   }

   /**
    * Renders a clean 3×2 gallery grid:
    *  Row 1 (TOP_COUNT=3): large featured thumbnails
    *  Row 2 (BOT_COUNT=3): smaller secondary thumbnails
    * Additional screenshots accessible via scroll.
    */
   private void renderFeaturedRow(class_332 context, int mouseX, int mouseY) {
      if (this.screenshots.isEmpty()) return;

      // ── Row geometry — each row reserves a label strip below its thumbnails
      int labelH   = 11;
      int rowGap   = 16;        // generous gap between the two rows
      int cellGap  = 12;        // generous gap between thumbnails in a row
      int availW   = this.gridW;
      int availH   = this.gridH - labelH * 2 - rowGap;  // space for 2 image rows
      int topH     = availH * 52 / 100;
      int botH     = availH - topH;
      int topW     = (availW - cellGap * (TOP_COUNT - 1)) / TOP_COUNT;
      int botW     = (availW - cellGap * (BOT_COUNT - 1)) / BOT_COUNT;
      int topY     = this.gridY;
      int topLabelY= topY + topH;
      int botY     = topLabelY + labelH + rowGap;
      int botLabelY= botY + botH;

      // ── Top row (large, scrolled) — centred
      int topRowW = TOP_COUNT * topW + cellGap * (TOP_COUNT - 1);
      int topStart = this.gridX + (availW - topRowW) / 2;
      for (int j = 0; j < TOP_COUNT; j++) {
         int idx = this.scrollOffset + j;
         if (idx >= this.screenshots.size()) break;
         File file = this.screenshots.get(idx);
         int tx = topStart + j * (topW + cellGap);
         renderThumb(context, mouseX, mouseY, file, idx, tx, topY, topW, topH, topLabelY);
      }

      // ── Bottom row (medium, offset by TOP_COUNT) — centred
      int botRowW = BOT_COUNT * botW + cellGap * (BOT_COUNT - 1);
      int botStart = this.gridX + (availW - botRowW) / 2;
      for (int j = 0; j < BOT_COUNT; j++) {
         int idx = this.scrollOffset + TOP_COUNT + j;
         if (idx >= this.screenshots.size()) break;
         File file = this.screenshots.get(idx);
         int tx = botStart + j * (botW + cellGap);
         renderThumb(context, mouseX, mouseY, file, idx, tx, botY, botW, botH, botLabelY);
      }

      // ── Scrollbar (right of grid)
      int totalScreenshots = this.screenshots.size();
      int perPage = TOP_COUNT + BOT_COUNT;
      if (totalScreenshots > perPage) {
         int sbX = this.gridX + this.gridW + 3;
         int sbH = this.gridH;
         context.method_25294(sbX, this.gridY, sbX + 2, this.gridY + sbH, 1429093934);
         int maxScroll = Math.max(1, totalScreenshots - perPage);
         float ratio  = (float) perPage / totalScreenshots;
         int tbH = Math.max(14, (int)(sbH * ratio));
         int tbY = this.gridY + (maxScroll > 0 ? (int)((sbH - tbH) * ((float)this.scrollOffset / maxScroll)) : 0);
         context.method_25294(sbX, tbY, sbX + 2, tbY + tbH, ACCENT_GREEN.getRGB());
      }
   }

   private void renderThumb(class_332 ctx, int mx, int my,
                            File file, int idx, int tx, int ty, int tw, int th, int labelY) {
      boolean sel = idx == this.selectedIdx;
      boolean hov = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;

      // Smooth per-thumb hover animation
      float anim = this.thumbHover.getOrDefault(idx, 0f);
      anim = TurtUIUtils.lerp01(anim, hov ? 1f : 0f, this.frameDt, 12f);
      this.thumbHover.put(idx, anim);

      // Card bg
      int bg = sel ? 0x33000000 : (hov ? 0x28FFFFFF : 0x18FFFFFF);
      TurtUIUtils.drawRoundedRect(ctx, tx, ty, tw, th, 3, new Color(bg, true));
      if (sel)      ctx.method_73198(tx - 2, ty - 2, tw + 4, th + 4, ACCENT_GREEN.getRGB());
      else if (anim > 0.01f) {
         int a = (int)(255 * Math.min(1f, anim));
         ctx.method_73198(tx - 1, ty - 1, tw + 2, th + 2,
            new Color(ACCENT_PINK.getRed(), ACCENT_PINK.getGreen(), ACCENT_PINK.getBlue(), a).getRGB());
      }

      // Thumbnail (fills card; zooms in slightly on hover, clipped to the card)
      class_2960 tid = this.getThumbnail(file);
      if (tid != null) {
         int z = (int)(anim * 5f);   // up to 5px inflation each side on hover
         ctx.method_44379(tx, ty, tx + tw, ty + th);
         ctx.method_25290(class_10799.field_56883, tid, tx - z, ty - z, 0f, 0f, tw + z * 2, th + z * 2, tw + z * 2, th + z * 2);
         ctx.method_44380();
      } else {
         ctx.method_25294(tx, ty, tx + tw, ty + th, 1149798536);
      }

      // Filename in its own dedicated strip below the thumbnail (never overlaps image)
      String name = file.getName();
      if (name.length() > 22) name = name.substring(0, 20) + "..";
      ctx.method_25300(this.field_22793, name, tx + tw / 2, labelY + 1,
         sel ? ACCENT_GREEN.getRGB() : 0xFFCCCCCC);
   }

   private void renderSmallGrid(class_332 ctx, int mx, int my) {
      // No-op — renderFeaturedRow handles both rows now
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      double mouseX = this.uiScale.toLogicalX(click.comp_4798());
      double mouseY = this.uiScale.toLogicalY(click.comp_4799());
      int button = click.method_74245();
      // Sidebar buttons first (highest priority — top layer)
      for (TurtUIButton btn : this.buttons) {
         if (btn.mouseClicked(mouseX, mouseY, button)) return true;
      }
      // ── 3×2 grid click detection (matches centred renderFeaturedRow layout)
      if (mouseX >= this.gridX && mouseX <= this.gridX + this.gridW
          && mouseY >= this.gridY && mouseY <= this.gridY + this.gridH) {
         int labelH = 11, rowGap = 16, cellGap = 12;
         int availW = this.gridW;
         int availH = this.gridH - labelH * 2 - rowGap;
         int topH = availH * 52 / 100;
         int botH = availH - topH;
         int topW = (availW - cellGap * (TOP_COUNT - 1)) / TOP_COUNT;
         int botW = (availW - cellGap * (BOT_COUNT - 1)) / BOT_COUNT;
         int topY = this.gridY;
         int botY = topY + topH + labelH + rowGap;
         int topStart = this.gridX + (availW - (TOP_COUNT * topW + cellGap * (TOP_COUNT - 1))) / 2;
         int botStart = this.gridX + (availW - (BOT_COUNT * botW + cellGap * (BOT_COUNT - 1))) / 2;
         // Top row
         if (mouseY >= topY && mouseY < topY + topH) {
            int rel = (int)(mouseX - topStart);
            int col = rel / (topW + cellGap);
            if (col >= 0 && col < TOP_COUNT && rel >= 0) {
               int idx = this.scrollOffset + col;
               if (idx >= 0 && idx < this.screenshots.size()) {
                  this.selectedIdx = idx;
                  this.setStatus("Selected: " + this.screenshots.get(idx).getName());
                  return true;
               }
            }
         }
         // Bottom row
         if (mouseY >= botY && mouseY < botY + botH) {
            int rel = (int)(mouseX - botStart);
            int col = rel / (botW + cellGap);
            if (col >= 0 && col < BOT_COUNT && rel >= 0) {
               int idx = this.scrollOffset + TOP_COUNT + col;
               if (idx >= 0 && idx < this.screenshots.size()) {
                  this.selectedIdx = idx;
                  this.setStatus("Selected: " + this.screenshots.get(idx).getName());
                  return true;
               }
            }
         }
      }

      return super.method_25402(click, bl);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int perPage = TOP_COUNT + BOT_COUNT;
      int maxScroll = Math.max(0, this.screenshots.size() - perPage);
      if (maxScroll > 0) {
         this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int)verticalAmount));
         return true;
      }
      return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }
}
