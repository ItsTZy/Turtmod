package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.ScrollableTooltipState;
import net.minecraft.class_332;
import net.minecraft.class_8000;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scrollable tooltips: when a tooltip is taller than the screen, offset it by the mouse-wheel scroll (fed by
 * {@link com.turtmod.mixin.client.TooltipScrollScreenMixin}). Every tooltip funnels through
 * {@code class_332.method_51435} which asks the positioner ({@code class_8000.method_47944}) for its top-left;
 * we take that result and shift the Y by the clamped scroll amount.
 */
@Mixin(class_332.class)
public abstract class ScrollableTooltipMixin {
   @Redirect(
      method = "method_51435",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_8000;method_47944(IIIIII)Lorg/joml/Vector2ic;"),
      require = 0
   )
   private Vector2ic turtmod$scrollTooltip(class_8000 positioner, int screenW, int screenH, int mouseX, int mouseY, int tooltipW, int tooltipH) {
      Vector2ic pos = positioner.method_47944(screenW, screenH, mouseX, mouseY, tooltipW, tooltipH);
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.hud.scrollableTooltips) {
         return pos;
      }
      int y = ScrollableTooltipState.applyAndClamp(pos.y(), tooltipH, screenH);
      return new Vector2i(pos.x(), y);
   }
}
