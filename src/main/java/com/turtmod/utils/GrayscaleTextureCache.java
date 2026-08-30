package com.turtmod.utils;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;

public class GrayscaleTextureCache {
   private static final Map<class_2960, class_2960> cache = new HashMap();

   public static class_2960 get(class_2960 original) {
      return original;
   }

   public static void clear() {
      cache.clear();
   }
}
