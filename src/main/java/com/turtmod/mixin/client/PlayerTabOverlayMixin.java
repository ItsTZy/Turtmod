package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.combat.PotionThrowTracker;
import com.turtmod.combat.TotemPopTracker;
import com.turtmod.config.TurtModConfig;
import java.util.UUID;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Appends totem-pop / potion-throw counts to player names in the tab list (class_355 =
 * PlayerTabOverlay, method_1918 = getNameForDisplay). Wires the previously-dead
 * {@code totemShowInTab} / {@code potionThrowShowInTab} options. Ported from
 * repos/potcounter-1.21.11 MixinPlayerTabOverlay.
 */
@Mixin({class_355.class})
public abstract class PlayerTabOverlayMixin {
   @Inject(
      method = {"method_1918"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void turtmod$appendCounts(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
      TurtModConfig config = TurtModClient.getConfig();
      class_310 client = class_310.method_1551();
      if (config == null || !config.misc.enabled || client == null || client.field_1687 == null) {
         return;
      }
      boolean showTotem = config.combat.totemShowInTab;
      boolean showPots = config.combat.potionThrowShowInTab;
      if (!showTotem && !showPots) {
         return;
      }
      UUID id = entry.method_2966().id();
      class_1657 player = client.field_1687.method_18470(id);
      if (player == null) {
         return;
      }
      class_2561 text = cir.getReturnValue();
      if (showTotem) {
         text = TotemPopTracker.appendPops(player, text);
      }
      if (showPots) {
         text = PotionThrowTracker.appendPots(player, text);
      }
      cir.setReturnValue(text);
   }
}
