package com.turtmod.mixin.client;

import com.turtmod.chat.BetterChatFeature;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_408.class})
public abstract class ChatScreenMixin {
   @ModifyVariable(
      method = {"method_44056"},
      at = @At("STORE"),
      ordinal = 0
   )
   private String turtmod$applyChatEmojis(String message) {
      return BetterChatFeature.replaceEmojis(message);
   }
}
