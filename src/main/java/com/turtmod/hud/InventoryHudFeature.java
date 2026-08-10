package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class InventoryHudFeature {
   private static final int PANEL_PADDING = 4;
   private static final int TITLE_HEIGHT = 0;
   private static final int SLOT_SIZE = 18;
   private static final int SLOT_GAP = 2;

   private InventoryHudFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (config.hud.inventoryHudEnabled && client.field_1724 != null) {
         int x = HudEditorFeature.clampToScreenX(client, config.hud.inventoryHudX, getScaledWidth(config));
         int y = HudEditorFeature.clampToScreenY(client, config.hud.inventoryHudY, getScaledHeight(config));
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.inventoryHudScalePercent);
         PanelSize size = getBaseSize();
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         if (config.hud.inventoryHudBackground) {
            CustomThemeRenderer.renderThemedBox(context, x, y, size.width, size.height, config);
         }

         int startX = x + 4;
         int startY = y + 4 + (config.hud.inventoryHudBackground ? 0 : 0);

         for(int i = 0; i < 27; ++i) {
            int slotX = startX + i % 9 * 20;
            int slotY = startY + i / 9 * 20;
            class_1799 stack = client.field_1724.method_31548().method_5438(i + 9);
            CustomThemeRenderer.renderSlotCell(context, slotX, slotY, 18, 18, config, !stack.method_7960());
            if (!stack.method_7960()) {
               context.method_51427(stack, slotX + 1, slotY + 1);
               context.method_51431(client.field_1772, stack, slotX + 1, slotY + 1);
            }
         }

         context.method_51448().popMatrix();
      }
   }

   public static int getScaledWidth(TurtModConfig config) {
      return Math.round((float)getBaseSize().width * CustomThemeRenderer.getHudScale(config, config.hud.inventoryHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round((float)getBaseSize().height * CustomThemeRenderer.getHudScale(config, config.hud.inventoryHudScalePercent));
   }

   private static PanelSize getBaseSize() {
      int gridWidth = 178;
      int gridHeight = getGridHeight();
      return new PanelSize(8 + gridWidth, 8 + gridHeight);
   }

   private static int getGridHeight() {
      return 58;
   }

   private static record PanelSize(int width, int height) {
   }
}
