package com.turtmod.config;

import com.turtmod.ui.Palette;
import com.turtmod.ui.TurtUIUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_10799;
import net.minecraft.class_1109;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_7923;

/**
 * Generic searchable picker GUI: lists every id from a registry (one row each), and clicking a row
 * toggles that id's membership in a backing {@code List<String>} which is saved to config. Used by
 * the Hide Particles and Mute Sounds modules so the user can pick exact ids from a scrollable list
 * instead of typing commands.
 */
public class RegistryPickerScreen extends class_437 {
   private final class_437 parent;
   private final String titleText;
   private final String subtitle;
   private final List<String> allIds;     // every available id (sorted)
   private final List<String> selected;   // backing config list (mutated + saved live)
   private final Runnable onSave;
   private final PreviewType preview;

   /** What to preview when a row is clicked: play the sound, spawn the particle, or nothing. */
   public enum PreviewType {
      NONE,
      SOUND,
      PARTICLE
   }

   private final List<String> filtered = new ArrayList<>();
   // particle id -> ordered list of texture frame identifiers (parsed from the particle's JSON)
   private static final Map<String, List<class_2960>> PARTICLE_FRAMES = new HashMap<>();
   private class_342 search;
   private float scrollY = 0.0F;
   private float openFade = 0.0F;
   private long lastFrameNs = System.nanoTime();
   private int listTop;
   private int listBottom;
   private int listLeft;
   private int listW;
   private final int rowH;       // taller rows for particles so each can show a live thumbnail
   private final int thumb;      // particle thumbnail size (0 if not a particle picker)

   private static final Color ACCENT = Palette.GREEN;

   public RegistryPickerScreen(class_437 parent, String title, String subtitle,
                               List<String> allIds, List<String> selected, Runnable onSave, PreviewType preview) {
      super(class_2561.method_43470(title));
      this.parent = parent;
      this.titleText = title;
      this.subtitle = subtitle;
      this.allIds = allIds;
      this.selected = selected;
      this.onSave = onSave;
      this.preview = preview == null ? PreviewType.NONE : preview;
      this.thumb = this.preview == PreviewType.PARTICLE ? 18 : 0;
      this.rowH = this.preview == PreviewType.PARTICLE ? 22 : 14;
   }

