package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1074;
import net.minecraft.class_10799;
import net.minecraft.class_1291;
import net.minecraft.class_1292;
import net.minecraft.class_1293;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_3532;

public final class HudPanelsFeature {
   private static final int SLOT_SIZE = 18;
   private static final int GAP = 2;
   private static final int ARMOR_SLOT_GAP = 3;
   private static final int PANEL_PADDING = 4;
   private static final int TITLE_HEIGHT = 0;
   private static final int ARMOR_TEXT_WIDTH = 24;
   private static final int ARMOR_DURABILITY_GAP = 4;
   // Icons-only cell pitch. Each pot is a 16px icon inside a 20px slot bubble; a 22px pitch leaves a
   // tight, even 2px gap between bubbles (was 26/24 → a loose ~6px gap the user disliked).
   private static final int POTION_CELL_WIDTH = 22;
   private static final int POTION_CELL_HEIGHT = 22;
   private static final int POTION_MAX_SIMPLE_EFFECTS = 8;
   private static final int POTION_FULL_COL_WIDTH = 132;
   private static final int POTION_FULL_ROW_H = 22;
   private static final int POTION_COMPACT_COL_WIDTH = 116;
   private static final int POTION_COMPACT_ROW_H = 18;
   // Vanilla hotbar tray sprite (class_329.field_45310 = "hud/hotbar"), 182x22, registered as a
   // nine-slice sprite so it stretches cleanly to any width without distorting the slot dividers.
   private static final class_2960 HOTBAR_SPRITE = class_2960.method_60656("hud/hotbar");
   private static final int HOTBAR_STEP = 20;
   private static final int HOTBAR_HEIGHT = 22;
   private static final class_1304[] ARMOR_ORDER;

