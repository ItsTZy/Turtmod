package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * Reorders the Clean F3 lines: one row per line key with up/down buttons that rearrange
 * {@code cleanF3Order}. The render feature draws lines in this order. Disabled lines still appear
 * here (their visibility is controlled by the per-line toggles in the config).
 */
public class CleanF3OrderScreen extends class_437 {
   private final class_437 parent;

   public CleanF3OrderScreen(class_437 parent) {
      super(class_2561.method_43470("Clean F3 — Line Order"));
      this.parent = parent;
   }

   private static String labelFor(String key) {
      return switch (key) {
         case "fps" -> "FPS / Ping";
         case "pos" -> "XYZ";
         case "block" -> "Block";
         case "chunk" -> "Chunk";
         case "light" -> "Light";
         case "facing" -> "Facing";
         case "speed" -> "Speed";
         case "biome" -> "Biome";
         case "dim" -> "Dimension";
         case "daytime" -> "Day / Time";
         case "held" -> "Held Item";
         case "mem" -> "Memory";
         case "look" -> "Looking At";
         default -> key;
      };
   }

   private List<String> order() {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg.hud.cleanF3Order == null || cfg.hud.cleanF3Order.isEmpty()) {
         cfg.hud.cleanF3Order = TurtModConfig.Hud.defaultCleanF3Order();
      }
      return cfg.hud.cleanF3Order;
   }

   protected void method_25426() {
      TurtModConfig cfg = TurtModClient.getConfig();
      List<String> order = order();
      int panelW = Math.min(320, this.field_22789 - 40);
      int left = (this.field_22789 - panelW) / 2;
      int top = 48;
      int rowH = 22;

      for (int i = 0; i < order.size(); i++) {
         final int idx = i;
         int y = top + i * rowH;
         this.method_37063(class_4185.method_46430(class_2561.method_43470("▲"), b -> {
            if (idx > 0) {
               java.util.Collections.swap(order, idx, idx - 1);
               ConfigManager.save(cfg);
               this.field_22787.method_1507(new CleanF3OrderScreen(this.parent));
            }
         }).method_46434(left + panelW - 44, y, 20, 20).method_46431());
         this.method_37063(class_4185.method_46430(class_2561.method_43470("▼"), b -> {
            if (idx < order.size() - 1) {
               java.util.Collections.swap(order, idx, idx + 1);
               ConfigManager.save(cfg);
               this.field_22787.method_1507(new CleanF3OrderScreen(this.parent));
            }
         }).method_46434(left + panelW - 22, y, 20, 20).method_46431());
      }

      this.method_37063(class_4185.method_46430(class_2561.method_43470("Reset Order"), b -> {
         cfg.hud.cleanF3Order = TurtModConfig.Hud.defaultCleanF3Order();
         ConfigManager.save(cfg);
         this.field_22787.method_1507(new CleanF3OrderScreen(this.parent));
      }).method_46434(left, this.field_22790 - 52, panelW, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
         .method_46434(left, this.field_22790 - 28, panelW, 20).method_46431());
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      ctx.method_25300(this.field_22793, this.method_25440().getString(), this.field_22789 / 2, 18, Palette.GREEN.getRGB());
      List<String> order = order();
      int panelW = Math.min(320, this.field_22789 - 40);
      int left = (this.field_22789 - panelW) / 2;
      int top = 48;
      int rowH = 22;
      for (int i = 0; i < order.size(); i++) {
         int y = top + i * rowH;
         ctx.method_51433(this.field_22793, (i + 1) + ".", left + 4, y + 6, Palette.GREEN.getRGB(), false);
         ctx.method_51433(this.field_22793, labelFor(order.get(i)), left + 24, y + 6, -1, false);
      }
      super.method_25394(ctx, mx, my, delta);
   }

   public void method_25419() {
      ConfigManager.save(TurtModClient.getConfig());
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
