package com.turtmod.hud;

import com.turtmod.ui.Palette;
import net.minecraft.class_327;
import net.minecraft.class_332;

/**
 * Draws simple, recognisable MOCK content inside each HUD box in the editor when there's no live player
 * (i.e. the main-menu editor), so you can see roughly what/where each HUD is without being in a world.
 * These are lightweight placeholders (rects + text), not the real renderers — in a world the actual HUDs
 * render behind the editor instead.
 */
public final class HudPreviewRenderer {
   private static final int TXT = 0xFFFFFFFF;
   private static final int MUTED = 0xFFB9C0C7;
   private static final int SLOT = 0xFF3A3F46;
   private static final int SLOT_BORDER = 0x80FFFFFF;
   private static int GREEN() { return Palette.GREEN.getRGB(); }

   private HudPreviewRenderer() {
   }

   public static void draw(class_332 ctx, class_327 font, HudEditorFeature.Anchor a, int x, int y, int w, int h) {
      // Clip so a mock never spills out of its box.
      ctx.method_44379(x, y, x + w, y + h);
      try {
         switch (a) {
            case ARMOR -> armor(ctx, x, y, w, h);
            case POTION -> potions(ctx, font, x, y);
            case OVERLAY -> text(ctx, font, x, y, "120 fps  20ms");
            case DEBUG -> lines(ctx, font, x, y, new String[]{"FPS: 120 (58-124)", "XYZ: 12.3 / 64 / -8.1", "Biome: plains"});
            case REACH -> text(ctx, font, x, y, "3.0 m");
            case SPRINT -> text(ctx, font, x, y, "▶ Sprinting");
            case KEYSTROKES -> keystrokes(ctx, font, x, y);
            case INVENTORY -> slots(ctx, x, y, 9, 1);
            case CPS_COUNTER -> text(ctx, font, x, y, "8 CPS");
            case COORDINATES -> text(ctx, font, x, y, "XYZ: 100 / 64 / -200");
            case HEALTH -> text(ctx, font, x, y, "❤ 20");
            case SCOREBOARD -> lines(ctx, font, x, y, new String[]{"Scoreboard", " Kills: 12", " Coins: 340"});
            case TITLE -> title(ctx, font, x, y, w, h);
            case BOSSBAR -> bossbar(ctx, font, x, y, w);
            default -> { }
         }
      } finally {
         ctx.method_44380();
      }
   }

   private static void text(class_332 ctx, class_327 font, int x, int y, String s) {
      ctx.method_51433(font, s, x + 1, y + 1, TXT, true);
   }

   private static void lines(class_332 ctx, class_327 font, int x, int y, String[] rows) {
      for (int i = 0; i < rows.length; i++) {
         ctx.method_51433(font, rows[i], x + 1, y + 1 + i * 9, i == 0 ? GREEN() : MUTED, true);
      }
   }

   private static void slot(class_332 ctx, int x, int y, int size) {
      ctx.method_25294(x, y, x + size, y + size, SLOT);
      ctx.method_73198(x, y, size, size, SLOT_BORDER);
   }

   private static void slots(class_332 ctx, int x, int y, int cols, int rows) {
      int s = 16, gap = 2;
      for (int r = 0; r < rows; r++) {
         for (int c = 0; c < cols; c++) {
            slot(ctx, x + c * (s + gap), y + r * (s + gap), s);
         }
      }
   }

   private static void armor(class_332 ctx, int x, int y, int w, int h) {
      // Four armour slots down a column, each with a little green durability bar.
      int s = 16, gap = 2;
      for (int i = 0; i < 4; i++) {
         int sy = y + i * (s + gap);
         slot(ctx, x, sy, s);
         ctx.method_25294(x + 2, sy + s - 3, x + 2 + (int) ((s - 4) * (1f - i * 0.2f)), sy + s - 2, GREEN());
      }
   }

   private static void potions(class_332 ctx, class_327 font, int x, int y) {
      String[] names = {"Speed", "Strength"};
      String[] times = {"1:30", "0:45"};
      for (int i = 0; i < 2; i++) {
         int sy = y + i * 20;
         slot(ctx, x, sy, 18);
         ctx.method_51433(font, names[i], x + 22, sy + 1, TXT, true);
         ctx.method_51433(font, times[i], x + 22, sy + 10, MUTED, true);
      }
   }

   private static void keystrokes(class_332 ctx, class_327 font, int x, int y) {
      int s = 14, gap = 2;
      key(ctx, font, x + s + gap, y, s, "W");
      key(ctx, font, x, y + s + gap, s, "A");
      key(ctx, font, x + s + gap, y + s + gap, s, "S");
      key(ctx, font, x + (s + gap) * 2, y + s + gap, s, "D");
   }

   private static void key(class_332 ctx, class_327 font, int x, int y, int s, String label) {
      ctx.method_25294(x, y, x + s, y + s, SLOT);
      ctx.method_73198(x, y, s, s, SLOT_BORDER);
      ctx.method_25300(font, label, x + s / 2, y + (s - 8) / 2, TXT);
   }

   private static void title(class_332 ctx, class_327 font, int x, int y, int w, int h) {
      // Big centred "Title" + smaller subtitle (matrix-scaled to read like the real one).
      int cx = x + w / 2;
      ctx.method_51448().pushMatrix();
      ctx.method_51448().translate(cx, y + 2);
      ctx.method_51448().scale(2.0f, 2.0f);
      ctx.method_25300(font, "Title", 0, 0, TXT);
      ctx.method_51448().popMatrix();
      ctx.method_25300(font, "Subtitle", cx, y + 20, MUTED);
   }

   private static void bossbar(class_332 ctx, class_327 font, int x, int y, int w) {
      ctx.method_25300(font, "Ender Dragon", x + w / 2, y, TXT);
      int by = y + 10;
      ctx.method_25294(x, by, x + w, by + 5, 0xFF5A2A6A);        // bar bg
      ctx.method_25294(x, by, x + (int) (w * 0.65f), by + 5, 0xFFC050E0);   // progress
   }
}
