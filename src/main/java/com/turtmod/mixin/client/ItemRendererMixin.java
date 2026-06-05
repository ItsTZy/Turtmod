package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.visual.TintingVertexConsumer;
import net.minecraft.class_4588;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({class_918.class})
public abstract class ItemRendererMixin {
   @ModifyArg(
      method = {"method_23181"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4720;method_24037(Lnet/minecraft/class_4588;Lnet/minecraft/class_4588;)Lnet/minecraft/class_4588;"
),
      index = 0,
      require = 0
   )
   private static class_4588 turtmod$tintItemGlint0(class_4588 original) {
      return turtmod$tintGlintConsumer(original);
   }

   @ModifyArg(
      method = {"method_23181"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4720;method_24037(Lnet/minecraft/class_4588;Lnet/minecraft/class_4588;)Lnet/minecraft/class_4588;"
),
      index = 1,
      require = 0
   )
   private static class_4588 turtmod$tintItemGlint1(class_4588 original) {
      return turtmod$tintGlintConsumer(original);
   }

   @ModifyArg(
      method = {"getArmorGlintConsumer"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4720;method_24037(Lnet/minecraft/class_4588;Lnet/minecraft/class_4588;)Lnet/minecraft/class_4588;"
),
      index = 0,
      require = 0
   )
   private static class_4588 turtmod$tintArmorGlint0(class_4588 original) {
      return turtmod$tintGlintConsumer(original);
   }

   @ModifyArg(
      method = {"getArmorGlintConsumer"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4720;method_24037(Lnet/minecraft/class_4588;Lnet/minecraft/class_4588;)Lnet/minecraft/class_4588;"
),
      index = 1,
      require = 0
   )
   private static class_4588 turtmod$tintArmorGlint1(class_4588 original) {
      return turtmod$tintGlintConsumer(original);
   }

   private static class_4588 turtmod$tintGlintConsumer(class_4588 original) {
      TurtModConfig cfg = TurtModClient.getConfig();
      return cfg != null && cfg.misc.enabled && cfg.visual.recolorEnchantGlint ? TintingVertexConsumer.wrapIfNeeded(original, cfg.visual.enchantGlintColor) : original;
   }
}
