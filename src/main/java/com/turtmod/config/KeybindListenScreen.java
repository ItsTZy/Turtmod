package com.turtmod.config;

import java.util.function.Supplier;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;

/** Tiny full-screen overlay that listens for the next key/mouse press and assigns it to a
 *  keybind, then returns to the module settings page (rebuilt so the new key shows). This is
 *  what makes keybinds editable from inside the TurtMod config — no vanilla Controls screen needed. */
public class KeybindListenScreen extends class_437 {
   private final class_304 binding;
   private final Supplier<class_437> returnScreen;

   public KeybindListenScreen(class_304 binding, Supplier<class_437> returnScreen) {
      super(class_2561.method_43470("Set Keybind"));
      this.binding = binding;
      this.returnScreen = returnScreen;
   }

   private void apply(class_3675.class_306 key) {
      this.binding.method_1422(key);
      class_304.method_1426();
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1690 != null) {
         mc.field_1690.method_1640();
      }
      this.back();
   }

   private void back() {
      class_310 mc = class_310.method_1551();
      if (mc != null) {
         mc.method_1507(this.returnScreen.get());
      }
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      ctx.method_25294(0, 0, this.field_22789, this.field_22790, -872415232);
      int cx = this.field_22789 / 2;
      int cy = this.field_22790 / 2;
      ctx.method_25300(this.field_22793, "Press a key or mouse button to bind", cx, cy - 22, -1);
      ctx.method_25300(this.field_22793, "Current: " + this.binding.method_16007().getString(), cx, cy - 6, -5570646);
      ctx.method_25300(this.field_22793, "ESC = unbind", cx, cy + 14, -5592406);
      super.method_25394(ctx, mx, my, delta);
   }

   public boolean method_25404(class_11908 input) {
      if (input.comp_4795() == 256) {
         this.apply(class_3675.field_16237);
      } else {
         this.apply(class_3675.method_15985(input));
      }
      return true;
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      this.apply(class_3675.class_307.field_1672.method_1447(click.method_74245()));
      return true;
   }

   public boolean method_25421() {
      return false;
   }
}
