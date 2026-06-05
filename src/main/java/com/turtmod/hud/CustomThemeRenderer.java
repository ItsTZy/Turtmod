package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5250;

public final class CustomThemeRenderer {
   private CustomThemeRenderer() {
   }

   public static void renderThemedBox(class_332 context, int x, int y, int w, int h, TurtModConfig config) {
      int bg = getBackground(config);
      int border = getBorder(config);
      if (bg >>> 24 > 0) {
         context.method_25294(x, y, x + w, y + h, bg);
      }

      if (config.theme.hudShowBorders && !isTransparentTextMode(config) && w > 10 && h > 6) {
         int accentLine = applyHudOpacity(config, applyAlpha(config.theme.hudAccentColor, 30));
         context.method_25294(x + 1, y + 1, x + w - 1, y + 2, accentLine);
      }

      if (config.theme.hudShowBorders && config.theme.hudBorderThickness > 0) {
         renderSimpleBorder(context, x, y, w, h, config.theme, config);
      }

   }

   private static void renderSimpleBorder(class_332 context, int x, int y, int w, int h, TurtModConfig.CustomTheme theme, TurtModConfig config) {
      int color = getBorder(config);
      int thickness = Math.max(1, theme.hudBorderThickness);

      for(int i = 0; i < thickness; ++i) {
         int ix = x + i;
         int iy = y + i;
         int iw = Math.max(1, w - i * 2);
         int ih = Math.max(1, h - i * 2);
         context.method_73198(ix, iy, iw, ih, color);
      }

   }

   public static void renderThemedText(class_332 context, class_327 textRenderer, class_2561 text, int x, int y, TurtModConfig config) {
      drawText(context, textRenderer, text, x, y, getTextColor(config), config);
   }

   public static int getTextColor(TurtModConfig config) {
      return applyHudOpacity(config, applyAlpha(config.theme.hudTextColor, 255));
   }

   public static int getMutedTextColor(TurtModConfig config) {
      return applyHudOpacity(config, applyAlpha(mix(config.theme.hudTextColor, 8030099, 0.38F), 255));
   }

   public static int getAccentColor(TurtModConfig config) {
      return applyHudOpacity(config, -16777216 | config.theme.hudAccentColor);
   }

   public static int getBackground(TurtModConfig config) {
      return applyHudOpacity(config, applyAlpha(config.theme.hudBackgroundColor, config.theme.hudBackgroundAlpha));
   }

   public static int getBorder(TurtModConfig config) {
      int border = isTransparentTextMode(config) ? mix(config.theme.hudTextColor, config.theme.hudAccentColor, 0.35F) : mix(config.theme.hudBorderColor, config.theme.hudAccentColor, 0.18F);
      int alpha = isTransparentTextMode(config) ? 160 : 220;
      return applyHudOpacity(config, applyAlpha(border, alpha));
   }

   public static int getSlotBackground(TurtModConfig config, boolean active) {
      int effectiveAlpha = getEffectiveBackgroundAlpha(config);
      if (effectiveAlpha <= 0) {
         return 0;
      } else {
         int idleAlpha = isTransparentTextMode(config) ? 16 : 54;
         int activeAlpha = isTransparentTextMode(config) ? 54 : 94;
         int color = active ? mix(config.theme.hudBackgroundColor, config.theme.hudAccentColor, 0.26F) : mix(config.theme.hudBackgroundColor, 16777215, 0.05F);
         return applyHudOpacity(config, applyAlpha(color, active ? activeAlpha : idleAlpha));
      }
   }

   public static int getKeyBackground(TurtModConfig config, boolean active) {
      return getSlotBackground(config, active);
   }

   public static int getKeyTextColor(TurtModConfig config, boolean active) {
      return active ? applyHudOpacity(config, applyAlpha(mix(config.theme.hudTextColor, 16777215, 0.3F), 255)) : getTextColor(config);
   }

   public static void renderSlotCell(class_332 context, int x, int y, int w, int h, TurtModConfig config, boolean active) {
      int fill = getSlotBackground(config, active);
      if (fill >>> 24 > 0) {
         context.method_25294(x, y, x + w, y + h, fill);
      }

      if (config.theme.slotOutlines && config.theme.hudShowBorders) {
         context.method_73198(x, y, w, h, active ? getAccentColor(config) : getBorder(config));
      }
   }

   public static void renderBar(class_332 context, int x, int y, int w, int h, float progress, int fillColor, TurtModConfig config) {
      int clampedWidth = Math.max(1, w);
      context.method_25294(x, y, x + clampedWidth, y + h, applyHudOpacity(config, applyAlpha(0, 64)));
      int filled = Math.max(0, Math.min(clampedWidth, Math.round((float)clampedWidth * Math.max(0.0F, Math.min(1.0F, progress)))));
      if (filled > 0) {
         context.method_25294(x, y, x + filled, y + h, applyHudOpacity(config, fillColor));
      }

      context.method_73198(x, y, clampedWidth, h, getBorder(config));
   }

