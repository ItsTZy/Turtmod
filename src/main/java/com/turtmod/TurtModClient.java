package com.turtmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.turtmod.chat.ScreenshotUploadFeature;
import com.turtmod.combat.HealthNumberFeature;
import com.turtmod.config.ConfigManager;
import com.turtmod.config.TurtModConfig;
import com.turtmod.config.TurtModMainMenuScreen;
import com.turtmod.config.TurtModConfigScreenFactory;
import com.turtmod.config.TurtModConfigScreenFactory.ModuleKind;
import com.turtmod.cosmetics.CosmeticManager;
import com.turtmod.discord.TurtDiscordRpcService;
import com.turtmod.hud.CommandKeysFeature;
import com.turtmod.hud.CpsCounterFeature;
import com.turtmod.hud.DeathCoordsFeature;
import com.turtmod.hud.ElytraPitchFeature;
import com.turtmod.hud.FpsPingOverlayFeature;
import com.turtmod.hud.HudPanelsFeature;
import com.turtmod.hud.InventoryHudFeature;
import com.turtmod.hud.KeystrokesFeature;
import com.turtmod.hud.ReachDisplayFeature;
import com.turtmod.hud.ToggleSprintFeature;
import com.turtmod.utils.CPSTracker;
import com.turtmod.utils.ShieldTracker;
import com.turtmod.utils.TurtLogger;
import com.turtmod.visual.FreeLookFeature;
import com.turtmod.visual.FullbrightFeature;
import com.turtmod.visual.HurtCamFeature;
import com.turtmod.visual.ScreenEffectsFeature;
import com.turtmod.visual.ZoomFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.class_1657;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_7923;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_9779;
import net.minecraft.class_304.class_11900;
import net.minecraft.class_3675.class_307;

public final class TurtModClient implements ClientModInitializer {
   public static final String MOD_ID = "turtmod";
   private static TurtModConfig config;
   private static class_304 openConfigKey;
   private static class_304 freelookKey;
   private static class_304 openHealthConfigKey;
   private static class_304 toggleHealthIndicatorKey;
   private static class_304 healthOffsetUpKey;
   private static class_304 healthOffsetDownKey;
   private static class_304 resetHealthOffsetKey;
   private static final class_304[] commandKeyBinds = new class_304[CommandKeysFeature.SLOTS];

   /** The keybind that activates a module, or null if the module has none. Only the daily-use modules
    *  (Zoom / Freelook / Health) have a dedicated binding; the generic per-module toggle keys were removed. */
   public static class_304 getModuleToggleKey(ModuleKind kind) {
      return switch (kind) {
         case ZOOM -> ZoomFeature.getZoomKey();
         case FREELOOK -> freelookKey;
         case HEALTH_INDICATOR -> toggleHealthIndicatorKey;
         default -> null;
      };
   }

   /** The keybind for command-key slot (0-based), or null if out of range / not yet registered. */
   public static class_304 getCommandKeyBind(int slot) {
      return slot >= 0 && slot < commandKeyBinds.length ? commandKeyBinds[slot] : null;
   }

   public static TurtModConfig getConfig() {
      return config;
   }

   /** Reloads the in-memory config from disk. Call after ConfigManager.reset()/external
    *  edits so the live mod state reflects the new values (otherwise getConfig() keeps
    *  returning the stale object and "Reset All" appears to do nothing). */
   public static void reloadConfig() {
      config = ConfigManager.load();
      TurtDiscordRpcService.bootstrap(config);
   }

   /**
    * One-time upgrade of the HUD theme to the menu-card look. Only applies when the theme still holds
    * the exact pre-revamp defaults, so anyone who customised their colours keeps them untouched.
    */
   private static void migrateLegacyTheme(TurtModConfig cfg) {
      if (cfg == null) {
         return;
      }
      TurtModConfig.CustomTheme t = cfg.theme;
      if (t.hudBackgroundColor == -14540254 && t.hudBackgroundAlpha == 49 && t.hudBorderColor == -9790395) {
         TurtModConfig.CustomTheme d = new TurtModConfig.CustomTheme();
         t.hudBackgroundColor = d.hudBackgroundColor;
         t.hudBackgroundAlpha = d.hudBackgroundAlpha;
         t.hudBorderColor = d.hudBorderColor;
         t.cornerRadius = d.cornerRadius;
         t.hudGlass = d.hudGlass;
         t.hudAccentBar = d.hudAccentBar;
         ConfigManager.save(cfg);
         TurtLogger.info("Upgraded HUD theme to the new menu-card defaults.");
      }
   }

