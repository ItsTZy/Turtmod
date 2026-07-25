package com.turtmod.hud;

/**
 * Tracks the vertical scroll offset for the tooltip currently on screen (Lunar-style scrollable tooltips).
 * The tooltip render adds {@link #applyAndClamp} to its Y; the screen's scroll handler feeds
 * {@link #addScroll}. The offset auto-resets once no tooltip has been drawn for a moment (i.e. you moved to
 * a different item or closed the screen).
 */
public final class ScrollableTooltipState {
   private static int scrollY = 0;
   private static long lastShownNanos = 0L;
   private static final long IDLE_RESET_NS = 250_000_000L;   // reset after ~0.25s with no tooltip

   private ScrollableTooltipState() {
   }

   /** Called from the tooltip render: reset if the previous tooltip vanished, clamp, and return the new Y. */
   public static int applyAndClamp(int baseY, int tooltipHeight, int screenHeight) {
      long now = System.nanoTime();
      if (now - lastShownNanos > IDLE_RESET_NS) {
         scrollY = 0;
      }
      lastShownNanos = now;
      // How far the tooltip spills past the bottom of the screen; only that much upward scroll is allowed.
      int overflow = Math.max(0, baseY + tooltipHeight - (screenHeight - 4));
      if (scrollY < -overflow) scrollY = -overflow;
      if (scrollY > 0) scrollY = 0;
      return baseY + scrollY;
   }

   /** Wheel up (vertical &gt; 0) reveals the lower part of the tooltip (moves content up). */
   public static void addScroll(double vertical) {
      scrollY -= (int) Math.round(vertical * 12.0);
   }

   /** True while a tooltip was drawn very recently (so the wheel should scroll it, not the screen behind). */
   public static boolean active() {
      return System.nanoTime() - lastShownNanos < 200_000_000L;
   }
}
