package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_11659;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4587;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_759.class})
public abstract class HeldItemScaleMixin {
   @Inject(
      method = {"method_3233"},
      at = {@At("HEAD")}
   )
   private void turtmod$applyHeldItemScale(class_1309 entity, class_1799 stack, class_811 displayContext, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) return;
      boolean firstPersonHand = displayContext == class_811.field_4321 || displayContext == class_811.field_4322;
      if (!firstPersonHand) return;

      // ── Totem Tweaks: scale + offset the totem in hand ──
      boolean isTotem = stack != null && stack.method_7909() == class_1802.field_8288;
      if (config.visual.enableSmallTotem && isTotem) {
         matrices.method_22903();
         float ts = config.visual.totemScale > 0.0F ? config.visual.totemScale : 1.0F;
         matrices.method_22905(ts, ts, ts);
         matrices.method_46416(config.visual.totemOffsetX, config.visual.totemOffsetY, config.visual.totemOffsetZ);
         return; // totem handled — don't double-apply held-item tweaks
      }

      if (config.visual.heldItemTweaksEnabled) {
         matrices.method_22903();
         float scale = (float)config.visual.heldItemScalePercent / 100.0F;
         if (scale != 1.0F && scale > 0.0F) {
            matrices.method_22905(scale, scale, scale);
         }
         boolean offhand = displayContext == class_811.field_4321;
         if (offhand) {
            if (config.visual.customOffhandHeldItemSize) {
               matrices.method_22905(config.visual.offhandHeldItemScaleX, config.visual.offhandHeldItemScaleY, config.visual.offhandHeldItemScaleZ);
               matrices.method_46416(config.visual.offhandHeldItemPosX, config.visual.offhandHeldItemPosY, config.visual.offhandHeldItemPosZ);
               turtmod$rotate(matrices, config.visual.offhandHeldItemRotX, config.visual.offhandHeldItemRotY, config.visual.offhandHeldItemRotZ);
            }
         } else if (config.visual.customHeldItemSize) {
            matrices.method_22905(config.visual.heldItemScaleX, config.visual.heldItemScaleY, config.visual.heldItemScaleZ);
            matrices.method_46416(config.visual.heldItemPosX, config.visual.heldItemPosY, config.visual.heldItemPosZ);
            turtmod$rotate(matrices, config.visual.heldItemRotX, config.visual.heldItemRotY, config.visual.heldItemRotZ);
         }
      }
   }

   private static void turtmod$rotate(class_4587 matrices, float degX, float degY, float degZ) {
      if (degX == 0.0F && degY == 0.0F && degZ == 0.0F) {
         return;
      }
      matrices.method_22907(new org.joml.Quaternionf().rotationXYZ(
         (float)Math.toRadians(degX), (float)Math.toRadians(degY), (float)Math.toRadians(degZ)));
   }

   @Inject(
      method = {"method_3233"},
      at = {@At("RETURN")}
   )
   private void turtmod$popHeldItemScale(class_1309 entity, class_1799 stack, class_811 displayContext, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) return;
      boolean firstPersonHand = displayContext == class_811.field_4321 || displayContext == class_811.field_4322;
      if (!firstPersonHand) return;
      boolean isTotem = stack != null && stack.method_7909() == class_1802.field_8288;
      boolean pushed = (config.visual.enableSmallTotem && isTotem) || config.visual.heldItemTweaksEnabled;
      if (pushed) {
         matrices.method_22909();
      }
   }
}
