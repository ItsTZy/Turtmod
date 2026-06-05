package com.turtmod.visual;

import java.lang.reflect.Proxy;
import net.minecraft.class_4588;

public final class TintingVertexConsumer {
   private TintingVertexConsumer() {
   }

   public static class_4588 wrapIfNeeded(class_4588 original, int tintArgb) {
      return original != null && tintArgb != -1 ? (class_4588)Proxy.newProxyInstance(class_4588.class.getClassLoader(), new Class[]{class_4588.class}, (proxy, method, args) -> {
         String name = method.getName();
         if ("equals".equals(name) && args != null && args.length == 1) {
            return proxy == args[0];
         } else if (!"hashCode".equals(name) || args != null && args.length != 0) {
            if (!"toString".equals(name) || args != null && args.length != 0) {
               Object[] adapted = args;
               int tintA = tintArgb >>> 24 & 255;
               int tintR = tintArgb >>> 16 & 255;
               int tintG = tintArgb >>> 8 & 255;
               int tintB = tintArgb & 255;
               if ("color".equals(name) && args != null) {
                  if (args.length == 4 && args[0] instanceof Integer && args[1] instanceof Integer && args[2] instanceof Integer && args[3] instanceof Integer) {
                     adapted = new Object[]{mul((Integer)args[0], tintR), mul((Integer)args[1], tintG), mul((Integer)args[2], tintB), mul((Integer)args[3], tintA)};
                  } else if (args.length == 4 && args[0] instanceof Float && args[1] instanceof Float && args[2] instanceof Float && args[3] instanceof Float) {
                     adapted = new Object[]{mul((Float)args[0], tintR), mul((Float)args[1], tintG), mul((Float)args[2], tintB), mul((Float)args[3], tintA)};
                  } else if (args.length == 1 && args[0] instanceof Integer) {
                     adapted = new Object[]{tintPackedArgb((Integer)args[0], tintA, tintR, tintG, tintB)};
                  }
               } else if ("colorRgb".equals(name) && args != null && args.length == 1 && args[0] instanceof Integer) {
                  adapted = new Object[]{tintPackedRgb((Integer)args[0], tintR, tintG, tintB)};
               } else if ("vertex".equals(name) && args != null && args.length == 11 && args[3] instanceof Integer) {
                  Object[] next = args.clone();
                  next[3] = tintPackedArgb((Integer)args[3], tintA, tintR, tintG, tintB);
                  adapted = next;
               }

               Object out = method.invoke(original, adapted);
               return out == original && method.getReturnType().isAssignableFrom(class_4588.class) ? proxy : out;
            } else {
               return "Tinted(" + String.valueOf(original) + ")";
            }
         } else {
            return System.identityHashCode(proxy);
         }
      }) : original;
   }

   private static int mul(int value, int tint) {
      return Math.max(0, Math.min(255, value * tint / 255));
   }

   private static float mul(float value, int tint) {
      float out = value * ((float)tint / 255.0F);
      if (out < 0.0F) {
         return 0.0F;
      } else {
         return out > 1.0F ? 1.0F : out;
      }
   }

   private static int tintPackedArgb(int argb, int tintA, int tintR, int tintG, int tintB) {
      int a = mul(argb >>> 24 & 255, tintA);
      int r = mul(argb >>> 16 & 255, tintR);
      int g = mul(argb >>> 8 & 255, tintG);
      int b = mul(argb & 255, tintB);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int tintPackedRgb(int rgb, int tintR, int tintG, int tintB) {
      int r = mul(rgb >>> 16 & 255, tintR);
      int g = mul(rgb >>> 8 & 255, tintG);
      int b = mul(rgb & 255, tintB);
      return r << 16 | g << 8 | b;
   }
}
