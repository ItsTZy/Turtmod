package com.turtmod.chat;

import com.turtmod.TurtModClient;
import com.turtmod.config.TurtModConfig;
import com.turtmod.ui.Palette;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3417;

/**
 * Animated corner preview shown after taking a screenshot (F2): the shot scales + fades into the
 * configured corner, holds for a few seconds, then drops away — with inline TurtMod-styled action
 * chips (view / copy / upload / delete). Original implementation; the actions reuse
 * {@link ScreenshotUploadFeature}. Fed a framebuffer image by {@code ScreenshotGrabMixin}, drawn each
 * HUD frame from {@code TurtModClient.onHudRender}, clicked via {@code MouseMixin}.
 */
public final class ScreenshotPreview {
   private static final class_2960 TEX_ID = class_2960.method_60655("turtmod", "screenshot_preview");
   private static class_1043 texture;
   private static int imgW, imgH;

   private static long shownAt = -1;
   private static long closeAt = -1;
   private static long flashAt = -1;
   private static long copyAt = -1;
   private static int hovered = -1;
   private static volatile boolean interactiveNow = false;

   private static final int[] bx = new int[4];
   private static final int[] by = new int[4];
   private static final int CHIP = 13, CHIP_GAP = 3;

   private static final long ENTER_MS = 240, EXIT_MS = 380, CLOSE_MS = 240, FLASH_MS = 320, COPY_MS = 340;

   private ScreenshotPreview() {
   }

