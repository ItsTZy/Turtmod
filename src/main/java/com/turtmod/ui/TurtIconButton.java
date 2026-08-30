package com.turtmod.ui;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;

/**
 * A vanilla-style button that shows a {@link TurtIcons} pixel icon instead of text — used for the TurtMod
 * buttons injected onto Minecraft's title / pause menus. Mirrors {@link TurtLogoButton}.
 */
//? if >=1.21.11 {
public class TurtIconButton extends class_4185.class_12231 {
//?} else {
/*public class TurtIconButton extends class_4185 {
*///?}
   private final String[] icon;

   public TurtIconButton(int x, int y, int w, int h, String[] icon, class_4185.class_4241 onPress) {
      super(x, y, w, h, class_2561.method_43473(), onPress, field_40754);
      this.icon = icon;
   }

   @Override
   //? if >=1.21.11 {
   protected void method_75752(class_332 ctx, int mouseX, int mouseY, float delta) {
   //?} else {
   /*protected void method_48579(class_332 ctx, int mouseX, int mouseY, float delta) {
   *///?}
      //? if >=1.21.11 {
      super.method_75752(ctx, mouseX, mouseY, delta);
      //?} else {
      /*super.method_48579(ctx, mouseX, mouseY, delta);
      *///?}
      int box = Math.min(this.method_25368(), this.method_25364()) - 6;
      int ix = this.method_46426() + (this.method_25368() - box) / 2;
      int iy = this.method_46427() + (this.method_25364() - box) / 2;
      TurtIcons.drawFit(ctx, this.icon, ix, iy, box);
   }
}
