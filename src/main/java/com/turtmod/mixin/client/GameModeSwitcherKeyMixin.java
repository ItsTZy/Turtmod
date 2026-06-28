package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_11908;
import net.minecraft.class_5289;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Companion to {@link GameModeSwitcherMixin} (ported from PersistentGamemodeSwitcher): vanilla only
 * opens the F3+F4 gamemode switcher if you hold the change-gamemode permission, otherwise it shows
 * {@code debug.gamemodes.error}. When the no-op switcher is enabled we intercept the debug-key
 * handler and open the switcher ourselves, skipping the gate. The actual change is then routed
 * through {@code /gamemode} by {@link GameModeSwitcherMixin} (which still needs server-side
 * {@code /gamemode} permission to take effect).
 *
 * <p>class_309 = KeyboardHandler; method_1468 = handleDebugKeys(KeyEvent)->boolean;
 * class_315.field_63482 = the "switch game mode" keybind.
 */
@Mixin(class_309.class)
public class GameModeSwitcherKeyMixin {
   @Inject(method = "method_1468", at = @At("HEAD"), cancellable = true)
   private void turtmod$openSwitcherWithoutPerm(class_11908 event, CallbackInfoReturnable<Boolean> cir) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.misc.noOpGamemodeSwitcher) {
         return;
      }
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1755 != null) {
         return;
      }
      if (mc.field_1690.field_63482.method_1417(event)) {
         mc.method_1507(new class_5289());
         cir.setReturnValue(true);
      }
   }
}
