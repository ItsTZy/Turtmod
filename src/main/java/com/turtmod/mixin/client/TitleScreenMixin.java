package com.turtmod.mixin.client;

import com.turtmod.config.TurtModMainMenuScreen;
import com.turtmod.cosmetics.CosmeticsScreen;
import com.turtmod.cosmetics.SkinPreviewButton;
import com.turtmod.gallery.ScreenshotGalleryScreen;
import com.turtmod.ui.TurtLogoButton;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_442.class})
public class TitleScreenMixin extends class_437 {
   @Shadow
   @Final
   private boolean field_18222;

   protected TitleScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("TAIL")}
   )
   private void turtmod$addTurtModButton(CallbackInfo ci) {
      int x = this.field_22789 / 2 + 104;
      int y = this.field_22790 / 4 + 48 + 48;
      this.method_37063(new TurtLogoButton(x, y, 20, 20, (button) -> this.field_22787.method_1507(new TurtModMainMenuScreen(this))));
      int col = this.field_22789 / 2 - 124;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("📸"), (button) -> this.field_22787.method_1507(new ScreenshotGalleryScreen(this))).method_46434(col, y, 20, 20).method_46431());
      com.turtmod.config.TurtModConfig cfg = com.turtmod.TurtModClient.getConfig();
      if (cfg == null || cfg.hud.skinChangerMenuButton) {
         int skinW = 96;
         this.method_37063(new SkinPreviewButton(col - 4 - skinW, y, skinW, 20,
            (button) -> this.field_22787.method_1507(new CosmeticsScreen(this))));
      }
   }
}
