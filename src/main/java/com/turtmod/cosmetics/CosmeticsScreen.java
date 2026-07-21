package com.turtmod.cosmetics;

import com.turtmod.cosmetics.skin.SkinLoader;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_10055;
import net.minecraft.class_10799;
import net.minecraft.class_11909;
import net.minecraft.class_12079;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7920;
import net.minecraft.class_8685;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The rebuilt Skin Changer, organised around <b>presets</b>. The gallery lists saved looks (each a skin +
 * model + cape); picking or creating one opens the editor, where you set the skin (fetch by IGN, upload a
 * PNG, or drag a .png in), toggle Slim/Classic, and choose one of the capes your account owns. Applying a
 * preset pushes the skin and cape to your real Mojang account.
 */
public class CosmeticsScreen extends class_437 {
   private enum View { GALLERY, EDIT, LIBRARY }

   private final class_437 parent;
   private View view = View.GALLERY;
   private List<SkinPreset> presets = new ArrayList<>();
   private int selected = -1;
   private SkinPreset editing = null;

   // Shared preview orbit (drag to rotate).
   private float previewYaw = 180f;
   private float previewPitch = 0f;
   private boolean draggingPreview = false;

   // Text fields (kept in logical space with everything else).
   private String ignInput = "";
   private boolean ignFocused = false;
   private int ignX, ignY, ignW, ignH;
   private String nameInput = "";
   private boolean nameFocused = false;
   private int nameX, nameY, nameW, nameH;

   // Capes for the editor.
   private List<CapeService.Cape> capes = new ArrayList<>();
   private boolean capesLoading = false;
   private final List<int[]> capeRects = new ArrayList<>();   // [x,y,w,h,index] in logical space

   private float galleryScroll = 0f;
   private final List<String> librarySkins = new ArrayList<>();   // absolute .png paths in the skins folder
   private float libraryScroll = 0f;

   // Preset search (GALLERY tab).
   private String presetSearch = "";
   private boolean searchFocused = false;
   private int searchX, searchY, searchW, searchH;
   private final List<Integer> presetFilter = new ArrayList<>();   // indices into `presets` matching the search
   private int modelTogX, modelTogY, modelTogW, modelTogH;        // segmented Classic|Slim toggle rect
   private String statusMessage = "";
   private int messageTicks = 0;
   private float openFade = 0f;
   private long lastFrameNs = System.nanoTime();

   private final List<TurtUIButton> buttons = new ArrayList<>();
   private static final int LOGICAL_W = 580;
   private static final int LOGICAL_H = 400;
   private final TurtUIScale uiScale = new TurtUIScale();
   private int panelX, panelY, panelW, panelH;
   private int contentX, contentY, contentW, contentH;

   private static final Color PANEL_BORDER = new Color(9289311, true);
   private static final Color ACCENT_GREEN = new Color(9289311, false);
   private static final Color ACCENT_PINK = new Color(16752046, false);
   private static final Color TEXT_MAIN = new Color(16775399, false);
   private static final Color BTN_BG = new Color(2433054, true);
   private static final Color BTN_HOVER = new Color(3482400, true);

   // Gallery grid metrics.
   private static final int CARD_W = 92;
   private static final int CARD_H = 118;
   private static final int CARD_GAP = 10;

   public CosmeticsScreen(class_437 parent) {
      super(class_2561.method_43470("TurtMod Skin Changer"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.presets = PresetStore.load();
      this.layoutPanels();
      this.rebuildButtons();
      loadCapes();   // so gallery cards and the editor preview can both show capes
   }

   private TurtUITheme theme() {
      return new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
   }

   private void layoutPanels() {
      this.panelW = LOGICAL_W;
      this.panelH = LOGICAL_H;
      this.panelX = 0;
      this.panelY = 0;
      this.contentX = TurtLauncher.contentX(this.panelX);
      this.contentY = TurtLauncher.contentY(this.panelY);
      this.contentW = TurtLauncher.contentW(this.panelW);
      this.contentH = TurtLauncher.contentH(this.panelH);
   }

   private void rebuildButtons() {
      this.buttons.clear();
      TurtUITheme t = this.theme();
      int sx = this.panelX + 6;
      int sw = TurtLauncher.SIDEBAR_W - 12;
      int sy = this.panelY + TurtLauncher.HEADER_H + 8;
      int gap = 4, bh = 18, group = 10;
      int backY = this.panelY + this.panelH - TurtLauncher.FOOTER_H - bh - 6;

      if (this.view == View.GALLERY) {
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "New Preset", t, this::newPreset)); sy += bh + group;
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Edit", t, () -> { if (hasSel()) openEditor(this.presets.get(this.selected)); else setStatus("Pick a preset first."); })); sy += bh + gap;
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Apply", t, this::applySelected)); sy += bh + gap;
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Delete", t, this::deleteSelected));
         this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back", t, this::method_25419));
      } else if (this.view == View.EDIT) {
         // Sidebar: save/apply both return to the gallery; model toggle is a segmented control under the preview.
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Save & Close", t, this::backToGallery)); sy += bh + gap;
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Apply Now", t, () -> { saveEditing(); applyPreset(this.editing); backToGallery(); })); sy += bh + group;
         this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back to Presets", t, this::backToGallery));

         // Content-area skin sources: IGN field + Fetch, then Upload PNG / Skins Library.
         int col = this.contentX;
         int colW = this.contentW * 52 / 100;
         int cy = this.contentY + 44;
         this.ignX = col; this.ignY = cy; this.ignW = colW - 60; this.ignH = bh;
         this.buttons.add(new TurtUIButton(col + colW - 56, cy, 56, bh, "Fetch", t, () -> fetchByName(this.ignInput))); cy += bh + gap;
         int halfW = (colW - gap) / 2;
         this.buttons.add(new TurtUIButton(col, cy, halfW, bh, "Upload PNG", t, this::importSkinFile));
         this.buttons.add(new TurtUIButton(col + halfW + gap, cy, colW - halfW - gap, bh, "Skins Library", t, this::openLibrary));
      } else { // LIBRARY — pull skins in from anywhere: upload, drag, or the folder itself.
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Upload PNG", t, this::importSkinToLibrary)); sy += bh + gap;
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Open Folder", t, this::openSkinsFolder)); sy += bh + gap;
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Refresh", t, this::loadAvailableSkins));
         this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back", t, () -> { this.view = View.EDIT; this.rebuildButtons(); }));
      }
   }

