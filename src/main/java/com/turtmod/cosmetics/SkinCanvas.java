package com.turtmod.cosmetics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.imageio.ImageIO;

/**
 * The editable 64x64 skin plus every paint operation the editor offers (pencil, eraser, bucket,
 * eyedropper, lighten, darken, noise), with brush size, mirroring, layer masking and undo/redo.
 *
 * <p>Pure AWT so it behaves identically on every branch and can be unit-reasoned about without the game
 * running. Pixels are plain ARGB.
 */
public final class SkinCanvas {
   public static final int SIZE = 64;
   private static final int MAX_HISTORY = 40;

   public enum Tool { PENCIL, ERASER, BUCKET, PICKER, LIGHTEN, DARKEN, NOISE }

   /** Which layer(s) a stroke is allowed to touch. */
   public enum LayerMask { BOTH, BASE, OVERLAY }

   private BufferedImage image;
   private final Deque<BufferedImage> undo = new ArrayDeque<>();
   private final Deque<BufferedImage> redo = new ArrayDeque<>();
   private boolean strokeOpen;

   public SkinCanvas() {
      this.image = blank();
   }

   private static BufferedImage blank() {
      return new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
   }

   public BufferedImage image() {
      return this.image;
   }

   public boolean canUndo() {
      return !this.undo.isEmpty();
   }

   public boolean canRedo() {
      return !this.redo.isEmpty();
   }

