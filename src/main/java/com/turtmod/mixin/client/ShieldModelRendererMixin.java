package com.turtmod.mixin.client;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.GrayscaleTextureCache;
import com.turtmod.utils.ShieldEntityContext;
import com.turtmod.utils.ShieldTracker;
import java.util.UUID;
import net.minecraft.class_10509;
import net.minecraft.class_11659;
//? if >=1.21.11 {
import net.minecraft.class_12249;
//?}
import net.minecraft.class_11701;
import net.minecraft.class_1657;
import net.minecraft.class_1767;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4722;
import net.minecraft.class_4730;
import net.minecraft.class_600;
import net.minecraft.class_630;
import net.minecraft.class_811;
import net.minecraft.class_9307;
import net.minecraft.class_9323;
import net.minecraft.class_9334;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Recolours the shield model to reflect its usable / cooling-down / actively-blocking
 * status. 1.21.11 renders the shield item model through the deferred {@code class_11659}
 * submit queue ({@code method_65707}); we must submit our recoloured geometry through the
 * SAME queue ({@code submitCustom} = method_73483) rather than drawing into the immediate
 * {@code method_23000()} buffers — bypassing the queue writes depth out of order, which is
 * why the recolour previously only showed up intermittently. Mirrors the 1.21.9/1.21.10
 * Walksy ShieldStatus reference (repos/ShieldStatus-1.21.9-1.21.10).
 */
@Mixin({class_10509.class})
public abstract class ShieldModelRendererMixin {
   @Shadow
   @Final
   private class_600 field_55441;

   // The atlas SpriteGetter the vanilla shield renderer uses to resolve banner-pattern sprites.
   // Needed so banner layers map their [0,1] UVs into the correct atlas sub-region — without it the
   // pattern samples an empty atlas region and the banner vanishes ("shield without a banner").
   @Shadow
   @Final
   private class_11701 field_61858;

