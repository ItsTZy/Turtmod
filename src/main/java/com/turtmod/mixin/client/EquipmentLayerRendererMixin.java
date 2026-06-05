package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.extension.minecraft.OverlayRendered;
import net.minecraft.class_10197;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({class_10197.class})
public abstract class EquipmentLayerRendererMixin implements OverlayRendered {
   @Unique private static final int TURTMOD_NO_OVERLAY = 655360;
   @Unique private int turtmod$overlayCoords = TURTMOD_NO_OVERLAY;

   // Substitute NO_OVERLAY -> hurt coords so the entity shader (after swap) samples the colored row
   @ModifyExpressionValue(
      method = {"method_64078"},
      at = @At(value = "FIELD", target = "Lnet/minecraft/class_4608;field_21444:I", opcode = Opcodes.GETSTATIC, ordinal = 0),
      require = 0
   )
   private int turtmod$modifyArmorOverlay(int previousCoords) {
      TurtModConfig c = TurtModClient.getConfig();
      return c != null && c.misc.enabled && c.visual.armorDamageTint && this.turtmod$overlayCoords != TURTMOD_NO_OVERLAY
         ? this.turtmod$overlayCoords : previousCoords;
   }

   @ModifyExpressionValue(
      method = {"method_64078"},
      at = @At(value = "FIELD", target = "Lnet/minecraft/class_4608;field_21444:I", opcode = Opcodes.GETSTATIC, ordinal = 1),
      require = 0
   )
   private int turtmod$modifyTrimOverlay1(int previousCoords) {
      TurtModConfig c = TurtModClient.getConfig();
      return c != null && c.misc.enabled && c.visual.armorDamageTint && c.visual.armorDamageTintTrim && this.turtmod$overlayCoords != TURTMOD_NO_OVERLAY
         ? this.turtmod$overlayCoords : previousCoords;
   }

   @ModifyExpressionValue(
      method = {"method_64078"},
      at = @At(value = "FIELD", target = "Lnet/minecraft/class_4608;field_21444:I", opcode = Opcodes.GETSTATIC, ordinal = 2),
      require = 0
   )
   private int turtmod$modifyTrimOverlay2(int previousCoords) {
      TurtModConfig c = TurtModClient.getConfig();
      return c != null && c.misc.enabled && c.visual.armorDamageTint && c.visual.armorDamageTintTrim && this.turtmod$overlayCoords != TURTMOD_NO_OVERLAY
         ? this.turtmod$overlayCoords : previousCoords;
   }

   // KEY FIX: armor_cutout_no_cull shader does NOT sample overlay. Swap it to entity_cutout_no_cull (method_75994) when hurt.
   // method_75966 + method_75964 = armorCutoutNoCull aliases (discovered via runtime probe)
   @WrapOperation(
      method = {"method_64078"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_12249;method_75966(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;"),
      require = 0
   )
   private class_1921 turtmod$swapArmor66(class_2960 id, Operation<class_1921> original) {
      return turtmod$maybeSwap(original.call(id), id, false);
   }

   @WrapOperation(
      method = {"method_64078"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_12249;method_75964(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;"),
      require = 0
   )
   private class_1921 turtmod$swapArmor64(class_2960 id, Operation<class_1921> original) {
      return turtmod$maybeSwap(original.call(id), id, false);
   }

   // method_75973 = armor_decal_cutout_no_cull (trim)
   @WrapOperation(
      method = {"method_64078"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_12249;method_75973(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;"),
      require = 0
   )
   private class_1921 turtmod$swapTrim(class_2960 id, Operation<class_1921> original) {
      return turtmod$maybeSwap(original.call(id), id, true);
   }

   @Unique
   private class_1921 turtmod$maybeSwap(class_1921 original, class_2960 id, boolean trim) {
      TurtModConfig c = TurtModClient.getConfig();
      if (c == null || !c.misc.enabled || !c.visual.armorDamageTint) return original;
      if (trim && !c.visual.armorDamageTintTrim) return original;
      if (this.turtmod$overlayCoords == TURTMOD_NO_OVERLAY) return original;
      return class_12249.method_75994(id);
   }

   @Override
   public void turtmod$setOverlayCoords(int overlayCoords) {
      this.turtmod$overlayCoords = overlayCoords;
   }
}
