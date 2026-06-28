package com.turtmod.ui.config.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the built category tree for one config screen. Values live on the mod's own config object
 * (each {@link Option}'s getter/setter persists via ConfigManager), so this carries no file I/O of
 * its own — {@link #runSave()} just fires the screen's onSave hook.
 */
public final class LocalConfig {
   private final List<Category> categories;
   private final Runnable onSave;

   private LocalConfig(List<Category> categories, Runnable onSave) {
      this.categories = categories;
      this.onSave = onSave;
   }

   public List<Category> categories() {
      return this.categories;
   }

   /** No-op: values are loaded by ConfigManager into the config object the getters read from. */
   public void load() {
   }

   public void runSave() {
      if (this.onSave != null) {
         this.onSave.run();
      }
   }

   public static Builder createBuilder(String name) {
      return new Builder();
   }

   public static final class Builder {
      private final List<Category> categories = new ArrayList<>();
      private Runnable onSave;

      private Builder() {
      }

      /** Kept for API symmetry; the path is unused (values persist via ConfigManager). */
      public Builder path(Path path) {
         return this;
      }

      public Builder category(Category category) {
         this.categories.add(category);
         return this;
      }

      public Builder onSave(Runnable onSave) {
         this.onSave = onSave;
         return this;
      }

      public LocalConfig build() {
         return new LocalConfig(this.categories, this.onSave);
      }
   }
}
