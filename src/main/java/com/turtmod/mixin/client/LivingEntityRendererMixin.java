package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.turtmod.TurtModClient;
import com.turtmod.combat.PlayerHeartSpriteRenderer;
import com.turtmod.config.TurtModConfig;
import com.turtmod.extension.minecraft.OverlayRendered;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.class_10197;
import net.minecraft.class_1309;
import net.minecraft.class_10042;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_310;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_5617;
import net.minecraft.class_583;
import net.minecraft.class_897;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_922.class})
public abstract class LivingEntityRendererMixin<T extends class_1309, S extends class_10042, M extends class_583<? super S>> extends class_897<T, S> {
   protected LivingEntityRendererMixin(class_5617.class_5618 context) {
      super(context);
   }

   @Shadow
   public static int method_23622(class_10042 state, float f) {
      return 0;
   }

   @Shadow
   protected abstract float method_23185(S state);

   private static boolean turtmod$loggedDirect = false;
   private static boolean turtmod$loggedReflect = false;
   private static boolean turtmod$loggedMiss = false;

   @Inject(
      method = {"method_4054"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_3887;method_4199(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10017;FF)V"
      )}
   )
   public void turtmod$renderWithOverlay(S state, class_4587 matrices, class_11659 collector, class_12075 camera, CallbackInfo ci, @Local class_3887<S, M> layer) {
      int overlayCoords = method_23622(state, this.method_23185(state));
      if (layer instanceof OverlayRendered overlayRendered) {
         if (!turtmod$loggedDirect) { com.turtmod.utils.TurtLogger.info("[turtmod] Overlay direct on " + layer.getClass().getName()); turtmod$loggedDirect = true; }
         overlayRendered.turtmod$setOverlayCoords(overlayCoords);
         return;
      }
      class_10197 inner = turtmod$findEquipmentRenderer(layer);
      if (inner instanceof OverlayRendered or) {
         if (!turtmod$loggedReflect) { com.turtmod.utils.TurtLogger.info("[turtmod] Overlay reflect on " + layer.getClass().getName() + " -> " + inner.getClass().getName()); turtmod$loggedReflect = true; }
         or.turtmod$setOverlayCoords(overlayCoords);
      } else if (!turtmod$loggedMiss) {
         com.turtmod.utils.TurtLogger.info("[turtmod] Overlay MISS on " + (layer == null ? "null" : layer.getClass().getName()) + " inner=" + (inner == null ? "null" : inner.getClass().getName()));
         turtmod$loggedMiss = true;
      }
   }

   private static final Map<Class<?>, Field> turtmod$equipmentFieldCache = new IdentityHashMap<>();
   private static final Field TURTMOD_SENTINEL;
   static {
      Field s = null;
      try { s = LivingEntityRendererMixin.class.getDeclaredField("TURTMOD_SENTINEL"); } catch (Throwable ignored) {}
      TURTMOD_SENTINEL = s;
   }

   private static class_10197 turtmod$findEquipmentRenderer(Object layer) {
      if (layer == null) return null;
      Class<?> cls = layer.getClass();
      Field cached = turtmod$equipmentFieldCache.get(cls);
      if (cached == TURTMOD_SENTINEL) return null;
      try {
         if (cached == null) {
            for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
               for (Field f : c.getDeclaredFields()) {
                  if (class_10197.class.isAssignableFrom(f.getType())) {
                     f.setAccessible(true);
                     turtmod$equipmentFieldCache.put(cls, f);
                     return (class_10197) f.get(layer);
                  }
               }
            }
            turtmod$equipmentFieldCache.put(cls, TURTMOD_SENTINEL);
            return null;
         }
         return (class_10197) cached.get(layer);
      } catch (Throwable t) {
         turtmod$equipmentFieldCache.put(cls, TURTMOD_SENTINEL);
         return null;
      }
   }

   @Inject(
      method = {"method_4054"},
      at = {@At("TAIL")}
   )
   public void turtmod$renderHeartSprites(S state, class_4587 matrices, class_11659 collector, class_12075 camera, CallbackInfo ci) {
      PlayerHeartSpriteRenderer.render(state, matrices, camera);
   }

   // Own-nametag (who-am-i approach): nullify the camera-entity self-check inside
   // shouldShowName so the own player's nametag passes, while keeping ALL other
   // vanilla checks (distance, invisibility, team rules, HUD-hidden).
   @ModifyExpressionValue(
      method = {"method_4055"},
      at = {@At(value = "INVOKE", target = "Lnet/minecraft/class_310;method_1560()Lnet/minecraft/class_1297;")}
   )
   private net.minecraft.class_1297 turtmod$showOwnLabel(net.minecraft.class_1297 cameraEntity) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.visual.showOwnNametag) {
         return null; // bypass only the `entity != cameraEntity` self-exclusion
      }
      return cameraEntity;
   }
}
