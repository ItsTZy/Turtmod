package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import com.turtmod.ui.TurtUIUtils;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5250;

public final class CustomThemeRenderer {
   private CustomThemeRenderer() {
   }

   /** ARGB int → awt Colour, so the shared {@link TurtUIUtils} rounded drawing helpers can be used. */
   private static java.awt.Color col(int argb) {
      return new java.awt.Color(argb, true);
   }

   /** The theme's corner radius, clamped so it can never exceed the box it's rounding. */
   private static int radiusFor(TurtModConfig config, int w, int h) {
      int r = Math.max(0, Math.min(12, config.theme.cornerRadius));
      return Math.max(0, Math.min(r, Math.min(w, h) / 2 - 1));
   }

   /**
    * The shared HUD panel. Styled to match the mod's own menu cards: a rounded surface, an optional
    * glass highlight along the top edge, an optional 2px accent bar down the left, and a rounded
    * hairline border. Geometry (x/y/w/h) is unchanged so HUD-editor hitboxes stay aligned.
    */
   public static void renderThemedBox(class_332 context, int x, int y, int w, int h, TurtModConfig config) {
      // Lunar-clean: when the background is (near) off, draw NOTHING — just the HUD's own text. Otherwise
      // draw a single plain rounded background fill. No border, glass, or accent line (all removed).
      if (isTransparentTextMode(config)) {
         return;
      }
      int bg = getBackground(config);
      if (bg >>> 24 == 0) {
         return;
      }
      // Gradient background: a vertical multi-stop fade at the panel alpha when the bg colour has a gradient.
      TurtModConfig.GradientDef bgGrad = config.gradients.get(com.turtmod.config.GradientKeys.HUD_BG);
      if (bgGrad != null && bgGrad.isGradient()) {
         int a = getEffectiveBackgroundAlpha(config);
         if (a == 0) {
            return;
         }
         int n = Math.max(2, h);
         for (int i = 0; i < n; i++) {
            float t = (float) i / (n - 1);
            int col = (a << 24) | (bgGrad.colorAt(t) & 0xFFFFFF);
            int yy = y + Math.round(t * (h - 1));
            context.method_25294(x, yy, x + w, yy + 1, col);
         }
         return;
      }
      int r = radiusFor(config, w, h);
      if (r > 0) {
         TurtUIUtils.drawRoundedRect(context, x, y, w, h, r, col(bg));
      } else {
         context.method_25294(x, y, x + w, y + h, bg);
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
         int r = radiusFor(config, iw, ih);
         if (r > 0) {
            TurtUIUtils.drawRoundedBorder(context, ix, iy, iw, ih, r, col(color));
         } else {
            context.method_73198(ix, iy, iw, ih, color);
         }
      }

   }

   /** Baseline Y that vertically centres one 8px text line inside a box of height {@code boxH}. */
   public static int centeredTextY(int boxY, int boxH) {
      return boxY + Math.max(0, (boxH - 8) / 2);
   }

   /** Left X that horizontally centres {@code text} inside a box of width {@code boxW}. */
   public static int centeredTextX(class_327 tr, String text, int boxX, int boxW, TurtModConfig config) {
      return boxX + Math.max(0, (boxW - textWidth(tr, text, config)) / 2);
   }

   /** Draw a HUD label centred both ways inside its panel. */
   public static void drawHudLabelCentered(class_332 context, class_327 tr, String text, int boxX, int boxY, int boxW, int boxH, int color, TurtModConfig config) {
      drawHudLabel(context, tr, text, centeredTextX(tr, text, boxX, boxW, config), centeredTextY(boxY, boxH), color, config);
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
      int alpha = isTransparentTextMode(config) ? 110 : 120;   // subtle hairline, like the menu cards
      return applyHudOpacity(config, applyAlpha(border, alpha));
   }

