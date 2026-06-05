package com.turtmod.ui.popup;

import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_5481;

public class WarningPopup {
   private final class_437 parent;
   private final String title;
   private final String message;
   private final Runnable onConfirm;
   private final Runnable onCancel;
   private boolean confirmed;
   private static final int POPUP_WIDTH = 300;
   private static final int POPUP_HEIGHT = 150;
   private static final Color BG_COLOR = new Color(1709588, true);
   private static final Color BORDER_COLOR = new Color(16734810, true);
   private static final Color BTN_CONFIRM = new Color(16734810, false);
   private static final Color BTN_CANCEL = new Color(9289311, false);

   public WarningPopup(class_437 parent, String title, String message, Runnable onConfirm) {
      this(parent, title, message, onConfirm, (Runnable)null);
   }

   public WarningPopup(class_437 parent, String title, String message, Runnable onConfirm, Runnable onCancel) {
      this.confirmed = false;
      this.parent = parent;
      this.title = title;
      this.message = message;
      this.onConfirm = onConfirm;
      this.onCancel = onCancel;
   }

   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_310 client = class_310.method_1551();
      int centerX = client.method_22683().method_4486() / 2;
      int centerY = client.method_22683().method_4502() / 2;
      int x = centerX - 150;
      int y = centerY - 75;
      TurtUIUtils.drawShadow(context, x, y, 300, 150, 10);
      context.method_25294(x, y, x + 300, y + 150, BG_COLOR.getRGB());
      context.method_73198(x, y, 300, 150, BORDER_COLOR.getRGB());
      context.method_25300(client.field_1772, this.title, centerX, y + 10, -42406);
      int textY = y + 30;

      for(class_5481 line : client.field_1772.method_1728(class_2561.method_43470(this.message), 280)) {
         context.method_35719(client.field_1772, line, centerX, textY, -1);
         textY += 12;
      }

      int btnY = y + 150 - 30;
      int btnW = 80;
      int btnH = 20;
      int btnGap = 10;
      int confirmX = centerX - btnW - btnGap / 2;
      boolean hoverConfirm = mouseX >= confirmX && mouseX <= confirmX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
      context.method_25294(confirmX, btnY, confirmX + btnW, btnY + btnH, hoverConfirm ? BTN_CONFIRM.brighter().getRGB() : BTN_CONFIRM.getRGB());
      context.method_73198(confirmX, btnY, btnW, btnH, -1);
      context.method_25300(client.field_1772, "Confirm", confirmX + btnW / 2, btnY + 6, -1);
      int cancelX = centerX + btnGap / 2;
      boolean hoverCancel = mouseX >= cancelX && mouseX <= cancelX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
      context.method_25294(cancelX, btnY, cancelX + btnW, btnY + btnH, hoverCancel ? BTN_CANCEL.brighter().getRGB() : BTN_CANCEL.getRGB());
      context.method_73198(cancelX, btnY, btnW, btnH, -1);
      context.method_25300(client.field_1772, "Cancel", cancelX + btnW / 2, btnY + 6, -1);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         class_310 client = class_310.method_1551();
         int centerX = client.method_22683().method_4486() / 2;
         int centerY = client.method_22683().method_4502() / 2;
         int x = centerX - 150;
         int y = centerY - 75;
         int btnY = y + 150 - 30;
         int btnW = 80;
         int btnH = 20;
         int btnGap = 10;
         int confirmX = centerX - btnW - btnGap / 2;
         if (mouseX >= (double)confirmX && mouseX <= (double)(confirmX + btnW) && mouseY >= (double)btnY && mouseY <= (double)(btnY + btnH)) {
            this.confirmed = true;
            if (this.onConfirm != null) {
               this.onConfirm.run();
            }

            return true;
         } else {
            int cancelX = centerX + btnGap / 2;
            if (mouseX >= (double)cancelX && mouseX <= (double)(cancelX + btnW) && mouseY >= (double)btnY && mouseY <= (double)(btnY + btnH)) {
               this.confirmed = false;
               if (this.onCancel != null) {
                  this.onCancel.run();
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   public boolean isConfirmed() {
      return this.confirmed;
   }
}
