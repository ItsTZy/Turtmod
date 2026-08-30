package com.turtmod.ui.config.model;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A single config entry bound to a live getter/setter on the mod's config object. The screen renders
 * a widget based on {@link #getType()}: Boolean→toggle, Integer/Float→slider, Enum→cycler,
 * {@link ConfigColor}→colour picker, Runnable→button. {@code min}/{@code max}/{@code increment} are
 * only used for numeric options.
 */
public final class Option<T> {
   private final String name;
   private final Supplier<T> getter;
   private final Consumer<T> setter;
   private final Class<?> type;
   private final T min;
   private final T max;
   private final T increment;
   private OptionDescription description;
   /** For colour options: the gradient-store key so the picker can offer a Solid/Gradient editor. */
   private String gradientKey;

   public Option(String name, Supplier<T> getter, Consumer<T> setter, Class<?> type, T min, T max, T increment) {
      this.name = name;
      this.getter = getter;
      this.setter = setter;
      this.type = type;
      this.min = min;
      this.max = max;
      this.increment = increment;
   }

   public String getName() {
      return this.name;
   }

   @SuppressWarnings("unchecked")
   public Class<T> getType() {
      return (Class<T>) this.type;
   }

   public T getValue() {
      return this.getter.get();
   }

   @SuppressWarnings("unchecked")
   public void setValue(Object value) {
      if (this.setter != null && value != null && this.type.isInstance(value)) {
         this.setter.accept((T) value);
      }
   }

   public T getMin() {
      return this.min;
   }

   public T getMax() {
      return this.max;
   }

   public T getIncrement() {
      return this.increment;
   }

   public OptionDescription getDescription() {
      return this.description;
   }

   public Option<T> description(OptionDescription description) {
      this.description = description;
      return this;
   }

   public String getGradientKey() {
      return this.gradientKey;
   }

   public Option<T> gradientKey(String key) {
      this.gradientKey = key;
      return this;
   }
}
