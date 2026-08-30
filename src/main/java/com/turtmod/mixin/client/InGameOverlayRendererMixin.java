package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2464;
import net.minecraft.class_2680;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({class_4603.class})
public abstract class InGameOverlayRendererMixin {
   // NOTE: In 1.21.11 the carved-pumpkin blur and powder-snow frosted border render
   // through Gui (class_329) — handled by InGameHudMixin. The methods here
   // (method_23068 / method_24225) are the IN-WALL (suffocation) camera overlay only.
   // We only suppress the in-wall overlay when the player is buried in a powder-snow
   // block AND the powder-snow overlay is disabled; everything else stays vanilla.
   @Redirect(
      method = {"method_23067"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4603;method_24225(Lnet/minecraft/class_1657;)Lnet/minecraft/class_2680;"
      ),
      require = 0
   )
   private class_2680 turtmod$filterOverlayBlockState(class_1657 player) {
      class_2680 state = this.turtmod$approximateInWallBlockState(player);
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.disablePowderSnowOverlay
            && state != null && state.method_27852(class_2246.field_27879)) {
         return null;
      }
      return state;
   }

   private class_2680 turtmod$approximateInWallBlockState(class_1657 player) {
      class_2338 pos = class_2338.method_49637(player.method_23317(), player.method_23320(), player.method_23321());
      class_2680 state = player.method_73183().method_8320(pos);
      if (state.method_26217() == class_2464.field_11455) {
         return null;
      } else {
         return state.method_26230(player.method_73183(), pos) ? state : null;
      }
   }
}