   private boolean hasSel() { return this.selected >= 0 && this.selected < this.presets.size(); }

   private void newPreset() {
      SkinPreset p = new SkinPreset("Preset " + (this.presets.size() + 1));
      this.presets.add(p);
      this.selected = this.presets.size() - 1;
      PresetStore.save(this.presets);
      openEditor(p);
   }

   private void openEditor(SkinPreset p) {
      this.editing = p;
      this.nameInput = p.name == null ? "" : p.name;
      this.ignInput = "";
      this.ignFocused = false;
      this.nameFocused = false;
      this.previewYaw = 180f;
      this.previewPitch = 0f;
      this.view = View.EDIT;
      this.rebuildButtons();
      loadCapes();
   }

   private void backToGallery() {
      saveEditing();
      this.view = View.GALLERY;
      this.editing = null;
      this.rebuildButtons();
   }

   private void saveEditing() {
      if (this.editing != null) {
         this.editing.name = this.nameInput.isBlank() ? this.editing.name : this.nameInput.trim();
         PresetStore.save(this.presets);
      }
   }

   private void toggleModel() {
      if (this.editing != null) {
         this.editing.slim = !this.editing.slim;
         setStatus("Model: " + (this.editing.slim ? "Slim" : "Classic"));
         this.rebuildButtons();
      }
   }

   private void deleteSelected() {
      if (!hasSel()) { setStatus("Pick a preset first."); return; }
      this.presets.remove(this.selected);
      this.selected = -1;
      PresetStore.save(this.presets);
      setStatus("Preset deleted.");
   }

   private void applySelected() {
      if (!hasSel()) { setStatus("Pick a preset first."); return; }
      applyPreset(this.presets.get(this.selected));
   }

   /** Push a preset's skin + cape to the real Mojang account. */
   private void applyPreset(SkinPreset p) {
      if (p == null) { return; }
      if (p.skinFile == null && p.capeId == null) { setStatus("This preset is empty."); return; }
      setStatus("Applying " + p.name + "...");
      final String skin = p.skinFile;
      final boolean slim = p.slim;
      final String capeId = p.capeId;
      CompletableFuture.runAsync(() -> {
         StringBuilder msg = new StringBuilder();
         if (skin != null) {
            CosmeticManager.UploadResult r = CosmeticManager.uploadSkinToMinecraftProfile(skin, slim);
            msg.append(r.success() ? "Skin set" : "Skin failed");
         }
         if (capeId != null) {
            boolean ok = CapeService.setActive(capeId);
            if (msg.length() > 0) msg.append(" · ");
            msg.append(ok ? "Cape set" : "Cape failed");
         }
         msg.append(" · rejoin to see it");
         this.field_22787.execute(() -> setStatus(msg.toString()));
      });
   }

   // ── Skin sources ────────────────────────────────────────────────────────────────────────────────

   private void fetchByName(String name) {
      if (name == null || name.trim().isEmpty()) { setStatus("Type a username first."); return; }
      String clean = name.trim();
      setStatus("Looking up " + clean + "...");
      new Thread(() -> {
         byte[] png = SkinFetcher.fetchSkin(clean);
         this.field_22787.execute(() -> {
            if (png == null) { setStatus("Could not fetch that player's skin."); return; }
            try {
               Path dir = CosmeticManager.getSkinsDirectory();
               Files.createDirectories(dir);
               Path dest = dir.resolve(clean + ".png");
               Files.write(dest, png);
               if (this.editing != null) { this.editing.skinFile = dest.toAbsolutePath().toString(); saveEditing(); }
               setStatus("Loaded " + clean + "'s skin.");
            } catch (Exception e) { setStatus("Could not save the fetched skin."); }
         });
      }, "turtmod-skin-fetch").start();
   }

   private void importSkinFile() {
      CompletableFuture.runAsync(() -> {
         String picked = null;
         try {
            org.lwjgl.PointerBuffer filters = org.lwjgl.system.MemoryUtil.memAllocPointer(1);
            java.nio.ByteBuffer png = org.lwjgl.system.MemoryUtil.memUTF8("*.png");
            filters.put(png); filters.flip();
            picked = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog("Select a skin PNG", "", filters, "PNG image (*.png)", false);
            org.lwjgl.system.MemoryUtil.memFree(filters);
            org.lwjgl.system.MemoryUtil.memFree(png);
         } catch (Throwable tt) {
            this.field_22787.execute(() -> setStatus("File picker unavailable — try dragging the PNG in."));
            return;
         }
         if (picked == null) { return; }
         final String path = picked;
         this.field_22787.execute(() -> importSkinPath(java.nio.file.Paths.get(path)));
      });
   }

