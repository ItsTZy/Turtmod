package com.turtmod.ui;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;

/**
 * Helpers for nicely-styled chat output. The {@code [TurtMod]} prefix is drawn with a per-character
 * green gradient and gray brackets so feature messages look branded instead of plain.
 */
public final class TurtChat {
   // Match the mod's brand green (Palette.GREEN) — gradient from a light tint into the brand green.
   private static final int GREEN_LIGHT = 0xCDEFA0;
   private static final int GREEN_DARK = Palette.GREEN.getRGB() & 0xFFFFFF;

   private TurtChat() {
   }

   /** A {@code [TurtMod]} prefix: gray brackets, gradient-green name, trailing space. */
   public static class_5250 prefix() {
      class_5250 out = class_2561.method_43470("[").method_27692(class_124.field_1063); // dark gray
      out.method_10852(gradient("TurtMod", GREEN_LIGHT, GREEN_DARK));
      out.method_10852(class_2561.method_43470("] ").method_27692(class_124.field_1063));
      return out;
   }

   /** Build a message with the branded prefix followed by {@code body}. */
   public static class_5250 message(class_2561 body) {
      return prefix().method_10852(body);
   }

   /** A string rendered with a per-character colour gradient from {@code from} to {@code to} (RGB). */
   public static class_5250 gradient(String text, int from, int to) {
      class_5250 out = class_2561.method_43473();
      int n = Math.max(1, text.length() - 1);
      for (int i = 0; i < text.length(); i++) {
         float t = (float)i / (float)n;
         out.method_10852(class_2561.method_43470(String.valueOf(text.charAt(i))).method_54663(lerpColor(from, to, t)));
      }
      return out;
   }

   private static int lerpColor(int a, int b, float t) {
      int ar = a >> 16 & 255, ag = a >> 8 & 255, ab = a & 255;
      int br = b >> 16 & 255, bg = b >> 8 & 255, bb = b & 255;
      int r = Math.round(ar + (br - ar) * t);
      int g = Math.round(ag + (bg - ag) * t);
      int bl = Math.round(ab + (bb - ab) * t);
      return r << 16 | g << 8 | bl;
   }
}
