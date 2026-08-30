package com.turtmod.ui;

import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_3417;

/**
 * Centralised UI sound feedback so every TurtMod screen sounds consistent. Historically the same
 * {@code ui.button.click} ({@link class_3417#field_15015}) play snippet was copy-pasted into
 * {@link TurtUIButton}, {@link TurtUICheckbox} and others, while the native config screen made no
 * sound at all. Route everything through here; pitch makes the different actions audibly distinct.
 */
public final class TurtSounds {
   private TurtSounds() {
   }

   /** Module / setting toggle: brighter when turning ON, duller when turning OFF. */
   public static void toggle(boolean on) {
      play(on ? 1.0F : 0.85F);
   }

   /** Switching a category tab. */
   public static void tab() {
      play(1.1F);
   }

   /** Small confirming tick (slider commit, preset/swatch pick, pin toggle, HUD snap). */
   public static void tick() {
      play(0.9F);
   }

   /** Apply / Done / Reset — a slightly higher, satisfying confirm. */
   public static void confirm() {
      play(1.15F);
   }

   /** Generic click at default pitch. */
   public static void click() {
      play(1.0F);
   }

   public static void play(float pitch) {
      try {
         class_310 mc = class_310.method_1551();
         if (mc != null && mc.method_1483() != null) {
            mc.method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, pitch));
         }
      } catch (Exception ignored) {
      }
   }
}
