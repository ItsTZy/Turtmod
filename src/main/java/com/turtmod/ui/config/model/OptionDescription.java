package com.turtmod.ui.config.model;

import java.util.function.Supplier;

/** One-line description shown as a hover tooltip for an {@link Option}. */
public final class OptionDescription {
   private final Supplier<String> textSupplier;

   private OptionDescription(Supplier<String> textSupplier) {
      this.textSupplier = textSupplier;
   }

   public static OptionDescription ofOrderedString(Supplier<String> textSupplier) {
      return new OptionDescription(textSupplier);
   }

   public Supplier<String> getStringSupplier() {
      return this.textSupplier;
   }
}
