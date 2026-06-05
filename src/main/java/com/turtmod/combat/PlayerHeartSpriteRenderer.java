package com.turtmod.combat;

import java.util.WeakHashMap;
import net.minecraft.class_10042;
import net.minecraft.class_10725;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_1058;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import org.joml.Matrix4f;

public final class PlayerHeartSpriteRenderer {
   private static final class_2960 EMPTY = class_2960.method_60656("hud/heart/container");
   private static final class_2960 RED_FULL = class_2960.method_60656("hud/heart/full");
   private static final class_2960 RED_HALF = class_2960.method_60656("hud/heart/half");
   private static final class_2960 ABS_FULL = class_2960.method_60656("hud/heart/absorbing_full");
   private static final class_2960 ABS_HALF = class_2960.method_60656("hud/heart/absorbing_half");
   private static final class_2960 GUI_ATLAS = class_2960.method_60656("textures/atlas/gui.png");
   private static final WeakHashMap<class_10042, float[]> DATA = new WeakHashMap();

   private PlayerHeartSpriteRenderer() {
   }

   public static void record(class_10042 state, float health, float maxHealth, float absorption, float height) {
      DATA.put(state, new float[]{health, maxHealth, absorption, height});
   }

   public static void clear(class_10042 state) {
      DATA.remove(state);
   }

   public static void render(class_10042 state, class_4587 matrices, class_12075 camera) {
      float[] data = (float[])DATA.get(state);
      if (data != null) {
         class_310 client = class_310.method_1551();
         if (client != null && camera != null) {
            class_1059 atlas = client.method_72703().method_73025(class_10725.field_56385);
            int healthRed = Math.max(0, class_3532.method_15386(data[0]));
            int maxHealth = Math.max(2, class_3532.method_15386(data[1]));
            int healthYellow = Math.max(0, class_3532.method_15386(data[2]));
            int heartsRed = class_3532.method_15386((float)healthRed / 2.0F);
            boolean lastRedHalf = (healthRed & 1) == 1;
            int heartsNormal = class_3532.method_15386((float)maxHealth / 2.0F);
            int heartsYellow = class_3532.method_15386((float)healthYellow / 2.0F);
            boolean lastYellowHalf = (healthYellow & 1) == 1;
            com.turtmod.config.TurtModConfig cfg = com.turtmod.TurtModClient.getConfig();
            int maxHeartsCap = cfg != null ? Math.max(1, Math.min(40, cfg.combat.playerHealthIndicatorMaxHearts)) : 40;
            int heartsTotal = Math.min(heartsNormal + heartsYellow, maxHeartsCap);
            if (heartsTotal > 0) {
               int heartsPerRow = 10;
               int rowsTotal = (heartsTotal + heartsPerRow - 1) / heartsPerRow;
               int rowOffset = Math.max(10 - (rowsTotal - 2), 3);
               int pixelsTotal = Math.min(heartsTotal, heartsPerRow) * 8 + 1;
               float maxX = (float)pixelsTotal / 2.0F;
               float yOff = cfg != null ? cfg.combat.playerHealthIndicatorYOffset : 0.6F;
               float sprS = (cfg != null ? Math.max(10, cfg.combat.playerHealthIndicatorSpriteScalePercent) : 100) * 0.025F / 100.0F;
               matrices.method_22903();
               matrices.method_46416(0.0F, data[3] + yOff, 0.0F);
               matrices.method_22907(camera.field_63081);
               matrices.method_22905(-sprS, sprS, sprS);

               for(int heart = 0; heart < heartsTotal; ++heart) {
                  int row = heart / heartsPerRow;
                  int col = heart % heartsPerRow;
                  float hx = maxX - (float)(col * 8);
                  float hy = (float)(row * rowOffset);
                  float hz = (float)row * 0.01F;
                  drawHeart(client, matrices, atlas, hx, hy, hz, EMPTY);
                  class_2960 type = null;
                  if (heart < heartsRed) {
                     type = heart == heartsRed - 1 && lastRedHalf ? RED_HALF : RED_FULL;
                  } else if (heart >= heartsNormal) {
                     type = heart == heartsTotal - 1 && lastYellowHalf ? ABS_HALF : ABS_FULL;
                  }

                  if (type != null) {
                     drawHeart(client, matrices, atlas, hx, hy, hz, type);
                  }
               }

               matrices.method_22909();
            }
         }
      }
   }

   private static void drawHeart(class_310 client, class_4587 matrices, class_1059 atlas, float x, float y, float z, class_2960 textureId) {
      class_1058 sprite = atlas.method_4608(textureId);
      float minU = sprite.method_4594();
      float maxU = sprite.method_4577();
      float minV = sprite.method_4593();
      float maxV = sprite.method_4575();
      float heartSize = 9.0F;
      matrices.method_22903();
      Matrix4f model = matrices.method_23760().method_23761();
      class_4588 vc = sprite.method_24108(client.method_22940().method_23000().method_73477(class_12249.method_75994(GUI_ATLAS)));
      drawVertex(model, vc, x, y - heartSize, z, minU, maxV);
      drawVertex(model, vc, x - heartSize, y - heartSize, z, maxU, maxV);
      drawVertex(model, vc, x - heartSize, y, z, maxU, minV);
      drawVertex(model, vc, x, y, z, minU, minV);
      matrices.method_22909();
   }

   private static void drawVertex(Matrix4f model, class_4588 vc, float x, float y, float z, float u, float v) {
      vc.method_22918(model, x, y, z).method_22913(u, v).method_1336(255, 255, 255, 255).method_60803(15728880).method_22922(class_4608.field_21444).method_22914(0.0F, 1.0F, 0.0F);
   }
}
