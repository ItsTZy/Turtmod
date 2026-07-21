package com.turtmod.cosmetics;

import net.minecraft.class_10055;
import net.minecraft.class_1068;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_8685;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A plain vanilla button that also renders a live 3D preview of the player's current skin (with cape)
 * floating above itself, then opens the Skin Changer. The preview is always clamped to the scaled screen
 * so it stays visible at any window size. 1.21.11 only — newer versions use a different render pipeline.
 */
public class SkinPreviewButton extends class_4185.class_12231 {

   private static final int PREVIEW_H = 84;   // preview height above the button
   private static final int BODY_W = 44;

   public SkinPreviewButton(int x, int y, int w, int h, class_4185.class_4241 onPress) {
      super(x, y, w, h, class_2561.method_43470("🧍 Skin Changer"), onPress, field_40754);
   }

   @Override
   protected void method_75752(class_332 ctx, int mouseX, int mouseY, float delta) {
      super.method_75752(ctx, mouseX, mouseY, delta);   // normal vanilla button
      class_8685 skin = currentSkin();
      if (skin == null) {
         return;
      }
      // Preview box centred above the button, clamped so it never leaves the screen.
      int bx = this.method_46426(), by = this.method_46427(), bw = this.method_25368();
      int cx = bx + bw / 2;
      int y2 = by - 4, y1 = y2 - PREVIEW_H;
      int x1 = cx - BODY_W / 2, x2 = cx + BODY_W / 2;

      class_310 mc = class_310.method_1551();
      int sw = mc.method_22683().method_4489();
      int sh = mc.method_22683().method_4507();
      if (x1 < 2) { int d = 2 - x1; x1 += d; x2 += d; }
      if (x2 > sw - 2) { int d = x2 - (sw - 2); x1 -= d; x2 -= d; }
      if (y1 < 2) { int d = 2 - y1; y1 += d; y2 += d; }
      if (y2 > sh - 2) { int d = y2 - (sh - 2); y1 -= d; y2 -= d; }

      // Scissor to the preview region before submitting the entity — the GUI entity render captures the
      // current scissor, and without this the model can be clipped away in a widget context (SkinShuffle
      // does the same in GuiEntityRenderer.drawEntity).
      ctx.method_44379(x1, y1, x2, y2);
      renderBody(ctx, skin, x1, y1, x2, y2, mouseX, mouseY);
      ctx.method_44380();
   }

   /** The player's live skin when in-world, else the account skin, else a default skin — never null. */
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
         class_8685 s = mc.method_1582().method_73544(mc.method_53462(), false).get();
         if (s != null) return s;
      } catch (Exception ignored) {
      }
      // Guaranteed fallback so a preview always shows on the title screen.
      try {
         return class_1068.method_4648(mc.method_1548().method_44717());
      } catch (Exception e) {
         return class_1068.method_62620();
      }
   }

   /**
    * Renders the paper-doll exactly like vanilla's inventory player render (class_490.method_2486): the
    * box corners and the scale are passed to method_70856 in GUI-scaled coords — NOT multiplied by the
    * GUI scale factor. (Multiplying by it threw the model far off-screen, which is why it never showed.)
    */
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

      float cx = (x1 + x2) / 2.0f, cy = (y1 + y2) / 2.0f;
      float p = (float) Math.atan((cx - mouseX) / 40.0f);
      float q = (float) Math.atan((cy - mouseY) / 40.0f);
      state.field_53446 = 180.0f;          // body faces the viewer (front)
      state.field_53447 = p * 20.0f;       // head tracks the cursor a little
      state.field_53448 = -q * 20.0f;

      int scale = Math.max(6, (y2 - y1) * 3 / 7);
      Quaternionf baseRot = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf tilt = new Quaternionf().rotateX(q * 20.0f * ((float) Math.PI / 180.0f));
      baseRot.mul(tilt);
      Vector3f pos = new Vector3f(0.0f, state.field_53330 / 2.0f + 0.0625f, 0.0f);
      ctx.method_70856(state, scale, pos, baseRot, tilt, x1, y1, x2, y2);
   }
}
