package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.hud.HudEditorScreen;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUICheckbox;
import com.turtmod.ui.TurtUILabel;
import com.turtmod.ui.TurtSounds;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUIPanel;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

public final class TurtModClientConfigScreen extends class_437 {
   private final class_437 parent;
   private Tab activeTab;
   private TurtUIPanel mainPanel;
   private TurtUILabel titleLabel;
   private final List<TurtUICheckbox> checkboxes;
   private final List<TurtUIButton> mainTabs;
   private final List<TurtUIButton> controlButtons;
   private class_342 moduleSearchField;
   private boolean searchFieldFocused;
   private float searchFocusAnim = 0f;
   private long searchFocusNs = System.nanoTime();
   private float openFade = 0f;
   private float tabFade = 1f;     // eases 0→1 on tab switch for a slide-in
   private float resetFlash = 0f;  // green confirmation flash over the grid after Reset All
   private long lastFrameNs = System.nanoTime();
   private long statCountNs = 0L;  // throttle for the sidebar active-module counter
   private int cachedActiveCount = 0;
   private int cachedTotalCount = 0;

   // ── Fit-to-screen + scrolling state ───────────────────────────────────────
   // The panel is laid out at a fixed LOGICAL size and scaled to fit any resolution / GUI scale.
   private static final int LOGICAL_W = 520;
   private static final int LOGICAL_H = 350;
   private final TurtUIScale uiScale = new TurtUIScale();
   private float scrollY = 0f;       // current vertical scroll of the module grid (logical px)
   private int gridContentHeight = 0; // total height of all module rows (logical px)
   private int listTop, listBottom, listLeft, listW; // module grid viewport (logical px)
   // Colours live in com.turtmod.ui.Palette now — these aliases keep the rest of the screen readable.
   private static final Color PANEL_BG = com.turtmod.ui.Palette.PANEL_BG;
   private static final Color PANEL_BORDER = com.turtmod.ui.Palette.PANEL_BORDER;
   private static final Color ACCENT_GREEN = com.turtmod.ui.Palette.GREEN;
   private static final Color ACCENT_PINK = com.turtmod.ui.Palette.PINK;
   private static final Color TEXT_MAIN = com.turtmod.ui.Palette.TEXT;
   private static final Color BTN_BG = com.turtmod.ui.Palette.BTN_BG;
   private static final Color BTN_HOVER = com.turtmod.ui.Palette.BTN_HOVER;
   private static final Color SEARCH_BG = com.turtmod.ui.Palette.SEARCH_BG;
   private static final Color SEARCH_BORDER = com.turtmod.ui.Palette.SEARCH_BORDER;
   private static final int PANEL_WIDTH = 440;
   private static final int PANEL_HEIGHT = 300;

   public TurtModClientConfigScreen(class_437 parent) {
      super(class_2561.method_43470("TurtMod Settings"));
      this.activeTab = TurtModClientConfigScreen.Tab.VISUALS;
      this.checkboxes = new ArrayList();
      this.mainTabs = new ArrayList();
      this.controlButtons = new ArrayList();
      this.searchFieldFocused = false;
      this.parent = parent;
   }

   protected void method_25426() {
      this.mainTabs.clear();
      this.controlButtons.clear();
      // The panel is laid out at a fixed LOGICAL size (0,0 .. LOGICAL_W,LOGICAL_H). method_25394
      // scales this whole layout to fit the real screen, so it stays on-screen and never overlaps at
      // any resolution / GUI scale. All coordinates below are logical.
      int panelX = 0;
      int panelY = 0;
      int panelW = LOGICAL_W;
      int panelH = LOGICAL_H;
      TurtUITheme theme = new TurtUITheme(PANEL_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_GREEN);
      TurtUITheme btnTheme = new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
      this.mainPanel = new TurtUIPanel(panelX, panelY, panelW, panelH, theme, false);
      this.titleLabel = new TurtUILabel(panelX + 20, panelY + 8, this.field_22793, "", theme, false, false);

      // Control buttons sit in the zone just above the chrome footer — laid out as four equal
      // columns across the content width so they always fit (any resolution / GUI scale).
      int contentX = TurtLauncher.contentX(panelX);
      int contentW = TurtLauncher.contentW(panelW);
      int btnZoneTop = panelY + panelH - TurtLauncher.FOOTER_H - 46;
      int btnH = 20;
      int btnGap = 6;
      int btnW = (contentW - btnGap * 3) / 4;
      this.controlButtons.add(new TurtUIButton(contentX, btnZoneTop, btnW, btnH, "All Config", btnTheme,
         () -> this.field_22787.method_1507(TurtModConfigScreenFactory.create(this))));
      this.controlButtons.add(new TurtUIButton(contentX + (btnW + btnGap), btnZoneTop, btnW, btnH, "HUD Editor", btnTheme,
         () -> this.field_22787.method_1507(new HudEditorScreen(this))));
      this.controlButtons.add(new TurtUIButton(contentX + (btnW + btnGap) * 2, btnZoneTop, btnW, btnH, "Reset All", btnTheme, () -> {
         ConfigManager.reset();
         TurtModClient.reloadConfig();
         this.rebuildModuleList();
         this.resetFlash = 1f;
      }));
      this.controlButtons.add(new TurtUIButton(contentX + (btnW + btnGap) * 3, btnZoneTop, btnW, btnH, "Back", btnTheme,
         () -> this.field_22787.method_1507(this.parent)));

      // Search field
      int searchX = contentX;
      int searchY = TurtLauncher.contentY(panelY);
      int searchW = TurtLauncher.contentW(panelW);
      this.moduleSearchField = new class_342(this.field_22793, searchX, searchY, searchW, 16, class_2561.method_43470("Search"));
      this.moduleSearchField.method_1880(64);
      this.moduleSearchField.method_47404(class_2561.method_43470("Search modules..."));
      this.moduleSearchField.method_1863((s) -> this.rebuildModuleList());
      this.rebuildModuleList();
   }

