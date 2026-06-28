package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.hud.CommandKeysFeature;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/**
 * Full macro editor for a single command-key slot: a list of message lines (each "/cmd" or chat with
 * an optional per-line delay in ticks), the fire mode (SEND / CYCLE / REPEAT), a send-vs-type toggle,
 * and the keybind. Structural edits (add/remove a line) rebuild the screen.
 */
public class CommandKeyEditScreen extends class_437 {
   private static final int MAX_LINES = 12;
   private static final String[] MODES = {"SEND", "CYCLE", "REPEAT"};

   private final class_437 parent;
   private final int slot;

   public CommandKeyEditScreen(class_437 parent, int slot) {
      super(class_2561.method_43470("Command Key #" + (slot + 1)));
      this.parent = parent;
      this.slot = slot;
   }

   private TurtModConfig.CommandKey key() {
      TurtModConfig cfg = TurtModClient.getConfig();
      cfg.misc.ensureCommandKeys();
      return cfg.misc.commandKeyMacros[this.slot];
   }

   protected void method_25426() {
      TurtModConfig cfg = TurtModClient.getConfig();
      TurtModConfig.CommandKey key = key();
      int panelW = Math.min(420, this.field_22789 - 40);
      int left = (this.field_22789 - panelW) / 2;
      int half = (panelW - 6) / 2;

      // Row 1: Mode + Send/Type.
      this.method_37063(class_4185.method_46430(modeLabel(key), b -> {
         key.mode = nextMode(key.mode);
         ConfigManager.save(cfg);
         b.method_25355(modeLabel(key));
      }).method_46434(left, 36, half, 20).method_46431());
      this.method_37063(class_4185.method_46430(typeLabel(key), b -> {
         key.typeInChat = !key.typeInChat;
         ConfigManager.save(cfg);
         b.method_25355(typeLabel(key));
      }).method_46434(left + half + 6, 36, half, 20).method_46431());

      // Row 2: keybind.
      this.method_37063(class_4185.method_46430(bindLabel(), b -> {
         class_304 bind = TurtModClient.getCommandKeyBind(this.slot);
         if (bind != null) {
            this.field_22787.method_1507(new KeybindListenScreen(bind, () -> new CommandKeyEditScreen(this.parent, this.slot)));
         }
      }).method_46434(left, 60, panelW, 20).method_46431());

      // Message rows: [text ............] [delay] [x]
      int top = 96;
      int rowH = 24;
      int delayW = 34;
      int delBtnW = 20;
      int textW = panelW - delayW - delBtnW - 12;
      for (int i = 0; i < key.messages.size(); i++) {
         final int idx = i;
         int y = top + i * rowH;
         TurtModConfig.CmdMsg msg = key.messages.get(i);

         class_342 text = new class_342(this.field_22793, left + 16, y, textW - 16, 18, class_2561.method_43470("Line"));
         text.method_1880(256);
         text.method_47404(class_2561.method_43470("/command or chat text"));
         text.method_1852(msg.text == null ? "" : msg.text);
         text.method_1863(s -> { msg.text = s; ConfigManager.save(cfg); });
         this.method_37063(text);

         class_342 delay = new class_342(this.field_22793, left + textW + 2, y, delayW, 18, class_2561.method_43470("Delay"));
         delay.method_1880(4);
         delay.method_1890(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
         delay.method_47404(class_2561.method_43470("0t"));
         delay.method_1852(msg.delay > 0 ? Integer.toString(msg.delay) : "");
         delay.method_1863(s -> {
            try { msg.delay = s.isEmpty() ? 0 : Integer.parseInt(s); } catch (NumberFormatException e) { msg.delay = 0; }
            ConfigManager.save(cfg);
         });
         this.method_37063(delay);

         this.method_37063(class_4185.method_46430(class_2561.method_43470("✕"), b -> {
            key.messages.remove(idx);
            ConfigManager.save(cfg);
            this.field_22787.method_1507(new CommandKeyEditScreen(this.parent, this.slot));
         }).method_46434(left + panelW - delBtnW, y, delBtnW, 18).method_46431());
      }

      int afterRows = top + key.messages.size() * rowH + 4;
      if (key.messages.size() < MAX_LINES) {
         this.method_37063(class_4185.method_46430(class_2561.method_43470("+ Add Line"), b -> {
            key.messages.add(new TurtModConfig.CmdMsg());
            ConfigManager.save(cfg);
            this.field_22787.method_1507(new CommandKeyEditScreen(this.parent, this.slot));
         }).method_46434(left, afterRows, panelW, 20).method_46431());
      }

      this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
         .method_46434(left, this.field_22790 - 28, panelW, 20).method_46431());
   }

   private static String nextMode(String mode) {
      for (int i = 0; i < MODES.length; i++) {
         if (MODES[i].equals(mode)) {
            return MODES[(i + 1) % MODES.length];
         }
      }
      return MODES[0];
   }

   private static class_2561 modeLabel(TurtModConfig.CommandKey key) {
      return class_2561.method_43470("Mode: " + (key.mode == null ? "SEND" : key.mode));
   }

   private static class_2561 typeLabel(TurtModConfig.CommandKey key) {
      return class_2561.method_43470(key.typeInChat ? "Action: Type in chat" : "Action: Send");
   }

   private class_2561 bindLabel() {
      class_304 bind = TurtModClient.getCommandKeyBind(this.slot);
      String k = bind == null ? "?" : bind.method_16007().getString();
      return class_2561.method_43470("Key: " + k);
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      ctx.method_25300(this.field_22793, this.method_25440().getString(), this.field_22789 / 2, 14, Palette.GREEN.getRGB());
      ctx.method_25300(this.field_22793, modeHint(), this.field_22789 / 2, 25, -7763575);
      int panelW = Math.min(420, this.field_22789 - 40);
      int left = (this.field_22789 - panelW) / 2;
      int top = 96;
      int rowH = 24;
      TurtModConfig.CommandKey key = key();
      for (int i = 0; i < key.messages.size(); i++) {
         ctx.method_51433(this.field_22793, (i + 1) + ".", left + 2, top + i * rowH + 5, Palette.GREEN.getRGB(), false);
      }
      super.method_25394(ctx, mx, my, delta);
   }

   private String modeHint() {
      TurtModConfig.CommandKey key = key();
      return switch (key.mode == null ? "SEND" : key.mode) {
         case "CYCLE" -> "CYCLE: each press fires the next line.";
         case "REPEAT" -> "REPEAT: loops the lines until pressed again. Delay = spacing.";
         default -> "SEND: fires all lines once. Delay = ticks before each line (20t = 1s).";
      };
   }

   public void method_25419() {
      ConfigManager.save(TurtModClient.getConfig());
      if (this.field_22787 != null) {
         this.field_22787.method_1507(new CommandKeysScreen(this.parent));
      }
   }
}
