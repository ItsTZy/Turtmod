package com.turtmod.hud;

import com.turtmod.TurtModClient;
import com.turtmod.config.ConfigManager;
import com.turtmod.config.TurtModConfig;
import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtLauncher;
import com.turtmod.ui.TurtUIButton;
import com.turtmod.ui.TurtUIScale;
import com.turtmod.ui.TurtUITheme;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1299;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7923;

/**
 * A turtle-themed screen for the user's custom per-entity hitbox colours. You add entities from a
 * searchable list; each one draws its hitbox in a colour you pick (with transparency). Everything not in
 * the list uses the normal hitbox colour.
 */
public class HitboxColorsScreen extends class_437 {
   private enum View { LIST, ADD }

   private final class_437 parent;
   private View view = View.LIST;

   private static final int LOGICAL_W = 580, LOGICAL_H = 400;
   private final TurtUIScale uiScale = new TurtUIScale();
   private int panelX, panelY, panelW, panelH;
   private int contentX, contentY, contentW, contentH;
   private final List<TurtUIButton> buttons = new ArrayList<>();

   private static final Color PANEL_BORDER = new Color(9289311, true);
   private static final Color ACCENT_GREEN = new Color(9289311, false);
   private static final Color ACCENT_PINK = new Color(16752046, false);
   private static final Color TEXT_MAIN = new Color(16775399, false);
   private static final Color BTN_BG = new Color(2433054, true);
   private static final Color BTN_HOVER = new Color(3482400, true);
   private static final int DEFAULT_COLOR = 0xFF00E5FF;

   private static final int ROW_H = 22;
   private static final int ADD_ROW_H = 15;

   // All entity-type ids (built once), and the search state for the ADD view.
   private final List<String> allIds = new ArrayList<>();
   private final List<String> filtered = new ArrayList<>();
   private String search = "";
   private boolean searchFocused = false;
   private int searchX, searchY, searchW, searchH;
   private float listScroll = 0f, addScroll = 0f;

   // Colour picker (opens for one entity at a time).
   private String editing = null;   // entity id whose colour is being edited
   private float pickH, pickS, pickV, pickA = 1f;
   private int pickX, pickY;
   private int dragMode = 0;   // 1 = SV square, 2 = hue, 3 = alpha
   private static final int PICK_W = 150, PICK_H = 110, HUE_H = 12, ALPHA_H = 12;

   private float openFade = 0f;
   private long lastFrameNs = System.nanoTime();

   public HitboxColorsScreen(class_437 parent) {
      super(class_2561.method_43470("Hitbox Entity Colors"));
      this.parent = parent;
   }

   private TurtModConfig cfg() { return TurtModClient.getConfig(); }

   protected void method_25426() {
      this.allIds.clear();
      for (class_1299<?> type : class_7923.field_41177) {
         class_2960 id = class_7923.field_41177.method_10221(type);
         if (id != null) this.allIds.add(id.toString());
      }
      this.allIds.sort(String::compareTo);
      layoutPanels();
      rebuildButtons();
   }

   private TurtUITheme theme() {
      return new TurtUITheme(BTN_BG, PANEL_BORDER, TEXT_MAIN, BTN_HOVER, ACCENT_PINK);
   }

   private void layoutPanels() {
      this.panelW = LOGICAL_W; this.panelH = LOGICAL_H; this.panelX = 0; this.panelY = 0;
      this.contentX = TurtLauncher.contentX(this.panelX);
      this.contentY = TurtLauncher.contentY(this.panelY);
      this.contentW = TurtLauncher.contentW(this.panelW);
      this.contentH = TurtLauncher.contentH(this.panelH);
   }

   private void rebuildButtons() {
      this.buttons.clear();
      TurtUITheme t = theme();
      int sx = this.panelX + 6, sw = TurtLauncher.SIDEBAR_W - 12;
      int sy = this.panelY + TurtLauncher.HEADER_H + 8, bh = 18, gap = 6;
      int backY = this.panelY + this.panelH - TurtLauncher.FOOTER_H - bh - 6;
      if (this.view == View.LIST) {
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Add Entity", t, () -> { this.view = View.ADD; this.search = ""; this.searchFocused = true; this.addScroll = 0f; rebuildButtons(); })); sy += bh + gap;
         this.buttons.add(new TurtUIButton(sx, sy, sw, bh, "Clear All", t, () -> { cfg().hud.hitboxEntityColors.clear(); ConfigManager.save(cfg()); }));
         this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back", t, this::method_25419));
      } else {
         this.buttons.add(new TurtUIButton(sx, backY, sw, bh, "Back", t, () -> { this.view = View.LIST; this.editing = null; rebuildButtons(); }));
      }
   }