   public void onInitializeClient() {
      config = ConfigManager.load();
      migrateLegacyTheme(config);
      CosmeticManager.init();
      TurtLogger.info("TurtMod initializing...");
      TurtLogger.info("Config loaded from: " + (config != null ? "memory" : "FAILED"));
      TurtDiscordRpcService.bootstrap(config);
      class_304.class_11900 generalCat = class_11900.method_74698(class_2960.method_60655("turtmod", "general"));
      class_304.class_11900 visualCat = class_11900.method_74698(class_2960.method_60655("turtmod", "visual"));
      class_304.class_11900 hudCat = class_11900.method_74698(class_2960.method_60655("turtmod", "hud"));
      openConfigKey = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.open_config", class_307.field_1668, 46, generalCat));
      freelookKey = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.freelook_hold", class_307.field_1668, 342, visualCat));
      openHealthConfigKey = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.health_config", class_307.field_1668, -1, hudCat));
      toggleHealthIndicatorKey = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.health_toggle", class_307.field_1668, -1, hudCat));
      healthOffsetUpKey = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.health_offset_up", class_307.field_1668, -1, hudCat));
      healthOffsetDownKey = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.health_offset_down", class_307.field_1668, -1, hudCat));
      resetHealthOffsetKey = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.health_offset_reset", class_307.field_1668, -1, hudCat));
      KeyBindingHelper.registerKeyBinding(ZoomFeature.getZoomKey());
      for (int i = 0; i < commandKeyBinds.length; i++) {
         commandKeyBinds[i] = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.command_key_" + (i + 1), class_307.field_1668, -1, generalCat));
      }
      this.registerClientCommands();
      com.turtmod.kit.KitIO.init();
      TurtLogger.success("TurtMod initialized successfully!");
      ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientLifecycleEvents.ClientStopping)(client) -> TurtDiscordRpcService.shutdown());
      HudRenderCallback.EVENT.register(this::onHudRender);

      // Death coordinates: draw on the vanilla death screen (no mixin needed).
      ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
         if (screen instanceof net.minecraft.class_418) {
            DeathCoordsFeature.addDeathScreenButtons(client, screen);
            ScreenEvents.afterRender(screen).register((scr, ctx, mx, my, delta) ->
               DeathCoordsFeature.render(ctx, client, scr.field_22789, scr.field_22790));
         }
      });
   }

   private void registerClientCommands() {
      ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
         LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("turtmod");

         root.then(ClientCommandManager.literal("screenshot")
            .then(ClientCommandManager.literal("open").executes((ctx) -> { ScreenshotUploadFeature.openLastScreenshot(class_310.method_1551()); return 1; }))
            .then(ClientCommandManager.literal("view").executes((ctx) -> { ScreenshotUploadFeature.viewLastScreenshot(class_310.method_1551()); return 1; }))
            .then(ClientCommandManager.literal("folder").executes((ctx) -> { ScreenshotUploadFeature.openScreenshotFolder(class_310.method_1551()); return 1; }))
            .then(ClientCommandManager.literal("copy").executes((ctx) -> { ScreenshotUploadFeature.copyLastScreenshotPath(class_310.method_1551()); return 1; })));

         root.then(ClientCommandManager.literal("cmdkey")
            .then(ClientCommandManager.literal("set")
               .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(1, CommandKeysFeature.SLOTS))
                  .then(ClientCommandManager.argument("command", StringArgumentType.greedyString()).executes((ctx) -> {
                     int slot = IntegerArgumentType.getInteger(ctx, "slot");
                     String text = StringArgumentType.getString(ctx, "command");
                     config.misc.ensureCommandKeys();
                     TurtModConfig.CommandKey key = config.misc.commandKeyMacros[slot - 1];
                     key.messages.clear();
                     key.messages.add(new TurtModConfig.CmdMsg(text, 0));
                     ConfigManager.save(config);
                     sendCommandKeyFeedback("Command Key " + slot + " set to: " + text);
                     return 1;
                  }))))
            .then(ClientCommandManager.literal("clear")
               .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(1, CommandKeysFeature.SLOTS)).executes((ctx) -> {
                  int slot = IntegerArgumentType.getInteger(ctx, "slot");
                  config.misc.ensureCommandKeys();
                  config.misc.commandKeyMacros[slot - 1].messages.clear();
                  ConfigManager.save(config);
                  sendCommandKeyFeedback("Command Key " + slot + " cleared.");
                  return 1;
               })))
            .then(ClientCommandManager.literal("list").executes((ctx) -> {
               config.misc.ensureCommandKeys();
               for (int i = 0; i < CommandKeysFeature.SLOTS; i++) {
                  TurtModConfig.CommandKey key = config.misc.commandKeyMacros[i];
                  int lines = key == null || key.messages == null ? 0 : key.messages.size();
                  String first = lines > 0 ? key.messages.get(0).text : "";
                  sendCommandKeyFeedback("Key " + (i + 1) + ": " + (lines == 0 ? "(empty)" : lines + " line(s), e.g. " + first));
               }
               return 1;
            })));

         root.then(ClientCommandManager.literal("particle")
            .then(ClientCommandManager.literal("hide").then(ClientCommandManager.argument("id", StringArgumentType.greedyString())
               .suggests((ctx, b) -> class_2172.method_9265(registryIds(class_7923.field_41180), b))
               .executes((ctx) -> { addToList(config.misc.hiddenParticleIds, StringArgumentType.getString(ctx, "id"), "Hiding particle"); return 1; })))
            .then(ClientCommandManager.literal("show").then(ClientCommandManager.argument("id", StringArgumentType.greedyString())
               .suggests((ctx, b) -> class_2172.method_9265(config.misc.hiddenParticleIds, b))
               .executes((ctx) -> { removeFromList(config.misc.hiddenParticleIds, StringArgumentType.getString(ctx, "id"), "Showing particle"); return 1; })))
            .then(ClientCommandManager.literal("list").executes((ctx) -> { sendCommandKeyFeedback("Hidden particles: " + listOrNone(config.misc.hiddenParticleIds)); return 1; }))
            .then(ClientCommandManager.literal("clear").executes((ctx) -> { config.misc.hiddenParticleIds.clear(); ConfigManager.save(config); sendCommandKeyFeedback("Cleared hidden particles."); return 1; })));

         root.then(ClientCommandManager.literal("sound")
            .then(ClientCommandManager.literal("mute").then(ClientCommandManager.argument("id", StringArgumentType.greedyString())
               .suggests((ctx, b) -> class_2172.method_9265(registryIds(class_7923.field_41172), b))
               .executes((ctx) -> { addToList(config.misc.mutedSoundIds, StringArgumentType.getString(ctx, "id"), "Muting sound"); return 1; })))
            .then(ClientCommandManager.literal("unmute").then(ClientCommandManager.argument("id", StringArgumentType.greedyString())
               .suggests((ctx, b) -> class_2172.method_9265(config.misc.mutedSoundIds, b))
               .executes((ctx) -> { removeFromList(config.misc.mutedSoundIds, StringArgumentType.getString(ctx, "id"), "Unmuting sound"); return 1; })))
            .then(ClientCommandManager.literal("list").executes((ctx) -> { sendCommandKeyFeedback("Muted sounds: " + listOrNone(config.misc.mutedSoundIds)); return 1; }))
            .then(ClientCommandManager.literal("clear").executes((ctx) -> { config.misc.mutedSoundIds.clear(); ConfigManager.save(config); sendCommandKeyFeedback("Cleared muted sounds."); return 1; })));

         root.then(ClientCommandManager.literal("kit")
            .then(ClientCommandManager.literal("save").then(ClientCommandManager.argument("name", StringArgumentType.word()).executes((ctx) -> {
               String name = StringArgumentType.getString(ctx, "name");
               if (com.turtmod.kit.KitIO.exists(name)) {
                  sendCommandKeyFeedback("A kit named '" + name + "' already exists.");
               } else {
                  sendCommandKeyFeedback(com.turtmod.kit.KitManager.saveKit(name) ? "Saved kit: " + name : "Failed to save kit.");
               }
               return 1;
            })))
            .then(ClientCommandManager.literal("load").then(ClientCommandManager.argument("name", StringArgumentType.word())
               .suggests((ctx, b) -> class_2172.method_9265(com.turtmod.kit.KitIO.listKits(), b))
               .executes((ctx) -> {
                  String name = StringArgumentType.getString(ctx, "name");
                  String err = com.turtmod.kit.KitManager.loadKit(name);
                  sendCommandKeyFeedback(err == null ? "Equipping kit: " + name : err);
                  return 1;
               })))
            .then(ClientCommandManager.literal("delete").then(ClientCommandManager.argument("name", StringArgumentType.word())
               .suggests((ctx, b) -> class_2172.method_9265(com.turtmod.kit.KitIO.listKits(), b))
               .executes((ctx) -> {
                  String name = StringArgumentType.getString(ctx, "name");
                  try { com.turtmod.kit.KitIO.moveToTrash(name); sendCommandKeyFeedback("Deleted kit: " + name); }
                  catch (Exception e) { sendCommandKeyFeedback("Could not delete kit (file error)."); }
                  return 1;
               })))
            .then(ClientCommandManager.literal("preview").then(ClientCommandManager.argument("name", StringArgumentType.word())
               .suggests((ctx, b) -> class_2172.method_9265(com.turtmod.kit.KitIO.listKits(), b))
               .executes((ctx) -> {
                  String name = StringArgumentType.getString(ctx, "name");
                  if (!com.turtmod.kit.KitIO.exists(name)) {
                     sendCommandKeyFeedback("No kit named '" + name + "'.");
                  } else {
                     class_310 mc = class_310.method_1551();
                     mc.execute(() -> mc.method_1507(com.turtmod.kit.KitPreviewScreen.ofKit(mc.field_1755, name)));
                  }
                  return 1;
               })))
            .then(ClientCommandManager.literal("list").executes((ctx) -> { sendCommandKeyFeedback("Kits: " + listOrNone(com.turtmod.kit.KitIO.listKits())); return 1; })));

         // Death-screen actions, also exposed as clickable chat links in the death message.
         root.then(ClientCommandManager.literal("death")
            .then(ClientCommandManager.literal("copy").executes((ctx) -> { DeathCoordsFeature.copyCoordsAction(); return 1; }))
            .then(ClientCommandManager.literal("tp").executes((ctx) -> { DeathCoordsFeature.respawnAndTpAction(); return 1; }))
            .then(ClientCommandManager.literal("items").executes((ctx) -> { DeathCoordsFeature.viewItemsAction(); return 1; })));

         dispatcher.register(root);
      });
   }

   private static java.util.List<String> registryIds(net.minecraft.class_2378<?> registry) {
      return registry.method_10235().stream().map(net.minecraft.class_2960::toString).sorted().toList();
   }

   private static void addToList(java.util.List<String> list, String id, String label) {
      if (!list.contains(id)) {
         list.add(id);
      }
      ConfigManager.save(config);
      sendCommandKeyFeedback(label + ": " + id);
   }

   private static void removeFromList(java.util.List<String> list, String id, String label) {
      list.remove(id);
      ConfigManager.save(config);
      sendCommandKeyFeedback(label + ": " + id);
   }

   private static String listOrNone(java.util.List<String> list) {
      return list.isEmpty() ? "(none)" : String.join(", ", list);
   }

   private static void sendCommandKeyFeedback(String message) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1724 != null) {
         mc.field_1724.method_7353(com.turtmod.ui.TurtChat.message(
            class_2561.method_43470(message).method_27692(net.minecraft.class_124.field_1080)), false);
      }
   }

   private void onClientTick(class_310 client) {
      while(openConfigKey.method_1436()) {
         if (client.field_1755 == null) {
            client.method_1507(new TurtModMainMenuScreen((class_437)null));
         }
      }

      while(openHealthConfigKey.method_1436()) {
         if (client.field_1755 == null) {
            client.method_1507(TurtModConfigScreenFactory.createForModule((class_437)null, TurtModConfigScreenFactory.ModuleKind.HEALTH_INDICATOR));
         }
      }

      while(toggleHealthIndicatorKey.method_1436()) {
         config.combat.playerHealthIndicator = !config.combat.playerHealthIndicator;
         ConfigManager.save(config);
         com.turtmod.hud.ModuleToastFeature.notify("Health Indicator", config.combat.playerHealthIndicator);
      }

      while(healthOffsetUpKey.method_1436()) {
         config.combat.playerHealthIndicatorYOffset += 0.1F;
         ConfigManager.save(config);
      }

      while(healthOffsetDownKey.method_1436()) {
         config.combat.playerHealthIndicatorYOffset -= 0.1F;
         ConfigManager.save(config);
      }

      while(resetHealthOffsetKey.method_1436()) {
         config.combat.playerHealthIndicatorYOffset = 0.6F;
         ConfigManager.save(config);
      }

      for (int i = 0; i < commandKeyBinds.length; i++) {
         while (commandKeyBinds[i] != null && commandKeyBinds[i].method_1436()) {
            CommandKeysFeature.trigger(client, config, i);
         }
      }
      safeRender("commandKeys.tick", () -> CommandKeysFeature.tick(client, config));

      // Per-feature ticks isolated so one feature's exception can't break the whole tick loop.
      safeRender("fullbright.tick", () -> FullbrightFeature.tick(client, config));
      safeRender("zoom.tick", () -> ZoomFeature.tick(client, config));
      safeRender("hurtCam.tick", () -> HurtCamFeature.tick(client, config));
      safeRender("screenEffects.tick", () -> ScreenEffectsFeature.tick(client, config));
      safeRender("reach.tick", () -> ReachDisplayFeature.tick(client, config));
      safeRender("cpsTracker.tick", () -> CPSTracker.tick());
      safeRender("keystrokes.tick", () -> KeystrokesFeature.tick(client, config));
      safeRender("cps.tick", () -> CpsCounterFeature.tick(client, config));
      safeRender("deathCoords.tick", () -> DeathCoordsFeature.tick(client, config));
      safeRender("kit.tick", () -> com.turtmod.kit.KitManager.tick());
      safeRender("shield.tick", () -> ShieldTracker.tick());
      safeRender("gradient.tick", () -> com.turtmod.hud.GradientRuntime.tick(config));
      safeRender("discord.tick", () -> TurtDiscordRpcService.tick(client, config));
      safeRender("freelook.tick", () -> this.updateFreelook(client));
   }

   private void onHudRender(class_332 context, class_9779 tickCounter) {
      class_310 client = class_310.method_1551();
      // Defensive: never let a single TurtMod HUD feature's exception take down the whole game.
      // Each render runs in isolation so one failure can't skip the rest or crash the render thread.
      if (client.field_1724 != null && !client.field_1690.field_1842) {
         // HUDs stay on-screen NON-destructively: each feature clamps its own DISPLAY position each frame
         // (via HudEditorFeature.clampToScreenX/Y) without ever rewriting the saved config, so a resize
         // never changes the user's saved layout.
         safeRender("health", () -> HealthNumberFeature.render(context, client, config));
         safeRender("fpsPing", () -> FpsPingOverlayFeature.render(context, client, config));
         safeRender("panels", () -> HudPanelsFeature.render(context, client, config));
         safeRender("reach", () -> ReachDisplayFeature.render(context, client, config));
         safeRender("sprint", () -> ToggleSprintFeature.render(context, client, config));
         safeRender("keystrokes", () -> KeystrokesFeature.render(context, client, config));
         safeRender("cps", () -> CpsCounterFeature.render(context, client, config));
         safeRender("inventory", () -> InventoryHudFeature.render(context, client, config));
         safeRender("elytra", () -> ElytraPitchFeature.render(context, client, config));
         safeRender("coords", () -> com.turtmod.hud.CoordinatesHudFeature.render(context, client, config));
         safeRender("screenshotPreview", () -> com.turtmod.chat.ScreenshotPreview.render(context, client, config));
         safeRender("moduleToasts", () -> com.turtmod.hud.ModuleToastFeature.render(context, client, config));
      }
   }

   // Throttled per-feature error log so a broken feature warns once-ish instead of spamming.
   private static final java.util.Map<String, Long> lastRenderError = new java.util.HashMap<>();

   private void safeRender(String name, Runnable r) {
      try {
         r.run();
      } catch (Throwable t) {
         long now = System.currentTimeMillis();
         Long last = lastRenderError.get(name);
         if (last == null || now - last > 10_000L) {
            lastRenderError.put(name, now);
            TurtLogger.error("Feature '" + name + "' threw (suppressed to protect the game): " + t);
         }
      }
   }

   private void updateFreelook(class_310 client) {
      if (client.field_1690 != null && config != null) {
         boolean shouldFreelook = config.visual.freelookEnabled && freelookKey.method_1434();
         FreeLookFeature.updateActive(client, config, shouldFreelook);
      }
   }
}
