package com.turtmod.visual;

import net.minecraft.class_4588;

public class TintedVertexConsumer implements class_4588 {
   private final class_4588 delegate;
   private final float red;
   private final float green;
   private final float blue;
   private final float alpha;

   public TintedVertexConsumer(class_4588 delegate, float red, float green, float blue, float alpha) {
      this.delegate = delegate;
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
   }

   public class_4588 method_22912(float x, float y, float z) {
      return this.delegate.method_22912(x, y, z);
   }

   public class_4588 method_1336(int r, int g, int b, int a) {
      return this.delegate.method_1336((int)((float)r * this.red), (int)((float)g * this.green), (int)((float)b * this.blue), (int)((float)a * this.alpha));
   }

   public class_4588 method_39415(int argb) {
      int a = argb >> 24 & 255;
      int r = argb >> 16 & 255;
      int g = argb >> 8 & 255;
      int b = argb & 255;
      return this.delegate.method_1336((int)((float)r * this.red), (int)((float)g * this.green), (int)((float)b * this.blue), (int)((float)a * this.alpha));
   }

   public class_4588 method_22913(float u, float v) {
      return this.delegate.method_22913(u, v);
   }

   public class_4588 method_60796(int u, int v) {
      return this.delegate.method_60796(u, v);
   }

   public class_4588 method_22921(int u, int v) {
      return this.delegate.method_22921(240, 240);
   }

   public class_4588 method_22914(float x, float y, float z) {
      return this.delegate.method_22914(x, y, z);
   }

   public class_4588 method_75298(float lineWidth) {
      return this.delegate.method_75298(lineWidth);
   }
}
