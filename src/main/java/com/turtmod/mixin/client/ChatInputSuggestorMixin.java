package com.turtmod.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.turtmod.chat.BetterChatFeature;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_2561;
import net.minecraft.class_342;
import net.minecraft.class_4717;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4717.class})
public abstract class ChatInputSuggestorMixin {
   @Shadow
   @Final
   class_342 field_21599;
   @Shadow
   private CompletableFuture<Suggestions> field_21611;

   @Shadow
   protected abstract void method_23920(boolean var1);

   @Inject(
      method = {"method_23934"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$onRefresh(CallbackInfo ci) {
      String text = this.field_21599.method_1882();
      int cursor = this.field_21599.method_1881();
      String textBeforeCursor = text.substring(0, cursor);
      int lastColonIndex = textBeforeCursor.lastIndexOf(58);
      if (lastColonIndex != -1 && (lastColonIndex == 0 || textBeforeCursor.charAt(lastColonIndex - 1) == ' ')) {
         String partialEmoji = textBeforeCursor.substring(lastColonIndex);
         if (!partialEmoji.contains(" ")) {
            SuggestionsBuilder builder = new SuggestionsBuilder(text, lastColonIndex);
            boolean hasSuggestions = false;

            for(String[] replacement : BetterChatFeature.getEmojiReplacements()) {
               String code = replacement[0];
               String emoji = replacement[1];
               if (code.startsWith(partialEmoji.toLowerCase())) {
                  builder.suggest(code, class_2561.method_43470(emoji));
                  hasSuggestions = true;
               }
            }

            if (hasSuggestions) {
               this.field_21611 = builder.buildFuture();
               this.method_23920(true);
               ci.cancel();
            }
         }
      }

   }
}
