package com.turtmod.hud;

import com.turtmod.config.TurtModConfig;
import com.turtmod.config.TurtModConfigScreenFactory.ModuleKind;
import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * Live "preview box" renderer for a module's settings page. Draws the actual HUD for a {@link ModuleKind}
 * inside a box using the CURRENT config, so edits (scale / colour / style / …) update the preview instantly.
 *
 * <p>It reuses each feature's real draw code via a small {@code renderPreview(ctx, client, cfg, x, y)} entry
 * point that draws at an explicit spot (no saved position / on-screen clamp), and temporarily forces the
 * module on so its sizing + content are correct even while the toggle is off.
 */
public final class HudPreview {
   private HudPreview() {
   }

   /** Whether this module has a visual HUD we can preview in a box. */
   public static boolean has(ModuleKind kind) {
      return kind == ModuleKind.KEYSTROKES;
   }

   /** Render the HUD for {@code kind} centred inside the box, clipped to it, using the live config. */
   public static void render(class_332 ctx, class_310 client, TurtModConfig cfg, ModuleKind kind,
                             int x, int y, int w, int h) {
      if (ctx == null || client == null || client.field_1724 == null || cfg == null || !has(kind)) {
         return;
      }
      ctx.method_44379(x, y, x + w, y + h); // clip to the preview box
      try {
         switch (kind) {
            case KEYSTROKES -> {
               boolean prev = cfg.hud.keystrokesHud;
               cfg.hud.keystrokesHud = true; // force on so sizing + the key cluster are correct
               try {
                  int hw = KeystrokesFeature.getScaledWidth(cfg);
                  int hh = KeystrokesFeature.getScaledHeight(cfg);
                  int px = x + Math.max(2, (w - hw) / 2);
                  int py = y + Math.max(2, (h - hh) / 2);
                  KeystrokesFeature.renderPreview(ctx, client, cfg, px, py);
               } finally {
                  cfg.hud.keystrokesHud = prev;
               }
            }
            default -> {
            }
         }
      } finally {
         ctx.method_44380();
      }
   }
}