   // ── Render ──
   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      float dt = Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f);
      this.lastFrameNs = now;
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, dt, 12f);

      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      TurtUIUtils.drawCursorGlow(ctx, mx, my);
      TurtUIUtils.update();

      this.uiScale.compute(this.field_22789, this.field_22790, LOGICAL_W, LOGICAL_H, 8);
      int lmx = (int) this.uiScale.toLogicalX(mx), lmy = (int) this.uiScale.toLogicalY(my);
      this.uiScale.push(ctx);

      String player = this.field_22787 != null && this.field_22787.method_1548() != null ? this.field_22787.method_1548().method_1676() : "Player";
      TurtLauncher.drawChrome(ctx, this.field_22793, this.panelX, this.panelY, this.panelW, this.panelH,
         this.view == View.LIST ? "Entity Colors" : "Add Entity", player, "Hitbox");

      if (this.view == View.LIST) renderList(ctx, lmx, lmy); else renderAdd(ctx, lmx, lmy);

      for (TurtUIButton b : this.buttons) b.render(ctx, lmx, lmy, this.field_22793);
      if (this.editing != null) renderPicker(ctx, lmx, lmy);

      this.uiScale.pop(ctx);
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   private void renderList(class_332 ctx, int mx, int my) {
      TurtLauncher.drawContentPanel(ctx, this.field_22793, this.contentX, this.contentY, this.contentW, this.contentH, "YOUR ENTITY COLORS");
      List<String> ids = new ArrayList<>(cfg().hud.hitboxEntityColors.keySet());
      int top = this.contentY + 22, h = this.contentH - 28;
      if (ids.isEmpty()) {
         ctx.method_25300(this.field_22793, "No entities added yet", this.contentX + this.contentW / 2, top + h / 2 - 8, 0xFF888888);
         ctx.method_25300(this.field_22793, "Click 'Add Entity'", this.contentX + this.contentW / 2, top + h / 2 + 4, 0xFF666666);
         return;
      }
      ctx.method_44379(this.contentX + 1, top, this.contentX + this.contentW - 1, top + h);
      for (int i = 0; i < ids.size(); i++) {
         int ry = top + i * ROW_H - (int) this.listScroll;
         if (ry + ROW_H <= top || ry >= top + h) continue;
         renderRow(ctx, ids.get(i), this.contentX + 6, ry, this.contentW - 12, mx, my);
      }
      ctx.method_44380();
   }

   private void renderRow(class_332 ctx, String id, int x, int y, int w, int mx, int my) {
      boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + ROW_H - 2;
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, ROW_H - 2, 4, new Color(hov ? 0x33000000 : 0x1E000000, true));
      int col = cfg().hud.hitboxEntityColors.getOrDefault(id, DEFAULT_COLOR);
      // Colour swatch (click to edit) + remove button, right-aligned.
      int swW = 34, swX = x + w - 34 - 8, swY = y + 3, swH = ROW_H - 8;
      // checker under transparency
      TurtUIUtils.drawRoundedRect(ctx, swX, swY, swW, swH, 3, new Color(0xFF303030, false));
      TurtUIUtils.drawRoundedRect(ctx, swX, swY, swW, swH, 3, new Color(col, true));
      TurtUIUtils.drawRoundedBorder(ctx, swX, swY, swW, swH, 3, new Color(255, 255, 255, 40));
      int rmX = x + w - 8, rmY = y + 3;   // "×" hit area is [rmX-16 .. rmX]
      ctx.method_51433(this.field_22793, "✕", rmX - 12, y + 5, 0xFFCC6666, false);
      // Name = path in bold-ish, full id muted under.
      String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
      ctx.method_51433(this.field_22793, path, x + 8, y + 3, (hov ? ACCENT_GREEN : TEXT_MAIN).getRGB(), false);
      ctx.method_51433(this.field_22793, id, x + 8, y + 12, 0xFF6C7278, false);
   }

   private void renderAdd(class_332 ctx, int mx, int my) {
      TurtLauncher.drawContentPanel(ctx, this.field_22793, this.contentX, this.contentY, this.contentW, this.contentH, "PICK AN ENTITY");
      this.searchX = this.contentX + 6; this.searchY = this.contentY + 20; this.searchW = this.contentW - 12; this.searchH = 16;
      drawField(ctx, this.searchX, this.searchY, this.searchW, this.searchH, this.search, this.searchFocused, "search entities...");

      this.filtered.clear();
      String q = this.search.trim().toLowerCase();
      for (String id : this.allIds) if (q.isEmpty() || id.toLowerCase().contains(q)) this.filtered.add(id);

      int top = this.contentY + 42, h = this.contentH - 48;
      ctx.method_44379(this.contentX + 1, top, this.contentX + this.contentW - 1, top + h);
      for (int i = 0; i < this.filtered.size(); i++) {
         int ry = top + i * ADD_ROW_H - (int) this.addScroll;
         if (ry + ADD_ROW_H <= top || ry >= top + h) continue;
         String id = this.filtered.get(i);
         boolean added = cfg().hud.hitboxEntityColors.containsKey(id);
         boolean hov = mx >= this.contentX + 6 && mx <= this.contentX + this.contentW - 6 && my >= ry && my <= ry + ADD_ROW_H - 1;
         if (hov) ctx.method_25294(this.contentX + 4, ry, this.contentX + this.contentW - 4, ry + ADD_ROW_H - 1, 0x33000000);
         String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
         ctx.method_51433(this.field_22793, path, this.contentX + 10, ry + 3, added ? 0xFF6C7278 : (hov ? ACCENT_GREEN.getRGB() : TEXT_MAIN.getRGB()), false);
         if (added) ctx.method_51433(this.field_22793, "added", this.contentX + this.contentW - 40, ry + 3, 0xFF6C7278, false);
      }
      ctx.method_44380();
   }

   private void renderPicker(class_332 ctx, int mx, int my) {
      int px = this.contentX + (this.contentW - PICK_W) / 2;
      int py = this.contentY + (this.contentH - (PICK_H + HUE_H + ALPHA_H + 40)) / 2;
      this.pickX = px; this.pickY = py;
      int totalH = PICK_H + HUE_H + ALPHA_H + 40;
      TurtUIUtils.drawRoundedRect(ctx, px - 8, py - 8, PICK_W + 16, totalH + 12, 5, Palette.alpha(Palette.PANEL_BG, 250));
      TurtUIUtils.drawRoundedBorder(ctx, px - 8, py - 8, PICK_W + 16, totalH + 12, 5, Palette.alpha(PANEL_BORDER, 255));

      for (int sxp = 0; sxp < PICK_W; sxp += 2) {
         float sat = (float) sxp / (PICK_W - 1);
         for (int syp = 0; syp < PICK_H; syp += 2) {
            float val = 1f - (float) syp / (PICK_H - 1);
            ctx.method_25294(px + sxp, py + syp, px + sxp + 2, py + syp + 2, 0xFF000000 | (Color.HSBtoRGB(this.pickH, sat, val) & 0xFFFFFF));
         }
      }
      int mxp = px + Math.round(this.pickS * (PICK_W - 1)), myp = py + Math.round((1f - this.pickV) * (PICK_H - 1));
      ctx.method_73198(mxp - 2, myp - 2, 5, 5, 0xFFFFFFFF);

      int hy = py + PICK_H + 4;
      for (int i = 0; i < PICK_W; i++) ctx.method_25294(px + i, hy, px + i + 1, hy + HUE_H, 0xFF000000 | (Color.HSBtoRGB((float) i / (PICK_W - 1), 1f, 1f) & 0xFFFFFF));
      ctx.method_73198(px + Math.round(this.pickH * (PICK_W - 1)) - 1, hy - 1, 3, HUE_H + 2, 0xFFFFFFFF);

      int ay = hy + HUE_H + 4;
      int base = Color.HSBtoRGB(this.pickH, this.pickS, this.pickV) & 0xFFFFFF;
      for (int i = 0; i < PICK_W; i++) {
         int a = Math.round((float) i / (PICK_W - 1) * 255f);
         ctx.method_25294(px + i, ay, px + i + 1, ay + ALPHA_H, (a << 24) | base);
      }
      ctx.method_73198(px + Math.round(this.pickA * (PICK_W - 1)) - 1, ay - 1, 3, ALPHA_H + 2, 0xFFFFFFFF);

      // Preview + Done.
      int prevY = ay + ALPHA_H + 6;
      TurtUIUtils.drawRoundedRect(ctx, px, prevY, 40, 14, 3, new Color(currentColor(), true));
      TurtUIUtils.drawRoundedBorder(ctx, px, prevY, 40, 14, 3, new Color(0, 0, 0, 255));
      String label = this.editing.contains(":") ? this.editing.substring(this.editing.indexOf(':') + 1) : this.editing;
      ctx.method_51433(this.field_22793, label, px + 46, prevY + 3, TEXT_MAIN.getRGB(), false);
      int dnX = px + PICK_W - 44;
      boolean dnHov = mx >= dnX && mx <= dnX + 44 && my >= prevY && my <= prevY + 14;
      TurtUIUtils.drawRoundedRect(ctx, dnX, prevY, 44, 14, 3, dnHov ? Palette.GREEN : BTN_BG);
      TurtUIUtils.drawRoundedBorder(ctx, dnX, prevY, 44, 14, 3, PANEL_BORDER);
      ctx.method_25300(this.field_22793, "Done", dnX + 22, prevY + 3, (dnHov ? Palette.alpha(Palette.PANEL_BG, 255) : TEXT_MAIN).getRGB());
      this.doneX = dnX; this.doneY = prevY;
   }
   private int doneX, doneY;

   private int currentColor() {
      return (Math.round(this.pickA * 255f) << 24) | (Color.HSBtoRGB(this.pickH, this.pickS, this.pickV) & 0xFFFFFF);
   }

   private void applyColor() {
      if (this.editing != null) {
         cfg().hud.hitboxEntityColors.put(this.editing, currentColor());
      }
   }

   private void drawField(class_332 ctx, int x, int y, int w, int h, String text, boolean focused, String ph) {
      TurtUIUtils.drawRoundedRect(ctx, x, y, w, h, 3, Palette.SEARCH_BG);
      TurtUIUtils.drawRoundedBorder(ctx, x, y, w, h, 3, focused ? Palette.GREEN : Palette.SEARCH_BORDER);
      boolean empty = text.isEmpty() && !focused;
      ctx.method_51433(this.field_22793, empty ? ph : text + (focused ? "_" : ""), x + 4, y + (h - 8) / 2, (empty ? Palette.TEXT_MUTED : Palette.TEXT).getRGB(), false);
   }

   // ── Input ──
   public boolean method_25402(class_11909 click, boolean bl) {
      double mx = this.uiScale.toLogicalX(click.comp_4798()), my = this.uiScale.toLogicalY(click.comp_4799());
      int button = click.method_74245();

      if (this.editing != null) {   // picker is modal
         if (pickerClick(mx, my)) return true;
         // click outside the picker closes it
         if (mx < this.pickX - 8 || mx > this.pickX + PICK_W + 8 || my < this.pickY - 8 || my > this.pickY + PICK_H + HUE_H + ALPHA_H + 52) {
            applyColor(); ConfigManager.save(cfg()); this.editing = null;
         }
         return true;
      }

      for (TurtUIButton b : this.buttons) if (b.mouseClicked(mx, my, button)) return true;

      if (this.view == View.LIST) {
         List<String> ids = new ArrayList<>(cfg().hud.hitboxEntityColors.keySet());
         int top = this.contentY + 22, h = this.contentH - 28;
         if (mx >= this.contentX && mx <= this.contentX + this.contentW && my >= top && my <= top + h) {
            for (int i = 0; i < ids.size(); i++) {
               int ry = top + i * ROW_H - (int) this.listScroll;
               int x = this.contentX + 6, w = this.contentW - 12;
               if (my < ry || my > ry + ROW_H - 2) continue;
               int rmX = x + w - 8;
               if (mx >= rmX - 16 && mx <= rmX + 2) {   // remove
                  cfg().hud.hitboxEntityColors.remove(ids.get(i));
                  ConfigManager.save(cfg());
                  return true;
               }
               openPicker(ids.get(i));   // anywhere else on the row edits the colour
               return true;
            }
         }
      } else {
         this.searchFocused = mx >= this.searchX && mx <= this.searchX + this.searchW && my >= this.searchY && my <= this.searchY + this.searchH;
         if (this.searchFocused) return true;
         int top = this.contentY + 42, h = this.contentH - 48;
         if (mx >= this.contentX && mx <= this.contentX + this.contentW && my >= top && my <= top + h) {
            for (int i = 0; i < this.filtered.size(); i++) {
               int ry = top + i * ADD_ROW_H - (int) this.addScroll;
               if (my < ry || my > ry + ADD_ROW_H - 1) continue;
               String id = this.filtered.get(i);
               if (!cfg().hud.hitboxEntityColors.containsKey(id)) {
                  cfg().hud.hitboxEntityColors.put(id, DEFAULT_COLOR);
                  ConfigManager.save(cfg());
               }
               this.view = View.LIST;
               rebuildButtons();
               openPicker(id);
               return true;
            }
         }
      }
      return super.method_25402(click, bl);
   }

   private void openPicker(String id) {
      this.editing = id;
      int c = cfg().hud.hitboxEntityColors.getOrDefault(id, DEFAULT_COLOR);
      float[] hsb = Color.RGBtoHSB((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, null);
      this.pickH = hsb[0]; this.pickS = hsb[1]; this.pickV = hsb[2];
      this.pickA = ((c >>> 24) & 0xFF) / 255f;
   }

   private boolean pickerClick(double mx, double my) {
      int px = this.pickX, py = this.pickY;
      if (mx >= this.doneX && mx <= this.doneX + 44 && my >= this.doneY && my <= this.doneY + 14) {
         applyColor(); ConfigManager.save(cfg()); this.editing = null; return true;
      }
      if (mx >= px && mx < px + PICK_W && my >= py && my < py + PICK_H) { this.dragMode = 1; updateSV(mx, my); return true; }
      int hy = py + PICK_H + 4;
      if (mx >= px && mx < px + PICK_W && my >= hy && my < hy + HUE_H) { this.dragMode = 2; updateHue(mx); return true; }
      int ay = hy + HUE_H + 4;
      if (mx >= px && mx < px + PICK_W && my >= ay && my < ay + ALPHA_H) { this.dragMode = 3; updateAlpha(mx); return true; }
      return false;
   }

   private void updateSV(double mx, double my) {
      this.pickS = (float) Math.max(0, Math.min(1, (mx - this.pickX) / (PICK_W - 1)));
      this.pickV = 1f - (float) Math.max(0, Math.min(1, (my - this.pickY) / (PICK_H - 1)));
      applyColor();
   }
   private void updateHue(double mx) { this.pickH = (float) Math.max(0, Math.min(1, (mx - this.pickX) / (PICK_W - 1))); applyColor(); }
   private void updateAlpha(double mx) { this.pickA = (float) Math.max(0, Math.min(1, (mx - this.pickX) / (PICK_W - 1))); applyColor(); }

   public boolean method_25403(class_11909 click, double dx, double dy) {
      if (this.editing != null && this.dragMode != 0) {
         double mx = this.uiScale.toLogicalX(click.comp_4798()), my = this.uiScale.toLogicalY(click.comp_4799());
         if (this.dragMode == 1) updateSV(mx, my); else if (this.dragMode == 2) updateHue(mx); else updateAlpha(mx);
         return true;
      }
      return super.method_25403(click, dx, dy);
   }

   public boolean method_25406(class_11909 click) {
      if (this.dragMode != 0) { this.dragMode = 0; return true; }
      return super.method_25406(click);
   }

   public boolean method_25401(double mx, double my, double ha, double va) {
      if (this.editing != null) return true;
      if (this.view == View.LIST) {
         int rows = cfg().hud.hitboxEntityColors.size();
         float max = Math.max(0f, rows * ROW_H - (this.contentH - 28));
         this.listScroll = Math.max(0f, Math.min(max, this.listScroll - (float) va * 24f));
         return true;
      } else {
         float max = Math.max(0f, this.filtered.size() * ADD_ROW_H - (this.contentH - 48));
         this.addScroll = Math.max(0f, Math.min(max, this.addScroll - (float) va * 24f));
         return true;
      }
   }

   public boolean method_25400(class_11905 event) {
      if (this.searchFocused) {
         String s = event.method_74226();
         if (s != null && !s.isEmpty() && this.search.length() < 32) { this.search += s; this.addScroll = 0f; return true; }
      }
      return super.method_25400(event);
   }

   public boolean method_25404(class_11908 input) {
      int key = input.comp_4795();
      if (this.editing != null && key == 256) { applyColor(); ConfigManager.save(cfg()); this.editing = null; return true; }
      if (this.searchFocused) {
         if (key == 259) { if (!this.search.isEmpty()) this.search = this.search.substring(0, this.search.length() - 1); this.addScroll = 0f; return true; }
         if (key == 257 || key == 335 || key == 256) { this.searchFocused = false; return true; }
      }
      return super.method_25404(input);
   }

   public void method_25419() {
      if (this.field_22787 != null) this.field_22787.method_1507(this.parent);
   }
}
