package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({class_765.class})
public abstract class LightmapTextureManagerMixin {
   @ModifyArg(
      method = {"method_3313"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
   ordinal = 0
),
      index = 0,
      require = 0
   )
   private float turtmod$fullbrightAmbient(float original) {
      return isFullbrightEnabled() ? 1.0F : original;
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
   ordinal = 1
),
      index = 0,
      require = 0
   )
   private float turtmod$fullbrightSkyBlend(float original) {
      return isFullbrightEnabled() ? 1.0F : original;
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
   ordinal = 3
),
      index = 0,
      require = 0
   )
   private float turtmod$fullbrightNightVision(float original) {
      return isFullbrightEnabled() ? 1.0F : original;
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
   ordinal = 4
),
      index = 0,
      require = 0
   )
   private float turtmod$fullbrightDarkness(float original) {
      return isFullbrightEnabled() ? 0.0F : original;
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
   ordinal = 5
),
      index = 0,
      require = 0
   )
   private float turtmod$fullbrightDarkenWorld(float original) {
      return isFullbrightEnabled() ? 0.0F : original;
   }

   @ModifyArg(
      method = {"method_3313"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
   ordinal = 6
),
      index = 0,
      require = 0
   )
   private float turtmod$fullbrightDarknessReduction(float original) {
      return isFullbrightEnabled() ? 0.0F : original;
   }

   private static boolean isFullbrightEnabled() {
      TurtModConfig config = TurtModClient.getConfig();
      return config != null && config.misc.enabled && config.visual.fullbright.enabled;
   }
}
