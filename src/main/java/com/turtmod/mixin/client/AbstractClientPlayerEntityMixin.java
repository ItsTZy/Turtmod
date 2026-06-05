package com.turtmod.mixin.client;

import com.turtmod.cosmetics.CosmeticManager;
import net.minecraft.class_12079;
import net.minecraft.class_2960;
import net.minecraft.class_742;
import net.minecraft.class_7920;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_742.class})
public abstract class AbstractClientPlayerEntityMixin {
   @Inject(
      method = {"method_52810"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void turtmod$overrideSkinTexture(CallbackInfoReturnable<class_8685> cir) {
      class_2960 skinId = null;
      if (CosmeticManager.isUiPreviewMode()) {
         skinId = CosmeticManager.getPreviewSkinTexture();
      }

      if (skinId != null) {
         class_12079.class_10726 bodyAsset = new class_12079.class_10726(skinId);
         cir.setReturnValue(new class_8685(bodyAsset, (class_12079.class_12081)null, (class_12079.class_12081)null, class_7920.field_41123, true));
      }

   }
}