   public static int getSlotBackground(TurtModConfig config, boolean active) {
      int effectiveAlpha = getEffectiveBackgroundAlpha(config);
      if (effectiveAlpha <= 0) {
         return 0;
      } else {
         // Clean mode (no background): idle slots draw nothing; only a pressed/active slot gets a subtle fill.
         int idleAlpha = isTransparentTextMode(config) ? 0 : 54;
         int activeAlpha = isTransparentTextMode(config) ? 60 : 94;
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
      // Slots are by far the most numerous HUD element (keystroke keys, armour/potion cells), so they
      // stay on plain fills unless they're big enough for rounding to actually be visible. This keeps
      // the per-frame draw count down - the rounded path costs ~45 fills per cell, a plain one costs 2.
      int r = (w < 16 || h < 16) ? 0
         : Math.max(0, Math.min(Math.min(3, config.theme.cornerRadius), Math.min(w, h) / 2 - 1));
      if (fill >>> 24 > 0) {
         if (r > 0) {
            TurtUIUtils.drawRoundedRect(context, x, y, w, h, r, col(fill));
         } else {
            context.method_25294(x, y, x + w, y + h, fill);
         }
      }

      if (config.theme.slotOutlines && config.theme.hudShowBorders) {
         int outline = active ? getAccentColor(config) : getBorder(config);
         if (r > 0) {
            TurtUIUtils.drawRoundedBorder(context, x, y, w, h, r, col(outline));
         } else {
            context.method_73198(x, y, w, h, outline);
         }
      }
   }

   /** Rounded fill honouring a caller-supplied ARGB colour, using the same corner radius as slot cells.
    *  Lets the keystrokes keys round their corners like every other HUD while still applying the user's
    *  custom pressed colour. Draws nothing for a fully-transparent fill. */
   public static void renderKeyCell(class_332 context, int x, int y, int w, int h, TurtModConfig config, int fillArgb) {
      if (fillArgb >>> 24 == 0) {
         return;
      }
      int r = (w < 16 || h < 16) ? 0
         : Math.max(0, Math.min(Math.min(3, config.theme.cornerRadius), Math.min(w, h) / 2 - 1));
      if (r > 0) {
         TurtUIUtils.drawRoundedRect(context, x, y, w, h, r, col(fillArgb));
      } else {
         context.method_25294(x, y, x + w, y + h, fillArgb);
      }
   }

   public static void renderBar(class_332 context, int x, int y, int w, int h, float progress, int fillColor, TurtModConfig config) {
      int clampedWidth = Math.max(1, w);
      context.method_25294(x, y, x + clampedWidth, y + h, applyHudOpacity(config, applyAlpha(0, 64)));
      int filled = Math.max(0, Math.min(clampedWidth, Math.round((float)clampedWidth * Math.max(0.0F, Math.min(1.0F, progress)))));
      if (filled > 0) {
         context.method_25294(x, y, x + filled, y + h, applyHudOpacity(config, fillColor));
      }

      // No hairline border in clean/no-background mode (it read as a stray line on every bar).
      if (!isTransparentTextMode(config)) {
         context.method_73198(x, y, clampedWidth, h, getBorder(config));
      }
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

   /**
    * Respect the alpha the user picked in the colour picker. Old configs stored colours as pure RGB
    * (alpha byte 0); treat that as fully opaque so nothing silently vanishes, but honour any real
    * picked alpha (1–255) so the picker's alpha slider actually affects the rendered colour.
    */
   public static int pickedArgb(int color) {
      int a = color >>> 24 & 255;
      return (a == 0 ? 0xFF000000 : a << 24) | color & 16777215;
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
      // Clean gradients: text drawn in the theme's Text colour (or Accent colour) renders as a per-glyph
      // multi-stop gradient when that colour has a gradient in the store. Other colours stay flat.
      int rgb = color & 0xFFFFFF;
      TurtModConfig.GradientDef g = null;
      if (rgb == (config.theme.hudTextColor & 0xFFFFFF)) {
         g = config.gradients.get(com.turtmod.config.GradientKeys.HUD_TEXT);
      } else if (rgb == (config.theme.hudAccentColor & 0xFFFFFF)) {
         g = config.gradients.get(com.turtmod.config.GradientKeys.HUD_ACCENT);
      }
      if (g != null && g.isGradient()) {
         drawGradientString(context, textRenderer, text.getString(), x, y, color, g, config);
         return;
      }

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

   /** Draw a string as a per-glyph horizontal multi-stop gradient (keeping the base colour's alpha). */
   private static void drawGradientString(class_332 context, class_327 tr, String s, int x, int y, int baseColor,
                                          TurtModConfig.GradientDef grad, TurtModConfig config) {
      int alpha = baseColor >>> 24 & 255;
      int total = Math.max(1, tr.method_1727(s));
      float phase = 0f;
      if (grad.animate) {
         float period = Math.max(500f, 6000f / Math.max(0.1f, grad.speed));
         phase = (System.currentTimeMillis() % (long) period) / period;
      }
      boolean bold = config.theme.hudTextBold;
      boolean shadow = config.theme.enableShadows;
      int cx = x;
      for (int i = 0; i < s.length(); i++) {
         String ch = String.valueOf(s.charAt(i));
         float t = (float)(cx - x) / total + phase;
         int col = (alpha << 24) | (grad.colorAt(t) & 0xFFFFFF);
         class_5250 gc = class_2561.method_43470(ch);
         if (bold) {
            gc.method_10862(gc.method_10866().method_10982(true));
         }
         if (shadow) {
            context.method_27535(tr, gc, cx, y, col);
         } else {
            context.method_51439(tr, gc, cx, y, col, false);
         }
         cx += textWidth(tr, ch, config);
      }
   }

   public static void drawHudLabel(class_332 context, class_327 textRenderer, String content, int x, int y, int color, TurtModConfig config) {
      drawText(context, textRenderer, class_2561.method_43470(content), x, y, color, config);
   }
}