   /** Copy a .png into the skins folder and assign it to the preset being edited. */
   private void importSkinPath(Path src) {
      try {
         String name = src.getFileName().toString();
         if (!name.toLowerCase().endsWith(".png")) { setStatus("Not a PNG file."); return; }
         Path dest = CosmeticManager.getSkinsDirectory().resolve(name);
         Files.createDirectories(dest.getParent());
         Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
         if (this.editing != null) { this.editing.skinFile = dest.toAbsolutePath().toString(); saveEditing(); }
         if (this.view == View.LIBRARY) loadAvailableSkins();   // show the freshly-added skin immediately
         setStatus("Imported " + name);
      } catch (Exception e) { setStatus("Import failed: " + e.getMessage()); }
   }

   /** Upload a PNG straight into the library (same picker as the editor, then refresh the grid). */
   private void importSkinToLibrary() {
      importSkinFile();
   }

   /** Open the skins folder in the OS file browser so you can drop / manage skins by hand. */
   private void openSkinsFolder() {
      try {
         Path dir = CosmeticManager.getSkinsDirectory();
         Files.createDirectories(dir);
         class_156.method_668().method_672(dir.toFile());
      } catch (Exception e) { setStatus("Could not open the skins folder."); }
   }

   /** Drag & drop: any dropped .png is imported (assigned to the preset, and shown in the library). */
   public void method_29638(List<Path> files) {
      if ((this.view == View.EDIT || this.view == View.LIBRARY) && files != null) {
         for (Path p : files) {
            if (p.getFileName().toString().toLowerCase().endsWith(".png")) { importSkinPath(p); break; }
         }
      }
   }

   private void openLibrary() {
      loadAvailableSkins();
      this.view = View.LIBRARY;
      this.libraryScroll = 0f;
      this.rebuildButtons();
   }

   /** All .png files currently in the skins folder (previously fetched / uploaded skins). */
   private void loadAvailableSkins() {
      this.librarySkins.clear();
      Path dir = CosmeticManager.getSkinsDirectory();
      try {
         Files.createDirectories(dir);
         java.io.File[] files = dir.toFile().listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
         if (files != null) {
            java.util.Arrays.sort(files, java.util.Comparator.comparing(java.io.File::getName));
            for (java.io.File f : files) this.librarySkins.add(f.getAbsolutePath());
         }
      } catch (Exception ignored) {
      }
   }

   private void loadCapes() {
      this.capesLoading = true;
      new Thread(() -> {
         List<CapeService.Cape> owned = CapeService.fetchOwnedCapes();
         this.field_22787.execute(() -> {
            this.capesLoading = false;
            this.capes = owned != null ? owned : new ArrayList<>();
         });
      }, "turtmod-cape-list").start();
   }

   private void setStatus(String msg) { this.statusMessage = msg; this.messageTicks = 100; }

   private class_2960 getSkinTexture(String path) {
      return path == null ? null : SkinLoader.getTexture(java.nio.file.Paths.get(path));
   }

