package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.visual.TintingVertexConsumer;
import net.minecraft.class_1921;
import net.minecraft.class_4588;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_1921.class})
public abstract class GlintTranslucentMixin {
   @ModifyVariable(
      method = {"getGlintTranslucent"},
      at = @At("RETURN"),
      argsOnly = true
   )
   private static class_4588 turtmod$tintGlintTranslucent(class_4588 original) {
      TurtModConfig cfg = TurtModClient.getConfig();
      return cfg != null && cfg.misc.enabled && cfg.visual.recolorEnchantGlint ? TintingVertexConsumer.wrapIfNeeded(original, cfg.visual.enchantGlintColor) : original;
   }
}
