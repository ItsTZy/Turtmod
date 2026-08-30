package com.turtmod.ui;

import java.awt.Color;

/**
 * Single source of truth for TurtMod's UI colours.
 *
 * <p>Historically the same colours were re-declared (as raw decimal {@code new Color(...)} literals)
 * in {@link TurtLauncher}, {@code TurtModClientConfigScreen}, {@link ClientUiTheme} and several other
 * screens. That made re-theming a magic-number hunt across many files. Everything visual should now
 * reference these constants so a palette change happens in exactly one place.
 *
 * <p>Values are kept byte-identical to the previous literals so the look is unchanged; only the
 * source of truth moved here.
 */
public final class Palette {
   private Palette() {
   }

   // ── Brand accents (opaque) ────────────────────────────────────────────────
   /** Primary accent — turtle green. */
   public static final Color GREEN = new Color(9289311, false);
   /** Secondary accent — pink. */
   public static final Color PINK = new Color(16752046, false);
   /** Primary readable text. */
   public static final Color TEXT = new Color(16775399, false);

   // ── Surfaces ──────────────────────────────────────────────────────────────
   /** Panel / card background. */
   public static final Color PANEL_BG = new Color(1709588, true);
   /** Panel / card border. */
   public static final Color PANEL_BORDER = new Color(9289311, true);
   /** Default button fill. */
   public static final Color BTN_BG = new Color(2433054, true);
   /** Button / row hover fill. */
   public static final Color BTN_HOVER = new Color(3482400, true);

   // ── Inputs ──────────────────────────────────────────────────────────────-─
   /** Search / text-field background. */
   public static final Color SEARCH_BG = new Color(2958626, true);
   /** Search / text-field idle border. */
   public static final Color SEARCH_BORDER = new Color(7052869, true);

   // ── Cards / rows ─────────────────────────────────────────────────────────-─
   /** Clean module-card surface (idle). */
   public static final Color CARD_BG = new Color(0xFF14181F, true);
   /** Module-card surface on hover. */
   public static final Color CARD_HOVER = new Color(0xFF1D232E, true);
   /** Hairline card border. */
   public static final Color CARD_BORDER = new Color(255, 255, 255, 20);
   /** Dimmed label text (module is off). */
   public static final Color TEXT_MUTED = new Color(150, 156, 168);
   /** Toggle-switch track when off. */
   public static final Color TOGGLE_OFF = new Color(255, 255, 255, 38);
   /** Toggle-switch knob. */
   public static final Color TOGGLE_KNOB = new Color(245, 245, 248);

   // ── Helpers ────────────────────────────────────────────────────────────────
   /** Same colour at a new alpha (0-255). */
   public static Color alpha(Color c, int a) {
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
   }

   /** A {@link TurtUITheme} built from the standard panel/accent colours. */
   public static TurtUITheme panelTheme() {
      return new TurtUITheme(PANEL_BG, PANEL_BORDER, TEXT, BTN_HOVER, GREEN);
   }

   /** A {@link TurtUITheme} for buttons (pink highlight). */
   public static TurtUITheme buttonTheme() {
      return new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT, BTN_HOVER, PINK);
   }
}
