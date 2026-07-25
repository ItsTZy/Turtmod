package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.ScrollableTooltipState;
import net.minecraft.class_312;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Feeds the mouse wheel into {@link ScrollableTooltipState} while a tooltip is on screen. The Mouse handler
 * ({@code class_312.method_1598}) dispatches scroll to the current screen via {@code class_437.method_25401};
 * when a tooltip is showing we consume that scroll to move the tooltip instead of the screen behind it.
 */
@Mixin(class_312.class)
public abstract class TooltipScrollScreenMixin {
   @Redirect(
      method = "method_1598",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_437;method_25401(DDDD)Z")
   )
   private boolean turtmod$tooltipScroll(class_437 screen, double mouseX, double mouseY, double horizontal, double vertical) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg != null && cfg.misc.enabled && cfg.hud.scrollableTooltips && ScrollableTooltipState.active()) {
         ScrollableTooltipState.addScroll(vertical);
         return true;   // consume — scroll the tooltip, not the list/screen behind it
      }
      return screen.method_25401(mouseX, mouseY, horizontal, vertical);
   }
}
