package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;

/**
 * Formerly drove time-animated "chroma" by writing a sampled colour into each field's int every frame. That
 * animation was removed at user request (it read as flickering). Gradients are now purely spatial + static;
 * see {@link #tick} and {@link GradientBoxRenderer}. Kept as a no-op so the client tick wiring is undisturbed.
 */
public final class GradientRuntime {
   private GradientRuntime() {
   }

   public static void tick(TurtModConfig cfg) {
      // Intentionally a no-op. Gradients are now STATIC (no time animation) per user request — the flickering
      // "colour changes every time you look at it" chroma has been removed everywhere. Spatial gradients are
      // rendered in place instead: hitboxes + block outline via GradientBoxRenderer (vertical per-vertex blend),
      // HUD text/background via CustomThemeRenderer, hit-colour via the overlay texture. Each field's plain int
      // holds the first stop as its solid fallback (written by the picker), so nothing needs per-frame updates.
   }
}
