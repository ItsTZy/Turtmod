package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import com.turtmod.utils.CPSTracker;
import java.util.Objects;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;

public final class KeystrokesFeature {
   private static final String CPS_BOTH_SAMPLE = "20 | 20";
   private static final int PANEL_PADDING = 3;
   private static final int KEY_SIZE = 16;
   private static final int GAP = 0;
   private static final int KEY_HEIGHT = 16;
   private static int leftCps = 0;
   private static int rightCps = 0;
   private static boolean wPressed = false;
   private static boolean aPressed = false;
   private static boolean sPressed = false;
   private static boolean dPressed = false;
   private static boolean spacePressed = false;
   private static boolean leftMousePressed = false;
   private static boolean rightMousePressed = false;

   private KeystrokesFeature() {
   }

   public static void tick(class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.keystrokesHud) {
         boolean currentLeftState = GLFW.glfwGetMouseButton(client.method_22683().method_4490(), 0) == 1;
         boolean currentRightState = GLFW.glfwGetMouseButton(client.method_22683().method_4490(), 1) == 1;
         leftCps = CPSTracker.getLeftCPS();
         rightCps = CPSTracker.getRightCPS();
         wPressed = isKeyPressed(87);
         aPressed = isKeyPressed(65);
         sPressed = isKeyPressed(83);
         dPressed = isKeyPressed(68);
         spacePressed = isKeyPressed(32);
         leftMousePressed = currentLeftState;
         rightMousePressed = currentRightState;
      }
   }

   public static void render(class_332 context, class_310 client, TurtModConfig config) {
      if (client.field_1724 != null && config.misc.enabled && config.hud.keystrokesHud) {
         int x = config.hud.keystrokesHudX;
         int y = config.hud.keystrokesHudY;
         float scale = CustomThemeRenderer.getHudScale(config, config.hud.keystrokesHudScalePercent);
         PanelSize size = getPanelBaseSize(client, config);
         boolean transparentText = CustomThemeRenderer.isTransparentTextMode(config);
         int clusterWidth = 48;
         int mouseKeyWidth = (clusterWidth - 0) / 2;
         int clusterHeight = config.hud.keystrokesHud ? 64 : 0;
         context.method_51448().pushMatrix();
         context.method_51448().translate((float)x, (float)y);
         context.method_51448().scale(scale, scale);
         context.method_51448().translate((float)(-x), (float)(-y));
         boolean renderPanel = !transparentText || config.hud.keystrokesHud;
         if (renderPanel) {
            CustomThemeRenderer.renderThemedBox(context, x, y, size.width, size.height, config);
         }

         int contentX = x + 3;
         int contentY = y + 3;
         if (config.hud.keystrokesHud) {
            int wX = contentX + 16 + 0;
            renderKey(context, client, config, "W", wPressed, wX, contentY, 16);
            int row2Y = contentY + 16 + 0;
            renderKey(context, client, config, "A", aPressed, contentX, row2Y, 16);
            renderKey(context, client, config, "S", sPressed, contentX + 16 + 0, row2Y, 16);
            renderKey(context, client, config, "D", dPressed, contentX + 32, row2Y, 16);
            int row3Y = contentY + 32;
            renderKey(context, client, config, "Space", spacePressed, contentX, row3Y, clusterWidth);
            int row4Y = contentY + 48;
            renderKey(context, client, config, "LMB", leftMousePressed, contentX, row4Y, mouseKeyWidth);
            renderKey(context, client, config, "RMB", rightMousePressed, contentX + mouseKeyWidth + 0, row4Y, mouseKeyWidth);
         }

         if (config.hud.keystrokesShowCps) {
            int cpsY = contentY + clusterHeight + (config.hud.keystrokesHud ? 3 : 0);
            String cpsText = buildCpsText(config);
            if (transparentText && !config.hud.keystrokesHud) {
               CustomThemeRenderer.renderBracketedText(context, client.field_1772, cpsText.toLowerCase(), contentX, cpsY, CustomThemeRenderer.getTextColor(config), config);
            } else {
               int cpsTextW = CustomThemeRenderer.textWidth(client.field_1772, cpsText, config);
               int cpsWidth = Math.max(clusterWidth, cpsTextW + 8);
               CustomThemeRenderer.renderSlotCell(context, contentX, cpsY, cpsWidth, 12, config, true);
               int textX = contentX + (cpsWidth - cpsTextW) / 2;
               CustomThemeRenderer.drawHudLabel(context, client.field_1772, cpsText, textX, cpsY + 2, CustomThemeRenderer.getTextColor(config), config);
            }
         }

         context.method_51448().popMatrix();
      }
   }

   private static void renderKey(class_332 context, class_310 client, TurtModConfig config, String label, boolean pressed, int x, int y, int width) {
      // Sharp-cornered fills so adjacent keys TILE and connect edge-to-edge (like the inventory HUD),
      // instead of the rounded per-key cells that left gaps. In clean/no-background mode idle keys draw
      // nothing (just the letter) and only a pressed key gets a subtle fill.
      int fill = pressed && config.hud.keystrokesUsePressedColor
         ? CustomThemeRenderer.applyHudOpacity(config, CustomThemeRenderer.pickedArgb(config.hud.keystrokesPressedColor))
         : CustomThemeRenderer.getKeyBackground(config, pressed);
      if (fill >>> 24 > 0) {
         context.method_25294(x, y, x + width, y + 16, fill);
      }

      int textColor = pressed && config.hud.keystrokesUsePressedColor
         ? CustomThemeRenderer.applyHudOpacity(config, CustomThemeRenderer.pickedArgb(config.hud.keystrokesPressedTextColor))
         : CustomThemeRenderer.getKeyTextColor(config, pressed);
      int textWidth = CustomThemeRenderer.textWidth(client.field_1772, label, config);
      int textX = x + Math.max(0, (width - textWidth) / 2);
      Objects.requireNonNull(client.field_1772);
      int textY = y + Math.max(0, (16 - 9) / 2);
      CustomThemeRenderer.drawHudLabel(context, client.field_1772, label, textX, textY, textColor, config);
   }

   public static int getScaledWidth(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      PanelSize size = getPanelBaseSize(client, config);
      return Math.round((float)size.width * CustomThemeRenderer.getHudScale(config, config.hud.keystrokesHudScalePercent));
   }

   public static int getScaledHeight(TurtModConfig config) {
      class_310 client = class_310.method_1551();
      PanelSize size = getPanelBaseSize(client, config);
      return Math.round((float)size.height * CustomThemeRenderer.getHudScale(config, config.hud.keystrokesHudScalePercent));
   }

   private static PanelSize getPanelBaseSize(class_310 client, TurtModConfig config) {
      int clusterWidth = 48;
      int clusterHeight = config.hud.keystrokesHud ? 64 : 0;
      String cpsText = buildCpsSampleText(config);
      if (client != null && client.field_1772 != null) {
         int var9 = client.field_1772.method_1727(cpsText) + 8;
      } else {
         boolean var10000 = true;
      }

      int panelWidth = clusterWidth + 6;
      int cpsHeight = config.hud.keystrokesShowCps ? 14 : 0;
      int panelHeight = clusterHeight + (config.hud.keystrokesShowCps && config.hud.keystrokesHud ? 6 : 0) + cpsHeight + 6;
      return new PanelSize(panelWidth, Math.max(panelHeight, 20));
   }

   private static String buildCpsText(TurtModConfig config) {
      if (config.hud.cpsShowBoth) {
         return leftCps + " | " + rightCps;
      } else {
         return config.hud.cpsShowRightClick ? String.valueOf(rightCps) : String.valueOf(leftCps);
      }
   }

   private static String buildCpsSampleText(TurtModConfig config) {
      return config.hud.cpsShowBoth ? "20 | 20" : "20";
   }

   private static boolean isKeyPressed(int keyCode) {
      class_310 client = class_310.method_1551();
      if (client.method_22683() == null) {
         return false;
      } else {
         return GLFW.glfwGetKey(client.method_22683().method_4490(), keyCode) == 1;
      }
   }

   private static record PanelSize(int width, int height) {
   }
}
