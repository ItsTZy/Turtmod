package com.turtmod.gallery;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * In-game screenshot editor (Snipping-Tool style): crop + draw pen/highlighter/line/arrow/rect/ellipse/text
 * on a screenshot, then Save / Save Copy / Copy. Edits are lightweight vector annotations drawn live over
 * the base texture (instant undo, no re-upload); crop is just a blit source sub-rect. Only on save are the
 * annotations rasterized into a PNG via {@link EditorRasterizer} (AWT).
 */
public class ScreenshotEditorScreen extends class_437 {
   public enum Tool { PAN, MOVE, ERASER, CROP, PEN, HIGHLIGHTER, LINE, ARROW, RECT, ELLIPSE, TEXT, BLUR, PIXELATE }

   /** One committed (or in-progress) vector edit, in image-pixel coordinates. */
   public static final class Annotation {
      public Tool type;
      public int argb;
      public float size;
      public boolean filled;      // RECT / ELLIPSE: solid fill instead of outline
      public final List<double[]> pts = new ArrayList<>();
      public String text = "";

      Annotation(Tool type, int argb, float size) {
         this.type = type;
         this.argb = argb;
         this.size = size;
      }
   }

   private static final class State {
      final List<Annotation> anns;
      final int[] crop;

      State(List<Annotation> anns, int[] crop) {
         this.anns = anns;
         this.crop = crop;
      }
   }

   private static final int[] SWATCHES = {
      0xFFF23B3B, 0xFFF08A24, 0xFFF5D523, 0xFF57E08A, 0xFF4AA3FF, 0xFFB07BFF,
      0xFFFF9DAE, 0xFFFFFFFF, 0xFF202225, 0xFF9AA0A6
   };
   private static final float[] SIZES = {2f, 5f, 9f, 14f};
   private static final String[] SIZE_LABELS = {"S", "M", "L", "XL"};

   private final class_437 parent;
   private final File file;
   private final List<TurtUIButton> buttons = new ArrayList<>();

   private class_2960 textureId;
   private int imageWidth, imageHeight;
   private String errorMessage;
   private String status = "";
   private int statusTicks;

   private int[] crop = {0, 0, 0, 0};
   private final List<Annotation> annotations = new ArrayList<>();
   private final Deque<State> undo = new ArrayDeque<>();
   private final Deque<State> redo = new ArrayDeque<>();

   private Tool tool = Tool.PEN;
   private Tool hoverTool = null;   // tool the cursor is over this frame (for the tooltip)
   private int colorIdx = 0;
   private int sizeIdx = 1;
   private int alphaPct = 100;      // opacity of the colour being drawn with
   private boolean fillShapes;      // rect/ellipse solid instead of outline

   // Custom colour: an extra swatch backed by an HSV picker (hue strip + saturation/value square).
   private float pickH = 0.55f, pickS = 0.85f, pickV = 1.0f;
   private boolean pickerOpen;
   private int pickerX, pickerY;
   private static final int PICK_W = 108, PICK_H = 64, HUE_H = 10;

   // ── Live overlay ───────────────────────────────────────────────────────────
   // Committed annotations are rasterized by EditorRasterizer (the same AWT code that writes the PNG)
   // into an image-space texture that is simply blitted. That makes the preview match the saved file
   // exactly, and costs one blit per frame instead of thousands of per-pixel fills.
   private static final int OVERLAY_MAX = 1920;
   private class_2960 overlayId;
   private int overlayW, overlayH;
   private double overlayScale = 1.0;
   private int annVersion, overlayVersion = -1;

   // The in-progress stroke gets its own small overlay covering just its bounds, rendered by the same
   // rasterizer, so a stroke looks identical while you draw it and after it is committed.
   private class_2960 draftId;
   private int draftTexW, draftTexH;
   private double draftOriginX, draftOriginY, draftBoxW, draftBoxH;
   private boolean draftDirty;
   private long lastDraftRenderNs;

   private Annotation draft;            // shape/pen currently being dragged
   private Annotation editingText;      // text annotation being typed
   private Annotation moving;           // committed annotation lifted out for dragging
   private int movingIndex = -1;        // where to drop it back in the z-order
   private int hoverAnn = -1;           // annotation under the cursor while the Move tool is active
   private boolean panning;
   private double lastMx, lastMy;

   // View transform (recomputed each frame).
   private float zoom = 1f, fitScale = 1f;
   private double panX, panY;
   private boolean pendingFit = true;
   private int canvasX, canvasY, canvasW, canvasH;
   private double drawX, drawY, sp = 1.0; // sp = screen px per image px
   private float openFade;
   private long lastFrameNs = System.nanoTime();

   // Tool-rail / bottom-bar hit rects, filled during render.
   private final int[] railX = new int[Tool.values().length];
   private final int[] railY = new int[Tool.values().length];
   private final int[] swX = new int[SWATCHES.length + 1];
   private int swY, sizeBarX, sizeBarY, undoX, redoX, barY, fillBtnX, opacityBtnX;

   private static final int RAIL_W = 26, RAIL_BTN = 22, RAIL_GAP = 4;
   private static final int SW = 16, SWG = 5;

   public ScreenshotEditorScreen(class_437 parent, File file) {
      super(class_2561.method_43470("Screenshot Editor"));
      this.parent = parent;
      this.file = file;
   }

   protected void method_25426() {
      if (this.textureId == null && this.errorMessage == null) {
         this.loadTexture();
      }
      this.rebuildButtons();
   }

   private void rebuildButtons() {
      this.buttons.clear();
      TurtUITheme t = new TurtUITheme(Palette.BTN_BG, Palette.PANEL_BORDER, Palette.TEXT, Palette.BTN_HOVER, Palette.GREEN);
      int bw = 78, bh = 20, gap = 6, y = 10;
      int x = this.field_22789 - 12 - bw;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Cancel", t, this::onCancel));
      x -= bw + gap;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Copy", t, this::onCopy));
      x -= bw + gap;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Save Copy", t, this::onSaveCopy));
      x -= bw + gap;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Save", t, this::onSave));
   }

   private void loadTexture() {
      try (FileInputStream in = new FileInputStream(this.file)) {
         class_1011 image = class_1011.method_4309(in);
         if (image == null) {
            this.errorMessage = "Could not load screenshot.";
            return;
         }
         this.imageWidth = image.method_4307();
         this.imageHeight = image.method_4323();
         this.crop = new int[]{0, 0, this.imageWidth, this.imageHeight};
         String key = "editor/" + Math.abs(this.file.getAbsolutePath().hashCode()) + "_" + this.file.lastModified();
         this.textureId = class_2960.method_60655("turtmod", key);
         class_310.method_1551().method_1531().method_4616(this.textureId, new class_1043(() -> "turtmod:" + key, image));
      } catch (Exception e) {
         this.errorMessage = "Could not load screenshot.";
      }
   }

   /** Topmost committed annotation whose painted bounds contain this image point, or -1. */
   private int hitAnnotation(double ix, double iy) {
      for (int i = this.annotations.size() - 1; i >= 0; i--) {
         double[] b = EditorRasterizer.bounds(this.annotations.get(i));
         if (ix >= b[0] && ix <= b[2] && iy >= b[1] && iy <= b[3]) {
            return i;
         }
      }
      return -1;
   }

   /** The custom swatch's RGB, from the picker's current hue/saturation/value. */
   private int customColor() {
      return 0xFF000000 | (Color.HSBtoRGB(this.pickH, this.pickS, this.pickV) & 0xFFFFFF);
   }