   public static float getHudScale(TurtModConfig config, int localPercent) {
      return (float)Math.max(50, Math.min(300, localPercent)) / 100.0F * getGlobalHudScale(config);
   }

   public static float getGlobalHudScale(TurtModConfig config) {
      return (float)Math.max(50, Math.min(300, config.hud.globalHudScalePercent)) / 100.0F;
   }

   public static boolean isTransparentTextMode(TurtModConfig config) {
      return getEffectiveBackgroundAlpha(config) <= 28;
   }

   public static int getEffectiveBackgroundAlpha(TurtModConfig config) {
      int alpha = Math.max(0, Math.min(255, config.theme.hudBackgroundAlpha));
      alpha = alpha * Math.max(0, Math.min(100, config.theme.themeAlphaPercent)) / 100;
      alpha = alpha * Math.max(5, Math.min(100, config.hud.globalHudOpacityPercent)) / 100;
      return alpha;
   }

   public static int getBracketColor(TurtModConfig config) {
      return applyHudOpacity(config, applyAlpha(mix(config.theme.hudAccentColor, config.theme.hudTextColor, 0.12F), 255));
   }

   // Bold-aware text width. When "Bold Text" is enabled, drawText() applies the bold style, which is
   // ~1px wider per glyph. Layout/box math must measure with that same style or bold text overflows
   // its box and overlaps neighbouring HUD elements. Use this everywhere a width drives layout.
   public static int textWidth(class_327 textRenderer, String content, TurtModConfig config) {
      if (config != null && config.theme.hudTextBold) {
         return textRenderer.method_27525(class_2561.method_43470(content).method_27696(class_2583.field_24360.method_10982(true)));
      }
      return textRenderer.method_1727(content);
   }

   // Frameless (no-box) HUDs render plain text — no [ ] brackets — per user preference. The width
   // helper therefore reports just the content width so layouts stay aligned.
   public static int getBracketedTextWidth(class_327 textRenderer, String content) {
      return textRenderer.method_1727(content);
   }

   // Bold-aware overload — prefer this in layout code so bold text doesn't overflow.
   public static int getBracketedTextWidth(class_327 textRenderer, String content, TurtModConfig config) {
      return textWidth(textRenderer, content, config);
   }

   public static int renderBracketedText(class_332 context, class_327 textRenderer, String content, int x, int y, int contentColor, TurtModConfig config) {
      drawText(context, textRenderer, class_2561.method_43470(content), x, y, contentColor, config);
      // Advance by the bold-aware width so the next inline element doesn't overlap when bold is on.
      return x + textWidth(textRenderer, content, config);
   }

   public static int applyHudOpacity(TurtModConfig config, int argbColor) {
      int hudPercent = Math.max(5, Math.min(100, config.hud.globalHudOpacityPercent));
      int themePercent = Math.max(0, Math.min(100, config.theme.themeAlphaPercent));
      int alpha = argbColor >>> 24 & 255;
      alpha = alpha * themePercent / 100;
      alpha = alpha * hudPercent / 100;
      return alpha << 24 | argbColor & 16777215;
   }

   public static int applyAlpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   public static int mix(int a, int b, float t) {
      float c = Math.max(0.0F, Math.min(1.0F, t));
      int ar = a >> 16 & 255;
      int ag = a >> 8 & 255;
      int ab = a & 255;
      int br = b >> 16 & 255;
      int bg = b >> 8 & 255;
      int bb = b & 255;
      return Math.round((float)ar + (float)(br - ar) * c) << 16 | Math.round((float)ag + (float)(bg - ag) * c) << 8 | Math.round((float)ab + (float)(bb - ab) * c);
   }

   private static void drawText(class_332 context, class_327 textRenderer, class_2561 text, int x, int y, int color, TurtModConfig config) {
      class_5250 drawn = text.method_27661();
      if (config.theme.hudTextBold) {
         drawn.method_10862(drawn.method_10866().method_10982(true));
      }

      if (config.theme.enableShadows) {
         context.method_27535(textRenderer, drawn, x, y, color);
      } else {
         context.method_51439(textRenderer, drawn, x, y, color, false);
      }

   }

   public static void drawHudLabel(class_332 context, class_327 textRenderer, String content, int x, int y, int color, TurtModConfig config) {
      drawText(context, textRenderer, class_2561.method_43470(content), x, y, color, config);
   }
}
