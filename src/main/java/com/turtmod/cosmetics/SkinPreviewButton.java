package com.turtmod.cosmetics;

import net.minecraft.class_10055;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_8685;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A menu button that renders a live 3D preview of the player's current skin (with cape) above itself, then
 * opens the Skin Changer. Modelled on SkinShuffle's OpenCarouselButton: the preview is drawn inside the
 * widget's own render pass (so it always shows, unlike a screen render-inject), using the same GUI
 * entity-render task the Skin Changer's preview uses. 1.21.11 only — newer versions use a different pipeline.
 */
public class SkinPreviewButton extends class_4185.class_12231 {

   public SkinPreviewButton(int x, int y, int w, int h, class_4185.class_4241 onPress) {
      super(x, y, w, h, class_2561.method_43470("🧍 Skin Changer"), onPress, field_40754);
   }

   @Override
   protected void method_75752(class_332 ctx, int mouseX, int mouseY, float delta) {
      super.method_75752(ctx, mouseX, mouseY, delta);
      class_8685 skin = currentSkin();
      if (skin == null) {
         return;
      }
      // Compact body preview centred above the button (fixed size so a wide, labelled button stays tidy).
      int pw = 44;
      int ph = (int) (pw * 1.9f);
      int cx = this.method_46426() + this.method_25368() / 2;
      int x1 = cx - pw / 2;
      int x2 = cx + pw / 2;
      int y2 = this.method_46427() - 4;
      int y1 = y2 - ph;
      renderBody(ctx, skin, x1, y1, x2, y2, mouseX, mouseY);
   }

   /** The player's live skin when in-world, else the account skin (both carry the active cape). */
   private static class_8685 currentSkin() {
      class_310 mc = class_310.method_1551();
      if (mc == null) {
         return null;
      }
      if (mc.field_1724 != null) {
         try {
            return mc.field_1724.method_52814();
         } catch (Exception ignored) {
         }
      }
      try {
         return mc.method_1582().method_73544(mc.method_53462(), true).get();
      } catch (Exception e) {
         return null;
      }
   }

   /** Mirrors CosmeticsScreen.drawModel3D but in raw screen coords (a widget isn't UI-scaled). */
   private static void renderBody(class_332 ctx, class_8685 skin, int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
      class_10055 state = new class_10055();
      state.field_53520 = skin;
      state.field_53329 = 0.6f;
      state.field_53330 = 1.8f;
      state.field_61820 = 15728880;
      state.field_61821 = 0;
      state.field_61823.clear();
      state.field_53453 = 1.0f;
      state.field_53454 = 1.0f;
      int cx = (x1 + x2) / 2, cy = (y1 + y2) / 2;
      float headYaw = (float) Math.toDegrees(Math.atan((cx - mouseX) * 0.008));
      float headPitch = (float) Math.toDegrees(Math.atan((mouseY - cy) * 0.008));
      state.field_53446 = 180f;            // body faces the viewer (front)
      state.field_53447 = headYaw;         // head tracks the cursor a little (relative to body — no 180 flip)
      state.field_53448 = headPitch;

      double gs = ctx.method_51421() > 0 ? class_310.method_1551().method_22683().method_4495() : 1.0;
      int lh = y2 - y1;
      int scale = Math.max(6, lh / 3);
      int sx1 = (int) Math.round(x1 * gs);
      int sx2 = (int) Math.round(x2 * gs);
      int sy1 = (int) Math.round(y1 * gs);
      int sy2 = (int) Math.round(y2 * gs);
      float sScale = (float) (scale * gs);

      Quaternionf baseRot = new Quaternionf().rotateZ((float) Math.PI);
      Vector3f pos = new Vector3f(0f, state.field_53330 / 2f + 0.0625f, 0f);
      ctx.method_70856(state, sScale, pos, baseRot, null, sx1, sy1, sx2, sy2);
   }
}
