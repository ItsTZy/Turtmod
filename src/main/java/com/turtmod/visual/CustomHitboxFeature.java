package com.turtmod.visual;

import com.turtmod.config.TurtModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_12249;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1671;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_746;
import net.minecraft.class_9974;

public final class CustomHitboxFeature {
   private CustomHitboxFeature() {
   }

   public static void render(WorldRenderContext context, class_310 client, TurtModConfig config) {
      if (config.misc.enabled && config.hud.customHitboxes && client.field_1724 != null && client.field_1687 != null) {
         if (context.matrices() != null && context.consumers() != null && context.worldState() != null && context.worldState().field_63082 != null) {
            class_243 cameraPos = context.worldState().field_63082.field_63078;
            double maxDistSq = (double)config.hud.hitboxMaxDistance * (double)config.hud.hitboxMaxDistance;
            class_4588 lineBuffer = context.consumers().method_73477(class_12249.field_64042);
            float lineWidth = 1.0F;
            class_1297 targeted = null;
            class_239 var10 = client.field_1765;
            if (var10 instanceof class_3966) {
               class_3966 ehr = (class_3966)var10;
               targeted = ehr.method_17782();
            }

            for(class_1297 entity : client.field_1687.method_18112()) {
               if (shouldRenderEntity(entity, client.field_1724, config, maxDistSq) && (!config.hud.hitboxHideFireworks || !(entity instanceof class_1671))) {
                  int color = getColor(entity, targeted, config);
                  class_238 box = entity.method_5829().method_989(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);
                  drawBox(context.matrices(), lineBuffer, box, color, lineWidth);
               }
            }

         }
      }
   }

   private static boolean shouldRenderEntity(class_1297 entity, class_746 player, TurtModConfig config, double maxDistSq) {
      if (entity != null && !entity.method_31481()) {
         if (entity == player && !config.hud.hitboxSelf) {
            return false;
         } else if (entity.method_5858(player) > maxDistSq) {
            return false;
         } else if (entity instanceof class_1657) {
            return config.hud.hitboxPlayers;
         } else if (entity instanceof class_1588) {
            return config.hud.hitboxHostile;
         } else {
            return entity instanceof class_1296 ? config.hud.hitboxPassive : config.hud.hitboxOthers;
         }
      } else {
         return false;
      }
   }

   private static int getColor(class_1297 entity, class_1297 targeted, TurtModConfig config) {
      if (config.hud.hitboxHurtColorEnabled && entity instanceof class_1309 living) {
         if (living.field_6235 > 0) {
            return config.hud.hitboxHurtColor;
         }
      }

      return config.hud.hitboxChangeTargetColor && targeted == entity ? config.hud.hitboxTargetColor : config.hud.hitboxColor;
   }

   private static void drawBox(class_4587 matrices, class_4588 buffer, class_238 box, int color, float lineWidth) {
      class_9974.method_62296(matrices, buffer, class_259.method_1078(box), (double)0.0F, (double)0.0F, (double)0.0F, color, lineWidth);
   }
}
