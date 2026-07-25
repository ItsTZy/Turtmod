package com.turtmod.mixin.client;

import com.turtmod.config.TurtModMainMenuScreen;
import com.turtmod.cosmetics.CosmeticsScreen;
import com.turtmod.cosmetics.SkinPreviewButton;
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
      // Left of the vanilla button block: the screenshot gallery, with the labelled Skin Changer to its left.
      int col = this.field_22789 / 2 - 124;
      com.turtmod.config.TurtModConfig cfg = com.turtmod.TurtModClient.getConfig();
      if (cfg == null || cfg.hud.screenshotMenuButton) {
         this.method_37063(class_4185.method_46430(class_2561.method_43470("📸"), (button) -> this.field_22787.method_1507(new ScreenshotGalleryScreen(this))).method_46434(col, y, 20, 20).method_46431());
      }
      // Skin Changer: a wider labelled button to the LEFT of the gallery button, with a live skin preview above.
      if (cfg == null || cfg.hud.skinChangerMenuButton) {
         int gap = 4;
         int skinW = 96;
         int skinX = col - gap - skinW;
         if (skinX < 4) {                     // not enough room at full width: keep the gap, shrink to fit
            skinW = Math.max(20, col - gap - 4);
            skinX = 4;
         }
         this.method_37063(new SkinPreviewButton(skinX, y, skinW, 20,
            (button) -> this.field_22787.method_1507(new CosmeticsScreen(this))));
      }
   }
}
