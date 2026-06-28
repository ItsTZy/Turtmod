package com.turtmod.ui.config.model;

/**
 * Minimal ARGB colour holder for the native config model (replaces WalksyLib's colour type). Carries
 * the static HSB⇄RGB helpers the colour picker needs so the model has no external dependency.
 */
public final class ConfigColor {
   private final int value;

   public ConfigColor(int r, int g, int b, int a) {
      this.value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
   }

   public ConfigColor(int argb) {
      this.value = argb;
   }

   public int getRGB() {
      return this.value;
   }

   public static int HSBtoRGB(float hue, float saturation, float brightness) {
      int r = 0;
      int g = 0;
      int b = 0;
      if (saturation == 0) {
         r = g = b = (int) (brightness * 255.0f + 0.5f);
      } else {
         float h = (hue - (float) Math.floor(hue)) * 6.0f;
         float f = h - (float) Math.floor(h);
         float p = brightness * (1.0f - saturation);
         float q = brightness * (1.0f - saturation * f);
         float t = brightness * (1.0f - (saturation * (1.0f - f)));
         switch ((int) h) {
            case 0 -> {
               r = (int) (brightness * 255.0f + 0.5f);
               g = (int) (t * 255.0f + 0.5f);
               b = (int) (p * 255.0f + 0.5f);
            }
            case 1 -> {
               r = (int) (q * 255.0f + 0.5f);
               g = (int) (brightness * 255.0f + 0.5f);
               b = (int) (p * 255.0f + 0.5f);
            }
            case 2 -> {
               r = (int) (p * 255.0f + 0.5f);
               g = (int) (brightness * 255.0f + 0.5f);
               b = (int) (t * 255.0f + 0.5f);
            }
            case 3 -> {
               r = (int) (p * 255.0f + 0.5f);
               g = (int) (q * 255.0f + 0.5f);
               b = (int) (brightness * 255.0f + 0.5f);
            }
            case 4 -> {
               r = (int) (t * 255.0f + 0.5f);
               g = (int) (p * 255.0f + 0.5f);
               b = (int) (brightness * 255.0f + 0.5f);
            }
            case 5 -> {
               r = (int) (brightness * 255.0f + 0.5f);
               g = (int) (p * 255.0f + 0.5f);
               b = (int) (q * 255.0f + 0.5f);
            }
            default -> {
            }
         }
      }
      return 0xff000000 | (r << 16) | (g << 8) | b;
   }

   public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
      float hue;
      float saturation;
      float brightness;
      if (hsbvals == null) {
         hsbvals = new float[3];
      }
      int cmax = Math.max(r, g);
      if (b > cmax) {
         cmax = b;
      }
      int cmin = Math.min(r, g);
      if (b < cmin) {
         cmin = b;
      }
      brightness = (float) cmax / 255.0f;
      saturation = cmax != 0 ? (float) (cmax - cmin) / (float) cmax : 0;
      if (saturation == 0) {
         hue = 0;
      } else {
         float redc = (float) (cmax - r) / (float) (cmax - cmin);
         float greenc = (float) (cmax - g) / (float) (cmax - cmin);
         float bluec = (float) (cmax - b) / (float) (cmax - cmin);
         if (r == cmax) {
            hue = bluec - greenc;
         } else if (g == cmax) {
            hue = 2.0f + redc - bluec;
         } else {
            hue = 4.0f + greenc - redc;
         }
         hue = hue / 6.0f;
         if (hue < 0) {
            hue = hue + 1.0f;
         }
      }
      hsbvals[0] = hue;
      hsbvals[1] = saturation;
      hsbvals[2] = brightness;
      return hsbvals;
   }
}
