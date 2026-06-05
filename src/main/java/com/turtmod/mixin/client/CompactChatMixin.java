package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_338;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_338.class})
public abstract class CompactChatMixin {
   private static final Pattern COMPACT_PATTERN = Pattern.compile(" x(\\d+)$");
   private static boolean turtmod$compacting = false;
   @Shadow
   @Final
   private List<class_303> field_2061;
   @Shadow
   @Final
   private List<class_303.class_7590> field_2064;

   @Shadow
   public abstract void method_44811(class_2561 var1, @Nullable class_7469 var2, @Nullable class_7591 var3);

   @Inject(
      method = {"method_44811"},
      at = {@At("HEAD")},
      require = 0
   )
   private void turtmod$captureHistory(class_2561 message, @Nullable class_7469 signature, @Nullable class_7591 indicator, CallbackInfo ci) {
      if (!turtmod$compacting && message != null) {
         com.turtmod.chat.BetterChatFeature.recordHistory(message.getString());
      }
   }

   @Inject(
      method = {"method_44811"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$compactChat(class_2561 message, @Nullable class_7469 signature, @Nullable class_7591 indicator, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (!turtmod$compacting && config != null && config.misc.enabled && config.hud.compactChat && !this.field_2061.isEmpty()) {
         int distance = this.resolveAttemptDistance(config.hud.compactChatDistance);
         int total = 1;
         boolean foundDuplicate = false;

         for(int i = 0; i < distance && i < this.field_2061.size(); ++i) {
            class_2561 previous = ((class_303)this.field_2061.get(i)).comp_893();
            if (isSameMessage(message, previous, config.hud.compactChatCheckStyle)) {
               total += extractCompactCount(previous.getString());
               this.removeVisibleMessage(i);
               this.field_2061.remove(i);
               foundDuplicate = true;
               --i;
               --distance;
            }
         }

         if (foundDuplicate && total > 1) {
            class_2561 merged = message.method_27661().method_10852(class_2561.method_43470(" x" + total).method_27692(class_124.field_1080));

            try {
               turtmod$compacting = true;
               this.method_44811(merged, signature, indicator);
            } finally {
               turtmod$compacting = false;
            }

            ci.cancel();
         }
      }
   }

   private static boolean isSameMessage(class_2561 incoming, class_2561 previous, boolean checkStyle) {
      String incomingContent = extractComparableContent(incoming);
      String previousContent = extractComparableContent(previous);
      if (!incomingContent.equalsIgnoreCase(previousContent)) {
         return false;
      } else {
         return !checkStyle || incoming.method_10866().equals(previous.method_10866());
      }
   }

   private int resolveAttemptDistance(int configuredDistance) {
      return configuredDistance == 0 ? Math.max(1, this.field_2064.size()) : Math.max(1, Math.min(Math.abs(configuredDistance), this.field_2061.size()));
   }

   private void removeVisibleMessage(int messageIndex) {
      int visibleIndex = this.messageToVisibleIndex(messageIndex);
      if (visibleIndex < 0) {
         if (messageIndex < this.field_2064.size()) {
            this.field_2064.remove(messageIndex);
         }

      } else {
         do {
            this.field_2064.remove(visibleIndex);
         } while(visibleIndex < this.field_2064.size() && !((class_303.class_7590)this.field_2064.get(visibleIndex)).comp_898());

      }
   }

   private int messageToVisibleIndex(int messageIndex) {
      int endOfEntryIndex = -1;

      for(int i = 0; i < this.field_2064.size(); ++i) {
         if (((class_303.class_7590)this.field_2064.get(i)).comp_898()) {
            ++endOfEntryIndex;
            if (endOfEntryIndex == messageIndex) {
               return i;
            }
         }
      }

      return -1;
   }

   private static String extractComparableContent(class_2561 text) {
      String raw = normalizeWhitespace(stripCompactSuffix(text.getString()));
      if (raw.isEmpty()) {
         return "";
      } else {
         int colonIndex = raw.indexOf(": ");
         if (colonIndex > 0 && colonIndex < 96) {
            return normalizeWhitespace(raw.substring(colonIndex + 2));
         } else {
            int angleIndex = raw.lastIndexOf("> ");
            return angleIndex >= 0 && angleIndex + 2 < raw.length() ? normalizeWhitespace(raw.substring(angleIndex + 2)) : raw;
         }
      }
   }

   private static String normalizeWhitespace(String value) {
      return value == null ? "" : value.trim().replaceAll("\\s+", " ");
   }

   private static int extractCompactCount(String value) {
      Matcher matcher = COMPACT_PATTERN.matcher(value);
      if (matcher.find()) {
         try {
            return Integer.parseInt(matcher.group(1));
         } catch (NumberFormatException var3) {
            return 1;
         }
      } else {
         return 1;
      }
   }

   private static String stripCompactSuffix(String value) {
      Matcher matcher = COMPACT_PATTERN.matcher(value);
      return matcher.find() ? value.substring(0, matcher.start()) : value;
   }
}
