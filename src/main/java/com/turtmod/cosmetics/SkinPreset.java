package com.turtmod.cosmetics;

/**
 * A saved look: a skin file + model type + an optional cape, under a name. Presets are what the rebuilt
 * Skin Changer manages — you pick or create one, edit its skin/model/cape, then apply the whole thing.
 */
public class SkinPreset {
   public String name = "New Preset";
   public String skinFile = null;   // absolute path to the .png in the skins folder, or null if unset
   public boolean slim = false;     // false = classic, true = slim
   public String capeId = null;     // Mojang cape id to wear, or null for none
   public String capeName = null;   // display name of the chosen cape

   public SkinPreset() {
   }

   public SkinPreset(String name) {
      this.name = name;
   }
}