   /** Load a skin file, normalising the legacy 64x32 layout up to 64x64. */
   public boolean load(File file) {
      try {
         BufferedImage read = ImageIO.read(file);
         if (read == null) {
            return false;
         }
         BufferedImage out = blank();
         java.awt.Graphics2D g = out.createGraphics();
         g.drawImage(read, 0, 0, null);
         g.dispose();
         if (read.getHeight() == 32) {
            // Legacy skins only define the top half; mirror the limbs into the 64x64 slots.
            copyLegacyLimbs(out);
         }
         this.pushHistory();
         this.image = out;
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   /** Load straight from decoded bytes (used by the "fetch by username" path). */
   public boolean load(byte[] png) {
      try {
         BufferedImage read = ImageIO.read(new java.io.ByteArrayInputStream(png));
         if (read == null) {
            return false;
         }
         BufferedImage out = blank();
         java.awt.Graphics2D g = out.createGraphics();
         g.drawImage(read, 0, 0, null);
         g.dispose();
         if (read.getHeight() == 32) {
            copyLegacyLimbs(out);
         }
         this.pushHistory();
         this.image = out;
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   /** Mirror the 64x32 right arm/leg into the 64x64 left arm/leg slots. */
   private static void copyLegacyLimbs(BufferedImage img) {
      copyMirrored(img, 0, 16, 16, 16, 16, 48);   // right leg -> left leg
      copyMirrored(img, 40, 16, 16, 16, 32, 48);  // right arm -> left arm
   }

   private static void copyMirrored(BufferedImage img, int sx, int sy, int w, int h, int dx, int dy) {
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int px = sx + x;
            int py = sy + y;
            if (px < SIZE && py < SIZE && dx + x < SIZE && dy + y < SIZE) {
               img.setRGB(dx + x, dy + y, img.getRGB(px, py));
            }
         }
      }
   }

   public boolean save(File file) {
      try {
         ImageIO.write(this.image, "png", file);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   // ── history ───────────────────────────────────────────────────────────────
   private BufferedImage copyOf(BufferedImage src) {
      BufferedImage c = blank();
      java.awt.Graphics2D g = c.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.dispose();
      return c;
   }

   private void pushHistory() {
      this.undo.push(this.copyOf(this.image));
      while (this.undo.size() > MAX_HISTORY) {
         this.undo.removeLast();
      }
      this.redo.clear();
   }

   /** Call once when a drag starts so the whole stroke is a single undo step. */
   public void beginStroke() {
      if (!this.strokeOpen) {
         this.pushHistory();
         this.strokeOpen = true;
      }
   }

   public void endStroke() {
      this.strokeOpen = false;
   }

   public void undo() {
      if (this.undo.isEmpty()) {
         return;
      }
      this.redo.push(this.copyOf(this.image));
      this.image = this.undo.pop();
   }

   public void redo() {
      if (this.redo.isEmpty()) {
         return;
      }
      this.undo.push(this.copyOf(this.image));
      this.image = this.redo.pop();
   }

   /** Wipe to fully transparent (a fresh skin). */
   public void clear() {
      this.pushHistory();
      this.image = blank();
   }

   // ── painting ──────────────────────────────────────────────────────────────
   private static boolean inBounds(int x, int y) {
      return x >= 0 && y >= 0 && x < SIZE && y < SIZE;
   }

   private boolean allowed(int x, int y, LayerMask mask) {
      if (mask == LayerMask.BOTH) {
         return true;
      }
      boolean overlay = SkinModel.isOverlayTexel(x, y);
      return mask == LayerMask.OVERLAY ? overlay : !overlay;
   }

   public int get(int x, int y) {
      return inBounds(x, y) ? this.image.getRGB(x, y) : 0;
   }

   private void put(int x, int y, int argb, LayerMask mask) {
      if (inBounds(x, y) && this.allowed(x, y, mask)) {
         this.image.setRGB(x, y, argb);
      }
   }

   /** Blend src over dst using src's alpha. */
   private static int blend(int dst, int src) {
      int sa = (src >>> 24) & 0xFF;
      if (sa == 255) {
         return src;
      }
      if (sa == 0) {
         return dst;
      }
      int da = (dst >>> 24) & 0xFF;
      int outA = sa + da * (255 - sa) / 255;
      if (outA == 0) {
         return 0;
      }
      int r = (((src >> 16) & 0xFF) * sa + ((dst >> 16) & 0xFF) * da * (255 - sa) / 255) / outA;
      int g = (((src >> 8) & 0xFF) * sa + ((dst >> 8) & 0xFF) * da * (255 - sa) / 255) / outA;
      int b = ((src & 0xFF) * sa + (dst & 0xFF) * da * (255 - sa) / 255) / outA;
      return (outA << 24) | (clamp8(r) << 16) | (clamp8(g) << 8) | clamp8(b);
   }

   private static int clamp8(int v) {
      return v < 0 ? 0 : (v > 255 ? 255 : v);
   }

   private static int scaleRgb(int argb, float factor) {
      int a = (argb >>> 24) & 0xFF;
      int r = clamp8(Math.round(((argb >> 16) & 0xFF) * factor));
      int g = clamp8(Math.round(((argb >> 8) & 0xFF) * factor));
      int b = clamp8(Math.round((argb & 0xFF) * factor));
      return (a << 24) | (r << 16) | (g << 8) | b;
   }

   /**
    * Apply a tool at one texel. {@code size} is the brush diameter in texels; {@code mirrorX} also
    * paints the horizontally mirrored texel so both halves of the skin stay symmetric.
    */
   public void apply(Tool tool, int x, int y, int argb, int size, boolean mirrorX, LayerMask mask) {
      if (tool == Tool.BUCKET) {
         this.bucket(x, y, argb, mask);
         if (mirrorX) {
            this.bucket(SIZE - 1 - x, y, argb, mask);
         }
         return;
      }
      int r = Math.max(0, size - 1) / 2;
      for (int dy = -r; dy <= r; dy++) {
         for (int dx = -r; dx <= r; dx++) {
            // Round brush for sizes above 2, square otherwise (a 1px pencil should be exact).
            if (size > 2 && dx * dx + dy * dy > r * r + r) {
               continue;
            }
            this.applyOne(tool, x + dx, y + dy, argb, mask);
            if (mirrorX) {
               this.applyOne(tool, SIZE - 1 - (x + dx), y + dy, argb, mask);
            }
         }
      }
   }

   private void applyOne(Tool tool, int x, int y, int argb, LayerMask mask) {
      if (!inBounds(x, y) || !this.allowed(x, y, mask)) {
         return;
      }
      int cur = this.image.getRGB(x, y);
      switch (tool) {
         case PENCIL -> this.put(x, y, blend(cur, argb), mask);
         case ERASER -> this.put(x, y, 0, mask);
         case LIGHTEN -> this.put(x, y, scaleRgb(cur, 1.12f), mask);
         case DARKEN -> this.put(x, y, scaleRgb(cur, 0.88f), mask);
         case NOISE -> {
            float f = 0.85f + (float) Math.random() * 0.3f;
            this.put(x, y, scaleRgb(cur, f), mask);
         }
         default -> {
         }
      }
   }

   /** Flood fill the contiguous region of matching colour, 4-connected, within the layer mask. */
   private void bucket(int x, int y, int argb, LayerMask mask) {
      if (!inBounds(x, y) || !this.allowed(x, y, mask)) {
         return;
      }
      int target = this.image.getRGB(x, y);
      if (target == argb) {
         return;
      }
      Deque<int[]> stack = new ArrayDeque<>();
      stack.push(new int[]{x, y});
      boolean[] seen = new boolean[SIZE * SIZE];
      while (!stack.isEmpty()) {
         int[] p = stack.pop();
         int px = p[0], py = p[1];
         if (!inBounds(px, py) || seen[py * SIZE + px] || !this.allowed(px, py, mask)) {
            continue;
         }
         if (this.image.getRGB(px, py) != target) {
            continue;
         }
         seen[py * SIZE + px] = true;
         this.image.setRGB(px, py, argb);
         stack.push(new int[]{px + 1, py});
         stack.push(new int[]{px - 1, py});
         stack.push(new int[]{px, py + 1});
         stack.push(new int[]{px, py - 1});
      }
   }

   /** Mirror the left half of the sheet onto the right for each body part (symmetry helper). */
   public void mirrorModel() {
      this.pushHistory();
      // Arms and legs: copy right-side blocks onto the left-side blocks.
      copyMirrored(this.image, 0, 16, 16, 16, 16, 48);
      copyMirrored(this.image, 40, 16, 16, 16, 32, 48);
   }
}
