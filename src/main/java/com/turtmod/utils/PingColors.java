package com.turtmod.utils;

/**
 * Ping-to-color gradient shared by the tab-list ping column and the over-head ping nametag.
 * Ported from better-ping-display / pingnametag: green -> yellow -> red across 0..300 ms,
 * grey for unknown (negative) latency. Returns a 24-bit RGB int; callers OR in 0xFF000000
 * for opaque text rendering.
 */
public final class PingColors {
   public static final int PING_START = 0;
   public static final int PING_MID = 150;
   public static final int PING_END = 300;

   public static final int COLOR_GREY = 0x535353;
   public static final int COLOR_START = 0x00E676;
   public static final int COLOR_MID = 0xD6CD30;
   public static final int COLOR_END = 0xE53935;

   private PingColors() {
   }

   public static int getColor(int ping) {
      if (ping < PING_START) {
         return COLOR_GREY;
      }
      if (ping < PING_MID) {
         return interpolate(COLOR_START, COLOR_MID, computeOffset(PING_START, PING_MID, ping));
      }
      return interpolate(COLOR_MID, COLOR_END, computeOffset(PING_MID, PING_END, Math.min(ping, PING_END)));
   }

   private static float computeOffset(int start, int end, int value) {
      float offset = (float)(value - start) / (float)(end - start);
      if (offset < 0.0F) {
         return 0.0F;
      }
      return Math.min(offset, 1.0F);
   }

   private static int interpolate(int colorStart, int colorEnd, float offset) {
      int rStart = colorStart >> 16 & 0xFF;
      int gStart = colorStart >> 8 & 0xFF;
      int bStart = colorStart & 0xFF;
      int rEnd = colorEnd >> 16 & 0xFF;
      int gEnd = colorEnd >> 8 & 0xFF;
      int bEnd = colorEnd & 0xFF;
      int r = Math.round(rStart + (rEnd - rStart) * offset);
      int g = Math.round(gStart + (gEnd - gStart) * offset);
      int b = Math.round(bStart + (bEnd - bStart) * offset);
      return r << 16 | g << 8 | b;
   }
}
