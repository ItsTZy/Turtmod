package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_12155;
import net.minecraft.class_12178;
import net.minecraft.class_12179;
import net.minecraft.class_238;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_12155.class)
public abstract class DebugHitboxCleanupMixin {
   @Redirect(
      method = "method_75432",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_12180;method_75541(Lnet/minecraft/class_238;Lnet/minecraft/class_12179;)Lnet/minecraft/class_12178;",
         ordinal = 1
      )
   )
   private class_12178 turtmod$hideRidingIndicator(class_238 box, class_12179 style) {
      return this.turtmod$shouldCleanDebugHitboxes() ? null : net.minecraft.class_12180.method_75541(box, style);
   }

   @Redirect(
      method = "method_75432",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_12180;method_75541(Lnet/minecraft/class_238;Lnet/minecraft/class_12179;)Lnet/minecraft/class_12178;",
         ordinal = 2
      )
   )
   private class_12178 turtmod$hideEyeHeight(class_238 box, class_12179 style) {
      return this.turtmod$shouldCleanDebugHitboxes() ? null : net.minecraft.class_12180.method_75541(box, style);
   }

   @Redirect(
      method = "method_75432",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_12180;method_75555(Lnet/minecraft/class_243;Lnet/minecraft/class_243;I)Lnet/minecraft/class_12178;",
         ordinal = 0
      )
   )
   private class_12178 turtmod$hideLookVector(net.minecraft.class_243 from, net.minecraft.class_243 to, int color) {
      return this.turtmod$shouldCleanDebugHitboxes() ? null : net.minecraft.class_12180.method_75555(from, to, color);
   }

   @Redirect(
      method = "method_75432",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_12180;method_75555(Lnet/minecraft/class_243;Lnet/minecraft/class_243;I)Lnet/minecraft/class_12178;",
         ordinal = 1
      )
   )
   private class_12178 turtmod$hideVelocityArrow(net.minecraft.class_243 from, net.minecraft.class_243 to, int color) {
      return this.turtmod$shouldCleanDebugHitboxes() ? null : net.minecraft.class_12180.method_75555(from, to, color);
   }

   private boolean turtmod$shouldCleanDebugHitboxes() {
      TurtModConfig config = TurtModClient.getConfig();
      return config != null && config.misc.enabled && config.hud.cleanDebugHitboxes;
   }
}
