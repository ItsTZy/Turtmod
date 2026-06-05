package com.turtmod.chat;

import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

/**
 * Searchable chat history. Shows every captured chat line (newest first) in a scrollable
 * turtmod-styled panel with a live search box at the top. Read-only; reads the buffer kept
 * by {@link BetterChatFeature#recordHistory}. Opened via a keybind.
 */
public class TurtChatHistoryScreen extends class_437 {
   private static final Color PANEL_BG     = new Color(1709588, true);
   private static final Color PANEL_BORDER = new Color(9289311, true);
   private static final Color TEXT_MAIN    = new Color(16775399, false);
   private static final Color ACCENT_PINK  = new Color(16752046, false);
   private static final Color ACCENT_GREEN = new Color(9289311, false);
   private static final Color SEARCH_BG    = new Color(2433054, true);

   private final class_437 parent;
   private class_342 searchField;
   private int scroll = 0;
   private int panelX, panelY, panelW, panelH, listTop, listBottom, rowH = 11;

   public TurtChatHistoryScreen(class_437 parent) {
      super(class_2561.method_43470("Chat History"));
      this.parent = parent;
   }

   protected void method_25426() {
      this.panelW = Math.min(360, this.field_22789 - 40);
      this.panelH = Math.min(240, this.field_22790 - 40);
      this.panelX = this.field_22789 / 2 - this.panelW / 2;
      this.panelY = this.field_22790 / 2 - this.panelH / 2;
      this.listTop = this.panelY + 40;
      this.listBottom = this.panelY + this.panelH - 10;

      this.searchField = new class_342(this.field_22793, this.panelX + 10, this.panelY + 22, this.panelW - 20, 14, class_2561.method_43470("Search"));
      this.searchField.method_1880(128);
      this.searchField.method_47404(class_2561.method_43470("Search messages..."));
      this.method_37063(this.searchField);
   }

   private List<String> filtered() {
      String q = this.searchField == null ? "" : this.searchField.method_1882().toLowerCase(Locale.ROOT).trim();
      List<String> all = BetterChatFeature.getHistory();
      if (q.isEmpty()) {
         return all;
      }
      List<String> out = new ArrayList();
      for (String s : all) {
         if (s.toLowerCase(Locale.ROOT).contains(q)) {
            out.add(s);
         }
      }
      return out;
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);

      TurtUIUtils.drawRoundedRect(ctx, this.panelX, this.panelY, this.panelW, this.panelH, 5, PANEL_BG);
      ctx.method_73198(this.panelX, this.panelY, this.panelW, this.panelH, PANEL_BORDER.getRGB());
      ctx.method_25294(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + 1, ACCENT_GREEN.getRGB());

      List<String> rows = this.filtered();
      ctx.method_25303(this.field_22793, "Chat History", this.panelX + 10, this.panelY + 8, TEXT_MAIN.getRGB());
      String count = rows.size() + (BetterChatFeature.getHistory().size() == rows.size() ? "" : "/" + BetterChatFeature.getHistory().size());
      ctx.method_25303(this.field_22793, count + " msgs", this.panelX + this.panelW - 10 - this.field_22793.method_1727(count + " msgs"), this.panelY + 8, ACCENT_PINK.getRGB());

      // Search field background then the widget itself (via super).
      TurtUIUtils.drawRoundedRect(ctx, this.panelX + 8, this.panelY + 20, this.panelW - 16, 18, 3, SEARCH_BG);

      int maxRows = (this.listBottom - this.listTop) / this.rowH;
      this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, rows.size() - maxRows)));
      ctx.method_44379(this.panelX + 6, this.listTop, this.panelX + this.panelW - 6, this.listBottom);
      for (int i = 0; i < maxRows; i++) {
         int idx = i + this.scroll;
         if (idx >= rows.size()) break;
         int y = this.listTop + i * this.rowH;
         if ((idx & 1) == 0) {
            ctx.method_25294(this.panelX + 6, y - 1, this.panelX + this.panelW - 6, y + this.rowH - 1, 0x18FFFFFF);
         }
         String line = rows.get(idx);
         String trimmed = this.field_22793.method_27523(line, this.panelW - 24);
         ctx.method_25303(this.field_22793, trimmed, this.panelX + 10, y, TEXT_MAIN.getRGB());
      }
      ctx.method_44380();

      if (rows.isEmpty()) {
         ctx.method_25300(this.field_22793, "No messages", this.field_22789 / 2, (this.listTop + this.listBottom) / 2 - 4, 0x99AAAAAA);
      }

      super.method_25394(ctx, mx, my, delta);
   }

   public boolean method_25401(double mx, double my, double horiz, double vert) {
      this.scroll -= (int)Math.signum(vert) * 3;
      if (this.scroll < 0) this.scroll = 0;
      return true;
   }

   public boolean method_25404(net.minecraft.class_11908 input) {
      if (input.comp_4795() == 256) {
         this.method_25419();
         return true;
      }
      return super.method_25404(input);
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
