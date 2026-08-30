package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.hud.CommandKeysFeature;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * Command Keys overview: an enable toggle plus one row per slot showing the bound key, the macro
 * mode and how many lines it has, with an Edit button that opens {@link CommandKeyEditScreen} for the
 * full macro editor (multiple messages, per-line delays, mode, send/type).
 */
public class CommandKeysScreen extends class_437 {
   private final class_437 parent;
   private float openFade = 0.0F;
   private long lastFrameNs = System.nanoTime();

   public CommandKeysScreen(class_437 parent) {
      super(class_2561.method_43470("Command Keys"));
      this.parent = parent;
   }

   protected void method_25426() {
      TurtModConfig cfg = TurtModClient.getConfig();
      cfg.misc.ensureCommandKeys();
      int panelW = Math.min(380, this.field_22789 - 40);
      int left = (this.field_22789 - panelW) / 2;

      this.method_37063(class_4185.method_46430(enableLabel(cfg), b -> {
         cfg.misc.commandKeysEnabled = !cfg.misc.commandKeysEnabled;
         ConfigManager.save(cfg);
         b.method_25355(enableLabel(cfg));
      }).method_46434(left, 34, panelW, 20).method_46431());

      int top = 64;
      int rowH = 24;
      for (int i = 0; i < CommandKeysFeature.SLOTS; i++) {
         final int slot = i;
         int y = top + i * rowH;
         // Key bind button.
         this.method_37063(class_4185.method_46430(bindLabel(slot), b -> {
            class_304 bind = TurtModClient.getCommandKeyBind(slot);
            if (bind != null) {
               this.field_22787.method_1507(new KeybindListenScreen(bind, () -> new CommandKeysScreen(this.parent)));
            }
         }).method_46434(left + 24, y, 60, 20).method_46431());
         // Edit button (opens the macro editor).
         this.method_37063(class_4185.method_46430(class_2561.method_43470("Edit"), b ->
            this.field_22787.method_1507(new CommandKeyEditScreen(this.parent, slot)))
            .method_46434(left + panelW - 60, y, 60, 20).method_46431());
      }

      this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
         .method_46434(left, this.field_22790 - 28, panelW, 20).method_46431());
   }

   private static class_2561 enableLabel(TurtModConfig cfg) {
      return class_2561.method_43470("Command Keys: " + (cfg.misc.commandKeysEnabled ? "ON" : "OFF"));
   }

   private static class_2561 bindLabel(int slot) {
      class_304 bind = TurtModClient.getCommandKeyBind(slot);
      String key = bind == null ? "?" : bind.method_16007().getString();
      return class_2561.method_43470(key);
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f), 12f);
      this.lastFrameNs = now;
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(ctx, mx, my);
      ctx.method_25300(this.field_22793, "Command Keys", this.field_22789 / 2, 14, -1);
      ctx.method_25300(this.field_22793, "Bind a key, then Edit to add commands/messages.", this.field_22789 / 2, 25, -7763575);
      TurtModConfig cfg = TurtModClient.getConfig();
      // Special (Command Keys): live count of configured macro slots.
      int configured = 0;
      for (int i = 0; i < CommandKeysFeature.SLOTS; i++) {
         if (cfg.misc.commandKeyMacros[i] != null && countLines(cfg.misc.commandKeyMacros[i]) > 0) {
            configured++;
         }
      }
      ctx.method_25300(this.field_22793, "⚡ " + configured + " / " + CommandKeysFeature.SLOTS + " macros set",
         this.field_22789 / 2, 38, Palette.GREEN.getRGB());
      int panelW = Math.min(380, this.field_22789 - 40);
      int left = (this.field_22789 - panelW) / 2;
      int top = 64;
      int rowH = 24;
      for (int i = 0; i < CommandKeysFeature.SLOTS; i++) {
         int y = top + i * rowH;
         ctx.method_51433(this.field_22793, "#" + (i + 1), left + 4, y + 6, Palette.GREEN.getRGB(), false);
         TurtModConfig.CommandKey key = cfg.misc.commandKeyMacros[i];
         int lines = key == null ? 0 : countLines(key);
         String mode = key == null || key.mode == null ? "SEND" : key.mode;
         String summary = mode + "  •  " + lines + (lines == 1 ? " line" : " lines") + (key != null && key.typeInChat ? "  •  type" : "");
         ctx.method_51433(this.field_22793, summary, left + 90, y + 6, lines > 0 ? -3355444 : -8947849, false);
      }
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   private static int countLines(TurtModConfig.CommandKey key) {
      if (key.messages == null) {
         return 0;
      }
      int n = 0;
      for (TurtModConfig.CmdMsg m : key.messages) {
         if (m != null && m.text != null && !m.text.trim().isEmpty()) {
            n++;
         }
      }
      return n;
   }

   public void method_25419() {
      ConfigManager.save(TurtModClient.getConfig());
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
