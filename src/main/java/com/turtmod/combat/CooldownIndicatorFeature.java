package com.turtmod.combat;

import com.turtmod.config.TurtModConfig;
import com.turtmod.hud.CustomThemeRenderer;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3489;

public final class CooldownIndicatorFeature {
   private static boolean playedChargedSound;

   private CooldownIndicatorFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.combat.customAttackCooldownIndicator) {
         if (config.combat.cooldownOnlyWeapon && !isWeapon(client.field_1724.method_6047())) {
            playedChargedSound = false;
         } else {
            float progress = client.field_1724.method_7261(0.0F);
            boolean charged = progress >= 1.0F;
            if (charged && config.combat.chargedSound && !playedChargedSound) {
               client.field_1724.method_5783((class_3414)class_3417.field_15015.comp_349(), 0.3F, 1.25F);
               playedChargedSound = true;
            } else if (!charged) {
               playedChargedSound = false;
            }

         }
      } else {
         playedChargedSound = false;
      }
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.combat.customAttackCooldownIndicator) {
         if (!config.combat.cooldownOnlyWeapon || isWeapon(client.field_1724.method_6047())) {
            int width = client.method_22683().method_4486();
            int height = client.method_22683().method_4502();
            float scale = CustomThemeRenderer.getHudScale(config, config.combat.cooldownScalePercent);
            int barWidth = Math.max(8, Math.round((float)config.combat.cooldownWidth * scale));
            int barHeight = Math.max(1, Math.round((float)config.combat.cooldownHeight * scale));
            int x = width / 2 + config.combat.cooldownOffsetX;
            int y = height / 2 + config.combat.cooldownOffsetY;
            float progress = Math.max(0.0F, Math.min(1.0F, client.field_1724.method_7261(0.0F)));
            int fill = (int)((float)(config.combat.cooldownVertical ? barHeight : barWidth) * progress);
            boolean charged = progress >= 1.0F;
            int color = config.combat.unchargedColorArgb;
            if (charged && config.combat.chargedColor) {
               color = config.combat.chargedColorArgb;
            }

            CustomThemeRenderer.renderThemedBox(context, x, y, barWidth, barHeight, config);
            if (config.combat.cooldownVertical) {
               context.method_25294(x, y + (barHeight - fill), x + barWidth, y + barHeight, CustomThemeRenderer.applyHudOpacity(config, color));
            } else {
               context.method_25294(x, y, x + fill, y + barHeight, CustomThemeRenderer.applyHudOpacity(config, color));
            }

            if (config.combat.cooldownOutline) {
               context.method_73198(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, CustomThemeRenderer.applyHudOpacity(config, -872415232));
            }

         }
      }
   }

   private static boolean isWeapon(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_1792 item = stack.method_7909();
         return stack.method_31573(class_3489.field_42611) || stack.method_31573(class_3489.field_42612) || item == class_1802.field_8547 || item == class_1802.field_49814;
      } else {
         return false;
      }
   }
}