   private HudPanelsFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled) {
         if (config.hud.movableArmorHud && hasArmorHudItems(client, config)) {
            renderArmorHud(context, client, config);
         }

         if (config.hud.movablePotionHud && !client.field_1724.method_6026().isEmpty()) {
            renderPotionHud(context, client, config);
         }

         if (config.combat.compactHeartsMode) {
            renderCompactHearts(context, client, config);
         }

      }
   }

   private static boolean hasArmorHudItems(class_310 client, TurtModConfig config) {
      if (client.field_1724 == null) {
         return false;
      } else {
         for(class_1304 slot : ARMOR_ORDER) {
            if (!client.field_1724.method_6118(slot).method_7960()) {
               return true;
            }
         }

         if (config.hud.armorHudShowOffhand && !client.field_1724.method_6079().method_7960()) {
            return true;
         }

         return config.hud.armorHudShowMainHand && !client.field_1724.method_6047().method_7960();
      }
   }

   private static boolean isHotbarTray(TurtModConfig config) {
      return config.hud.armorHudStyle == TurtModConfig.ArmorHudStyle.HOTBAR || config.hud.armorHudHotbarStyle;
   }

   private static void renderArmorHud(class_332 context, class_310 client, TurtModConfig config) {
      int x = config.hud.armorHudX;
      int y = config.hud.armorHudY;
      float scale = CustomThemeRenderer.getHudScale(config, config.hud.armorHudScalePercent);
      boolean vertical = config.hud.armorHudVertical;
      List<class_1799> stacks = getArmorHudStacks(client, config);
      if (stacks.isEmpty()) {
         return;
      }
      boolean hotbarTray = isHotbarTray(config);
      PanelSize size = getArmorHudSize(config);
      int scaledWidth = client.method_22683().method_4486();
      int scaledHeight = client.method_22683().method_4502();
      int hudWidth = Math.round((float)size.width * scale);
      int hudHeight = Math.round((float)size.height * scale);
      x = Math.max(0, Math.min(x, scaledWidth - hudWidth));
      y = Math.max(0, Math.min(y, scaledHeight - hudHeight));
      int drawX = x + getArmorHudDrawOffsetX(config);
      int drawY = y + getArmorHudDrawOffsetY(config);
      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_51448().scale(scale, scale);
      context.method_51448().translate((float)(-x), (float)(-y));

      if (hotbarTray) {
         if (vertical) {
            renderArmorHotbarTrayVertical(context, client, config, drawX, drawY, stacks);
         } else {
            renderArmorHotbarTray(context, client, config, drawX, drawY, stacks);
         }
      } else {
         renderArmorThemed(context, client, config, x, y, drawX, drawY, size, vertical, stacks);
      }

      context.method_51448().popMatrix();
   }

   /** Faithful armor-hud "HOTBAR" style: the real vanilla hotbar tray sprite as the background,
    *  items at the vanilla 20px stride / +3 inset, just like ru.berdinskiybear.armorhud. */
   private static void renderArmorHotbarTray(class_332 context, class_310 client, TurtModConfig config, int x, int y, List<class_1799> stacks) {
      int count = stacks.size();
      // Faithful port of ru.berdinskiybear.armorhud MixinGui HOTBAR style: blit the native 182x22
      // sprite at 1:1 pixel scale (NOT stretched) up to textureWidth-3, then cap with the rightmost
      // 3px of the sprite. This keeps the slot dividers and rounded ends at vanilla proportions —
      // method_52707 stretched the whole sprite, which squashed the dividers (the "not the same
      // hotbar" bug). textureWidth = SIZE + (count-1)*STEP, with SIZE=22 (HOTBAR_HEIGHT), STEP=20.
      int textureWidth = HOTBAR_HEIGHT + (count - 1) * HOTBAR_STEP;
      int color = CustomThemeRenderer.applyHudOpacity(config, -1);
      // 1:1 port of ru.berdinskiybear.armorhud MixinGui HOTBAR case: translate to (x,y) and blit the
      // sprite at the origin, exactly as the reference does (pushMatrix + translate(rect.x, rect.y)).
      context.method_51448().pushMatrix();
      context.method_51448().translate((float)x, (float)y);
      context.method_52708(class_10799.field_56883, HOTBAR_SPRITE, 182, 22, 0, 0, 0, 0, textureWidth - 3, HOTBAR_HEIGHT, color);
      context.method_52708(class_10799.field_56883, HOTBAR_SPRITE, 182, 22, 182 - 3, 0, textureWidth - 3, 0, 3, HOTBAR_HEIGHT, color);
      context.method_51448().popMatrix();

      for(int i = 0; i < count; ++i) {
         class_1799 stack = stacks.get(i);
         int itemX = x + 3 + i * HOTBAR_STEP;
         int itemY = y + 3;
         if (!stack.method_7960()) {
            context.method_51427(stack, itemX, itemY);
            context.method_51431(client.field_1772, stack, itemX, itemY);
         }

         if (config.hud.armorHudWarnings && isLowDurability(stack, config.hud.armorHudWarningThresholdPercent)) {
            drawLowDurabilityWarning(context, client, config, itemX, itemY);
         }

         String durabilityText = getDurabilityText(stack, config.hud.armorHudDurabilityMode, config.hud.armorHudShowDurability);
         if (durabilityText != null) {
            int durabilityColor = CustomThemeRenderer.applyHudOpacity(config, -16777216 | stack.method_31580());
            int textX = itemX + (16 - client.field_1772.method_1727(durabilityText)) / 2;
            // Horizontal row: TOP places text above the tray, otherwise below.
            int textY = config.hud.armorHudSide == TurtModConfig.ArmorHudSide.TOP ? y - 10 : y + HOTBAR_HEIGHT + 2;
            context.method_27535(client.field_1772, class_2561.method_43470(durabilityText), textX, textY, durabilityColor);
         }
      }
   }

   /** Vertical vanilla hotbar tray: the same 182x22 sprite, rotated 90° so the slot dividers run
    *  horizontally and the rounded caps land top/bottom. Items are drawn upright (unrotated) so
    *  they stay readable. Mirrors the horizontal HOTBAR style but stacked downward. */
   private static void renderArmorHotbarTrayVertical(class_332 context, class_310 client, TurtModConfig config, int x, int y, List<class_1799> stacks) {
      int count = stacks.size();
      int textureWidth = HOTBAR_HEIGHT + (count - 1) * HOTBAR_STEP;
      int color = CustomThemeRenderer.applyHudOpacity(config, -1);

      // Background: translate to the top-right of the vertical strip, rotate +90°, then blit the
      // horizontal sprite — it maps onto a vertical 22-wide strip running downward from (x,y).
      context.method_51448().pushMatrix();
      context.method_51448().translate((float)(x + HOTBAR_HEIGHT), (float)y);
      context.method_51448().rotate((float)(Math.PI / 2.0));
      context.method_52708(class_10799.field_56883, HOTBAR_SPRITE, 182, 22, 0, 0, 0, 0, textureWidth - 3, HOTBAR_HEIGHT, color);
      context.method_52708(class_10799.field_56883, HOTBAR_SPRITE, 182, 22, 182 - 3, 0, textureWidth - 3, 0, 3, HOTBAR_HEIGHT, color);
      context.method_51448().popMatrix();

      for(int i = 0; i < count; ++i) {
         class_1799 stack = stacks.get(i);
         int itemX = x + 3;
         int itemY = y + 3 + i * HOTBAR_STEP;
         if (!stack.method_7960()) {
            context.method_51427(stack, itemX, itemY);
            context.method_51431(client.field_1772, stack, itemX, itemY);
         }

         if (config.hud.armorHudWarnings && isLowDurability(stack, config.hud.armorHudWarningThresholdPercent)) {
            drawLowDurabilityWarning(context, client, config, itemX, itemY);
         }

         String durabilityText = getDurabilityText(stack, config.hud.armorHudDurabilityMode, config.hud.armorHudShowDurability);
         if (durabilityText != null) {
            int durabilityColor = CustomThemeRenderer.applyHudOpacity(config, -16777216 | stack.method_31580());
            // Place to the side (right by default, left if anchored left) so it never overlaps the slot.
            int textX = config.hud.armorHudSide == TurtModConfig.ArmorHudSide.LEFT
               ? x - 4 - client.field_1772.method_1727(durabilityText)
               : x + HOTBAR_HEIGHT + 4;
            int textY = itemY + 4;
            context.method_27535(client.field_1772, class_2561.method_43470(durabilityText), textX, textY, durabilityColor);
         }
      }
   }

   /** themed-box style (CLASSIC / MINIMAL) — turtmod's own panel look. */
   private static void renderArmorThemed(class_332 context, class_310 client, TurtModConfig config, int panelX, int panelY, int x, int y, PanelSize size, boolean vertical, List<class_1799> stacks) {
      if (config.hud.armorHudStyle != TurtModConfig.ArmorHudStyle.MINIMAL) {
         CustomThemeRenderer.renderThemedBox(context, panelX, panelY, size.width, size.height, config);
      }

      int slotStartX = x + 4;
      int slotStartY = y + 4;

      for(int i = 0; i < stacks.size(); ++i) {
         class_1799 stack = stacks.get(i);
         int slotX = vertical ? slotStartX : slotStartX + i * 21;
         int slotY = vertical ? slotStartY + i * 21 : slotStartY;
         CustomThemeRenderer.renderSlotCell(context, slotX, slotY, 18, 18, config, !stack.method_7960());
         if (config.hud.armorHudWarnings && isLowDurability(stack, config.hud.armorHudWarningThresholdPercent)) {
            drawLowDurabilityWarning(context, client, config, slotX + 1, slotY + 1);
         }

         if (!stack.method_7960()) {
            context.method_51427(stack, slotX + 1, slotY + 1);
            context.method_51431(client.field_1772, stack, slotX + 1, slotY + 1);
         }

         String durabilityText = getDurabilityText(stack, config.hud.armorHudDurabilityMode, config.hud.armorHudShowDurability);
         if (durabilityText != null) {
            int durabilityColor = CustomThemeRenderer.applyHudOpacity(config, -16777216 | stack.method_31580());
            if (vertical) {
               int textX = slotX + 18 + 4;
               int textY = slotY + 5;
               if (config.hud.armorHudSide == TurtModConfig.ArmorHudSide.LEFT) {
                  textX = slotX - 4 - client.field_1772.method_1727(durabilityText);
               }
               context.method_27535(client.field_1772, class_2561.method_43470(durabilityText), textX, textY, durabilityColor);
            } else {
               int textX = slotX + (18 - client.field_1772.method_1727(durabilityText)) / 2;
               // Horizontal row: TOP places text above the slot, otherwise below.
               int textY = config.hud.armorHudSide == TurtModConfig.ArmorHudSide.TOP ? slotY - 10 : slotY + 18 + 6;
               context.method_27535(client.field_1772, class_2561.method_43470(durabilityText), textX, textY, durabilityColor);
            }
         }
      }
   }

   /** Clean low-durability cue: a soft pulsing red ring around the 16x16 item plus a small "!"
    *  badge in the top-right corner — replaces the old hard red box + stray rectangles. */
   private static void drawLowDurabilityWarning(class_332 context, class_310 client, TurtModConfig config, int itemX, int itemY) {
      float pulse = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 220.0);
      int ringAlpha = 130 + (int)(pulse * 105.0F);
      int ring = CustomThemeRenderer.applyHudOpacity(config, ringAlpha << 24 | 0xFF4D4D);
      // 1px ring hugging the item
      context.method_73198(itemX - 1, itemY - 1, 18, 18, ring);
      // small "!" badge, top-right
      int bx = itemX + 11;
      int by = itemY - 2;
      context.method_25294(bx, by, bx + 7, by + 7, CustomThemeRenderer.applyHudOpacity(config, 0xE0CC1111));
      context.method_73198(bx, by, 7, 7, CustomThemeRenderer.applyHudOpacity(config, 0xFFFFFFFF));
      context.method_25294(bx + 3, by + 1, bx + 4, by + 4, CustomThemeRenderer.applyHudOpacity(config, -1));
      context.method_25294(bx + 3, by + 5, bx + 4, by + 6, CustomThemeRenderer.applyHudOpacity(config, -1));
   }

   public static int getArmorHudScaledWidth(TurtModConfig config) {
      PanelSize size = getArmorHudSize(config);
      return Math.round((float)size.width * CustomThemeRenderer.getHudScale(config, config.hud.armorHudScalePercent));
   }

   public static int getArmorHudScaledHeight(TurtModConfig config) {
      PanelSize size = getArmorHudSize(config);
      return Math.round((float)size.height * CustomThemeRenderer.getHudScale(config, config.hud.armorHudScalePercent));
   }

   private static int getArmorHudDrawOffsetX(TurtModConfig config) {
      boolean showDurability = config.hud.armorHudShowDurability && config.hud.armorHudDurabilityMode != TurtModConfig.ArmorHudDurabilityMode.OFF;
      return config.hud.armorHudVertical && showDurability && config.hud.armorHudSide == TurtModConfig.ArmorHudSide.LEFT ? 28 : 0;
   }

   private static int getArmorHudDrawOffsetY(TurtModConfig config) {
      boolean showDurability = config.hud.armorHudShowDurability && config.hud.armorHudDurabilityMode != TurtModConfig.ArmorHudDurabilityMode.OFF;
      return !config.hud.armorHudVertical && showDurability && config.hud.armorHudSide == TurtModConfig.ArmorHudSide.TOP ? 12 : 0;
   }

   private static int getDisplayedSlotCount(TurtModConfig config) {
      class_310 c = class_310.method_1551();
      if (c != null && c.field_1724 != null) {
         return Math.max(1, getArmorHudStacks(c, config).size());
      }
      return ARMOR_ORDER.length;
   }

   private static PanelSize getArmorHudSize(TurtModConfig config) {
      boolean vertical = config.hud.armorHudVertical;
      boolean showDurability = config.hud.armorHudShowDurability && config.hud.armorHudDurabilityMode != TurtModConfig.ArmorHudDurabilityMode.OFF;
      int count = getDisplayedSlotCount(config);
      if (isHotbarTray(config)) {
         if (vertical) {
            int width = HOTBAR_HEIGHT + (showDurability ? 28 : 0);
            int height = HOTBAR_HEIGHT + (count - 1) * HOTBAR_STEP;
            return new PanelSize(width, height);
         }
         int width = count * HOTBAR_STEP + 2;
         int height = HOTBAR_HEIGHT + (showDurability ? 12 : 0);
         return new PanelSize(width, height);
      }

      int lane = count * 18 + Math.max(0, count - 1) * 3;
      int contentWidth = vertical ? 18 + (showDurability ? 28 : 0) : lane;
      int contentHeight = vertical ? lane : 18 + (showDurability ? 14 : 0);
      return new PanelSize(8 + contentWidth, 8 + contentHeight);
   }

   /** The slots we actually draw. The offhand and main-hand slots only appear when their toggle
    *  is on AND the slot is non-empty — so a disabled or empty offhand never reserves/loads a
    *  ghost slot (the old "Reserve Offhand Space" behaviour is gone). Armor slots always show. */
   private static List<class_1799> getArmorHudStacks(class_310 client, TurtModConfig config) {
      List<class_1799> stacks = new ArrayList();
      if (config.hud.armorHudShowOffhand) {
         class_1799 off = client.field_1724.method_6079();
         if (!off.method_7960()) {
            stacks.add(off);
         }
      }

      for(class_1304 slot : ARMOR_ORDER) {
         stacks.add(client.field_1724.method_6118(slot));
      }

      if (config.hud.armorHudShowMainHand) {
         class_1799 main = client.field_1724.method_6047();
         if (!main.method_7960()) {
            stacks.add(main);
         }
      }

      return stacks;
   }

   private static int getArmorLaneWidth() {
      return ARMOR_ORDER.length * 18 + (ARMOR_ORDER.length - 1) * 3;
   }

   private static int getArmorLaneWidth(TurtModConfig config) {
      int count = ARMOR_ORDER.length + (config.hud.armorHudShowMainHand ? 1 : 0) + (config.hud.armorHudShowOffhand || config.hud.armorHudReserveOffhandSpace ? 1 : 0);
      return count * 18 + Math.max(0, count - 1) * 3;
   }

   private static int getArmorLaneHeight() {
      return ARMOR_ORDER.length * 18 + (ARMOR_ORDER.length - 1) * 3;
   }

   private static int getArmorLaneHeight(TurtModConfig config) {
      int count = ARMOR_ORDER.length + (config.hud.armorHudShowMainHand ? 1 : 0) + (config.hud.armorHudShowOffhand || config.hud.armorHudReserveOffhandSpace ? 1 : 0);
      return count * 18 + Math.max(0, count - 1) * 3;
   }

   private static String getDurabilityText(class_1799 stack, TurtModConfig.ArmorHudDurabilityMode mode, boolean legacyShowDurability) {
      if (!legacyShowDurability || mode == TurtModConfig.ArmorHudDurabilityMode.OFF || stack.method_7960() || !stack.method_7963()) {
         return null;
      }

      int max = stack.method_7936();
      int damage = stack.method_7919();
      int remaining = Math.max(0, max - damage);
      return switch (mode) {
         case PERCENT -> Math.max(0, Math.round((float)remaining * 100.0F / (float)Math.max(1, max))) + "%";
         case DAMAGE -> Integer.toString(damage);
         case REMAINING -> Integer.toString(remaining);
         case OFF -> null;
      };
   }

   private static boolean isLowDurability(class_1799 stack, int thresholdPercent) {
      if (stack.method_7960() || !stack.method_7963()) {
         return false;
      }

      int max = Math.max(1, stack.method_7936());
      int remaining = Math.max(0, stack.method_7936() - stack.method_7919());
      int percent = Math.round((float)remaining * 100.0F / (float)max);
      return percent <= Math.max(1, Math.min(100, thresholdPercent));
   }

   // ── Scoreboard HUD-editor sync: live bounds captured from the sidebar's background fills, so the
   //    editor box overlays the real scoreboard exactly (and dragging stays in sync). ──
   private static int sbL, sbT, sbR, sbB;
   private static boolean sbValid;
   private static long sbNs;

   public static void captureScoreboard(int x1, int y1, int x2, int y2) {
      long now = System.nanoTime();
      int l = Math.min(x1, x2), t = Math.min(y1, y2), r = Math.max(x1, x2), b = Math.max(y1, y2);
      if (now - sbNs > 5_000_000L) { sbL = l; sbT = t; sbR = r; sbB = b; }
      else { sbL = Math.min(sbL, l); sbT = Math.min(sbT, t); sbR = Math.max(sbR, r); sbB = Math.max(sbB, b); }
      sbNs = now;
      sbValid = true;
   }

   private static float sbScale(TurtModConfig c) {
      int sp = c.visual.scoreboardScalePercent;
      return (sp <= 0 ? 100 : sp) / 100.0F;
   }

   public static int scoreboardEditorWidth(TurtModConfig c) { return Math.round((sbValid ? sbR - sbL : 100) * sbScale(c)); }
   public static int scoreboardEditorHeight(TurtModConfig c) { return Math.round((sbValid ? sbB - sbT : 90) * sbScale(c)); }

   public static int scoreboardEditorX(class_310 mc, TurtModConfig c) {
      // SCALED width — sbL/sbR are captured from the sidebar's GuiGraphics fills (scaled gui space), so
      // mixing them with the RAW window width put the box/clamp in the wrong space at GUI scale != 1.
      float s = sbScale(c);
      int sw = mc.method_22683().method_4489();
      int ox = c.visual.scoreboardOffsetX;
      return sbValid ? Math.round(sw + ox + (sbL - sw) * s) : (sw - 103 + ox);
   }

   public static int scoreboardEditorY(class_310 mc, TurtModConfig c) {
      float s = sbScale(c);
      int sh = mc.method_22683().method_4507();
      int oy = c.visual.scoreboardOffsetY;
      return sbValid ? Math.round(oy + sbT * s) : (sh / 2 - 45 + oy);
   }

   public static void scoreboardApplyMove(class_310 mc, TurtModConfig c, int x, int y) {
      float s = sbScale(c);
      int sw = mc.method_22683().method_4489();
      int sh = mc.method_22683().method_4507();
      if (sbValid) {
         c.visual.scoreboardOffsetX = Math.round(x - sw - (sbL - sw) * s);
         c.visual.scoreboardOffsetY = Math.round(y - sbT * s);
      } else {
         c.visual.scoreboardOffsetX = x - (sw - 103);
         c.visual.scoreboardOffsetY = y - (sh / 2 - 45);
      }
   }

   private static void renderPotionHud(class_332 context, class_310 client, TurtModConfig config) {
      List<class_1293> effects = new ArrayList(client.field_1724.method_6026());
      if (!effects.isEmpty()) {
         // Honour the user's chosen sort order (was hard-coded to longest-duration before).
         if (config.hud.potionSortMode != null) {
            sortEffects(effects, config.hud.potionSortMode);
         } else {
            sortEffectsByLongestDuration(effects);
         }
         int visibleCount = Math.max(1, Math.min(effects.size(), POTION_MAX_SIMPLE_EFFECTS));
         List<class_1293> visibleEffects = new ArrayList(effects.subList(0, visibleCount));
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.potionHudScalePercent);
         PanelSize size = getPotionHudSize(config, visibleEffects.size());
         // Anchor-aware placement: potionEditorX/Y pin whichever edge the panel was dropped near and
         // grow the panel INWARD as effects are added/removed — so the anchored edge never drifts and
         // a full panel can never spill off-screen, while a small panel can still be dropped anywhere
         // (no dead zone). The HUD editor draws this exact same rect, so what you place is what you get.
         int x = potionEditorX(client, config);
         int y = potionEditorY(client, config);
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         // Full-panel themed background for the TEXT styles (FULL/COMPACT), which read as a panel. The
         // ICONS_ONLY style is vanilla-like — each pot gets its own slot bubble instead (drawn per-icon
         // in renderPotionIconsOnly), so a single wrapping box there just looks like an odd blob.
         if (config.hud.potionHudStyle != TurtModConfig.PotionHudStyle.ICONS_ONLY) {
            CustomThemeRenderer.renderThemedBox(context, x, y, size.width, size.height, config);
         }
         // Honour the chosen style (FULL / COMPACT / ICONS_ONLY) — previously only icons rendered.
         int cols = potionTextColumns(config, visibleEffects.size());
         switch (config.hud.potionHudStyle) {
            case FULL -> renderPotionFull(context, client, config, x, y, size.width, cols, visibleEffects);
            case COMPACT -> renderPotionCompact(context, client, config, x, y, size.width, cols, visibleEffects);
            default -> renderPotionIconsOnly(context, client, config, x, y, visibleEffects);
         }

         context.method_51448().popMatrix();
      }
   }

   /** Column count for the text potion styles (FULL/COMPACT). Horizontal = one row of all effects. */
   private static int potionTextColumns(TurtModConfig config, int count) {
      if (config.hud.potionHudHorizontal) {
         return Math.max(1, count);
      }
      int c = config.hud.potionHudColumns <= 0 ? 1 : config.hud.potionHudColumns;
      return Math.max(1, Math.min(c, count));
   }

   // ── Potion HUD editor / anchor sync ────────────────────────────────────────────────────────────
   //  The stored (potionHudX/Y) is an ANCHOR corner, not a fixed top-left. Whichever screen half the
   //  panel sits in, that edge is pinned and the panel grows inward as effects are added/removed — so
   //  the anchored edge never drifts, a full panel never spills off-screen, AND a small panel can be
   //  dropped anywhere (no more max-footprint dead zone / oversized editor box). The editor box and the
   //  live HUD both go through these helpers, so they line up exactly. Mirrors the scoreboard sync above.

   /** Number of effect cells to size the panel for right now (min 1 so the editor box is never empty). */
   private static int potionLiveEffectCount() {
      class_310 client = class_310.method_1551();
      int count = client != null && client.field_1724 != null ? client.field_1724.method_6026().size() : 1;
      return Math.max(1, Math.min(count, POTION_MAX_SIMPLE_EFFECTS));
   }

   /** Current on-screen footprint of the potion panel (live effect count, scaled). */
   private static PanelSize potionScaledLiveSize(TurtModConfig config) {
      PanelSize s = getPotionHudSize(config, potionLiveEffectCount());
      float scale = CustomThemeRenderer.getHudScale(config, config.hud.potionHudScalePercent);
      return new PanelSize(Math.round((float)s.width * scale), Math.round((float)s.height * scale));
   }

   public static int getPotionHudScaledWidth(TurtModConfig config) { return potionScaledLiveSize(config).width; }

   public static int getPotionHudScaledHeight(TurtModConfig config) { return potionScaledLiveSize(config).height; }

   public static int potionEditorWidth(TurtModConfig config) { return potionScaledLiveSize(config).width; }

   public static int potionEditorHeight(TurtModConfig config) { return potionScaledLiveSize(config).height; }

   /** Screen-space top-left of the panel, derived from the anchored edge + the live size. */
   public static int potionEditorX(class_310 mc, TurtModConfig config) {
      // SCALED gui width — must match the HUD editor + GuiGraphics space. Using raw width here made the
      // panel's own clamp disagree with the editor's per-frame clamp at any GUI scale != 1, so the two
      // fought each other and the HUD jittered.
      int sw = mc.method_22683().method_4489();
      int w = potionScaledLiveSize(config).width;
      int ax = config.hud.potionHudX;
      int left = ax > sw / 2 ? ax - w : ax; // right-anchored → ax is the right edge; else the left edge
      return Math.max(0, Math.min(left, Math.max(0, sw - w)));
   }

   public static int potionEditorY(class_310 mc, TurtModConfig config) {
      int sh = mc.method_22683().method_4507();
      int h = potionScaledLiveSize(config).height;
      int ay = config.hud.potionHudY;
      int top = ay > sh / 2 ? ay - h : ay; // bottom-anchored → ay is the bottom edge; else the top edge
      return Math.max(0, Math.min(top, Math.max(0, sh - h)));
   }

   /** Convert a dragged top-left back into the anchored corner, picking the edge nearest to it. */
   public static void potionApplyMove(class_310 mc, TurtModConfig config, int x, int y) {
      int sw = mc.method_22683().method_4489();
      int sh = mc.method_22683().method_4507();
      PanelSize sz = potionScaledLiveSize(config);
      int cx = x + sz.width / 2;
      int cy = y + sz.height / 2;
      config.hud.potionHudX = cx > sw / 2 ? x + sz.width : x;
      config.hud.potionHudY = cy > sh / 2 ? y + sz.height : y;
   }

   private static PanelSize getPotionHudSize(TurtModConfig config, int effectCount) {
      int count = Math.max(1, Math.min(effectCount, POTION_MAX_SIMPLE_EFFECTS));
      TurtModConfig.PotionHudStyle style = config.hud.potionHudStyle;
      if (style == TurtModConfig.PotionHudStyle.FULL || style == TurtModConfig.PotionHudStyle.COMPACT) {
         int cols = potionTextColumns(config, count);
         int rowsPerColumn = class_3532.method_15386((float) count / (float) cols);
         int colW = style == TurtModConfig.PotionHudStyle.FULL ? POTION_FULL_COL_WIDTH : POTION_COMPACT_COL_WIDTH;
         int rowH = style == TurtModConfig.PotionHudStyle.FULL ? POTION_FULL_ROW_H : POTION_COMPACT_ROW_H;
         return new PanelSize(cols * colW, rowsPerColumn * rowH + 8);
      }
      return config.hud.potionHudHorizontal
         ? new PanelSize(count * POTION_CELL_WIDTH, POTION_CELL_HEIGHT)
         : new PanelSize(POTION_CELL_WIDTH, count * POTION_CELL_HEIGHT);
   }

   private static void renderPotionFull(class_332 context, class_310 client, TurtModConfig config, int x, int y, int panelWidth, int columns, List<class_1293> effects) {
      int startY = y + 4 + 0;
      columns = Math.max(1, Math.min(8, columns));
      int colWidth = panelWidth / columns;
      int rowsPerColumn = class_3532.method_15386((float)effects.size() / (float)columns);

      for(int i = 0; i < effects.size(); ++i) {
         class_1293 effect = (class_1293)effects.get(i);
         int col = i / rowsPerColumn;
         int rowInCol = i % rowsPerColumn;
         int colX = x + col * colWidth;
         int rowY = startY + rowInCol * 22;
         int iconX = colX + 4;
         int textX = iconX + 18 + 8;
         CustomThemeRenderer.renderSlotCell(context, iconX, rowY, 18, 18, config, true);
         drawEffectIcon(context, effect, iconX + 1, rowY + 1);
         String name = trim(buildPotionName(effect), 22);
         String meta = trim(buildPotionMeta(effect, config), 22);
         context.method_27535(client.field_1772, class_2561.method_43470(name), textX, rowY + 2, CustomThemeRenderer.getTextColor(config));
         context.method_27535(client.field_1772, class_2561.method_43470(meta), textX, rowY + 11, CustomThemeRenderer.getMutedTextColor(config));
         if (rowInCol < rowsPerColumn - 1 && i < effects.size() - 1) {
            int lineY = rowY + 21;
            context.method_25294(textX, lineY, colX + colWidth - 4, lineY + 1, CustomThemeRenderer.applyHudOpacity(config, 860509542));
         }
      }

   }

   private static void renderPotionCompact(class_332 context, class_310 client, TurtModConfig config, int x, int y, int panelWidth, int columns, List<class_1293> effects) {
      int startY = y + 4 + 0;
      columns = Math.max(1, Math.min(8, columns));
      int colWidth = panelWidth / columns;
      int rowsPerColumn = class_3532.method_15386((float)effects.size() / (float)columns);

      for(int i = 0; i < effects.size(); ++i) {
         class_1293 effect = (class_1293)effects.get(i);
         int col = i / rowsPerColumn;
         int rowInCol = i % rowsPerColumn;
         int colX = x + col * colWidth;
         int rowY = startY + rowInCol * 18;
         int iconX = colX + 4;
         int durationWidth = client.field_1772.method_1727(formatDuration(effect, config));
         int durationX = colX + colWidth - 4 - durationWidth;
         int nameX = iconX + 18 + 6;
         int available = Math.max(8, durationX - nameX - 6);
         String name = client.field_1772.method_27523(buildPotionName(effect), available);
         CustomThemeRenderer.renderSlotCell(context, iconX, rowY, 18, 18, config, true);
         drawEffectIcon(context, effect, iconX + 1, rowY + 1);
         context.method_27535(client.field_1772, class_2561.method_43470(name), nameX, rowY + 5, CustomThemeRenderer.getTextColor(config));
         context.method_27535(client.field_1772, class_2561.method_43470(formatDuration(effect, config)), durationX, rowY + 5, CustomThemeRenderer.getMutedTextColor(config));
      }

   }

   private static void renderPotionIconsOnly(class_332 context, class_310 client, TurtModConfig config, int x, int y, List<class_1293> effects) {
      int columns = config.hud.potionHudHorizontal ? Math.max(1, effects.size()) : 1;

      for(int i = 0; i < effects.size(); ++i) {
         class_1293 effect = (class_1293)effects.get(i);
         int col = i % columns;
         int row = i / columns;
         int cellX = x + col * POTION_CELL_WIDTH;
         int cellY = y + row * POTION_CELL_HEIGHT;
         // Vanilla-like: each pot gets its own slot bubble (padded around the 16x16 icon), instead of
         // one big box wrapping the whole HUD. Draws nothing in the no-background/transparent theme.
         CustomThemeRenderer.renderSlotCell(context, cellX - 2, cellY - 2, 20, 20, config, true);
         drawEffectIcon(context, effect, cellX, cellY);
         drawIconOverlay(context, client, effect, cellX, cellY);
      }

   }

   private static void drawEffectIcon(class_332 context, class_1293 effect, int x, int y) {
      context.method_52706(class_10799.field_56883, class_329.method_71644(effect.method_5579()), x, y, 16, 16);
   }

   private static void drawIconOverlay(class_332 context, class_310 client, class_1293 effect, int x, int y) {
      // Icon is 16px at (x,y); its slot bubble spans x-2..x+18 / y-2..y+18. Keep the timer + level text
      // INSIDE that bubble (centred over the icon, near the bottom) instead of spilling below it.
      String duration = getTimerDuration(effect);
      int durationWidth = client.field_1772.method_1727(duration);
      context.method_27535(client.field_1772, class_2561.method_43470(duration), x + 8 - durationWidth / 2, y + 11, -1711276033);
      if (effect.method_5578() > 0) {
         String amp = getAmplifierText(effect.method_5578() + 1);
         int ampWidth = client.field_1772.method_1727(amp);
         context.method_27535(client.field_1772, class_2561.method_43470(amp), x + 16 - ampWidth, y - 1, -1711276033);
      }

   }

   private static void renderCompactHearts(class_332 context, class_310 client, TurtModConfig config) {
      float hp = client.field_1724.method_6032();
      float maxHp = client.field_1724.method_6063();
      float absorption = client.field_1724.method_6067();
      String hpText = String.format("%.1f / %.1f", hp, maxHp);
      if (absorption > 0.0F) {
         hpText = hpText + " +" + String.format("%.1f", absorption);
      }

      int x = client.method_22683().method_4486() / 2 - 28;
      int y = client.method_22683().method_4502() - 41;
      int width = client.field_1772.method_1727("HP " + hpText) + 12;
      CustomThemeRenderer.renderThemedBox(context, x, y, width, 14, config);
      context.method_27535(client.field_1772, class_2561.method_43470("HP " + hpText), x + 6, y + 3, CustomThemeRenderer.getTextColor(config));
   }

   private static void sortEffects(List<class_1293> effects, TurtModConfig.PotionSortMode mode) {
      Comparator var10000;
      switch (mode) {
         case DURATION_ASC -> var10000 = Comparator.comparingInt(class_1293::method_5584);
         case AMPLIFIER_DESC -> var10000 = Comparator.comparingInt(class_1293::method_5578).reversed().thenComparingInt(class_1293::method_5584).reversed();
         case AMPLIFIER_ASC -> var10000 = Comparator.comparingInt(class_1293::method_5578).thenComparingInt(class_1293::method_5584);
         case NAME_ASC -> var10000 = Comparator.comparing(HudPanelsFeature::buildPotionName, String.CASE_INSENSITIVE_ORDER);
         case NAME_DESC -> var10000 = Comparator.comparing(HudPanelsFeature::buildPotionName, String.CASE_INSENSITIVE_ORDER).reversed();
         case DURATION_DESC -> var10000 = Comparator.comparingInt(class_1293::method_5584).reversed();
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      Comparator<class_1293> comparator = var10000;
      effects.sort(comparator);
   }

   private static void sortEffectsByLongestDuration(List<class_1293> effects) {
      effects.sort((a, b) -> {
         boolean aInfinite = isInfinitePotion(a);
         boolean bInfinite = isInfinitePotion(b);
         if (aInfinite != bInfinite) {
            return aInfinite ? -1 : 1;
         }
         return Integer.compare(b.method_5584(), a.method_5584());
      });
   }

   private static boolean isInfinitePotion(class_1293 effect) {
      return effect.method_48559();
   }

   private static String buildPotionName(class_1293 effect) {
      String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
      if (effect.method_5578() > 0) {
         name = name + " " + toRoman(effect.method_5578() + 1);
      }

      return name;
   }

   private static String buildPotionMeta(class_1293 effect, TurtModConfig config) {
      String duration = formatDuration(effect, config);
      if (!config.hud.potionShowFlags) {
         return duration;
      } else {
         List<String> parts = new ArrayList();
         parts.add(duration);
         if (effect.method_5591()) {
            parts.add("Ambient");
         }

         if (effect.method_48559()) {
            parts.add("Infinite");
         }

         return String.join(" | ", parts);
      }
   }

   private static String getAmplifierText(int level) {
      if (level <= 0) {
         return "";
      } else {
         return level < 10 ? class_1074.method_4662("enchantment.level." + level, new Object[0]) : "**";
      }
   }

   private static String getTimerDuration(class_1293 effect) {
      if (effect.method_48559()) {
         return class_1074.method_4662("effect.duration.infinite", new Object[0]);
      } else {
         int ticks = class_3532.method_15375((float)effect.method_5584());
         int seconds = ticks / 20;
         TurtModConfig cfg = com.turtmod.TurtModClient.getConfig();
         if (cfg != null && cfg.hud.potionTimerClock) {
            return clockFormat(seconds);
         }
         if (seconds >= 3600) {
            return seconds / 3600 + "h";
         } else {
            return seconds >= 60 ? seconds / 60 + "m" : Integer.toString(seconds);
         }
      }
   }

   private static String toRoman(int value) {
      String var10000;
      switch (Math.max(1, Math.min(value, 5))) {
         case 1 -> var10000 = "I";
         case 2 -> var10000 = "II";
         case 3 -> var10000 = "III";
         case 4 -> var10000 = "IV";
         default -> var10000 = "V";
      }

      return var10000;
   }

   private static String formatDuration(class_1293 effect, TurtModConfig config) {
      if (!config.hud.potionTimerCompact) {
         return class_1292.method_5577(effect, 1.0F, 20.0F).getString();
      } else if (effect.method_48559()) {
         return "INF";
      } else {
         int seconds = Math.max(0, effect.method_5584() / 20);
         if (config.hud.potionTimerClock) {
            return clockFormat(seconds);   // "1:30" style
         }
         if (seconds >= 3600) {
            return seconds / 3600 + "h";
         } else {
            return seconds >= 60 ? seconds / 60 + "m" : Integer.toString(seconds);
         }
      }
   }

   /** m:ss (or h:mm:ss past an hour) clock format, e.g. 90s -> "1:30". */
   private static String clockFormat(int seconds) {
      if (seconds >= 3600) {
         return String.format("%d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
      }
      return String.format("%d:%02d", seconds / 60, seconds % 60);
   }

   private static String trim(String value, int maxChars) {
      if (value.length() <= maxChars) {
         return value;
      } else {
         String var10000 = value.substring(0, Math.max(1, maxChars - 2));
         return var10000 + "..";
      }
   }

   static {
      ARMOR_ORDER = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
   }

   private static record PanelSize(int width, int height) {
   }
}
