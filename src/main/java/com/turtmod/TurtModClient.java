package com.turtmod;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.turtmod.chat.ScreenshotUploadFeature;
import com.turtmod.combat.HealthNumberFeature;
import com.turtmod.config.ConfigManager;
import com.turtmod.config.TurtModConfig;
import com.turtmod.config.TurtModMainMenuScreen;
import com.turtmod.config.TurtModWalksyConfigScreenFactory;
import com.turtmod.config.TurtModWalksyConfigScreenFactory.ModuleKind;
import com.turtmod.cosmetics.CosmeticManager;
import com.turtmod.discord.TurtDiscordRpcService;
import com.turtmod.hud.CpsCounterFeature;
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
import com.turtmod.visual.ProjectileTrailsFeature;
import com.turtmod.visual.ScreenEffectsFeature;
import com.turtmod.visual.ZoomFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
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
   private static final java.util.Map<ModuleKind, class_304> moduleToggleKeys = new java.util.EnumMap<>(ModuleKind.class);
   /** Modules that get a (default-unbound) toggle keybind, editable in their config page or MC Controls. */
   private static final ModuleKind[] MODULE_TOGGLE_KINDS = new ModuleKind[]{
      ModuleKind.FULLBRIGHT, ModuleKind.HIT_COLOR, ModuleKind.SCOREBOARD, ModuleKind.BLOCK_OUTLINE,
      ModuleKind.ELYTRA_HUD, ModuleKind.OWN_NAMETAG, ModuleKind.ARMOR_HUD, ModuleKind.POTION_HUD,
      ModuleKind.FPS_PING, ModuleKind.KEYSTROKES, ModuleKind.CPS_COUNTER, ModuleKind.COORDINATES_HUD,
      ModuleKind.CLEAN_F3, ModuleKind.INVENTORY_HUD, ModuleKind.REACH, ModuleKind.CUSTOM_HITBOXES
   };

   /** The keybind that toggles/activates a module, or null if the module has none. Special-cased
    *  modules reuse their existing dedicated bindings (Zoom/Freelook/Health). */
   public static class_304 getModuleToggleKey(ModuleKind kind) {
      return switch (kind) {
         case ZOOM -> ZoomFeature.getZoomKey();
         case FREELOOK -> freelookKey;
         case HEALTH_INDICATOR -> toggleHealthIndicatorKey;
         default -> moduleToggleKeys.get(kind);
      };
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

   /** Re-skins the WalksyLib config GUI (the per-module settings screens, all tabs) to
    *  match turtmod's palette by recolouring its shared outline colours. WalksyLib draws
    *  every widget's outline from MainColors, so this themes the whole settings UI without
    *  any render mixins. Wrapped defensively in case the bundled lib version differs. */
   private static void turtmod$themeWalksyLib() {
      try {
         // Hovered widget outline → turtmod accent pink; idle outline → faint green tint;
         // widget body outline → a dark turtle-shell green-black instead of flat black.
         main.walksy.lib.core.utils.MainColors.OUTLINE_WHITE_HOVERED = new java.awt.Color(255, 157, 174, 205);
         main.walksy.lib.core.utils.MainColors.OUTLINE_WHITE = new java.awt.Color(141, 208, 95, 64);
         main.walksy.lib.core.utils.MainColors.OUTLINE_BLACK = new java.awt.Color(8, 18, 13, 196);
      } catch (Throwable ignored) {
      }
   }

   public void onInitializeClient() {
      config = ConfigManager.load();
      CosmeticManager.init();
      turtmod$themeWalksyLib();
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
      class_304.class_11900 modulesCat = class_11900.method_74698(class_2960.method_60655("turtmod", "modules"));
      for (ModuleKind kind : MODULE_TOGGLE_KINDS) {
         class_304 k = KeyBindingHelper.registerKeyBinding(new class_304("key.turtmod.toggle." + kind.name().toLowerCase(java.util.Locale.ROOT), class_307.field_1668, -1, modulesCat));
         moduleToggleKeys.put(kind, k);
      }
      this.registerClientCommands();
      TurtLogger.success("TurtMod initialized successfully!");
      ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientLifecycleEvents.ClientStopping)(client) -> TurtDiscordRpcService.shutdown());
      HudRenderCallback.EVENT.register(this::onHudRender);
      WorldRenderEvents.END_MAIN.register((WorldRenderEvents.EndMain)(context) -> ProjectileTrailsFeature.render(context));
   }

   private void registerClientCommands() {
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("turtmod").then(ClientCommandManager.literal("uploadlastscreenshot").executes((ctx) -> {
            ScreenshotUploadFeature.uploadLastScreenshot(class_310.method_1551());
            return 1;
         }))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("screenshot").then(ClientCommandManager.literal("open").executes((ctx) -> {
            ScreenshotUploadFeature.openLastScreenshot(class_310.method_1551());
            return 1;
         }))).then(ClientCommandManager.literal("view").executes((ctx) -> {
            ScreenshotUploadFeature.viewLastScreenshot(class_310.method_1551());
            return 1;
         }))).then(ClientCommandManager.literal("folder").executes((ctx) -> {
            ScreenshotUploadFeature.openScreenshotFolder(class_310.method_1551());
            return 1;
         }))).then(ClientCommandManager.literal("copy").executes((ctx) -> {
            ScreenshotUploadFeature.copyLastScreenshotPath(class_310.method_1551());
            return 1;
         }))).then(ClientCommandManager.literal("upload").executes((ctx) -> {
            ScreenshotUploadFeature.uploadLastScreenshot(class_310.method_1551());
            return 1;
         })))));
   }

   private void onClientTick(class_310 client) {
      while(openConfigKey.method_1436()) {
         if (client.field_1755 == null) {
            client.method_1507(new TurtModMainMenuScreen((class_437)null));
         }
      }

      while(openHealthConfigKey.method_1436()) {
         if (client.field_1755 == null) {
            client.method_1507(TurtModWalksyConfigScreenFactory.createForModule((class_437)null, TurtModWalksyConfigScreenFactory.ModuleKind.HEALTH_INDICATOR));
         }
      }

      while(toggleHealthIndicatorKey.method_1436()) {
         config.combat.playerHealthIndicator = !config.combat.playerHealthIndicator;
         ConfigManager.save(config);
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

      for (java.util.Map.Entry<ModuleKind, class_304> entry : moduleToggleKeys.entrySet()) {
         while (entry.getValue().method_1436()) {
            this.toggleModuleConfig(entry.getKey());
         }
      }

      FullbrightFeature.tick(client, config);
      ZoomFeature.tick(client, config);
      HurtCamFeature.tick(client, config);
      ScreenEffectsFeature.tick(client, config);
      ReachDisplayFeature.tick(client, config);
      CPSTracker.tick();
      KeystrokesFeature.tick(client, config);
      CpsCounterFeature.tick(client, config);
      ShieldTracker.tick();
      TurtDiscordRpcService.tick(client, config);
      this.updateFreelook(client);
   }

   /** Flip the enable flag backing a module's toggle keybind, then persist. */
   private void toggleModuleConfig(ModuleKind kind) {
      switch (kind) {
         case FULLBRIGHT -> config.visual.fullbright.enabled = !config.visual.fullbright.enabled;
         case HIT_COLOR -> config.visual.hitColor.enabled = !config.visual.hitColor.enabled;
         case SCOREBOARD -> config.visual.hideScoreboard = !config.visual.hideScoreboard;
         case BLOCK_OUTLINE -> config.visual.recolorBlockOutline = !config.visual.recolorBlockOutline;
         case ELYTRA_HUD -> config.visual.elytraPitchHud = !config.visual.elytraPitchHud;
         case OWN_NAMETAG -> config.visual.showOwnNametag = !config.visual.showOwnNametag;
         case ARMOR_HUD -> config.hud.movableArmorHud = !config.hud.movableArmorHud;
         case POTION_HUD -> config.hud.movablePotionHud = !config.hud.movablePotionHud;
         case FPS_PING -> config.hud.minimalFpsPingOverlay = !config.hud.minimalFpsPingOverlay;
         case KEYSTROKES -> config.hud.keystrokesHud = !config.hud.keystrokesHud;
         case CPS_COUNTER -> config.hud.cpsCounterHud = !config.hud.cpsCounterHud;
         case COORDINATES_HUD -> config.hud.coordinatesHud = !config.hud.coordinatesHud;
         case CLEAN_F3 -> config.hud.cleanF3Mode = !config.hud.cleanF3Mode;
         case INVENTORY_HUD -> config.hud.inventoryHudEnabled = !config.hud.inventoryHudEnabled;
         case REACH -> config.hud.reachDisplay = !config.hud.reachDisplay;
         case CUSTOM_HITBOXES -> config.hud.customHitboxes = !config.hud.customHitboxes;
         default -> {
         }
      }
      ConfigManager.save(config);

      // Pop an action-bar toast so the toggle has visible feedback.
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1724 != null) {
         boolean on = isModuleEnabled(kind);
         String name = TurtModWalksyConfigScreenFactory.getModuleDisplayName(kind);
         class_2561 msg = class_2561.method_43470(name + ": ")
            .method_10852(class_2561.method_43470(on ? "ON" : "OFF")
               .method_27692(on ? net.minecraft.class_124.field_1060 : net.minecraft.class_124.field_1061));
         mc.field_1724.method_7353(msg, true);
      }
   }

   /** Current enable state backing a module's toggle keybind (for action-bar feedback). */
   private boolean isModuleEnabled(ModuleKind kind) {
      return switch (kind) {
         case FULLBRIGHT -> config.visual.fullbright.enabled;
         case HIT_COLOR -> config.visual.hitColor.enabled;
         case SCOREBOARD -> config.visual.hideScoreboard;
         case BLOCK_OUTLINE -> config.visual.recolorBlockOutline;
         case ELYTRA_HUD -> config.visual.elytraPitchHud;
         case OWN_NAMETAG -> config.visual.showOwnNametag;
         case ARMOR_HUD -> config.hud.movableArmorHud;
         case POTION_HUD -> config.hud.movablePotionHud;
         case FPS_PING -> config.hud.minimalFpsPingOverlay;
         case KEYSTROKES -> config.hud.keystrokesHud;
         case CPS_COUNTER -> config.hud.cpsCounterHud;
         case COORDINATES_HUD -> config.hud.coordinatesHud;
         case CLEAN_F3 -> config.hud.cleanF3Mode;
         case INVENTORY_HUD -> config.hud.inventoryHudEnabled;
         case REACH -> config.hud.reachDisplay;
         case CUSTOM_HITBOXES -> config.hud.customHitboxes;
         default -> false;
      };
   }

   private void onHudRender(class_332 context, class_9779 tickCounter) {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && !client.field_1690.field_1842) {
         HealthNumberFeature.render(context, client, config);
         FpsPingOverlayFeature.render(context, client, config);
         HudPanelsFeature.render(context, client, config);
         ReachDisplayFeature.render(context, client, config);
         ToggleSprintFeature.render(context, client, config);
         KeystrokesFeature.render(context, client, config);
         CpsCounterFeature.render(context, client, config);
         InventoryHudFeature.render(context, client, config);
         ElytraPitchFeature.render(context, client, config);
         com.turtmod.hud.CoordinatesHudFeature.render(context, client, config);
      }
   }

   private void updateFreelook(class_310 client) {
      if (client.field_1690 != null && config != null) {
         boolean shouldFreelook = config.visual.freelookEnabled && freelookKey.method_1434();
         FreeLookFeature.updateActive(client, config, shouldFreelook);
      }
   }
}
