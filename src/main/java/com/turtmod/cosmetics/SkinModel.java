package com.turtmod.cosmetics;

import java.util.ArrayList;
import java.util.List;

/**
 * The vanilla player model as plain geometry plus its 64x64 skin UV layout, with a ray test that turns a
 * point on the rotated 3D preview into the exact skin texel under the cursor.
 *
 * <p>Coordinates are Minecraft model units with the origin between the feet, +Y up, +Z toward the
 * viewer's back (so the face is at -Z). One unit equals one skin pixel, which is what makes the UV
 * mapping a direct lookup. No Minecraft classes are referenced, so this file is identical on every
 * branch.
 */
public final class SkinModel {
   private SkinModel() {
   }

   /** Face indices, ordered the way the vanilla UV sheet lays a box out. */
   public static final int DOWN = 0, UP = 1, NORTH = 2, SOUTH = 3, WEST = 4, EAST = 5;

   /** One cuboid of the player model plus the UV origin of each of its six faces. */
   public static final class Part {
      public final String name;
      public final double x0, y0, z0, x1, y1, z1;
      /** Per-face {u, v} origin on the 64x64 sheet, base layer. */
      final int[][] baseUv;
      /** Per-face {u, v} origin for the second (overlay) layer, or null when the part has none. */
      final int[][] overlayUv;

      Part(String name, double x0, double y0, double z0, double x1, double y1, double z1,
           int[][] baseUv, int[][] overlayUv) {
         this.name = name;
         this.x0 = x0;
         this.y0 = y0;
         this.z0 = z0;
         this.x1 = x1;
         this.y1 = y1;
         this.z1 = z1;
         this.baseUv = baseUv;
         this.overlayUv = overlayUv;
      }

      public double width() {
         return this.x1 - this.x0;
      }

      public double height() {
         return this.y1 - this.y0;
      }

      public double depth() {
         return this.z1 - this.z0;
      }
   }

   /**
    * UV origins for a box laid out the vanilla way from its top-left corner {@code (u,v)} with the given
    * width/height/depth. The sheet order is: top and bottom on the first row, then right/front/left/back.
    */
   private static int[][] boxUv(int u, int v, int w, int h, int d) {
      return new int[][]{
         {u + d + w, v},          // DOWN
         {u + d, v},              // UP
         {u + d + w + d, v + d},  // NORTH (back)
         {u + d, v + d},          // SOUTH (front)
         {u, v + d},              // WEST  (right arm side)
         {u + d + w, v + d}       // EAST  (left side)
      };
   }

   /** Pixel size of a face: {width, height} on the sheet. */
   static int[] faceSize(Part p, int face) {
      int w = (int) Math.round(p.width());
      int h = (int) Math.round(p.height());
      int d = (int) Math.round(p.depth());
      return switch (face) {
         case DOWN, UP -> new int[]{w, d};
         case WEST, EAST -> new int[]{d, h};
         default -> new int[]{w, h};   // NORTH / SOUTH
      };
   }

   /** The model's parts. Slim ("Alex") narrows both arms to 3 wide, matching vanilla. */
   public static List<Part> parts(boolean slim) {
      double armW = slim ? 3 : 4;
      List<Part> out = new ArrayList<>(6);
      // Head sits on top of the 24-unit body+legs stack.
      out.add(new Part("Head", -4, 24, -4, 4, 32, 4, boxUv(0, 0, 8, 8, 8), boxUv(32, 0, 8, 8, 8)));
      out.add(new Part("Body", -4, 12, -2, 4, 24, 2, boxUv(16, 16, 8, 12, 4), boxUv(16, 32, 8, 12, 4)));
      out.add(new Part("Right Arm", -4 - armW, 12, -2, -4, 24, 2,
         boxUv(40, 16, (int) armW, 12, 4), boxUv(40, 32, (int) armW, 12, 4)));
      out.add(new Part("Left Arm", 4, 12, -2, 4 + armW, 24, 2,
         boxUv(32, 48, (int) armW, 12, 4), boxUv(48, 48, (int) armW, 12, 4)));
      out.add(new Part("Right Leg", -4, 0, -2, 0, 12, 2, boxUv(0, 16, 4, 12, 4), boxUv(0, 32, 4, 12, 4)));
      out.add(new Part("Left Leg", 0, 0, -2, 4, 12, 2, boxUv(16, 48, 4, 12, 4), boxUv(0, 48, 4, 12, 4)));
      return out;
   }

   /** Result of a pick: which texel was hit, and on which part/face. */
   public static final class Hit {
      public final int texelX, texelY;
      public final Part part;
      public final int face;
      public final double distance;

      Hit(int texelX, int texelY, Part part, int face, double distance) {
         this.texelX = texelX;
         this.texelY = texelY;
         this.part = part;
         this.face = face;
         this.distance = distance;
      }
   }

