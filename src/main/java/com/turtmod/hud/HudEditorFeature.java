package com.turtmod.hud;

import com.turtmod.combat.HealthNumberFeature;
import com.turtmod.combat.PotionThrowTracker;
import com.turtmod.combat.TotemCounterFeature;
import com.turtmod.config.ConfigManager;
import com.turtmod.config.TurtModConfig;
import net.minecraft.class_11908;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class HudEditorFeature {
   private static Anchor dragging;
   private static Anchor selected;
   private static int dragOffsetX;
   private static int dragOffsetY;
   private static final int CLOSE_SIZE = 10;

   // Snap feedback state (read by HudEditorScreen for the pulse + guides).
   private static boolean snappedX;
   private static boolean snappedY;
   public static long snapPulseNs;

   /** The anchor currently being dragged, or null. Lets the editor screen draw guides/chip for it. */
   public static Anchor getDragging() {
      return dragging;
   }

   private HudEditorFeature() {
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config, int mouseX, int mouseY) {
      int sw = context.method_51421();
      int sh = context.method_51443();
      if (selected != null && !isEnabled(selected, config)) {
         selected = null;
         dragging = null;
      }

      // When the resolution or GUI scale changes, an element's stored X/Y can fall outside the new
      // screen bounds, leaving it off-screen and impossible to grab. Pull every enabled element back
      // on-screen so it's always reachable in the editor.
      clampAllToScreen(client, config, sw, sh);

      int cx = sw / 2;
      int cy = sh / 2;
      context.method_25294(cx, 0, cx + 1, sh, 1150139999);
      context.method_25294(0, cy, sw, cy + 1, 1150139999);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.ARMOR, "Armor", config.hud.movableArmorHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.POTION, "Potions", config.hud.movablePotionHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.TOTEM, "Totems", config.combat.totemCounterHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.POTS, "Pots", config.combat.potionThrowCounterHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.OVERLAY, "FPS/Ping", config.hud.minimalFpsPingOverlay);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.DEBUG, "Clean F3", config.hud.cleanF3Mode);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.REACH, "Reach", config.hud.reachDisplay);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.SPRINT, "Sprint", config.hud.toggleSprintHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.KEYSTROKES, "Keystrokes", config.hud.keystrokesHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.INVENTORY, "Inventory", config.hud.inventoryHudEnabled);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.CPS_COUNTER, "CPS Counter", config.hud.cpsCounterHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.COORDINATES, "Coordinates", config.hud.coordinatesHud);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.HEALTH, "Health", config.combat.showExactHealthNumber);
      drawAnchor(context, client, config, HudEditorFeature.Anchor.SCOREBOARD, "Scoreboard", !config.visual.hideScoreboard);
      context.method_25303(client.field_1772, "Left drag: move | Click [x]: disable | Mouse wheel: scale | [+/-]: scale | [R]: reset", 6, sh - 20, -7487905);
      if (selected != null) {
         int x = getX(selected, client, config);
         int y = getY(selected, client, config);
         int w = getWidth(selected, client, config);
         int h = getHeight(selected, client, config);
         int accentColor = -7487905;
         context.method_73198(x - 2, y - 2, w + 4, h + 4, accentColor);
         String var10000 = getSelectedName(selected);
         String info = "Selected: " + var10000 + " | Scale: " + getElementScalePercent(selected, config) + "% | [+/-] scale | [R] reset | Arrows move";
         int infoW = client.field_1772.method_1727(info) + 10;
         int infoY = sh - 35;
         context.method_25294(6, infoY, 6 + infoW, infoY + 12, -1441130988);
         context.method_73198(6, infoY, infoW, 12, accentColor);
         context.method_51433(client.field_1772, info, 9, infoY + 3, -1, false);
      }

   }

   /** Clamp every enabled, movable element so it stays fully inside the current screen bounds.
    *  Runs each frame the editor is open; saves only when a position actually changed. */
   private static void clampAllToScreen(class_310 client, TurtModConfig config, int sw, int sh) {
      boolean changed = false;
      for (Anchor anchor : Anchor.values()) {
         // ZOOM has no movable position; skip non-positionable / disabled anchors.
         if (anchor == Anchor.ZOOM || !isEnabled(anchor, config)) {
            continue;
         }
         int w = getWidth(anchor, client, config);
         int h = getHeight(anchor, client, config);
         int x = getX(anchor, client, config);
         int y = getY(anchor, client, config);
         int maxX = Math.max(0, sw - w);
         int maxY = Math.max(0, sh - h);
         int cx = Math.max(0, Math.min(maxX, x));
         int cy = Math.max(0, Math.min(maxY, y));
         if (cx != x || cy != y) {
            moveAnchor(anchor, cx, cy, client, config);
            changed = true;
         }
      }
      if (changed) {
         ConfigManager.save(config);
      }
   }

   private static String getSelectedName(Anchor selected) {
      String var10000;
      switch (selected.ordinal()) {
         case 0 -> var10000 = "Armor";
         case 1 -> var10000 = "Potions";
         case 2 -> var10000 = "Totems";
         case 3 -> var10000 = "FPS/Ping";
         case 4 -> var10000 = "Clean F3";
         case 5 -> var10000 = "Reach";
         case 6 -> var10000 = "Sprint";
         case 7 -> var10000 = "Keystrokes";
         case 8 -> var10000 = "CPS Counter";
         case 9 -> var10000 = "Zoom";
         case 10 -> var10000 = "Inventory HUD";
         case 11 -> var10000 = "Coordinates";
         case 12 -> var10000 = "Health";
         case 13 -> var10000 = "Scoreboard";
         case 14 -> var10000 = "Pots";
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static boolean mouseClicked(double mouseX, double mouseY, int button, class_310 client, TurtModConfig config) {
      // Clicking the [x] button in an element's top-right corner disables that HUD element.
      for(Anchor anchor : HudEditorFeature.Anchor.values()) {
         if (anchor != Anchor.ZOOM && isEnabled(anchor, config)) {
            int x = getX(anchor, client, config);
            int y = getY(anchor, client, config);
            int w = getWidth(anchor, client, config);
            int bx = x + w - CLOSE_SIZE;
            if (mouseX >= (double)bx && mouseX <= (double)(bx + CLOSE_SIZE) && mouseY >= (double)y && mouseY <= (double)(y + CLOSE_SIZE)) {
               setEnabled(anchor, false, config);
               if (selected == anchor) {
                  selected = null;
               }
               dragging = null;
               ConfigManager.save(config);
               return true;
            }
         }
      }

      for(Anchor anchor : HudEditorFeature.Anchor.values()) {
         if (isEnabled(anchor, config)) {
            int x = getX(anchor, client, config);
            int y = getY(anchor, client, config);
            int w = getWidth(anchor, client, config);
            int h = getHeight(anchor, client, config);
            if (mouseX >= (double)x && mouseX <= (double)(x + w) && mouseY >= (double)y && mouseY <= (double)(y + h)) {
               dragging = anchor;
               selected = anchor;
               dragOffsetX = (int)mouseX - x;
               dragOffsetY = (int)mouseY - y;
               return true;
            }
         }
      }

      selected = null;
      return false;
   }

   public static boolean isEnabled(Anchor anchor, TurtModConfig config) {
      boolean var10000;
      switch (anchor.ordinal()) {
         case 0 -> var10000 = config.hud.movableArmorHud;
         case 1 -> var10000 = config.hud.movablePotionHud;
         case 2 -> var10000 = config.combat.totemCounterHud;
         case 3 -> var10000 = config.hud.minimalFpsPingOverlay;
         case 4 -> var10000 = config.hud.cleanF3Mode;
         case 5 -> var10000 = config.hud.reachDisplay;
         case 6 -> var10000 = config.hud.toggleSprintHud;
         case 7 -> var10000 = config.hud.keystrokesHud;
         case 8 -> var10000 = config.hud.cpsCounterHud;
         case 9 -> var10000 = config.visual.zoomEnabled;
         case 10 -> var10000 = config.hud.inventoryHudEnabled;
         case 11 -> var10000 = config.hud.coordinatesHud;
         case 12 -> var10000 = config.combat.showExactHealthNumber;
         case 13 -> var10000 = !config.visual.hideScoreboard;
         case 14 -> var10000 = config.combat.potionThrowCounterHud;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   /** Toggle the config flag that backs an editor anchor (used by the in-editor [x] disable button). */
   private static void setEnabled(Anchor anchor, boolean enabled, TurtModConfig config) {
      switch (anchor.ordinal()) {
         case 0 -> config.hud.movableArmorHud = enabled;
         case 1 -> config.hud.movablePotionHud = enabled;
         case 2 -> config.combat.totemCounterHud = enabled;
         case 3 -> config.hud.minimalFpsPingOverlay = enabled;
         case 4 -> config.hud.cleanF3Mode = enabled;
         case 5 -> config.hud.reachDisplay = enabled;
         case 6 -> config.hud.toggleSprintHud = enabled;
         case 7 -> config.hud.keystrokesHud = enabled;
         case 8 -> config.hud.cpsCounterHud = enabled;
         case 9 -> config.visual.zoomEnabled = enabled;
         case 10 -> config.hud.inventoryHudEnabled = enabled;
         case 11 -> config.hud.coordinatesHud = enabled;
         case 12 -> config.combat.showExactHealthNumber = enabled;
         case 13 -> config.visual.hideScoreboard = !enabled;
         case 14 -> config.combat.potionThrowCounterHud = enabled;
      }
   }

   public static boolean isSelected(Anchor anchor) { return anchor == selected; }

   public static void mouseReleased(double mouseX, double mouseY, int button) {
      dragging = null;
      dragOffsetX = 0;
      dragOffsetY = 0;
      snappedX = false;
      snappedY = false;
   }

   public static void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, class_310 client, TurtModConfig config) {
      if (dragging != null) {
         int newX = (int)mouseX - dragOffsetX;
         int newY = (int)mouseY - dragOffsetY;
         if (config.hud.snapToGrid) {
            newX = newX / config.hud.gridSize * config.hud.gridSize;
            newY = newY / config.hud.gridSize * config.hud.gridSize;
         }

         boolean sx = false;
         boolean sy = false;
         if (config.hud.snapToCenter) {
            int cx = client.method_22683().method_4486() / 2;
            int cy = client.method_22683().method_4502() / 2;
            if (Math.abs(newX - cx) < config.hud.centerSnapRange) {
               newX = cx;
               sx = true;
            }

            if (Math.abs(newY - cy) < config.hud.centerSnapRange) {
               newY = cy;
               sy = true;
            }
         }
         // Pulse + tick only when a snap is freshly entered (not every dragged frame).
         if ((sx && !snappedX) || (sy && !snappedY)) {
            snapPulseNs = System.nanoTime();
            com.turtmod.ui.TurtSounds.tick();
         }
         snappedX = sx;
         snappedY = sy;

         int maxX = client.method_22683().method_4486() - getWidth(dragging, client, config);
         int maxY = client.method_22683().method_4502() - getHeight(dragging, client, config);
         newX = Math.max(0, Math.min(maxX, newX));
         newY = Math.max(0, Math.min(maxY, newY));
         moveAnchor(dragging, newX, newY, client, config);
         ConfigManager.save(config);
      }

   }

   private static void moveAnchor(Anchor anchor, int x, int y, class_310 client, TurtModConfig config) {
      switch (anchor.ordinal()) {
         case 0:
            config.hud.armorHudX = x;
            config.hud.armorHudY = y;
            break;
         case 1:
            HudPanelsFeature.potionApplyMove(client, config, x, y);
            break;
         case 2:
            config.hud.totemHudX = x;
            config.hud.totemHudY = y;
            break;
         case 3:
            config.hud.minimalOverlayX = x;
            config.hud.minimalOverlayY = y;
            break;
         case 4:
            config.hud.cleanF3X = x;
            config.hud.cleanF3Y = y;
            break;
         case 5:
            config.hud.reachHudX = x;
            config.hud.reachHudY = y;
            break;
         case 6:
            config.hud.toggleSprintHudX = x;
            config.hud.toggleSprintHudY = y;
            break;
         case 7:
            config.hud.keystrokesHudX = x;
            config.hud.keystrokesHudY = y;
            break;
         case 8:
            config.hud.cpsCounterX = x;
            config.hud.cpsCounterY = y;
         case 9:
         default:
            break;
         case 10:
            config.hud.inventoryHudX = x;
            config.hud.inventoryHudY = y;
            break;
         case 11:
            config.hud.coordinatesHudX = x;
            config.hud.coordinatesHudY = y;
            break;
         case 12:
            HealthNumberFeature.setPosition(client, config, x, y);
            break;
         case 13:
            HudPanelsFeature.scoreboardApplyMove(client, config, x, y);
            break;
         case 14:
            config.hud.potionThrowHudX = x;
            config.hud.potionThrowHudY = y;
      }

   }

   private static void drawAnchor(class_332 context, class_310 client, TurtModConfig config, Anchor anchor, String name, boolean enabled) {
      if (enabled) {
         int x = getX(anchor, client, config);
         int y = getY(anchor, client, config);
         int w = getWidth(anchor, client, config);
         int h = getHeight(anchor, client, config);
         int baseColor;
         int borderColor;
         if (anchor == selected) {
            baseColor = -2003976609;
            borderColor = -7487905;
         } else if (anchor == dragging) {
            baseColor = -2006213051;
            borderColor = -7487905;
         } else {
            baseColor = 1145022061;
            borderColor = 1720565343;
         }

         context.method_25294(x, y, x + w, y + h, baseColor);
         if (anchor == selected || anchor == dragging) {
            context.method_73198(x, y, w, h, borderColor);
            if (anchor == selected) {
               int cs = 4;
               context.method_25294(x, y, x + cs, y + 1, borderColor);
               context.method_25294(x, y, x + 1, y + cs, borderColor);
               context.method_25294(x + w - cs, y, x + w, y + 1, borderColor);
               context.method_25294(x + w - 1, y, x + w, y + cs, borderColor);
               context.method_25294(x, y + h - 1, x + cs, y + h, borderColor);
               context.method_25294(x, y + h - cs, x + 1, y + h, borderColor);
               context.method_25294(x + w - cs, y + h - 1, x + w, y + h, borderColor);
               context.method_25294(x + w - 1, y + h - cs, x + w, y + h, borderColor);
            }
         }

         context.method_25300(client.field_1772, name, x + w / 2, y + h / 2 - 8, -1);
         context.method_25300(client.field_1772, getElementScalePercent(anchor, config) + "%", x + w / 2, y + h / 2 + 2, -7487905);

         // [x] disable button in the top-right corner — themed in the mod's accent pink.
         int pink = -25170; // 0xFFFF9DAE
         int bx = x + w - CLOSE_SIZE;
         context.method_25294(bx, y, bx + CLOSE_SIZE, y + CLOSE_SIZE, -1308622848);
         context.method_73198(bx, y, CLOSE_SIZE, CLOSE_SIZE, pink);
         String mark = "x";
         int tw = client.field_1772.method_1727(mark);
         int tx = bx + (CLOSE_SIZE - tw + 1) / 2;
         int ty = y + (CLOSE_SIZE - client.field_1772.field_2000) / 2 + 1;
         context.method_51433(client.field_1772, mark, tx, ty, pink, false);
      }
   }

   public static int getX(Anchor anchor, class_310 client, TurtModConfig config) {
      int var10000;
      switch (anchor.ordinal()) {
         case 0 -> var10000 = config.hud.armorHudX;
         case 1 -> var10000 = HudPanelsFeature.potionEditorX(client, config);
         case 2 -> var10000 = config.hud.totemHudX;
         case 3 -> var10000 = config.hud.minimalOverlayX;
         case 4 -> var10000 = config.hud.cleanF3X;
         case 5 -> var10000 = config.hud.reachHudX;
         case 6 -> var10000 = config.hud.toggleSprintHudX;
         case 7 -> var10000 = config.hud.keystrokesHudX;
         case 8 -> var10000 = config.hud.cpsCounterX;
         case 9 -> var10000 = 0;
         case 10 -> var10000 = config.hud.inventoryHudX;
         case 11 -> var10000 = config.hud.coordinatesHudX;
         case 12 -> var10000 = HealthNumberFeature.getX(client, config);
         case 13 -> var10000 = HudPanelsFeature.scoreboardEditorX(client, config);
         case 14 -> var10000 = config.hud.potionThrowHudX;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static int getY(Anchor anchor, class_310 client, TurtModConfig config) {
      int var10000;
      switch (anchor.ordinal()) {
         case 0 -> var10000 = config.hud.armorHudY;
         case 1 -> var10000 = HudPanelsFeature.potionEditorY(client, config);
         case 2 -> var10000 = config.hud.totemHudY;
         case 3 -> var10000 = config.hud.minimalOverlayY;
         case 4 -> var10000 = config.hud.cleanF3Y;
         case 5 -> var10000 = config.hud.reachHudY;
         case 6 -> var10000 = config.hud.toggleSprintHudY;
         case 7 -> var10000 = config.hud.keystrokesHudY;
         case 8 -> var10000 = config.hud.cpsCounterY;
         case 9 -> var10000 = 0;
         case 10 -> var10000 = config.hud.inventoryHudY;
         case 11 -> var10000 = config.hud.coordinatesHudY;
         case 12 -> var10000 = HealthNumberFeature.getY(client, config);
         case 13 -> var10000 = HudPanelsFeature.scoreboardEditorY(client, config);
         case 14 -> var10000 = config.hud.potionThrowHudY;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static int getWidth(Anchor anchor, class_310 client, TurtModConfig config) {
      int var10000;
      switch (anchor.ordinal()) {
         case 0 -> var10000 = HudPanelsFeature.getArmorHudScaledWidth(config);
         case 1 -> var10000 = HudPanelsFeature.potionEditorWidth(config);
         case 2 -> var10000 = TotemCounterFeature.getScaledWidth(config);
         case 3 -> var10000 = FpsPingOverlayFeature.getScaledWidth(config);
         case 4 -> var10000 = Math.round(Math.max(80, CleanF3Feature.boxWidth(client, CleanF3Feature.buildLines(client, config))) * CustomThemeRenderer.getHudScale(config, config.hud.cleanF3ScalePercent));
         case 5 -> var10000 = ReachDisplayFeature.getScaledWidth(config);
         case 6 -> var10000 = ToggleSprintFeature.getScaledWidth(config);
         case 7 -> var10000 = KeystrokesFeature.getScaledWidth(config);
         case 8 -> var10000 = CpsCounterFeature.getScaledWidth(config);
         case 9 -> var10000 = 0;
         case 10 -> var10000 = InventoryHudFeature.getScaledWidth(config);
         case 11 -> var10000 = CoordinatesHudFeature.getScaledWidth(config);
         case 12 -> var10000 = HealthNumberFeature.getScaledWidth(config);
         case 13 -> var10000 = HudPanelsFeature.scoreboardEditorWidth(config);
         case 14 -> var10000 = PotionThrowTracker.getScaledWidth(config);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static int getHeight(Anchor anchor, class_310 client, TurtModConfig config) {
      int var10000;
      switch (anchor.ordinal()) {
         case 0 -> var10000 = HudPanelsFeature.getArmorHudScaledHeight(config);
         case 1 -> var10000 = HudPanelsFeature.potionEditorHeight(config);
         case 2 -> var10000 = TotemCounterFeature.getScaledHeight(config);
         case 3 -> var10000 = FpsPingOverlayFeature.getScaledHeight(config);
         case 4 -> var10000 = Math.round(Math.max(20, CleanF3Feature.boxHeight(CleanF3Feature.buildLines(client, config))) * CustomThemeRenderer.getHudScale(config, config.hud.cleanF3ScalePercent));
         case 5 -> var10000 = ReachDisplayFeature.getScaledHeight(config);
         case 6 -> var10000 = ToggleSprintFeature.getScaledHeight(config);
         case 7 -> var10000 = KeystrokesFeature.getScaledHeight(config);
         case 8 -> var10000 = CpsCounterFeature.getScaledHeight(config);
         case 9 -> var10000 = 0;
         case 10 -> var10000 = InventoryHudFeature.getScaledHeight(config);
         case 11 -> var10000 = CoordinatesHudFeature.getScaledHeight(config);
         case 12 -> var10000 = HealthNumberFeature.getScaledHeight(config);
         case 13 -> var10000 = HudPanelsFeature.scoreboardEditorHeight(config);
         case 14 -> var10000 = PotionThrowTracker.getScaledHeight(config);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static boolean keyPressed(class_11908 input, TurtModConfig config) {
      if (selected == null) {
         return false;
      } else {
         int moveAmount = (input.comp_4797() & 1) == 1 ? 10 : 1;
         switch (input.comp_4795()) {
            case 45:
            case 333:
               scaleSelected(config, -5);
               break;
            case 61:
            case 334:
               scaleSelected(config, 5);
               break;
            case 82:
               resetSelectedScale(config);
               break;
            case 262:
               moveAnchor(selected, getX(selected, class_310.method_1551(), config) + moveAmount, getY(selected, class_310.method_1551(), config), class_310.method_1551(), config);
               break;
            case 263:
               moveAnchor(selected, getX(selected, class_310.method_1551(), config) - moveAmount, getY(selected, class_310.method_1551(), config), class_310.method_1551(), config);
               break;
            case 264:
               moveAnchor(selected, getX(selected, class_310.method_1551(), config), getY(selected, class_310.method_1551(), config) + moveAmount, class_310.method_1551(), config);
               break;
            case 265:
               moveAnchor(selected, getX(selected, class_310.method_1551(), config), getY(selected, class_310.method_1551(), config) - moveAmount, class_310.method_1551(), config);
               break;
            default:
               return false;
         }

         ConfigManager.save(config);
         return true;
      }
   }

   public static boolean mouseScrolled(double mouseX, double mouseY, double amount, TurtModConfig config) {
      if (selected != null && amount != (double)0.0F) {
         scaleSelected(config, amount > (double)0.0F ? 5 : -5);
         ConfigManager.save(config);
         return true;
      } else {
         return false;
      }
   }

   private static void scaleSelected(TurtModConfig config, int amount) {
      setElementScalePercent(selected, config, getElementScalePercent(selected, config) + amount);
   }

   private static int getElementScalePercent(Anchor anchor, TurtModConfig config) {
      int var10000;
      switch (anchor.ordinal()) {
         case 0 -> var10000 = config.hud.armorHudScalePercent;
         case 1 -> var10000 = config.hud.potionHudScalePercent;
         case 2 -> var10000 = config.hud.totemHudScalePercent;
         case 3 -> var10000 = config.hud.overlayScalePercent;
         case 4 -> var10000 = config.hud.cleanF3ScalePercent;
         case 5 -> var10000 = config.hud.reachHudScalePercent;
         case 6 -> var10000 = config.hud.toggleSprintHudScalePercent;
         case 7 -> var10000 = config.hud.keystrokesHudScalePercent;
         case 8 -> var10000 = config.hud.cpsCounterScalePercent;
         case 9 -> var10000 = Math.round((float)(config.visual.zoomLevel * 10));
         case 10 -> var10000 = config.hud.inventoryHudScalePercent;
         case 11 -> var10000 = config.hud.coordinatesHudScalePercent;
         case 12 -> var10000 = config.combat.healthScalePercent;
         case 13 -> var10000 = config.visual.scoreboardScalePercent <= 0 ? 100 : config.visual.scoreboardScalePercent;
         case 14 -> var10000 = config.hud.potionThrowHudScalePercent;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      int raw = var10000;
      return Math.max(50, Math.min(300, raw));
   }

   private static void setElementScalePercent(Anchor anchor, TurtModConfig config, int next) {
      next = Math.max(50, Math.min(300, next));
      switch (anchor.ordinal()) {
         case 0 -> config.hud.armorHudScalePercent = next;
         case 1 -> config.hud.potionHudScalePercent = next;
         case 2 -> config.hud.totemHudScalePercent = next;
         case 3 -> config.hud.overlayScalePercent = next;
         case 4 -> config.hud.cleanF3ScalePercent = next;
         case 5 -> config.hud.reachHudScalePercent = next;
         case 6 -> config.hud.toggleSprintHudScalePercent = next;
         case 7 -> config.hud.keystrokesHudScalePercent = next;
         case 8 -> config.hud.cpsCounterScalePercent = next;
         case 9 -> config.visual.zoomLevel = Math.max(1, Math.min(10, next / 10));
         case 10 -> config.hud.inventoryHudScalePercent = next;
         case 11 -> config.hud.coordinatesHudScalePercent = next;
         case 12 -> config.combat.healthScalePercent = next;
         case 13 -> config.visual.scoreboardScalePercent = next;
         case 14 -> config.hud.potionThrowHudScalePercent = next;
      }

   }

   public static boolean resetSelectedScale(TurtModConfig config) {
      if (config != null && selected != null) {
         setElementScalePercent(selected, config, 100);
         ConfigManager.save(config);
         return true;
      } else {
         return false;
      }
   }

   public static void resetAllPositions(TurtModConfig config) {
      if (config != null) {
         config.hud.armorHudX = 413;
         config.hud.armorHudY = 271;
         config.hud.potionHudX = 613;
         config.hud.potionHudY = 0;
         config.hud.totemHudX = 505;
         config.hud.totemHudY = 162;
         config.hud.potionThrowHudX = 505;
         config.hud.potionThrowHudY = 190;
         config.hud.minimalOverlayX = 0;
         config.hud.minimalOverlayY = 0;
         config.hud.cleanF3X = 0;
         config.hud.cleanF3Y = 25;
         config.hud.reachHudX = 0;
         config.hud.reachHudY = 180;
         config.hud.toggleSprintHudX = 170;
         config.hud.toggleSprintHudY = 342;
         config.hud.keystrokesHudX = 594;
         config.hud.keystrokesHudY = 41;
         config.hud.inventoryHudX = 0;
         config.hud.inventoryHudY = 203;
         config.hud.cpsCounterX = 602;
         config.hud.cpsCounterY = 210;
         ConfigManager.save(config);
      }
   }

   public enum Anchor {
      ARMOR,
      POTION,
      TOTEM,
      OVERLAY,
      DEBUG,
      REACH,
      SPRINT,
      KEYSTROKES,
      CPS_COUNTER,
      ZOOM,
      INVENTORY,
      COORDINATES,
      HEALTH,
      SCOREBOARD,
      POTS;

      // $FF: synthetic method
      private static Anchor[] $values() {
         return new Anchor[]{ARMOR, POTION, TOTEM, OVERLAY, DEBUG, REACH, SPRINT, KEYSTROKES, CPS_COUNTER, ZOOM, INVENTORY, COORDINATES, HEALTH, SCOREBOARD, POTS};
      }
   }
}
