package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;

/**
 * Formerly drove time-animated "chroma" by writing a sampled colour into each field's int every frame. That
 * animation was removed at user request (it read as flickering). The only gradients left are the HUD
 * text/accent/background ones, which render spatially in CustomThemeRenderer. Kept as a no-op so the client
 * tick wiring is undisturbed.
 */
public final class GradientRuntime {
   private GradientRuntime() {
   }

   public static void tick(TurtModConfig cfg) {
      // Intentionally a no-op — gradients are static and rendered in place (HUD text/background in
      // CustomThemeRenderer). Nothing needs per-frame colour updates.
   }
}
