package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.hud.CustomThemeRenderer;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * BetterF3-style line manager for Clean F3: one row per line with an On/Off toggle and up/down arrows that
 * reorder {@code cleanF3Order} and flip the per-line visibility flags. A live preview on the right shows
 * exactly what the F3 overlay will look like (order + which lines are on + the global colours).
 */
public class CleanF3OrderScreen extends class_437 {
   private final class_437 parent;

   public CleanF3OrderScreen(class_437 parent) {
      super(class_2561.method_43470("Clean F3 — Lines"));
      this.parent = parent;
   }

   private static String labelFor(String key) {
      return switch (key) {
         case "fps" -> "FPS";
         case "pos" -> "XYZ";
         case "block" -> "Block";
         case "chunk" -> "Chunk";
         case "light" -> "Light";
         case "facing" -> "Facing";
         case "speed" -> "Speed";
         case "biome" -> "Biome";
         case "dim" -> "Dimension";
         case "daytime" -> "Time";
         case "held" -> "Held";
         case "mem" -> "Mem";
         case "look" -> "Looking at";
         default -> key;
      };
   }

   /** A representative value for the preview (works even outside a world). */
   private static String sampleFor(String key) {
      return switch (key) {
         case "fps" -> "120 | Ping: 24ms";
         case "pos" -> "12.34 / 64.00 / -8.10";
         case "block" -> "12 64 -9";
         case "chunk" -> "0 -1 | Local 12 0 7";
         case "light" -> "15 block, 15 sky";
         case "facing" -> "NORTH (-12.4 / 3.1)";
         case "speed" -> "4.32 m/s";
         case "biome" -> "minecraft:plains";
         case "dim" -> "minecraft:overworld";
         case "daytime" -> "Day 42 | 08:15";
         case "held" -> "Netherite Sword";
         case "mem" -> "38% 1200MB/3072MB";
         case "look" -> "block 10 63 -4";
         default -> "…";
      };
   }

   private static boolean enabled(TurtModConfig c, String key) {
      return switch (key) {
         case "fps" -> c.hud.cleanF3ShowFpsPing;
         case "pos" -> c.hud.cleanF3ShowPosition;
         case "block" -> c.hud.cleanF3ShowBlock;
         case "chunk" -> c.hud.cleanF3ShowChunk;
         case "light" -> c.hud.cleanF3ShowLight;
         case "facing" -> c.hud.cleanF3ShowFacing;
         case "speed" -> c.hud.cleanF3ShowSpeed;
         case "biome" -> c.hud.cleanF3ShowBiome;
         case "dim" -> c.hud.cleanF3ShowDimension;
         case "daytime" -> c.hud.cleanF3ShowDayTime;
         case "held" -> c.hud.cleanF3ShowHeldItem;
         case "mem" -> c.hud.cleanF3ShowMemory;
         case "look" -> c.hud.cleanF3ShowLookingAt;
         default -> true;
      };
   }

   private static void setEnabled(TurtModConfig c, String key, boolean v) {
      switch (key) {
         case "fps" -> c.hud.cleanF3ShowFpsPing = v;
         case "pos" -> c.hud.cleanF3ShowPosition = v;
         case "block" -> c.hud.cleanF3ShowBlock = v;
         case "chunk" -> c.hud.cleanF3ShowChunk = v;
         case "light" -> c.hud.cleanF3ShowLight = v;
         case "facing" -> c.hud.cleanF3ShowFacing = v;
         case "speed" -> c.hud.cleanF3ShowSpeed = v;
         case "biome" -> c.hud.cleanF3ShowBiome = v;
         case "dim" -> c.hud.cleanF3ShowDimension = v;
         case "daytime" -> c.hud.cleanF3ShowDayTime = v;
         case "held" -> c.hud.cleanF3ShowHeldItem = v;
         case "mem" -> c.hud.cleanF3ShowMemory = v;
         case "look" -> c.hud.cleanF3ShowLookingAt = v;
         default -> { }
      }
   }