   // ── Render ──────────────────────────────────────────────────────────────────────────────────────

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastFrameNs) / 1_000_000_000f, 0.1f);
      lastFrameNs = now;
      openFade = TurtUIUtils.lerp01(openFade, 1f, dt, 12f);

      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(ctx, mx, my);
      TurtUIUtils.update();

      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      mx = (int) this.uiScale.toLogicalX(mx);
      my = (int) this.uiScale.toLogicalY(my);
      this.uiScale.push(ctx);

      ctx.method_51448().pushMatrix();
      float introE = TurtUIUtils.ease(openFade);
      ctx.method_51448().translate(LOGICAL_W / 2f, LOGICAL_H / 2f + (1f - introE) * 12f);
      ctx.method_51448().scale(0.97f + 0.03f * introE, 0.97f + 0.03f * introE);
      ctx.method_51448().translate(-LOGICAL_W / 2f, -LOGICAL_H / 2f);

      String player = this.field_22787 != null && this.field_22787.method_1548() != null
         ? this.field_22787.method_1548().method_1676() : "Player";
      String chromeTitle = this.view == View.GALLERY ? "Skin Presets" : this.view == View.EDIT ? "Edit Preset" : "Skins Library";
      TurtLauncher.drawChrome(ctx, this.field_22793, this.panelX, this.panelY, this.panelW, this.panelH,
         chromeTitle, player, "Skins");

      if (this.view == View.GALLERY) {
         renderGallery(ctx, mx, my);
      } else if (this.view == View.EDIT) {
         renderEditor(ctx, mx, my);
      } else {
         renderLibrary(ctx, mx, my);
      }

      if (this.messageTicks > 0) {
         ctx.method_25300(this.field_22793, this.statusMessage, this.contentX + this.contentW / 2, this.contentY - 12, ACCENT_GREEN.getRGB());
         --this.messageTicks;
      }

      for (TurtUIButton btn : this.buttons) btn.render(ctx, mx, my, this.field_22793);

      ctx.method_51448().popMatrix();
      this.uiScale.pop(ctx);
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   private int galleryCols() {
      return Math.max(1, (this.contentW + CARD_GAP) / (CARD_W + CARD_GAP));
   }

   /** Rebuild the list of preset indices matching the search box (all of them when the box is empty). */
   private void rebuildFilter() {
      this.presetFilter.clear();
      String q = this.presetSearch.trim().toLowerCase();
      for (int i = 0; i < this.presets.size(); i++) {
         String n = this.presets.get(i).name == null ? "" : this.presets.get(i).name.toLowerCase();
         if (q.isEmpty() || n.contains(q)) this.presetFilter.add(i);
      }
   }

   private void renderGallery(class_332 ctx, int mx, int my) {
      TurtLauncher.drawContentPanel(ctx, this.field_22793, this.contentX, this.contentY, this.contentW, this.contentH, "YOUR PRESETS");
      rebuildFilter();

      // Search bar across the top.
      this.searchX = this.contentX + 6; this.searchY = this.contentY + 20; this.searchW = this.contentW - 12; this.searchH = 16;
      drawField(ctx, this.searchX, this.searchY, this.searchW, this.searchH, this.presetSearch, this.searchFocused, "search presets...");

      int viewTop = this.contentY + 44, viewH = this.contentH - 50;

      if (this.presets.isEmpty()) {
         ctx.method_25300(this.field_22793, "No presets yet", this.contentX + this.contentW / 2, viewTop + viewH / 2 - 8, 0xFF888888);
         ctx.method_25300(this.field_22793, "Click 'New Preset'", this.contentX + this.contentW / 2, viewTop + viewH / 2 + 4, 0xFF666666);
         return;
      }
      if (this.presetFilter.isEmpty()) {
         ctx.method_25300(this.field_22793, "No presets match \"" + this.presetSearch + "\"", this.contentX + this.contentW / 2, viewTop + viewH / 2 - 4, 0xFF888888);
         return;
      }

      int cols = galleryCols();
      int usedW = cols * CARD_W + (cols - 1) * CARD_GAP;
      int startX = this.contentX + (this.contentW - usedW) / 2;

      ctx.method_44379(this.contentX + 1, viewTop, this.contentX + this.contentW - 1, viewTop + viewH);
      for (int f = 0; f < this.presetFilter.size(); f++) {
         int i = this.presetFilter.get(f);
         int col = f % cols, row = f / cols;
         int cardX = startX + col * (CARD_W + CARD_GAP);
         int cardY = viewTop + row * (CARD_H + CARD_GAP) - (int) this.galleryScroll;
         if (cardY + CARD_H <= viewTop || cardY >= viewTop + viewH) continue;
         renderPresetCard(ctx, this.presets.get(i), cardX, cardY, i == this.selected, mx, my);
      }
      ctx.method_44380();
   }

   private void renderLibrary(class_332 ctx, int mx, int my) {
      TurtLauncher.drawContentPanel(ctx, this.field_22793, this.contentX, this.contentY, this.contentW, this.contentH, "PICK A SAVED SKIN");
      int viewTop = this.contentY + 22, viewH = this.contentH - 28;
      if (this.librarySkins.isEmpty()) {
         ctx.method_25300(this.field_22793, "No saved skins yet", this.contentX + this.contentW / 2, viewTop + viewH / 2 - 8, 0xFF888888);
         ctx.method_25300(this.field_22793, "Fetch or upload one first", this.contentX + this.contentW / 2, viewTop + viewH / 2 + 4, 0xFF666666);
         return;
      }
      int cols = galleryCols();
      int usedW = cols * CARD_W + (cols - 1) * CARD_GAP;
      int startX = this.contentX + (this.contentW - usedW) / 2;
      ctx.method_44379(this.contentX + 1, viewTop, this.contentX + this.contentW - 1, viewTop + viewH);
      for (int i = 0; i < this.librarySkins.size(); i++) {
         int col = i % cols, row = i / cols;
         int cardX = startX + col * (CARD_W + CARD_GAP);
         int cardY = viewTop + row * (CARD_H + CARD_GAP) - (int) this.libraryScroll;
         if (cardY + CARD_H <= viewTop || cardY >= viewTop + viewH) continue;
         renderLibraryCard(ctx, this.librarySkins.get(i), cardX, cardY, mx, my);
      }
      ctx.method_44380();
   }

   private void renderLibraryCard(class_332 ctx, String path, int x, int y, int mx, int my) {
      boolean hovered = mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H;
      TurtUIUtils.drawRoundedRect(ctx, x, y, CARD_W, CARD_H, 5, new Color(hovered ? 0x33000000 : 0x1E000000, true));
      int stageX = x + 8, stageY = y + 6, stageW = CARD_W - 16, stageH = CARD_H - 24;
      TurtUIUtils.drawRoundedRect(ctx, stageX, stageY, stageW, stageH, 4, new Color(0, 0, 0, 130));
      TurtUIUtils.drawStage(ctx, stageX, stageY, stageW, stageH, ACCENT_GREEN);
      class_2960 tex = getSkinTexture(path);
      if (tex != null) {
         float rot = ((float) (System.currentTimeMillis() % 9000L) / 9000f * 360f);
         drawModel3D(ctx, tex, false, null, stageX, stageY, stageW, stageH, 180f + rot, 0f, 0f, 0f);
      }
      TurtUIUtils.drawRoundedBorder(ctx, stageX, stageY, stageW, stageH, 4, new Color(255, 255, 255, hovered ? 60 : 24));
      String name = new java.io.File(path).getName();
      if (name.toLowerCase().endsWith(".png")) name = name.substring(0, name.length() - 4);
      if (name.length() > 13) name = name.substring(0, 12) + "..";
      ctx.method_25300(this.field_22793, name, x + CARD_W / 2, y + CARD_H - 11, hovered ? ACCENT_GREEN.getRGB() : TEXT_MAIN.getRGB());
   }

   private void renderPresetCard(class_332 ctx, SkinPreset p, int x, int y, boolean selected, int mx, int my) {
      boolean hovered = mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H;
      int bg = selected ? 0x48000000 : (hovered ? 0x33000000 : 0x1E000000);
      TurtUIUtils.drawRoundedRect(ctx, x, y, CARD_W, CARD_H, 5, new Color(bg, true));

      // Body preview on a mini stage.
      int stageX = x + 8, stageY = y + 6, stageW = CARD_W - 16, stageH = CARD_H - 34;
      TurtUIUtils.drawRoundedRect(ctx, stageX, stageY, stageW, stageH, 4, new Color(0, 0, 0, 130));
      TurtUIUtils.drawStage(ctx, stageX, stageY, stageW, stageH, ACCENT_GREEN);
      class_2960 tex = getSkinTexture(p.skinFile);
      if (tex != null) {
         float rot = ((float) (System.currentTimeMillis() % 9000L) / 9000f * 360f);
         drawModel3D(ctx, tex, p.slim, capeTextureFor(p.capeId), stageX, stageY, stageW, stageH, 180f + rot, 0f, 0f, 0f);
      } else {
         ctx.method_25300(this.field_22793, "empty", stageX + stageW / 2, stageY + stageH / 2 - 4, 0xFF666666);
      }
      TurtUIUtils.drawRoundedBorder(ctx, stageX, stageY, stageW, stageH, 4, new Color(255, 255, 255, 24));

      String name = p.name == null ? "?" : p.name;
      if (name.length() > 13) name = name.substring(0, 12) + "..";
      ctx.method_25300(this.field_22793, name, x + CARD_W / 2, y + CARD_H - 22, selected ? ACCENT_GREEN.getRGB() : TEXT_MAIN.getRGB());
      String badge = (p.slim ? "Slim" : "Classic") + (p.capeId != null ? " · Cape" : "");
      ctx.method_25300(this.field_22793, badge, x + CARD_W / 2, y + CARD_H - 11, 0xFF7A8088);

      if (selected) {
         TurtUIUtils.drawRoundedBorder(ctx, x, y, CARD_W, CARD_H, 5, ACCENT_GREEN);
      }
   }

   private void renderEditor(class_332 ctx, int mx, int my) {
      // Left column: name + upload + capes. Right column: big preview.
      int colW = this.contentW * 52 / 100;
      int leftX = this.contentX;
      int rightX = this.contentX + colW + 10;
      int rightW = this.contentW - colW - 10;

      TurtLauncher.drawContentPanel(ctx, this.field_22793, leftX, this.contentY, colW, this.contentH, "SKIN & CAPE");
      TurtLauncher.drawContentPanel(ctx, this.field_22793, rightX, this.contentY, rightW, this.contentH, "PREVIEW");

      // Name field.
      this.nameX = leftX + 6; this.nameY = this.contentY + 22; this.nameW = colW - 12; this.nameH = 18;
      drawField(ctx, this.nameX, this.nameY, this.nameW, this.nameH, this.nameInput, this.nameFocused, "preset name...");

      // IGN field label (the Fetch button + field are TurtUIButtons/handled in rebuildButtons).
      drawField(ctx, this.ignX, this.ignY, this.ignW, this.ignH, this.ignInput, this.ignFocused, "player IGN...");

      // Model toggle (segmented Classic | Slim) — kept visible right above the capes.
      this.modelTogX = leftX + 6; this.modelTogY = this.contentY + 90; this.modelTogW = colW - 12; this.modelTogH = 18;
      drawModelToggle(ctx);

      // Drag hint.
      int hintY = this.contentY + 114;
      ctx.method_51433(this.field_22793, "…or drag a .png onto this window", leftX + 6, hintY, 0xFF7A8088, false);

      // Capes strip (wraps to show every cape you own).
      int capeTop = hintY + 14;
      ctx.method_51433(this.field_22793, "CAPES YOU OWN", leftX + 6, capeTop, ACCENT_GREEN.getRGB(), false);
      renderCapeStrip(ctx, leftX + 6, capeTop + 12, colW - 12, this.contentY + this.contentH - (capeTop + 12) - 6, mx, my);

      // Big preview.
      int cx = rightX + rightW / 2;
      int top = this.contentY + 26, bot = this.contentY + this.contentH - 14;
      int boxH = bot - top;
      int scale = Math.max(20, boxH / 3);
      int stageX = rightX + 2, stageY = this.contentY + 18, stageW = rightW - 4, stageH = this.contentH - 24;
      ctx.method_25296(stageX, stageY, stageX + stageW, stageY + stageH, 0x2AA8FFC0, 0x00101418);
      int feetY = bot - 2;
      int platRx = (int) (scale * 0.85f), platRy = Math.max(4, (int) (scale * 0.22f));
      drawFilledEllipse(ctx, cx, feetY + 3, platRx + 3, platRy + 2, 0x4D000000);
      drawFilledEllipse(ctx, cx, feetY, platRx, platRy, new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 45).getRGB());

      class_2960 skinId = this.editing != null ? getSkinTexture(this.editing.skinFile) : null;
      if (skinId != null) {
         try {
            drawModel3D(ctx, skinId, this.editing.slim, capeTextureFor(this.editing.capeId), cx - scale, top, scale * 2, boxH, this.previewYaw, 0f, 0f, this.previewPitch);
         } catch (Throwable e) {
            ctx.method_25300(this.field_22793, "preview error", cx, top + boxH / 2, 0xFFFF5555);
         }
      } else {
         ctx.method_25300(this.field_22793, "No skin yet", cx, top + boxH / 2 - 6, 0xFF888888);
         ctx.method_25300(this.field_22793, "fetch / upload / drag", cx, top + boxH / 2 + 6, 0xFF666666);
      }
      TurtUIUtils.drawRoundedBorder(ctx, stageX, stageY, stageW, stageH, 7, new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 70));
      ctx.method_25300(this.field_22793, this.editing != null && this.editing.slim ? "Slim Model" : "Classic Model", cx, bot + 2, 0xFF888888);
   }

   private void drawModelToggle(class_332 ctx) {
      boolean slim = this.editing != null && this.editing.slim;
      int x = this.modelTogX, y = this.modelTogY, w = this.modelTogW, h = this.modelTogH;
      int halfW = w / 2;
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 4, new Color(0, 0, 0, 120));
      int ax = slim ? x + halfW : x;
      int aw = slim ? w - halfW : halfW;
      TurtUIUtils.drawRoundedRect(ctx, ax, y, aw, h, 4, Palette.alpha(ACCENT_GREEN, 210));
      ctx.method_25300(this.field_22793, "Classic", x + halfW / 2, y + (h - 8) / 2, (!slim ? Palette.alpha(Palette.PANEL_BG, 255) : TEXT_MAIN).getRGB());
      ctx.method_25300(this.field_22793, "Slim", x + halfW + (w - halfW) / 2, y + (h - 8) / 2, (slim ? Palette.alpha(Palette.PANEL_BG, 255) : TEXT_MAIN).getRGB());
      TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 4, new Color(255, 255, 255, 24));
   }

   /** Wrapping grid of the "no cape" chip + every owned cape, so nothing is truncated. */
   private void renderCapeStrip(class_332 ctx, int x, int y, int w, int h, int mx, int my) {
      this.capeRects.clear();
      if (this.capesLoading) {
         ctx.method_51433(this.field_22793, "loading...", x, y + 4, 0xFF888888, false);
         return;
      }
      int chipW = 30, chipH = 42, gap = 5;
      int perRow = Math.max(1, (w + gap) / (chipW + gap));
      int total = this.capes.size() + 1;   // index 0 = "no cape"
      for (int idx = 0; idx < total; idx++) {
         int col = idx % perRow, row = idx / perRow;
         int cxp = x + col * (chipW + gap);
         int cyp = y + row * (chipH + gap);
         if (cyp + chipH > y + h) break;   // clip to the available height
         if (idx == 0) {
            boolean noneSel = this.editing != null && this.editing.capeId == null;
            drawCapeChip(ctx, cxp, cyp, chipW, chipH, null, noneSel, mx, my);
            this.capeRects.add(new int[]{cxp, cyp, chipW, chipH, -1});
         } else {
            CapeService.Cape c = this.capes.get(idx - 1);
            boolean sel = this.editing != null && c.id.equals(this.editing.capeId);
            drawCapeChip(ctx, cxp, cyp, chipW, chipH, c, sel, mx, my);
            this.capeRects.add(new int[]{cxp, cyp, chipW, chipH, idx - 1});
         }
      }
   }

   private void drawCapeChip(class_332 ctx, int x, int y, int w, int h, CapeService.Cape c, boolean sel, int mx, int my) {
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 3, new Color(0, 0, 0, 130));
      if (c == null) {
         ctx.method_51433(this.field_22793, "✕", x + w / 2 - 3, y + h / 2 - 4, 0xFFBBBBBB, false);
      } else {
         CapeTextureCache.Entry tex = c.url != null ? CapeTextureCache.get(c.url) : null;
         if (tex != null && tex.id != null) {
            float sx = tex.texW / 64f, sy = tex.texH / 32f;
            ctx.method_25293(class_10799.field_56883, tex.id, x + 3, y + 3, 1f * sx, 1f * sy,
               w - 6, h - 6, Math.max(1, (int) (10 * sx)), Math.max(1, (int) (16 * sy)), tex.texW, tex.texH, -1);
         }
      }
      TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 3, sel ? ACCENT_GREEN : new Color(255, 255, 255, 22));
   }

   private void drawField(class_332 ctx, int x, int y, int w, int h, String text, boolean focused, String placeholder) {
      ctx.method_25294(x, y, x + w, y + h, Palette.SEARCH_BG.getRGB());
      ctx.method_73198(x, y, w, h, (focused ? Palette.GREEN : Palette.SEARCH_BORDER).getRGB());
      boolean ph = text.isEmpty() && !focused;
      String shown = ph ? placeholder : text + (focused ? "_" : "");
      ctx.method_51433(this.field_22793, shown, x + 4, y + (h - 8) / 2, (ph ? Palette.TEXT_MUTED : Palette.TEXT).getRGB(), false);
   }

   // ── 3D model (unchanged framebuffer mapping; slim is now a parameter) ─────────────────────────────

   private void drawModel3D(class_332 ctx, class_2960 skinId, boolean slim, class_2960 capeId, int lx, int ly, int lw, int lh,
                            float bodyYaw, float headYaw, float headPitch, float tiltPitch) {
      if (skinId == null) return;
      class_8685 skinTextures = buildSkinTextures(skinId, slim, capeId);
      class_10055 state = new class_10055();
      state.field_53520 = skinTextures;
      state.field_53329 = 0.6f;
      state.field_53330 = 1.8f;
      state.field_61820 = 15728880;
      state.field_61821 = 0;
      state.field_61823.clear();
      state.field_53453 = 1.0f;
      state.field_53454 = 1.0f;
      state.field_53446 = bodyYaw;
      state.field_53447 = headYaw;
      state.field_53448 = headPitch;

      int cx = lx + lw / 2;
      int top = ly, bot = ly + lh;
      int scale = Math.max(6, lh / 3);
      float openOff = (1f - this.openFade) * 7f;
      int sx1 = this.uiScale.toScreenX(cx - scale);
      int sx2 = this.uiScale.toScreenX(cx + scale);
      int sy1 = Math.round(this.uiScale.offsetY + (top + openOff) * this.uiScale.scale);
      int sy2 = Math.round(this.uiScale.offsetY + (bot + openOff) * this.uiScale.scale);
      float sScale = scale * this.uiScale.scale;

      Quaternionf baseRot = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf orbit = null;
      if (tiltPitch != 0.0f) {
         Quaternionf pitch = new Quaternionf().rotateX(tiltPitch);
         baseRot.mul(pitch);
         orbit = new Quaternionf(pitch).conjugate();
      }
      Vector3f pos = new Vector3f(0.0f, state.field_53330 / 2.0f + 0.0625f, 0.0f);
      ctx.method_70856(state, sScale, pos, baseRot, orbit, sx1, sy1, sx2, sy2);
   }

   private class_8685 buildSkinTextures(class_2960 id, boolean slim, class_2960 capeId) {
      class_12079.class_12081 cape = capeId == null ? null : new class_12079.class_12080(capeId, "turtmod_cape");
      return new class_8685(new class_12079.class_12080(id, "turtmod_skin"), cape, null,
         slim ? class_7920.field_41122 : class_7920.field_41123, true);
   }

   /** The registered cape texture for a preset's capeId (looked up in the owned-capes list), or null. */
   private class_2960 capeTextureFor(String capeId) {
      if (capeId == null) return null;
      for (CapeService.Cape c : this.capes) {
         if (capeId.equals(c.id) && c.url != null) {
            return CapeTextureCache.get(c.url).id;
         }
      }
      return null;
   }

   private void drawFilledEllipse(class_332 ctx, int cx, int cy, int rx, int ry, int argb) {
      if (rx <= 0 || ry <= 0) return;
      for (int dy = -ry; dy <= ry; dy++) {
         double f = 1.0 - (double) (dy * dy) / (double) (ry * ry);
         if (f < 0) continue;
         int halfW = (int) (rx * Math.sqrt(f));
         ctx.method_25294(cx - halfW, cy + dy, cx + halfW, cy + dy + 1, argb);
      }
   }

   // ── Input ──────────────────────────────────────────────────────────────────────────────────────

   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = this.uiScale.toLogicalX(click.comp_4798()), my = this.uiScale.toLogicalY(click.comp_4799());
      int button = click.method_74245();

      if (this.view == View.EDIT) {
         this.nameFocused = mx >= this.nameX && mx <= this.nameX + this.nameW && my >= this.nameY && my <= this.nameY + this.nameH;
         this.ignFocused = mx >= this.ignX && mx <= this.ignX + this.ignW && my >= this.ignY && my <= this.ignY + this.ignH;
      }

      for (TurtUIButton btn : this.buttons) if (btn.mouseClicked(mx, my, button)) return true;

      if (this.view == View.GALLERY) {
         // Search-box focus.
         this.searchFocused = mx >= this.searchX && mx <= this.searchX + this.searchW && my >= this.searchY && my <= this.searchY + this.searchH;
         if (this.searchFocused) return true;
         int viewTop = this.contentY + 44, viewH = this.contentH - 50;
         if (!this.presetFilter.isEmpty() && mx >= this.contentX && mx <= this.contentX + this.contentW && my >= viewTop && my <= viewTop + viewH) {
            int cols = galleryCols();
            int usedW = cols * CARD_W + (cols - 1) * CARD_GAP;
            int startX = this.contentX + (this.contentW - usedW) / 2;
            for (int f = 0; f < this.presetFilter.size(); f++) {
               int i = this.presetFilter.get(f);
               int col = f % cols, row = f / cols;
               int cardX = startX + col * (CARD_W + CARD_GAP);
               int cardY = viewTop + row * (CARD_H + CARD_GAP) - (int) this.galleryScroll;
               if (mx >= cardX && mx <= cardX + CARD_W && my >= cardY && my <= cardY + CARD_H) {
                  this.selected = i;
                  setStatus("Selected: " + this.presets.get(i).name);
                  return true;
               }
            }
         }
      } else if (this.view == View.EDIT) {
         if (this.nameFocused || this.ignFocused) return true;
         // Model toggle (left half = Classic, right half = Slim).
         if (mx >= this.modelTogX && mx <= this.modelTogX + this.modelTogW && my >= this.modelTogY && my <= this.modelTogY + this.modelTogH) {
            if (this.editing != null) {
               this.editing.slim = mx >= this.modelTogX + this.modelTogW / 2;
               setStatus("Model: " + (this.editing.slim ? "Slim" : "Classic"));
               saveEditing();
            }
            return true;
         }
         // Cape chips.
         for (int[] r : this.capeRects) {
            if (mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3]) {
               if (this.editing != null) {
                  if (r[4] < 0) { this.editing.capeId = null; this.editing.capeName = null; setStatus("Cape: none"); }
                  else { CapeService.Cape c = this.capes.get(r[4]); this.editing.capeId = c.id; this.editing.capeName = c.alias; setStatus("Cape: " + c.alias); }
                  saveEditing();
               }
               return true;
            }
         }
         // Preview orbit (right half of content).
         int rightX = this.contentX + this.contentW * 52 / 100 + 10;
         if (button == 0 && mx >= rightX && mx <= this.contentX + this.contentW && my >= this.contentY && my <= this.contentY + this.contentH) {
            this.draggingPreview = true;
            return true;
         }
      } else { // LIBRARY — pick a saved skin for the preset.
         int viewTop = this.contentY + 22, viewH = this.contentH - 28;
         if (!this.librarySkins.isEmpty() && mx >= this.contentX && mx <= this.contentX + this.contentW && my >= viewTop && my <= viewTop + viewH) {
            int cols = galleryCols();
            int usedW = cols * CARD_W + (cols - 1) * CARD_GAP;
            int startX = this.contentX + (this.contentW - usedW) / 2;
            for (int i = 0; i < this.librarySkins.size(); i++) {
               int col = i % cols, row = i / cols;
               int cardX = startX + col * (CARD_W + CARD_GAP);
               int cardY = viewTop + row * (CARD_H + CARD_GAP) - (int) this.libraryScroll;
               if (mx >= cardX && mx <= cardX + CARD_W && my >= cardY && my <= cardY + CARD_H) {
                  if (this.editing != null) { this.editing.skinFile = this.librarySkins.get(i); saveEditing(); }
                  this.view = View.EDIT;
                  this.rebuildButtons();
                  setStatus("Skin set from library.");
                  return true;
               }
            }
         }
      }
      return super.method_25402(click, bl);
   }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      if (this.draggingPreview) {
         float inv = this.uiScale.scale <= 0f ? 1f : this.uiScale.scale;
         this.previewYaw += (float) (dx / inv);
         this.previewPitch += (float) (dy / inv) * 0.02f;
         this.previewPitch = Math.max(-0.7f, Math.min(0.7f, this.previewPitch));
         return true;
      }
      return super.method_25403(click, dx, dy);
   }

   public boolean method_25406(class_11909 click) {
      if (this.draggingPreview) { this.draggingPreview = false; return true; }
      return super.method_25406(click);
   }

   public boolean method_25401(double mx, double my, double ha, double va) {
      if (this.view == View.GALLERY || this.view == View.LIBRARY) {
         int cols = galleryCols();
         int count = this.view == View.GALLERY ? this.presetFilter.size() : this.librarySkins.size();
         int rows = (count + cols - 1) / cols;
         int viewH = this.view == View.GALLERY ? this.contentH - 50 : this.contentH - 28;
         float maxScroll = Math.max(0f, rows * (CARD_H + CARD_GAP) - viewH);
         if (maxScroll > 0f) {
            float ns = (this.view == View.GALLERY ? this.galleryScroll : this.libraryScroll) - (float) va * 30f;
            ns = Math.max(0f, Math.min(maxScroll, ns));
            if (this.view == View.GALLERY) this.galleryScroll = ns; else this.libraryScroll = ns;
            return true;
         }
      }
      return super.method_25401(mx, my, ha, va);
   }

   public boolean method_25400(net.minecraft.class_11905 event) {
      String s = event.method_74226();
      if (s != null && !s.isEmpty()) {
         if (this.nameFocused && this.nameInput.length() < 24) { this.nameInput += s; return true; }
         if (this.ignFocused && this.ignInput.length() < 16) { this.ignInput += s; return true; }
         if (this.searchFocused && this.presetSearch.length() < 24) { this.presetSearch += s; this.galleryScroll = 0f; return true; }
      }
      return super.method_25400(event);
   }

   public boolean method_25404(net.minecraft.class_11908 input) {
      int key = input.comp_4795();
      if (this.searchFocused) {
         if (key == 259) { if (!this.presetSearch.isEmpty()) this.presetSearch = this.presetSearch.substring(0, this.presetSearch.length() - 1); this.galleryScroll = 0f; return true; }
         if (key == 257 || key == 335 || key == 256) { this.searchFocused = false; return true; }
      }
      if (this.nameFocused || this.ignFocused) {
         if (key == 259) {   // backspace
            if (this.nameFocused && !this.nameInput.isEmpty()) this.nameInput = this.nameInput.substring(0, this.nameInput.length() - 1);
            if (this.ignFocused && !this.ignInput.isEmpty()) this.ignInput = this.ignInput.substring(0, this.ignInput.length() - 1);
            return true;
         }
         if (key == 257 || key == 335) {   // enter
            if (this.ignFocused) fetchByName(this.ignInput);
            if (this.nameFocused) saveEditing();
            this.nameFocused = false; this.ignFocused = false;
            return true;
         }
         if (key == 256) { this.nameFocused = false; this.ignFocused = false; return true; }
      }
      return super.method_25404(input);
   }

   public void method_25419() {
      saveEditing();
      if (this.field_22787 != null) this.field_22787.method_1507(this.parent);
   }
}
