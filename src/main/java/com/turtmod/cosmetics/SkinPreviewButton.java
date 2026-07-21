package com.turtmod.cosmetics;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import net.minecraft.class_10055;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_8685;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A menu button that renders a live 3D preview of the player's current skin (with cape) above itself, then
 * opens the Skin Changer. The widget's bounds cover BOTH the preview area and the button strip, so the
 * preview can be dragged around; the button chrome is drawn only in the bottom strip and its press is
 * delegated to the vanilla button logic. The preview is clamped on-screen so it can never be cut off.
 * 1.21.11 only — newer versions use a different render pipeline.
 */
public class SkinPreviewButton extends class_4185.class_12231 {

   private static final int PREVIEW_H = 84;   // reserved space above the button for the paper-doll
   private static final int BODY_W = 44;
   private final int btnH;
   private int dragOffX = 0, dragOffY = 0;
   private boolean dragging = false;

   public SkinPreviewButton(int x, int y, int w, int h, class_4185.class_4241 onPress) {
      // Expand upward so the preview area is part of this widget (needed to catch drags there).
      super(x, y - PREVIEW_H, w, PREVIEW_H + h, class_2561.method_43470("🧍 Skin Changer"), onPress, field_40754);
      this.btnH = h;
   }

   private int btnX() { return this.method_46426(); }
   private int btnY() { return this.method_46427() + PREVIEW_H; }
   private int btnW() { return this.method_25368(); }

   @Override
   protected void method_75752(class_332 ctx, int mouseX, int mouseY, float delta) {
      int bx = btnX(), by = btnY(), bw = btnW(), bh = this.btnH;
      boolean hover = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;

      // Button chrome (bottom strip only), themed rather than the tall default background.
      TurtUIUtils.drawRoundedRect(ctx, bx, by, bw, bh, 4, new Color(0, 0, 0, hover ? 175 : 135));
      TurtUIUtils.drawRoundedBorder(ctx, bx, by, bw, bh, 4, hover ? Palette.GREEN : new Color(255, 255, 255, 45));
      class_327 tr = class_310.method_1551().field_1772;
      ctx.method_27534(tr, this.method_25369(), bx + bw / 2, by + (bh - 8) / 2, hover ? Palette.GREEN.getRGB() : 0xFFFFFFFF);

      class_8685 skin = currentSkin();
      if (skin == null) {
         return;
      }
      // Preview box: centred above the button, shifted by the user's drag, then clamped on-screen.
      int cx = bx + bw / 2 + this.dragOffX;
      int y2 = by - 4 + this.dragOffY;
      int x1 = cx - BODY_W / 2, x2 = cx + BODY_W / 2, y1 = y2 - PREVIEW_H;

      class_310 mc = class_310.method_1551();
      int sw = mc.method_22683().method_4489();
      int sh = mc.method_22683().method_4507();
      if (x1 < 2) { int d = 2 - x1; x1 += d; x2 += d; }
      if (x2 > sw - 2) { int d = x2 - (sw - 2); x1 -= d; x2 -= d; }
      if (y1 < 2) { int d = 2 - y1; y1 += d; y2 += d; }
      if (y2 > sh - 2) { int d = y2 - (sh - 2); y1 -= d; y2 -= d; }

      renderBody(ctx, skin, x1, y1, x2, y2, mouseX, mouseY);
   }

   @Override
   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = click.comp_4798(), my = click.comp_4799();
      int bx = btnX(), by = btnY(), bw = btnW(), bh = this.btnH;
      if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
         return super.method_25402(click, bl);   // normal button press (open the Skin Changer)
      }
      // Anywhere in the preview area starts a drag to reposition it.
      if (mx >= this.method_46426() && mx <= this.method_46426() + bw && my >= this.method_46427() && my <= by) {
         this.dragging = true;
         return true;
      }
      return false;
   }

   @Override
   public boolean method_25403(class_11909 click, double dx, double dy) {
      if (this.dragging) {
         this.dragOffX += (int) Math.round(dx);
         this.dragOffY += (int) Math.round(dy);
         return true;
      }
      return super.method_25403(click, dx, dy);
   }

   @Override
   public boolean method_25406(class_11909 click) {
      if (this.dragging) { this.dragging = false; return true; }
      return super.method_25406(click);
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