   @Inject(
      method = {"method_65707"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void turtmod$renderShieldWithStatusColor(@Nullable class_9323 componentMap, class_811 displayContext, class_4587 matrices, class_11659 queue, int light, int overlay, boolean glint, int seed, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      class_310 client = class_310.method_1551();
      if (config == null || client == null || client.field_1724 == null) {
         return;
      }
      if (!config.misc.enabled || !config.visual.shieldStatusRecolor) {
         return;
      }
      // field_4318 = ground (dropped item) — leave it vanilla. GUI (field_4317) is NOW recoloured
      // too so the shield ITEM in the hotbar/inventory reflects status, mirroring ShieldStatus's
      // GUI recolour (the holder is forced to the local player for GUI in getShieldHolder).
      if (displayContext == class_811.field_4318) {
         return;
      }

      class_1657 player = this.getShieldHolder(client, displayContext);
      final int color = this.getShieldColor(player, config);
      class_9307 patterns = componentMap != null ? (class_9307)componentMap.method_58695(class_9334.field_49619, class_9307.field_49404) : class_9307.field_49404;
      class_1767 baseColor = componentMap != null ? (class_1767)componentMap.method_58694(class_9334.field_49620) : null;
      boolean hasPatterns = !patterns.comp_2428().isEmpty() || baseColor != null;

      matrices.method_22903();
      matrices.method_22905(1.0F, -1.0F, -1.0F);
      matrices.method_46416(0.0F, (float)config.visual.shieldYOffset / 16.0F, 0.0F);
      if (config.visual.customShieldSize) {
         boolean isSelf = player == client.field_1724;
         float scale = isSelf ? config.visual.selfShieldScale : config.visual.othersShieldScale;
         matrices.method_22905(scale, scale, scale);
         if (isSelf) {
            matrices.method_46416(config.visual.selfShieldOffsetX, config.visual.selfShieldOffsetY, 0.0F);
         }
      }

      class_2960 baseTexture = class_2960.method_60656("textures/entity/shield_base_nopattern.png");
      if (config.visual.shieldGrayscaleTexture) {
         baseTexture = GrayscaleTextureCache.get(baseTexture);
      }
      //? if >=1.21.11 {
      final class_1921 layer = class_12249.method_76000(baseTexture);
      //?} else {
      /*final class_1921 layer = class_1921.method_23580(baseTexture);
      *///?}
      final int fLight = light;
      final int fOverlay = overlay;
      final class_600 model = this.field_55441;

      // Handle (always rendered) — submit through the queue so depth ordering is correct.
      queue.method_73483(matrices, layer, (entry, vc) -> {
         class_4587 stack = new class_4587();
         stack.method_23760().method_66521(entry);
         stack.method_22903();
         stack.method_46416(0.0F, 0.0F, 1.0E-4F);
         model.method_23775().method_22699(stack, vc, fLight, fOverlay, color);
         stack.method_22909();
      });

      if (hasPatterns) {
         final class_1767 canvasBase = baseColor == null ? class_1767.field_7952 : baseColor;
         final class_9307 canvasPatterns = patterns;

         // 1) Plate base (nopattern texture) — recoloured, through the deferred queue.
         queue.method_73483(matrices, layer, (entry, vc) -> {
            class_4587 stack = new class_4587();
            stack.method_23760().method_66521(entry);
            model.method_23774().method_22699(stack, vc, fLight, fOverlay, color);
         });

         // 2) Banner layers (shield-pattern base + each pattern) on the shield-pattern atlas, ALSO
         //    through the deferred queue. The previous code drew these into the immediate world
         //    buffer (method_23000) and never flushed it, so in the GUI/held pipeline they rendered
         //    with stale state and ignored the status colour — only the base appeared recoloured.
         //? if >=1.21.11 {
         final class_1921 bannerLayer = class_4722.field_49770.method_24146(class_12249::method_76000);
         //?} else {
         /*final class_1921 bannerLayer = class_4722.field_49770.method_24146(class_1921::method_23580);
         *///?}
         queue.method_73483(matrices, bannerLayer, (entry, vc) -> {
            class_4587 stack = new class_4587();
            stack.method_23760().method_66521(entry);
            class_630 plate = model.method_23774();
            this.renderBannerLayer(stack, fLight, fOverlay, plate, vc, class_4722.field_49770, canvasBase, color);
            for (int i = 0; i < 16 && i < canvasPatterns.comp_2428().size(); ++i) {
               class_9307.class_9308 pl = (class_9307.class_9308) canvasPatterns.comp_2428().get(i);
               class_4730 mat = class_4722.method_33083(pl.comp_2429());
               this.renderBannerLayer(stack, fLight, fOverlay, plate, vc, mat, pl.comp_2430(), color);
            }
         });
      } else {
         queue.method_73483(matrices, layer, (entry, vc) -> {
            class_4587 stack = new class_4587();
            stack.method_23760().method_66521(entry);
            model.method_23774().method_22699(stack, vc, fLight, fOverlay, color);
         });
      }

      matrices.method_22909();
      ci.cancel();
   }

   private class_1657 getShieldHolder(class_310 client, class_811 displayContext) {
      // Mirror ShieldStatus.checkDisplayContext: GUI and first-person always belong to the local
      // player. For GUI this MUST override any stale ShieldEntityContext left over from rendering
      // another player's held item, otherwise the hotbar shield would show someone else's status.
      if (displayContext == class_811.field_4317 || displayContext == class_811.field_4321 || displayContext == class_811.field_4322) {
         return client.field_1724;
      }

      if (client.field_1687 != null) {
         UUID uuid = ShieldEntityContext.getUuid();
         if (uuid != null) {
            for(class_1657 candidate : client.field_1687.method_18456()) {
               if (candidate.method_5667().equals(uuid)) {
                  return candidate;
               }
            }
         }
      }

      return null;
   }

   private int getShieldColor(class_1657 player, TurtModConfig config) {
      if (player == null) {
         return -1;
      } else if (config.visual.shieldSelfOnly && player != class_310.method_1551().field_1724) {
         return -1;
      } else {
         boolean usingShield = ShieldTracker.isUsingShield(player);
         boolean coolingDown = ShieldTracker.isCoolingDown(player);
         if (config.visual.shieldUseUsingColor && usingShield) {
            return config.visual.shieldUsingColor;
         } else if (config.visual.shieldColorInterpolation && coolingDown) {
            if (!config.visual.shieldUseBrokenColor) {
               return -1;
            } else {
               // Interpolate FROM the broken color TO the usable color as the shield recovers
               int usableColor = config.visual.shieldUseUsableColor ? config.visual.shieldUsableColor : -1;
               float progress = ShieldTracker.getCooldownProgress(player);
               return this.interpolateColor(config.visual.shieldBrokenColor, usableColor, progress);
            }
         } else if (coolingDown) {
            return config.visual.shieldUseBrokenColor ? config.visual.shieldBrokenColor : -1;
         } else {
            return config.visual.shieldUseUsableColor ? config.visual.shieldUsableColor : -1;
         }
      }
   }

   private int interpolateColor(int color1, int color2, float progress) {
      int a1 = color1 >> 24 & 255;
      int r1 = color1 >> 16 & 255;
      int g1 = color1 >> 8 & 255;
      int b1 = color1 & 255;
      int a2 = color2 >> 24 & 255;
      int r2 = color2 >> 16 & 255;
      int g2 = color2 >> 8 & 255;
      int b2 = color2 & 255;
      return (int)((float)a1 + (float)(a2 - a1) * progress) << 24 | (int)((float)r1 + (float)(r2 - r1) * progress) << 16 | (int)((float)g1 + (float)(g2 - g1) * progress) << 8 | (int)((float)b1 + (float)(b2 - b1) * progress);
   }

   // How strongly the status colour washes over the banner pattern (0 = banner keeps its real
   // colours, 1 = fully replaced by the status colour). A partial blend keeps the banner's own
   // colour and pattern shape visible with the status colour tinted over it.
   private static final float BANNER_STATUS_TINT = 0.5F;

   private void renderBannerLayer(class_4587 matrices, int light, int overlay, class_630 plate, class_4588 vc, class_4730 material, class_1767 color, int statusColor) {
      // With no active status (-1) render the layer with its real dye colour. When a status colour
      // IS active, blend the dye colour toward it so the banner is washed with the status colour but
      // its own colours and pattern shape still show through (a "translucent" status tint).
      int dye = color.method_7787() | 0xFF000000;
      int tinted = statusColor == -1 ? dye : this.interpolateColor(dye, statusColor, BANNER_STATUS_TINT);
      // Wrap the queue's consumer with the sprite's UV expander (SpriteGetter -> Sprite ->
      // getTextureSpecificVertexConsumer) so the pattern samples its atlas sub-region; a raw
      // consumer would ignore the sprite UVs and break the banner texture.
      class_4588 expanded = this.field_61858.method_73030(material).method_24108(vc);
      plate.method_22699(matrices, expanded, light, overlay, tinted);
   }
}
