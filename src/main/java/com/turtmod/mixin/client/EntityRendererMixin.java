package com.turtmod.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.turtmod.TurtModClient;
import com.turtmod.combat.PlayerHeartSpriteRenderer;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import net.minecraft.class_1304;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1007.class})
public abstract class EntityRendererMixin {
   @Inject(
      method = {"method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V"},
      at = {@At("TAIL")}
   )
   private void turtmod$setHeartsLine(class_11890 player, class_10055 state, float tickDelta, CallbackInfo ci) {
      TurtModConfig config = TurtModClient.getConfig();
      if (config != null && config.misc.enabled && config.combat.playerHealthIndicator) {
         if (config.combat.playerHealthIndicatorStyle == TurtModConfig.PlayerHealthIndicatorStyle.SPRITE) {
            class_310 client = class_310.method_1551();
            if (client != null && player != client.field_1724) {
               PlayerHeartSpriteRenderer.record(state, player.method_6032(), player.method_6063(), player.method_6067(), player.method_17682());
            } else {
               PlayerHeartSpriteRenderer.clear(state);
            }
         } else {
            PlayerHeartSpriteRenderer.clear(state);
            if (!state.field_53542) {
               state.field_53525 = buildIndicatorText(player.method_6032(), player.method_6063(), player.method_6067(), config.combat.playerHealthIndicatorStyle, config.combat.playerHealthIndicatorMaxHearts);
            }
         }
      } else {
         PlayerHeartSpriteRenderer.clear(state);
      }
   }

   @ModifyReturnValue(
      method = {"method_74935(Lnet/minecraft/class_11890;D)Z"},
      at = {@At("RETURN")}
   )
   private boolean turtmod$showHealthForInvisible(boolean original, class_11890 player, double distance) {
      if (original) {
         return true;
      }
      TurtModConfig config = TurtModClient.getConfig();
      if (config == null || !config.misc.enabled) {
         return false;
      }
      class_310 client = class_310.method_1551();
      if (config.visual.showOwnNametag && player == client.field_1724) {
         return true;
      }
      if (config.combat.playerHealthIndicator && config.combat.playerHealthIndicatorInvisible && player != client.field_1724 && player.method_5767() && turtmod$hasVisibleArmor(player)) {
         return true;
      }
      return false;
   }

   private static boolean turtmod$hasVisibleArmor(class_11890 entity) {
      return !entity.method_6118(class_1304.field_6169).method_7960()
         || !entity.method_6118(class_1304.field_6174).method_7960()
         || !entity.method_6118(class_1304.field_6172).method_7960()
         || !entity.method_6118(class_1304.field_6166).method_7960();
   }

   private static class_2561 buildIndicatorText(float health, float maxHealth, float absorption, TurtModConfig.PlayerHealthIndicatorStyle style, int maxHeartsCap) {
      if (style == TurtModConfig.PlayerHealthIndicatorStyle.NUMBER) {
         return class_2561.method_43470(String.format("%.1f + %.1f", health, absorption)).method_54663(16777215);
      } else if (style == TurtModConfig.PlayerHealthIndicatorStyle.COMPACT) {
         float total = health + absorption;
         return class_2561.method_43470(String.format("%.1f HP", total)).method_54663(healthColor(health, maxHealth));
      } else {
         return buildHotbarHeartText(health, maxHealth, absorption, maxHeartsCap);
      }
   }

   private static class_2561 buildHotbarHeartText(float health, float maxHealth, float absorption, int maxHeartsCap) {
      int hpHalfUnits = Math.max(0, Math.round(health));
      int maxHalfUnits = Math.max(2, Math.round(maxHealth));
      int absHalfUnits = Math.max(0, Math.round(absorption));
      int heartsCap = Math.max(1, Math.min(40, maxHeartsCap));
      int full = hpHalfUnits / 2;
      boolean half = hpHalfUnits % 2 == 1;
      int empty = Math.max(0, maxHalfUnits / 2 - full - (half ? 1 : 0));
      full = Math.min(full, heartsCap);
      empty = Math.min(empty, heartsCap);
      class_5250 label = class_2561.method_43473();
      if (full > 0) {
         label.method_10852(class_2561.method_43470("❤".repeat(full)).method_54663(16733525));
      }

      if (half) {
         label.method_10852(class_2561.method_43470("♥").method_54663(16733525));
      }

      if (empty > 0) {
         label.method_10852(class_2561.method_43470("♡".repeat(empty)).method_54663(6710886));
      }

      if (absHalfUnits > 0) {
         label.method_10852(class_2561.method_43470(" "));
         label.method_10852(class_2561.method_43470(heartsFromHalfUnits(absHalfUnits)).method_54663(16755200));
      }

      return label;
   }

   private static String heartsFromHalfUnits(int halfUnits) {
      int full = halfUnits / 2;
      boolean half = halfUnits % 2 == 1;
      StringBuilder sb = new StringBuilder();
      if (full > 0) {
         sb.append("❤".repeat(Math.min(full, 20)));
      }

      if (half) {
         sb.append("♥");
      }

      if (sb.isEmpty()) {
         sb.append("❤");
      }

      return sb.toString();
   }

   private static int healthColor(float health, float maxHealth) {
      float pct = maxHealth <= 0.0F ? 0.0F : health / maxHealth;
      if (pct > 0.66F) {
         return 5635925;
      } else {
         return pct > 0.33F ? 16777045 : 16733525;
      }
   }
}
