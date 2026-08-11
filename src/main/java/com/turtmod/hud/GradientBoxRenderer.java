package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_238;
import net.minecraft.class_265;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Vector3f;

/**
 * Draws 3D wireframe boxes / voxel-shape edges with a SPATIAL vertical gradient — each line's two endpoints
 * get their own colour sampled from the gradient by height, and the GPU interpolates along the edge. This is
 * how real clients (e.g. Custom-Hitboxes' VertexRenderer) do gradient hitboxes: bottom of the box = the first
 * stop, top = the last stop, verticals blend between them. Static — no time animation.
 *
 * <p>Uses the exact vanilla 1.21.11 line-vertex sequence verified from {@code class_9974.method_62296}:
 * {@code vc.method_56824(entry,x,y,z).method_39415(argb).method_61959(entry,normal).method_75298(width)}.
 * The lines render layer is {@code class_12249.field_64042}.
 */
public final class GradientBoxRenderer {
   private GradientBoxRenderer() {
   }

   /** Colour (ARGB) at height fraction t in [0,1] across the gradient. */
   private static int colorAt(TurtModConfig.GradientDef grad, double minY, double span, double y) {
      float t = span > 1.0e-6 ? (float) ((y - minY) / span) : 0f;
      return grad.colorAt(t);
   }

   /** Voxel-shape edges (block outline) with a vertical gradient. Mirrors {@code class_9974.method_62296}. */
   public static void drawShapeGradient(class_4587 matrices, class_4588 vc, class_265 shape,
                                        double dx, double dy, double dz, TurtModConfig.GradientDef grad, float width) {
      class_4587.class_4665 entry = matrices.method_23760();
      // First pass: find the shape's vertical extent so t maps bottom→top cleanly.
      double[] bounds = {Double.MAX_VALUE, -Double.MAX_VALUE};
      shape.method_1104((x1, y1, z1, x2, y2, z2) -> {
         bounds[0] = Math.min(bounds[0], Math.min(y1, y2));
         bounds[1] = Math.max(bounds[1], Math.max(y1, y2));
      });
      final double minY = bounds[0];
      final double span = bounds[1] - bounds[0];
      shape.method_1104((x1, y1, z1, x2, y2, z2) -> {
         Vector3f normal = new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
         int c1 = colorAt(grad, minY, span, y1);
         int c2 = colorAt(grad, minY, span, y2);
         vc.method_56824(entry, (float) (x1 + dx), (float) (y1 + dy), (float) (z1 + dz)).method_39415(c1).method_61959(entry, normal).method_75298(width);
         vc.method_56824(entry, (float) (x2 + dx), (float) (y2 + dy), (float) (z2 + dz)).method_39415(c2).method_61959(entry, normal).method_75298(width);
      });
   }

   /** A single box outline (12 edges) with a vertical gradient — used for entity hitboxes. */
   public static void drawBoxGradient(class_4587 matrices, class_4588 vc, class_238 box, TurtModConfig.GradientDef grad, float width) {
      class_4587.class_4665 entry = matrices.method_23760();
      double minX = box.field_1323, minY = box.field_1322, minZ = box.field_1321;
      double maxX = box.field_1320, maxY = box.field_1325, maxZ = box.field_1324;
      double span = maxY - minY;
      int cb = grad.colorAt(0f);     // bottom
      int ct = grad.colorAt(1f);     // top
      // Bottom rectangle (all at minY → bottom colour).
      edge(vc, entry, minX, minY, minZ, maxX, minY, minZ, cb, cb, width);
      edge(vc, entry, maxX, minY, minZ, maxX, minY, maxZ, cb, cb, width);
      edge(vc, entry, maxX, minY, maxZ, minX, minY, maxZ, cb, cb, width);
      edge(vc, entry, minX, minY, maxZ, minX, minY, minZ, cb, cb, width);
      // Top rectangle (all at maxY → top colour).
      edge(vc, entry, minX, maxY, minZ, maxX, maxY, minZ, ct, ct, width);
      edge(vc, entry, maxX, maxY, minZ, maxX, maxY, maxZ, ct, ct, width);
      edge(vc, entry, maxX, maxY, maxZ, minX, maxY, maxZ, ct, ct, width);
      edge(vc, entry, minX, maxY, maxZ, minX, maxY, minZ, ct, ct, width);
      // Verticals (bottom colour → top colour).
      edge(vc, entry, minX, minY, minZ, minX, maxY, minZ, cb, ct, width);
      edge(vc, entry, maxX, minY, minZ, maxX, maxY, minZ, cb, ct, width);
      edge(vc, entry, maxX, minY, maxZ, maxX, maxY, maxZ, cb, ct, width);
      edge(vc, entry, minX, minY, maxZ, minX, maxY, maxZ, cb, ct, width);
   }

   private static void edge(class_4588 vc, class_4587.class_4665 entry,
                            double x1, double y1, double z1, double x2, double y2, double z2,
                            int c1, int c2, float width) {
      Vector3f normal = new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
      vc.method_56824(entry, (float) x1, (float) y1, (float) z1).method_39415(c1).method_61959(entry, normal).method_75298(width);
      vc.method_56824(entry, (float) x2, (float) y2, (float) z2).method_39415(c2).method_61959(entry, normal).method_75298(width);
   }
}
