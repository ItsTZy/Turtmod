package com.turtmod.ui.config.model;

import java.util.ArrayList;
import java.util.List;

/** A named, collapsible cluster of {@link Option}s within a {@link Category}. */
public final class OptionGroup {
   private final String name;
   private final List<Option<?>> options;
   private boolean expanded;

   private OptionGroup(String name, List<Option<?>> options, boolean expanded) {
      this.name = name;
      this.options = options;
      this.expanded = expanded;
   }

   public String getName() {
      return this.name;
   }

   public List<Option<?>> getOptions() {
      return this.options;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public void toggleExpanded() {
      this.expanded = !this.expanded;
   }

   public static Builder createBuilder(String name) {
      return new Builder(name);
   }

   public static final class Builder {
      private final String name;
      private final List<Option<?>> options = new ArrayList<>();
      private boolean expanded = true;

      private Builder(String name) {
         this.name = name;
      }

      public Builder addOption(Option<?> option) {
         this.options.add(option);
         return this;
      }

      public Builder setExpanded(boolean expanded) {
         this.expanded = expanded;
         return this;
      }

      public OptionGroup build() {
         return new OptionGroup(this.name, this.options, this.expanded);
      }
   }
}
