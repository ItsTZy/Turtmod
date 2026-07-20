package com.turtmod.cosmetics;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10055;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_12079;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7920;
import net.minecraft.class_8685;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A full skin editor: paint straight onto the rotating 3D model or onto the flat 64x64 sheet, with the
 * usual pixel-art toolset (pencil, eraser, bucket, eyedropper, lighten, darken, noise), brush sizes,
 * base/overlay layer masking, mirroring, a colour palette with an HSV picker, undo/redo, and save /
 * apply-to-account / import / fetch-by-username.
 *
 * <p>All pixel work lives in {@link SkinCanvas} and all geometry/UV/picking in {@link SkinModel}, so this
 * class is only the screen: layout, input routing and drawing.
 */
public class SkinEditorScreen extends class_437 {
   private final class_437 parent;
   private final SkinCanvas canvas = new SkinCanvas();
   private final List<TurtUIButton> buttons = new ArrayList<>();

   // Live texture of the working image, re-uploaded whenever pixels change.
   private class_2960 texId;
   private int texVersion, uploadedVersion = -1;

   // Tools / brush
   private SkinCanvas.Tool tool = SkinCanvas.Tool.PENCIL;
   private int brush = 1;
   private SkinCanvas.LayerMask layerMask = SkinCanvas.LayerMask.BOTH;
   private boolean mirrorX;
   private boolean showGrid = true;
   private boolean slim;
   private boolean showOverlay = true;

   // Colours
   private static final int[] SWATCHES = {
      0xFF000000, 0xFFFFFFFF, 0xFF9AA0A6, 0xFFB3541E, 0xFFE23B3B, 0xFFF08A24,
      0xFFF5D523, 0xFF57E08A, 0xFF4AA3FF, 0xFFB07BFF, 0xFFFF9DAE, 0xFF5B3A1E
   };
   private int colorIdx = 0;
   private float pickH = 0.08f, pickS = 0.7f, pickV = 0.55f;
   private boolean pickerOpen;
   private int pickerX, pickerY;
   private static final int PICK_W = 104, PICK_H = 56, HUE_H = 9;

   // 3D preview
   private float previewYaw = 0.5f, previewPitch = 0f;
   private int viewX, viewY, viewW, viewH;
   private boolean rotating;

   // 2D sheet
   private int sheetX, sheetY, sheetScale = 4;
   private boolean paintingSheet, painting3d;

   // Username fetch
   private String ignInput = "";
   private boolean ignFocused;
   private int ignX, ignY, ignW = 110, ignH = 16;

   private String status = "";
   private int statusTicks;

   // hit rects
   private final int[] toolY = new int[SkinCanvas.Tool.values().length];
   private final int[] swX = new int[SWATCHES.length + 1];
   private int railX, swY, brushBarX, brushBarY, optY, mirrorX0, layerX0, gridX0, undoX0, redoX0;

   private static final int RAIL_BTN = 20, RAIL_GAP = 3, SW = 14, SWG = 4;

   public SkinEditorScreen(class_437 parent, File initial) {
      super(class_2561.method_43470("Skin Editor"));
      this.parent = parent;
      if (initial != null && initial.isFile()) {
         this.canvas.load(initial);
      }
      this.texVersion = 1;
   }

   protected void method_25426() {
      this.buttons.clear();
      TurtUITheme t = new TurtUITheme(Palette.BTN_BG, Palette.PANEL_BORDER, Palette.TEXT, Palette.BTN_HOVER, Palette.GREEN);
      int bw = 62, bh = 18, gap = 5, y = 8;
      int x = this.field_22789 - 10 - bw;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Close", t, this::onClose2));
      x -= bw + gap;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Apply", t, this::onApply));
      x -= bw + gap;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Save", t, this::onSave));
      x -= bw + gap;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Import", t, this::onImport));
      x -= bw + gap;
      this.buttons.add(new TurtUIButton(x, y, bw, bh, "Clear", t, () -> {
         this.canvas.clear();
         this.dirty();
      }));
   }

