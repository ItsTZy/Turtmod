package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.PingColors;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_742;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Appends the player's ping (ms) to their over-head nametag. class_897 = EntityRenderer,
 * method_62426 = getNameTag (returns @Nullable class_2561). Ported from repos/pingnametag-26.1.
 */
@Mixin({class_897.class})
public class EntityNameTagPingMixin {
   @Inject(
      method = {"method_62426"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void turtmod$appendPing(class_1297 entity, CallbackInfoReturnable<class_2561> cir) {
      TurtModConfig config = TurtModClient.getConfig();
      class_2561 original = cir.getReturnValue();
      if (config == null || !config.misc.enabled || !config.visual.pingOnNametag) {
         return;
      }
      if (original == null || !(entity instanceof class_742 player)) {
         return;
      }
      class_310 client = class_310.method_1551();
      if (client == null || client.method_1562() == null) {
         return;
      }
      class_634 handler = client.method_1562();
      UUID id = player.method_5667();
      class_640 listEntry = handler.method_2871(id);
      if (listEntry == null) {
         return;
      }
      int latency = listEntry.method_2959();
      int color = config.visual.pingNametagAutoColor ? PingColors.getColor(latency) : 0xFFFFFF;
      class_2561 pingText = class_2561.method_43470(String.format(config.visual.pingNametagFormat, latency)).method_54663(color);

      class_5250 result = class_2561.method_43473();
      if (config.visual.pingNametagPosition == TurtModConfig.PingTextPosition.LEFT) {
         result.method_10852(pingText).method_10852(class_2561.method_43470(" ")).method_10852(original);
      } else {
         result.method_10852(original).method_10852(class_2561.method_43470(" ")).method_10852(pingText);
      }
      cir.setReturnValue(result);
   }
}
