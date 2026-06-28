package com.turtmod.kit;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * Kit Loader GUI: type a name + Save to store the current inventory as a kit; the list below shows
 * saved kits — click a row to equip it, or click the ✕ to delete it. Pure hand-drawn rows (like
 * {@link com.turtmod.config.RegistryPickerScreen}) so no per-row widgets are needed.
 */
public class KitManagerScreen extends class_437 {
   private final class_437 parent;
   private List<String> kits;
   private class_342 nameField;
   private float scrollY = 0.0F;
   private float openFade = 0.0F;
   private long lastFrameNs = System.nanoTime();
   private String status = "";
   private int listTop;
   private int listBottom;
   private int listLeft;
   private int listW;
   private static final int ROW_H = 16;
   private static final Color ACCENT = Palette.GREEN;

   public KitManagerScreen(class_437 parent) {
      super(class_2561.method_43470("Kit Loader"));
      this.parent = parent;
   }

   protected void method_25426() {
      int panelW = Math.min(360, this.field_22789 - 40);
      int panelX = (this.field_22789 - panelW) / 2;
      this.listLeft = panelX + 10;
      this.listW = panelW - 20;
      this.listTop = 66;
      this.listBottom = this.field_22790 - 40;

      int saveBtnW = 56;
      this.nameField = new class_342(this.field_22793, this.listLeft, 38, this.listW - saveBtnW - 4, 18, class_2561.method_43470("Kit name"));
      this.nameField.method_1880(32);
      this.nameField.method_47404(class_2561.method_43470("New kit name..."));
      this.method_37063(this.nameField);
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Save"), b -> this.saveCurrent())
         .method_46434(this.listLeft + this.listW - saveBtnW, 38, saveBtnW, 18).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
         .method_46434(this.listLeft, this.field_22790 - 28, this.listW, 20).method_46431());

      this.kits = KitIO.listKits();
   }

   private void refresh() {
      this.kits = KitIO.listKits();
      this.scrollY = Math.max(0.0F, Math.min(this.scrollY, (float)maxScroll()));
   }

   private int maxScroll() {
      return Math.max(0, this.kits.size() * ROW_H - (this.listBottom - this.listTop));
   }

   private void saveCurrent() {
      String name = this.nameField.method_1882().trim();
      if (name.isEmpty()) {
         return;
      }
      if (class_310.method_1551().field_1724 == null) {
         this.feedback("Join a world to save a kit.");
         return;
      }
      if (KitIO.exists(name)) {
         this.feedback("A kit named '" + name + "' already exists.");
         return;
      }
      this.feedback(KitManager.saveKit(name) ? "Saved kit: " + name : "Failed to save kit.");
      this.nameField.method_1852("");
      this.refresh();
   }

   private void feedback(String msg) {
      this.status = msg;
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1724 != null) {
         mc.field_1724.method_7353(com.turtmod.ui.TurtChat.message(
            class_2561.method_43470(msg).method_27692(net.minecraft.class_124.field_1080)), false);
      }
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f);
      this.lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, dt, 12f);
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(ctx, mx, my);
      ctx.method_25300(this.field_22793, "Kit Loader", this.field_22789 / 2, 14, -1);
      ctx.method_25300(this.field_22793, "Click to equip · 👁 preview · ✕ delete", this.field_22789 / 2, 26, -7763575);
      // Special (Kit Loader): live count of saved kits.
      ctx.method_25300(this.field_22793, "🐢 " + this.kits.size() + (this.kits.size() == 1 ? " kit saved" : " kits saved"),
         this.field_22789 / 2, 38, com.turtmod.ui.Palette.GREEN.getRGB());

      ctx.method_44379(this.listLeft, this.listTop, this.listLeft + this.listW, this.listBottom);
      int y = this.listTop - Math.round(this.scrollY);
      if (this.kits.isEmpty()) {
         ctx.method_51433(this.field_22793, "No kits saved yet.", this.listLeft + 4, this.listTop + 4, -7763575, false);
      }
      int delBox = 10;
      int delX = this.listLeft + this.listW - delBox - 4;
      int prevX = delX - 16;
      for (String name : this.kits) {
         if (y + ROW_H >= this.listTop && y <= this.listBottom) {
            boolean hovRow = mx >= this.listLeft && mx < prevX && my >= y && my <= y + ROW_H;
            boolean hovPrev = mx >= prevX && mx <= prevX + delBox && my >= y + 3 && my <= y + 3 + delBox;
            boolean hovDel = mx >= delX && mx <= delX + delBox && my >= y + 3 && my <= y + 3 + delBox;
            if (hovRow) {
               ctx.method_25294(this.listLeft, y, prevX - 2, y + ROW_H, 0x22FFFFFF);
            }
            ctx.method_51433(this.field_22793, name, this.listLeft + 4, y + 4, hovRow ? ACCENT.getRGB() : -1, false);
            ctx.method_51433(this.field_22793, "👁", prevX, y + 4, hovPrev ? -1 : -5592406, false);
            ctx.method_25294(delX, y + 3, delX + delBox, y + 3 + delBox, hovDel ? 0xFFFF5555 : 0x55FF5555);
            ctx.method_51433(this.field_22793, "✕", delX + 2, y + 4, -1, false);
         }
         y += ROW_H;
      }
      ctx.method_44380();
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      if (super.method_25402(click, bl)) {
         return true;
      }
      int button = click.method_74245();
      double mxx = click.comp_4798();
      double myy = click.comp_4799();
      if (button == 0 && myy >= this.listTop && myy <= this.listBottom && mxx >= this.listLeft && mxx <= this.listLeft + this.listW) {
         int idx = (int)((myy - this.listTop + this.scrollY) / ROW_H);
         if (idx >= 0 && idx < this.kits.size()) {
            String name = this.kits.get(idx);
            int delBox = 10;
            int delX = this.listLeft + this.listW - delBox - 4;
            int prevX = delX - 16;
            if (mxx >= delX && mxx <= delX + delBox) {
               try {
                  KitIO.moveToTrash(name);
                  this.feedback("Deleted kit: " + name);
               } catch (Exception e) {
                  this.feedback("Could not delete kit.");
               }
               this.refresh();
            } else if (mxx >= prevX && mxx <= prevX + delBox) {
               this.field_22787.method_1507(KitPreviewScreen.ofKit(this, name));
            } else {
               String err = KitManager.loadKit(name);
               this.feedback(err == null ? "Equipping kit: " + name : err);
               this.method_25419();
            }
            return true;
         }
      }
      return false;
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (maxScroll() > 0 && mouseY >= this.listTop && mouseY <= this.listBottom) {
         this.scrollY = Math.max(0.0F, Math.min(this.scrollY - (float)verticalAmount * 18.0F, (float)maxScroll()));
         return true;
      }
      return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
