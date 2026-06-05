package com.turtmod.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class TurtToast {
   private static final List<ToastInstance> TOASTS = new ArrayList();

   public static void show(String title, String message) {
      TOASTS.add(new ToastInstance(title, message));
   }

   public static void render(class_332 context, float delta) {
      int x = context.method_51421() - 170;
      int y = context.method_51443() - 50;
      TOASTS.removeIf((t) -> t.dead);
      int offset = 0;

      for(ToastInstance toast : TOASTS) {
         toast.update(delta);
         drawToast(context, x, y - offset, toast);
         offset += 45;
      }

   }

   private static void drawToast(class_332 context, int x, int y, ToastInstance toast) {
      float alpha = toast.getAlpha();
      float slide = (1.0F - toast.getSlide()) * 20.0F;
      int tx = x + (int)slide;
      int w = 160;
      int h = 40;
      int glassColor = (int)(208.0F * alpha) << 24 | 5392;
      int borderColor = (int)(255.0F * alpha) << 24 | 3050327;
      int accentColor = (int)(255.0F * alpha) << 24 | 4251856;
      context.method_73198(tx - 1, y - 1, w + 2, h + 2, (int)(34.0F * alpha) << 24 | 4251856);
      context.method_25294(tx, y, tx + w, y + h, glassColor);
      context.method_73198(tx, y, w, h, borderColor);
      int barW = (int)((float)(w - 4) * (1.0F - toast.progress));
      context.method_25294(tx + 2, y + h - 3, tx + 2 + barW, y + h - 2, accentColor);
      class_310 client = class_310.method_1551();
      context.method_25303(client.field_1772, toast.title, tx + 8, y + 8, accentColor);
      context.method_51433(client.field_1772, toast.message, tx + 8, y + 20, (int)(200.0F * alpha) << 24 | 16777215, false);
      context.method_51433(client.field_1772, "\ud83d\udc22 Turt", tx + w - 35, y + h - 10, (int)(100.0F * alpha) << 24 | 9419919, false);
   }

   private static class ToastInstance {
      final String title;
      final String message;
      float progress = 0.0F;
      boolean dead = false;

      ToastInstance(String title, String message) {
         this.title = title;
         this.message = message;
      }

      void update(float delta) {
         this.progress += delta * 0.01F;
         if (this.progress >= 1.0F) {
            this.dead = true;
         }

      }

      float getAlpha() {
         if (this.progress < 0.1F) {
            return this.progress / 0.1F;
         } else {
            return this.progress > 0.9F ? (1.0F - this.progress) / 0.1F : 1.0F;
         }
      }

      float getSlide() {
         return this.progress < 0.1F ? this.progress / 0.1F : 1.0F;
      }
   }
}
