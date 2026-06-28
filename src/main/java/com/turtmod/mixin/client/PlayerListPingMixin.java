package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.PingColors;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Faithful "number next to bars" tab-list ping (repos/better-ping-display-fabric-1.21.x).
 * class_355 = PlayerTabOverlay, method_1919 = render, method_1923 = renderLatencyIcon.
 *
 * <p>Widens the per-player slot only when the feature is on, then redirects the latency-icon
 * call to draw a colored "<ms>" before still rendering the vanilla bars via
 * {@link PlayerListPingInvoker}.
 */
@Mixin({class_355.class})
public abstract class PlayerListPingMixin {
   @Unique
   private static final int TURTMOD_PING_SLOT_EXTRA = 45;
   @Unique
   private static final int TURTMOD_PING_TEXT_OFFSET = 13;

   @ModifyConstant(
      method = {"method_1919"},
      constant = {@Constant(intValue = 13)},
      require = 0
   )
   private int turtmod$widenSlot(int original) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.hud.pingInTab) {
         return original + TURTMOD_PING_SLOT_EXTRA;
      }
      return original;
   }

   @Redirect(
      method = {"method_1919"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_355;method_1923(Lnet/minecraft/class_332;IIILnet/minecraft/class_640;)V"
      ),
      require = 0
   )
   private void turtmod$pingIcon(class_355 instance, class_332 context, int width, int x, int y, class_640 entry) {
      TurtModConfig config = TurtModClient.getConfig();
      class_310 client = class_310.method_1551();
      if (config == null || !config.misc.enabled || !config.hud.pingInTab || client == null) {
         ((PlayerListPingInvoker)instance).turtmod$renderLatencyIcon(context, width, x, y, entry);
         return;
      }
      int latency = entry.method_2959();
      String pingString = String.format(config.hud.pingTabFormat, latency);
      int textWidth = client.field_1772.method_1727(pingString);
      int color = config.hud.pingTabAutoColor ? 0xFF000000 | PingColors.getColor(latency) : config.hud.pingTabColor;
      int textX = width + x - textWidth - TURTMOD_PING_TEXT_OFFSET;
      context.method_25303(client.field_1772, pingString, textX, y, color);
      ((PlayerListPingInvoker)instance).turtmod$renderLatencyIcon(context, width, x, y, entry);
   }
}
