package com.turtmod.cosmetics;

import com.turtmod.cosmetics.skin.SkinLoader;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_10055;
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

public class CosmeticsScreen extends class_437 {
   private final class_437 parent;
   private List<SkinEntry> availableSkins = new ArrayList();
   private float scrollPx = 0f;     // smooth pixel scroll offset
   private float scrollVel = 0f;    // momentum velocity (px/s)
   private int selectedSkinIndex = -1;
   private boolean globalSlimModel = false;
   // Main-preview manual orbit (drag to rotate; no auto-spin)
   private float previewYaw = 180f;
   private float previewPitch = 0f;
   private boolean draggingPreview = false;
   private String statusMessage = "";
   private int messageTicks = 0;
   private float openFade = 0f;
   private long lastFrameNs = System.nanoTime();

   private final List<TurtUIButton> buttons = new ArrayList();
   // Fixed logical layout scaled to fit any resolution / GUI scale.
   private static final int LOGICAL_W = 580;
   private static final int LOGICAL_H = 400;
   private final TurtUIScale uiScale = new TurtUIScale();
   private int panelX, panelY, panelW, panelH;
   private int listX, listY, listW, listH;
   private int previewX, previewY, previewW, previewH;

   private static final Color PANEL_BG = new Color(1709588, true);
   private static final Color PANEL_BORDER = new Color(9289311, true);
   private static final Color ACCENT_GREEN = new Color(9289311, false);
   private static final Color ACCENT_PINK = new Color(16752046, false);
   private static final Color TEXT_MAIN = new Color(16775399, false);
   private static final Color BTN_BG = new Color(2433054, true);
   private static final Color BTN_HOVER = new Color(3482400, true);
   private static final int ROW_H = 54;