   /** Fed the freshly-grabbed framebuffer image by the capture mixin (runs off-thread → hop to main). */
   public static void onCaptured(class_1011 image) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg == null || !cfg.misc.enabled || !cfg.hud.screenshotPreview) {
         image.close();
         return;
      }
      class_310 mc = class_310.method_1551();
      mc.execute(() -> {
         if (texture != null) {
            texture.close();
         }
         imgW = image.method_4307();
         imgH = image.method_4323();
         texture = new class_1043(() -> "turtmod_screenshot_preview", image);
         mc.method_1531().method_4616(TEX_ID, texture);
         long now = System.currentTimeMillis();
         shownAt = now;
         closeAt = -1;
         copyAt = -1;
         flashAt = now;
         hovered = -1;
         if (cfg.hud.screenshotShutterSound) {
            mc.method_1483().method_4870(class_1109.method_4758(class_3417.field_15015.comp_349(), 1.7F));
         }
      });
   }

   private static void dismissNow() {
      interactiveNow = false;
      shownAt = -1;
      closeAt = -1;
      flashAt = -1;
      copyAt = -1;
      hovered = -1;
      Arrays.fill(bx, -1000);
      Arrays.fill(by, -1000);
   }

   private static float easeOut(float t) {
      return 1f - (float) Math.pow(1f - t, 3);
   }

   private static float easeIn(float t) {
      return t * t * t;
   }

   private static int argb(Color c, int alpha) {
      return (Math.max(0, Math.min(255, alpha)) << 24) | (c.getRGB() & 0xFFFFFF);
   }

   public static void render(class_332 ctx, class_310 mc, TurtModConfig cfg) {
      if (shownAt < 0 || texture == null) {
         return;
      }
      if (!cfg.hud.screenshotPreview || mc.field_1724 == null) {
         dismissNow();
         return;
      }
      long now = System.currentTimeMillis();
      int sw = ctx.method_51421();
      int sh = ctx.method_51443();
      int w = Math.max(96, sw / 5);
      int h = imgW > 0 ? w * imgH / imgW : w * 9 / 16;
      int margin = 10;

      TurtModConfig.ScreenshotCorner corner = cfg.hud.screenshotPreviewCorner;
      boolean right = corner == TurtModConfig.ScreenshotCorner.BOTTOM_RIGHT || corner == TurtModConfig.ScreenshotCorner.TOP_RIGHT;
      boolean top = corner == TurtModConfig.ScreenshotCorner.TOP_RIGHT || corner == TurtModConfig.ScreenshotCorner.TOP_LEFT;
      int baseX = right ? sw - w - margin : margin;
      int baseY = top ? margin : sh - h - margin;

      float alpha = 1f, scale = 1f, offX = 0f, offY = 0f;
      long life = now - shownAt;
      long holdMs = Math.max(1, cfg.hud.screenshotPreviewSeconds) * 1000L;
      long exitStart = shownAt + holdMs;

      if (closeAt > 0) {
         long e = now - closeAt;
         if (e > CLOSE_MS) { dismissNow(); return; }
         float t = easeIn((float) e / CLOSE_MS);
         offX = (right ? 1 : -1) * (w + margin + 14) * t;
         alpha = 1f - t;
      } else if (now >= exitStart) {
         long e = now - exitStart;
         if (e > EXIT_MS) { dismissNow(); return; }
         float t = easeIn((float) e / EXIT_MS);
         offY = (top ? -1 : 1) * (h * 0.55f) * t;
         alpha = 1f - t;
      } else if (life < ENTER_MS) {
         float t = easeOut((float) life / ENTER_MS);
         scale = 1.12f - 0.12f * t;
         alpha = t;
      }

      int drawW = Math.round(w * scale);
      int drawH = Math.round(h * scale);
      int drawX = right ? baseX + (w - drawW) + Math.round(offX) : baseX + Math.round(offX);
      int drawY = top ? baseY + Math.round(offY) : baseY + (h - drawH) + Math.round(offY);
      int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));

      ctx.method_25294(drawX - 4, drawY - 3, drawX + drawW + 4, drawY + drawH + 5, argb(Color.BLACK, a * 40 / 255));
      ctx.method_25294(drawX - 2, drawY - 2, drawX + drawW + 2, drawY + drawH + 2, argb(Palette.PANEL_BG, a));
      ctx.method_25290(class_10799.field_56883, TEX_ID, drawX, drawY, 0f, 0f, drawW, drawH, drawW, drawH);
      if (a < 255) {
         ctx.method_25294(drawX, drawY, drawX + drawW, drawY + drawH, argb(Palette.PANEL_BG, 255 - a));
      }
      drawBorder(ctx, drawX - 2, drawY - 2, drawW + 4, drawH + 4, argb(Palette.GREEN, a), 1);

      if (flashAt > 0) {
         long fe = now - flashAt;
         if (fe < FLASH_MS) {
            int fa = Math.round((1f - (float) fe / FLASH_MS) * 200);
            ctx.method_25294(drawX, drawY, drawX + drawW, drawY + drawH, (fa << 24) | 0x00FFFFFF);
         } else {
            flashAt = -1;
         }
      }
      if (copyAt > 0) {
         long ce = now - copyAt;
         if (ce < COPY_MS) {
            int ca = Math.round((1f - (float) ce / COPY_MS) * 120);
            ctx.method_25294(drawX, drawY, drawX + drawW, drawY + drawH, argb(Palette.GREEN, ca));
         } else {
            copyAt = -1;
         }
      }

      boolean interactive = closeAt < 0 && now < exitStart && life >= ENTER_MS;
      interactiveNow = interactive;
      if (interactive) {
         updateHover(mc, ctx);
         int total = 4 * CHIP + 3 * CHIP_GAP;
         int rowX = drawX + drawW - total - 4;
         int rowY = drawY + drawH - CHIP - 4;
         for (int i = 0; i < 4; i++) {
            int cx = rowX + i * (CHIP + CHIP_GAP);
            bx[i] = cx;
            by[i] = rowY;
            boolean hov = hovered == i;
            ctx.method_25294(cx, rowY, cx + CHIP, rowY + CHIP, argb(hov ? Palette.GREEN : Palette.PANEL_BG, hov ? 235 : 205));
            drawBorder(ctx, cx, rowY, CHIP, CHIP, argb(hov ? Palette.GREEN : Palette.PANEL_BORDER, 235), 1);
            drawIcon(ctx, i, cx, rowY, hov ? Palette.PANEL_BG : Palette.TEXT);
         }
      } else {
         hovered = -1;
         Arrays.fill(bx, -1000);
         Arrays.fill(by, -1000);
      }
   }

   private static void updateHover(class_310 mc, class_332 ctx) {
      double sx = Math.max(1, mc.method_22683().method_4489());
      double sy = Math.max(1, mc.method_22683().method_4506());
      double mx = mc.field_1729.method_1603() * ctx.method_51421() / sx;
      double my = mc.field_1729.method_1604() * ctx.method_51443() / sy;
      hovered = -1;
      for (int i = 0; i < 4; i++) {
         if (mx >= bx[i] && mx <= bx[i] + CHIP && my >= by[i] && my <= by[i] + CHIP) {
            hovered = i;
         }
      }
   }

   /** Left-click while the preview is up: consume it if it hits a chip. Returns true if handled. */
   public static boolean onClick() {
      if (shownAt < 0 || closeAt > 0 || !interactiveNow) {
         return false;
      }
      class_310 mc = class_310.method_1551();
      double sx = Math.max(1, mc.method_22683().method_4489());
      double sy = Math.max(1, mc.method_22683().method_4506());
      double mx = mc.field_1729.method_1603() * mc.method_22683().method_4486() / sx;
      double my = mc.field_1729.method_1604() * mc.method_22683().method_4502() / sy;
      for (int i = 0; i < 4; i++) {
         if (mx >= bx[i] && mx <= bx[i] + CHIP && my >= by[i] && my <= by[i] + CHIP) {
            doAction(i, mc);
            return true;
         }
      }
      return false;
   }

   private static void doAction(int i, class_310 mc) {
      switch (i) {
         case 0 -> { ScreenshotUploadFeature.viewLastScreenshot(mc); closeAt = System.currentTimeMillis(); }
         case 1 -> { ScreenshotUploadFeature.copyLastScreenshotPath(mc); copyAt = System.currentTimeMillis(); }
         case 2 -> ScreenshotUploadFeature.uploadLastScreenshot(mc);
         case 3 -> { deleteNewest(mc); dismissNow(); }
         default -> { }
      }
   }

   private static void deleteNewest(class_310 mc) {
      try {
         File dir = new File(mc.field_1697, "screenshots");
         File[] files = dir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".png"));
         if (files == null || files.length == 0) {
            return;
         }
         Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
         files[0].delete();
      } catch (Exception ignored) {
      }
   }

   private static void drawBorder(class_332 ctx, int x, int y, int w, int h, int color, int t) {
      ctx.method_25294(x, y, x + w, y + t, color);
      ctx.method_25294(x, y + h - t, x + w, y + h, color);
      ctx.method_25294(x, y, x + t, y + h, color);
      ctx.method_25294(x + w - t, y, x + w, y + h, color);
   }

   private static void drawIcon(class_332 ctx, int type, int cx, int cy, Color col) {
      int c = argb(col, 255);
      int hole = argb(Palette.PANEL_BG, 255);
      int x = cx + 3, y = cy + 3;
      switch (type) {
         case 0 -> {
            ctx.method_25294(x, y + 2, x + 7, y + 5, c);
            ctx.method_25294(x + 2, y + 1, x + 5, y + 6, c);
            ctx.method_25294(x + 2, y + 2, x + 5, y + 5, hole);
            ctx.method_25294(x + 3, y + 3, x + 4, y + 4, c);
         }
         case 1 -> {
            drawBorder(ctx, x + 2, y, 5, 5, c, 1);
            drawBorder(ctx, x, y + 2, 5, 5, c, 1);
         }
         case 2 -> {
            ctx.method_25294(x + 3, y + 1, x + 4, y + 7, c);
            ctx.method_25294(x + 1, y + 3, x + 2, y + 4, c);
            ctx.method_25294(x + 2, y + 2, x + 3, y + 3, c);
            ctx.method_25294(x + 4, y + 2, x + 5, y + 3, c);
            ctx.method_25294(x + 5, y + 3, x + 6, y + 4, c);
         }
         case 3 -> {
            ctx.method_25294(x, y + 1, x + 7, y + 2, c);
            ctx.method_25294(x + 2, y, x + 5, y + 1, c);
            drawBorder(ctx, x + 1, y + 2, 5, 5, c, 1);
         }
         default -> { }
      }
   }
}
