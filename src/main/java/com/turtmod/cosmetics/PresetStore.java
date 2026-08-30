package com.turtmod.cosmetics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Loads and saves the user's {@link SkinPreset}s as JSON next to the skins folder. */
public final class PresetStore {

   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   private PresetStore() {
   }

   private static Path file() {
      return CosmeticManager.getSkinsDirectory().resolveSibling("skin_presets.json");
   }

   public static List<SkinPreset> load() {
      try {
         Path f = file();
         if (!Files.exists(f)) {
            return new ArrayList<>();
         }
         String json = Files.readString(f);
         List<SkinPreset> list = GSON.fromJson(json, new TypeToken<List<SkinPreset>>() {}.getType());
         return list != null ? list : new ArrayList<>();
      } catch (Exception e) {
         return new ArrayList<>();
      }
   }

   public static void save(List<SkinPreset> presets) {
      try {
         Path f = file();
         Files.createDirectories(f.getParent());
         Files.writeString(f, GSON.toJson(presets));
      } catch (Exception ignored) {
      }
   }
}