   protected void method_25426() {
      int panelW = Math.min(360, this.field_22789 - 40);
      int panelX = (this.field_22789 - panelW) / 2;
      this.listLeft = panelX + 10;
      this.listW = panelW - 20;
      this.listTop = 64;
      this.listBottom = this.field_22790 - 40;

      this.search = new class_342(this.field_22793, this.listLeft, 40, this.listW, 16, class_2561.method_43470("Search"));
      this.search.method_47404(class_2561.method_43470("Search..."));
      this.search.method_1863(s -> this.rebuild());
      this.method_37063(this.search);

      int btnW = (this.listW - 8) / 2;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Clear All"), b -> {
         this.selected.clear();
         this.onSave.run();
         this.rebuild();
      }).method_46434(this.listLeft, this.field_22790 - 28, btnW, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"), b -> this.method_25419())
         .method_46434(this.listLeft + this.listW - btnW, this.field_22790 - 28, btnW, 20).method_46431());

      this.rebuild();
   }

   private void rebuild() {
      this.filtered.clear();
      String q = this.search == null ? "" : this.search.method_1882().trim().toLowerCase(Locale.ROOT);
      for (String id : this.allIds) {
         if (q.isEmpty() || id.toLowerCase(Locale.ROOT).contains(q)) {
            this.filtered.add(id);
         }
      }
      this.clampScroll();
   }

   private int maxScroll() {
      return Math.max(0, this.filtered.size() * this.rowH - (this.listBottom - this.listTop));
   }

   private void clampScroll() {
      this.scrollY = Math.max(0.0F, Math.min(this.scrollY, (float)this.maxScroll()));
   }

   public void method_25394(class_332 ctx, int mx, int my, float delta) {
      long now = System.nanoTime();
      this.openFade = TurtUIUtils.lerp01(this.openFade, 1f, Math.min((now - this.lastFrameNs) / 1_000_000_000f, 0.1f), 12f);
      this.lastFrameNs = now;
      TurtUIUtils.drawMenuBackdrop(ctx, this.field_22789, this.field_22790);
      ctx.method_25300(this.field_22793, this.titleText, this.field_22789 / 2, 14, -1);
      if (this.subtitle != null) {
         ctx.method_25300(this.field_22793, this.subtitle, this.field_22789 / 2, 26, -7763575);
      }
      ctx.method_51433(this.field_22793, "Selected: " + this.selected.size(), this.listLeft, this.listTop - 11, ACCENT.getRGB(), false);

      ctx.method_44379(this.listLeft, this.listTop, this.listLeft + this.listW, this.listBottom);
      int y = this.listTop - Math.round(this.scrollY);
      int textY = (this.rowH - 8) / 2;
      for (String id : this.filtered) {
         if (y + this.rowH >= this.listTop && y <= this.listBottom) {
            boolean on = this.selected.contains(id);
            boolean hov = mx >= this.listLeft && mx <= this.listLeft + this.listW && my >= y && my <= y + this.rowH;
            if (hov) {
               ctx.method_25294(this.listLeft, y, this.listLeft + this.listW, y + this.rowH, 0x22FFFFFF);
            }
            int boxX = this.listLeft + 2;
            int boxY = y + (this.rowH - 10) / 2;
            int box = 10;
            ctx.method_25294(boxX, boxY, boxX + box, boxY + box, on ? ACCENT.getRGB() : 0x44FFFFFF);
            if (on) {
               ctx.method_51433(this.field_22793, "✔", boxX + 1, boxY + 1, 0xFF000000, false);
            }
            ctx.method_51433(this.field_22793, id, this.listLeft + 16, y + textY, on ? -1 : -5592406, false);
            if (this.preview == PreviewType.PARTICLE) {
               // Live animated thumbnail per row.
               int tx = this.listLeft + this.listW - this.thumb - 4;
               int ty = y + (this.rowH - this.thumb) / 2;
               drawParticleThumb(ctx, id, tx, ty, this.thumb);
            } else if (this.preview == PreviewType.SOUND) {
               int pvX = this.listLeft + this.listW - 14;
               boolean hovPv = mx >= pvX - 2 && mx <= pvX + 10 && my >= y && my <= y + this.rowH;
               ctx.method_51433(this.field_22793, "▶", pvX, y + textY, hovPv ? ACCENT.getRGB() : -5592406, false);
            }
         }
         y += this.rowH;
      }
      ctx.method_44380();
      this.drawScrollbar(ctx);
      TurtUIUtils.drawOpenFade(ctx, this.field_22789, this.field_22790, this.openFade);
      super.method_25394(ctx, mx, my, delta);
   }

   /** Draws an animated particle texture thumbnail with a checker backing so it's always visible. */
   private void drawParticleThumb(class_332 ctx, String id, int x, int y, int size) {
      ctx.method_25294(x, y, x + size, y + size, 0xFF000000);
      drawChecker(ctx, x, y, size, size);
      List<class_2960> frames = particleFrames(id);
      if (!frames.isEmpty()) {
         int frame = (int)((System.currentTimeMillis() / 90L) % (long)frames.size());
         ctx.method_25302(class_10799.field_56883, frames.get(frame), x, y, 0.0F, 0.0F, size, size, 16, 16, 16, 16);
      }
      TurtUIUtils.drawBorder(ctx, x, y, size, size, new Color(255, 255, 255, 40));
   }

   private void drawScrollbar(class_332 ctx) {
      int max = this.maxScroll();
      if (max <= 0) {
         return;
      }
      int viewportH = this.listBottom - this.listTop;
      int trackX = this.listLeft + this.listW - 2;
      int thumbH = Math.max(16, Math.round((float)viewportH * viewportH / (float)(this.filtered.size() * this.rowH)));
      int thumbY = this.listTop + Math.round(this.scrollY / max * (viewportH - thumbH));
      ctx.method_25294(trackX, this.listTop, trackX + 2, this.listBottom, 0x33FFFFFF);
      ctx.method_25294(trackX, thumbY, trackX + 2, thumbY + thumbH, ACCENT.getRGB());
   }

   public boolean method_25402(class_11909 click, boolean bl) {
      if (super.method_25402(click, bl)) {
         return true;
      }
      int button = click.method_74245();
      double mxx = click.comp_4798();
      double myy = click.comp_4799();
      if (button == 0 && mxx >= this.listLeft && mxx <= this.listLeft + this.listW && myy >= this.listTop && myy <= this.listBottom) {
         int idx = (int)((myy - this.listTop + this.scrollY) / this.rowH);
         if (idx >= 0 && idx < this.filtered.size()) {
            String id = this.filtered.get(idx);
            // Sounds: the right-side ▶ plays the sound; the rest of the row toggles. Particles show
            // an always-on thumbnail instead, so the whole row just toggles.
            int pvX = this.listLeft + this.listW - 14;
            if (this.preview == PreviewType.SOUND && mxx >= pvX - 2) {
               this.previewEntry(id);
               return true;
            }
            if (!this.selected.remove(id)) {
               this.selected.add(id);
            }
            this.onSave.run();
            return true;
         }
      }
      return false;
   }

   /** Play the clicked sound (particles use an inline thumbnail instead). */
   private void previewEntry(String id) {
      if (this.preview != PreviewType.SOUND) {
         return;
      }
      class_310 mc = class_310.method_1551();
      class_2960 rid = class_2960.method_12829(id);
      if (mc == null || rid == null) {
         return;
      }
      try {
         class_7923.field_41172.method_10223(rid).ifPresent(entry ->
            mc.method_1483().method_4873(class_1109.method_4758(entry.comp_349(), 1.0F)));
      } catch (Throwable ignored) {
         // Preview is best-effort; never let a bad entry break the picker.
      }
   }

   /**
    * Texture frame identifiers for a particle. Reads the particle's {@code particles/<id>.json}
    * "textures" list, keeping only frames whose PNG actually exists; if none resolve (the JSON is
    * missing or the particle has no static texture) it falls back to {@code particle/<id>(_N).png}
    * naming. Cached per id.
    */
   private static List<class_2960> particleFrames(String id) {
      return PARTICLE_FRAMES.computeIfAbsent(id, key -> {
         List<class_2960> frames = new ArrayList<>();
         class_310 mc = class_310.method_1551();
         class_2960 pid = class_2960.method_12829(key);
         if (pid == null || mc == null) {
            return frames;
         }
         net.minecraft.class_3300 rm = mc.method_1478();
         // 1) From the particle definition JSON.
         class_2960 jsonId = class_2960.method_60655(pid.method_12836(), "particles/" + pid.method_12832() + ".json");
         rm.method_14486(jsonId).ifPresent(res -> {
            try (java.io.BufferedReader reader = res.method_43039()) {
               com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
               if (obj.has("textures")) {
                  for (com.google.gson.JsonElement el : obj.getAsJsonArray("textures")) {
                     class_2960 t = class_2960.method_12829(el.getAsString());
                     if (t != null) {
                        class_2960 tex = class_2960.method_60655(t.method_12836(), "textures/particle/" + t.method_12832() + ".png");
                        if (rm.method_14486(tex).isPresent()) {
                           frames.add(tex);
                        }
                     }
                  }
               }
            } catch (Exception ignored) {
            }
         });
         // 2) Fallback: convention-named textures (particle.png and particle_0.._15.png).
         if (frames.isEmpty()) {
            class_2960 single = class_2960.method_60655(pid.method_12836(), "textures/particle/" + pid.method_12832() + ".png");
            if (rm.method_14486(single).isPresent()) {
               frames.add(single);
            } else {
               for (int i = 0; i < 16; i++) {
                  class_2960 numbered = class_2960.method_60655(pid.method_12836(), "textures/particle/" + pid.method_12832() + "_" + i + ".png");
                  if (rm.method_14486(numbered).isPresent()) {
                     frames.add(numbered);
                  } else if (i > 0) {
                     break;
                  }
               }
            }
         }
         return frames;
      });
   }

   /** Light/dark checkerboard so a particle's true colours read against any background. */
   private static void drawChecker(class_332 ctx, int x, int y, int w, int h) {
      int cell = 4;
      for (int row = 0; row * cell < h; row++) {
         for (int col = 0; col * cell < w; col++) {
            if (((row + col) & 1) == 0) {
               continue;
            }
            int x0 = x + col * cell;
            int y0 = y + row * cell;
            ctx.method_25294(x0, y0, Math.min(x0 + cell, x + w), Math.min(y0 + cell, y + h), 0x22FFFFFF);
         }
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.maxScroll() > 0 && mouseY >= this.listTop && mouseY <= this.listBottom) {
         this.scrollY -= (float)verticalAmount * 18.0F;
         this.clampScroll();
         return true;
      }
      return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public void method_25419() {
      this.onSave.run();
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   /** Don't pause the (singleplayer) world while open, so previewed particles tick and are visible. */
   public boolean method_25421() {
      return false;
   }
}
