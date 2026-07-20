package com.turtmod.gallery;

import com.turtmod.gallery.ScreenshotEditorScreen.Annotation;
import com.turtmod.utils.ImageClipboardUtils;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Bakes the editor's vector annotations into a real PNG using AWT (no GPU / NativeImage pixel juggling).
 * The screenshot is re-read fresh from disk with {@link ImageIO} so colors are plain ARGB — the on-screen
 * preview in {@link ScreenshotEditorScreen} draws the same geometry/colors, so the result matches closely.
 */
public final class EditorRasterizer {
   private EditorRasterizer() {
   }

   /** Apply the shared quality hints so preview and saved output rasterize identically. */
   private static void applyHints(Graphics2D g) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
   }

   /**
    * Draw just the annotations onto a transparent image of the given size, scaled by {@code scale}
    * (annotation coordinates are in full image pixels). This is what the editor blits as a live
    * overlay, so the on-screen preview is produced by the very same code that writes the PNG.
    */
   public static BufferedImage renderOverlay(int w, int h, double scale, List<Annotation> anns) {
      BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = img.createGraphics();
      applyHints(g);
      g.scale(scale, scale);
      for (Annotation a : anns) {
         draw(g, a);
      }
      g.dispose();
      return img;
   }

   /**
    * Same as {@link #renderOverlay} but only covering the image-space region starting at
    * ({@code offX},{@code offY}). Used for the in-progress stroke so it is drawn by this exact
    * renderer while you drag — no change in appearance when the stroke is committed.
    */
   public static BufferedImage renderOverlayRegion(int w, int h, double scale, double offX, double offY, List<Annotation> anns) {
      BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = img.createGraphics();
      applyHints(g);
      g.scale(scale, scale);
      g.translate(-offX, -offY);
      for (Annotation a : anns) {
         draw(g, a);
      }
      g.dispose();
      return img;
   }

   /** Rough image-space bounds an annotation paints into, padded for stroke width / arrow heads. */
   public static double[] bounds(Annotation a) {
      double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
      for (double[] p : a.pts) {
         minX = Math.min(minX, p[0]);
         minY = Math.min(minY, p[1]);
         maxX = Math.max(maxX, p[0]);
         maxY = Math.max(maxY, p[1]);
      }
      if (minX > maxX) {
         return new double[]{0, 0, 0, 0};
      }
      double pad = Math.max(a.size * 3.0, 12.0);
      if (a.type == ScreenshotEditorScreen.Tool.TEXT) {
         int fp = fontPx(a.size);
         String[] lines = (a.text == null ? "" : a.text).split("\n", -1);
         int longest = 0;
         for (String line : lines) {
            longest = Math.max(longest, line.length());
         }
         maxX += fp * 0.75 * Math.max(1, longest);
         maxY += fp * 1.35 * Math.max(1, lines.length);
         pad = Math.max(pad, fp);
      }
      return new double[]{minX - pad, minY - pad, maxX + pad, maxY + pad};
   }

   /** Compose the base image + annotations, apply crop, and return the final RGB(A) image. */
   public static BufferedImage bake(File source, int[] crop, List<Annotation> anns) throws Exception {
      BufferedImage read = ImageIO.read(source);
      if (read == null) {
         throw new java.io.IOException("Unsupported image: " + source.getName());
      }
      BufferedImage full = new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = full.createGraphics();
      g.drawImage(read, 0, 0, null);
      applyHints(g);
      for (Annotation a : anns) {
         if (a.type == ScreenshotEditorScreen.Tool.BLUR || a.type == ScreenshotEditorScreen.Tool.PIXELATE) {
            g.dispose();
            applyRegionEffect(full, a);
            g = full.createGraphics();
            applyHints(g);
         } else {
            draw(g, a);
         }
      }
      g.dispose();

      int cx = Math.max(0, Math.min(crop[0], full.getWidth() - 1));
      int cy = Math.max(0, Math.min(crop[1], full.getHeight() - 1));
      int cw = Math.max(1, Math.min(crop[2], full.getWidth() - cx));
      int ch = Math.max(1, Math.min(crop[3], full.getHeight() - cy));
      if (cx == 0 && cy == 0 && cw == full.getWidth() && ch == full.getHeight()) {
         return full;
      }
      BufferedImage out = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
      Graphics2D og = out.createGraphics();
      og.drawImage(full, -cx, -cy, null);
      og.dispose();
      return out;
   }

   /**
    * Region effects (blur / pixelate) have to sample the image underneath, so they are applied to the
    * composed picture rather than stroked like the other tools.
    */
   private static void applyRegionEffect(BufferedImage img, Annotation a) {
      int x0 = Math.max(0, Math.min(ix(a, 0), ix(a, 1)));
      int y0 = Math.max(0, Math.min(iy(a, 0), iy(a, 1)));
      int x1 = Math.min(img.getWidth(), Math.max(ix(a, 0), ix(a, 1)));
      int y1 = Math.min(img.getHeight(), Math.max(iy(a, 0), iy(a, 1)));
      int w = x1 - x0, h = y1 - y0;
      if (w <= 1 || h <= 1) {
         return;
      }
      if (a.type == ScreenshotEditorScreen.Tool.PIXELATE) {
         int block = Math.max(2, Math.round(a.size * 2f));
         for (int by = y0; by < y1; by += block) {
            for (int bx = x0; bx < x1; bx += block) {
               int bw = Math.min(block, x1 - bx), bh = Math.min(block, y1 - by);
               long r = 0, g2 = 0, b = 0, n = 0;
               for (int yy = by; yy < by + bh; yy++) {
                  for (int xx = bx; xx < bx + bw; xx++) {
                     int c = img.getRGB(xx, yy);
                     r += (c >> 16) & 0xFF;
                     g2 += (c >> 8) & 0xFF;
                     b += c & 0xFF;
                     n++;
                  }
               }
               if (n == 0) {
                  continue;
               }
               int avg = 0xFF000000 | ((int) (r / n) << 16) | ((int) (g2 / n) << 8) | (int) (b / n);
               for (int yy = by; yy < by + bh; yy++) {
                  for (int xx = bx; xx < bx + bw; xx++) {
                     img.setRGB(xx, yy, avg);
                  }
               }
            }
         }
         return;
      }
      // BLUR: a couple of box-blur passes over the region.
      int radius = Math.max(1, Math.round(a.size));
      int[] src = img.getRGB(x0, y0, w, h, null, 0, w);
      int[] dst = new int[src.length];
      for (int pass = 0; pass < 2; pass++) {
         for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
               long r = 0, g2 = 0, b = 0, n = 0;
               for (int ky = -radius; ky <= radius; ky++) {
                  int sy = yy + ky;
                  if (sy < 0 || sy >= h) {
                     continue;
                  }
                  for (int kx = -radius; kx <= radius; kx++) {
                     int sx = xx + kx;
                     if (sx < 0 || sx >= w) {
                        continue;
                     }
                     int c = src[sy * w + sx];
                     r += (c >> 16) & 0xFF;
                     g2 += (c >> 8) & 0xFF;
                     b += c & 0xFF;
                     n++;
                  }
               }
               dst[yy * w + xx] = 0xFF000000 | ((int) (r / n) << 16) | ((int) (g2 / n) << 8) | (int) (b / n);
            }
         }
         System.arraycopy(dst, 0, src, 0, src.length);
      }
      img.setRGB(x0, y0, w, h, src, 0, w);
   }

   private static void draw(Graphics2D g, Annotation a) {
      Color color = new Color(a.argb, true);
      float t = Math.max(1f, a.size);
      switch (a.type) {
         case PEN -> {
            g.setColor(color);
            g.setStroke(new BasicStroke(t, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(pathOf(a));
         }
         case HIGHLIGHTER -> {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
            g.setColor(new Color(a.argb | 0xFF000000, false));
            g.setStroke(new BasicStroke(t * 2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(pathOf(a));
            g.setComposite(AlphaComposite.SrcOver);
         }
         case LINE -> {
            g.setColor(color);
            g.setStroke(new BasicStroke(t, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(ix(a, 0), iy(a, 0), ix(a, 1), iy(a, 1));
         }
         case ARROW -> {
            g.setColor(color);
            g.setStroke(new BasicStroke(t, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x1 = ix(a, 0), y1 = iy(a, 0), x2 = ix(a, 1), y2 = iy(a, 1);
            g.drawLine(x1, y1, x2, y2);
            double ang = Math.atan2(y2 - y1, x2 - x1);
            double head = Math.max(10.0, t * 4.0);
            double a1 = ang + Math.toRadians(160), a2 = ang - Math.toRadians(160);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(x2, y2);
            p.lineTo(x2 + head * Math.cos(a1), y2 + head * Math.sin(a1));
            p.lineTo(x2 + head * Math.cos(a2), y2 + head * Math.sin(a2));
            p.closePath();
            g.fill(p);
         }
         case RECT -> {
            g.setColor(color);
            g.setStroke(new BasicStroke(t, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = Math.min(ix(a, 0), ix(a, 1)), y = Math.min(iy(a, 0), iy(a, 1));
            int w = Math.abs(ix(a, 1) - ix(a, 0)), h = Math.abs(iy(a, 1) - iy(a, 0));
            if (a.filled) {
               g.fillRect(x, y, w, h);
            } else {
               g.drawRect(x, y, w, h);
            }
         }
         case ELLIPSE -> {
            g.setColor(color);
            g.setStroke(new BasicStroke(t, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = Math.min(ix(a, 0), ix(a, 1)), y = Math.min(iy(a, 0), iy(a, 1));
            int w = Math.abs(ix(a, 1) - ix(a, 0)), h = Math.abs(iy(a, 1) - iy(a, 0));
            if (a.filled) {
               g.fillOval(x, y, w, h);
            } else {
               g.drawOval(x, y, w, h);
            }
         }
         case BLUR, PIXELATE -> {
            // In the live overlay these only mark their area; the real effect is applied on save.
            g.setColor(new Color(255, 255, 255, 90));
            g.setStroke(new BasicStroke(Math.max(1f, t / 2f)));
            int x = Math.min(ix(a, 0), ix(a, 1)), y = Math.min(iy(a, 0), iy(a, 1));
            g.drawRect(x, y, Math.abs(ix(a, 1) - ix(a, 0)), Math.abs(iy(a, 1) - iy(a, 0)));
         }
         case TEXT -> {
            if (a.text != null && !a.text.isEmpty()) {
               g.setColor(color);
               g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, fontPx(a.size)));
               java.awt.FontMetrics fm = g.getFontMetrics();
               String[] lines = a.text.split("\n", -1);
               int ly = iy(a, 0) + fm.getAscent();
               for (String line : lines) {
                  g.drawString(line, ix(a, 0), ly);
                  ly += fm.getHeight();
               }
            }
         }
         default -> {
         }
      }
   }

   static int fontPx(float size) {
      return Math.max(10, Math.round(8 + size * 4f));
   }

   private static GeneralPath pathOf(Annotation a) {
      GeneralPath p = new GeneralPath();
      if (a.pts.isEmpty()) {
         return p;
      }
      double[] first = a.pts.get(0);
      p.moveTo(first[0], first[1]);
      for (int i = 1; i < a.pts.size(); i++) {
         double[] q = a.pts.get(i);
         p.lineTo(q[0], q[1]);
      }
      if (a.pts.size() == 1) {
         p.lineTo(first[0] + 0.01, first[1] + 0.01); // a lone dot
      }
      return p;
   }

   private static int ix(Annotation a, int i) {
      return (int) Math.round(a.pts.get(Math.min(i, a.pts.size() - 1))[0]);
   }

   private static int iy(Annotation a, int i) {
      return (int) Math.round(a.pts.get(Math.min(i, a.pts.size() - 1))[1]);
   }

   /** Write the baked image to a PNG file. */
   public static void save(BufferedImage img, File out) throws Exception {
      ImageIO.write(img, "png", out);
   }

   /** Bake, write to a temp PNG, and copy it to the system clipboard. */
   public static boolean copyToClipboard(BufferedImage img) {
      try {
         File tmp = File.createTempFile("turtmod-edit", ".png");
         tmp.deleteOnExit();
         ImageIO.write(img, "png", tmp);
         return ImageClipboardUtils.copyImageToClipboard(tmp);
      } catch (Exception e) {
         return false;
      }
   }
}