   private void onClose2() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   public void method_25419() {
      this.onClose2();
   }

   private void dirty() {
      this.texVersion++;
   }

   private void setStatus(String s) {
      this.status = s;
      this.statusTicks = 100;
   }

   private int currentColor() {
      return this.colorIdx < SWATCHES.length ? SWATCHES[this.colorIdx] : this.customColor();
   }

   private int customColor() {
      return 0xFF000000 | (Color.HSBtoRGB(this.pickH, this.pickS, this.pickV) & 0xFFFFFF);
   }

   // ── texture upload ────────────────────────────────────────────────────────
   private void ensureTexture() {
      if (this.uploadedVersion == this.texVersion) {
         return;
      }
      this.uploadedVersion = this.texVersion;
      try {
         class_1011 img = new class_1011(SkinCanvas.SIZE, SkinCanvas.SIZE, false);
         for (int y = 0; y < SkinCanvas.SIZE; y++) {
            for (int x = 0; x < SkinCanvas.SIZE; x++) {
               img.method_61941(x, y, this.canvas.image().getRGB(x, y));
            }
         }
         String key = "skin_editor/live";
         this.texId = class_2960.method_60655("turtmod", key);
         class_310.method_1551().method_1531().method_4616(this.texId, new class_1043(() -> "turtmod:" + key, img));
      } catch (Exception e) {
         this.texId = null;
      }
   }

   // ── actions ───────────────────────────────────────────────────────────────
   private void onSave() {
      try {
         File dir = CosmeticManager.getSkinsDirectory().toFile();
         if (!dir.exists()) {
            dir.mkdirs();
         }
         File out = new File(dir, "edited-" + System.currentTimeMillis() + ".png");
         this.setStatus(this.canvas.save(out) ? "Saved " + out.getName() : "Save failed.");
      } catch (Exception e) {
         this.setStatus("Save failed: " + e.getMessage());
      }
   }

   private void onApply() {
      try {
         File dir = CosmeticManager.getSkinsDirectory().toFile();
         if (!dir.exists()) {
            dir.mkdirs();
         }
         File tmp = new File(dir, "applied-skin.png");
         if (!this.canvas.save(tmp)) {
            this.setStatus("Could not write the skin file.");
            return;
         }
         this.setStatus("Uploading to your profile...");
         final boolean slimVariant = this.slim;
         new Thread(() -> {
            CosmeticManager.UploadResult r = CosmeticManager.uploadSkinToMinecraftProfile(tmp.getAbsolutePath(), slimVariant);
            class_310.method_1551().execute(() -> this.setStatus(r.success() ? "Skin applied!" : r.message()));
         }, "turtmod-skin-apply").start();
      } catch (Exception e) {
         this.setStatus("Apply failed: " + e.getMessage());
      }
   }

