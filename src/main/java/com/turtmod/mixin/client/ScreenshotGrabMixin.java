package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.chat.ScreenshotPreview;
import com.turtmod.config.TurtModConfig;
import java.io.File;
import java.util.function.Consumer;
import net.minecraft.class_276;
import net.minecraft.class_2561;
import net.minecraft.class_318;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Grabs a copy of the framebuffer whenever a screenshot is saved (F2), feeding it to the animated
 * corner {@link ScreenshotPreview}. Non-cancelling — vanilla still writes the file and posts its chat
 * line; we just additionally capture an image for the preview. class_318 = Screenshot,
 * method_1659 = grab(File, RenderTarget, Consumer), method_1663 = takeScreenshot.
 */
@Mixin(class_318.class)
public class ScreenshotGrabMixin {
   @Inject(
      method = "method_1659(Ljava/io/File;Lnet/minecraft/class_276;Ljava/util/function/Consumer;)V",
      at = @At("HEAD"),
      require = 0
   )
   private static void turtmod$capturePreview(File gameDirectory, class_276 framebuffer, Consumer<class_2561> messageReceiver, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled || !config.hud.screenshotPreview || framebuffer == null) {
         return;
      }
      class_318.method_1663(framebuffer, ScreenshotPreview::onCaptured);
   }
}
