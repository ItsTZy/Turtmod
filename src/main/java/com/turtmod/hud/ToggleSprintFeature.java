package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import com.turtmod.mixin.client.GameOptionsAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class ToggleSprintFeature {
   // Emoji glyphs (match toggle-sprint-display's lang file).
   private static final String ICON_SPRINT = "🏃";
   private static final String ICON_SWIM   = "🏊";
   private static final String ICON_SNEAK  = "🦵";
   private static final int LINE_H = 10;

   private ToggleSprintFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      // No runtime state needed — "Toggled" vs "Held" is read from the control setting.
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 == null || !config.misc.enabled || !config.hud.toggleSprintHud) {
         return;
      }

      boolean isSprinting = client.field_1724.method_5624();
      boolean isSwimming = config.hud.sprintShowSwimming && client.field_1724.method_5681();
      boolean isSneaking = config.hud.sprintShowSneaking && client.field_1724.method_5715();
      boolean toggleMode = isToggleSprintEnabled(client);
      TurtModConfig.SprintDisplayStyle style = config.hud.sprintDisplayStyle;
      boolean transparentText = CustomThemeRenderer.isTransparentTextMode(config) || !config.hud.sprintShowBackground;
      int accent = CustomThemeRenderer.getAccentColor(config);
      int normal = CustomThemeRenderer.getTextColor(config);

      // Dimmed colour for the idle state (sprint line stays visible even when not sprinting).
      int muted = 0xFF8A8A8A;

      // Build the lines (and their colours). The primary sprint/swim line is ALWAYS shown so the HUD
      // stays put and doesn't disappear when you stop moving; it just dims while idle.
      List<String> lines = new ArrayList<>();
      List<Integer> colors = new ArrayList<>();
      if (isSwimming) {
         lines.add(style == TurtModConfig.SprintDisplayStyle.ICON ? ICON_SWIM : "Swimming");
         colors.add(normal);
      } else {
         switch (style) {
            case VERBOSE -> lines.add("Sprint " + (toggleMode ? "Toggled" : "Held"));
            case SHORT   -> lines.add(isSprinting ? "Sprinting" : "Sprint");
            case ICON    -> lines.add(ICON_SPRINT);
         }
         colors.add(isSprinting ? (toggleMode ? accent : normal) : muted);
      }
      if (isSneaking) {
         lines.add(style == TurtModConfig.SprintDisplayStyle.ICON ? ICON_SNEAK : "Sneaking");
         colors.add(normal);
      }
      // Icon mode collapses everything onto one line.
      if (style == TurtModConfig.SprintDisplayStyle.ICON && lines.size() > 1) {
         String merged = String.join(" ", lines);
         lines.clear();
         colors.clear();
         lines.add(merged);
         colors.add(normal);
      }

      if (lines.isEmpty()) {
         return;
      }

      int x = HudEditorFeature.clampToScreenX(client, config.hud.toggleSprintHudX, getScaledWidth(config));
      int y = HudEditorFeature.clampToScreenY(client, config.hud.toggleSprintHudY, getScaledHeight(config));
      float scale = CustomThemeRenderer.getHudScale(config, config.hud.toggleSprintHudScalePercent);
      class_327 font = client.field_1772;

      int maxW = 0;
      for (String s : lines) {
         maxW = Math.max(maxW, CustomThemeRenderer.textWidth(font, s, config));   // proper case in both modes
      }
      int boxW = maxW + (transparentText ? 0 : 12);
      int textLocalH = lines.size() * LINE_H + (transparentText ? 0 : 4);

      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float)(-x), (float)(-y));
      if (transparentText) {
         int ly = y;
         for (int i = 0; i < lines.size(); i++) {
            CustomThemeRenderer.renderBracketedText(context, font, lines.get(i), x, ly, colors.get(i), config);
            ly += LINE_H;
         }
      } else {
         CustomThemeRenderer.renderThemedBox(context, x, y, boxW, textLocalH, config);
         // Centre the stack vertically, then centre each line horizontally.
         int ly = y + Math.max(0, (textLocalH - lines.size() * LINE_H) / 2);
         for (int i = 0; i < lines.size(); i++) {
            String lineText = lines.get(i);   // proper case (no forced UPPERCASE), centered like the clean mode
            CustomThemeRenderer.drawHudLabel(context, font, lineText,
               CustomThemeRenderer.centeredTextX(font, lineText, x, boxW, config), ly, colors.get(i), config);
            ly += LINE_H;
         }
      }
      context.method_51448().popMatrix();
   }

   /** True when the player's controls use toggle-sprint (vanilla "toggleSprint" option). */
   private static boolean isToggleSprintEnabled(class_310 client) {
      try {
         Boolean v = ((GameOptionsAccessor)(Object)client.field_1690).turtmod$getToggleSprint().method_41753();
         return v != null && v;
      } catch (Throwable t) {
         return false;
      }
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      int baseWidth = 68;
      if (client != null && client.field_1772 != null) {
         int boxed = CustomThemeRenderer.textWidth(client.field_1772, "SPRINT TOGGLE", config) + 12;
         int transparent = CustomThemeRenderer.getBracketedTextWidth(client.field_1772, "sprint toggle", config);
         baseWidth = Math.max(boxed, transparent);
      }

      return Math.round((float)baseWidth * CustomThemeRenderer.getHudScale(config, config.hud.toggleSprintHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      return Math.round(14.0F * CustomThemeRenderer.getHudScale(config, config.hud.toggleSprintHudScalePercent));
   }
}
