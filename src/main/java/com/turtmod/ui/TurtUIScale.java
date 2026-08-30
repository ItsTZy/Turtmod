package com.turtmod.ui;

import net.minecraft.class_332;

/**
 * "Design once, fit anywhere" helper. A screen lays its panel out at a fixed <em>logical</em> size
 * (e.g. 520x350) and draws everything through this transform. The transform shrinks the whole panel
 * uniformly so it always fits the current (GUI-scaled) screen — at any resolution or GUI scale —
 * without elements ever overlapping or running off-screen. It never scales <em>up</em> past 1.0, so
 * on normal screens the UI renders at its native, crisp size.
 *
 * <p>Usage:
 * <pre>
 *   scale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, margin);
 *   scale.push(ctx);
 *   ... draw using logical coordinates (panel at 0,0 .. LOGICAL_W,LOGICAL_H) ...
 *   scale.pop(ctx);
 * </pre>
 * In input handlers, convert screen mouse coords to logical with {@link #toLogicalX(double)} /
 * {@link #toLogicalY(double)} before hit-testing.
 */
public final class TurtUIScale {
   public float scale = 1f;
   public float offsetX = 0f;
   public float offsetY = 0f;
   public int logicalW = 0;
   public int logicalH = 0;

   /** Recompute the fit transform. Call every frame (cheap) so resizes are handled automatically. */
   public void compute(int screenW, int screenH, int logicalW, int logicalH, int margin) {
      this.logicalW = logicalW;
      this.logicalH = logicalH;
      float fitW = (float)(screenW - margin * 2) / (float)logicalW;
      float fitH = (float)(screenH - margin * 2) / (float)logicalH;
      this.scale = Math.min(1f, Math.min(fitW, fitH));
      if (this.scale <= 0f) {
         this.scale = 0.01f;
      }
      this.offsetX = (screenW - logicalW * this.scale) / 2f;
      this.offsetY = (screenH - logicalH * this.scale) / 2f;
   }

   public void push(class_332 ctx) {
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(this.offsetX, this.offsetY);
      ctx.method_51448().scale(this.scale, this.scale);
   }

   public void pop(class_332 ctx) {
      ctx.method_51448().popMatrix();
   }

   public double toLogicalX(double screenX) {
      return (screenX - this.offsetX) / this.scale;
   }

   public double toLogicalY(double screenY) {
      return (screenY - this.offsetY) / this.scale;
   }

   /** Logical X → screen pixel (for scissor rectangles, which are framebuffer-space). */
   public int toScreenX(int logicalX) {
      return Math.round(this.offsetX + logicalX * this.scale);
   }

   public int toScreenY(int logicalY) {
      return Math.round(this.offsetY + logicalY * this.scale);
   }
}