   private void onImport() {
      new Thread(() -> {
         try {
            String picked = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
               "Import skin", "", null, "PNG image", false);
            if (picked != null) {
               File f = new File(picked);
               class_310.method_1551().execute(() -> {
                  if (this.canvas.load(f)) {
                     this.dirty();
                     this.setStatus("Imported " + f.getName());
                  } else {
                     this.setStatus("Could not read that image.");
                  }
               });
            }
         } catch (Exception e) {
            class_310.method_1551().execute(() -> this.setStatus("Import failed."));
         }
      }, "turtmod-skin-import").start();
   }

   /** Look a player up by name and load their current skin into the canvas. */
   private void fetchByName(String name) {
      if (name == null || name.trim().isEmpty()) {
         this.setStatus("Type a username first.");
         return;
      }
      this.setStatus("Looking up " + name + "...");
      new Thread(() -> {
         byte[] png = SkinFetcher.fetchSkin(name.trim());
         class_310.method_1551().execute(() -> {
            if (png != null && this.canvas.load(png)) {
               this.dirty();
               this.setStatus("Loaded " + name + "'s skin.");
            } else {
               this.setStatus("Could not fetch that player's skin.");
            }
         });
      }, "turtmod-skin-fetch").start();
   }

   // ── painting ──────────────────────────────────────────────────────────────
   private void paintAt(int tx, int ty, boolean sampleOnly) {
      if (tx < 0 || ty < 0 || tx >= SkinCanvas.SIZE || ty >= SkinCanvas.SIZE) {
         return;
      }
      if (this.tool == SkinCanvas.Tool.PICKER || sampleOnly) {
         int c = this.canvas.get(tx, ty);
         if ((c >>> 24) != 0) {
            float[] hsb = Color.RGBtoHSB((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, null);
            this.pickH = hsb[0];
            this.pickS = hsb[1];
            this.pickV = hsb[2];
            this.colorIdx = SWATCHES.length;
            this.setStatus("Picked colour.");
         }
         return;
      }
      this.canvas.beginStroke();
      this.canvas.apply(this.tool, tx, ty, this.currentColor(), this.brush, this.mirrorX, this.layerMask);
      this.dirty();
   }

   /** Map a click inside the 3D viewport to a skin texel via the model raycast. */
   private void paint3d(double mx, double my) {
      double unitsPerPx = 16.0 / Math.max(1, this.modelScale());
      double vx = (mx - (this.viewX + this.viewW / 2.0)) * unitsPerPx;
      double vy = ((this.viewY + this.viewH / 2.0) - my) * unitsPerPx;
      SkinModel.Hit hit = SkinModel.pick(vx, vy, this.previewYaw, this.previewPitch, this.slim,
         this.layerMask == SkinCanvas.LayerMask.OVERLAY);
      if (hit == null && this.layerMask != SkinCanvas.LayerMask.OVERLAY) {
         return;
      }
      if (hit != null) {
         this.paintAt(hit.texelX, hit.texelY, false);
      }
   }

   private int modelScale() {
      return Math.max(10, this.viewH / 3);
   }

   // ── input ─────────────────────────────────────────────────────────────────
   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = click.comp_4798(), my = click.comp_4799();
      int button = click.method_74245();
      for (TurtUIButton b : this.buttons) {
         if (b.mouseClicked(mx, my, button)) {
            return true;
         }
      }
      if (button == 0) {
         if (this.pickerOpen && this.handlePickerClick(mx, my)) {
            return true;
         }
         // Tool rail
         for (SkinCanvas.Tool tv : SkinCanvas.Tool.values()) {
            int i = tv.ordinal();
            if (mx >= this.railX && mx <= this.railX + RAIL_BTN && my >= this.toolY[i] && my <= this.toolY[i] + RAIL_BTN) {
               this.tool = tv;
               this.pickerOpen = false;
               this.setStatus(tv.name().charAt(0) + tv.name().substring(1).toLowerCase());
               return true;
            }
         }
         // Swatches
         for (int i = 0; i < SWATCHES.length + 1; i++) {
            if (mx >= this.swX[i] && mx <= this.swX[i] + SW && my >= this.swY && my <= this.swY + SW) {
               this.pickerOpen = i == SWATCHES.length && (this.colorIdx != i || !this.pickerOpen);
               this.colorIdx = i;
               return true;
            }
         }
         // Brush sizes 1..8
         for (int i = 0; i < 8; i++) {
            int bx = this.brushBarX + i * 15;
            if (mx >= bx && mx <= bx + 13 && my >= this.brushBarY && my <= this.brushBarY + 13) {
               this.brush = i + 1;
               return true;
            }
         }
         // Option chips
         if (my >= this.optY && my <= this.optY + 14) {
            if (mx >= this.mirrorX0 && mx <= this.mirrorX0 + 48) {
               this.mirrorX = !this.mirrorX;
               return true;
            }
            if (mx >= this.layerX0 && mx <= this.layerX0 + 74) {
               SkinCanvas.LayerMask[] v = SkinCanvas.LayerMask.values();
               this.layerMask = v[(this.layerMask.ordinal() + 1) % v.length];
               return true;
            }
            if (mx >= this.gridX0 && mx <= this.gridX0 + 40) {
               this.showGrid = !this.showGrid;
               return true;
            }
            if (mx >= this.undoX0 && mx <= this.undoX0 + 30) {
               this.canvas.undo();
               this.dirty();
               return true;
            }
            if (mx >= this.redoX0 && mx <= this.redoX0 + 30) {
               this.canvas.redo();
               this.dirty();
               return true;
            }
         }
         // Username field + fetch
         if (mx >= this.ignX && mx <= this.ignX + this.ignW && my >= this.ignY && my <= this.ignY + this.ignH) {
            this.ignFocused = true;
            return true;
         }
         if (mx >= this.ignX + this.ignW + 4 && mx <= this.ignX + this.ignW + 52 && my >= this.ignY && my <= this.ignY + this.ignH) {
            this.fetchByName(this.ignInput);
            return true;
         }
         this.ignFocused = false;

         // Canvases
         if (this.inSheet(mx, my)) {
            this.paintingSheet = true;
            int[] t = this.sheetTexel(mx, my);
            this.paintAt(t[0], t[1], false);
            return true;
         }
         if (this.inView(mx, my)) {
            this.painting3d = true;
            this.paint3d(mx, my);
            return true;
         }
      }
      if (button == 1 && this.inView(mx, my)) {
         this.rotating = true;
         return true;
      }
      return super.method_25402(click, bl);
   }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      double mx = click.comp_4798(), my = click.comp_4799();
      if (this.pickerOpen && this.handlePickerClick(mx, my)) {
         return true;
      }
      if (this.rotating) {
         this.previewYaw += (float) dx * 0.02f;
         this.previewPitch = Math.max(-1.2f, Math.min(1.2f, this.previewPitch + (float) dy * 0.02f));
         return true;
      }
      if (this.paintingSheet && this.inSheet(mx, my)) {
         int[] t = this.sheetTexel(mx, my);
         this.paintAt(t[0], t[1], false);
         return true;
      }
      if (this.painting3d && this.inView(mx, my)) {
         this.paint3d(mx, my);
         return true;
      }
      return super.method_25403(click, dx, dy);
   }

   public boolean method_25406(class_11909 click) {
      this.rotating = false;
      if (this.paintingSheet || this.painting3d) {
         this.paintingSheet = false;
         this.painting3d = false;
         this.canvas.endStroke();
         return true;
      }
      return super.method_25406(click);
   }

   public boolean method_25401(double mx, double my, double h, double v) {
      if (this.inSheet(mx, my)) {
         this.sheetScale = Math.max(2, Math.min(9, this.sheetScale + (v > 0 ? 1 : -1)));
         return true;
      }
      if (this.inView(mx, my)) {
         this.brush = Math.max(1, Math.min(8, this.brush + (v > 0 ? 1 : -1)));
         return true;
      }
      return super.method_25401(mx, my, h, v);
   }

   public boolean method_25400(class_11905 event) {
      if (this.ignFocused) {
         String s = event.method_74226();
         if (s != null && !s.isEmpty() && this.ignInput.length() < 16) {
            this.ignInput += s;
            return true;
         }
      }
      return super.method_25400(event);
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (this.ignFocused) {
         if (key == 259 && !this.ignInput.isEmpty()) {
            this.ignInput = this.ignInput.substring(0, this.ignInput.length() - 1);
            return true;
         }
         if (key == 257) {
            this.fetchByName(this.ignInput);
            this.ignFocused = false;
            return true;
         }
         if (key == 256) {
            this.ignFocused = false;
            return true;
         }
      }
      SkinCanvas.Tool sc = switch (key) {
         case 80 -> SkinCanvas.Tool.PENCIL;   // P
         case 69 -> SkinCanvas.Tool.ERASER;   // E
         case 66 -> SkinCanvas.Tool.BUCKET;   // B
         case 73 -> SkinCanvas.Tool.PICKER;   // I
         case 76 -> SkinCanvas.Tool.LIGHTEN;  // L
         case 75 -> SkinCanvas.Tool.DARKEN;   // K
         case 78 -> SkinCanvas.Tool.NOISE;    // N
         default -> null;
      };
      if (sc != null) {
         this.tool = sc;
         return true;
      }
      if (key == 90) {   // Z undo
         this.canvas.undo();
         this.dirty();
         return true;
      }
      if (key == 89) {   // Y redo
         this.canvas.redo();
         this.dirty();
         return true;
      }
      if (key == 256) {
         this.onClose2();
         return true;
      }
      return super.method_25404(input);
   }

   private boolean inView(double mx, double my) {
      return mx >= this.viewX && mx <= this.viewX + this.viewW && my >= this.viewY && my <= this.viewY + this.viewH;
   }

   private boolean inSheet(double mx, double my) {
      int s = SkinCanvas.SIZE * this.sheetScale;
      return mx >= this.sheetX && mx < this.sheetX + s && my >= this.sheetY && my < this.sheetY + s;
   }

   private int[] sheetTexel(double mx, double my) {
      return new int[]{(int) ((mx - this.sheetX) / this.sheetScale), (int) ((my - this.sheetY) / this.sheetScale)};
   }

   // ── render ────────────────────────────────────────────────────────────────
   public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, 0xE6121316);
      ctx.method_51433(this.field_22793, "SKIN EDITOR", 12, 12, Palette.GREEN.getRGB(), false);

      this.ensureTexture();

      // Layout
      this.railX = 10;
      int contentTop = 34;
      int bottomBar = this.field_22790 - 62;
      this.viewX = 40;
      this.viewY = contentTop;
      this.viewW = Math.max(120, (this.field_22789 - 60) / 2 - 20);
      this.viewH = bottomBar - contentTop - 6;

      int sheetPx = SkinCanvas.SIZE * this.sheetScale;
      this.sheetX = this.viewX + this.viewW + 24;
      this.sheetY = contentTop + Math.max(0, (this.viewH - sheetPx) / 2);

      this.renderToolRail(ctx, mouseX, mouseY);
      this.render3d(ctx);
      this.renderSheet(ctx, mouseX, mouseY);
      this.renderBottomBar(ctx, mouseX, mouseY, bottomBar);

      for (TurtUIButton b : this.buttons) {
         b.render(ctx, mouseX, mouseY, this.field_22793);
      }
      if (this.statusTicks > 0) {
         ctx.method_25300(this.field_22793, this.status, this.field_22789 / 2, this.field_22790 - 12, 0xFFB9F5C4);
         this.statusTicks--;
      }
      super.method_25394(ctx, mouseX, mouseY, delta);
   }

   private void render3d(class_332 ctx) {
      TurtUIUtils.drawRoundedRect(ctx, this.viewX - 4, this.viewY - 4, this.viewW + 8, this.viewH + 8, 5,
         Palette.alpha(Palette.PANEL_BG, 235));
      TurtUIUtils.drawRoundedBorder(ctx, this.viewX - 4, this.viewY - 4, this.viewW + 8, this.viewH + 8, 5,
         Palette.alpha(Palette.PANEL_BORDER, 255));
      if (this.texId == null) {
         return;
      }
      class_10055 state = new class_10055();
      state.field_53520 = new class_8685(new class_12079.class_12080(this.texId, "turtmod_skin_edit"),
         null, null, this.slim ? class_7920.field_41122 : class_7920.field_41123, true);
      state.field_53329 = 0.6f;
      state.field_53330 = 1.8f;
      state.field_61820 = 15728880;
      state.field_61821 = 0;
      state.field_61823.clear();
      state.field_53453 = 1.0f;
      state.field_53454 = 1.0f;
      state.field_53446 = (float) Math.toDegrees(this.previewYaw);
      state.field_53447 = (float) Math.toDegrees(this.previewYaw);
      state.field_53448 = 0f;

      int cx = this.viewX + this.viewW / 2;
      int scale = this.modelScale();
      Quaternionf baseRot = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf orbit = null;
      if (this.previewPitch != 0f) {
         Quaternionf p = new Quaternionf().rotateX(this.previewPitch);
         baseRot.mul(p);
         orbit = new Quaternionf(p).conjugate();
      }
      Vector3f pos = new Vector3f(0f, state.field_53330 / 2f + 0.0625f, 0f);
      ctx.method_70856(state, scale, pos, baseRot, orbit,
         cx - scale, this.viewY, cx + scale, this.viewY + this.viewH);
   }

   private void renderSheet(class_332 ctx, int mouseX, int mouseY) {
      int s = this.sheetScale;
      int px = SkinCanvas.SIZE * s;
      TurtUIUtils.drawRoundedRect(ctx, this.sheetX - 4, this.sheetY - 4, px + 8, px + 8, 5,
         Palette.alpha(Palette.PANEL_BG, 235));
      TurtUIUtils.drawRoundedBorder(ctx, this.sheetX - 4, this.sheetY - 4, px + 8, px + 8, 5,
         Palette.alpha(Palette.PANEL_BORDER, 255));
      // Checkerboard so transparent texels are obvious.
      for (int y = 0; y < SkinCanvas.SIZE; y++) {
         for (int x = 0; x < SkinCanvas.SIZE; x++) {
            int argb = this.canvas.get(x, y);
            int sx = this.sheetX + x * s, sy = this.sheetY + y * s;
            if ((argb >>> 24) == 0) {
               boolean dark = ((x >> 2) + (y >> 2)) % 2 == 0;
               ctx.method_25294(sx, sy, sx + s, sy + s, dark ? 0xFF1B1F26 : 0xFF232833);
            } else {
               ctx.method_25294(sx, sy, sx + s, sy + s, 0xFF000000 | (argb & 0xFFFFFF));
            }
         }
      }
      if (this.showGrid && s >= 4) {
         int line = 0x22FFFFFF;
         for (int i = 0; i <= SkinCanvas.SIZE; i += 8) {
            ctx.method_25294(this.sheetX + i * s, this.sheetY, this.sheetX + i * s + 1, this.sheetY + px, line);
            ctx.method_25294(this.sheetX, this.sheetY + i * s, this.sheetX + px, this.sheetY + i * s + 1, line);
         }
      }
      // Cursor highlight
      if (this.inSheet(mouseX, mouseY)) {
         int[] t = this.sheetTexel(mouseX, mouseY);
         int hx = this.sheetX + t[0] * s, hy = this.sheetY + t[1] * s;
         ctx.method_73198(hx, hy, s, s, Palette.GREEN.getRGB());
      }
   }

   private void renderToolRail(class_332 ctx, int mouseX, int mouseY) {
      int y = 34;
      SkinCanvas.Tool[] tools = SkinCanvas.Tool.values();
      TurtUIUtils.drawRoundedRect(ctx, this.railX - 2, y - 4, RAIL_BTN + 4,
         tools.length * (RAIL_BTN + RAIL_GAP) + 6, 5, Palette.alpha(Palette.PANEL_BG, 235));
      for (SkinCanvas.Tool tv : tools) {
         int i = tv.ordinal();
         int by = y + i * (RAIL_BTN + RAIL_GAP);
         this.toolY[i] = by;
         boolean sel = this.tool == tv;
         boolean hov = mouseX >= this.railX && mouseX <= this.railX + RAIL_BTN && mouseY >= by && mouseY <= by + RAIL_BTN;
         ctx.method_25294(this.railX, by, this.railX + RAIL_BTN, by + RAIL_BTN,
            (sel ? Palette.GREEN : (hov ? Palette.BTN_HOVER : Palette.BTN_BG)).getRGB());
         ctx.method_73198(this.railX, by, RAIL_BTN, RAIL_BTN, (sel ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
         ctx.method_25300(this.field_22793, tv.name().substring(0, 1), this.railX + RAIL_BTN / 2, by + 6,
            (sel ? Palette.PANEL_BG : Palette.TEXT).getRGB());
      }
   }

   private void renderBottomBar(class_332 ctx, int mouseX, int mouseY, int barY) {
      int x = 40;
      this.swY = barY;
      for (int i = 0; i < SWATCHES.length + 1; i++) {
         this.swX[i] = x;
         boolean custom = i == SWATCHES.length;
         int c = custom ? this.customColor() : SWATCHES[i];
         ctx.method_25294(x, barY, x + SW, barY + SW, 0xFF000000 | (c & 0xFFFFFF));
         ctx.method_73198(x, barY, SW, SW, (this.colorIdx == i ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
         if (custom) {
            ctx.method_25294(x + SW - 4, barY + SW - 4, x + SW - 1, barY + SW - 1, 0xFFFFFFFF);
         }
         x += SW + SWG;
      }

      // Brush sizes
      this.brushBarX = x + 8;
      this.brushBarY = barY;
      for (int i = 0; i < 8; i++) {
         int bx = this.brushBarX + i * 15;
         boolean sel = this.brush == i + 1;
         ctx.method_25294(bx, barY, bx + 13, barY + 13, (sel ? Palette.GREEN : Palette.BTN_BG).getRGB());
         ctx.method_73198(bx, barY, 13, 13, (sel ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
         ctx.method_25300(this.field_22793, String.valueOf(i + 1), bx + 6, barY + 3,
            (sel ? Palette.PANEL_BG : Palette.TEXT).getRGB());
      }

      // Option chips row
      this.optY = barY + 20;
      this.mirrorX0 = 40;
      this.chip(ctx, this.mirrorX0, this.optY, 48, "Mirror", this.mirrorX, mouseX, mouseY);
      this.layerX0 = this.mirrorX0 + 52;
      this.chip(ctx, this.layerX0, this.optY, 74, "Layer: " + this.layerMask.name().charAt(0)
         + this.layerMask.name().substring(1).toLowerCase(), this.layerMask != SkinCanvas.LayerMask.BOTH, mouseX, mouseY);
      this.gridX0 = this.layerX0 + 78;
      this.chip(ctx, this.gridX0, this.optY, 40, "Grid", this.showGrid, mouseX, mouseY);
      this.undoX0 = this.gridX0 + 44;
      this.chip(ctx, this.undoX0, this.optY, 30, "Undo", this.canvas.canUndo(), mouseX, mouseY);
      this.redoX0 = this.undoX0 + 34;
      this.chip(ctx, this.redoX0, this.optY, 30, "Redo", this.canvas.canRedo(), mouseX, mouseY);

      // Username fetch
      this.ignX = this.redoX0 + 40;
      this.ignY = this.optY;
      ctx.method_25294(this.ignX, this.ignY, this.ignX + this.ignW, this.ignY + this.ignH, Palette.SEARCH_BG.getRGB());
      ctx.method_73198(this.ignX, this.ignY, this.ignW, this.ignH,
         (this.ignFocused ? Palette.GREEN : Palette.SEARCH_BORDER).getRGB());
      String shown = this.ignInput.isEmpty() && !this.ignFocused ? "player name..." : this.ignInput + (this.ignFocused ? "_" : "");
      ctx.method_51433(this.field_22793, shown, this.ignX + 4, this.ignY + 4,
         this.ignInput.isEmpty() && !this.ignFocused ? Palette.TEXT_MUTED.getRGB() : Palette.TEXT.getRGB(), false);
      this.chip(ctx, this.ignX + this.ignW + 4, this.ignY, 48, "Fetch", false, mouseX, mouseY);

      if (this.pickerOpen) {
         this.renderPicker(ctx);
      }
   }

   private void chip(class_332 ctx, int x, int y, int w, String label, boolean on, int mouseX, int mouseY) {
      boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 14;
      ctx.method_25294(x, y, x + w, y + 14, (on ? Palette.GREEN : (hov ? Palette.BTN_HOVER : Palette.BTN_BG)).getRGB());
      ctx.method_73198(x, y, w, 14, (on ? Palette.GREEN : Palette.PANEL_BORDER).getRGB());
      ctx.method_25300(this.field_22793, label, x + w / 2, y + 3, (on ? Palette.PANEL_BG : Palette.TEXT).getRGB());
   }

   private void renderPicker(class_332 ctx) {
      int px = Math.min(this.swX[SWATCHES.length], this.field_22789 - PICK_W - 10);
      int py = this.swY - PICK_H - HUE_H - 16;
      this.pickerX = px;
      this.pickerY = py;
      TurtUIUtils.drawRoundedRect(ctx, px - 4, py - 4, PICK_W + 8, PICK_H + HUE_H + 14, 4,
         Palette.alpha(Palette.PANEL_BG, 245));
      TurtUIUtils.drawRoundedBorder(ctx, px - 4, py - 4, PICK_W + 8, PICK_H + HUE_H + 14, 4,
         Palette.alpha(Palette.PANEL_BORDER, 255));
      for (int sxp = 0; sxp < PICK_W; sxp += 2) {
         float sat = (float) sxp / (PICK_W - 1);
         for (int syp = 0; syp < PICK_H; syp += 2) {
            float val = 1f - (float) syp / (PICK_H - 1);
            ctx.method_25294(px + sxp, py + syp, px + sxp + 2, py + syp + 2,
               0xFF000000 | (Color.HSBtoRGB(this.pickH, sat, val) & 0xFFFFFF));
         }
      }
      int mxp = px + Math.round(this.pickS * (PICK_W - 1));
      int myp = py + Math.round((1f - this.pickV) * (PICK_H - 1));
      ctx.method_73198(mxp - 2, myp - 2, 5, 5, 0xFFFFFFFF);
      int hy = py + PICK_H + 4;
      for (int i = 0; i < PICK_W; i++) {
         ctx.method_25294(px + i, hy, px + i + 1, hy + HUE_H,
            0xFF000000 | (Color.HSBtoRGB((float) i / (PICK_W - 1), 1f, 1f) & 0xFFFFFF));
      }
      ctx.method_73198(px + Math.round(this.pickH * (PICK_W - 1)) - 1, hy - 1, 3, HUE_H + 2, 0xFFFFFFFF);
   }

   private boolean handlePickerClick(double mx, double my) {
      int px = this.pickerX, py = this.pickerY;
      if (mx >= px && mx < px + PICK_W && my >= py && my < py + PICK_H) {
         this.pickS = (float) Math.max(0, Math.min(1, (mx - px) / (PICK_W - 1)));
         this.pickV = 1f - (float) Math.max(0, Math.min(1, (my - py) / (PICK_H - 1)));
         this.colorIdx = SWATCHES.length;
         return true;
      }
      int hy = py + PICK_H + 4;
      if (mx >= px && mx < px + PICK_W && my >= hy && my < hy + HUE_H) {
         this.pickH = (float) Math.max(0, Math.min(1, (mx - px) / (PICK_W - 1)));
         this.colorIdx = SWATCHES.length;
         return true;
      }
      return mx >= px - 4 && mx <= px + PICK_W + 4 && my >= py - 4 && my <= py + PICK_H + HUE_H + 10;
   }
}