   private void rebuildTabList() {
      // Tabs are now rendered via TurtLauncher.drawNavItem — no button objects needed
   }

   private void rebuildModuleList() {
      this.checkboxes.clear();
      int panelX = this.mainPanel.x;
      int panelY = this.mainPanel.y;
      int panelW = this.mainPanel.width;
      int panelH = this.mainPanel.height;

      // Module grid viewport (logical px): below the search bar, above the control-button zone.
      this.listLeft   = TurtLauncher.contentX(panelX);
      this.listW      = TurtLauncher.contentW(panelW);
      this.listTop    = TurtLauncher.contentY(panelY) + 22;            // below the 16px search bar + margin
      this.listBottom = panelY + panelH - TurtLauncher.FOOTER_H - 52;  // above the control buttons

      // Simple two-column checkbox grid (row-major so it scrolls vertically).
      int cols = 2;
      int colGap = 8;
      int colWidth = (this.listW - colGap * (cols - 1)) / cols;
      int rowHeight = 20;

      TurtModConfig cfg = TurtModClient.getConfig();
      TurtUITheme theme = new TurtUITheme(PANEL_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_GREEN);
      String q = this.moduleSearchField != null ? this.moduleSearchField.method_1882().trim().toLowerCase(Locale.ROOT) : "";

      List<ModuleOption> options = q.isEmpty()
         ? this.getModuleOptionsForTab(this.activeTab)
         : getAllModuleOptions(cfg).stream().filter((o) -> o.displayName.toLowerCase(Locale.ROOT).contains(q)).toList();

      // Pinned modules float to the front (stable — order within each group is preserved).
      List<String> pins = cfg.misc.pinnedModules;
      if (pins != null && !pins.isEmpty()) {
         options = new ArrayList<>(options);
         options.sort((a, b) -> {
            boolean pa = pins.contains(a.displayName);
            boolean pb = pins.contains(b.displayName);
            return pa == pb ? 0 : (pa ? -1 : 1);
         });
      }

      for (int i = 0; i < options.size(); i++) {
         ModuleOption option = options.get(i);
         int col = i % cols;
         int row = i / cols;
         int cx = this.listLeft + col * (colWidth + colGap);
         int cy = this.listTop + row * rowHeight;
         TurtUICheckbox cb = new TurtUICheckbox(cx, cy, 12, 6, this.field_22793, option.displayName, theme, true,
            (Boolean)option.getEnabled.get(), (v) -> option.setEnabled.accept(v));
         cb.rowWidth = colWidth;          // Lunar-style full-width row + pill toggle
         cb.rowHeight = rowHeight - 2;
         cb.highlightQuery = q.isEmpty() ? null : q;
         this.checkboxes.add(cb);
      }

      int rows = (options.size() + cols - 1) / cols;
      this.gridContentHeight = rows * rowHeight;
      this.clampScroll();
   }

   /** Maximum scroll offset given the current grid height vs. its viewport. */
   private int maxScroll() {
      return Math.max(0, this.gridContentHeight - (this.listBottom - this.listTop));
   }

   private void clampScroll() {
      this.scrollY = Math.max(0f, Math.min(this.scrollY, (float)this.maxScroll()));
   }

   private List<ModuleOption> getAllModuleOptions(TurtModConfig cfg) {
      List<ModuleOption> all = new ArrayList();

      for(Tab tab : TurtModClientConfigScreen.Tab.values()) {
         all.addAll(this.getModuleOptionsForTab(tab));
      }

      return all;
   }