   /** Palette entry i - the fixed swatches, then the custom one at the end. */
   private int swatchColor(int i) {
      return i < SWATCHES.length ? SWATCHES[i] : this.customColor();
   }

   /** Number of selectable swatches (fixed palette + the custom one). */
   private int swatchCount() {
      return SWATCHES.length + 1;
   }

   /** The active swatch with the current opacity applied. */
   private int currentColor() {
      int a = Math.max(0, Math.min(255, Math.round(this.alphaPct * 2.55f)));
      return (a << 24) | (this.swatchColor(this.colorIdx) & 0xFFFFFF);
   }

   /** Mark the committed-annotation set dirty so the overlay is rebuilt next frame. */
   private void invalidateOverlay() {
      this.annVersion++;
   }

   /** Mark the in-progress stroke dirty (it re-renders on the next frame, throttled). */
   private void markDraftDirty() {
      this.draftDirty = true;
   }

   /** The live, not-yet-committed annotations: the stroke being dragged and any text being typed. */
   private List<Annotation> liveAnnotations() {
      List<Annotation> live = new ArrayList<>(2);
      if (this.moving != null) {
         live.add(this.moving);
      }
      if (this.draft != null && this.draft.type != Tool.CROP) {
         live.add(this.draft);
      }
      if (this.editingText != null && !this.editingText.text.isEmpty()) {
         live.add(this.editingText);
      }
      return live;
   }

   /**
    * Rebuild the small overlay for the live stroke. Rendered by {@link EditorRasterizer} exactly like the
    * committed overlay, so committing a stroke never changes how it looks. Throttled because a drag can
    * fire many times a frame; the bounds are just the stroke's own box, so it stays cheap.
    */
   private void ensureDraftOverlay() {
      List<Annotation> live = this.liveAnnotations();
      if (live.isEmpty()) {
         this.draftId = null;
         this.draftDirty = false;
         return;
      }
      long now = System.nanoTime();
      if (!this.draftDirty || (this.draftId != null && now - this.lastDraftRenderNs < 25_000_000L)) {
         return;
      }
      this.draftDirty = false;
      this.lastDraftRenderNs = now;
      try {
         double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
         for (Annotation a : live) {
            double[] b = EditorRasterizer.bounds(a);
            minX = Math.min(minX, b[0]);
            minY = Math.min(minY, b[1]);
            maxX = Math.max(maxX, b[2]);
            maxY = Math.max(maxY, b[3]);
         }
         minX = Math.max(0, minX);
         minY = Math.max(0, minY);
         maxX = Math.min(this.imageWidth, maxX);
         maxY = Math.min(this.imageHeight, maxY);
         if (maxX <= minX || maxY <= minY) {
            this.draftId = null;
            return;
         }
         this.draftOriginX = minX;
         this.draftOriginY = minY;
         this.draftBoxW = maxX - minX;
         this.draftBoxH = maxY - minY;
         this.draftTexW = Math.max(1, (int) Math.round(this.draftBoxW * this.overlayScale));
         this.draftTexH = Math.max(1, (int) Math.round(this.draftBoxH * this.overlayScale));

         BufferedImage img = EditorRasterizer.renderOverlayRegion(this.draftTexW, this.draftTexH,
            this.overlayScale, this.draftOriginX, this.draftOriginY, live);
         class_1011 baked = new class_1011(this.draftTexW, this.draftTexH, false);
         int[] row = new int[this.draftTexW];
         for (int y = 0; y < this.draftTexH; y++) {
            img.getRGB(0, y, this.draftTexW, 1, row, 0, this.draftTexW);
            for (int x = 0; x < this.draftTexW; x++) {
               baked.method_61941(x, y, row[x]);
            }
         }
         String key = "editor_draft/" + Math.abs(this.file.getAbsolutePath().hashCode());
         this.draftId = class_2960.method_60655("turtmod", key);
         class_310.method_1551().method_1531().method_4616(this.draftId, new class_1043(() -> "turtmod:" + key, baked));
      } catch (Exception e) {
         this.draftId = null;
      }
   }

   /**
    * Rebuild the overlay texture from the committed annotations, using the same rasterizer as save.
    * Only runs when the annotation set actually changed — panning/zooming just re-blits.
    */
   private void ensureOverlay() {
      if (this.overlayVersion == this.annVersion || this.imageWidth <= 0) {
         return;
      }
      this.overlayVersion = this.annVersion;
      try {
         this.overlayScale = Math.min(1.0, (double) OVERLAY_MAX / Math.max(this.imageWidth, this.imageHeight));
         this.overlayW = Math.max(1, (int) Math.round(this.imageWidth * this.overlayScale));
         this.overlayH = Math.max(1, (int) Math.round(this.imageHeight * this.overlayScale));
         BufferedImage img = EditorRasterizer.renderOverlay(this.overlayW, this.overlayH, this.overlayScale, this.annotations);

         class_1011 native_ = new class_1011(this.overlayW, this.overlayH, false);
         int[] row = new int[this.overlayW];
         for (int y = 0; y < this.overlayH; y++) {
            img.getRGB(0, y, this.overlayW, 1, row, 0, this.overlayW);
            for (int x = 0; x < this.overlayW; x++) {
               // These accessors take ARGB straight from BufferedImage - no channel swap.
               native_.method_61941(x, y, row[x]);
            }
         }
         String key = "editor_overlay/" + Math.abs(this.file.getAbsolutePath().hashCode());
         this.overlayId = class_2960.method_60655("turtmod", key);
         // Registering under the same id replaces (and disposes) the previous overlay texture,
         // matching how the gallery/viewer manage their textures.
         class_310.method_1551().method_1531().method_4616(this.overlayId, new class_1043(() -> "turtmod:" + key, native_));
      } catch (Exception e) {
         this.overlayId = null;
      }
   }

   // ── Undo / redo ──
   private void pushUndo() {
      this.undo.push(new State(new ArrayList<>(this.annotations), this.crop.clone()));
      if (this.undo.size() > 60) {
         this.undo.removeLast();
      }
      this.redo.clear();
   }

   private void doUndo() {
      if (this.undo.isEmpty()) {
         return;
      }
      this.redo.push(new State(new ArrayList<>(this.annotations), this.crop.clone()));
      State s = this.undo.pop();
      this.applyState(s);
   }

   private void doRedo() {
      if (this.redo.isEmpty()) {
         return;
      }
      this.undo.push(new State(new ArrayList<>(this.annotations), this.crop.clone()));
      State s = this.redo.pop();
      this.applyState(s);
   }

   private void applyState(State s) {
      this.annotations.clear();
      this.annotations.addAll(s.anns);
      boolean cropChanged = this.crop[0] != s.crop[0] || this.crop[1] != s.crop[1]
         || this.crop[2] != s.crop[2] || this.crop[3] != s.crop[3];
      this.crop = s.crop.clone();
      this.editingText = null;
      this.invalidateOverlay();
      if (cropChanged) {
         this.pendingFit = true;
      }
   }

   // ── Coordinate transform ──
   private double sx(double ix) {
      return this.drawX + (ix - this.crop[0]) * this.sp;
   }

   private double sy(double iy) {
      return this.drawY + (iy - this.crop[1]) * this.sp;
   }

   private double toImageX(double screenX) {
      return this.crop[0] + (screenX - this.drawX) / this.sp;
   }

