package com.turtmod.mixin.client;

import com.turtmod.config.TurtModMainMenuScreen;
import com.turtmod.gallery.ScreenshotGalleryScreen;
import com.turtmod.ui.TurtLogoButton;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_433;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_433.class})
public abstract class GameMenuScreenMixin extends class_437 {
   protected GameMenuScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(
      method = {"method_20543"},
      at = {@At("TAIL")}
   )
   private void turtmod$addTurtModButton(CallbackInfo ci) {
      int x = this.field_22789 / 2 + 104;
      int y = this.field_22790 / 4 + 112;
      this.method_37063(new TurtLogoButton(x, y, 20, 20, (button) -> this.field_22787.method_1507(new TurtModMainMenuScreen(this))));
      com.turtmod.config.TurtModConfig cfg = com.turtmod.TurtModClient.getConfig();
      if (cfg == null || cfg.hud.screenshotMenuButton) {
         this.method_37063(class_4185.method_46430(class_2561.method_43470("\ud83d\udcf8"), (button) -> this.field_22787.method_1507(new ScreenshotGalleryScreen(this))).method_46434(this.field_22789 / 2 - 124, y, 20, 20).method_46431());
      }
   }
}
