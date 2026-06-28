package com.turtmod.kit;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4185;

/**
 * Read-only view of a saved kit's contents (armor/offhand/main/hotbar), rendered as inventory-style
 * slots. Opened from {@code /turtmod kit preview <name>} or the Kit Manager. Does not touch the
 * player's inventory.
 */
public class KitPreviewScreen extends class_437 {
   private final class_437 parent;
   private final String titleText;
   private final String data;       // SNBT to render
   private final String equipName;  // kit name to equip, or null for read-only (e.g. death items)
   private class_1799[] kit;
   private String error;

   private static final int SLOT = 18;

   public KitPreviewScreen(class_437 parent, String title, String data, String equipName) {
      super(class_2561.method_43470(title));
      this.parent = parent;
      this.titleText = title;
      this.data = data;
      this.equipName = equipName;
   }

   /** Preview a saved kit (with an Equip button). */
   public static KitPreviewScreen ofKit(class_437 parent, String name) {
      return new KitPreviewScreen(parent, "Kit: " + name, KitIO.load(name), name);
   }

   protected void method_25426() {
      if (!KitSerializer.isValid(this.data)) {
         this.error = "Data is corrupted or missing.";
      } else {
         this.kit = KitSerializer.deserializeIcons(this.data);
      }
      int btnW = 120;
      if (this.equipName != null) {
         this.method_37063(class_4185.method_46430(class_2561.method_43470("Equip This Kit"), b -> {
            String err = KitManager.loadKit(this.equipName);
            feedback(err == null ? "Equipping kit: " + this.equipName : err);
            this.method_25419();
         }).method_46434(this.field_22789 / 2 - btnW - 4, this.field_22790 - 30, btnW, 20).method_46431());
         this.method_37063(class_4185.method_46430(class_2561.method_43470("Back"), b -> this.method_25419())
            .method_46434(this.field_22789 / 2 + 4, this.field_22790 - 30, btnW, 20).method_46431());
      } else {
         this.method_37063(class_4185.method_46430(class_2561.method_43470("Back"), b -> this.method_25419())
            .method_46434(this.field_22789 / 2 - btnW / 2, this.field_22790 - 30, btnW, 20).method_46431());
      }
   }

   private void feedback(String msg) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1724 != null) {
         mc.field_1724.method_7353(com.turtmod.ui.TurtChat.message(class_2561.method_43470(msg)), false);
      }
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      class_327 font = this.field_22793;
      ctx.method_25300(font, this.method_25440().getString(), this.field_22789 / 2, 18, Palette.GREEN.getRGB());

      if (this.error != null) {
         ctx.method_25300(font, this.error, this.field_22789 / 2, this.field_22790 / 2, 0xFFFF5555);
         super.method_25394(ctx, mx, my, delta);
         return;
      }

      int gridW = 9 * SLOT;
      int left = (this.field_22789 - gridW) / 2;
      int top = 46;

      // Main inventory rows (indices 9..35) then hotbar (0..8) underneath, like the real inventory.
      ctx.method_51433(font, "Inventory", left, top - 11, -5592406, false);
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            drawSlot(ctx, font, 9 + row * 9 + col, left + col * SLOT, top + row * SLOT);
         }
      }
      int hotbarY = top + 3 * SLOT + 4;
      for (int col = 0; col < 9; col++) {
         drawSlot(ctx, font, col, left + col * SLOT, hotbarY);
      }

      // Armor (36..39) + offhand (40), grouped on the right of a labelled row below.
      int gearY = hotbarY + SLOT + 14;
      ctx.method_51433(font, "Armor & Offhand", left, gearY - 11, -5592406, false);
      for (int i = 0; i < 4; i++) {
         drawSlot(ctx, font, 39 - i, left + i * SLOT, gearY);
      }
      drawSlot(ctx, font, 40, left + 5 * SLOT, gearY);

      super.method_25394(ctx, mx, my, delta);
   }

   private void drawSlot(class_332 ctx, class_327 font, int index, int x, int y) {
      TurtUIUtils.drawRoundedRect(ctx, x, y, SLOT, SLOT, 3, new Color(255, 255, 255, 18));
      TurtUIUtils.drawRoundedBorder(ctx, x, y, SLOT, SLOT, 3, new Color(255, 255, 255, 28));
      if (this.kit != null && index < this.kit.length) {
         class_1799 stack = this.kit[index];
         if (stack != null && !stack.method_7960()) {
            ctx.method_51445(stack, x + 1, y + 1);
            ctx.method_51432(font, stack, x + 1, y + 1, null);
         }
      }
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
