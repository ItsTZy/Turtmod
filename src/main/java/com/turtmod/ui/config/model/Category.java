package com.turtmod.ui.config.model;

import java.util.ArrayList;
import java.util.List;

/** A top-level config tab containing groups of options (and optionally ungrouped options). */
public record Category(String name, List<OptionGroup> optionGroups, List<Option<?>> options) {

   public static Builder createBuilder(String name) {
      return new Builder(name);
   }

   public static final class Builder {
      private final String name;
      private final List<OptionGroup> groups = new ArrayList<>();
      private final List<Option<?>> options = new ArrayList<>();

      private Builder(String name) {
         this.name = name;
      }

      public Builder group(OptionGroup group) {
         this.groups.add(group);
         return this;
      }

      public Builder addOption(Option<?> option) {
         this.options.add(option);
         return this;
      }

      public Category build() {
         return new Category(this.name, this.groups, this.options);
      }
   }
}