   private double toImageY(double screenY) {
      return this.crop[1] + (screenY - this.drawY) / this.sp;
   }

   private boolean inCanvas(double mx, double my) {
      return mx >= this.canvasX && mx <= this.canvasX + this.canvasW && my >= this.canvasY && my <= this.canvasY + this.canvasH;
   }

   private double clampIX(double ix) {
      return Math.max(0, Math.min(this.imageWidth, ix));
   }

   private double clampIY(double iy) {
      return Math.max(0, Math.min(this.imageHeight, iy));
   }

   private void setStatus(String s) {
      this.status = s;
      this.statusTicks = 80;
   }

   // ── Actions ──
   private void onCancel() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   private void commitEditingText() {
      if (this.editingText != null) {
         if (!this.editingText.text.isEmpty()) {
            this.pushUndo();
            this.annotations.add(this.editingText);
            this.invalidateOverlay();
         }
         this.editingText = null;
      }
   }

   private BufferedImage bakeOrStatus() {
      this.commitEditingText();
      try {
         return EditorRasterizer.bake(this.file, this.crop, this.annotations);
      } catch (Exception e) {
         this.setStatus("Bake failed: " + e.getMessage());
         return null;
      }
   }

   private void onSave() {
      BufferedImage img = this.bakeOrStatus();
      if (img == null) {
         return;
      }
      try {
         EditorRasterizer.save(img, this.file);
         this.setStatus("Saved.");
      } catch (Exception e) {
         this.setStatus("Save failed: " + e.getMessage());
      }
   }

   private void onSaveCopy() {
      BufferedImage img = this.bakeOrStatus();
      if (img == null) {
         return;
      }
      try {
         String name = this.file.getName();
         int dot = name.lastIndexOf('.');
         String base = dot > 0 ? name.substring(0, dot) : name;
         File out = new File(this.file.getParentFile(), base + "-edited.png");
         int n = 2;
         while (out.exists()) {
            out = new File(this.file.getParentFile(), base + "-edited-" + n++ + ".png");
         }
         EditorRasterizer.save(img, out);
         this.setStatus("Saved " + out.getName());
      } catch (Exception e) {
         this.setStatus("Save failed: " + e.getMessage());
      }
   }

   private void onCopy() {
      BufferedImage img = this.bakeOrStatus();
      if (img == null) {
         return;
      }
      this.setStatus(EditorRasterizer.copyToClipboard(img) ? "Copied to clipboard." : "Copy failed.");
   }

   // ── Input ──
   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = click.comp_4798(), my = click.comp_4799();
      int button = click.method_74245();
      for (TurtUIButton b : this.buttons) {
         if (b.mouseClicked(mx, my, button)) {
            return true;
         }
      }
      if (button == 0) {
         // Tool rail.
         for (Tool tv : Tool.values()) {
            int i = tv.ordinal();
            if (mx >= this.railX[i] && mx <= this.railX[i] + RAIL_BTN && my >= this.railY[i] && my <= this.railY[i] + RAIL_BTN) {
               this.commitEditingText();
               this.tool = tv;
               return true;
            }
         }
         // Colour picker panel (checked before the bar so its area isn't stolen by what's underneath).
         if (this.pickerOpen && this.handlePickerClick(mx, my)) {
            return true;
         }
         // Swatches; clicking the custom one (last) opens/closes the picker.
         for (int i = 0; i < this.swatchCount(); i++) {
            if (mx >= this.swX[i] && mx <= this.swX[i] + SW && my >= this.swY && my <= this.swY + SW) {
               if (i == SWATCHES.length) {
                  this.pickerOpen = this.colorIdx == i ? !this.pickerOpen : true;
               } else {
                  this.pickerOpen = false;
               }
               this.colorIdx = i;
               if (this.editingText != null) {
                  this.editingText.argb = this.currentColor();
               }
               return true;
            }
         }
         // Sizes.
         for (int i = 0; i < SIZES.length; i++) {
            int bx = this.sizeBarX + i * (18 + 3);
            if (mx >= bx && mx <= bx + 18 && my >= this.sizeBarY && my <= this.sizeBarY + SW) {
               this.sizeIdx = i;
               if (this.editingText != null) {
                  this.editingText.size = SIZES[i];
               }
               return true;
            }
         }
         // Fill toggle.
         if (mx >= this.fillBtnX && mx <= this.fillBtnX + 30 && my >= this.sizeBarY && my <= this.sizeBarY + SW) {
            this.fillShapes = !this.fillShapes;
            return true;
         }
         // Opacity cycle: 100 -> 75 -> 50 -> 25 -> 100.
         if (mx >= this.opacityBtnX && mx <= this.opacityBtnX + 34 && my >= this.sizeBarY && my <= this.sizeBarY + SW) {
            this.alphaPct = this.alphaPct <= 25 ? 100 : this.alphaPct - 25;
            if (this.editingText != null) {
               this.editingText.argb = this.currentColor();
            }
            return true;
         }
         // Undo / Redo.
         if (mx >= this.undoX && mx <= this.undoX + 26 && my >= this.barY && my <= this.barY + 18) {
            this.commitEditingText();
            this.doUndo();
            return true;
         }
         if (mx >= this.redoX && mx <= this.redoX + 26 && my >= this.barY && my <= this.barY + 18) {
            this.commitEditingText();
            this.doRedo();
            return true;
         }
      }