   private List<String> order() {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg.hud.cleanF3Order == null || cfg.hud.cleanF3Order.isEmpty()) {
         cfg.hud.cleanF3Order = TurtModConfig.Hud.defaultCleanF3Order();
      }
      return cfg.hud.cleanF3Order;
   }

   private int listLeft, listTop, rowH = 20, listW = 250;

   protected void method_25426() {
      TurtModConfig cfg = TurtModClient.getConfig();
      List<String> order = order();
      int totalW = this.listW + 12 + 190;
      this.listLeft = (this.field_22789 - totalW) / 2;
      this.listTop = 44;

      for (int i = 0; i < order.size(); i++) {
         final int idx = i;
         String key = order.get(i);
         int y = this.listTop + i * rowH;
         // On/Off toggle.
         this.method_37063(class_4185.method_46430(class_2561.method_43470(enabled(cfg, key) ? "On" : "Off"), b -> {
            setEnabled(cfg, key, !enabled(cfg, key));
            ConfigManager.save(cfg);
            this.field_22787.method_1507(new CleanF3OrderScreen(this.parent));
         }).method_46434(this.listLeft + this.listW - 90, y, 40, 18).method_46431());
         // Up / down.
         this.method_37063(class_4185.method_46430(class_2561.method_43470("▲"), b -> {
            if (idx > 0) { java.util.Collections.swap(order, idx, idx - 1); ConfigManager.save(cfg); this.field_22787.method_1507(new CleanF3OrderScreen(this.parent)); }
         }).method_46434(this.listLeft + this.listW - 46, y, 20, 18).method_46431());
         this.method_37063(class_4185.method_46430(class_2561.method_43470("▼"), b -> {
            if (idx < order.size() - 1) { java.util.Collections.swap(order, idx, idx + 1); ConfigManager.save(cfg); this.field_22787.method_1507(new CleanF3OrderScreen(this.parent)); }
         }).method_46434(this.listLeft + this.listW - 24, y, 20, 18).method_46431());
      }

      this.method_37063(class_4185.method_46430(class_2561.method_43470("Reset"), b -> {
         cfg.hud.cleanF3Order = TurtModConfig.Hud.defaultCleanF3Order();
         ConfigManager.save(cfg);
         this.field_22787.method_1507(new CleanF3OrderScreen(this.parent));
      }).method_46434(this.listLeft, this.field_22790 - 28, this.listW / 2 - 3, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
         .method_46434(this.listLeft + this.listW / 2 + 3, this.field_22790 - 28, this.listW / 2 - 3, 20).method_46431());
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      TurtModConfig cfg = TurtModClient.getConfig();
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawGradientText(ctx, this.field_22793, "CLEAN F3 — LINES", this.field_22789 / 2, 16,
         Palette.GREEN, Palette.PINK, true, true);
      List<String> order = order();

      // Row labels (numbered, dimmed when the line is off).
      for (int i = 0; i < order.size(); i++) {
         String key = order.get(i);
         int y = this.listTop + i * rowH;
         boolean on = enabled(cfg, key);
         TurtUIUtils.drawRoundedRect(ctx, this.listLeft, y - 1, this.listW - 96, 18, 3, new Color(on ? 0x33000000 : 0x1A000000, true));
         ctx.method_51433(this.field_22793, (i + 1) + ".", this.listLeft + 5, y + 5, on ? Palette.GREEN.getRGB() : 0xFF5A5F66, false);
         ctx.method_51433(this.field_22793, labelFor(key), this.listLeft + 24, y + 5, on ? -1 : 0xFF6C7278, false);
      }

      // Live preview panel.
      int px = this.listLeft + this.listW + 12;
      int pw = 190;
      int ph = this.field_22790 - this.listTop - 40;
      TurtUIUtils.drawRoundedRect(ctx, px, this.listTop - 6, pw, ph, 5, Palette.alpha(Palette.PANEL_BG, 235));
      TurtUIUtils.drawRoundedBorder(ctx, px, this.listTop - 6, pw, ph, 5, Palette.alpha(Palette.PANEL_BORDER, 255));
      ctx.method_51433(this.field_22793, "PREVIEW", px + 8, this.listTop, Palette.GREEN.getRGB(), false);
      int labelColor = cfg.hud.cleanF3ThemeColors ? CustomThemeRenderer.getAccentColor(cfg) : cfg.hud.cleanF3LabelColor;
      int valueColor = cfg.hud.cleanF3ThemeColors ? CustomThemeRenderer.getTextColor(cfg) : cfg.hud.cleanF3ValueColor;
      int ly = this.listTop + 16;
      for (String key : order) {
         if (!enabled(cfg, key)) continue;
         String label = labelFor(key) + ": ";
         if (cfg.hud.cleanF3ShowBackground) {
            int w = this.field_22793.method_1727(label + sampleFor(key));
            ctx.method_25294(px + 7, ly - 1, px + 9 + w, ly + 8, 0x6F505050);
         }
         ctx.method_51433(this.field_22793, label, px + 8, ly, labelColor, false);
         ctx.method_51433(this.field_22793, sampleFor(key), px + 8 + this.field_22793.method_1727(label), ly, valueColor, false);
         ly += 9;
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
