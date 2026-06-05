package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import net.minecraft.class_5250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_338.class})
public class ChatTimestampMixin {
   private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

   @ModifyVariable(
      method = {"method_44811"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private class_2561 turtmod$prependTimestamp(class_2561 message) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || !config.hud.chatTimestamps) {
         return message;
      }
      String format = config.hud.chatTimestampFormat;
      DateTimeFormatter formatter;
      try {
         formatter = DateTimeFormatter.ofPattern(format);
      } catch (Exception e) {
         formatter = TIMESTAMP_FORMATTER;
      }
      String timeStr = LocalTime.now().format(formatter);
      class_2561 timestamp = class_2561.method_43470("[" + timeStr + "] ").method_27692(class_124.field_1080);
      class_5250 result = class_2561.method_43473();
      result.method_10852(timestamp);
      result.method_10852(message);
      return result;
   }
}
