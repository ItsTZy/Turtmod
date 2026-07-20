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
         draw(g, a);
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
