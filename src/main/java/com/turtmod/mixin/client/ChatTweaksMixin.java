package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Chat Tweaks: keep more chat history than vanilla. The chat hud ({@code class_338}) trims its wrapped
 * display lines to 100 in {@code method_1815} and its stored messages to 100 in {@code method_58744};
 * we raise both caps to the configured length. Interaction (link clicks, scrolling) is unaffected — only
 * how much is retained changes.
 */
@Mixin(class_338.class)
public abstract class ChatTweaksMixin {
   @ModifyConstant(method = {"method_1815", "method_58744"}, constant = @Constant(intValue = 100))
   private int turtmod$chatHistory(int original) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg != null && cfg.misc.enabled && cfg.hud.chatTweaksEnabled) {
         return Math.max(100, cfg.hud.chatHistoryLength);
      }
      return original;
   }
}
