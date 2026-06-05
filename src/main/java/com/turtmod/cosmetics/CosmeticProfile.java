package com.turtmod.cosmetics;

public class CosmeticProfile {
   public String name;
   public String skinPath;
   public boolean enableSkin;

   public CosmeticProfile() {
      this.name = "New Profile";
      this.skinPath = "";
      this.enableSkin = true;
   }

   public CosmeticProfile(String name, String skinPath) {
      this.name = name;
      this.skinPath = skinPath;
      this.enableSkin = true;
   }

   public boolean isValid() {
      return this.name != null && !this.name.isEmpty();
   }
}
