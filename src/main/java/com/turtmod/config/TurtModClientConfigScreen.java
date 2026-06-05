package com.turtmod.config;

import com.turtmod.TurtModClient;
import com.turtmod.hud.HudEditorScreen;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUICheckbox;
import com.turtmod.ui.TurtUILabel;
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
import net.minecraft.class_1109;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3417;
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
   private float openFade = 0f;
   private float tabFade = 1f;     // eases 0→1 on tab switch for a slide-in
   private long lastFrameNs = System.nanoTime();

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
         () -> this.field_22787.method_1507(TurtModWalksyConfigScreenFactory.create(this))));
      this.controlButtons.add(new TurtUIButton(contentX + (btnW + btnGap), btnZoneTop, btnW, btnH, "HUD Editor", btnTheme,
         () -> this.field_22787.method_1507(new HudEditorScreen(this))));
      this.controlButtons.add(new TurtUIButton(contentX + (btnW + btnGap) * 2, btnZoneTop, btnW, btnH, "Reset All", btnTheme, () -> {
         ConfigManager.reset();
         TurtModClient.reloadConfig();
         this.rebuildModuleList();
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

      for (int i = 0; i < options.size(); i++) {
         ModuleOption option = options.get(i);
         int col = i % cols;
         int row = i / cols;
         int cx = this.listLeft + col * (colWidth + colGap);
         int cy = this.listTop + row * rowHeight;
         TurtUICheckbox cb = new TurtUICheckbox(cx, cy, 12, 6, this.field_22793, option.displayName, theme, true,
            (Boolean)option.getEnabled.get(), (v) -> option.setEnabled.accept(v));
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
         case 1 -> var10000 = List.of(new ModuleOption("Armor HUD", () -> cfg.hud.movableArmorHud, (v) -> cfg.hud.movableArmorHud = v), new ModuleOption("Potion HUD", () -> cfg.hud.movablePotionHud, (v) -> cfg.hud.movablePotionHud = v), new ModuleOption("FPS/Ping", () -> cfg.hud.minimalFpsPingOverlay, (v) -> cfg.hud.minimalFpsPingOverlay = v), new ModuleOption("Inventory HUD", () -> cfg.hud.inventoryHudEnabled, (v) -> cfg.hud.inventoryHudEnabled = v), new ModuleOption("Keystrokes", () -> cfg.hud.keystrokesHud, (v) -> cfg.hud.keystrokesHud = v), new ModuleOption("CPS Counter", () -> cfg.hud.cpsCounterHud, (v) -> cfg.hud.cpsCounterHud = v), new ModuleOption("Elytra Pitch HUD", () -> cfg.visual.elytraPitchHud, (v) -> cfg.visual.elytraPitchHud = v), new ModuleOption("Coordinates", () -> cfg.hud.coordinatesHud, (v) -> cfg.hud.coordinatesHud = v), new ModuleOption("Health Indicator", () -> cfg.combat.playerHealthIndicator, (v) -> cfg.combat.playerHealthIndicator = v), new ModuleOption("Reach Display", () -> cfg.hud.reachDisplay, (v) -> cfg.hud.reachDisplay = v));
         // 🛠 Utility (7)
         case 2 -> var10000 = List.of(new ModuleOption("Freelook", () -> cfg.visual.freelookEnabled, (v) -> cfg.visual.freelookEnabled = v), new ModuleOption("Hide Scoreboard", () -> cfg.visual.hideScoreboard, (v) -> cfg.visual.hideScoreboard = v), new ModuleOption("Shield Tweaks", () -> cfg.visual.shieldStatusRecolor, (v) -> cfg.visual.shieldStatusRecolor = v), new ModuleOption("Held Item Tweaks", () -> cfg.visual.heldItemTweaksEnabled, (v) -> cfg.visual.heldItemTweaksEnabled = v), new ModuleOption("Zoom", () -> cfg.visual.zoomEnabled, (v) -> cfg.visual.zoomEnabled = v), new ModuleOption("Clean F3", () -> cfg.hud.cleanF3Mode, (v) -> cfg.hud.cleanF3Mode = v), new ModuleOption("Screenshot Tools", () -> cfg.hud.betterScreenshotActions, (v) -> cfg.hud.betterScreenshotActions = v));
         // 📦 Misc (3)
         case 3 -> var10000 = List.of(new ModuleOption("Theme Settings", () -> true, (v) -> {
}), new ModuleOption("Enable TurtMod", () -> cfg.misc.enabled, (v) -> cfg.misc.enabled = v), new ModuleOption("Discord RPC", () -> cfg.misc.discordRpc.enabled, (v) -> cfg.misc.discordRpc.enabled = v));
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f);
      this.lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, dt, 12f);
      this.tabFade = TurtUIUtils.lerp01(this.tabFade, 1f, dt, 14f);

      TurtUIUtils.drawMenuBackdrop(context, this.field_22789, this.field_22790);

      // Fit the fixed logical layout to the real screen (any resolution / GUI scale).
      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      // Mouse in logical coordinates so hover/hit-tests line up under the transform.
      int mlx = (int)this.uiScale.toLogicalX(mouseX);
      int mly = (int)this.uiScale.toLogicalY(mouseY);

      this.uiScale.push(context);
      // subtle slide-up on open
      context.method_51448().pushMatrix();
      context.method_51448().translate(0f, (1f - this.openFade) * 7f);

      int px = this.mainPanel.x, py = this.mainPanel.y, pw = this.mainPanel.width, ph = this.mainPanel.height;
      TurtLauncher.drawChrome(context, this.field_22793, px, py, pw, ph, "SETTINGS", null, null);

      // Sidebar: tab buttons as nav items
      Tab[] tabs = TurtModClientConfigScreen.Tab.values();
      for (int i = 0; i < tabs.length; i++) {
         boolean active = this.activeTab == tabs[i];
         boolean hov = TurtLauncher.navItemHovered(px, py, i, mlx, mly);
         String name = tabs[i] == Tab.HUD ? "HUD" : tabs[i].name().charAt(0) + tabs[i].name().substring(1).toLowerCase();
         TurtLauncher.drawNavItem(context, this.field_22793, px, py, i, name, active, hov);
      }

      // Search bar inside content area
      this.drawSearchBar(context, mlx, mly);

      // Module grid — scissor-clipped to its viewport and scrolled vertically. (Scissor coords are
      // logical here; class_332 applies the current matrix, so they map to the right screen pixels.)
      context.method_44379(this.listLeft - 2, this.listTop - 2, this.listLeft + this.listW + 2, this.listBottom + 2);
      context.method_51448().pushMatrix();
      context.method_51448().translate((1f - this.tabFade) * 14f, -this.scrollY);
      for (TurtUICheckbox cb : this.checkboxes) {
         cb.render(context, mlx, mly + Math.round(this.scrollY));
      }
      context.method_51448().popMatrix();
      context.method_44380();

      // Scrollbar (only when the grid overflows its viewport).
      this.drawScrollbar(context);

      // Bottom control buttons (already positioned in method_25426)
      for (TurtUIButton btn : this.controlButtons) btn.render(context, mlx, mly, this.field_22793);

      context.method_51448().popMatrix();
      this.uiScale.pop(context);
      super.method_25394(context, mouseX, mouseY, delta);
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
      context.method_25294(trackX, this.listTop, trackX + 2, this.listBottom, 0x33FFFFFF);
      context.method_25294(trackX, thumbY, trackX + 2, thumbY + thumbH, ACCENT_GREEN.getRGB());
   }

   private void drawSearchBar(class_332 context, int mouseX, int mouseY) {
      if (this.moduleSearchField != null) {
         int x = this.moduleSearchField.method_46426();
         int y = this.moduleSearchField.method_46427();
         int w = this.moduleSearchField.method_25368();
         int h = this.moduleSearchField.method_25364();
         
         boolean hov = TurtUIUtils.isHovered(mouseX, mouseY, x, y, w, h);
         TurtUIUtils.drawRoundedRect(context, x, y, w, h, 4, SEARCH_BG);
         TurtUIUtils.drawBorder(context, x, y, w, h, this.searchFieldFocused ? ACCENT_PINK : (hov ? ACCENT_GREEN : SEARCH_BORDER));
         
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
      if (!this.searchFieldFocused || this.moduleSearchField == null || !Character.isLetterOrDigit(chr) && chr != ' ' && chr != '_' && chr != '-') {
         return super.method_25400(input);
      } else {
         class_342 var10000 = this.moduleSearchField;
         String var10001 = this.moduleSearchField.method_1882();
         var10000.method_1852(var10001 + chr);
         this.rebuildModuleList();
         return true;
      }
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
               if (button == 1) {
                  TurtModWalksyConfigScreenFactory.ModuleKind kind = this.getModuleKind(cb.label);
                  if (kind != null) {
                     this.field_22787.method_1507(TurtModWalksyConfigScreenFactory.createForModule(this, kind));
                  }
                  return true;
               }
               if (button == 0 && cb.mouseClicked(mouseX, gy, 0)) {
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

   private TurtModWalksyConfigScreenFactory.ModuleKind getModuleKind(String label) {
      TurtModWalksyConfigScreenFactory.ModuleKind var10000;
      switch (label) {
         case "Fullbright":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.FULLBRIGHT;
            break;
         case "Freelook":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.FREELOOK;
            break;
         case "Hit Color":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.HIT_COLOR;
            break;
         case "Low Fire":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.LOW_FIRE;
            break;
         case "Shield Tweaks":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.LOW_SHIELD;
            break;
         case "Fog Tweaks":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.FOG_CONTROLS;
            break;
         case "Overlays":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.OVERLAYS;
            break;
         case "Hurt Cam":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.CAMERA_SETTINGS;
            break;
         case "Block Outline":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.BLOCK_OUTLINE;
            break;
         case "Totem Tweaks":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.SMALL_TOTEM;
            break;
         case "Discord RPC":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.DISCORD_RPC;
            break;
         case "Armor HUD":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.ARMOR_HUD;
            break;
         case "Potion HUD":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.POTION_HUD;
            break;
         case "FPS/Ping":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.FPS_PING;
            break;
         case "Reach Display":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.REACH;
            break;
         case "Keystrokes":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.KEYSTROKES;
            break;
         case "CPS Counter":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.CPS_COUNTER;
            break;
         case "Inventory HUD":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.INVENTORY_HUD;
            break;
         case "Coordinates":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.COORDINATES_HUD;
            break;
         case "Hitboxes":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.CUSTOM_HITBOXES;
            break;
         case "Scoreboard Tweaks":
         case "Hide Scoreboard":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.SCOREBOARD;
            break;
         case "Screenshot Tools":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.BETTER_SCREENSHOT;
            break;
         case "Clean F3":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.CLEAN_F3;
            break;
         case "Health Indicator":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.HEALTH_INDICATOR;
            break;
         case "Held Item Tweaks":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.HELD_ITEM;
            break;
         case "Theme Settings":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.THEME_SETTINGS;
            break;
         case "Zoom":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.ZOOM;
            break;
         case "Elytra Pitch HUD":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.ELYTRA_HUD;
            break;
         case "Own Nametag":
            var10000 = TurtModWalksyConfigScreenFactory.ModuleKind.OWN_NAMETAG;
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

   private void playSound() {
      try {
         class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
      } catch (Exception var2) {
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