   private List<ModuleOption> getModuleOptionsForTab(Tab tab) {
      TurtModConfig cfg = TurtModClient.getConfig();
      List var10000;
      switch (tab.ordinal()) {
         // 🎨 Visuals (10)
         case 0 -> var10000 = List.of(new ModuleOption("Fullbright", () -> cfg.visual.fullbright.enabled, (v) -> cfg.visual.fullbright.enabled = v), new ModuleOption("Low Fire", () -> cfg.visual.disableFireOverlay, (v) -> cfg.visual.disableFireOverlay = v), new ModuleOption("Fog Tweaks", () -> cfg.visual.disableAllFog, (v) -> cfg.visual.disableAllFog = v), new ModuleOption("Overlays", () -> cfg.visual.disablePumpkinBlur, (v) -> cfg.visual.disablePumpkinBlur = v), new ModuleOption("Hurt Cam", () -> cfg.visual.hurtCamEnabled, (v) -> cfg.visual.hurtCamEnabled = v), new ModuleOption("Block Outline", () -> cfg.visual.recolorBlockOutline, (v) -> cfg.visual.recolorBlockOutline = v), new ModuleOption("Hit Color", () -> cfg.visual.hitColor.enabled, (v) -> cfg.visual.hitColor.enabled = v), new ModuleOption("Totem Tweaks", () -> cfg.visual.enableSmallTotem, (v) -> cfg.visual.enableSmallTotem = v), new ModuleOption("Own Nametag", () -> cfg.visual.showOwnNametag, (v) -> cfg.visual.showOwnNametag = v), new ModuleOption("Hitboxes", () -> cfg.hud.customHitboxes, (v) -> cfg.hud.customHitboxes = v));
         // 🖥 HUD (10)
         case 1 -> var10000 = List.of(new ModuleOption("Armor HUD", () -> cfg.hud.movableArmorHud, (v) -> cfg.hud.movableArmorHud = v), new ModuleOption("Potion HUD", () -> cfg.hud.movablePotionHud, (v) -> cfg.hud.movablePotionHud = v), new ModuleOption("FPS/Ping", () -> cfg.hud.minimalFpsPingOverlay, (v) -> cfg.hud.minimalFpsPingOverlay = v), new ModuleOption("Inventory HUD", () -> cfg.hud.inventoryHudEnabled, (v) -> cfg.hud.inventoryHudEnabled = v), new ModuleOption("Keystrokes", () -> cfg.hud.keystrokesHud, (v) -> cfg.hud.keystrokesHud = v), new ModuleOption("CPS Counter", () -> cfg.hud.cpsCounterHud, (v) -> cfg.hud.cpsCounterHud = v), new ModuleOption("Elytra Pitch HUD", () -> cfg.visual.elytraPitchHud, (v) -> cfg.visual.elytraPitchHud = v), new ModuleOption("Coordinates", () -> cfg.hud.coordinatesHud, (v) -> cfg.hud.coordinatesHud = v), new ModuleOption("Health Indicator", () -> cfg.combat.playerHealthIndicator, (v) -> cfg.combat.playerHealthIndicator = v), new ModuleOption("Reach Display", () -> cfg.hud.reachDisplay, (v) -> cfg.hud.reachDisplay = v), new ModuleOption("Sprint Display", () -> cfg.hud.toggleSprintHud, (v) -> cfg.hud.toggleSprintHud = v), new ModuleOption("Ping Display", () -> cfg.hud.pingInTab || cfg.visual.pingOnNametag, (v) -> { cfg.hud.pingInTab = v; cfg.visual.pingOnNametag = v; }), new ModuleOption("Module Notifications", () -> cfg.hud.moduleToasts, (v) -> cfg.hud.moduleToasts = v));
         // 🛠 Utility (7)
         case 2 -> var10000 = List.of(new ModuleOption("Freelook", () -> cfg.visual.freelookEnabled, (v) -> cfg.visual.freelookEnabled = v), new ModuleOption("Scoreboard Tweaks", () -> !cfg.visual.hideScoreboard, (v) -> cfg.visual.hideScoreboard = !v), new ModuleOption("Shield Tweaks", () -> cfg.visual.shieldStatusRecolor, (v) -> cfg.visual.shieldStatusRecolor = v), new ModuleOption("Held Item Tweaks", () -> cfg.visual.heldItemTweaksEnabled, (v) -> cfg.visual.heldItemTweaksEnabled = v), new ModuleOption("Zoom", () -> cfg.visual.zoomEnabled, (v) -> cfg.visual.zoomEnabled = v), new ModuleOption("Clean F3", () -> cfg.hud.cleanF3Mode, (v) -> cfg.hud.cleanF3Mode = v), new ModuleOption("Title Tweaks", () -> cfg.hud.titleTweaksEnabled, (v) -> cfg.hud.titleTweaksEnabled = v), new ModuleOption("Bossbar Tweaks", () -> cfg.hud.bossbarTweaksEnabled, (v) -> cfg.hud.bossbarTweaksEnabled = v), new ModuleOption("Scrollable Tooltips", () -> cfg.hud.scrollableTooltips, (v) -> cfg.hud.scrollableTooltips = v), new ModuleOption("Chat Tweaks", () -> cfg.hud.chatTweaksEnabled, (v) -> cfg.hud.chatTweaksEnabled = v), new ModuleOption("Screenshot Tools", () -> cfg.hud.betterScreenshotActions, (v) -> cfg.hud.betterScreenshotActions = v), new ModuleOption("Death Coords", () -> cfg.misc.deathCoords, (v) -> cfg.misc.deathCoords = v), new ModuleOption("Mute Sounds", () -> cfg.misc.muteSoundsEnabled, (v) -> cfg.misc.muteSoundsEnabled = v), new ModuleOption("Hide Particles", () -> cfg.misc.hideParticlesEnabled, (v) -> cfg.misc.hideParticlesEnabled = v), new ModuleOption("Clear View", () -> cfg.misc.clearViewEnabled, (v) -> cfg.misc.clearViewEnabled = v), new ModuleOption("Command Keys", () -> cfg.misc.commandKeysEnabled, (v) -> cfg.misc.commandKeysEnabled = v), new ModuleOption("Kit Loader", () -> true, (v) -> {}));
         // 📦 Misc (3)
         case 3 -> var10000 = List.of(new ModuleOption("Theme Settings", () -> true, (v) -> {
}), new ModuleOption("Enable TurtMod", () -> cfg.misc.enabled, (v) -> cfg.misc.enabled = v), new ModuleOption("Discord RPC", () -> cfg.misc.discordRpc.enabled, (v) -> cfg.misc.discordRpc.enabled = v), new ModuleOption("Gamemode Switcher", () -> cfg.misc.noOpGamemodeSwitcher, (v) -> cfg.misc.noOpGamemodeSwitcher = v));
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   /** One-line descriptions shown as a hover tooltip in the module grid (keyed by display name). */
   private static final java.util.Map<String, String> MODULE_DESC = buildModuleDescriptions();

   private static java.util.Map<String, String> buildModuleDescriptions() {
      java.util.Map<String, String> m = new java.util.HashMap<>();
      m.put("Fullbright", "See in the dark — removes all darkness without night vision.");
      m.put("Low Fire", "Lowers the fire overlay so you can see while burning.");
      m.put("Fog Tweaks", "Reduce or remove world fog for clearer view distance.");
      m.put("Overlays", "Hide pumpkin/powder-snow screen overlays.");
      m.put("Hurt Cam", "Toggle the camera tilt when you take damage.");
      m.put("Block Outline", "Recolor the block selection outline.");
      m.put("Hit Color", "Tint entities red when you hit them.");
      m.put("Totem Tweaks", "Shrink the totem-of-undying pop animation.");
      m.put("Own Nametag", "Render your own nametag above your head.");
      m.put("Hitboxes", "Custom entity hitbox rendering.");
      m.put("Armor HUD", "Show your worn armor + durability on screen.");
      m.put("Potion HUD", "Movable active-effects display with styles & sorting.");
      m.put("FPS/Ping", "Minimal FPS and ping overlay.");
      m.put("Inventory HUD", "Show your inventory contents on screen.");
      m.put("Keystrokes", "WASD / mouse keystroke display with CPS.");
      m.put("CPS Counter", "Clicks-per-second counter.");
      m.put("Elytra Pitch HUD", "Show elytra glide pitch angle.");
      m.put("Coordinates", "On-screen XYZ coordinates.");
      m.put("Health Indicator", "Numeric health indicator.");
      m.put("Reach Display", "Show your attack reach distance.");
      m.put("Sprint Display", "Show sprint / sneak / swim state.");
      m.put("Ping Display", "Show ping in tab list and on nametags.");
      m.put("Freelook", "Hold a key to look around without turning.");
      m.put("Scoreboard Tweaks", "Hide numbers/background, scale & move the scoreboard.");
      m.put("Shield Tweaks", "Recolor the shield by its status.");
      m.put("Held Item Tweaks", "Custom held-item size, position & rotation.");
      m.put("Zoom", "Hold a key to zoom in (Optifine-style).");
      m.put("Clean F3", "Cleaner, minimal debug screen.");
      m.put("Screenshot Tools", "View / copy / open actions after a screenshot.");
      m.put("Death Coords", "Save the coordinates where you last died.");
      m.put("Mute Sounds", "Mute selected in-game sounds.");
      m.put("Hide Particles", "Hide selected particle types.");
      m.put("Clear View", "Cleaner first person: hide your own potion swirls and shrink/hide eating crumbs.");
      m.put("Title Tweaks", "Hide, scale, or reposition the on-screen Title & Subtitle.");
      m.put("Bossbar Tweaks", "Hide, hide-in-F3, scale, or reposition server boss bars.");
      m.put("Scrollable Tooltips", "Mouse-wheel scroll tooltips taller than the screen.");
      m.put("Chat Tweaks", "Keep more chat history than vanilla's 100-message limit.");
      m.put("Command Keys", "Bind keys to run chat commands / macros.");
      m.put("Kit Loader", "Save and load hotbar/inventory kits.");
      m.put("Theme Settings", "Customize the HUD theme (colors, borders, opacity).");
      m.put("Enable TurtMod", "Master switch for all TurtMod features.");
      m.put("Discord RPC", "Show TurtMod activity on your Discord profile.");
      m.put("Gamemode Switcher", "Open the F3+F4 gamemode wheel without op.");
      m.put("Module Notifications", "Toast popup when a module toggles on/off.");
      return m;
   }

   private void drawModuleTooltip(class_332 ctx, int mlx, int mly) {
      String desc = null;
      for (TurtUICheckbox cb : this.checkboxes) {
         float vy = cb.y - this.scrollY;
         if (vy < this.listTop - 2 || vy > this.listBottom) {
            continue; // scrolled out of the viewport
         }
         if (cb.isHoveredPublic(mlx, mly + Math.round(this.scrollY))) {
            desc = MODULE_DESC.get(cb.label);
            break;
         }
      }
      if (desc == null) {
         return;
      }
      // Simple word wrap to ~150px.
      java.util.List<String> lines = new ArrayList<>();
      String[] words = desc.split(" ");
      StringBuilder cur = new StringBuilder();
      for (String w : words) {
         String trial = cur.length() == 0 ? w : cur + " " + w;
         if (this.field_22793.method_1727(trial) > 150 && cur.length() > 0) {
            lines.add(cur.toString());
            cur = new StringBuilder(w);
         } else {
            cur = new StringBuilder(trial);
         }
      }
      if (cur.length() > 0) {
         lines.add(cur.toString());
      }

      int tw = 0;
      for (String l : lines) {
         tw = Math.max(tw, this.field_22793.method_1727(l));
      }
      int pad = 5;
      int lh = 10;
      int boxW = tw + pad * 2 + 4;
      int boxH = lines.size() * lh + pad * 2 - 2;
      int bx = mlx + 12;
      int by = mly + 10;
      int panelR = this.mainPanel.x + this.mainPanel.width - 4;
      int panelB = this.mainPanel.y + this.mainPanel.height - 4;
      if (bx + boxW > panelR) {
         bx = mlx - boxW - 8;
      }
      if (by + boxH > panelB) {
         by = panelB - boxH;
      }
      TurtUIUtils.drawRoundedRect(ctx, bx, by, boxW, boxH, 4, new Color(0xF00C0F15, true));
      TurtUIUtils.drawRoundedBorder(ctx, bx, by, boxW, boxH, 4, ACCENT_GREEN);
      ctx.method_25294(bx + 2, by + 4, bx + 4, by + boxH - 4, ACCENT_GREEN.getRGB());
      int ty = by + pad;
      for (String l : lines) {
         ctx.method_51433(this.field_22793, l, bx + pad + 4, ty, 0xFFD8DEE6, false);
         ty += lh;
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f);
      this.lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, dt, 12f);
      this.tabFade = TurtUIUtils.lerp01(this.tabFade, 1f, dt, 14f);
      this.resetFlash = TurtUIUtils.lerp01(this.resetFlash, 0f, dt, 3.5f);

      TurtUIUtils.drawMenuBackdrop(context, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(context, mouseX, mouseY);

      // Fit the fixed logical layout to the real screen (any resolution / GUI scale).
      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      // Mouse in logical coordinates so hover/hit-tests line up under the transform.
      int mlx = (int)this.uiScale.toLogicalX(mouseX);
      int mly = (int)this.uiScale.toLogicalY(mouseY);

      this.uiScale.push(context);
      // subtle slide-up on open
      context.method_51448().pushMatrix();
      float introE = TurtUIUtils.ease(this.openFade);
      context.method_51448().translate(LOGICAL_W / 2f, LOGICAL_H / 2f + (1f - introE) * 12f);
      context.method_51448().scale(0.97f + 0.03f * introE, 0.97f + 0.03f * introE);
      context.method_51448().translate(-LOGICAL_W / 2f, -LOGICAL_H / 2f);

      int px = this.mainPanel.x, py = this.mainPanel.y, pw = this.mainPanel.width, ph = this.mainPanel.height;
      TurtLauncher.drawChrome(context, this.field_22793, px, py, pw, ph, "SETTINGS", null, null);

      // Sidebar: tab buttons as nav items (sliding highlight under the labels)
      Tab[] tabs = TurtModClientConfigScreen.Tab.values();
      TurtLauncher.drawNavIndicator(context, px, py, this.activeTab.ordinal());
      for (int i = 0; i < tabs.length; i++) {
         boolean active = this.activeTab == tabs[i];
         boolean hov = TurtLauncher.navItemHovered(px, py, i, mlx, mly);
         String name = tabs[i] == Tab.HUD ? "HUD" : tabs[i].name().charAt(0) + tabs[i].name().substring(1).toLowerCase();
         TurtLauncher.drawNavItem(context, this.field_22793, px, py, i, name, active, hov);
      }

      // Special (Settings): live count of active modules at the foot of the sidebar.
      // Throttled to ~4x/sec — recomputing the full option list every frame is needless allocation.
      if (now - this.statCountNs > 250_000_000L) {
         this.statCountNs = now;
         int active = 0;
         int total = 0;
         for (ModuleOption o : this.getAllModuleOptions(TurtModClient.getConfig())) {
            total++;
            if (Boolean.TRUE.equals(o.getEnabled.get())) {
               active++;
            }
         }
         this.cachedActiveCount = active;
         this.cachedTotalCount = total;
      }
      int statY = py + this.mainPanel.height - TurtLauncher.FOOTER_H - 15;
      TurtUIUtils.drawRoundedRect(context, px + 6, statY - 3, TurtLauncher.SIDEBAR_W - 12, 14, 4, new Color(0x33000000, true));
      TurtUIUtils.drawText(context, this.field_22793, "⚡ " + this.cachedActiveCount + " / " + this.cachedTotalCount + " active", px + 12, statY, ACCENT_GREEN, false, false);

      // Search bar inside content area
      this.drawSearchBar(context, mlx, mly);

      // Module grid — scissor-clipped to its viewport and scrolled vertically. (Scissor coords are
      // logical here; class_332 applies the current matrix, so they map to the right screen pixels.)
      context.method_44379(this.listLeft - 2, this.listTop - 2, this.listLeft + this.listW + 2, this.listBottom + 2);
      context.method_51448().pushMatrix();
      context.method_51448().translate(0f, -this.scrollY);
      // Lunar-style staggered reveal: rows cascade in from the right when a tab opens.
      for (int i = 0; i < this.checkboxes.size(); i++) {
         TurtUICheckbox cb = this.checkboxes.get(i);
         float rp = this.tabFade * 2.0f - i * 0.12f;
         rp = rp < 0f ? 0f : (rp > 1f ? 1f : rp);
         float e = TurtUIUtils.ease(rp);
         context.method_51448().pushMatrix();
         context.method_51448().translate((1f - e) * 24f, 0f);
         cb.render(context, mlx, mly + Math.round(this.scrollY));
         this.drawPinStar(context, cb, mlx, mly + Math.round(this.scrollY));
         context.method_51448().popMatrix();
      }
      context.method_51448().popMatrix();
      context.method_44380();

      // Friendly on-theme empty state when a search matches nothing.
      if (this.checkboxes.isEmpty()) {
         int ecx = this.listLeft + this.listW / 2;
         int ecy = (this.listTop + this.listBottom) / 2 - 4;
         TurtUIUtils.drawText(context, this.field_22793, "🐢  No modules match", ecx, ecy, new Color(0xFF9CA8B4, true), true, false);
      }

      // Scrollbar (only when the grid overflows its viewport).
      this.drawScrollbar(context);

      // Inline "reset" confirmation: a brief green wash over the grid (no toast).
      if (this.resetFlash > 0.02f) {
         TurtUIUtils.drawRoundedRect(context, this.listLeft - 2, this.listTop - 2, this.listW + 4,
            this.listBottom - this.listTop + 4, 4,
            new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), (int) (70 * this.resetFlash)));
      }

      // Bottom control buttons (already positioned in method_25426)
      for (TurtUIButton btn : this.controlButtons) btn.render(context, mlx, mly, this.field_22793);

      // Lunar/Essential-style hover tooltip describing the module under the cursor.
      this.drawModuleTooltip(context, mlx, mly);

      context.method_51448().popMatrix();
      this.uiScale.pop(context);
      TurtUIUtils.drawOpenFade(context, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   /** Star toggle on a module row: gold ★ when pinned, faint ☆ on row hover. */
   private void drawPinStar(class_332 ctx, TurtUICheckbox cb, int mlx, int mly) {
      List<String> pins = TurtModClient.getConfig().misc.pinnedModules;
      boolean pinned = pins != null && pins.contains(cb.label);
      boolean rowHov = cb.isHoveredPublic(mlx, mly);
      if (!pinned && !rowHov) {
         return;
      }
      int sx = cb.x + cb.rowWidth - 45;
      int sy = cb.y + (cb.rowHeight - 8) / 2;
      boolean starHov = mlx >= sx - 2 && mlx <= sx + 10 && mly >= sy - 2 && mly <= sy + 10;
      int color = pinned ? 0xFFFFD24A : (starHov ? 0xFFFFFFFF : 0x66FFFFFF);
      ctx.method_51433(this.field_22793, pinned ? "★" : "☆", sx, sy, color, false);
   }

   private boolean pinStarHit(TurtUICheckbox cb, double mx, double gy) {
      int sx = cb.x + cb.rowWidth - 45;
      int sy = cb.y + (cb.rowHeight - 8) / 2;
      return mx >= sx - 2 && mx <= sx + 10 && gy >= sy - 2 && gy <= sy + 10;
   }

   private void togglePin(String label) {
      TurtModConfig cfg = TurtModClient.getConfig();
      if (cfg.misc.pinnedModules == null) {
         cfg.misc.pinnedModules = new ArrayList<>();
      }
      if (cfg.misc.pinnedModules.contains(label)) {
         cfg.misc.pinnedModules.remove(label);
      } else {
         cfg.misc.pinnedModules.add(label);
      }
      ConfigManager.save(cfg);
      TurtSounds.tick();
      this.rebuildModuleList();
   }

   private void drawScrollbar(class_332 context) {
      int max = this.maxScroll();
      if (max <= 0) {
         return;
      }
      int viewportH = this.listBottom - this.listTop;
      int trackX = this.listLeft + this.listW - 2;
      int thumbH = Math.max(16, Math.round((float)viewportH * viewportH / (float)this.gridContentHeight));
      int thumbY = this.listTop + Math.round((this.scrollY / max) * (viewportH - thumbH));
      TurtUIUtils.drawRoundedRect(context, trackX, this.listTop, 3, this.listBottom - this.listTop, 1, new Color(0x22FFFFFF, true));
      TurtUIUtils.drawRoundedRect(context, trackX, thumbY, 3, thumbH, 1, ACCENT_GREEN);
   }

   private void drawSearchBar(class_332 context, int mouseX, int mouseY) {
      if (this.moduleSearchField != null) {
         int x = this.moduleSearchField.method_46426();
         int y = this.moduleSearchField.method_46427();
         int w = this.moduleSearchField.method_25368();
         int h = this.moduleSearchField.method_25364();
         
         boolean hov = TurtUIUtils.isHovered(mouseX, mouseY, x, y, w, h);
         long now = System.nanoTime();
         float dt = Math.min((now - this.searchFocusNs) / 1_000_000_000f, 0.1f);
         this.searchFocusNs = now;
         this.searchFocusAnim = TurtUIUtils.lerp01(this.searchFocusAnim, this.searchFieldFocused ? 1f : 0f, dt, 14f);

         TurtUIUtils.drawRoundedRect(context, x, y, w, h, 4, SEARCH_BG);
         TurtUIUtils.drawRoundedBorder(context, x, y, w, h, 4, this.searchFieldFocused ? ACCENT_PINK : (hov ? ACCENT_GREEN : SEARCH_BORDER));
         // Lunar-style accent underline that grows from the centre when focused.
         float fe = TurtUIUtils.ease(this.searchFocusAnim);
         if (fe > 0.01f) {
            int uw = (int)((w - 6) * fe);
            int ux = x + w / 2 - uw / 2;
            context.method_25294(ux, y + h - 1, ux + uw, y + h, ACCENT_PINK.getRGB());
         }

         context.method_25303(this.field_22793, "\ud83d\udd0d ", x + 6, y + 6, -7487905);
         String text = this.moduleSearchField.method_1882();
         if (text.isEmpty() && !this.searchFieldFocused) {
            context.method_51433(this.field_22793, "Search modules...", x + 16, y + 6, -2004318072, false);
         } else {
            context.method_25303(this.field_22793, text, x + 16, y + 6, -1);
         }

         if (this.searchFieldFocused) {
            int cursorX = x + 16 + this.field_22793.method_1727(text);
            if (System.currentTimeMillis() / 500L % 2L == 0L) {
               context.method_25294(cursorX, y + 2, cursorX + 1, y + 14, -1);
            }
         }
      }
   }

   public boolean method_25400(class_11905 input) {
      char chr = (char)input.comp_4793();
      if (this.moduleSearchField == null) {
         return super.method_25400(input);
      }
      // Auto-focus the search the moment the user starts typing (Steam/Discord-style); '/' focuses empty.
      if (!this.searchFieldFocused) {
         if (chr == '/') {
            this.searchFieldFocused = true;
            return true;
         }
         if (Character.isLetterOrDigit(chr)) {
            this.searchFieldFocused = true;
            this.moduleSearchField.method_1852(this.moduleSearchField.method_1882() + chr);
            this.rebuildModuleList();
            return true;
         }
         return super.method_25400(input);
      }
      if (Character.isLetterOrDigit(chr) || chr == ' ' || chr == '_' || chr == '-') {
         this.moduleSearchField.method_1852(this.moduleSearchField.method_1882() + chr);
         this.rebuildModuleList();
         return true;
      }
      return super.method_25400(input);
   }

   public boolean method_25404(class_11908 input) {
      int keyCode = input.comp_4795();
      if (this.searchFieldFocused && this.moduleSearchField != null) {
         if (keyCode == 259) {
            String t = this.moduleSearchField.method_1882();
            if (!t.isEmpty()) {
               this.moduleSearchField.method_1852(t.substring(0, t.length() - 1));
               this.rebuildModuleList();
            }

            return true;
         }

         if (keyCode == 261) {
            this.moduleSearchField.method_1852("");
            this.rebuildModuleList();
            return true;
         }

         if (keyCode == 256) {
            this.searchFieldFocused = false;
            return true;
         }
      }

      return super.method_25404(input);
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      int button = click.method_74245();
      // Convert the screen click into the panel's logical coordinate space.
      double mouseX = this.uiScale.toLogicalX(click.comp_4798());
      double mouseY = this.uiScale.toLogicalY(click.comp_4799());

      if (button == 0 && this.moduleSearchField != null) {
         int x = this.moduleSearchField.method_46426();
         int y = this.moduleSearchField.method_46427();
         int w = this.moduleSearchField.method_25368();
         int h = this.moduleSearchField.method_25364();
         if (mouseX >= (double)x && mouseX <= (double)(x + w) && mouseY >= (double)y && mouseY <= (double)(y + h)) {
            this.searchFieldFocused = true;
            return true;
         }

         this.searchFieldFocused = false;
      }

      // Sidebar tab clicks
      if (button == 0) {
         Tab[] tabs = TurtModClientConfigScreen.Tab.values();
         for (int i = 0; i < tabs.length; i++) {
            if (TurtLauncher.navItemHovered(this.mainPanel.x, this.mainPanel.y, i, (int)mouseX, (int)mouseY)) {
               if (this.activeTab != tabs[i]) {
                  this.tabFade = 0f;   // trigger slide-in
                  this.scrollY = 0f;   // reset scroll for the new tab
                  TurtSounds.tab();
               }
               this.activeTab = tabs[i];
               this.rebuildModuleList();
               return true;
            }
         }
      }

      for(TurtUIButton btn : this.controlButtons) {
         if (btn.mouseClicked(mouseX, mouseY, button)) {
            return true;
         }
      }

      // Module checkboxes (clipped + scrolled). Only react inside the grid viewport; account for the
      // scroll offset by testing against the unscrolled position (mouseY + scrollY).
      if (mouseY >= this.listTop && mouseY <= this.listBottom) {
         double gy = mouseY + this.scrollY;
         for (TurtUICheckbox cb : this.checkboxes) {
            if (cb.isHoveredPublic(mouseX, gy)) {
               // Star hotspot toggles pin without toggling the module.
               if (button == 0 && this.pinStarHit(cb, mouseX, gy)) {
                  this.togglePin(cb.label);
                  return true;
               }
               if (button == 1) {
                  TurtModConfigScreenFactory.ModuleKind kind = this.getModuleKind(cb.label);
                  if (kind != null) {
                     this.field_22787.method_1507(TurtModConfigScreenFactory.createForModule(this, kind));
                  }
                  return true;
               }
               if (button == 0 && cb.mouseClicked(mouseX, gy, 0)) {
                  // Toast the module's new on/off state (skip rows that aren't real toggles).
                  if (!"Theme Settings".equals(cb.label) && !"Kit Loader".equals(cb.label)) {
                     com.turtmod.hud.ModuleToastFeature.notify(cb.label, cb.checked);
                  }
                  return true;
               }
            }
         }
      }

      return super.method_25402(click, bl);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      double lx = this.uiScale.toLogicalX(mouseX);
      double ly = this.uiScale.toLogicalY(mouseY);
      if (this.maxScroll() > 0 && lx >= this.listLeft && lx <= this.listLeft + this.listW
            && ly >= this.listTop && ly <= this.listBottom) {
         this.scrollY -= (float)verticalAmount * 18f;
         this.clampScroll();
         return true;
      }
      return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   private TurtModConfigScreenFactory.ModuleKind getModuleKind(String label) {
      TurtModConfigScreenFactory.ModuleKind var10000;
      switch (label) {
         case "Fullbright":
            var10000 = TurtModConfigScreenFactory.ModuleKind.FULLBRIGHT;
            break;
         case "Freelook":
            var10000 = TurtModConfigScreenFactory.ModuleKind.FREELOOK;
            break;
         case "Hit Color":
            var10000 = TurtModConfigScreenFactory.ModuleKind.HIT_COLOR;
            break;
         case "Low Fire":
            var10000 = TurtModConfigScreenFactory.ModuleKind.LOW_FIRE;
            break;
         case "Shield Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.LOW_SHIELD;
            break;
         case "Fog Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.FOG_CONTROLS;
            break;
         case "Overlays":
            var10000 = TurtModConfigScreenFactory.ModuleKind.OVERLAYS;
            break;
         case "Hurt Cam":
            var10000 = TurtModConfigScreenFactory.ModuleKind.CAMERA_SETTINGS;
            break;
         case "Block Outline":
            var10000 = TurtModConfigScreenFactory.ModuleKind.BLOCK_OUTLINE;
            break;
         case "Totem Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.SMALL_TOTEM;
            break;
         case "Discord RPC":
            var10000 = TurtModConfigScreenFactory.ModuleKind.DISCORD_RPC;
            break;
         case "Armor HUD":
            var10000 = TurtModConfigScreenFactory.ModuleKind.ARMOR_HUD;
            break;
         case "Potion HUD":
            var10000 = TurtModConfigScreenFactory.ModuleKind.POTION_HUD;
            break;
         case "FPS/Ping":
            var10000 = TurtModConfigScreenFactory.ModuleKind.FPS_PING;
            break;
         case "Reach Display":
            var10000 = TurtModConfigScreenFactory.ModuleKind.REACH;
            break;
         case "Keystrokes":
            var10000 = TurtModConfigScreenFactory.ModuleKind.KEYSTROKES;
            break;
         case "CPS Counter":
            var10000 = TurtModConfigScreenFactory.ModuleKind.CPS_COUNTER;
            break;
         case "Sprint Display":
            var10000 = TurtModConfigScreenFactory.ModuleKind.SPRINT_HUD;
            break;
         case "Inventory HUD":
            var10000 = TurtModConfigScreenFactory.ModuleKind.INVENTORY_HUD;
            break;
         case "Coordinates":
            var10000 = TurtModConfigScreenFactory.ModuleKind.COORDINATES_HUD;
            break;
         case "Ping Display":
            var10000 = TurtModConfigScreenFactory.ModuleKind.PING_DISPLAY;
            break;
         case "Hitboxes":
            var10000 = TurtModConfigScreenFactory.ModuleKind.CUSTOM_HITBOXES;
            break;
         case "Scoreboard Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.SCOREBOARD;
            break;
         case "Screenshot Tools":
            var10000 = TurtModConfigScreenFactory.ModuleKind.BETTER_SCREENSHOT;
            break;
         case "Clean F3":
            var10000 = TurtModConfigScreenFactory.ModuleKind.CLEAN_F3;
            break;
         case "Health Indicator":
            var10000 = TurtModConfigScreenFactory.ModuleKind.HEALTH_INDICATOR;
            break;
         case "Held Item Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.HELD_ITEM;
            break;
         case "Theme Settings":
            var10000 = TurtModConfigScreenFactory.ModuleKind.THEME_SETTINGS;
            break;
         case "Zoom":
            var10000 = TurtModConfigScreenFactory.ModuleKind.ZOOM;
            break;
         case "Elytra Pitch HUD":
            var10000 = TurtModConfigScreenFactory.ModuleKind.ELYTRA_HUD;
            break;
         case "Own Nametag":
            var10000 = TurtModConfigScreenFactory.ModuleKind.OWN_NAMETAG;
            break;
         case "Death Coords":
            var10000 = TurtModConfigScreenFactory.ModuleKind.DEATH_COORDS;
            break;
         case "Mute Sounds":
            var10000 = TurtModConfigScreenFactory.ModuleKind.MUTE_SOUNDS;
            break;
         case "Hide Particles":
            var10000 = TurtModConfigScreenFactory.ModuleKind.HIDE_PARTICLES;
            break;
         case "Clear View":
            var10000 = TurtModConfigScreenFactory.ModuleKind.CLEAR_VIEW;
            break;
         case "Title Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.TITLE_TWEAKS;
            break;
         case "Bossbar Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.BOSSBAR_TWEAKS;
            break;
         case "Scrollable Tooltips":
            var10000 = TurtModConfigScreenFactory.ModuleKind.SCROLLABLE_TOOLTIPS;
            break;
         case "Chat Tweaks":
            var10000 = TurtModConfigScreenFactory.ModuleKind.CHAT_TWEAKS;
            break;
         case "Command Keys":
            var10000 = TurtModConfigScreenFactory.ModuleKind.COMMAND_KEYS;
            break;
         case "Kit Loader":
            var10000 = TurtModConfigScreenFactory.ModuleKind.KIT_LOADER;
            break;
         case "Gamemode Switcher":
            var10000 = TurtModConfigScreenFactory.ModuleKind.GAMEMODE_SWITCHER;
            break;
         case "Module Notifications":
            var10000 = TurtModConfigScreenFactory.ModuleKind.MODULE_TOASTS;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public void method_25419() {
      ConfigManager.save(TurtModClient.getConfig());
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }

   }

   private static enum Tab {
      VISUALS,
      HUD,
      UTILITY,
      MISC;

      // $FF: synthetic method
      private static Tab[] $values() {
         return new Tab[]{VISUALS, HUD, UTILITY, MISC};
      }
   }

   private static record ModuleOption(String displayName, Supplier<Boolean> getEnabled, Consumer<Boolean> setEnabled) {
   }
}
