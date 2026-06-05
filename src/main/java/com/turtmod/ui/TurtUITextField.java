package com.turtmod.ui;

import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.class_327;
import net.minecraft.class_332;

public class TurtUITextField {
   public int x;
   public int y;
   public int width;
   public int height;
   private String text = "";
   private final String placeholder;
   private final TurtUITheme theme;
   private boolean focused = false;
   private final Consumer<String> onValueChange;
   private int cursorCounter = 0;
   private int maxLength = 256;

   public TurtUITextField(int x, int y, int width, int height, String placeholder, TurtUITheme theme, Consumer<String> onValueChange) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.placeholder = placeholder;
      this.theme = theme;
      this.onValueChange = onValueChange;
   }

   public void render(class_332 context, int mouseX, int mouseY, class_327 tr) {
      ++this.cursorCounter;
      TurtUIUtils.drawRectangle(context, this.x, this.y, this.width, this.height, this.theme.background());
      TurtUIUtils.drawBorder(context, this.x, this.y, this.width, this.height, this.focused ? this.theme.highlighted() : this.theme.border());
      String displayText = this.text;
      Color textColor = this.theme.text();
      if (this.text.isEmpty() && !this.focused) {
         displayText = this.placeholder;
         textColor = Color.GRAY;
      }

      context.method_51433(tr, displayText, this.x + 5, this.y + (this.height - 8) / 2, textColor.getRGB(), false);
      if (this.focused && this.cursorCounter / 10 % 2 == 0) {
         int cursorX = this.x + 5 + tr.method_1727(this.text);
         TurtUIUtils.drawRectangle(context, cursorX, this.y + 4, 1, this.height - 8, this.theme.highlighted());
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.focused = TurtUIUtils.isHovered((int)mouseX, (int)mouseY, this.x, this.y, this.width, this.height);
      return this.focused;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.focused) {
         return false;
      } else if (keyCode == 259) {
         if (!this.text.isEmpty()) {
            this.text = this.text.substring(0, this.text.length() - 1);
            this.onValueChange.accept(this.text);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (!this.focused) {
         return false;
      } else if (chr >= ' ' && chr <= '~' && this.text.length() < this.maxLength) {
         this.text = this.text + chr;
         this.onValueChange.accept(this.text);
         return true;
      } else {
         return false;
      }
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      if (text == null) {
         this.text = "";
      } else {
         this.text = text.length() <= this.maxLength ? text : text.substring(0, this.maxLength);
      }
   }

   public void setMaxLength(int maxLength) {
      this.maxLength = Math.max(1, maxLength);
      if (this.text.length() > this.maxLength) {
         this.text = this.text.substring(0, this.maxLength);
      }

   }
}
