package com.turtmod.cosmetics;

import com.turtmod.ui.Palette;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

/** Blits a player's 8x8 face (plus the hat overlay) from a 64x64 skin, scaled up, as a small menu icon. */
public final class SkinFaceRenderer {
   private SkinFaceRenderer() {
   }

   /** Draw the face+hat of {@code skinId} at (x,y) filling a {@code size}px square. Null draws a placeholder. */
   public static void draw(class_332 ctx, class_2960 skinId, int x, int y, int size) {
      // Backing card so the icon reads as a framed avatar.
      com.turtmod.ui.TurtUIUtils.drawRoundedRect(ctx, x - 2, y - 2, size + 4, size + 4, 3, Palette.alpha(Palette.CARD_BG, 235));
      com.turtmod.ui.TurtUIUtils.drawRoundedBorder(ctx, x - 2, y - 2, size + 4, size + 4, 3, Palette.alpha(Palette.GREEN, 200));
      if (skinId == null) {
         // Neutral silhouette head.
         ctx.method_25294(x + size / 4, y + size / 5, x + size * 3 / 4, y + size * 4 / 5, 0xFF3A4150);
         return;
      }
      // Face (base layer) then hat (overlay), both 8x8 source regions from a 64x64 skin.
      ctx.method_25302(class_10799.field_56883, skinId, x, y, 8f, 8f, size, size, 8, 8, 64, 64);
      ctx.method_25302(class_10799.field_56883, skinId, x, y, 40f, 8f, size, size, 8, 8, 64, 64);
   }
}