   public CosmeticsScreen(class_437 parent) {
      super(class_2561.method_43470("TurtMod Skin Changer"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.loadAvailableSkins();
      this.layoutPanels();
      TurtUITheme btnTheme = new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
      this.buttons.clear();

      // ── All action buttons live INSIDE the sidebar (chrome layer) ──
      int sx = this.panelX + 6;
      int sw = TurtLauncher.SIDEBAR_W - 12;
      int sy = this.panelY + TurtLauncher.HEADER_H + 8;
      int gap = 4;
      int bh = 18;

      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Apply", btnTheme, this::uploadSelectedSkinGlobal)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Upload", btnTheme, this::importSkinFile)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Refresh", btnTheme, () -> {
         this.loadAvailableSkins(); this.setStatus("List refreshed.");
      })); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Clear", btnTheme, this::clearSelection)); sy += bh + gap;
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Open Folder", btnTheme, this::openSkinsFolder)); sy += bh + gap + 6;

      // Functional Model toggle (single button, flips on click)
      this.buttons.add(new TurtUIButton(sx, sy, sw, bh, modelLabel(), btnTheme, () -> {
         this.globalSlimModel = !this.globalSlimModel;
         this.setStatus("Model: " + (this.globalSlimModel ? "Slim" : "Classic"));
         this.method_25426(); // rebuild to update label
      })); sy += bh + gap + 6;

      // Back at bottom of sidebar
      int backY = this.panelY + this.panelH - TurtLauncher.FOOTER_H - bh - 6;
      this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back", btnTheme, this::method_25419));
   }

   private String modelLabel() {
      return this.globalSlimModel ? "Model: Slim" : "Model: Classic";
   }

   private void layoutPanels() {
      // Fixed logical size; method_25394 scales the whole panel to fit the screen.
      this.panelW = LOGICAL_W;
      this.panelH = LOGICAL_H;
      this.panelX = 0;
      this.panelY = 0;
      int cX = TurtLauncher.contentX(this.panelX);
      int cY = TurtLauncher.contentY(this.panelY);
      int cW = TurtLauncher.contentW(this.panelW);
      int cH = TurtLauncher.contentH(this.panelH);
      // Left = list (56%), right = 3D preview (rest)
      this.listX = cX;
      this.listY = cY;
      this.listW = cW * 56 / 100;
      this.listH = cH;
      this.previewX = this.listX + this.listW + 10;
      this.previewY = cY;
      this.previewW = cW - this.listW - 10;
      this.previewH = cH;
   }

   private void loadAvailableSkins() {
      this.availableSkins.clear();
      Path skinsDir = CosmeticManager.getSkinsDirectory();
      if (!Files.exists(skinsDir, new LinkOption[0])) {
         try { Files.createDirectories(skinsDir); } catch (IOException ignored) {}
      }
      File[] files = skinsDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
      if (files != null) {
         Arrays.sort(files, Comparator.comparing(File::getName));
         for (File f : files) this.availableSkins.add(new SkinEntry(f.getName(), f.getAbsolutePath()));
      }
   }

   private class_2960 getSkinTexture(SkinEntry skin) {
      return SkinLoader.getTexture(java.nio.file.Paths.get(skin.filePath));
   }

   /**
    * Render a real 3D player model inside a LOGICAL-space box.
    *
    * <p>{@code class_332.method_70856} enqueues a GUI entity-render task that uses raw framebuffer
    * coordinates and does <em>not</em> apply the 2D matrix stack ({@code field_44657}) — unlike blits
    * and scissors. So we must map the logical box (and the open-slide offset) to framebuffer pixels
    * ourselves and scale the model size by the UI fit-scale, otherwise the model lands off the pane
    * (which is why the preview "sometimes" vanished at certain window sizes / GUI scales).
    */
   private void drawModel3D(class_332 ctx, class_2960 skinId, int lx, int ly, int lw, int lh,
                            float bodyYaw, float headYaw, float headPitch, float tiltPitch) {
      if (skinId == null) return;
      class_8685 skinTextures = buildSkinTextures(skinId);
      class_10055 state = new class_10055();
      state.field_53520 = skinTextures;     // skin textures (direct path)
      state.field_53329 = 0.6f;             // bounding box width
      state.field_53330 = 1.8f;             // bounding box height
      state.field_61820 = 15728880;          // full bright
      state.field_61821 = 0;                // no outline
      state.field_61823.clear();             // no shadow pieces
      state.field_53453 = 1.0f;             // scale divisor
      state.field_53454 = 1.0f;
      state.field_53446 = bodyYaw;          // bodyYaw
      state.field_53447 = headYaw;          // headYaw
      state.field_53448 = headPitch;        // headPitch

      int cx = lx + lw / 2;
      int top = ly;
      int bot = ly + lh;
      int scale = Math.max(6, lh / 3);
      // Logical → framebuffer. X has no animated translate; Y includes the open-slide offset so the
      // model tracks the 2D content during the open animation.
      float openOff = (1f - this.openFade) * 7f;
      int sx1 = this.uiScale.toScreenX(cx - scale);
      int sx2 = this.uiScale.toScreenX(cx + scale);
      int sy1 = Math.round(this.uiScale.offsetY + (top + openOff) * this.uiScale.scale);
      int sy2 = Math.round(this.uiScale.offsetY + (bot + openOff) * this.uiScale.scale);
      float sScale = scale * this.uiScale.scale;

      Quaternionf baseRot = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf orbit = null;
      if (tiltPitch != 0.0f) {
         // Whole-model vertical tilt — mirror vanilla InventoryScreen.drawEntity: fold the pitch into
         // the base rotation and pass its inverse as rot2 so lighting stays camera-relative.
         Quaternionf pitch = new Quaternionf().rotateX(tiltPitch);
         baseRot.mul(pitch);
         orbit = new Quaternionf(pitch).conjugate();
      }
      Vector3f pos = new Vector3f(0.0f, state.field_53330 / 2.0f + 0.0625f, 0.0f);
      ctx.method_70856(state, sScale, pos, baseRot, orbit, sx1, sy1, sx2, sy2);
   }

   /** Build SkinTextures using DIRECT path (class_12080) — never resource-pack lookup. */
   private class_8685 buildSkinTextures(class_2960 id) {
      return new class_8685(
         new class_12079.class_12080(id, "turtmod_skin"),  // direct registered-texture path
         null, null,
         this.globalSlimModel ? class_7920.field_41122 : class_7920.field_41123,
         true
      );
   }

   private void selectCurrentSkin() {
      if (this.selectedSkinIndex >= 0 && this.selectedSkinIndex < this.availableSkins.size()) {
         SkinEntry skin = (SkinEntry) this.availableSkins.get(this.selectedSkinIndex);
         CosmeticProfile profile = CosmeticManager.getActiveProfile();
         if (profile == null) {
            profile = new CosmeticProfile("Default", skin.filePath);
            CosmeticManager.addProfile(profile);
         } else {
            profile.skinPath = skin.filePath;
            profile.enableSkin = true;
         }
         CosmeticManager.setActiveProfile(profile);
         CosmeticManager.saveProfiles();
         this.setStatus("Applied " + skin.fileName);
      } else {
         this.setStatus("Select a skin first.");
      }
   }

   private void uploadSelectedSkinGlobal() {
      if (this.selectedSkinIndex >= 0 && this.selectedSkinIndex < this.availableSkins.size()) {
         SkinEntry skin = (SkinEntry) this.availableSkins.get(this.selectedSkinIndex);
         CompletableFuture.runAsync(() -> {
            this.setStatus("Uploading...");
            CosmeticManager.UploadResult result = CosmeticManager.uploadSkinToMinecraftProfile(skin.filePath, this.globalSlimModel);
            this.field_22787.execute(() -> this.setStatus(result.success() ? "Global skin updated!" : "Upload failed: " + result.message()));
         });
      } else {
         this.setStatus("Select a skin first.");
      }
   }

   private void clearSelection() {
      this.selectedSkinIndex = -1;
      CosmeticProfile profile = CosmeticManager.getActiveProfile();
      if (profile != null) {
         profile.enableSkin = false;
         CosmeticManager.setActiveProfile(profile);
         CosmeticManager.saveProfiles();
      }
      this.setStatus("Selection cleared.");
   }

   private void openSkinsFolder() {
      File folder = CosmeticManager.getSkinsDirectory().toFile();
      if (!folder.exists()) folder.mkdirs();
      class_156.method_668().method_672(folder);
   }

   /** Opens a native file picker to import a skin PNG into the skins folder. */
   private void importSkinFile() {
      CompletableFuture.runAsync(() -> {
         String picked = null;
         try {
            org.lwjgl.PointerBuffer filters = org.lwjgl.system.MemoryUtil.memAllocPointer(1);
            java.nio.ByteBuffer png = org.lwjgl.system.MemoryUtil.memUTF8("*.png");
            filters.put(png);
            filters.flip();
            picked = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
               "Select a skin PNG", "", filters, "PNG image (*.png)", false);
            org.lwjgl.system.MemoryUtil.memFree(filters);
            org.lwjgl.system.MemoryUtil.memFree(png);
         } catch (Throwable t) {
            this.field_22787.execute(() -> this.setStatus("File picker unavailable."));
            return;
         }
         if (picked == null) return;
         final String path = picked;
         this.field_22787.execute(() -> {
            try {
               java.nio.file.Path src = java.nio.file.Paths.get(path);
               String name = src.getFileName().toString();
               if (!name.toLowerCase().endsWith(".png")) { this.setStatus("Not a PNG file."); return; }
               java.nio.file.Path dest = CosmeticManager.getSkinsDirectory().resolve(name);
               Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
               this.loadAvailableSkins();
               this.setStatus("Imported " + name);
            } catch (Exception e) {
               this.setStatus("Import failed: " + e.getMessage());
            }
         });
      });
   }

   private void setStatus(String msg) {
      this.statusMessage = msg;
      this.messageTicks = 80;
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - lastFrameNs) / 1_000_000_000f, 0.1f);
      lastFrameNs = now;
      openFade = TurtUIUtils.lerp01(openFade, 1f, dt, 12f);

      // ── momentum scroll physics for the skin list
      this.scrollPx += this.scrollVel * dt;
      this.scrollVel *= (float) Math.exp(-9.0 * dt);     // eased slowdown
      if (Math.abs(this.scrollVel) < 1.5f) this.scrollVel = 0f;
      float maxScroll = Math.max(0f, this.availableSkins.size() * ROW_H - this.skinViewH());
      if (this.scrollPx < 0f)         { this.scrollPx = 0f; this.scrollVel = 0f; }
      if (this.scrollPx > maxScroll)  { this.scrollPx = maxScroll; this.scrollVel = 0f; }

      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(ctx, mx, my);
      TurtUIUtils.update();

      // Fit the fixed logical layout to the screen, then work in logical mouse coords.
      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      mx = (int)this.uiScale.toLogicalX(mx);
      my = (int)this.uiScale.toLogicalY(my);
      this.uiScale.push(ctx);

      // subtle slide-up on open (matches Hub / Settings / Gallery)
      ctx.method_51448().pushMatrix();
      float introE = TurtUIUtils.ease(openFade);
      ctx.method_51448().translate(LOGICAL_W / 2f, LOGICAL_H / 2f + (1f - introE) * 12f);
      ctx.method_51448().scale(0.97f + 0.03f * introE, 0.97f + 0.03f * introE);
      ctx.method_51448().translate(-LOGICAL_W / 2f, -LOGICAL_H / 2f);

      String player = this.field_22787 != null && this.field_22787.method_1548() != null
         ? this.field_22787.method_1548().method_1676() : "Player";

      // ── CHROME LAYER ──
      TurtLauncher.drawChrome(ctx, this.field_22793, this.panelX, this.panelY, this.panelW, this.panelH,
         "Skin Changer", player, "Skins");

      // ── CONTENT LAYER ──
      renderSkinList(ctx, mx, my);
      renderPreviewPane(ctx);

      // Status toast (in content area, above list)
      if (this.messageTicks > 0) {
         ctx.method_25300(this.field_22793, this.statusMessage, this.listX + this.listW / 2, this.listY - 1, ACCENT_GREEN.getRGB());
         --this.messageTicks;
      }

      // ── INTERACTION LAYER (buttons, drawn last = top) ──
      for (TurtUIButton btn : this.buttons) btn.render(ctx, mx, my, this.field_22793);

      ctx.method_51448().popMatrix();
      this.uiScale.pop(ctx);
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   private int skinViewTop() { return this.listY + 22; }
   private int skinViewH()   { return this.listH - 28; }

   private void renderSkinList(class_332 ctx, int mx, int my) {
      // List container (shared polished panel + title)
      TurtLauncher.drawContentPanel(ctx, this.field_22793, this.listX, this.listY, this.listW, this.listH, "SKIN LIBRARY");

      int viewTop = this.skinViewTop();
      int viewH   = this.skinViewH();
      if (this.availableSkins.isEmpty()) {
         ctx.method_25300(this.field_22793, "No skins found", this.listX + this.listW / 2, this.listY + this.listH / 2 - 8, 0xFF888888);
         ctx.method_25300(this.field_22793, "Click 'Open Folder'", this.listX + this.listW / 2, this.listY + this.listH / 2 + 4, 0xFF666666);
         return;
      }

      // Clip rows to the list viewport so partial rows glide cleanly at the edges
      ctx.method_44379(this.listX + 1, viewTop, this.listX + this.listW - 1, viewTop + viewH);
      int firstIdx = Math.max(0, (int) (this.scrollPx / ROW_H));
      for (int idx = firstIdx; idx < this.availableSkins.size(); idx++) {
         int rowY = viewTop + idx * ROW_H - (int) this.scrollPx;
         if (rowY >= viewTop + viewH) break;
         if (rowY + ROW_H <= viewTop) continue;
         SkinEntry skin = (SkinEntry) this.availableSkins.get(idx);
         boolean selected = idx == this.selectedSkinIndex;
         boolean hovered = mx >= this.listX + 2 && mx <= this.listX + this.listW - 2 && my >= rowY && my <= rowY + ROW_H - 2 && my >= viewTop && my <= viewTop + viewH;

         // ── Row card ──
         int cardX = this.listX + 3, cardW = this.listW - 7;
         int cardY = rowY, cardH = ROW_H - 4;
         int cardBg = selected ? 0x48000000 : (hovered ? 0x33000000 : 0x1E000000);
         TurtUIUtils.drawRoundedRect(ctx, cardX, cardY, cardW, cardH, 4, new Color(cardBg, true));

         // ── Model on a mini presentation stage (matches the big preview) ──
         int icoSz = cardH - 8, icoX = cardX + 6, icoY = cardY + 4;
         TurtUIUtils.drawRoundedRect(ctx, icoX, icoY, icoSz, icoSz, 3, new Color(0, 0, 0, 130));
         TurtUIUtils.drawStage(ctx, icoX, icoY, icoSz, icoSz, ACCENT_GREEN);
         class_2960 tex = getSkinTexture(skin);
         if (tex != null) {
            // gentle auto rotation, offset per row so the column isn't perfectly in sync
            float rot = ((float)(System.currentTimeMillis() % 9000L) / 9000.0f * 360.0f) + idx * 35.0f;
            drawModel3D(ctx, tex, icoX, icoY, icoSz, icoSz, 180.0f + rot, 0.0f, 0.0f, 0.0f);
         }
         TurtUIUtils.drawRoundedBorder(ctx, icoX, icoY, icoSz, icoSz, 3, new Color(255, 255, 255, 28));

         String name = skin.fileName;
         if (name.toLowerCase().endsWith(".png")) name = name.substring(0, name.length() - 4);
         if (name.length() > 18) name = name.substring(0, 16) + "..";
         ctx.method_25303(this.field_22793, name, icoX + icoSz + 8, rowY + ROW_H / 2 - 4,
            selected ? ACCENT_GREEN.getRGB() : TEXT_MAIN.getRGB());

         // ── Selection accents ──
         if (selected) {
            TurtUIUtils.drawRoundedBorder(ctx, cardX, cardY, cardW, cardH, 4, ACCENT_GREEN);
            ctx.method_25294(cardX + 2, cardY + 4, cardX + 4, cardY + cardH - 4, ACCENT_GREEN.getRGB());
         }
      }
      ctx.method_44380();

      // Scrollbar
      float maxScroll = Math.max(0f, this.availableSkins.size() * ROW_H - viewH);
      if (maxScroll > 0f) {
         int sbX = this.listX + this.listW - 4;
         int sbY = viewTop, sbH = viewH;
         TurtUIUtils.drawRoundedRect(ctx, sbX, sbY, 3, sbH, 1, new Color(0x22FFFFFF, true));
         float ratio = (float) viewH / (this.availableSkins.size() * ROW_H);
         int thumbH = Math.max(14, (int)(sbH * ratio));
         int thumbY = sbY + (int)((sbH - thumbH) * (this.scrollPx / maxScroll));
         TurtUIUtils.drawRoundedRect(ctx, sbX, thumbY, 3, thumbH, 1, ACCENT_GREEN);
      }
   }

   /** Real 3D player model preview using the selected skin (direct texture path). */
   private void renderPreviewPane(class_332 ctx) {
      // Preview container (shared polished panel + title)
      TurtLauncher.drawContentPanel(ctx, this.field_22793, this.previewX, this.previewY, this.previewW, this.previewH, "PREVIEW");

      int cx = this.previewX + this.previewW / 2;
      int top = this.previewY + 24;
      int bot = this.previewY + this.previewH - 22;
      int boxH = bot - top;
      int scale = Math.max(20, boxH / 3);

      // ── PRESENTATION STAGE (drawn behind the model) ──
      int stageX = this.previewX + 2, stageY = this.previewY + 18;
      int stageW = this.previewW - 4, stageH = this.previewH - 30;
      // Soft spotlight — bright green-tinted glow up top fading into the dark floor
      ctx.method_25296(stageX, stageY, stageX + stageW, stageY + stageH, 0x2AA8FFC0, 0x00101418);
      // Brighter spotlight core near the top centre
      drawFilledEllipse(ctx, cx, stageY + stageH / 5, stageW / 3, stageH / 5, 0x14FFFFFF);
      // Ambient contact shadow + rotating platform disc at the feet
      int feetY = bot - 2;
      int platRx = (int)(scale * 0.85f), platRy = Math.max(4, (int)(scale * 0.22f));
      drawFilledEllipse(ctx, cx, feetY + 3, platRx + 3, platRy + 2, 0x4D000000);        // shadow
      drawFilledEllipse(ctx, cx, feetY, platRx, platRy,
         new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 40).getRGB());
      drawFilledEllipse(ctx, cx, feetY, platRx * 2 / 3, platRy * 2 / 3,
         new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 55).getRGB());
      // Rotating highlight travelling around the platform rim
      float pa = (float)(System.currentTimeMillis() % 4000L) / 4000.0f * (float)(Math.PI * 2);
      int hx = cx + (int)(Math.cos(pa) * platRx);
      int hy = feetY + (int)(Math.sin(pa) * platRy);
      ctx.method_25294(hx - 1, hy - 1, hx + 1, hy + 1, 0xCCB8FFD0);

      class_2960 skinId = (this.selectedSkinIndex >= 0 && this.selectedSkinIndex < this.availableSkins.size())
         ? getSkinTexture((SkinEntry) this.availableSkins.get(this.selectedSkinIndex)) : null;

      if (skinId != null) {
         try {
            // Manual orbit — driven by mouse drag (previewYaw / previewPitch), no auto-spin.
            // headYaw is RELATIVE to the body, so 0 keeps the head aligned as the body rotates.
            drawModel3D(ctx, skinId, cx - scale, top, scale * 2, bot - top,
               this.previewYaw, 0.0f, 0.0f, this.previewPitch);
         } catch (Throwable e) {
            ctx.method_25300(this.field_22793, "preview error", cx, top + boxH / 2, 0xFFFF5555);
         }
      } else {
         long t = System.currentTimeMillis() / 300 % 3;
         String dots = t == 0 ? "." : t == 1 ? ".." : "...";
         ctx.method_25300(this.field_22793, "Select a skin" , cx, top + boxH / 2 - 10, 0xFF888888);
         ctx.method_25300(this.field_22793, dots, cx, top + boxH / 2 + 2, 0xFF666666);
      }

      // Edge vignette — darken the stage corners for depth/focus
      ctx.method_25296(stageX, stageY, stageX + stageW, stageY + 14, 0x33000000, 0x00000000);
      ctx.method_25296(stageX, stageY + stageH - 14, stageX + stageW, stageY + stageH, 0x00000000, 0x33000000);
      // Rounded AA frame so the preview reads as a clean rounded card (the square fill corners sit
      // under the dark vignette, so the rounded border defines the visible shape).
      TurtUIUtils.drawRoundedBorder(ctx, stageX, stageY, stageW, stageH, 7,
         new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 70));

      // Apply-success green glow pulse on the preview frame
      if (this.messageTicks > 0 && (this.statusMessage.contains("updated") || this.statusMessage.startsWith("Applied"))) {
         float p = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() / 180.0);
         Color g = new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), (int)(60 + 120 * p));
         ctx.method_73198(this.previewX, this.previewY, this.previewW, this.previewH, g.getRGB());
         ctx.method_73198(this.previewX - 1, this.previewY - 1, this.previewW + 2, this.previewH + 2, g.getRGB());
      }

      // Model label at bottom
      ctx.method_25300(this.field_22793, this.globalSlimModel ? "Slim Model" : "Classic Model",
         cx, this.previewY + this.previewH - 12, 0xFF888888);
   }

   /** Filled ellipse via horizontal scanlines (no shader). */
   private void drawFilledEllipse(class_332 ctx, int cx, int cy, int rx, int ry, int argb) {
      if (rx <= 0 || ry <= 0) return;
      for (int dy = -ry; dy <= ry; dy++) {
         double f = 1.0 - (double)(dy * dy) / (double)(ry * ry);
         if (f < 0) continue;
         int halfW = (int)(rx * Math.sqrt(f));
         ctx.method_25294(cx - halfW, cy + dy, cx + halfW, cy + dy + 1, argb);
      }
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = this.uiScale.toLogicalX(click.comp_4798()), my = this.uiScale.toLogicalY(click.comp_4799());
      int button = click.method_74245();
      // Buttons (sidebar) — highest priority
      for (TurtUIButton btn : this.buttons) if (btn.mouseClicked(mx, my, button)) return true;
      // List row selection (pixel-based, accounts for smooth scroll)
      int viewTop = this.skinViewTop();
      int viewH = this.skinViewH();
      if (mx >= this.listX && mx <= this.listX + this.listW && my >= viewTop && my <= viewTop + viewH) {
         int idx = (int)((my - viewTop + this.scrollPx) / ROW_H);
         if (idx >= 0 && idx < this.availableSkins.size()) {
            this.selectedSkinIndex = idx;
            this.setStatus("Selected: " + ((SkinEntry) this.availableSkins.get(idx)).fileName);
            return true;
         }
      }
      // Grab the preview pane to orbit the model (drag rotates; no auto-spin)
      if (button == 0 && mx >= this.previewX && mx <= this.previewX + this.previewW
            && my >= this.previewY && my <= this.previewY + this.previewH) {
         this.draggingPreview = true;
         return true;
      }
      return super.method_25402(click, bl);
   }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      if (this.draggingPreview) {
         float inv = this.uiScale.scale <= 0f ? 1f : this.uiScale.scale;
         this.previewYaw += (float)(dx / inv);                 // horizontal drag → yaw (degrees)
         this.previewPitch += (float)(dy / inv) * 0.02f;       // vertical drag → tilt (radians)
         this.previewPitch = Math.max(-0.7f, Math.min(0.7f, this.previewPitch));
         return true;
      }
      return super.method_25403(click, dx, dy);
   }

   public boolean method_25406(class_11909 click) {
      if (this.draggingPreview) {
         this.draggingPreview = false;
         return true;
      }
      return super.method_25406(click);
   }

   public boolean method_25401(double mx, double my, double ha, double va) {
      float maxScroll = Math.max(0f, this.availableSkins.size() * ROW_H - this.skinViewH());
      if (maxScroll > 0f) {
         // impulse into the momentum velocity — eased slowdown handled in render
         this.scrollVel -= (float) va * 1500f;
         return true;
      }
      return super.method_25401(mx, my, ha, va);
   }

   public void method_25419() {
      if (this.field_22787 != null) this.field_22787.method_1507(this.parent);
   }

   private static class SkinEntry {
      final String fileName;
      final String filePath;
      SkinEntry(String fileName, String filePath) {
         this.fileName = fileName;
         this.filePath = filePath;
      }
   }
}
