package com.turtmod.cosmetics;

import net.minecraft.class_10055;
import net.minecraft.class_1068;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_8685;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A plain vanilla button that also renders a live 3D preview of the player's current skin (with cape)
 * floating above itself, then opens the Skin Changer. The model slowly auto-rotates, but you can grab the
 * preview and drag to rotate it yourself. The preview is clamped to the scaled screen so it stays visible.
 * 1.21.11 only — newer versions use a different render pipeline.
 */
public class SkinPreviewButton extends class_4185.class_12231 {

   private static final int PREVIEW_H = 130;   // preview height above the button
   private static final int BODY_W = 68;

   // Last-rendered preview rect (for drag hit-testing).
   private int pvx1, pvy1, pvx2, pvy2;
   private boolean dragging = false;
   private boolean manual = false;            // true once the user has dragged to rotate
   private float userYaw = 0f;
   private float userPitch = 0f;
   private int lastMx, lastMy;                // for computing drag delta in the render loop

   public SkinPreviewButton(int x, int y, int w, int h, class_4185.class_4241 onPress) {
      super(x, y, w, h, class_2561.method_43470("  Skin Changer"), onPress, field_40754);
   }

   @Override
   protected void method_75752(class_332 ctx, int mouseX, int mouseY, float delta) {
      super.method_75752(ctx, mouseX, mouseY, delta);   // normal vanilla button
      // Player-head item icon on the left of the button face (matches the item icons used in the menu nav).
      net.minecraft.class_1799 headIcon = com.turtmod.ui.TurtModuleIcons.forModule("Skin Changer");
      if (headIcon != null) {
         com.turtmod.ui.TurtModuleIcons.drawItem(ctx, headIcon, this.method_46426() + 4, this.method_46427() + 2);
      }
      class_8685 skin = currentSkin();
      if (skin == null) {
         return;
      }
      // Preview box centred above the button with a gap so it doesn't sit on it; clamped on-screen.
      int bx = this.method_46426(), by = this.method_46427(), bw = this.method_25368();
      int cx = bx + bw / 2;
      int y2 = by - 12, y1 = y2 - PREVIEW_H;
      int x1 = cx - BODY_W / 2, x2 = cx + BODY_W / 2;

      class_310 mc = class_310.method_1551();
      int sw = mc.method_22683().method_4486(); // SCALED gui width (method_4489 is raw framebuffer)
      int sh = mc.method_22683().method_4502(); // SCALED gui height
      if (x1 < 2) { int d = 2 - x1; x1 += d; x2 += d; }
      if (x2 > sw - 2) { int d = x2 - (sw - 2); x1 -= d; x2 -= d; }
      if (y1 < 2) { int d = 2 - y1; y1 += d; y2 += d; }
      if (y2 > sh - 2) { int d = y2 - (sh - 2); y1 -= d; y2 -= d; }
      this.pvx1 = x1; this.pvy1 = y1; this.pvx2 = x2; this.pvy2 = y2;

      // Drive rotation from the render loop's live cursor delta (reliable, unlike drag-event routing).
      if (this.dragging) {
         this.manual = true;
         this.userYaw += (mouseX - this.lastMx);
         this.userPitch += (mouseY - this.lastMy) * 0.02f;
         this.userPitch = Math.max(-0.6f, Math.min(0.6f, this.userPitch));
      }
      this.lastMx = mouseX;
      this.lastMy = mouseY;

      float bodyYaw, tiltPitch;
      if (this.manual) {
         bodyYaw = 180f + this.userYaw;
         tiltPitch = this.userPitch;
      } else {
         float spin = (System.currentTimeMillis() % 6000L) / 6000.0f * 360.0f;   // slow turntable
         bodyYaw = 180f + spin;
         tiltPitch = 0f;
      }

      // Scissor to the preview region before submitting (the GUI entity render captures the scissor).
      ctx.method_44379(x1, y1, x2, y2);
      renderBody(ctx, skin, x1, y1, x2, y2, mouseX, mouseY, bodyYaw, tiltPitch);
      ctx.method_44380();
   }

   @Override
   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = click.comp_4798(), my = click.comp_4799();
      if (mx >= this.pvx1 && mx <= this.pvx2 && my >= this.pvy1 && my <= this.pvy2) {
         this.dragging = true;   // grab the preview to rotate it (delta applied in the render loop)
         this.lastMx = (int) mx;
         this.lastMy = (int) my;
         return true;
      }
      return super.method_25402(click, bl);   // button press if over the button, else no-op
   }

   /**
    * Report "mouse over" for the preview area as well as the button. The screen only dispatches clicks /
    * drags to the widget the cursor is over (isMouseOver), so without this the preview never received the
    * grab and only the head (which tracks the cursor) appeared to move.
    */
   @Override
   public boolean method_25405(double x, double y) {
      if (super.method_25405(x, y)) {
         return true;
      }
      return x >= this.pvx1 && x <= this.pvx2 && y >= this.pvy1 && y <= this.pvy2;
   }

   @Override
   public boolean method_25403(class_11909 click, double dx, double dy) {
      if (this.dragging) {
         return true;   // consume; the actual rotation is applied from the render-loop cursor delta
      }
      return super.method_25403(click, dx, dy);
   }

   @Override
   public boolean method_25406(class_11909 click) {
      if (this.dragging) { this.dragging = false; return true; }
      return super.method_25406(click);
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
    * Renders the paper-doll like vanilla's inventory player render (class_490.method_2486): the box corners
    * and the scale are passed to method_70856 in GUI-scaled coords (NOT multiplied by the GUI scale factor).
    */
   private static void renderBody(class_332 ctx, class_8685 skin, int x1, int y1, int x2, int y2,
                                  int mouseX, int mouseY, float bodyYaw, float tiltPitch) {
      class_10055 state = new class_10055();
      state.field_53520 = skin;
      state.field_53329 = 0.6f;
      state.field_53330 = 1.8f;
      state.field_61820 = 15728880;
      state.field_61821 = 0;
      state.field_61823.clear();
      state.field_53453 = 1.0f;
      state.field_53454 = 1.0f;
      state.field_53532 = true;            // show the cape layer

      float cx = (x1 + x2) / 2.0f;
      float p = (float) Math.atan((cx - mouseX) / 40.0f);
      state.field_53446 = bodyYaw;         // body (auto-spin or user drag)
      state.field_53447 = p * 20.0f;       // head tracks the cursor a little
      state.field_53448 = -tiltPitch * 20.0f;

      int scale = Math.max(6, (y2 - y1) * 3 / 7);
      Quaternionf baseRot = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf tilt = new Quaternionf().rotateX(tiltPitch);
      baseRot.mul(tilt);
      Vector3f pos = new Vector3f(0.0f, state.field_53330 / 2.0f + 0.0625f, 0.0f);
      ctx.method_70856(state, scale, pos, baseRot, tilt, x1, y1, x2, y2);
   }
}
