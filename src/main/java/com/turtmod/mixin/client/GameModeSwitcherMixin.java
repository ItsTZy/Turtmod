package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_11410;
import net.minecraft.class_12090;
import net.minecraft.class_12096;
import net.minecraft.class_2596;
import net.minecraft.class_310;
import net.minecraft.class_5289;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * "No-op gamemode switcher" apply path (ported from PersistentGamemodeSwitcher). The switcher's
 * apply method ({@code class_5289.method_28064}) is gated:
 * <pre>if (target != current &amp;&amp; class_3064.field_63167.method_75022(player.method_75004())) sendPacket(...)</pre>
 * On a server where the client doesn't locally know it has the change-gamemode permission (e.g. a
 * permission plugin grants {@code /gamemode} without vanilla op), that check is false, so nothing is
 * sent. We therefore:
 * <ol>
 *   <li>redirect {@code method_75022} (the permission check) to true so the send branch is reached;</li>
 *   <li>redirect the packet send to instead run {@code /gamemode <mode>} as a command, which the
 *       server authorises normally.</li>
 * </ol>
 * Companion {@link GameModeSwitcherKeyMixin} bypasses the equivalent gate that opens the screen.
 */
@Mixin(class_5289.class)
public class GameModeSwitcherMixin {
   @Redirect(
      method = "method_28064",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_12090;method_75022(Lnet/minecraft/class_12096;)Z"
      )
   )
   private static boolean turtmod$bypassPermission(class_12090 permission, class_12096 source) {
      if (enabled()) {
         return true;
      }
      return permission.method_75022(source);
   }

   @Redirect(
      method = "method_28064",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_634;method_52787(Lnet/minecraft/class_2596;)V"
      )
   )
   private static void turtmod$switchViaCommand(class_634 handler, class_2596<?> packet) {
      class_310 mc = class_310.method_1551();
      if (enabled() && packet instanceof class_11410 gmPacket
            && mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         mc.field_1724.field_3944.method_45730("gamemode " + gmPacket.comp_4293().method_8381());
         return;
      }
      handler.method_52787(packet);
   }

   private static boolean enabled() {
      TurtModConfig cfg = TurtModClient.getConfig();
      return cfg != null && cfg.misc.enabled && cfg.misc.noOpGamemodeSwitcher;
   }
}
