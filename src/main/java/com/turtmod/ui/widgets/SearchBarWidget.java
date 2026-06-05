package com.turtmod.ui.widgets;

import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;

public class SearchBarWidget extends class_342 {
   private final Consumer<String> onSearch;
   private boolean hovered = false;
   private static final Color BG_COLOR = new Color(2958626, true);
   private static final Color BORDER_COLOR = new Color(7052869, true);
   private static final Color FOCUSED_COLOR = new Color(9289311, true);
   private static final Color PLACEHOLDER_COLOR = new Color(-2004318072);
   private static final Color TEXT_COLOR = new Color(-1);

   public SearchBarWidget(int x, int y, int width, int height, String placeholder, Consumer<String> onSearch) {
      super(class_310.method_1551().field_1772, x, y, width, height, class_2561.method_43470(placeholder));
      this.onSearch = onSearch;
      this.method_1880(64);
      this.method_47404(class_2561.method_43470(placeholder));
      this.method_1863(this::onTextChanged);
   }

   public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      if (this.method_1885()) {
         this.hovered = mouseX >= this.method_46426() && mouseX <= this.method_46426() + this.method_25368() && mouseY >= this.method_46427() && mouseY <= this.method_46427() + this.method_25364();
         int x = this.method_46426();
         int y = this.method_46427();
         int w = this.method_25368();
         int h = this.method_25364();
         context.method_25294(x + 1, y + 1, x + w - 1, y + h - 1, BG_COLOR.getRGB());
         int borderColor = this.method_25370() ? FOCUSED_COLOR.getRGB() : (this.hovered ? BORDER_COLOR.brighter().getRGB() : BORDER_COLOR.getRGB());
         context.method_73198(x, y, w, h, borderColor);
         context.method_25303(class_310.method_1551().field_1772, "\ud83d\udd0d ", x + 4, y + 6, -7487905);
         String text = this.method_1882();
         int textX = x + 18;
         int textY = y + (h - 8) / 2;
         if (text.isEmpty() && !this.method_25370()) {
            context.method_51439(class_310.method_1551().field_1772, this.method_25369(), textX, textY, PLACEHOLDER_COLOR.getRGB(), false);
         } else {
            context.method_25303(class_310.method_1551().field_1772, text, textX, textY, TEXT_COLOR.getRGB());
         }

         if (this.method_25370() && System.currentTimeMillis() / 500L % 2L == 0L) {
            int cursorX = textX + class_310.method_1551().field_1772.method_1727(text);
            context.method_25294(cursorX, textY - 1, cursorX + 1, textY + 9, TEXT_COLOR.getRGB());
         }

      }
   }

   private void onTextChanged(String text) {
      if (this.onSearch != null) {
         this.onSearch.accept(text);
      }

   }

   public boolean method_25402(class_11909 click, boolean bl) {
      boolean clicked = super.method_25402(click, bl);
      if (clicked && click.method_74245() == 0) {
         this.method_25365(true);
      }

      return clicked;
   }

   public boolean method_49606() {
      return this.hovered;
   }
}
