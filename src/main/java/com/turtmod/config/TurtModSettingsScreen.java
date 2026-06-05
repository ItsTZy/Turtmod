package com.turtmod.config;

import com.turtmod.cosmetics.CosmeticsScreen;
import com.turtmod.gallery.ScreenshotGalleryScreen;
import com.turtmod.hud.HudEditorScreen;
import com.turtmod.ui.BrandingRenderer;
import com.turtmod.ui.BubbleParticle;
import com.turtmod.ui.TurtUIUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class TurtModSettingsScreen extends class_437 {
   private final class_437 parent;
   private float animTime = 0.0F;
   private final List<BubbleParticle> bubbles = new ArrayList();
   private final Random random = new Random();

   public TurtModSettingsScreen(class_437 parent) {
      super(class_2561.method_43470("TurtMod"));
      this.parent = parent;
   }

   protected void method_25426() {
      int centerX = this.field_22789 / 2;
      int y = this.field_22790 / 2 - 48;
      int w = 210;
      this.bubbles.clear();
      int safeW = Math.max(1, this.field_22789);
      int safeH = Math.max(1, this.field_22790);

      for(int i = 0; i < 25; ++i) {
         this.bubbles.add(new BubbleParticle(safeW, safeH, this.random));
      }

      this.method_37063(class_4185.method_46430(class_2561.method_43470("Settings"), (button) -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(TurtModWalksyConfigScreenFactory.create(this));
         }

      }).method_46434(centerX - w / 2, y, w, 22).method_46431());
      y += 26;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("HUD Editor"), (button) -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new HudEditorScreen(this));
         }

      }).method_46434(centerX - w / 2, y, w, 22).method_46431());
      y += 26;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Skin Changer"), (button) -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new CosmeticsScreen(this));
         }

      }).method_46434(centerX - w / 2, y, w, 22).method_46431());
      y += 26;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Screenshot Gallery"), (button) -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new ScreenshotGalleryScreen(this));
         }

      }).method_46434(centerX - w / 2, y, w, 22).method_46431());
      y += 32;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Back"), (button) -> this.method_25419()).method_46434(centerX - 50, y, 100, 20).method_46431());
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      TurtUIUtils.drawMenuBackdrop(context, this.field_22789, this.field_22790);
      this.animTime += delta;
      float alpha = class_3532.method_15363(this.animTime / 10.0F, 0.0F, 1.0F);

      for(BubbleParticle b : this.bubbles) {
         b.update(mouseX, mouseY, this.field_22789, this.field_22790, this.random);
         b.draw(context);
      }

      int accentColor = -12525360;
      int glassColor = -587198449;
      int borderColor = -13726889;
      int panelW = 252;
      int panelH = 215;
      int panelX = this.field_22789 / 2 - panelW / 2;
      int panelY = this.field_22790 / 2 - panelH / 2 - 5;
      int glowColor = (int)(34.0F * alpha) << 24 | 4251856;

      for(int i = 1; i <= 4; ++i) {
         context.method_73198(panelX - i, panelY - i, panelW + i * 2, panelH + i * 2, glowColor);
      }

      context.method_25294(panelX, panelY, panelX + panelW, panelY + panelH, glassColor);
      float shimmer = (class_3532.method_15374((double)(this.animTime * 0.05F)) + 1.0F) * 0.5F;
      context.method_73198(panelX, panelY, panelW, panelH, (int)(alpha * 255.0F) << 24 | borderColor);
      context.method_73198(panelX + 1, panelY + 1, panelW - 2, panelH - 2, (int)(alpha * (100.0F + shimmer * 50.0F)) << 24 | accentColor);
      float logoSwimY = class_3532.method_15374((double)(this.animTime * 0.04F)) * 3.0F;
      float logoSwimX = class_3532.method_15362((double)(this.animTime * 0.03F)) * 2.0F;
      int logoY = panelY + 28 + (int)logoSwimY;
      int logoX = panelX + panelW / 2 - 35 + (int)logoSwimX;
      BrandingRenderer.drawLogo(context, logoX, logoY, 70, 45);
      context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, panelY + 10, (int)(255.0F * alpha) << 24 | accentColor);
      context.method_25300(this.field_22793, "Menu: . | Zoom: C | Freelook: L-Alt", this.field_22789 / 2, panelY + panelH - 14, (int)(170.0F * alpha) << 24 | 9419919);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }

   }
}