      if (this.inCanvas(mx, my) && this.textureId != null) {
         if (button == 1 || this.tool == Tool.PAN) {
            this.panning = true;
            this.lastMx = mx;
            this.lastMy = my;
            return true;
         }
         if (button == 0) {
            double ix = this.clampIX(this.toImageX(mx)), iy = this.clampIY(this.toImageY(my));
            if (this.tool == Tool.MOVE) {
               int hit = this.hitAnnotation(ix, iy);
               if (hit >= 0) {
                  this.pushUndo();
                  this.movingIndex = hit;
                  this.moving = this.annotations.remove(hit);
                  this.invalidateOverlay();   // it now renders through the cheap live overlay
                  this.markDraftDirty();
                  this.lastMx = mx;
                  this.lastMy = my;
                  this.setStatus("Moving - drag to reposition.");
               }
               return true;
            }
            if (this.tool == Tool.ERASER) {
               int hit = this.hitAnnotation(ix, iy);
               if (hit >= 0) {
                  this.pushUndo();
                  this.annotations.remove(hit);
                  this.invalidateOverlay();
                  this.markDraftDirty();
                  this.setStatus("Erased.");
               } else {
                  this.setStatus("Nothing to erase there.");
               }
               return true;
            }
            if (this.tool == Tool.TEXT) {
               this.commitEditingText();
               this.editingText = new Annotation(Tool.TEXT, this.currentColor(), SIZES[this.sizeIdx]);
               this.editingText.pts.add(new double[]{ix, iy});
               return true;
            }
            this.draft = new Annotation(this.tool, this.currentColor(), SIZES[this.sizeIdx]);
            this.draft.filled = this.fillShapes && (this.tool == Tool.RECT || this.tool == Tool.ELLIPSE);
            this.draft.pts.add(new double[]{ix, iy});
            if (this.tool != Tool.PEN && this.tool != Tool.HIGHLIGHTER) {
               this.draft.pts.add(new double[]{ix, iy}); // second point for shapes
            }
            this.markDraftDirty();
            return true;
         }
      }
      return super.method_25402(click, bl);
   }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      double mx = click.comp_4798(), my = click.comp_4799();
      // Dragging inside the open picker keeps updating the colour.
      if (this.pickerOpen && this.handlePickerClick(mx, my)) {
         return true;
      }
      if (this.panning) {
         this.panX += mx - this.lastMx;
         this.panY += my - this.lastMy;
         this.lastMx = mx;
         this.lastMy = my;
         return true;
      }
      if (this.moving != null) {
         double dix = (mx - this.lastMx) / this.sp;
         double diy = (my - this.lastMy) / this.sp;
         for (double[] pt : this.moving.pts) {
            pt[0] += dix;
            pt[1] += diy;
         }
         this.lastMx = mx;
         this.lastMy = my;
         this.markDraftDirty();
         return true;
      }
      if (this.draft != null) {
         double ix = this.clampIX(this.toImageX(mx)), iy = this.clampIY(this.toImageY(my));
         if (this.draft.type == Tool.PEN || this.draft.type == Tool.HIGHLIGHTER) {
            this.draft.pts.add(new double[]{ix, iy});
         } else {
            this.draft.pts.set(1, new double[]{ix, iy});
         }
         this.markDraftDirty();
         return true;
      }
      return super.method_25403(click, dx, dy);
   }

   public boolean method_25406(class_11909 click) {
      if (this.panning) {
         this.panning = false;
         return true;
      }
      if (this.moving != null) {
         this.annotations.add(Math.min(this.movingIndex, this.annotations.size()), this.moving);
         this.moving = null;
         this.movingIndex = -1;
         this.draftId = null;
         this.invalidateOverlay();
         return true;
      }
      if (this.draft != null) {
         Annotation d = this.draft;
         this.draft = null;
         this.draftId = null;
         this.markDraftDirty();
         if (d.type == Tool.CROP) {
            this.applyCrop(d);
         } else if (this.validDraft(d)) {
            this.pushUndo();
            this.annotations.add(d);
            this.invalidateOverlay();
         }
         return true;
      }
      return super.method_25406(click);
   }

   private boolean validDraft(Annotation d) {
      if (d.type == Tool.PEN || d.type == Tool.HIGHLIGHTER) {
         return d.pts.size() >= 2 || (d.pts.size() == 1);
      }
      double[] a = d.pts.get(0), b = d.pts.get(1);
      return Math.hypot(b[0] - a[0], b[1] - a[1]) >= 2.0;
   }

   private void applyCrop(Annotation d) {
      double[] a = d.pts.get(0), b = d.pts.get(1);
      int x = (int) Math.round(Math.min(a[0], b[0]));
      int y = (int) Math.round(Math.min(a[1], b[1]));
      int w = (int) Math.round(Math.abs(b[0] - a[0]));
      int h = (int) Math.round(Math.abs(b[1] - a[1]));
      if (w < 8 || h < 8) {
         return;
      }
      this.pushUndo();
      this.crop = new int[]{x, y, w, h};
      this.pendingFit = true;
   }

   public boolean method_25401(double mx, double my, double horizontal, double vertical) {
      if (this.textureId != null && this.inCanvas(mx, my)) {
         if (vertical > 0) {
            this.zoom *= 1.12f;
         } else if (vertical < 0) {
            this.zoom /= 1.12f;
         }
         this.zoom = Math.max(this.fitScale * 0.5f, Math.min(12f, this.zoom));
         return true;
      }
      return super.method_25401(mx, my, horizontal, vertical);
   }

   public boolean method_25400(class_11905 event) {
      if (this.editingText != null) {
         String s = event.method_74226();
         if (s != null && !s.isEmpty()) {
            this.editingText.text += s;
            this.markDraftDirty();
            return true;
         }
      }
      return super.method_25400(event);
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (this.editingText != null) {
         switch (key) {
            case 256 -> this.editingText = null;              // esc: discard
            case 257, 335 -> this.commitEditingText();        // enter: commit
            case 259 -> {                                     // backspace
               String tx = this.editingText.text;
               if (!tx.isEmpty()) {
                  this.editingText.text = tx.substring(0, tx.length() - 1);
                  this.markDraftDirty();
               }
            }
            default -> {
            }
         }
         return true;
      }
      // Single-key tool shortcuts, like a real editor.
      Tool shortcut = switch (key) {
         case 86 -> Tool.MOVE;         // V
         case 68 -> Tool.ERASER;       // D (delete)
         case 67 -> Tool.CROP;         // C
         case 80 -> Tool.PEN;          // P
         case 72 -> Tool.HIGHLIGHTER;  // H
         case 76 -> Tool.LINE;         // L
         case 65 -> Tool.ARROW;        // A
         case 82 -> Tool.RECT;         // R
         case 69 -> Tool.ELLIPSE;      // E
         case 84 -> Tool.TEXT;         // T
         case 66 -> Tool.BLUR;         // B
         case 88 -> Tool.PIXELATE;     // X
         case 32 -> Tool.PAN;          // Space
         default -> null;
      };
      if (shortcut != null) {
         this.tool = shortcut;
         this.pickerOpen = false;
         this.setStatus(shortcut.name().charAt(0) + shortcut.name().substring(1).toLowerCase());
         return true;
      }
      // Delete / Backspace removes whatever the Move tool is hovering.
      if ((key == 261 || key == 259) && this.hoverAnn >= 0 && this.hoverAnn < this.annotations.size()) {
         this.pushUndo();
         this.annotations.remove(this.hoverAnn);
         this.hoverAnn = -1;
         this.invalidateOverlay();
         this.setStatus("Deleted.");
         return true;
      }
      if (key == 256) {
         this.onCancel();
         return true;
      }
      return super.method_25404(input);
   }

   public void method_25419() {
      this.onCancel();
   }

   // ── Render ──
   public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
      long nowNs = System.nanoTime();
      float dt = Math.min((float) (nowNs - this.lastFrameNs) / 1.0E9F, 0.1F);
      this.lastFrameNs = nowNs;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, dt, 12f);

      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      // Gentle extra darkening for canvas contrast, but keep the shared backdrop (gradient + drifting
      // glows) visible so the editor matches the hub/gallery instead of reading as a separate app.
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, 0x66121316);
      // Mod branding logo + gradient title, like the chrome screens.
      com.turtmod.ui.BrandingRenderer.drawLogo(ctx, 12, 8, 20, 20);
      TurtUIUtils.drawGradientText(ctx, this.field_22793, "SCREENSHOT EDITOR", 38, 12,
         Palette.GREEN, Palette.PINK, false, true);

      this.canvasX = 46;
      this.canvasY = 40;
      this.canvasW = this.field_22789 - this.canvasX - 12;
      this.canvasH = this.field_22790 - this.canvasY - 46;

      if (this.errorMessage != null || this.textureId == null) {
         ctx.method_25300(this.field_22793, this.errorMessage == null ? "Loading…" : this.errorMessage,
            this.field_22789 / 2, this.field_22790 / 2, 0xFFFF8080);
      } else {
         this.renderCanvas(ctx, mouseX, mouseY);
      }

      this.renderToolRail(ctx, mouseX, mouseY);
      this.renderBottomBar(ctx, mouseX, mouseY);

      for (TurtUIButton b : this.buttons) {
         b.render(ctx, mouseX, mouseY, this.field_22793);
      }
      if (this.statusTicks > 0) {
         ctx.method_25300(this.field_22793, this.status, this.field_22789 / 2, this.field_22790 - 14, 0xFFB9F5C4);
         this.statusTicks--;
      }
      this.renderToolTooltip(ctx, mouseX, mouseY);
      if (this.openFade < 0.99F) {
         int a = (int) ((1f - this.openFade) * 255f) & 255;
         ctx.method_25294(0, 0, this.field_22789, this.field_22790, a << 24);
      }
      super.method_25394(ctx, mouseX, mouseY, delta);
   }

   private void renderCanvas(class_332 ctx, int mouseX, int mouseY) {
      TurtUIUtils.drawRoundedRect(ctx, this.canvasX - 4, this.canvasY - 4, this.canvasW + 8, this.canvasH + 8, 5, Palette.alpha(Palette.PANEL_BG, 235));
      ctx.method_73198(this.canvasX - 4, this.canvasY - 4, this.canvasW + 8, this.canvasH + 8, Palette.PANEL_BORDER.getRGB());

      double guiScale = class_310.method_1551().method_22683().method_4495();
      double scaledW = this.crop[2] / guiScale, scaledH = this.crop[3] / guiScale;
      if (this.pendingFit) {
         this.fitScale = (float) Math.min(1.0, Math.min((this.canvasW - 20) / scaledW, (this.canvasH - 20) / scaledH));
         if (this.fitScale <= 0f) {
            this.fitScale = 0.05f;
         }
         this.zoom = this.fitScale;
         this.panX = 0;
         this.panY = 0;
         this.pendingFit = false;
      }
      int drawW = Math.max(1, (int) Math.round(scaledW * this.zoom));
      int drawH = Math.max(1, (int) Math.round(scaledH * this.zoom));
      this.drawX = this.canvasX + this.canvasW / 2.0 - drawW / 2.0 + this.panX;
      this.drawY = this.canvasY + this.canvasH / 2.0 - drawH / 2.0 + this.panY;
      this.sp = drawW / (double) this.crop[2];

      ctx.method_44379(this.canvasX, this.canvasY, this.canvasX + this.canvasW, this.canvasY + this.canvasH);
      ctx.method_25302(class_10799.field_56883, this.textureId, (int) Math.round(this.drawX), (int) Math.round(this.drawY),
         (float) this.crop[0], (float) this.crop[1], drawW, drawH, this.crop[2], this.crop[3], this.imageWidth, this.imageHeight);

      // Committed edits come from the shared AWT rasterizer, so what you see is what gets saved.
      this.ensureOverlay();
      if (this.overlayId != null) {
         float ou = (float) (this.crop[0] * this.overlayScale);
         float ov = (float) (this.crop[1] * this.overlayScale);
         int orw = Math.max(1, (int) Math.round(this.crop[2] * this.overlayScale));
         int orh = Math.max(1, (int) Math.round(this.crop[3] * this.overlayScale));
         ctx.method_25302(class_10799.field_56883, this.overlayId, (int) Math.round(this.drawX), (int) Math.round(this.drawY),
            ou, ov, drawW, drawH, orw, orh, this.overlayW, this.overlayH);
      }
      // The live stroke is drawn by the same rasterizer, so committing it never changes its look.
      this.ensureDraftOverlay();
      if (this.draftId != null) {
         int dx = (int) Math.round(this.sx(this.draftOriginX));
         int dy = (int) Math.round(this.sy(this.draftOriginY));
         int dw = Math.max(1, (int) Math.round(this.draftBoxW * this.sp));
         int dh = Math.max(1, (int) Math.round(this.draftBoxH * this.sp));
         ctx.method_25302(class_10799.field_56883, this.draftId, dx, dy, 0f, 0f, dw, dh,
            this.draftTexW, this.draftTexH, this.draftTexW, this.draftTexH);
      }
      if (this.draft != null && this.draft.type == Tool.CROP) {
         this.drawCropOverlay(ctx, this.draft);
      }
      // Move tool: outline whatever is under the cursor so it's clear what will be grabbed/deleted.
      this.hoverAnn = -1;
      if (this.tool == Tool.MOVE && this.moving == null && this.inCanvas(mouseX, mouseY)) {
         this.hoverAnn = this.hitAnnotation(this.toImageX(mouseX), this.toImageY(mouseY));
         if (this.hoverAnn >= 0) {
            double[] b = EditorRasterizer.bounds(this.annotations.get(this.hoverAnn));
            int bx1 = (int) Math.round(this.sx(b[0])), by1 = (int) Math.round(this.sy(b[1]));
            int bx2 = (int) Math.round(this.sx(b[2])), by2 = (int) Math.round(this.sy(b[3]));
            int accent = Palette.GREEN.getRGB();
            ctx.method_25294(bx1, by1, bx2, by1 + 1, accent);
            ctx.method_25294(bx1, by2 - 1, bx2, by2, accent);
            ctx.method_25294(bx1, by1, bx1 + 1, by2, accent);
            ctx.method_25294(bx2 - 1, by1, bx2, by2, accent);
         }
      }
      if (this.editingText != null) {
         double cx = this.sx(this.editingText.pts.get(0)[0]);
         double cy = this.sy(this.editingText.pts.get(0)[1]);
         if ((System.currentTimeMillis() / 500) % 2 == 0) {
            int th = Math.max(1, (int) Math.round(EditorRasterizer.fontPx(this.editingText.size) * this.sp));
            int tw = this.field_22793.method_1727(lastLine(this.editingText.text));
            double scpx = th / 8.0;
            ctx.method_25294((int) (cx + tw * scpx) + 1, (int) cy, (int) (cx + tw * scpx) + 2, (int) (cy + th), this.editingText.argb);
         }
      }
      ctx.method_44380();

      // Info line.
      String info = this.imageWidth + "×" + this.imageHeight
         + (this.isCropped() ? "  ✂ " + this.crop[2] + "×" + this.crop[3] : "")
         + "  " + Math.round(this.zoom / Math.max(0.001f, this.fitScale) * 100) + "%";
      ctx.method_51433(this.field_22793, info, this.canvasX, this.canvasY + this.canvasH + 6, 0xFF8A9199, false);
      ctx.method_51433(this.field_22793, "Right-drag pans · scroll zooms", this.canvasX + 220, this.canvasY + this.canvasH + 6, 0xFF6C7278, false);
   }

   private boolean isCropped() {
      return this.crop[0] != 0 || this.crop[1] != 0 || this.crop[2] != this.imageWidth || this.crop[3] != this.imageHeight;
   }

   private static String lastLine(String s) {
      int nl = s.lastIndexOf('\n');
      return nl >= 0 ? s.substring(nl + 1) : s;
   }

   private void drawCropOverlay(class_332 ctx, Annotation d) {
      double x1 = this.sx(d.pts.get(0)[0]), y1 = this.sy(d.pts.get(0)[1]);
      double x2 = this.sx(d.pts.get(1)[0]), y2 = this.sy(d.pts.get(1)[1]);
      int lx = (int) Math.min(x1, x2), ly = (int) Math.min(y1, y2), hx = (int) Math.max(x1, x2), hy = (int) Math.max(y1, y2);
      // Dim everything outside the selection.
      int dim = 0x99000000;
      ctx.method_25294(this.canvasX, this.canvasY, this.canvasX + this.canvasW, ly, dim);
      ctx.method_25294(this.canvasX, hy, this.canvasX + this.canvasW, this.canvasY + this.canvasH, dim);
      ctx.method_25294(this.canvasX, ly, lx, hy, dim);
      ctx.method_25294(hx, ly, this.canvasX + this.canvasW, hy, dim);
      ctx.method_73198(lx, ly, hx - lx, hy - ly, Palette.GREEN.getRGB());
   }

   private void drawAnnotation(class_332 ctx, Annotation a) {
      int th = Math.max(1, (int) Math.round(a.size * this.sp));
      switch (a.type) {
         case PEN -> this.drawPolyline(ctx, a, a.argb, th);
         case HIGHLIGHTER -> this.drawPolyline(ctx, a, (a.argb & 0xFFFFFF) | 0x60000000, Math.max(2, (int) Math.round(a.size * 2.4 * this.sp)));
         case LINE -> this.thickLine(ctx, this.sx(a.pts.get(0)[0]), this.sy(a.pts.get(0)[1]), this.sx(a.pts.get(1)[0]), this.sy(a.pts.get(1)[1]), a.argb, th);
         case ARROW -> this.drawArrow(ctx, a, th);
         case RECT -> this.drawRect(ctx, a, th);
         case ELLIPSE -> this.drawEllipse(ctx, a, th);
         case TEXT -> this.drawText(ctx, a);
         default -> {
         }
      }
   }

   private void drawPolyline(class_332 ctx, Annotation a, int argb, int th) {
      if (a.pts.size() == 1) {
         double x = this.sx(a.pts.get(0)[0]), y = this.sy(a.pts.get(0)[1]);
         this.dot(ctx, x, y, argb, th);
         return;
      }
      for (int i = 1; i < a.pts.size(); i++) {
         this.thickLine(ctx, this.sx(a.pts.get(i - 1)[0]), this.sy(a.pts.get(i - 1)[1]), this.sx(a.pts.get(i)[0]), this.sy(a.pts.get(i)[1]), argb, th);
      }
   }

   private void drawArrow(class_332 ctx, Annotation a, int th) {
      double x1 = this.sx(a.pts.get(0)[0]), y1 = this.sy(a.pts.get(0)[1]);
      double x2 = this.sx(a.pts.get(1)[0]), y2 = this.sy(a.pts.get(1)[1]);
      this.thickLine(ctx, x1, y1, x2, y2, a.argb, th);
      double ang = Math.atan2(y2 - y1, x2 - x1);
      double head = Math.max(8, th * 3.5);
      this.thickLine(ctx, x2, y2, x2 + head * Math.cos(ang + Math.toRadians(160)), y2 + head * Math.sin(ang + Math.toRadians(160)), a.argb, th);
      this.thickLine(ctx, x2, y2, x2 + head * Math.cos(ang - Math.toRadians(160)), y2 + head * Math.sin(ang - Math.toRadians(160)), a.argb, th);
   }

   private void drawRect(class_332 ctx, Annotation a, int th) {
      double x1 = this.sx(Math.min(a.pts.get(0)[0], a.pts.get(1)[0])), y1 = this.sy(Math.min(a.pts.get(0)[1], a.pts.get(1)[1]));
      double x2 = this.sx(Math.max(a.pts.get(0)[0], a.pts.get(1)[0])), y2 = this.sy(Math.max(a.pts.get(0)[1], a.pts.get(1)[1]));
      this.thickLine(ctx, x1, y1, x2, y1, a.argb, th);
      this.thickLine(ctx, x1, y2, x2, y2, a.argb, th);
      this.thickLine(ctx, x1, y1, x1, y2, a.argb, th);
      this.thickLine(ctx, x2, y1, x2, y2, a.argb, th);
   }

   private void drawEllipse(class_332 ctx, Annotation a, int th) {
      double cx = this.sx((a.pts.get(0)[0] + a.pts.get(1)[0]) / 2), cy = this.sy((a.pts.get(0)[1] + a.pts.get(1)[1]) / 2);
      double rxp = Math.abs(this.sx(a.pts.get(1)[0]) - this.sx(a.pts.get(0)[0])) / 2, ryp = Math.abs(this.sy(a.pts.get(1)[1]) - this.sy(a.pts.get(0)[1])) / 2;
      int seg = 48;
      double px = cx + rxp, py = cy;
      for (int i = 1; i <= seg; i++) {
         double ang = i / (double) seg * Math.PI * 2;
         double nx = cx + rxp * Math.cos(ang), ny = cy + ryp * Math.sin(ang);
         this.thickLine(ctx, px, py, nx, ny, a.argb, th);
         px = nx;
         py = ny;
      }
   }

   private void drawText(class_332 ctx, Annotation a) {
      double x = this.sx(a.pts.get(0)[0]), y = this.sy(a.pts.get(0)[1]);
      double scale = EditorRasterizer.fontPx(a.size) * this.sp / 8.0;
      if (scale <= 0) {
         return;
      }
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate((float) x, (float) y);
      ctx.method_51448().scale((float) scale, (float) scale);
      String[] lines = a.text.split("\n", -1);
      int ly = 0;
      for (String line : lines) {
         ctx.method_51433(this.field_22793, line, 0, ly, a.argb | 0xFF000000, true);
         ly += 10;
      }
      ctx.method_51448().popMatrix();
   }

   private void thickLine(class_332 ctx, double x1, double y1, double x2, double y2, int argb, int th) {
      double dx = x2 - x1, dy = y2 - y1;
      double dist = Math.max(1, Math.hypot(dx, dy));
      int steps = (int) Math.ceil(dist);
      for (int i = 0; i <= steps; i++) {
         double t = i / (double) steps;
         this.dot(ctx, x1 + dx * t, y1 + dy * t, argb, th);
      }
   }

   private void dot(class_332 ctx, double cx, double cy, int argb, int th) {
      int r = Math.max(1, th) / 2;
      int x = (int) Math.round(cx) - r, y = (int) Math.round(cy) - r;
      ctx.method_25294(x, y, x + Math.max(1, th), y + Math.max(1, th), argb);
   }

   private void renderToolRail(class_332 ctx, int mouseX, int mouseY) {
      int x = 10;
      int y = 40;
      this.hoverTool = null;
      TurtUIUtils.drawRoundedRect(ctx, x - 2, y - 4, RAIL_W, Tool.values().length * (RAIL_BTN + RAIL_GAP) + 6, 5, Palette.alpha(Palette.PANEL_BG, 235));
      for (Tool tv : Tool.values()) {
         int i = tv.ordinal();
         int by = y + i * (RAIL_BTN + RAIL_GAP);
         this.railX[i] = x;
         this.railY[i] = by;
         boolean sel = this.tool == tv;
         boolean hov = mouseX >= x && mouseX <= x + RAIL_BTN && mouseY >= by && mouseY <= by + RAIL_BTN;
         if (hov) {
            this.hoverTool = tv;
         }
         // Selected = solid green fill; hovered = green-tinted; idle = plain. Selected also gets a bright
         // outer ring so the active tool is unmistakable.
         ctx.method_25294(x, by, x + RAIL_BTN, by + RAIL_BTN, (sel ? Palette.GREEN : (hov ? Palette.BTN_HOVER : Palette.BTN_BG)).getRGB());
         ctx.method_73198(x, by, RAIL_BTN, RAIL_BTN, (sel ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
         if (sel) {
            ctx.method_73198(x - 1, by - 1, RAIL_BTN + 2, RAIL_BTN + 2, Palette.GREEN.getRGB());
         }
         this.drawToolIcon(ctx, tv, x, by, (sel ? Palette.PANEL_BG : (hov ? Palette.GREEN : Palette.TEXT)).getRGB());
      }
   }

   /** Floating tooltip for the hovered tool: bold green name + a short "how it works" line. */
   private void renderToolTooltip(class_332 ctx, int mouseX, int mouseY) {
      if (this.hoverTool == null) {
         return;
      }
      String name = toolName(this.hoverTool);
      String help = toolHelp(this.hoverTool);
      int wName = this.field_22793.method_1727(name);
      int wHelp = this.field_22793.method_1727(help);
      int boxW = Math.max(wName, wHelp) + 14;
      int boxH = 30;
      int bx = mouseX + 14;
      int by = mouseY + 6;
      if (bx + boxW > this.field_22789 - 4) {
         bx = mouseX - 14 - boxW;
      }
      if (by + boxH > this.field_22790 - 4) {
         by = this.field_22790 - 4 - boxH;
      }
      TurtUIUtils.drawRoundedRect(ctx, bx, by, boxW, boxH, 4, new Color(11, 13, 20, 235));
      TurtUIUtils.drawRoundedBorder(ctx, bx, by, boxW, boxH, 4, Palette.alpha(Palette.GREEN, 150));
      ctx.method_51433(this.field_22793, name, bx + 7, by + 6, Palette.GREEN.getRGB(), false);
      ctx.method_51433(this.field_22793, help, bx + 7, by + 18, Palette.TEXT_MUTED.getRGB(), false);
   }

   private static String toolName(Tool t) {
      return switch (t) {
         case PAN -> "Pan  (Space)";
         case MOVE -> "Move  (V)";
         case ERASER -> "Eraser  (D)";
         case CROP -> "Crop  (C)";
         case PEN -> "Pen  (P)";
         case HIGHLIGHTER -> "Highlighter  (H)";
         case LINE -> "Line  (L)";
         case ARROW -> "Arrow  (A)";
         case RECT -> "Rectangle  (R)";
         case ELLIPSE -> "Ellipse  (E)";
         case TEXT -> "Text  (T)";
         case BLUR -> "Blur  (B)";
         case PIXELATE -> "Pixelate  (X)";
      };
   }

   private static String toolHelp(Tool t) {
      return switch (t) {
         case PAN -> "Drag to move the image around the canvas.";
         case MOVE -> "Click a mark and drag to reposition it.";
         case ERASER -> "Click a mark to delete it.";
         case CROP -> "Drag a box, then confirm to trim the image.";
         case PEN -> "Freehand draw in the current colour.";
         case HIGHLIGHTER -> "Draw a soft translucent highlight.";
         case LINE -> "Drag to draw a straight line.";
         case ARROW -> "Drag to draw an arrow.";
         case RECT -> "Drag a rectangle (toggle Fill below).";
         case ELLIPSE -> "Drag an ellipse (toggle Fill below).";
         case TEXT -> "Click, then type. Enter to commit.";
         case BLUR -> "Drag over an area to blur it out.";
         case PIXELATE -> "Drag over an area to pixelate it.";
      };
   }

   private void renderBottomBar(class_332 ctx, int mouseX, int mouseY) {
      int y = this.field_22790 - 30;
      this.barY = y;
      int x = this.canvasX;
      this.swY = y;
      for (int i = 0; i < this.swatchCount(); i++) {
         this.swX[i] = x;
         boolean custom = i == SWATCHES.length;
         ctx.method_25294(x, y, x + SW, y + SW, 0xFF000000 | (this.swatchColor(i) & 0xFFFFFF));
         ctx.method_73198(x, y, SW, SW, (this.colorIdx == i ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
         if (this.colorIdx == i) {
            ctx.method_73198(x - 1, y - 1, SW + 2, SW + 2, Palette.GREEN.getRGB());
         }
         if (custom) {
            // little corner notch marks this as the editable one
            ctx.method_25294(x + SW - 5, y + SW - 5, x + SW - 1, y + SW - 1, 0xFFFFFFFF);
            ctx.method_25294(x + SW - 4, y + SW - 4, x + SW - 2, y + SW - 2, 0xFF000000 | (this.customColor() & 0xFFFFFF));
         }
         x += SW + SWG;
      }
      if (this.pickerOpen) {
         this.renderColorPicker(ctx, mouseX, mouseY);
      }
      x += 8;
      this.sizeBarX = x;
      this.sizeBarY = y;
      for (int i = 0; i < SIZES.length; i++) {
         int bx = x + i * (18 + 3);
         boolean sel = this.sizeIdx == i;
         ctx.method_25294(bx, y, bx + 18, y + SW, (sel ? Palette.GREEN : Palette.BTN_BG).getRGB());
         ctx.method_73198(bx, y, 18, SW, (sel ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
         ctx.method_25300(this.field_22793, SIZE_LABELS[i], bx + 9, y + 4, (sel ? Palette.PANEL_BG : Palette.TEXT).getRGB());
      }
      // Fill toggle + opacity cycle.
      this.fillBtnX = x + SIZES.length * (18 + 3) + 8;
      boolean fillHov = mouseX >= this.fillBtnX && mouseX <= this.fillBtnX + 30 && mouseY >= y && mouseY <= y + SW;
      ctx.method_25294(this.fillBtnX, y, this.fillBtnX + 30, y + SW, (this.fillShapes ? Palette.GREEN : (fillHov ? Palette.BTN_HOVER : Palette.BTN_BG)).getRGB());
      ctx.method_73198(this.fillBtnX, y, 30, SW, (this.fillShapes ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
      ctx.method_25300(this.field_22793, "Fill", this.fillBtnX + 15, y + 4, (this.fillShapes ? Palette.PANEL_BG : Palette.TEXT).getRGB());

      this.opacityBtnX = this.fillBtnX + 34;
      boolean opHov = mouseX >= this.opacityBtnX && mouseX <= this.opacityBtnX + 34 && mouseY >= y && mouseY <= y + SW;
      ctx.method_25294(this.opacityBtnX, y, this.opacityBtnX + 34, y + SW, (opHov ? Palette.BTN_HOVER : Palette.BTN_BG).getRGB());
      ctx.method_73198(this.opacityBtnX, y, 34, SW, Palette.PANEL_BORDER.getRGB());
      ctx.method_25300(this.field_22793, this.alphaPct + "%", this.opacityBtnX + 17, y + 4, Palette.TEXT.getRGB());

      // Undo / redo at the right end of the bar.
      this.redoX = this.field_22789 - 12 - 26;
      this.undoX = this.redoX - 26 - 6;
      this.drawIconButton(ctx, this.undoX, y, mouseX, mouseY, "↶", !this.undo.isEmpty());
      this.drawIconButton(ctx, this.redoX, y, mouseX, mouseY, "↷", !this.redo.isEmpty());
   }

   /** HSV picker: a saturation/value square with a hue strip under it, above the custom swatch. */
   private void renderColorPicker(class_332 ctx, int mouseX, int mouseY) {
      int px = this.swX[SWATCHES.length] - PICK_W + SW;
      int py = this.swY - PICK_H - HUE_H - 12;
      px = Math.max(this.canvasX, Math.min(px, this.field_22789 - PICK_W - 8));
      py = Math.max(this.canvasY, py);
      this.pickerX = px;
      this.pickerY = py;

      TurtUIUtils.drawRoundedRect(ctx, px - 4, py - 4, PICK_W + 8, PICK_H + HUE_H + 14, 4, Palette.alpha(Palette.PANEL_BG, 245));
      TurtUIUtils.drawRoundedBorder(ctx, px - 4, py - 4, PICK_W + 8, PICK_H + HUE_H + 14, 4, Palette.alpha(Palette.PANEL_BORDER, 255));

      // Saturation (x) / value (y) square for the current hue, drawn in 2px cells to stay cheap.
      for (int sxp = 0; sxp < PICK_W; sxp += 2) {
         float sat = (float) sxp / (PICK_W - 1);
         for (int syp = 0; syp < PICK_H; syp += 2) {
            float val = 1f - (float) syp / (PICK_H - 1);
            int rgb = 0xFF000000 | (Color.HSBtoRGB(this.pickH, sat, val) & 0xFFFFFF);
            ctx.method_25294(px + sxp, py + syp, px + sxp + 2, py + syp + 2, rgb);
         }
      }
      // Current SV marker.
      int mxp = px + Math.round(this.pickS * (PICK_W - 1));
      int myp = py + Math.round((1f - this.pickV) * (PICK_H - 1));
      ctx.method_73198(mxp - 2, myp - 2, 5, 5, 0xFFFFFFFF);

      // Hue strip.
      int hy = py + PICK_H + 4;
      for (int i = 0; i < PICK_W; i++) {
         int rgb = 0xFF000000 | (Color.HSBtoRGB((float) i / (PICK_W - 1), 1f, 1f) & 0xFFFFFF);
         ctx.method_25294(px + i, hy, px + i + 1, hy + HUE_H, rgb);
      }
      int hxp = px + Math.round(this.pickH * (PICK_W - 1));
      ctx.method_73198(hxp - 1, hy - 1, 3, HUE_H + 2, 0xFFFFFFFF);
   }

   /** Route a click inside the open picker to the SV square or the hue strip. */
   private boolean handlePickerClick(double mx, double my) {
      int px = this.pickerX, py = this.pickerY;
      if (mx >= px && mx < px + PICK_W && my >= py && my < py + PICK_H) {
         this.pickS = (float) Math.max(0, Math.min(1, (mx - px) / (PICK_W - 1)));
         this.pickV = 1f - (float) Math.max(0, Math.min(1, (my - py) / (PICK_H - 1)));
         this.colorIdx = SWATCHES.length;
         if (this.editingText != null) {
            this.editingText.argb = this.currentColor();
         }
         return true;
      }
      int hy = py + PICK_H + 4;
      if (mx >= px && mx < px + PICK_W && my >= hy && my < hy + HUE_H) {
         this.pickH = (float) Math.max(0, Math.min(1, (mx - px) / (PICK_W - 1)));
         this.colorIdx = SWATCHES.length;
         if (this.editingText != null) {
            this.editingText.argb = this.currentColor();
         }
         return true;
      }
      // Clicking anywhere else inside the panel keeps it open without doing anything.
      return mx >= px - 4 && mx <= px + PICK_W + 4 && my >= py - 4 && my <= py + PICK_H + HUE_H + 10;
   }

   private void drawIconButton(class_332 ctx, int x, int y, int mouseX, int mouseY, String glyph, boolean enabled) {
      boolean hov = enabled && mouseX >= x && mouseX <= x + 26 && mouseY >= y && mouseY <= y + 18;
      ctx.method_25294(x, y, x + 26, y + 18, (hov ? Palette.BTN_HOVER : Palette.BTN_BG).getRGB());
      ctx.method_73198(x, y, 26, 18, Palette.PANEL_BORDER.getRGB());
      ctx.method_25300(this.field_22793, glyph, x + 13, y + 5, (enabled ? Palette.TEXT : Palette.TEXT_MUTED).getRGB());
   }

   /** Small primitive glyphs for each tool, drawn inside a 22px button at (bx,by). */
   private void drawToolIcon(class_332 ctx, Tool tv, int bx, int by, int c) {
      int x = bx + 5, y = by + 5; // 12x12 icon field
      switch (tv) {
         case PAN -> {
            ctx.method_25294(x + 5, y, x + 7, y + 12, c);
            ctx.method_25294(x, y + 5, x + 12, y + 7, c);
         }
         case MOVE -> {
            // four-way arrow
            ctx.method_25294(x + 5, y + 1, x + 7, y + 11, c);
            ctx.method_25294(x + 1, y + 5, x + 11, y + 7, c);
            ctx.method_25294(x + 4, y + 2, x + 8, y + 3, c);
            ctx.method_25294(x + 4, y + 9, x + 8, y + 10, c);
            ctx.method_25294(x + 2, y + 4, x + 3, y + 8, c);
            ctx.method_25294(x + 9, y + 4, x + 10, y + 8, c);
         }
         case ERASER -> {
            // A slanted eraser block wiping across a base line.
            for (int i = 0; i < 6; i++) {
               ctx.method_25294(x + 1 + i, y + 8 - i, x + 8 + i, y + 12 - i, c);
            }
            ctx.method_25294(x + 1, y + 11, x + 11, y + 12, c); // surface line
         }
         case CROP -> {
            ctx.method_25294(x + 2, y, x + 3, y + 12, c);
            ctx.method_25294(x + 9, y, x + 10, y + 12, c);
            ctx.method_25294(x, y + 2, x + 12, y + 3, c);
            ctx.method_25294(x, y + 9, x + 12, y + 10, c);
         }
         case PEN -> {
            for (int i = 0; i < 9; i++) {
               ctx.method_25294(x + i, y + 9 - i, x + i + 2, y + 11 - i, c);
            }
            ctx.method_25294(x, y + 9, x + 3, y + 12, c);
         }
         case HIGHLIGHTER -> {
            ctx.method_25294(x + 3, y, x + 9, y + 7, c);
            ctx.method_25294(x + 2, y + 7, x + 10, y + 10, c);
            ctx.method_25294(x + 2, y + 11, x + 10, y + 12, c);
         }
         case LINE -> {
            for (int i = 0; i < 12; i++) {
               ctx.method_25294(x + i, y + 11 - i, x + i + 1, y + 12 - i, c);
            }
         }
         case ARROW -> {
            for (int i = 0; i < 12; i++) {
               ctx.method_25294(x + i, y + 11 - i, x + i + 1, y + 12 - i, c);
            }
            ctx.method_25294(x + 7, y, x + 12, y + 2, c);
            ctx.method_25294(x + 10, y, x + 12, y + 5, c);
         }
         case RECT -> ctx.method_73198(x, y + 1, 12, 10, c);
         case ELLIPSE -> {
            ctx.method_25294(x + 3, y, x + 9, y + 1, c);
            ctx.method_25294(x + 3, y + 11, x + 9, y + 12, c);
            ctx.method_25294(x, y + 3, x + 1, y + 9, c);
            ctx.method_25294(x + 11, y + 3, x + 12, y + 9, c);
         }
         case TEXT -> {
            ctx.method_25294(x, y, x + 12, y + 2, c);
            ctx.method_25294(x + 5, y, x + 7, y + 12, c);
         }
         case BLUR -> {
            // soft concentric blocks
            ctx.method_25294(x + 2, y + 2, x + 10, y + 10, c);
            ctx.method_25294(x + 4, y + 4, x + 8, y + 8, 0xFF101216);
         }
         case PIXELATE -> {
            for (int gx = 0; gx < 3; gx++) {
               for (int gy = 0; gy < 3; gy++) {
                  if ((gx + gy) % 2 == 0) {
                     ctx.method_25294(x + gx * 4, y + gy * 4, x + gx * 4 + 4, y + gy * 4 + 4, c);
                  }
               }
            }
         }
         default -> {
         }
      }
   }
}