   /**
    * Cast a ray through the rotated model and return the skin texel it lands on.
    *
    * <p>The preview is orthographic, so the ray starts at the clicked point on the view plane and travels
    * straight into the screen. We rotate that ray into model space (inverse of the preview's yaw then
    * pitch) and slab-test every box, keeping the nearest front face.
    *
    * @param vx      click X in model units relative to the model's centre, +X right
    * @param vy      click Y in model units relative to the model's centre, +Y up
    * @param yaw     preview yaw in radians (same value used to draw it)
    * @param pitch   preview pitch in radians
    * @param slim    slim arm model
    * @param overlay pick the overlay layer's UVs instead of the base layer
    */
   public static Hit pick(double vx, double vy, double yaw, double pitch, boolean slim, boolean overlay) {
      // Ray in view space: straight into the screen from far in front.
      double[] o = {vx, vy, 64.0};
      double[] d = {0, 0, -1};
      // Into model space: undo pitch (about X) then yaw (about Y).
      o = rotX(o, -pitch);
      d = rotX(d, -pitch);
      o = rotY(o, -yaw);
      d = rotY(d, -yaw);

      Hit best = null;
      for (Part p : parts(slim)) {
         Hit h = intersect(p, o, d, overlay);
         if (h != null && (best == null || h.distance < best.distance)) {
            best = h;
         }
      }
      return best;
   }

   private static double[] rotX(double[] v, double a) {
      double c = Math.cos(a), s = Math.sin(a);
      return new double[]{v[0], v[1] * c - v[2] * s, v[1] * s + v[2] * c};
   }

   private static double[] rotY(double[] v, double a) {
      double c = Math.cos(a), s = Math.sin(a);
      return new double[]{v[0] * c + v[2] * s, v[1], -v[0] * s + v[2] * c};
   }

   /** Slab test against one box; returns the texel on the entry face, or null when missed. */
   private static Hit intersect(Part p, double[] o, double[] d, boolean overlay) {
      // The model's vertical centre is at y=16, and pick() works relative to the centre.
      double oy = o[1] + 16.0;
      double tMin = Double.NEGATIVE_INFINITY, tMax = Double.POSITIVE_INFINITY;
      int enterAxis = -1;
      boolean enterNeg = false;

      double[] lo = {p.x0, p.y0, p.z0};
      double[] hi = {p.x1, p.y1, p.z1};
      double[] org = {o[0], oy, o[2]};

      for (int axis = 0; axis < 3; axis++) {
         double dd = d[axis];
         double oo = org[axis];
         if (Math.abs(dd) < 1e-9) {
            if (oo < lo[axis] || oo > hi[axis]) {
               return null;
            }
            continue;
         }
         double t1 = (lo[axis] - oo) / dd;
         double t2 = (hi[axis] - oo) / dd;
         boolean neg = t1 > t2;
         if (neg) {
            double tmp = t1;
            t1 = t2;
            t2 = tmp;
         }
         if (t1 > tMin) {
            tMin = t1;
            enterAxis = axis;
            enterNeg = neg;
         }
         tMax = Math.min(tMax, t2);
         if (tMin > tMax) {
            return null;
         }
      }
      if (enterAxis < 0 || tMin < 0) {
         return null;
      }

      double hx = org[0] + d[0] * tMin;
      double hy = org[1] + d[1] * tMin;
      double hz = org[2] + d[2] * tMin;

      // Which face did we enter through, and where on it?
      int face;
      double fu, fv;
      int[] size;
      if (enterAxis == 0) {
         face = enterNeg ? EAST : WEST;                     // -X is the model's right side
         size = faceSize(p, face);
         fu = face == WEST ? (hz - p.z0) : (p.z1 - hz);
         fv = p.y1 - hy;
      } else if (enterAxis == 1) {
         face = enterNeg ? UP : DOWN;
         size = faceSize(p, face);
         fu = hx - p.x0;
         fv = face == UP ? (hz - p.z0) : (p.z1 - hz);
      } else {
         face = enterNeg ? NORTH : SOUTH;                   // -Z is the front
         size = faceSize(p, face);
         fu = face == SOUTH ? (hx - p.x0) : (p.x1 - hx);
         fv = p.y1 - hy;
      }

      int[][] uv = overlay ? p.overlayUv : p.baseUv;
      if (uv == null) {
         return null;
      }
      int tx = uv[face][0] + (int) Math.floor(clamp(fu, 0, size[0] - 1e-6));
      int ty = uv[face][1] + (int) Math.floor(clamp(fv, 0, size[1] - 1e-6));
      return new Hit(tx, ty, p, face, tMin);
   }

   private static double clamp(double v, double lo, double hi) {
      return v < lo ? lo : (v > hi ? hi : v);
   }

   /** True when this texel belongs to the overlay (second) layer of the 64x64 sheet. */
   public static boolean isOverlayTexel(int x, int y) {
      if (y < 16) {
         return x >= 32 && x < 64;          // hat
      }
      if (y < 32) {
         return false;                       // base body/arms/legs row
      }
      if (y < 48) {
         return true;                        // jacket + sleeves row
      }
      return (x >= 0 && x < 16) || (x >= 48 && x < 64);   // second-layer leg/arm blocks
   }
}
