package com.turtmod.gallery;

import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import com.turtmod.utils.ImageClipboardUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class ScreenshotViewScreen extends class_437 {
   private static final Color PANEL_BG = new Color(1709588, true);
   private static final Color PANEL_BORDER = new Color(9289311, true);
   private static final Color TEXT_MAIN = new Color(16775399, false);
   private static final Color BTN_BG = new Color(2433054, true);
   private static final Color BTN_HOVER = new Color(3482400, true);
   private static final Color ACCENT_PINK = new Color(16752046, false);
   private final class_437 parent;
   private final List<File> screenshots = new ArrayList();
   private final List<TurtUIButton> buttons = new ArrayList();

   /** Attach a pixel icon to a button and return it (for inline use when building the button row). */
   private static TurtUIButton icon(TurtUIButton b, String[] ic) { b.icon = ic; return b; }

   private int currentIndex = -1;
   private class_2960 textureId;
   private int imageWidth;
   private int imageHeight;
   private String errorMessage;
   private String statusMessage = "";
   private int statusTicks = 0;
   private float zoom = 1.0F;
   private float fitScale = 1.0F;
   private double panX = (double)0.0F;
   private double panY = (double)0.0F;
   private boolean dragging = false;
   private double dragStartX;
   private double dragStartY;
   private double dragPanStartX;
   private double dragPanStartY;
   private int imageAreaX;
   private int imageAreaY;
   private int imageAreaWidth;
   private int imageAreaHeight;
   private boolean pendingViewReset = true;
   private float openFade = 0.0F;
   private float imgFade = 1.0F;   // quick fade over the image area when navigating prev/next
   private long lastFrameNs = System.nanoTime();

   public ScreenshotViewScreen(class_437 parent, File initialFile) {
      super(class_2561.method_43470("Screenshot Viewer"));
      this.parent = parent;
      this.loadScreenshotList(initialFile);
   }

   protected void method_25426() {
      this.rebuildButtons();
      if (this.textureId == null && this.errorMessage == null && this.currentIndex >= 0) {
         this.loadCurrentTexture(true);
      }

   }

   private void rebuildButtons() {
      this.buttons.clear();
      TurtUITheme btnTheme = new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
      int btnW = 70;
      int btnGap = 7;
      int rowY = this.field_22790 - 28;
      int totalW = btnW * 7 + btnGap * 6;
      int startX = this.field_22789 / 2 - totalW / 2;
      this.buttons.add(icon(new TurtUIButton(startX, rowY, btnW, 20, "Prev", btnTheme, this::previousScreenshot), com.turtmod.ui.TurtIcons.arrowBack()));
      this.buttons.add(icon(new TurtUIButton(startX + btnW + btnGap, rowY, btnW, 20, "Next", btnTheme, this::nextScreenshot), com.turtmod.ui.TurtIcons.arrowFwd()));
      this.buttons.add(icon(new TurtUIButton(startX + (btnW + btnGap) * 2, rowY, btnW, 20, "Edit", btnTheme, this::editCurrent), com.turtmod.ui.TurtIcons.pencil()));
      this.buttons.add(icon(new TurtUIButton(startX + (btnW + btnGap) * 3, rowY, btnW, 20, "Copy", btnTheme, this::copyCurrent), com.turtmod.ui.TurtIcons.copy()));
      this.buttons.add(icon(new TurtUIButton(startX + (btnW + btnGap) * 4, rowY, btnW, 20, "Open", btnTheme, this::openCurrent), com.turtmod.ui.TurtIcons.export()));
      this.buttons.add(icon(new TurtUIButton(startX + (btnW + btnGap) * 5, rowY, btnW, 20, "Folder", btnTheme, this::openFolder), com.turtmod.ui.TurtIcons.folder()));
      this.buttons.add(icon(new TurtUIButton(startX + (btnW + btnGap) * 6, rowY, btnW, 20, "Delete", btnTheme, this::deleteCurrent), com.turtmod.ui.TurtIcons.trash()));
      this.buttons.add(icon(new TurtUIButton(this.field_22789 - 68, 10, 58, 20, "Back", btnTheme, this::method_25419), com.turtmod.ui.TurtIcons.arrowBack()));
      this.buttons.add(icon(new TurtUIButton(10, this.field_22790 / 2 - 10, 28, 20, "", btnTheme, this::previousScreenshot), com.turtmod.ui.TurtIcons.arrowBack()));
      this.buttons.add(icon(new TurtUIButton(this.field_22789 - 38, this.field_22790 / 2 - 10, 28, 20, "", btnTheme, this::nextScreenshot), com.turtmod.ui.TurtIcons.arrowFwd()));
   }

   private void loadScreenshotList(File initialFile) {
      this.screenshots.clear();
      File directory = initialFile.getParentFile();
      if (directory != null && directory.exists() && directory.isDirectory()) {
         File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
         if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            Collections.addAll(this.screenshots, files);
         }
      }

      for(int i = 0; i < this.screenshots.size(); ++i) {
         if (this.sameFile((File)this.screenshots.get(i), initialFile)) {
            this.currentIndex = i;
            break;
         }
      }

      if (this.currentIndex < 0 && !this.screenshots.isEmpty()) {
         this.currentIndex = 0;
      }

   }

   private boolean sameFile(File left, File right) {
      try {
         return left.getCanonicalFile().equals(right.getCanonicalFile());
      } catch (IOException var4) {
         return left.equals(right);
      }
   }

   private File getCurrentFile() {
      return this.currentIndex >= 0 && this.currentIndex < this.screenshots.size() ? (File)this.screenshots.get(this.currentIndex) : null;
   }

   private void loadCurrentTexture(boolean resetView) {
      File file = this.getCurrentFile();
      this.textureId = null;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.errorMessage = null;
      if (file == null) {
         this.errorMessage = "No screenshot available.";
      } else {
         try {
            FileInputStream input = new FileInputStream(file);

            label41: {
               try {
                  class_1011 image = class_1011.method_4309(input);
                  if (image != null) {
                     this.imageWidth = image.method_4307();
                     this.imageHeight = image.method_4323();
                     int var10000 = Math.abs(file.getAbsolutePath().hashCode());
                     String key = "viewer/" + var10000 + "_" + file.lastModified();
                     this.textureId = class_2960.method_60655("turtmod", key);
                     class_310.method_1551().method_1531().method_4616(this.textureId, new class_1043(() -> "turtmod:" + key, image));
                     if (resetView) {
                        this.pendingViewReset = true;
                     }
                     break label41;
                  }

                  this.errorMessage = "Could not load screenshot.";
               } catch (Throwable var7) {
                  try {
                     input.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }

                  throw var7;
               }

               input.close();
               return;
            }

            input.close();
         } catch (IOException var8) {
            this.errorMessage = "Could not load screenshot.";
         }

      }
   }

   private void resetView() {
      this.fitScale = this.computeFitScale();
      this.zoom = this.fitScale;
      this.panX = (double)0.0F;
      this.panY = (double)0.0F;
      this.pendingViewReset = false;
   }

   private float computeFitScale() {
      if (this.imageWidth > 0 && this.imageHeight > 0) {
         double guiScale = (double)class_310.method_1551().method_22683().method_4495();
         float scaledW = (float)((double)this.imageWidth / guiScale);
         float scaledH = (float)((double)this.imageHeight / guiScale);
         int maxWidth = Math.max(1, this.imageAreaWidth - 32);
         int maxHeight = Math.max(1, this.imageAreaHeight - 32);
         float scale = Math.min((float)maxWidth / scaledW, (float)maxHeight / scaledH);
         return Math.min(1.0F, scale);
      } else {
         return 1.0F;
      }
   }

   private void clampPan() {
      float scale = this.getCurrentScale();
      double guiScale = (double)class_310.method_1551().method_22683().method_4495();
      double drawWidth = (double)this.imageWidth / guiScale * (double)scale;
      double drawHeight = (double)this.imageHeight / guiScale * (double)scale;
      double overflowX = Math.max((double)0.0F, (drawWidth - (double)this.imageAreaWidth) / (double)2.0F);
      double overflowY = Math.max((double)0.0F, (drawHeight - (double)this.imageAreaHeight) / (double)2.0F);
      this.panX = Math.max(-overflowX, Math.min(overflowX, this.panX));
      this.panY = Math.max(-overflowY, Math.min(overflowY, this.panY));
   }

   private float getCurrentScale() {
      return Math.max(0.05F, Math.min(8.0F, this.zoom));
   }

   private void setStatus(String message) {
      this.statusMessage = message;
      this.statusTicks = 80;
   }

   private void previousScreenshot() {
      if (!this.screenshots.isEmpty()) {
         this.currentIndex = (this.currentIndex - 1 + this.screenshots.size()) % this.screenshots.size();
         this.imgFade = 0.0F;
         this.loadCurrentTexture(true);
      }

   }

   private void nextScreenshot() {
      if (!this.screenshots.isEmpty()) {
         this.currentIndex = (this.currentIndex + 1) % this.screenshots.size();
         this.imgFade = 0.0F;
         this.loadCurrentTexture(true);
      }

   }

   private void editCurrent() {
      File f = this.getCurrentFile();
      if (f == null) {
         this.setStatus("No screenshot selected.");
      } else if (this.field_22787 != null) {
         this.field_22787.method_1507(new ScreenshotEditorScreen(this, f));
      }
   }

   private void copyCurrent() {
      File f = this.getCurrentFile();
      if (f == null) {
         this.setStatus("No screenshot selected.");
      } else {
         this.setStatus(ImageClipboardUtils.copyImageToClipboard(f) ? "Copied to clipboard." : "Copy failed.");
      }
   }

   private void openCurrent() {
      File f = this.getCurrentFile();
      if (f == null) {
         this.setStatus("No screenshot selected.");
      } else {
         try {
            class_156.method_668().method_672(f);
            this.setStatus("Opened image.");
         } catch (Exception var3) {
            this.setStatus("Could not open image.");
         }

      }
   }

   private void openFolder() {
      File f = this.getCurrentFile();
      File folder = f == null ? null : f.getParentFile();
      if (folder == null) {
         this.setStatus("Folder not found.");
      } else {
         try {
            class_156.method_668().method_672(folder);
            this.setStatus("Opened folder.");
         } catch (Exception var4) {
            this.setStatus("Could not open folder.");
         }

      }
   }

   private void deleteCurrent() {
      File f = this.getCurrentFile();
      if (f == null) {
         this.setStatus("No screenshot selected.");
      } else {
         try {
            Files.delete(f.toPath());
            this.screenshots.remove(this.currentIndex);
            if (this.screenshots.isEmpty()) {
               this.method_25419();
               return;
            }

            if (this.currentIndex >= this.screenshots.size()) {
               this.currentIndex = this.screenshots.size() - 1;
            }

            this.loadCurrentTexture(true);
            this.setStatus("Deleted image.");
         } catch (IOException var3) {
            this.setStatus("Delete failed.");
         }

      }
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }

   }

   public boolean method_25404(class_11908 input) {
      if (super.method_25404(input)) {
         return true;
      } else {
         boolean var10000;
         switch (input.comp_4795()) {
            case 256:
               this.method_25419();
               var10000 = true;
               break;
            case 257:
            case 258:
            case 260:
            case 261:
            default:
               var10000 = false;
               break;
            case 259:
               this.deleteCurrent();
               var10000 = true;
               break;
            case 262:
               this.nextScreenshot();
               var10000 = true;
               break;
            case 263:
               this.previousScreenshot();
               var10000 = true;
         }

         return var10000;
      }
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      double mouseX = click.comp_4798();
      double mouseY = click.comp_4799();
      int button = click.method_74245();

      for(TurtUIButton btn : this.buttons) {
         if (btn.mouseClicked(mouseX, mouseY, button)) {
            return true;
         }
      }

      if (button == 0 && this.isInsideImageArea(mouseX, mouseY)) {
         this.dragging = true;
         this.dragStartX = mouseX;
         this.dragStartY = mouseY;
         this.dragPanStartX = this.panX;
         this.dragPanStartY = this.panY;
         return true;
      } else if (button == 1) {
         this.resetView();
         return true;
      } else {
         return super.method_25402(click, bl);
      }
   }

   public boolean method_25406(class_11909 click) {
      if (click.method_74245() == 0) {
         this.dragging = false;
      }

      return super.method_25406(click);
   }

   public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
      if (this.dragging && click.method_74245() == 0) {
         this.panX = this.dragPanStartX + (click.comp_4798() - this.dragStartX);
         this.panY = this.dragPanStartY + (click.comp_4799() - this.dragStartY);
         this.clampPan();
         return true;
      } else {
         return super.method_25403(click, deltaX, deltaY);
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.textureId != null && this.imageWidth > 0 && this.imageHeight > 0) {
         if (verticalAmount > (double)0.0F) {
            this.zoom *= 1.15F;
         } else if (verticalAmount < (double)0.0F) {
            this.zoom /= 1.15F;
         }

         this.zoom = Math.max(this.fitScale * 0.75F, Math.min(8.0F, this.zoom));
         this.clampPan();
         return true;
      } else {
         return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   private boolean isInsideImageArea(double mouseX, double mouseY) {
      return mouseX >= (double)this.imageAreaX && mouseX <= (double)(this.imageAreaX + this.imageAreaWidth) && mouseY >= (double)this.imageAreaY && mouseY <= (double)(this.imageAreaY + this.imageAreaHeight);
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      long nowNs = System.nanoTime();
      float dt = Math.min((float)(nowNs - this.lastFrameNs) / 1.0E9F, 0.1F);
      this.lastFrameNs = nowNs;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1.0F, dt, 12.0F);
      this.imgFade = TurtUIUtils.lerp01(this.imgFade, 1.0F, dt, 16.0F);

      TurtUIUtils.drawMenuBackdrop(context, this.field_22789, this.field_22790);
      context.method_25294(0, 0, this.field_22789, this.field_22790, -435221750);
      this.imageAreaX = 12;
      this.imageAreaY = 36;
      this.imageAreaWidth = this.field_22789 - 24;
      this.imageAreaHeight = this.field_22790 - 72;
      context.method_25300(this.field_22793, "SCREENSHOT VIEWER", this.field_22789 / 2, 10, TEXT_MAIN.getRGB());
      File current = this.getCurrentFile();
      context.method_25300(this.field_22793, current == null ? "No screenshot" : current.getName(), this.field_22789 / 2, 24, -204150);
      if (this.statusTicks > 0) {
         context.method_25300(this.field_22793, this.statusMessage, this.field_22789 / 2, this.field_22790 - 54, -4787574);
         --this.statusTicks;
      }

      // Glass frame: an 8px translucent margin band around the image, a rounded backdrop, then a soft outline.
      TurtUIUtils.drawGlassPanel(context, this.imageAreaX - 8, this.imageAreaY - 8, this.imageAreaWidth + 16, this.imageAreaHeight + 16, PANEL_BG);
      TurtUIUtils.drawRoundedRect(context, this.imageAreaX, this.imageAreaY, this.imageAreaWidth, this.imageAreaHeight, 4, PANEL_BG);
      context.method_73198(this.imageAreaX, this.imageAreaY, this.imageAreaWidth, this.imageAreaHeight, PANEL_BORDER.getRGB());
      if (this.errorMessage == null && this.textureId != null && this.imageWidth > 0 && this.imageHeight > 0) {
         if (this.pendingViewReset) {
            this.resetView();
         }

         float scale = this.getCurrentScale();
         double guiScale = (double)class_310.method_1551().method_22683().method_4495();
         int drawWidth = Math.max(1, Math.round((float)((double)this.imageWidth / guiScale) * scale));
         int drawHeight = Math.max(1, Math.round((float)((double)this.imageHeight / guiScale) * scale));
         int drawX = this.imageAreaX + this.imageAreaWidth / 2 - drawWidth / 2 + (int)Math.round(this.panX);
         int drawY = this.imageAreaY + this.imageAreaHeight / 2 - drawHeight / 2 + (int)Math.round(this.panY);
         this.clampPan();
         drawX = this.imageAreaX + this.imageAreaWidth / 2 - drawWidth / 2 + (int)Math.round(this.panX);
         drawY = this.imageAreaY + this.imageAreaHeight / 2 - drawHeight / 2 + (int)Math.round(this.panY);
         context.method_44379(this.imageAreaX, this.imageAreaY, this.imageAreaX + this.imageAreaWidth, this.imageAreaY + this.imageAreaHeight);
         context.method_25302(class_10799.field_56883, this.textureId, drawX, drawY, 0.0F, 0.0F, drawWidth, drawHeight, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
         if (this.imgFade < 0.99F) {
            int veil = ((int)((1.0F - this.imgFade) * 255.0F) << 24) | (PANEL_BG.getRGB() & 0xFFFFFF);
            context.method_25294(this.imageAreaX, this.imageAreaY, this.imageAreaX + this.imageAreaWidth, this.imageAreaY + this.imageAreaHeight, veil);
         }
         context.method_44380();
         int var10000 = this.currentIndex + 1;
         String meta = var10000 + "/" + this.screenshots.size() + "  " + this.imageWidth + "x" + this.imageHeight + "  " + Math.round(scale / this.fitScale * 100.0F) + "%";
         context.method_25300(this.field_22793, meta, this.field_22789 / 2, this.field_22790 - 42, -204150);
      } else {
         context.method_25300(this.field_22793, this.errorMessage == null ? "Could not load screenshot." : this.errorMessage, this.field_22789 / 2, this.field_22790 / 2, -32640);
      }

      for(TurtUIButton button : this.buttons) {
         button.render(context, mouseX, mouseY, this.field_22793);
      }

      // Smooth fade-in from black on open (~180ms)
      if (this.openFade < 0.99F) {
         int a = (int)((1.0F - this.openFade) * 255.0F) & 255;
         context.method_25294(0, 0, this.field_22789, this.field_22790, a << 24);
      }

      super.method_25394(context, mouseX, mouseY, delta);
   }
}
